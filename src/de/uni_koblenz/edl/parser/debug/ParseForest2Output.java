package de.uni_koblenz.edl.parser.debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.TreeTraverser;
import de.uni_koblenz.edl.parser.stack.elements.StackElement;

/**
 * This is the base class for all parse forest to anything converter.
 */
public abstract class ParseForest2Output {

	/**
	 * The current {@link TreeTraverser}.
	 */
	protected TreeTraverser treeTraverser;

	/**
	 * In case of <code>true</code> all applied rules are printed in the way
	 * they were generated after BNF transformation. Otherwise only the rules
	 * which were defined in the grammar are printed the way they were written.
	 */
	protected boolean isVerbose;

	/**
	 * The {@link AbstractParseNode} which semantic actions caused an
	 * {@link Exception}. This node will be highlighted in the output.
	 */
	protected AbstractParseNode nodeWithException;

	/**
	 * Stores the positions of all {@link AbstractParseNode}s in the internal
	 * parse forest, from the current node to the root. The bottom element has
	 * to be a {@link Position} with the initial position values.
	 */
	protected Stack<Position> positions;

	/**
	 * This contains the {@link String} to mark ambiguity nodes or
	 * {@link #nodeWithException}.
	 */
	protected String MARK_OF_ERROR;

	/**
	 * <code>true</code> if the subtree of an node which stands for the
	 * application of a {@link RuleType#LITERAL} rule is currently traversed
	 */
	private boolean isInLiteralRule = false;

	/**
	 * <code>true</code> if the subtree of an node which stands for the
	 * application of a {@link RuleType#WHITESPACE} rule is currently traversed
	 */
	private boolean isWhitespace = false;

	/**
	 * Creates a new {@link ParseForest2Output} instance.
	 * 
	 * @param traverser
	 *            {@link TreeTraverser} which gives access to the rules and the
	 *            content of the currently parsed file.
	 */
	public ParseForest2Output(TreeTraverser traverser) {
		this.treeTraverser = traverser;
		positions = new Stack<Position>();
		Position initialPosition = traverser.getStack().getInitialPosition();
		positions.push(new Position(initialPosition.getOffset(), 0,
				initialPosition.getFirstLine(), initialPosition
						.getFirstColumn()));
	}

	/**
	 * Creates an representation of the internal parse forest with root
	 * <code>root</code>. Ambiguity nodes will be marked.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} the root of the internal parse
	 *            forest which representation will be created
	 * @param isVerbose
	 *            <code>boolean</code> In case of <code>true</code> all applied
	 *            rules are printed in the way they were generated after BNF
	 *            transformation. Otherwise only the rules which were defined in
	 *            the grammar are printed the way they were written.
	 * @return {@link String} which should be given to the user
	 */
	public String convertParseForest(AbstractParseNode root, boolean isVerbose) {
		return convertParseForest(root, isVerbose, null);
	}

	/**
	 * Creates an representation of the internal parse forest with root
	 * <code>root</code>. Ambiguity nodes and <code>nodeWithException</code>
	 * will be marked.
	 * 
	 * @param root
	 *            {@link AbstractParseNode} the root of the internal parse
	 *            forest which representation will be created
	 * @param isVerbose
	 *            <code>boolean</code> In case of <code>true</code> all applied
	 *            rules are printed in the way they were generated after BNF
	 *            transformation. Otherwise only the rules which were defined in
	 *            the grammar are printed the way they were written.
	 * @param nodeWithException
	 *            {@link AbstractParseNode} which semantic actions caused an
	 *            {@link Exception}. This node will be highlighted in the
	 *            output.
	 * @return {@link String} which should be given to the user
	 */
	public abstract String convertParseForest(AbstractParseNode root,
			boolean isVerbose, AbstractParseNode nodeWithException);

	/**
	 * Traverses the internal parse forest with root <code>node</code> and
	 * appends its representation to <code>appendable</code>.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the representation
	 *            <code>node</code> and its children will be appended
	 * @param node
	 *            {@link AbstractParseNode} which is the <code>root</code> of
	 *            the internal parse forest
	 * @throws IOException
	 *             if the appending to <code>appendable</code> causes an
	 *             {@link IOException}
	 */
	protected void traverse(Appendable appendable, AbstractParseNode node)
			throws IOException {
		class Tree {
			public StringBuilder representation;
			private List<Tree> children;

			public void addChild(Tree child) {
				if (children == null) {
					children = new ArrayList<Tree>();
				}
				children.add(child);
			}

			public void print(Appendable app) throws IOException {
				Stack<Tree> stack = new Stack<Tree>();
				stack.push(this);
				while (!stack.isEmpty()) {
					Tree current = stack.pop();
					if (current.representation != null
							&& current.representation.length() > 0) {
						app.append(current.representation);
					}
					if (current.children != null) {
						for (Tree child : current.children) {
							stack.push(child);
						}
					}
				}
			}
		}

		// entries on the call stack consist of:
		// 0. AbstractParseNode: parent node
		// 1. AbstractParseNode: current node
		// 2. the current tree (only on backward entries)
		Stack<Object[]> callStack = new Stack<Object[]>();
		Tree tree = new Tree();
		callStack.push(new Object[] { null, node, tree });
		callStack.push(new Object[] { null, node });
		while (!callStack.isEmpty()) {
			Object[] currentEntry = callStack.pop();
			AbstractParseNode parent = (AbstractParseNode) currentEntry[0];
			AbstractParseNode current = (AbstractParseNode) currentEntry[1];
			boolean isReturnToNode = currentEntry.length == 3;

			if (isReturnToNode) {
				// leave node
				leaveNode(current);
				StringBuilder sb = new StringBuilder();
				if (isPrintable(current)) {
					convertNode(sb, current);
				} else if (isApplicationOfLiteralRule(current)) {
					if (isVerbose || !isWhitespace) {
						convertLiteralNode(sb, current);
					}
					isInLiteralRule = false;
				} else if (isApplicationOfWhitespaceRule(current)) {
					isWhitespace = false;
				}
				assert ((Tree) currentEntry[2]).representation == null : ((Tree) currentEntry[2]).representation
						+ "" + sb;
				((Tree) currentEntry[2]).representation = sb;

				if (parent != null) {
					// return to parent node
					AbstractParseNode[] parentsChildren = parent.getChildren();
					returnToNode(
							parent,
							current,
							current == parentsChildren[parentsChildren.length - 1]);
				}
			} else {
				// enter a new node
				enterNode(current);
				if (isApplicationOfLiteralRule(current)) {
					isInLiteralRule = true;
				} else if (isApplicationOfWhitespaceRule(current)) {
					isWhitespace = true;
				}
				Tree parentTreeNode = (Tree) callStack.peek()[2];
				AbstractParseNode[] children = current.getChildren();
				for (int i = children.length - 1; i >= 0; i--) {
					Tree childTree = new Tree();
					parentTreeNode.addChild(childTree);
					AbstractParseNode child = children[i];
					callStack.push(new Object[] { current, child, childTree });
					callStack.push(new Object[] { current, child });
				}
			}
		}

		tree.print(appendable);
	}

	/**
	 * If <code>node</code> is the application of a {@link RuleType#LITERAL}
	 * rule and not the verbose mode is activated, then this method is called.
	 * It appends the whole recognized literal to <code>appendable</code>.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the representation
	 *            <code>node</code> and its children will be appended
	 * @param node
	 *            {@link AbstractParseNode} which is the application of a
	 *            {@link RuleType#LITERAL} rule
	 * @throws IOException
	 */
	protected abstract void convertLiteralNode(Appendable appendable,
			AbstractParseNode node) throws IOException;

	/**
	 * Checks if <code>node</code> stands for the application of a
	 * {@link RuleType#LITERAL} rule.
	 * 
	 * @param node
	 *            {@link AbstractParseNode} which is checked
	 * @return <code>boolean</code> <code>true</code> if <code>node</code>
	 *         stands for the application of a {@link RuleType#LITERAL} rule
	 */
	private boolean isApplicationOfLiteralRule(AbstractParseNode node) {
		if (node.isAmbNode()) {
			return false;
		}
		Rule rule = treeTraverser.getRule(node.getLabel());
		if (rule != null) {
			return rule.getType() == RuleType.LITERAL;
		}
		return false;
	}

	/**
	 * Checks if <code>node</code> stands for the first application of a
	 * {@link RuleType#WHITESPACE} rule in the path to the root.
	 * 
	 * @param node
	 *            {@link AbstractParseNode} which is checked
	 * @return <code>boolean</code> <code>true</code> if <code>node</code>
	 *         stands for the application of a {@link RuleType#WHITESPACE} rule
	 */
	private boolean isApplicationOfWhitespaceRule(AbstractParseNode node) {
		if (node.isAmbNode()) {
			return false;
		}
		Rule rule = treeTraverser.getRule(node.getLabel());
		if (rule != null) {
			return rule.getType() == RuleType.WHITESPACE
					&& rule.getNumberOfTermsInBody() < 2;
		}
		return false;
	}

	/**
	 * Checks if the <code>node</code> should be contained in the output. This
	 * is the case if at least one of the following conditions is
	 * <code>true</code>:
	 * <ul>
	 * <li>verbose mode is activated</li>
	 * <li><code>node</code> is not a rule of type {@link RuleType#WHITESPACE}
	 * or direct or indirect child of such a rule</li>
	 * <li><code>node</code> represents an ambiguity</li>
	 * <li><code>node</code> is the recognition of an input character which is
	 * not child of a literal rule</li>
	 * <li><code>node</code> is the application of a defined rule</li>
	 * </ul>
	 * 
	 * @param node
	 *            {@link AbstractParseNode} which is checked
	 * @return <code>boolean</code> <code>true</code> it the representation of
	 *         <code>node</code> should be contained in the output
	 */
	protected boolean isPrintable(AbstractParseNode node) {
		if (node.isAmbNode()) {
			return true;
		}
		Rule rule = treeTraverser.getRule(node.getLabel());
		return isVerbose
				|| (!isWhitespace && ((rule == null && !isInLiteralRule) || (rule != null && rule
						.getType() == RuleType.DEFINED)));
	}

	/**
	 * This method is called, when the new node <code>node</code> is visited. A
	 * new {@link Position} is pushed on {@link #positions} with the values:
	 * <ul>
	 * <li>
	 * <code>newPosition.offset = oldTopOfStack.offset + oldTopOfStack.length</code>
	 * </li>
	 * <li><code>newPosition.length = 0</code></li>
	 * <li><code>newPosition.firstLine = oldTopOfStack.lastLine</code></li>
	 * <li><code>newPosition.lastLine = oldTopOfStack.lastLine</code></li>
	 * <li><code>newPosition.firstColumn = oldTopOfStack.lastColumn</code></li>
	 * <li><code>newPosition.lastColumn = oldTopOfStack.lastColumn</code></li>
	 * </ul>
	 * 
	 * @param node
	 *            {@link AbstractParseNode} which is the entered node
	 */
	protected void enterNode(AbstractParseNode node) {
		assert !positions.isEmpty() : "The positions Stack must not be empty.";
		Position parentPos = positions.peek();
		Position newPos = new Position(parentPos.getOffset()
				+ parentPos.getLength(), 0, parentPos.getLastLine(),
				parentPos.getLastLine(), parentPos.getLastColumn(),
				parentPos.getLastColumn());
		positions.push(newPos);
	}

	/**
	 * This method is called by the {@link TreeTraverser} when it returns to the
	 * parent node of the internal parse forest. It does the following:
	 * <ul>
	 * <li>
	 * <code>newTopOfStack.length = newTopOfStack.getLength() + node.getLength()</code>
	 * </li>
	 * <li><code>newTopOfStack.lastLine = node.getLastLine()</code></li>
	 * <li><code>newTopOfStack.lastColumn = node.getLastColumn()</code></li>
	 * </ul>
	 * </li> Pop the top element of {@link #positions}.
	 * 
	 * @param node
	 *            {@link AbstractParseNode} to which the traversal is returned
	 * @param child
	 *            {@link AbstractParseNode} from which the traversal is returned
	 * @param wasLastChildTraversed
	 *            <code>boolean</code> is <code>true</code> if the last child
	 *            was iterated.
	 */
	protected void returnToNode(AbstractParseNode node,
			AbstractParseNode child, boolean wasLastChildTraversed) {
		assert positions.size() >= 2 : "The positions Stack must have at least two elements, but it has only "
				+ positions.size() + " elements.";
		Position oldPos = positions.pop();
		if (node.isAmbNode() && !wasLastChildTraversed) {
			return;
		}
		Position newTopPos = positions.peek();
		newTopPos.setLength(newTopPos.getLength() + oldPos.getLength());
		newTopPos.setLastLine(oldPos.getLastLine());
		newTopPos.setLastColumn(oldPos.getLastColumn());
	}

	/**
	 * This method is executed by the {@link TreeTraverser} when a node of the
	 * internal parse forest is left. If the left node is a leaf,
	 * <ul>
	 * <li>which stands for an epsilon rule, than
	 * <ul>
	 * <li><code>node.getLastLine() = node.getFirstLine()</code></li>
	 * <li><code>node.getLastColumn() = node.getFirstColumn()</code></li>
	 * </ul>
	 * </li>
	 * <li>which stands for the recognition of <code>\n</code>
	 * <ul>
	 * <li><code>node.lastLine = node.getFirstLine() + 1</code></li>
	 * <li><code>node.lastColumn = 0</code></li>
	 * </ul>
	 * </li>
	 * <li>which stands for the recognition of a character different from
	 * <code>\n</code>
	 * <ul>
	 * <li><code>node.lastLine = node.getFirstLine()</code></li>
	 * <li><code>node.lastColumn = node.getFirstColumn() + 1</code></li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param node
	 *            {@link AbstractParseNode} the left node
	 */
	protected void leaveNode(AbstractParseNode node) {
		// update positions
		Position position = positions.peek();
		if (node.isAmbNode()) {
			return;
		}
		Rule appliedRule = treeTraverser.getRule(node.getLabel());
		if (appliedRule == null) {
			// current is a leaf which recognizes a character
			position.setLength(1);
			if (treeTraverser.getStack().getLexem(position).equals("\n")) {
				// a new line character was recognized
				position.setLastLine(position.getFirstLine() + 1);
				position.setLastColumn(0);
			} else {
				position.setLastLine(position.getFirstLine());
				position.setLastColumn(position.getFirstColumn() + 1);
			}
		} else if (appliedRule.isEpsilonRule()) {
			position.setLastLine(position.getFirstLine());
			position.setLastColumn(position.getFirstColumn());
		}
	}

	/**
	 * Creates the representation of <code>node</code> and appends it to
	 * <code>appendable</code>.
	 * 
	 * @param appendable
	 *            {@link Appendable} to which the representation of
	 *            <code>node</code> will be appended
	 * @param node
	 *            {@link AbstractParseNode} which representation will be created
	 * @throws IOException
	 *             if the appending to <code>appendable</code> causes an
	 *             {@link IOException}
	 */
	protected abstract void convertNode(Appendable appendable,
			AbstractParseNode node) throws IOException;

	/**
	 * Checks if the current node must be marked as a reason for an exception.
	 * 
	 * @param node
	 *            {@link AbstractParseNode} the current node of the internal
	 *            parse forest
	 * @return <code>boolean</code> <code>true</code> if <code>node</code> must
	 *         be marked
	 */
	protected boolean isErrorNode(AbstractParseNode node) {
		if (node.isAmbNode()) {
			return true;
		}
		StackElement currentElement = treeTraverser.getStack()
				.getCurrentElement();
		if (currentElement == null) {
			return node == nodeWithException;
		}
		if (isVerbose) {
			currentElement = currentElement.getParentApplicationOfDefinedRule();
		}
		return node == nodeWithException
				&& positions.peek().getOffset() == currentElement.getOffset();
	}
}
