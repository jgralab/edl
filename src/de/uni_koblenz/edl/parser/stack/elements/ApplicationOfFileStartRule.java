package de.uni_koblenz.edl.parser.stack.elements;

import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for the application of a rule which head is
 * <code>&lt;Start&gt;</code>.
 */
public class ApplicationOfFileStartRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfFileStartRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	@Override
	public ApplicationOfDefinedRule getParentApplicationOfDefinedRule() {
		throw new UnsupportedOperationException(
				"The application of a rule with type "
						+ getAppliedRule().getType()
						+ " has no application of a defined rule.");
	}

	@Override
	public List<Object> getWhitespaceBefore(int indexOfTerm) {
		if (indexOfTerm <= 0 && getParent() == null) {
			throw new IllegalArgumentException(
					"The application of a rule with type "
							+ getAppliedRule().getType()
							+ " has no whitespaces before term " + indexOfTerm
							+ " because it is the root.");
		}
		return super.getWhitespaceBefore(indexOfTerm);
	}

	@Override
	public Object getValueOfTemporaryVariable(String name) {
		throw new UnsupportedOperationException(
				"The application of a rule with type "
						+ getAppliedRule().getType()
						+ " has no temporaryVariables.");
	}

	@Override
	public Object setValueOfTemporaryVariable(String name, Object value) {
		throw new UnsupportedOperationException(
				"The application of a rule with type "
						+ getAppliedRule().getType()
						+ " has no temporaryVariables.");
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
				if (child.getAppliedRule().getType() != RuleType.WHITESPACE) {
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
