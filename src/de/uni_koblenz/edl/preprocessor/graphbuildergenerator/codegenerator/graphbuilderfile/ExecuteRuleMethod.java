package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;

public class ExecuteRuleMethod implements ExecuteMethodGenerator {

	private String javaDocPrefix = "";

	private Rule rule;

	private final Map<Integer, ExecutePositionMethod> executePositionMethods = new HashMap<Integer, ExecutePositionMethod>();

	private final Map<Integer, ExecuteTermMethod> executeTermMethods = new HashMap<Integer, ExecuteTermMethod>();

	private int numberOfTermsInBody;

	private String methodName;

	public ExecuteRuleMethod(Rule rule) {
		this.rule = rule;
		numberOfTermsInBody = rule.getNumberOfTermsInBody();
	}

	public void setRule(Rule rule) {
		this.rule = rule;
		numberOfTermsInBody = rule.getNumberOfTermsInBody();
	}

	public Rule getRule() {
		return rule;
	}

	public void addExecuteMethod(ExecuteMethodGenerator executeMethod) {
		if (executeMethod instanceof ExecutePositionMethod) {
			int index = ((ExecutePositionMethod) executeMethod).getPosition();
			assert index <= numberOfTermsInBody : index + " must be <= "
					+ numberOfTermsInBody + " concerning rule: " + rule;
			executePositionMethods.put(index,
					(ExecutePositionMethod) executeMethod);
		} else {
			assert executeMethod instanceof ExecuteTermMethod;
			int index = ((ExecuteTermMethod) executeMethod).getTermIndex();
			assert index < numberOfTermsInBody : index + " must be < "
					+ numberOfTermsInBody + " concerning rule: " + rule;
			executeTermMethods.put(index, (ExecuteTermMethod) executeMethod);
		}
	}

	public String getMethodName(String prefix) {
		if (methodName == null) {
			methodName = prefix + "_Rule" + rule.getNumber();
		}
		return methodName;
	}

	@Override
	public void generateCode(Appendable appendable, Module module,
			SemanticActionGenerator semanticActionGenerator) throws IOException {
		assert methodName != null : "getMethodName(String prefix) must be called first!";
		String currentJavaDoc = javaDocPrefix
				+ "\t * Rule "
				+ rule.getNumber()
				+ ": "
				+ rule.toString().replace(">", "&gt;").replace("<", "&lt;")
						.replace("*/", "*&#47") + "\n";

		appendable.append("\n\t/**\n");
		appendable.append(currentJavaDoc);
		appendable.append("\t */\n");
		appendable
				.append("\tprivate void ")
				.append(methodName)
				.append("(StackElement currentElement, StackElement parent) {\n");
		appendable
				.append("\t\tint currentPos = parent.getCurrentSemanticActionPosition();\n");
		if (executePositionMethods.isEmpty()) {
			appendable.append("\t\tif (currentElement != parent) {\n");
		} else {
			appendable.append("\t\tif (currentElement == parent) {\n");
			appendable.append("\t\t\tswitch (currentPos) {\n");
			for (Entry<Integer, ExecutePositionMethod> entry : executePositionMethods
					.entrySet()) {
				appendable.append("\t\t\tcase ")
						.append(Integer.toString(entry.getKey())).append(":\n");
				appendable.append("\t\t\t\t")
						.append(entry.getValue().getMethodName(methodName))
						.append("(currentElement);\n");
				appendable.append("\t\t\t\tbreak;\n");
			}
			appendable.append("\t\t\t}\n");
			appendable.append("\t\t}");
			if (executeTermMethods.isEmpty()) {
				appendable.append("\n");
			} else {
				appendable.append(" else {\n");
			}
		}
		if (!executeTermMethods.isEmpty()) {
			appendable.append("\t\t\tswitch (currentPos - 1) {\n");
			for (Entry<Integer, ExecuteTermMethod> entry : executeTermMethods
					.entrySet()) {
				appendable.append("\t\t\tcase ")
						.append(Integer.toString(entry.getKey())).append(":\n");
				appendable.append("\t\t\t\t")
						.append(entry.getValue().getMethodName(methodName))
						.append("(currentElement, parent);\n");
				appendable.append("\t\t\t\tbreak;\n");
			}
			appendable.append("\t\t\t}\n");
			appendable.append("\t\t}\n");
		}
		appendable.append("\t}\n");

		for (Entry<Integer, ExecutePositionMethod> entry : executePositionMethods
				.entrySet()) {
			currentJavaDoc = javaDocPrefix
					+ "\t * Rule "
					+ rule.getNumber()
					+ ": "
					+ rule.toStringWithMarkedPosition(entry.getKey())
							.replaceAll("([^\\<])(\\/)", "$1&#47") + "\n";
			entry.getValue().setJavaDocPrefix(currentJavaDoc);
			entry.getValue().generateCode(appendable, module,
					semanticActionGenerator);
		}

		for (Entry<Integer, ExecuteTermMethod> entry : executeTermMethods
				.entrySet()) {
			currentJavaDoc = javaDocPrefix
					+ "\t * Rule "
					+ rule.getNumber()
					+ ": "
					+ rule.toStringWithMarkedTerm(entry.getKey()).replace("*/",
							"*&#47") + "\n";
			entry.getValue().setJavaDocPrefix(currentJavaDoc);
			entry.getValue().generateCode(appendable, module,
					semanticActionGenerator);
		}
	}

	public void setJavaDocPrefix(String javaDocPrefix) {
		this.javaDocPrefix = javaDocPrefix.substring(0,
				javaDocPrefix.length() - 1) + "<br>\n";
	}

	@Override
	public boolean isEmpty() {
		return executePositionMethods.isEmpty() && executeTermMethods.isEmpty();
	}

	@Override
	public String toString() {
		return rule.toString();
	}
}
