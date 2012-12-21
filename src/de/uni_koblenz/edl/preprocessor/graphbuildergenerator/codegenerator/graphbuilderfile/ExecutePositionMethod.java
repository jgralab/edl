package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StatementSemanticAction;
import de.uni_koblenz.jgralab.EdgeDirection;

public class ExecutePositionMethod implements ExecuteMethodGenerator {

	private final StatementSemanticAction semanticAction;

	private String methodName;

	private final int position;

	private String javaDocPrefix;

	private boolean wasAlreadyGenerated = false;

	private final Rule definedBaseRule;

	public ExecutePositionMethod(int position, SemanticAction semanticAction,
			Rule definedRule) {
		this.position = position;
		this.semanticAction = (StatementSemanticAction) semanticAction;
		definedBaseRule = definedRule;
	}

	public String getMethodName(String prefix) {
		if (methodName == null) {
			methodName = prefix + "_Position" + position;
		}
		return methodName;
	}

	@Override
	public void generateCode(Appendable appendable, Module module,
			SemanticActionGenerator semanticActionGenerator) throws IOException {
		if (!wasAlreadyGenerated) {
			wasAlreadyGenerated = true;
			assert methodName != null : "getMethodName(String prefix) must be called first!";
			String currentJavaDoc = javaDocPrefix;

			appendable.append("\n\t/**\n");
			appendable.append(currentJavaDoc);
			appendable.append("\t */\n");
			appendable.append("\tprivate void ").append(methodName)
					.append("(StackElement currentElement) {\n");
			semanticActionGenerator
					.createCodeForStatementsInStatementSemanticAction(
							appendable, semanticAction, module,
							definedBaseRule, false);
			appendable.append("\t}\n");
		}
	}

	public void setJavaDocPrefix(String javaDocPrefix) {
		this.javaDocPrefix = javaDocPrefix;
	}

	@Override
	public boolean isEmpty() {
		return semanticAction == null
				|| semanticAction.getDegree(EdgeDirection.IN) == 0;
	}

	public int getPosition() {
		return position;
	}

	public Rule getDefinedBaseRule() {
		return definedBaseRule;
	}

}
