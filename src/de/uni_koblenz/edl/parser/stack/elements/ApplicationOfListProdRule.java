package de.uni_koblenz.edl.parser.stack.elements;

import java.util.LinkedList;
import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>T1 -&gt; cf({T1 T2}+)</li>
 * <li>T1 -&gt; lex({T1 T2}+)</li>
 * </ul>
 */
public class ApplicationOfListProdRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfListProdRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * The {@link StackElement#result} of this {@link StackElement} is a
	 * {@link List}. If the result of this {@link StackElement} is set via an
	 * semantic action defined in an EDL grammar this result is added to the
	 * {@link List}. Otherwise the result of the child is added to the
	 * {@link List}.<br>
	 * The T1 in {T1 T2 #X#}+ or {T1 T2 #X#}* can be accessed in the semantic
	 * action of X. This action will be executed at a rule of type
	 * {@link RuleType#LIST}. To enable this access,
	 * {@link ApplicationOfListRule#resultOfLastT1} of the element below this in
	 * {@link StackElement#stack} <code>e</code> is set to the result of the
	 * recognized T1, if <code>e</code> is of type {@link ApplicationOfListRule}
	 * .
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDefaultValue() {
		// result could be set in X or Y of {#X# T1 #Y# T2}+ or {#X# T1 #Y# T2}*
		Object resultOfList = isResultAlreadySet ? result : getChild(0)
				.getResult();
		if (getParent() instanceof ApplicationOfListRule) {
			// propagate the result of the last T1 to parent LIST rule
			// replacement of $0 in X of {T1 T2 #X#}+ or {T1 T2 #X#}*
			// by getResultOfLastT1() is possible
			((ApplicationOfListRule) getParent()).resultOfLastT1 = resultOfList;
		}
		List newResult = new LinkedList();
		newResult.add(resultOfList);
		result = newResult;
	}
}
