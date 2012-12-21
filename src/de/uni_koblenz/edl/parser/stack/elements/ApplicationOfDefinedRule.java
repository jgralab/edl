package de.uni_koblenz.edl.parser.stack.elements;

import java.util.HashMap;
import java.util.Map;

import org.spoofax.jsglr.client.AbstractParseNode;

import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.Stack;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This is the {@link StackElement} for rule which were defined in an EDL
 * grammar by the user.
 */
public class ApplicationOfDefinedRule extends StackElement {

	/**
	 * Stores the temporary variables and their corresponding values which are
	 * defined in the namespace of this defined rule.
	 */
	private Map<String, Object> temporaryVariables;

	/**
	 * {@link InternalGraphBuilder} used to merge result vertices.
	 */
	private final InternalGraphBuilder graphBuilder;

	/**
	 * @param stack
	 * @param rule
	 * @param node
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} used to merge result vertices.
	 * @see StackElement#StackElement(Stack, Rule)
	 */
	public ApplicationOfDefinedRule(Stack stack, Rule rule,
			AbstractParseNode node, InternalGraphBuilder graphBuilder) {
		super(stack, rule, node);
		ruleApply = this;
		this.graphBuilder = graphBuilder;
	}

	@Override
	public Object getValueOfTemporaryVariable(String name) {
		if (temporaryVariables == null || !temporaryVariables.containsKey(name)) {
			throw new IllegalArgumentException("The variable \"" + name
					+ "\" is unknown.");
		}
		return temporaryVariables.get(name);
	}

	@Override
	public Object setValueOfTemporaryVariable(String name, Object value) {
		if (temporaryVariables == null) {
			temporaryVariables = new HashMap<String, Object>();
		}
		temporaryVariables.put(name, value);
		return value;
	}

	/**
	 * Additionally it merges the results, if {@link StackElement#result} and
	 * <code>result</code> are instances of Vertex. If the old result was an
	 * instance of Edge it is deleted.
	 * 
	 * @see #mergeResults(Vertex, Vertex)
	 */
	@Override
	public Object setResult(Object result) {
		if (result instanceof Vertex && this.result instanceof Vertex
				&& result != this.result) {
			graphBuilder.mergeVertices((Vertex) this.result, (Vertex) result);
			this.result = null;
		} else if (this.result instanceof GraphElement && result != this.result) {
			((GraphElement<?, ?>) this.result).delete();
		}
		return super.setResult(result);
	}

	/**
	 * As the default value {@link StackElement#result} keeps unchanged.
	 */
	@Override
	public void createDefaultValue() {
		assert isResultAlreadySet || result == null;
	}

}
