package de.uni_koblenz.edl.parser.stack;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.TreeTraverser;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfAlternativeRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfDefinedRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfEpsilon2StarRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfEpsilonRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfFileStartRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfFunctionRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfLex2CfRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfListProdRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfListRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfLiteralRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfOptionRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfPlus2StarRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfRepetitionProdRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfRepetitionRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfSequenceRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfStartRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfTupleRule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfWhitespaceRule;
import de.uni_koblenz.edl.parser.stack.elements.Leaf;
import de.uni_koblenz.edl.parser.stack.elements.StackElement;

/**
 * This class represents a stack which stores the path to the root of the
 * internal parse forest.
 */
public class Stack {

	/**
	 * Stores the name of the currently parsed file.
	 */
	private final String fileName;

	/**
	 * Stores the part of the file {@link #fileName} which should be parsed.
	 */
	private final char[] fileContent;

	/**
	 * Stores the position of the first character of {@link #fileContent} in
	 * {@link #fileName}. Important values are {@link Position#offset},
	 * {@link Position#firstLine} and {@link Position#firstColumn}. The default
	 * values are:
	 * <ul>
	 * <li>offset = 0</li>
	 * <li>length = 0</li>
	 * <li>firstLine = 1</li>
	 * <li>firstColumn = 0</li>
	 * </ul>
	 */
	private Position initialPosition = new Position(0, 0, 1, 1, 0, 0);

	/**
	 * Points the bottom element of the stack. If <code>root == null</code> the
	 * current {@link Stack} is empty.
	 */
	private StackElement root;

	/**
	 * Points the top element of this {@link Stack}. If
	 * <code>current == null</code> this stack is empty.
	 */
	private StackElement current;

	/**
	 * Creates an empty {@link Stack}.
	 * 
	 * @param fileName
	 *            {@link String} the name of the currently parsed file
	 * @param fileContent
	 *            <code>char[]</code> the content of the file
	 *            <code>fileName</code>
	 */
	public Stack(String fileName, char[] fileContent) {
		this.fileName = fileName;
		this.fileContent = fileContent;
	}

	/**
	 * Creates an empty {@link Stack}.
	 * 
	 * @param fileName
	 *            {@link String} the name of the currently parsed file
	 * @param fileContent
	 *            {@link String} the content of the file <code>fileName</code>
	 */
	public Stack(String fileName, String fileContent) {
		this(fileName, fileContent.toCharArray());
	}

	/**
	 * Creates an empty {@link Stack}.
	 * 
	 * @param fileName
	 *            {@link String} the name of the currently parsed file
	 * @param fileContent
	 *            <code>char[]</code> the content of the file
	 *            <code>fileName</code>
	 * @param initialPosition
	 *            {@link Position} the position of <code>fileContent</code> in
	 *            <code>fileName</code>
	 */
	public Stack(String fileName, char[] fileContent, Position initalPosition) {
		this(fileName, fileContent);
		this.initialPosition = initalPosition;
	}

	/**
	 * Creates an empty {@link Stack}.
	 * 
	 * @param fileName
	 *            {@link String} the name of the currently parsed file
	 * @param fileContent
	 *            {@link String} the content of the file <code>fileName</code>
	 * @param initialPosition
	 *            {@link Position} the position of <code>fileContent</code> in
	 *            <code>fileName</code>
	 */
	public Stack(String fileName, String fileContent, Position initalPosition) {
		this(fileName, fileContent.toCharArray(), initalPosition);
	}

	public boolean isEmpty() {
		return current == null;
	}

	public Position getInitialPosition() {
		return initialPosition;
	}

	/**
	 * @return {@link String} the name of the currently parsed file
	 *         {@link #fileName}
	 */
	public String getNameOfParsedFile() {
		return fileName;
	}

	/**
	 * @param position
	 *            {@link Position} contains the offset and length values to
	 *            determine the part of {@link #fileContent} which is the
	 *            requested lexem
	 * @return {@link String} the requested lexem
	 * @see #getLexem(int, int)
	 */
	public String getLexem(Position position) {
		return getLexem(position.getOffset(), position.getLength());
	}

	/**
	 * Returns the lexem which is identivied by:<br>
	 * <code>new String({@link #fileContent}, offset - {@link #initialPosition}.getOffset(), length)</code>
	 * 
	 * @param offset
	 *            <code>int</code> the index of the first character of the
	 *            requested lexem
	 * @param length
	 *            <code>int</code> the number of characters in the requested
	 *            lexem
	 * @return {@link String} the requested lexem
	 */
	public String getLexem(int offset, int length) {
		assert offset - initialPosition.getOffset() >= 0;
		assert offset - initialPosition.getOffset() + length - 1 < fileContent.length : "The given length "
				+ length
				+ " would leave the range of the input. Last input character has the index "
				+ (fileContent.length - 1 + initialPosition.getOffset())
				+ " but the requested index was " + (offset + length - 1) + ".";
		return new String(fileContent, offset - initialPosition.getOffset(),
				length);
	}

	public char[] getFileContent() {
		return fileContent;
	}

	/**
	 * @return {@link StackElement} which is top of stack
	 * @see #current
	 */
	public StackElement getCurrentElement() {
		return current;
	}

	/**
	 * This method is called by the {@link TreeTraverser} when a new child in
	 * the internal parse forest is entered. It puts a new {@link StackElement}
	 * which represents the application of <code>rule</code> on this
	 * {@link Stack}. If this is the first element of the stack, its position is
	 * initialized with {@link #initialPosition}. Otherwise the new element gets
	 * the values:
	 * <ul>
	 * <li>
	 * <code>offset = {@link #current}.getOffset() + {@link #current}.getLength()</code>
	 * </li>
	 * <li><code>length = 0</code></li>
	 * <li><code>firstLine = {@link #current}.getLastLine()</code></li>
	 * <li><code>lastLine = {@link #current}.getLastLine()</code></li>
	 * <li><code>firstColumn = {@link #current}.getLastColumn()</code></li>
	 * <li><code>lastColumn = {@link #current}.getLastColumn()</code></li>
	 * </ul>
	 * 
	 * @param rule
	 *            {@link Rule} which application is represented by the newly
	 *            created {@link StackElement}.
	 * @param node
	 *            {@link AbstractParseNode} of the internal parse forest for
	 *            which this {@link StackElement} stands for
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} used to merge result vertices.
	 * @see #createStackElement(Rule, int)
	 */
	public void enterNode(Rule rule, AbstractParseNode node,
			InternalGraphBuilder graphBuilder) {
		StackElement newElement = createStackElement(rule, node, graphBuilder);
		assert newElement != null;
		if (root == null) {
			root = newElement;
			newElement.setPostionOfLexem(initialPosition.getOffset(), 0,
					initialPosition.getFirstLine(),
					initialPosition.getFirstLine(),
					initialPosition.getFirstColumn(),
					initialPosition.getFirstColumn());
		} else {
			assert current != null;
			newElement.setPostionOfLexem(
					current.getOffset() + current.getLength(), 0,
					current.getLastLine(), current.getLastLine(),
					current.getLastColumn(), current.getLastColumn());
		}
		current = newElement;
	}

	/**
	 * If <code>rule</code> is <code>null</code> a {@link Leaf} is created.
	 * Otherwise a {@link StackElement} of a type
	 * <code>ApplicationOfXRule</code> is created, where <code>X</code> is the
	 * {@link RuleType} of <code>rule</code>.
	 * 
	 * @param rule
	 *            {@link Rule} which is applied.
	 * @param node
	 *            {@link AbstractParseNode} of the internal parse forest for
	 *            which this {@link StackElement} stands for
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} used to merge result vertices.
	 * @return {@link StackElement} which is newly created.
	 */
	private StackElement createStackElement(Rule rule, AbstractParseNode node,
			InternalGraphBuilder graphBuilder) {
		if (rule == null) {
			return new Leaf(this, rule, node);
		} else {
			switch (rule.getType()) {
			case ALTERNATIVE:
				return new ApplicationOfAlternativeRule(this, rule, node);
			case DEFINED:
				return new ApplicationOfDefinedRule(this, rule, node,
						graphBuilder);
			case EPSILON:
				return new ApplicationOfEpsilonRule(this, rule, node);
			case EPSILON2STAR:
				return new ApplicationOfEpsilon2StarRule(this, rule, node);
			case FILE_START:
				return new ApplicationOfFileStartRule(this, rule, node);
			case FUNCTION:
				return new ApplicationOfFunctionRule(this, rule, node);
			case LEX2CF:
				return new ApplicationOfLex2CfRule(this, rule, node);
			case LIST:
				return new ApplicationOfListRule(this, rule, node);
			case LIST_PROD:
				return new ApplicationOfListProdRule(this, rule, node);
			case LITERAL:
				return new ApplicationOfLiteralRule(this, rule, node);
			case OPTION:
				return new ApplicationOfOptionRule(this, rule, node);
			case PLUS2STAR:
				return new ApplicationOfPlus2StarRule(this, rule, node);
			case REPETITION:
				return new ApplicationOfRepetitionRule(this, rule, node);
			case REPETITION_PROD:
				return new ApplicationOfRepetitionProdRule(this, rule, node);
			case SEQUENCE:
				return new ApplicationOfSequenceRule(this, rule, node);
			case START:
				return new ApplicationOfStartRule(this, rule, node,
						graphBuilder);
			case TUPLE:
				return new ApplicationOfTupleRule(this, rule, node);
			case WHITESPACE:
				return new ApplicationOfWhitespaceRule(this, rule, node);
			default:
				throw new RuntimeException("Unknown type of rule: "
						+ rule.getType());
			}
		}
	}

	/**
	 * This method is executed by the {@link TreeTraverser} when a node of the
	 * internal parse forest is left. If the left node is a leaf,
	 * <ul>
	 * <li>which stands for an epsilon rule, than
	 * <ul>
	 * <li>{@link #current}.getLastLine() = {@link #current}.getFirstLine()</li>
	 * <li>{@link #current}.getLastColumn() = {@link #current}.getFirstColumn()</li>
	 * </ul>
	 * </li>
	 * <li>which stands for the recognition of <code>\n</code>
	 * <ul>
	 * <li>{@link #current}.lastLine = {@link #current}.getFirstLine() + 1</li>
	 * <li>{@link #current}.lastColumn = 0</li>
	 * </ul>
	 * </li>
	 * <li>which stands for the recognition of a character different from
	 * <code>\n</code>
	 * <ul>
	 * <li>{@link #current}.lastLine = {@link #current}.getFirstLine()</li>
	 * <li>{@link #current}.lastColumn = {@link #current}.getFirstColumn() + 1</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	public void leaveNode() {
		// update positions
		Position position = current.getPosition();
		if (current.getAppliedRule() == null) {
			// current is a leaf which recognizes a character
			position.setLength(1);
			if (getLexem(position).equals("\n")) {
				// a new line character was recognized
				position.setLastLine(position.getFirstLine() + 1);
				position.setLastColumn(0);
			} else {
				position.setLastLine(position.getFirstLine());
				position.setLastColumn(position.getFirstColumn() + 1);
			}
		} else if (current.getAppliedRule().isEpsilonRule()) {
			position.setLastLine(position.getFirstLine());
			position.setLastColumn(position.getFirstColumn());
		}
	}

	/**
	 * This method is called by the {@link TreeTraverser} when it returns to the
	 * parent node of the internal parse forest. It does the following:
	 * <ul>
	 * <li>Create the default value of {@link #current}.</li>
	 * <li>Delete all children of {@link #current}.</li>
	 * <li>Update the position values of the parent {@link StackElement}:
	 * <ul>
	 * <li>parent.length = parent.getLength() + {@link #current}.getLength()</li>
	 * <li>parent.lastLine = {@link #current}.getLastLine()</li>
	 * <li>parent.lastColumn = {@link #current}.getLastColumn()</li>
	 * </ul>
	 * </li>
	 * <li>Set the parent {@link StackElement} as {@link #current}.</li>
	 * </ul>
	 */
	public void returnToNode() {
		current.createDefaultValue();
		current.deleteAllChildren();
		StackElement parent = current.getParent();
		if (parent != null) {
			parent.setPostionOfLexem(parent.getOffset(), parent.getLength()
					+ current.getLength(), parent.getFirstLine(),
					current.getLastLine(), parent.getFirstColumn(),
					current.getLastColumn());
		}
		current = parent;
	}
}
