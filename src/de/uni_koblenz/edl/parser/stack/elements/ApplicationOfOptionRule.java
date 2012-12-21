package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf(T) -&gt; cf(T?)</li>
 * <li>lex(T) -&gt; lex(T?)</li>
 * </ul>
 */
public class ApplicationOfOptionRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfOptionRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If {@link StackElement#result} was not set by semantic actions of an EDL
	 * grammar yet, then the it is set to the result of its child.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = getChild(0).getResult();
		}
	}

}
