package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;

public class MainExecuteMethod implements ExecuteMethodGenerator {

	private final Map<Module, Set<ExecuteRuleMethod>> executeRuleMethods = new HashMap<Module, Set<ExecuteRuleMethod>>();

	public void addExecuteMethod(Module module, Rule rule,
			ExecuteRuleMethod executeRuleMethod) {
		Set<ExecuteRuleMethod> methodsOfModule = executeRuleMethods.get(module);
		if (methodsOfModule == null) {
			methodsOfModule = new HashSet<ExecuteRuleMethod>();
			executeRuleMethods.put(module, methodsOfModule);
		}
		methodsOfModule.add(executeRuleMethod);
	}

	public void generateCode(Appendable appendable,
			SemanticActionGenerator semanticActionGenerator) throws IOException {
		appendable.append("\n\t@Override\n");
		appendable.append("\tpublic void execute(Stack stack) {\n");
		if (!executeRuleMethods.isEmpty()) {
			appendable
					.append("\t\tStackElement currentElement = stack.getCurrentElement();\n");
			appendable
					.append("\t\tassert currentElement.getAppliedRule() != null : \"There must not be called\"\n");
			appendable
					.append("\t\t\t+ \" any semantic actions for leafs which recognize an input character.\";\n");
			appendable.append("\t\tStackElement parent = currentElement\n");
			appendable.append("\t\t\t.getParentApplicationOfDefinedRule();\n");
			appendable
					.append("\t\tswitch (parent.getAppliedRule().getNumber()) {\n");
			for (Entry<Module, Set<ExecuteRuleMethod>> entry : executeRuleMethods
					.entrySet()) {
				appendable
						.append("\t\t// #########################################\n");
				appendable.append("\t\t// Module ")
						.append(entry.getKey().get_identifier().get_name())
						.append("\n");
				appendable
						.append("\t\t// #########################################\n");
				for (ExecuteRuleMethod method : entry.getValue()) {
					Rule rule = method.getRule();
					appendable.append("\t\tcase ")
							.append(Integer.toString(rule.getNumber()))
							.append(":\n");
					if (rule.getType() == RuleType.DEFINED) {
						appendable
								.append("\t\t\t// ")
								.append(rule.isContextFree() ? "cf: " : "lex: ")
								.append(rule.getDefinedRepresentation())
								.append("\n");
					} else {
						assert rule.getType() == RuleType.START;
						String ruleString = rule.toString();
						if (rule.isContextFree()) {
							ruleString = ruleString.substring(15,
									ruleString.length() - 24);
						} else if (ruleString.startsWith("lex(")
								|| ruleString.startsWith("var(")) {
							ruleString = ruleString.substring(4,
									ruleString.length() - 12);
						}
						appendable
								.append("\t\t\t// ")
								.append(rule.isContextFree() ? "context-free "
										: "lexical ").append("start symbol: ")
								.append(ruleString).append("\n");
					}
					appendable.append("\t\t\t")
							.append(method.getMethodName("execute"))
							.append("(currentElement, parent);\n");
					appendable.append("\t\t\tbreak;\n");

				}
			}
			appendable.append("\t\t}\n");
		}
		appendable.append("\t}\n");
		for (Entry<Module, Set<ExecuteRuleMethod>> entry : executeRuleMethods
				.entrySet()) {
			for (ExecuteRuleMethod method : entry.getValue()) {
				method.generateCode(appendable, entry.getKey(),
						semanticActionGenerator);
			}
		}
	}

	@Override
	public void generateCode(Appendable appendable, Module module,
			SemanticActionGenerator semanticActionGenerator) throws IOException {
		generateCode(appendable, semanticActionGenerator);
	}

	@Override
	public boolean isEmpty() {
		return executeRuleMethods.isEmpty();
	}

}
