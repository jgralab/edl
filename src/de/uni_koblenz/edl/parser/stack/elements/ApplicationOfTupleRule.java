package de.uni_koblenz.edl.parser.stack.elements;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>"&lt;" cf(LAYOUT?) T1 cf(LAYOUT?) "," cf(LAYOUT?) ... cf(LAYOUT?) ","
 * cf(LAYOUT?) Tn cf(LAYOUT?) "&gt;" -&gt; cf(&lt;T1, ..., Tn&gt;)</li>
 * <li>"&lt;" T1 "," ... "," Tn "&gt;" -&gt; lex(&lt;T1, ..., Tn&gt;)</li>
 * </ul>
 */
public class ApplicationOfTupleRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule, AbstractParseNode)
	 */
	public ApplicationOfTupleRule(Stack stack, Rule rule, AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * The default value of a tuple is always <code>null</code>.
	 */
	@Override
	public void createDefaultValue() {
		assert isResultAlreadySet || result == null;
	}

}
