package de.uni_koblenz.edl.parser.stack.elements;

import java.util.List;
import java.util.Map;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for the application of a rule which head is
 * <code>&lt;START&gt;</code>.
 */
public class ApplicationOfStartRule extends ApplicationOfDefinedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} used to merge result vertices.
	 * @see ApplicationOfDefinedRule#ApplicationOfDefinedRule(Stack, Rule,
	 *      AbstractParseNode, Map)
	 */
	public ApplicationOfStartRule(Stack stack, Rule rule,
			AbstractParseNode node, InternalGraphBuilder graphBuilder) {
		super(stack, rule, node, graphBuilder);
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
