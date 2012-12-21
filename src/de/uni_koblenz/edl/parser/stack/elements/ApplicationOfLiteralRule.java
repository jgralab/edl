package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This class represents automatically generated rules, which have
 * <code>'ALexem'</code> or <code>"ALexem"</code> as head.
 */
public class ApplicationOfLiteralRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfLiteralRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If no {@link StackElement#result} is set, the recognized lexem (
	 * {@link String}) is used as default value.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = stack.getLexem(getPosition());
		}
	}

}
