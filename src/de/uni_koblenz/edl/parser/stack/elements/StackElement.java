package de.uni_koblenz.edl.parser.stack.elements;

import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the superclass for all elements of the {@link Stack}. They represent
 * the application of a rule in the internal parse forest. If it represents a
 * leaf in the internal parse forest, it has the type of subclass {@link Leaf}.
 * Otherwise it has the type of the subclass named
 * <code>ApplicationOfXRule</code> where <code>X</code> stands for the
 * {@link RuleType} of the applied {@link #rule}.
 */
public abstract class StackElement {

	/**
	 * Reference to the {@link Stack} this element is part of.
	 */
	protected final Stack stack;

	/**
	 * The {@link StackElement} below this in the {@link Stack}. If it is the
	 * bottom element it is <code>null</code>.
	 */
	private final StackElement parent;

	/**
	 * Holds the references to all elements which are and were directly above
	 * this in the {@link Stack}. If this {@link StackElement} represents a leaf
	 * in the internal parse forest or all children are deleted it is
	 * <code>null</code>. Its length represents the number of terms in the body
	 * of the applied {@link #rule}.
	 */
	private StackElement[] children;

	/**
	 * This is the index of the last child in {@link #children}. If no child is
	 * present it has the value <code>-1</code>.
	 */
	private int lastChild = -1;

	/**
	 * This is the {@link Position} of the lexem which is recognized by
	 * {@link #rule}. The values <code>length</code>, <code>lastLine</code> and
	 * <code>lastColumn</code> have the correct values only if the this
	 * {@link StackElement} is left.
	 */
	private final Position position;

	/**
	 * {@link Rule} representation of the applied rule in the internal parse
	 * forest.
	 */
	private final Rule rule;

	/**
	 * Refers to the first {@link ApplicationOfDefinedRule} element which is
	 * below the current {@link StackElement} in the {@link Stack}. If none
	 * exists it is <code>null</code>. This is needed to get the
	 * {@link StackElement} which is the namespace for temporary variables and $
	 * in semantic actions defined for this rule.
	 */
	protected ApplicationOfDefinedRule ruleApply;

	/**
	 * Saves the result of this rule application.
	 */
	protected Object result;

	/**
	 * Is set to <code>true</code> if {@link #result} is set via an semantic
	 * action defined in an EDL grammar.
	 */
	protected boolean isResultAlreadySet = false;

	/**
	 * {@link AbstractParseNode} of the internal parse forest for which this
	 * {@link StackElement} stands for
	 */
	private final AbstractParseNode node;

	/**
	 * Creates a new {@link StackElement}.
	 * 
	 * @param stack
	 *            {@link Stack} in which this element is inserted.
	 * @param rule
	 *            {@link Rule} which is applied
	 * @param node
	 *            {@link AbstractParseNode} of the internal parse forest for
	 *            which this {@link StackElement} stands for
	 */
	public StackElement(Stack stack, Rule rule, AbstractParseNode node) {
		this.stack = stack;
		this.rule = rule;
		int numberOfChildren = rule == null ? 0 : rule.getNumberOfTermsInBody();
		if (numberOfChildren > 0) {
			children = new StackElement[numberOfChildren];
		}
		parent = stack.getCurrentElement();
		if (parent != null) {
			parent.addChild(this);
		}
		position = new Position();
		this.node = node;
	}

	/**
	 * @return {@link AbstractParseNode} of the internal parse forest for which
	 *         this {@link StackElement} stands for
	 */
	public AbstractParseNode getRepresentedNode() {
		return node;
	}

	/**
	 * Inserts <code>child</code> in the next empty position of
	 * {@link #children}.
	 * 
	 * @param child
	 *            {@link StackElement} which is above this in the {@link #stack}
	 */
	private void addChild(StackElement child) {
		assert children != null : "This StackElement has no children.";
		assert lastChild + 1 < children.length : "All children were already added.";
		children[++lastChild] = child;
	}

	/**
	 * Deletes all children of this {@link StackElement} by setting
	 * {@link #children} to <code>null</code>.
	 */
	public void deleteAllChildren() {
		children = null;
		lastChild = -1;
	}

	/**
	 * Returns all children. It should only be called when all children are
	 * parsed. Ensured by an assertion.
	 * 
	 * @return {@link StackElement}[] {@link #children}
	 */
	protected StackElement[] getChildren() {
		assert lastChild == children.length - 1 : "Not all children are parsed.";
		return children;
	}

	/**
	 * @return <code>int &gt;=0</code> the number of children which were already
	 *         handled
	 */
	public int getNumberOfChildren() {
		return lastChild + 1;
	}

	/**
	 * @return <code>int &gt;= 0</code> the current position of the semantic
	 *         action which can be executed
	 */
	public int getCurrentSemanticActionPosition() {
		return lastChild + 1;
	}

	/**
	 * @return {@link StackElement} which is below this element in the
	 *         {@link #stack}
	 */
	public StackElement getParent() {
		return parent;
	}

	/**
	 * @param index
	 *            <code>int</code> the index of the requested child
	 * @return {@link StackElement} which is at <code>index</code> of
	 *         {@link #children}
	 */
	public StackElement getChild(int index) {
		if (children == null || index > lastChild) {
			throw new IllegalArgumentException(
					"There does not exist a child with index "
							+ index
							+ ". "
							+ (children == null ? "There exists no child."
									: "The last child has index " + lastChild
											+ "."));
		}
		return children[index];
	}

	/**
	 * @return {@link Rule} which is the applied {@link #rule} of this
	 *         {@link StackElement}. If it represents the recognition of an
	 *         input character, <code>null</code> is returned.
	 */
	public Rule getAppliedRule() {
		return rule;
	}

	/**
	 * @return {@link Position} {@link #position} of the recognized lexem in the
	 *         parsed file
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @return <code>int</code> the index of the first recognized character
	 */
	public int getOffset() {
		return position.getOffset();
	}

	/**
	 * @return <code>int</code> the number of recognized characters
	 */
	public int getLength() {
		return position.getLength();
	}

	/**
	 * @return <code>int</code> the line of the parsed file in which the first
	 *         character of the recognized lexem is found
	 */
	public int getFirstLine() {
		return position.getFirstLine();
	}

	/**
	 * @return <code>int</code> the line of the parsed file in which the last
	 *         character of the recognized lexem is found
	 */
	public int getLastLine() {
		return position.getLastLine();
	}

	/**
	 * @return <code>int</code> the column of the parsed file in which the first
	 *         character of the recognized lexem is found
	 */
	public int getFirstColumn() {
		return position.getFirstColumn();
	}

	/**
	 * @return <code>int</code> the column of the parsed file in which the last
	 *         character of the recognized lexem is found
	 */
	public int getLastColumn() {
		return position.getLastColumn();
	}

	/**
	 * Updates all position values.
	 * 
	 * @param offset
	 *            {@link Position#offset}
	 * @param length
	 *            {@link Position#length}
	 * @param firstLine
	 *            {@link Position#firstLine}
	 * @param lastLine
	 *            {@link Position#lastLine}
	 * @param firstColumn
	 *            {@link Position#firstColumn}
	 * @param lastColumn
	 *            {@link Position#lastColumn}
	 */
	public void setPostionOfLexem(int offset, int length, int firstLine,
			int lastLine, int firstColumn, int lastColumn) {
		position.setOffset(offset);
		position.setLength(length);
		position.setFirstLine(firstLine);
		position.setLastLine(lastLine);
		position.setFirstColumn(firstColumn);
		position.setLastColumn(lastColumn);
	}

	/**
	 * @return {@link String} which is the name of the currently parsed file
	 */
	public String getNameOfParsedFile() {
		return stack.getNameOfParsedFile();
	}

	/**
	 * @return {@link Object} which is the result of the current rule
	 *         application
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * This method is called by semantic actions in an EDL grammar. Do NOT call
	 * it otherwise.
	 * 
	 * @param result
	 *            {@link Object} the new result.
	 * @return {@link Object} <code>result</code>
	 */
	public Object setResult(Object result) {
		this.result = result;
		isResultAlreadySet = true;
		return result;
	}

	/**
	 * Finds the parsed whitespaces directly before the term with index
	 * <code>indexOfTerm</code> and returns its result. If such an whitespace
	 * could not be found, an {@link IllegalArgumentException} is thrown. If
	 * this method is called in an lexical rule an
	 * {@link UnsupportedOperationException} is thrown.
	 * 
	 * @param indexOfTerm
	 *            <code>int</code> the index of the term whose previous
	 *            whitespace is requested
	 * @return {@link List}&lt;{@link Object}&gt; containing all results
	 *         different from <code>null</code> which were produced by the
	 *         requested application of {@link RuleType#WHITESPACE} rules
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getWhitespaceBefore(int indexOfTerm) {
		assert indexOfTerm <= children.length : "The current rule\n" + rule
				+ "\ndoes not have an term with index " + indexOfTerm + ".";
		assert indexOfTerm <= lastChild : "The requested rerm has not been parsed yet.";
		if (!getAppliedRule().isContextFree()) {
			throw new UnsupportedOperationException(
					"Only context free rules may have whitespaces. But this rule is a lexical one:\n"
							+ getAppliedRule());
		}
		if (indexOfTerm == 0) {
			assert getParent().lastChild >= 0;
			return getParent().getWhitespaceBefore(getParent().lastChild);
		} else {
			return (List<Object>) children[indexOfTerm - 1].getResult();
		}
	}

	/**
	 * Looks up the value of a temporary variable with name <code>name</code>.
	 * These kind of variables are bound to the namespace of the first
	 * {@link ApplicationOfDefinedRule} below the current {@link StackElement}
	 * in {@link #stack}. If such a parent rule is not present a
	 * {@link UnsupportedOperationException} is thrown. If this variable is
	 * unknown an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param name
	 *            {@link String} name of the temporary variable
	 * @return {@link Object} the value of the temporary variable with name
	 *         <code>name</code>
	 */
	public Object getValueOfTemporaryVariable(String name) {
		return getParentApplicationOfDefinedRule().getValueOfTemporaryVariable(
				name);
	}

	/**
	 * Stores temporary variable with name <code>name</code> and sets its value
	 * to <code>value</code>. If a variable with that name already exists, only
	 * its value is updated. These kind of variables are bound to the namespace
	 * of the first {@link ApplicationOfDefinedRule} below the current
	 * {@link StackElement} in {@link #stack}. If such a parent rule is not
	 * present a {@link UnsupportedOperationException} is thrown.
	 * 
	 * @param name
	 *            {@link String} name of the temporary variable
	 * @param value
	 *            {@link Object} the value of the temporary variable with name
	 *            <code>name</code>
	 * @return {@link Object} <code>value</code>
	 */
	public Object setValueOfTemporaryVariable(String name, Object value) {
		getParentApplicationOfDefinedRule().setValueOfTemporaryVariable(name,
				value);
		return value;
	}

	/**
	 * @return {@link ApplicationOfDefinedRule} which is the first below this
	 *         {@link StackElement} in {@link #stack} of <code>this</code> if
	 *         this is an instance of {@link ApplicationOfDefinedRule} or
	 *         {@link ApplicationOfStartRule}
	 */
	public ApplicationOfDefinedRule getParentApplicationOfDefinedRule() {
		return ruleApply;
	}

	/**
	 * Creates the default values for the current rule application.
	 */
	public abstract void createDefaultValue();

	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ " represents "
				+ (rule != null ? "application of rule\n" + rule
						: "the recognition of an input character")
				+ "\ncurrent result: " + result + "\nrecognized lexem: \""
				+ stack.getLexem(position) + "\" " + " from line "
				+ position.getFirstLine() + " column "
				+ position.getFirstColumn() + " length " + position.getLength()
				+ " in file " + getNameOfParsedFile();
	}

	/**
	 * @return {@link String} which is the currently recognized lexem of the
	 *         input
	 */
	public String getLexem() {
		return stack.getLexem(getPosition());
	}

	/*
	 * The following methods are used for the traversal.
	 */

	public boolean haveAllChildrenBeenHandeled() {
		return children == null || children.length == 0
				|| lastChild == children.length - 1;
	}
}
