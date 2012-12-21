package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf((T1 ... Tn => T)) cf(LAYOUT?) "(" cf(LAYOUT?) T1 cf(LAYOUT?) ...
 * cf(LAYOUT?) Tn cf(LAYOUT?) ")" -&gt; T</li>
 * <li>lex((T1 ... Tn => T)) "(" T1 ... Tn ")" -&gt; T</li>
 * </ul>
 */
public class ApplicationOfFunctionRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfFunctionRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * As the default value {@link StackElement#result} keeps unchanged.
	 */
	@Override
	public void createDefaultValue() {
		assert isResultAlreadySet || result == null;
	}

}
