package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This class represents the recognition of an input character.
 * {@link StackElement#rule} is <code>null</code>.
 */
public class Leaf extends StackElement {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public Leaf(Stack stack, Rule rule, AbstractParseNode node) {
		super(stack, rule, node);
		ruleApply = getParent().getParentApplicationOfDefinedRule();
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
