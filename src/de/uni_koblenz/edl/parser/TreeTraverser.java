package de.uni_koblenz.edl.parser;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.client.AbstractParseNode;
import org.spoofax.jsglr.client.ITreeBuilder;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.terms.ParseError;

import de.uni_koblenz.edl.GraphBuilder;
import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.edl.parser.debug.ParseForest2Dot;
import de.uni_koblenz.edl.parser.debug.ParseForest2Output;
import de.uni_koblenz.edl.parser.debug.ParseForest2String;
import de.uni_koblenz.edl.parser.stack.Stack;
import de.uni_koblenz.edl.parser.stack.elements.StackElement;

/**
 * This class implements the {@link ITreeBuilder} interface. Instances of this
 * class may be registered at the interpreter which parses the input. The
 * resulting internal parse forest is traversed via depth first strategy. The
 * {@link GraphBuilder} is called to execute the corresponding semantic actions.
 */
public class TreeTraverser implements ITreeBuilder {

	/**
	 * The identifying number of the first rule.
	 */
	private int offsetOfRuleIndex;

	/**
	 * An array which stores all {@link Rule}s which are known to the used
	 * grammar.
	 */
	private Rule[] rules;

	/**
	 * This {@link Stack} is used during the depth first traversal of the
	 * internal parse forest.
	 */
	private Stack stack;

	/**
	 * The {@link GraphBuilder} which is used to execute the corresponding
	 * semantic actions. Further more it stores the command line arguments.
	 */
	private final InternalGraphBuilder graphBuilder;

	private long startTimeOfParsing;

	/**
	 * Creates a new {@link TreeTraverser}.
	 * 
	 * @param builder
	 *            {@link GraphBuilder} which is used to execute the semantic
	 *            actions.
	 */
	public TreeTraverser(InternalGraphBuilder builder) {
		assert builder != null;
		graphBuilder = builder;
	}

	/**
	 * This Method is called by the interpreter of Stratego/XT to inform the
	 * TreeBuilder about: (see parameter description)<br>
	 * In this implementation it creates an {@link Rule}-Array for all rules.
	 * 
	 * @param table
	 *            {@link ParseTable} which is used by the interpreter
	 * @param productionCount
	 *            <code>int</code> ???
	 * @param labelStart
	 *            <code>int</code> the number of the first used rule
	 * @param labelEnd
	 *            <code>int</code> the number of the last used rule
	 */
	@Override
	public void initializeTable(ParseTable table, int productionCount,
			int labelStart, int labelEnd) {
		offsetOfRuleIndex = labelStart;
		rules = new Rule[labelEnd - labelStart];
	}

	/**
	 * This method is called for each rule defined in the grammar and created
	 * during the BNF transformation.
	 * 
	 * @param labelNumber
	 *            <code>int</code> the number of rule
	 *            <code>parseTreeProduction</code>
	 * @param parseTreeProduction
	 *            {@link IStrategoAppl} the current rule
	 */
	@Override
	public void initializeLabel(int labelNumber,
			IStrategoAppl parseTreeProduction) {
		if (!isRuleRecognitionPrinted) {
			if (GraphBuilderBaseImpl.printDebugInformationToTheConsole) {
				System.out.println("\tInitializing rules...");
			}
			isRuleRecognitionPrinted = true;
		}
		setRule(labelNumber, new Rule(labelNumber, parseTreeProduction));
	}

	/**
	 * Used to print the message "Initializing rules..." message only once.
	 */
	private boolean isRuleRecognitionPrinted = false;

	/**
	 * Informs the {@link TreeTraverser} about:
	 * 
	 * @param input
	 *            {@link String} the content which will be parsed
	 * @param filename
	 *            {@link String} the name of the file of which
	 *            <code>input</code> is the content of
	 */
	@Override
	public void initializeInput(String input, String filename) {
		stack = new Stack(filename, input, graphBuilder.getInitialPosition());
	}

	/**
	 * Returns <code>null</code> to tell the interpreter that the default
	 * {@link ITokenizer} should be used
	 * 
	 * @return {@link ITokenizer}
	 */
	@Override
	public ITokenizer getTokenizer() {
		startTimeOfParsing = System.nanoTime();
		return null;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void reset() {
	}

	/**
	 * Recognizes the {@link TreeTraverser} about the internal parse forest. It
	 * is depth first traversed and the corresponding semantic actions are
	 * executed
	 * 
	 * @param node
	 *            {@link AbstractParseNode} the root of the internal parse
	 *            forest
	 * @return {@link Object} <code>node</code> to be reused as first argument
	 *         in {@link #buildTreeTop(Object, int)}
	 */
	@Override
	public Object buildTree(AbstractParseNode node) {
		((GraphBuilderBaseImpl) graphBuilder).addToParseTime(System.nanoTime()
				- startTimeOfParsing);
		if (GraphBuilderBaseImpl.printDebugInformationToTheConsole) {
			System.out.println("\tExecuting semantic actions...");
		}
		long startTime = System.nanoTime();
		traverse(node);
		((GraphBuilderBaseImpl) graphBuilder)
				.addToSemanticActionExecutionTime(System.nanoTime() - startTime);
		return node;
	}

	/**
	 * This method is used to inform the {@link TreeTraverser} about: (see
	 * params)<br>
	 * In debug mode <code>subtree</code> is printed.
	 * 
	 * @param subtree
	 *            {@link Object} the internal parse forest
	 * @param ambiguityCount
	 *            <code>int</code> the number of ambiguities in the internal
	 *            parse forest
	 * @return {@link Object} which is the graph produced by the semantic
	 *         actions. This result is returned by the interpreter.
	 */
	@Override
	public Object buildTreeTop(Object subtree, int ambiguityCount) {
		if (graphBuilder.isDebugMode()) {
			assert subtree instanceof AbstractParseNode;
			System.out.println(printParseForest((AbstractParseNode) subtree));
		}
		return graphBuilder.getGraph();
	}

	/**
	 * In dot mode a graphical representation of the internal parse forest
	 * <code>root</code> is created and the path to the created file is
	 * returned. Otherwise a {@link String} representation is returned.<br>
	 * In verbose mode all used rules are printed. Otherwise only the rules
	 * defined in the grammar are printed.<br>
	 * In each case ambiguity nodes are highlighted.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} which is the root of the internal
	 *            parse forest
	 * @return {@link String} message as a result of the printing process
	 */
	private String printParseForest(AbstractParseNode root) {
		ParseForest2Output printer = graphBuilder.isDotMode() ? new ParseForest2Dot(
				this, graphBuilder.getDotOutputFormat())
				: new ParseForest2String(this);
		return printer.convertParseForest(root, graphBuilder.isVerboseMode());
	}

	/**
	 * In dot mode a graphical representation of the internal parse forest
	 * <code>root</code> is created and the path to the created file is
	 * returned. Otherwise a {@link String} representation is returned.<br>
	 * In verbose mode all used rules are printed. Otherwise only the rules
	 * defined in the grammar are printed.<br>
	 * In each case ambiguity nodes and <code>nodeWithException</code> are
	 * highlighted.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} which is the root of the internal
	 *            parse forest
	 * @param nodeWithException
	 *            {@link AbstractParseNode} which caused an exception an will be
	 *            highlighted in the output
	 * @return {@link String} message as a result of the printing process
	 */
	private String printParseForest(AbstractParseNode root,
			AbstractParseNode nodeWithException) {
		ParseForest2Output printer = graphBuilder.isDotMode() ? new ParseForest2Dot(
				this, graphBuilder.getDotOutputFormat())
				: new ParseForest2String(this);
		return printer.convertParseForest(root, graphBuilder.isVerboseMode(),
				graphBuilder.isVerboseMode() ? nodeWithException : stack
						.getCurrentElement()
						.getParentApplicationOfDefinedRule()
						.getRepresentedNode());
	}

	/**
	 * Inserts <code>rule</code> into {@link #rules} at position
	 * <code>index - {@link #offsetOfRuleIndex}</code>.
	 * 
	 * @param index
	 *            <code>int</code> the number of <code>rule</code> in the range<br>
	 *            <code>{@link #offsetOfRuleIndex} <= index < {@link #offsetOfRuleIndex}+{@link #rules}.length</code>
	 * @param rule
	 *            {@link Rule} which will be inserted in {@link #rules}
	 * @return {@link Rule} which was the previous rule <code>index</code>
	 */
	private Rule setRule(int index, Rule rule) {
		assert index >= offsetOfRuleIndex : "The rule must have at least an index of "
				+ offsetOfRuleIndex + " but has an index of " + index + ".";
		assert index <= offsetOfRuleIndex + rules.length - 1 : "The rule must have an index <= "
				+ (offsetOfRuleIndex + rules.length - 1)
				+ " but has an index of " + index + ".";
		Rule oldRule = rules[index - offsetOfRuleIndex];
		rules[index - offsetOfRuleIndex] = rule;
		return oldRule;
	}

	/**
	 * Returns the {@link Rule} with number <code>index</code>. If such a rule
	 * is not present <code>null</code> is returned.
	 * 
	 * @param index
	 *            <code>int</code> the number of the requested rule
	 * @return {@link Rule}
	 */
	public Rule getRule(int index) {
		// returns null if a leaf which recognizes a char is found
		return index >= offsetOfRuleIndex
				&& index <= offsetOfRuleIndex + rules.length - 1 ? rules[index
				- offsetOfRuleIndex] : null;
	}

	/**
	 * @return {@link Stack} which is referenced by {@link #stack}
	 */
	public Stack getStack() {
		return stack;
	}

	/**
	 * Traverses the tree of <code>root</code> via the depth first strategy and
	 * executes the corresponding semantic actions. If an ambiguity node is
	 * found an {@link ParseError} is thrown.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} the root of the internal parse
	 *            forest <code>currentNode</code> belongs to
	 */
	private void traverse(AbstractParseNode root) {
		// check if root is an ambiguity node
		if (root.isAmbNode()) {
			System.out.println("\tAn ambiguity was detected...");
			throw new ParseError("An ambiguity was detected.\n"
					+ printParseForest(root));
		}
		// enter root
		stack.enterNode(getRule(root.getLabel()), root, graphBuilder);
		executeSemanticAction(root, stack.getCurrentElement()
				.getRepresentedNode());
		while (!stack.isEmpty()) {
			((GraphBuilderBaseImpl) graphBuilder)
					.addToSizeOfInternalParseForest(1);
			if (stack.getCurrentElement().haveAllChildrenBeenHandeled()) {
				// leave node
				stack.leaveNode();
				// return to parent node
				stack.returnToNode();
				if (stack.isEmpty()) {
					break;
				}
				executeSemanticAction(root, stack.getCurrentElement()
						.getRepresentedNode());
			} else {
				AbstractParseNode nextChild = stack.getCurrentElement()
						.getRepresentedNode().getChildren()[stack
						.getCurrentElement().getNumberOfChildren()];
				// check if nextChild is an ambiguity node
				if (nextChild.isAmbNode()) {
					System.out.println("\tAn ambiguity was detected...");
					throw new ParseError("An ambiguity was detected.\n"
							+ printParseForest(root));
				}
				stack.enterNode(getRule(nextChild.getLabel()), nextChild,
						graphBuilder);
				executeSemanticAction(root, stack.getCurrentElement()
						.getRepresentedNode());
			}
		}
	}

	/**
	 * Executes the semantic actions which can be identified by:
	 * <ul>
	 * <li>the number of the currently executed rule</li>
	 * <li>the rule defined in an EDL grammar which was lastly applied before
	 * the currently applied rule</li>
	 * <li>the current position in the rule (<code>position</code>)</li>
	 * </ul>
	 * 
	 * If an {@link Exception} occurs during the execution of the semantic
	 * actions and the debug mode is activated then the internal parse forest is
	 * printed and the application of rule which caused the {@link Exception} is
	 * highlighted.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} which is the root of the internal
	 *            parse forest
	 * @param currentNode
	 *            {@link AbstractParseNode} the currently traversed node
	 */
	private void executeSemanticAction(AbstractParseNode root,
			AbstractParseNode currentNode) {
		try {
			StackElement currentElement = stack.getCurrentElement();
			if (currentElement.getAppliedRule() != null
					&& currentElement.getAppliedRule().canHaveSemanticAciton()) {
				// if the current element recognizes an input character no
				// semantic actions can be defined for that node
				graphBuilder.execute(stack);
			}
		} catch (Throwable e) {
			if (graphBuilder.isDebugMode()) {
				System.err.println("\tAn error occured: " + e.toString());
				System.err.println(printParseForest(root, currentNode));
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void reset(int arg0) {
	}

}
