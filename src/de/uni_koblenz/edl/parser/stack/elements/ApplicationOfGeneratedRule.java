package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This class represents the application of a rule, which is not defined in an
 * EDL grammar by the user.
 */
public abstract class ApplicationOfGeneratedRule extends StackElement {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfGeneratedRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
		if (getParent() != null) {
			ruleApply = getParent().getParentApplicationOfDefinedRule();
		}
	}

}
