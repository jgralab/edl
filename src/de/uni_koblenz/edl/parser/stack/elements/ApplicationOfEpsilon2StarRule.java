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
 * <li>-&gt; cf(T*)</li>
 * <li>-&gt; lex(T*)</li>
 * <li>-&gt; cf({T1 T2}*)</li>
 * <li>-&gt; lex({T1 T2}*)</li>
 * </ul>
 */
public class ApplicationOfEpsilon2StarRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfEpsilon2StarRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If no result is set an empty {@link List} is set as a default value.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = new LinkedList();
		}
	}

}
