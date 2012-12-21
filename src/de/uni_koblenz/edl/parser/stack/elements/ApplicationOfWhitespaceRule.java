package de.uni_koblenz.edl.parser.stack.elements;

import java.util.LinkedList;
import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf(LAYOUT) -&gt; cf(LAYOUT?)</li>
 * <li>-&gt; cf(LAYOUT?)</li>
 * <li>cf(LAYOUT) cf(LAYOUT) -&gt; cf(LAYOUT) {left}</li>
 * </ul>
 */
public class ApplicationOfWhitespaceRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfWhitespaceRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * The created default value of {@link StackElement#result} depends of the
	 * rule:
	 * <table border="1">
	 * <tr>
	 * <th>rule</th>
	 * <th>default value</th>
	 * </tr>
	 * <tr>
	 * <td>cf(LAYOUT) -&gt; cf(LAYOUT?)</td>
	 * <td>If the child does not apply a rule of type
	 * {@link RuleType#WHITESPACE} the default value is a {@link List} which
	 * contains the result of the child.<br>
	 * Otherwise the list of the child is used as default value.</td>
	 * </tr>
	 * <tr>
	 * <td>-&gt; cf(LAYOUT?)</td>
	 * <td>An empty {@link List} is used as default value.</td>
	 * </tr>
	 * <tr>
	 * <td>cf(LAYOUT) cf(LAYOUT) -&gt; cf(LAYOUT) {left}</td>
	 * <td>If the first child is a further application of this rule, it has a
	 * result of type {@link List}. The result of the second child is added to
	 * that {@link List}.<br>
	 * Otherwise a new {@link List} is created and the results of both children
	 * are added.</td>
	 * </tr>
	 * </table>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDefaultValue() {
		assert !isResultAlreadySet;
		if (getNumberOfChildren() == 2
				&& getChild(0).getAppliedRule().getType() == RuleType.WHITESPACE) {
			// cf(LAYOUT) cf(LAYOUT) -> cf(LAYOUT) {left}
			// and first cf(LAYOUT) is the same rule
			result = getChild(0).getResult();
			if (getChild(1).getResult() != null) {
				((List) result).add(getChild(1));
			}
		} else {
			result = new LinkedList();
			if (getNumberOfChildren() > 1 && getChild(1).getResult() != null) {
				// cf(LAYOUT) -> cf(LAYOUT?)
				((List) result).add(getChild(1).getResult());
			}
		}
	}

}
