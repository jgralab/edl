package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>-&gt; cf(T?)</li>
 * <li>-&gt; lex(T?)</li>
 * <li>-&gt; cf(())</li>
 * <li>-&gt; lex(())</li>
 * </ul>
 */
public class ApplicationOfEpsilonRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfEpsilonRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If no {@link StackElement#result} is set, an empty {@link String} is used
	 * as default value.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = "";
		}
	}

}
