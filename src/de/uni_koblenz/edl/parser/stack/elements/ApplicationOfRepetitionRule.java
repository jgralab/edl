package de.uni_koblenz.edl.parser.stack.elements;

import java.util.List;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;

/**
 * This is the {@link StackElement} for automatically generated rules of the
 * form:
 * <ul>
 * <li>cf(T*) cf(LAYOUT?) cf(T*) -&gt; cf(T*) {left}</li>
 * <li>cf(T*) cf(LAYOUT?) cf(T+) -&gt; cf(T+)</li>
 * <li>cf(T+) cf(LAYOUT?) cf(T*) -&gt; cf(T+)</li>
 * <li>cf(T+) cf(LAYOUT?) cf(T+) -&gt; cf(T+) {left}</li>
 * <li>lex(T*) lex(T*) -&gt; lex(T*) {left}</li>
 * <li>lex(T*) lex(T+) -&gt; lex(T+)</li>
 * <li>lex(T+) lex(T*) -&gt; lex(T+)</li>
 * <li>lex(T+) lex(T+) -&gt; lex(T+) {left}</li>
 * </ul>
 */
public class ApplicationOfRepetitionRule extends ApplicationOfGeneratedRule {

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfRepetitionRule(Stack stack, Rule rule,
			AbstractParseNode node) {
		super(stack, rule, node);
	}

	/**
	 * {@link StackElement#result} is the result {@link List} of the first child
	 * appended by the elements of the result {@link List} of the last child.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDefaultValue() {
		assert !isResultAlreadySet;
		result = getChild(0).getResult();
		((List) result).addAll((List) getChild(getNumberOfChildren() - 1)
				.getResult());
	}

}
