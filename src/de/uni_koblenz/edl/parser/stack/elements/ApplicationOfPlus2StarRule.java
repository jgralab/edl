package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf(T+) -&gt; cf(T*)</li>
 * <li>lex(T+) -&gt; lex(T*)</li>
 * <li>cf({T1 T2}+) -&gt; cf({T1 T2}*)</li>
 * <li>lex({T1 T2}+) -&gt; lex({T1 T2}*)</li>
 * </ul>
 */
public class ApplicationOfPlus2StarRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfPlus2StarRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If {@link StackElement#result} was not set by semantic actions of an EDL
	 * grammar yet, then it is set to the result of its child. Further more in
	 * case of a {@link RuleType#LIST} or {@link RuleType#LIST_PROD} child
	 * {@link StackElement#isResultAlreadySet} is set to the value of
	 * {@link StackElement#isResultAlreadySet} of its child.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = getChild(0).getResult();
			if (getChild(0).getAppliedRule().getType() == RuleType.LIST
					|| getChild(0).getAppliedRule().getType() == RuleType.LIST_PROD) {
				// for lists the isResultAlreadySet value must be set
				isResultAlreadySet = getChild(0).isResultAlreadySet;
			}
		}
	}

}
