package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>T1 cf(LAYOUT?) ... cf(LAYOUT?) Tn -&gt; cf((T1 ... Tn))</li>
 * <li>T1 ... Tn -&gt; lex((T1 ... Tn))</li>
 * </ul>
 */
public class ApplicationOfSequenceRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfSequenceRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If {@link StackElement#result} is not set yet, it is checked if there
	 * exists exactly one child which do not apply a {@link RuleType#WHITESPACE}
	 * rule. In this case the current result is the result of that child.
	 * Otherwise <code>null</code> is used as a default value.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			boolean isOneChildDifferentFormWhitespaceFound = false;
			for (StackElement child : getChildren()) {
				assert child != null;
				if (child.getAppliedRule() == null
						|| child.getAppliedRule().getType() != RuleType.WHITESPACE) {
					if (isOneChildDifferentFormWhitespaceFound) {
						// there are more than two defined children
						result = null;
						break;
					} else {
						isOneChildDifferentFormWhitespaceFound = true;
						result = child.getResult();
					}
				}
			}
		}
	}

}
