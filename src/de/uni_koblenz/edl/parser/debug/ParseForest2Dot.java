package de.uni_koblenz.edl.parser.debug;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Stack;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.TreeTraverser;

/**
 * Creates a graphical representation of the internal parse forest. The external
 * program dot which is part of graphviz must be installed and be known at the
 * command line. You can find graphviz under: <a
 * href="http://www.graphviz.org/">http://www.graphviz.org/</a>
 */
public class ParseForest2Dot extends ParseForest2Output {

	/**
	 * The format of the graphic which will be created. The user must check, if
	 * it is supported by dot.
	 */
	private final String outputFormat;

	/**
	 * The unique id number for the created vertices in the dot file.
	 */
	private int uniqueID = 1;

	/**
	 * This {@link Stack} stores the ids of the created vertices in the dot
	 * file, which represent all nodes from the current node to the root of the
	 * internal parse forest.
	 */
	private final Stack<String> idStack;

	/**
	 * Marks recognized lexems in the parse tree.
	 */
	private final String MARK_OF_LEXEM;

	/**
	 * Marks applied context free rules.
	 */
	private final String MARK_OF_CONTEXT_FREE_RULES;

	/**
	 * Marks applied lexical rules.
	 */
	private final String MARK_OF_LEXICAL_RULES;

	/**
	 * Creates a new {@link ParseForest2Dot} instance.
	 * {@link ParseForest2Output#MARK_OF_ERROR} is set to fill the marked nodes
	 * with red color.
	 * 
	 * @param traverser
	 *            {@link TreeTraverser} which gives access to the rules and the
	 *            content of the currently parsed file.
	 * @param outputFormat
	 *            {@link String} the format of the generated graphical
	 *            representation of the internal parse forest
	 */
	public ParseForest2Dot(TreeTraverser traverser, String outputFormat) {
		super(traverser);
		this.outputFormat = outputFormat;
		MARK_OF_ERROR = " style=\"filled\" fillcolor=\"red\"";
		MARK_OF_LEXEM = " style=\"filled\" fillcolor=\"yellow\"";
		MARK_OF_CONTEXT_FREE_RULES = " style=\"filled\" fillcolor=\"aquamarine\"";
		MARK_OF_LEXICAL_RULES = " style=\"filled\" fillcolor=\"chartreuse\"";
		idStack = new Stack<String>();
	}

	/**
	 * Tries to generate the graphical representation in the execution path. If
	 * this fails it is generated in the temp directory of the operating system.
	 * 
	 * @return {@link String} which contains the path to the generated graphical
	 *         representation
	 */
	@Override
	public String convertParseForest(AbstractParseNode root, boolean isVerbose,
			AbstractParseNode nodeWithException) {
		if (GraphBuilderBaseImpl.printDebugInformationToTheConsole) {
			System.out
					.println("\tCreating visualization of internal parse forest....");
		}
		this.isVerbose = isVerbose;
		this.nodeWithException = nodeWithException;

		Position initialPosition = treeTraverser.getStack()
				.getInitialPosition();

		String outputPath = "./";
		String outputFileName = treeTraverser.getStack().getNameOfParsedFile()
				.replaceAll("\\W", "_")
				+ "-line"
				+ initialPosition.getFirstLine()
				+ "column"
				+ initialPosition.getFirstColumn()
				+ "length"
				+ initialPosition.getLength();

		// convert .dot to graphic
		File graphicalOutput = new File(outputPath + outputFileName + "."
				+ outputFormat);
		try {
			graphicalOutput.createNewFile();
		} catch (IOException e) {
			// file could not be created in the call position
			// create file in the temp directory
			outputPath = System.getProperty("java.io.tmpdir");
			graphicalOutput = new File(outputPath + outputFileName + ".dot");
		}
		String result = runDot(graphicalOutput, root);
		if (result != null) {
			return result;
		}

		return "\tSee output at " + graphicalOutput.getAbsolutePath();
	}

	/**
	 * Runs dot which creates the graphic from the .dot file. If an exception
	 * occurs the error message is returned. Otherwise <code>null</code> is
	 * returned.
	 * 
	 * @param graphicalOutput
	 *            {@link File} which is the created graphical file
	 * @param root
	 *            {@link AbstractParseNode} which is the root of the internal
	 *            parse forest represented in a graphic
	 * @return {@link String} which is the message of an occurred
	 *         {@link Exception}. Otherwise <code>null</code> is returned.
	 */
	private String runDot(File graphicalOutput, final AbstractParseNode root) {
		try {
			final Process process = Runtime.getRuntime().exec(
					new String[] { "dot", "-T" + outputFormat, "-o",
							graphicalOutput.toString() }, null, new File("./"));
			new Thread() {
				@Override
				public void run() {
					try {
						PrintStream dotWriter = new PrintStream(
								process.getOutputStream());
						dotWriter.print("digraph InternalParseForest{\n");
						dotWriter.print("node[shape=\"record\"];\n");
						traverse(dotWriter, root);
						dotWriter.print("}");
						dotWriter.flush();
						dotWriter.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}.start();
			BufferedInputStream errorstream = new BufferedInputStream(
					process.getErrorStream());
			BufferedInputStream inputstream = new BufferedInputStream(
					process.getInputStream());

			process.waitFor();

			int c = -1;
			while ((c = inputstream.read()) != -1) {
				System.out.print((char) c);
			}

			if (process.exitValue() != 0) {
				StringBuilder errorMessage = new StringBuilder();
				int e = -1;
				while ((e = errorstream.read()) != -1) {
					errorMessage.append((char) e);
				}
				return "dot caused an error:\n" + errorMessage.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "An error occured:\n" + e.getMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "An error occured:\n" + e.getMessage();
		}
		return null;
	}

	/**
	 * Additionally updates {@link #idStack} if <code>node</code> is printable.
	 */
	@Override
	protected void enterNode(AbstractParseNode node) {
		super.enterNode(node);
		if (isPrintable(node)) {
			idStack.push(getNewId());
		}
	}

	/**
	 * @return {@link String} the newly created uniqze id
	 */
	private String getNewId() {
		return "n" + uniqueID++;
	}

	/**
	 * Additionally updates {@link #idStack} if <code>node</code> is printable.
	 */
	@Override
	protected void returnToNode(AbstractParseNode node,
			AbstractParseNode child, boolean wasLastChildTraversed) {
		super.returnToNode(node, child, wasLastChildTraversed);
		if (isPrintable(child)) {
			idStack.pop();
		}
	}

	@Override
	protected void convertNode(Appendable appendable, AbstractParseNode node)
			throws IOException {
		if (idStack.size() > 1) {
			// the current element has a parent
			// print an edge
			appendable.append(idStack.get(idStack.size() - 2) + "->"
					+ idStack.peek() + ";\n");
		}
		appendable.append(idStack.peek() + " [label=\"{");
		if (node.isAmbNode()) {
			appendable.append("ambiguity");
		} else {
			printRule(appendable, node);
		}
		appendable.append("|");
		printPositionInformantion(appendable, positions.peek());
		appendable.append("}\"");
		if (isErrorNode(node)) {
			appendable.append(MARK_OF_ERROR);
		} else if (treeTraverser.getRule(node.getLabel()) == null) {
			// a input char is recognized
			appendable.append(MARK_OF_LEXEM);
		} else {
			Rule rule = treeTraverser.getRule(node.getLabel());
			if (rule.isContextFree()) {
				appendable.append(MARK_OF_CONTEXT_FREE_RULES);
			} else {
				appendable.append(MARK_OF_LEXICAL_RULES);
			}
		}
		appendable.append("];\n");
	}

	@Override
	protected void convertLiteralNode(Appendable appendable,
			AbstractParseNode node) throws IOException {
		String newId = getNewId();
		appendable.append(idStack.peek() + "->" + newId + ";\n");
		appendable.append(newId + " [label=\"{\\\'");
		quote(appendable, treeTraverser.getStack().getLexem(positions.peek()));
		appendable.append("\\\'|");
		printPositionInformantion(appendable, positions.peek());
		appendable.append("}\"");
		if (isErrorNode(node)) {
			appendable.append(MARK_OF_ERROR);
		} else {
			appendable.append(MARK_OF_LEXEM);
		}
		appendable.append("];\n");
	}

	/**
	 * Appends the {@link String} representation of the applied rule of
	 * <code>node</code> to <code>appendable</code>. In the verbose mode the
	 * rule is given as it was generated by the BNF transformation. Otherwise it
	 * is given how it was defined in the grammar.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the {@link String} representation
	 *            of the rule is appended
	 * @param node
	 *            {@link AbstractParseNode} which stands for the applied rule.
	 * @throws IOException
	 */
	private void printRule(Appendable appendable, AbstractParseNode node)
			throws IOException {
		Rule rule = treeTraverser.getRule(node.getLabel());
		if (rule == null) {
			// the current rule is a leaf and recognizes an input char
			appendable.append("\\\'");
			quote(appendable,
					treeTraverser.getStack().getLexem(positions.peek()));
			appendable.append("\\\'");
		} else {
			quote(appendable,
					isVerbose ? rule.toString() : rule
							.getDefinedRepresentation());
		}
	}

	/**
	 * Following characters are quoted in <code>lexem</code>:
	 * <code>\n \r \t \ " | { }</code><br>
	 * <code>&lt;</code> will be transformed into <code>&amp;lt;</code><br>
	 * <code>&gt;</code> will be transformed into <code>&amp;gt;</code><br>
	 * a control character is converted into <code>\DecimalValue</code><br>
	 * The quoted lexem is appended to <code>appendable</code>.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the quoted lexem will be added
	 * @param lexem
	 *            {@link String} which characters will be possibly quoted
	 * @throws IOException
	 */
	private void quote(Appendable appendable, String lexem) throws IOException {
		for (char c : lexem.toCharArray()) {
			switch (c) {
			case '\n':
				appendable.append("\\\\n");
				break;
			case '\r':
				appendable.append("\\\\r");
				break;
			case '\t':
				appendable.append("\\\\t");
				break;
			case '\\':
				appendable.append("\\\\");
				break;
			case '"':
				appendable.append("\\\"");
				break;
			case '|':
				appendable.append("\\|");
				break;
			case '{':
				appendable.append("\\{");
				break;
			case '}':
				appendable.append("\\}");
				break;
			case '<':
				appendable.append("&lt;");
				break;
			case '>':
				appendable.append("&gt;");
				break;
			default:
				if (String.valueOf(c).matches("^[\\p{Cntrl}]$")) {
					appendable.append("\\\\" + (int) c);
				} else {
					appendable.append(c);
				}
			}
		}
	}

	/**
	 * Appends the first line number, the column and the length of
	 * <code>position</code> to <code>appendable</code>.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the position information is added
	 * @param position
	 *            {@link Position} which will be appended to
	 *            <code>appendable</code>
	 * @throws IOException
	 */
	private void printPositionInformantion(Appendable appendable,
			Position position) throws IOException {
		appendable.append("line = " + position.getFirstLine() + "\\n");
		appendable.append("column = " + position.getFirstColumn() + "\\n");
		appendable.append("length = " + position.getLength());
	}

}
