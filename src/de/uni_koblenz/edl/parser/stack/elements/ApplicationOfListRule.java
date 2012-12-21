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
 * <li>cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) -&gt; cf({T1 T2}*)
 * {left}</li>
 * <li>cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) -&gt; cf({T1 T2}+)</li>
 * <li>cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) -&gt; cf({T1 T2}+)</li>
 * <li>cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) -&gt; cf({T1 T2}+)
 * {left}</li>
 * <li>lex({T1 T2}*) T2 lex({T1 T2}*) -&gt; lex({T1 T2}*) {left}</li>
 * <li>lex({T1 T2}*) T2 lex({T1 T2}+) -&gt; lex({T1 T2}+)</li>
 * <li>lex({T1 T2}+) T2 lex({T1 T2}*) -&gt; lex({T1 T2}+)</li>
 * <li>lex({T1 T2}+) T2 lex({T1 T2}+) -&gt; lex({T1 T2}+) {left}</li>
 * </ul>
 */
public class ApplicationOfListRule extends ApplicationOfGeneratedRule {

	/**
	 * This is the result of the last recognized T1 in {T1 T2}+ or {T1 T2}*.
	 */
	Object resultOfLastT1 = null;

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfListRule(Stack stack, Rule rule, AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * @return {@link Object} which is the result of the last recognized T1 in
	 *         {T1 T2}+ or {T1 T2}*.
	 */
	public Object getResultOfLastT1() {
		return resultOfLastT1;
	}

	/**
	 * The {@link StackElement#result} of this {@link StackElement} is a
	 * {@link List}. If the result of this {@link StackElement} is set via an
	 * semantic action defined in an EDL grammar, this result is added to the
	 * {@link List}. This means that the result was set in an semantic action X
	 * in {T1 T2 #X#}+ or {T1 T2 #X#}*.<br>
	 * Otherwise the {@link List} of the first child is set as result. If the
	 * result of no child is set by an semantic action, this would mean that
	 * result was not set in a semantic action X or Y in {#X# T1 #Y# T2}+ or
	 * {#X# T1 #Y# T2}*. Only in this case the result of T2 is appended to the
	 * current result {@link List}. After that the resulting {@link List} of the
	 * last child is appended to the current result {@link List}.<br>
	 * The T1 in {T1 T2 #X#}+ or {T1 T2 #X#}* can be accessed in the semantic
	 * action of X. This action will be executed at a rule of type
	 * {@link RuleType#LIST}. To enable this access,
	 * {@link ApplicationOfListRule#resultOfLastT1} of the element below this in
	 * {@link StackElement#stack} <code>e</code> is set to the result of the
	 * second recognized T1, if <code>e</code> is of type
	 * {@link ApplicationOfListRule} .
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDefaultValue() {
		StackElement firstChild = getChild(0);
		StackElement lastChild = getChild(getNumberOfChildren() - 1);

		if (getParent() instanceof ApplicationOfListRule) {
			// propagate the result of the last T1 to parent LIST rule
			// replacement of $0 in X of {T1 T2 #X#}+ or {T1 T2 #X#}*
			// by getResultOfLastT1() is possible
			((ApplicationOfListRule) getParent()).resultOfLastT1 = resultOfLastT1;
		}

		if (!isResultAlreadySet) {
			result = firstChild.getResult();
			isResultAlreadySet = firstChild.isResultAlreadySet
					|| lastChild.isResultAlreadySet;
			if (!isResultAlreadySet) {
				// the result of T_2 in the middle is only added if the result
				// of no child of type LIST_PROD is set
				((List) result).add(getChild(
						getAppliedRule().isContextFree() ? 2 : 1).getResult());
			}
			((List) result).addAll((List) lastChild.getResult());
		} else {
			// result is set in X {T1 T2 #X#}+ or {T1 T2 #X#}*
			List newResult = null;
			if (firstChild.getAppliedRule().getType() == RuleType.LIST) {
				// get List of ApplicationOfListRule child
				newResult = (List) firstChild.getResult();
			} else {
				newResult = new LinkedList();
			}
			newResult.add(result);
			result = newResult;
		}
	}

}
