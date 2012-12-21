package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf(T1) -&gt; cf(T1 | ... | Tn)</li>
 * <li>...</li>
 * <li>cf(Tn) -&gt; cf(T1 | ... | Tn)</li>
 * <li>lex(T1) -&gt; lex(T1 | ... | Tn)</li>
 * <li>...</li>
 * <li>lex(Tn) -&gt; lex(T1 | ... | Tn)</li>
 * </ul>
 */
public class ApplicationOfAlternativeRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfAlternativeRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * If {@link StackElement#result} was not set by semantic actions of an EDL
	 * grammar yet, then it is set to the result of its child.
	 */
	@Override
	public void createDefaultValue() {
		if (!isResultAlreadySet) {
			result = getChild(0).getResult();
		}
	}

}
