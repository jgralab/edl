package de.uni_koblenz.edl.parser.debug;

import java.io.IOException;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.TreeTraverser;

/**
 * Creates a {@link String} representation of the internal parse forest.
 */
public class ParseForest2String extends ParseForest2Output {

	/**
	 * Stores the number of the indention steps. At least one indention is
	 * always done.
	 */
	private int indention = 1;

	/**
	 * Creates a new {@link ParseForest2String} instance.
	 * {@link ParseForest2Output#MARK_OF_ERROR} is set to "&gt;&gt;&gt;&gt;".
	 * 
	 * @param traverser
	 *            {@link TreeTraverser} which gives access to the rules and the
	 *            content of the currently parsed file.
	 */
	public ParseForest2String(TreeTraverser traverser) {
		super(traverser);
		MARK_OF_ERROR = ">>>>";
	}

	/**
	 * @return {@link String} which is the representation of the internal parse
	 *         forest <code>root</code>.
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
		StringBuilder content = new StringBuilder("parsed file: "
				+ treeTraverser.getStack().getNameOfParsedFile()
				+ "\nparsed content: line " + initialPosition.getFirstLine()
				+ " column " + initialPosition.getFirstColumn() + " length "
				+ initialPosition.getLength() + "\n");
		try {
			traverse(content, root);
		} catch (IOException e) {
			e.printStackTrace();
			return "An error occured:\n" + e.getMessage();
		}
		return content.toString();
	}

	/**
	 * Additionally {@link #indention} is incremented if <code>node</code> has
	 * to be printed.
	 */
	@Override
	protected void enterNode(AbstractParseNode node) {
		super.enterNode(node);
		if (isPrintable(node)) {
			indention++;
		}
	}

	/**
	 * Additionally {@link #indention} is decremented if <code>node</code> has
	 * to be printed.
	 */
	@Override
	protected void leaveNode(AbstractParseNode node) {
		super.leaveNode(node);
		if (isPrintable(node)) {
			indention--;
		}
	}

	@Override
	protected void convertNode(Appendable appendable, AbstractParseNode node)
			throws IOException {
		// print indention for the current node
		if (isErrorNode(node)) {
			// node caused an exception or is an ambiguity node
			appendable.append(MARK_OF_ERROR);
			indent(appendable, indention - 1);
		} else {
			indent(appendable, indention);
		}
		// print node
		if (node.isAmbNode()) {
			// node is an ambiguity node
			appendable.append("ambiguity");
		} else {
			Rule rule = treeTraverser.getRule(node.getLabel());
			if (rule == null) {
				// a recognition of a character was found
				appendable.append("\'");
				quote(appendable,
						treeTraverser.getStack().getLexem(positions.peek()));
				appendable.append("\'");
			} else if (isVerbose) {
				// print rule as it is
				appendable.append(rule.toString().trim());
			} else {
				// print rule as it was defined in the grammar
				appendable.append(rule.getDefinedRepresentation().trim());
			}
		}
		// print position information
		Position pos = positions.peek();
		appendable.append(" (line " + pos.getFirstLine() + ", column "
				+ pos.getFirstColumn() + ", length " + pos.getLength() + ")\n");
	}

	@Override
	protected void convertLiteralNode(Appendable appendable,
			AbstractParseNode node) throws IOException {
		// print indention for the current node
		if (isErrorNode(node)) {
			// node caused an exception or is an ambiguity node
			appendable.append(MARK_OF_ERROR);
			indent(appendable, indention - 1);
		} else {
			indent(appendable, indention);
		}
		// print recognized lexem
		appendable.append("\'");
		quote(appendable, treeTraverser.getStack().getLexem(positions.peek()));
		appendable.append("\'");
		// print position information
		Position pos = positions.peek();
		appendable.append(" (line " + pos.getFirstLine() + ", column "
				+ pos.getFirstColumn() + ", length " + pos.getLength() + ")\n");
	}

	/**
	 * Following characters are quoted in <code>lexem</code>:
	 * <code>\n \r \t</code><br>
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
				appendable.append("\\n");
				break;
			case '\r':
				appendable.append("\\r");
				break;
			case '\t':
				appendable.append("\\t");
				break;
			default:
				appendable.append(c);
			}
		}
	}

	/**
	 * Appends for each indention step four whitespaces to
	 * <code>appendable</code>
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the indentions will be appended
	 * @param numberOfIndentions
	 *            <code>int</code> the number of indention steps
	 * @throws IOException
	 *             if the appending to <code>appendable</code> causes an
	 *             {@link IOException}
	 */
	private void indent(Appendable appendable, int numberOfIndentions)
			throws IOException {
		for (int i = 0; i < numberOfIndentions; i++) {
			appendable.append("    ");
		}
	}

}
