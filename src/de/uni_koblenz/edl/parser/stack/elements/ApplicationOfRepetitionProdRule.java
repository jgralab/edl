package de.uni_koblenz.edl.parser.stack.elements;

import java.util.LinkedList;
import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>T -&gt; cf(T+)</li>
 * <li>T -&gt; lex(T+)</li>
 * </ul>
 */
public class ApplicationOfRepetitionProdRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfRepetitionProdRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * The {@link StackElement#result} of this {@link StackElement} is a
	 * {@link List}. If the result of this {@link StackElement} is set via an
	 * semantic action defined in an EDL grammar this result is added to the
	 * {@link List}. Otherwise the result of the child is added to the
	 * {@link List}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDefaultValue() {
		List newResult = new LinkedList();
		newResult.add(isResultAlreadySet ? result : getChild(0).getResult());
		result = newResult;
	}

}
