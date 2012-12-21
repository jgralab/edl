package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;

public class ExecuteTermMethod implements ExecuteMethodGenerator {

	private final Map<Rule, ExecuteRuleMethod> executeRuleMethods = new HashMap<Rule, ExecuteRuleMethod>();

	private String methodName;

	private int termIndex;

	private String javaDocPrefix;

	private boolean wasAlreadyGenerated = false;

	public ExecuteTermMethod(int termIndex) {
		this.termIndex = termIndex;
	}

	public void addExecuteMethod(Rule rule, ExecuteRuleMethod executeMethod) {
		executeRuleMethods.put(rule, executeMethod);
	}

	public String getMethodName(String prefix) {
		if (methodName == null) {
			methodName = prefix + "_Term" + termIndex;
		}
		return methodName;
	}

	@Override
	public void generateCode(Appendable appendable, Module module,
			SemanticActionGenerator semanticActionGenerator) throws IOException {
		if (wasAlreadyGenerated) {
			return;
		}
		wasAlreadyGenerated = true;
		assert methodName != null : "getMethodName(String prefix) must be called first!";
		String currentJavaDoc = javaDocPrefix;

		appendable.append("\n\t/**\n");
		appendable.append(currentJavaDoc);
		appendable.append("\t */\n");
		appendable
				.append("\tprivate void ")
				.append(methodName)
				.append("(StackElement currentElement, StackElement parent) {\n");
		appendable
				.append("\t\tStackElement nextParent = skipElementsWithNoSemanticAction(parent, currentElement);\n");
		appendable
				.append("\t\tswitch (nextParent.getAppliedRule().getNumber()) {\n");
		for (Entry<Rule, ExecuteRuleMethod> entry : executeRuleMethods
				.entrySet()) {
			Rule rule = entry.getKey();
			appendable.append("\t\tcase ")
					.append(Integer.toString(rule.getNumber())).append(":\n");
			appendable.append("\t\t\t// ").append(entry.getKey().toString())
					.append("\n");
			appendable.append("\t\t\t")
					.append(entry.getValue().getMethodName(methodName))
					.append("(currentElement, nextParent);\n");
			appendable.append("\t\t\tbreak;\n");
		}
		appendable.append("\t\t}\n");
		appendable.append("\t}\n");

		for (Entry<Rule, ExecuteRuleMethod> entry : executeRuleMethods
				.entrySet()) {
			entry.getValue().setJavaDocPrefix(currentJavaDoc);
			entry.getValue().generateCode(appendable, module,
					semanticActionGenerator);
		}
	}

	public void setJavaDocPrefix(String javaDocPrefix) {
		this.javaDocPrefix = javaDocPrefix;
	}

	@Override
	public boolean isEmpty() {
		return executeRuleMethods.isEmpty();
	}

	public void setTermIndex(int termIndex) {
		this.termIndex = termIndex;
	}

	public int getTermIndex() {
		return termIndex;
	}
}
