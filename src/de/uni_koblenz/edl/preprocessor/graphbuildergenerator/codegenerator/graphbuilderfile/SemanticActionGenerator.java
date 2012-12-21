package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLEdge;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLVertex;
import de.uni_koblenz.edl.preprocessor.schema.common.EnumConstant;
import de.uni_koblenz.edl.preprocessor.schema.common.Enumeration;
import de.uni_koblenz.edl.preprocessor.schema.common.GraphElementClass;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.common.Record;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.AlphaConstant;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Assignment;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.BodyVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.BooleanLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ConstructorCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccessible;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DoubleLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.EnumAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Expression;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Field;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.HeadVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IntegerLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedVariableOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsExpressionOfStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsParameterOfConstructor;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsParameterOfMethod;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsStatementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsTypeOfCreatedRecord;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.JavaCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ListAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.LongLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.MethodCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.NullLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.OmegaConstant;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Statement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StatementSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StringLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SymbolTableVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.TemporaryVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.UserCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Variable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.VariableAccess;
import de.uni_koblenz.edl.preprocessor.schema.term.IsAttachedBy;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;

public class SemanticActionGenerator {

	private Schema schema;

	private final Map<SymbolTableDefinition, String> symbolTable2fieldname;

	public SemanticActionGenerator(
			Map<SymbolTableDefinition, String> symbolTable2fieldname) {
		this.symbolTable2fieldname = symbolTable2fieldname;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	/*
	 * Following methods handle SemanticAction
	 */

	private int uniqueMethodId = 0;

	private final Map<SemanticAction, String> semanticAction2MethodName = new HashMap<SemanticAction, String>();

	private final List<StringBuilder> suffixMethods = new ArrayList<StringBuilder>();

	public String createSemanticActionMethod(SemanticAction semanticAction,
			Module module, Rule definedRule, boolean isInDefaultValue)
			throws IOException {
		String methodName = semanticAction2MethodName.get(semanticAction);
		if (methodName == null) {
			methodName = "semanticAction_" + uniqueMethodId++;
			semanticAction2MethodName.put(semanticAction, methodName);
			boolean needsReturn = semanticAction
					.isInstanceOf(ExpressionSemanticAction.VC);

			StringBuilder sb = new StringBuilder();
			sb.append("\t/**\n");
			EDLEdge edlEdge = getParentEdge(semanticAction);
			sb.append("\t * module: ")
					.append(module.get_identifier().get_name())
					.append("<br>\n");
			sb.append("\t * line: ").append(edlEdge.get_line())
					.append(" column: ").append(edlEdge.get_column())
					.append(" length: ").append(edlEdge.get_length());
			if (definedRule != null) {
				sb.append("<br>\n");
				sb.append("\t * rule: ").append(definedRule.toString())
						.append("\n");
			} else {
				sb.append("\n");
			}
			sb.append("\t */\n");
			sb.append("\tprivate ").append(needsReturn ? "Object" : "void")
					.append(" ").append(methodName)
					.append("(StackElement currentElement) {\n");
			if (needsReturn) {
				createCodeForExpressionInExpressionSemanticAction(sb,
						(ExpressionSemanticAction) semanticAction, module,
						definedRule, isInDefaultValue);
			} else {
				createCodeForStatementsInStatementSemanticAction(sb,
						(StatementSemanticAction) semanticAction, module,
						definedRule, isInDefaultValue);
			}
			sb.append("\t}");
			suffixMethods.add(sb);
		}
		return methodName;
	}

	private EDLEdge getParentEdge(EDLVertex vertex) {
		EDLEdge edge = vertex.getFirstEDLEdgeIncidence(EdgeDirection.OUT);
		return edge;
	}

	private void createCodeForExpressionInExpressionSemanticAction(
			Appendable appendable,
			ExpressionSemanticAction expressionSemanticAction, Module module,
			Rule definedRule, boolean isInDefaultValue) throws IOException {
		StringBuilder prettyPrinted = new StringBuilder();
		EDLEdge parentEdge = expressionSemanticAction
				.getFirstIsExpressionOfSemanticActionIncidence(EdgeDirection.IN);
		appendable.append("\t\ttry {\n");
		appendable.append("\t\t\treturn ");
		generate(prettyPrinted, appendable,
				expressionSemanticAction.get_expression(), module, definedRule,
				false, isInDefaultValue);

		appendable.append(";\n");
		appendable.append("\t\t} catch (Throwable t) {\n");
		appendable.append("\t\t\tthrow new SemanticActionException(\"")
				.append(prettyPrinted).append(";\\n\"\n");
		appendable.append("\t\t\t\t+ \"module: ")
				.append(module.get_identifier().get_name()).append(" line: ")
				.append(Integer.toString(parentEdge.get_line()))
				.append(" column: ")
				.append(Integer.toString(parentEdge.get_column()))
				.append(" length: ")
				.append(Integer.toString(parentEdge.get_length()))
				.append("\\n\"");
		if (definedRule != null) {
			appendable
					.append("\n\t\t\t\t+ \"at rule: \" + ")
					.append(GraphIO
							.toUtfString(definedRule.getType() == RuleType.DEFINED ? definedRule
									.getDefinedRepresentation()
									: "rule of "
											+ (definedRule.isContextFree() ? "context-free"
													: "lexical")
											+ " start-symbol: "
											+ definedRule.toString()))
					.append(" + \"\\n\"");
		}
		appendable.append(" + t.toString(), t);\n");
		appendable.append("\t\t}\n");
	}

	void createCodeForStatementsInStatementSemanticAction(
			Appendable appendable,
			StatementSemanticAction statementSemanticAction, Module module,
			Rule definedRule, boolean isInDefaultValue) throws IOException {
		for (IsStatementOf iso : statementSemanticAction
				.getIsStatementOfIncidences(EdgeDirection.IN)) {
			createCodeForStatement(appendable, (Statement) iso.getThat(),
					module, definedRule, isInDefaultValue);
		}
	}

	public void createSemanticActionsMethods(Appendable appendable)
			throws IOException {
		if (!suffixMethods.isEmpty()) {
			appendable.append("\n");
			appendable.append("\t// #######################################\n");
			appendable.append("\t// methods needed by user specific code ##\n");
			for (StringBuilder sb : suffixMethods) {
				appendable.append("\n").append(sb).append("\n");
			}
			appendable.append("\n");
			appendable.append("\t// methods needed by user specific code ##\n");
			appendable.append("\t// #######################################\n");
		}
	}

	/*
	 * Following methods handle statements
	 */

	private void createCodeForStatement(Appendable appendable,
			Statement statement, Module module, Rule definedRule,
			boolean isInDefaultValue) throws IOException {
		StringBuilder prettyPrinted = new StringBuilder();

		Expression expression = null;
		if (statement.isInstanceOf(ExpressionStatement.VC)) {
			expression = (Expression) statement.getFirstIncidence(
					IsExpressionOfStatement.EC, EdgeDirection.IN).getThat();
		} else if (statement.isInstanceOf(UserCode.VC)) {
			expression = (Expression) statement;
		}

		EDLEdge parentEdge = statement
				.getFirstIsStatementOfIncidence(EdgeDirection.OUT);

		appendable.append("\t\ttry {\n");
		appendable.append("\t\t\t");
		if (expression != null) {
			generate(prettyPrinted, appendable, expression, module,
					definedRule, false, isInDefaultValue);
		}
		appendable.append(";\n");
		appendable.append("\t\t} catch (Throwable t) {\n");
		appendable.append("\t\t\tthrow new SemanticActionException(\"")
				.append(prettyPrinted).append(";\\n\"\n");
		appendable.append("\t\t\t\t+ \"module: ")
				.append(module.get_identifier().get_name()).append(" line: ")
				.append(Integer.toString(parentEdge.get_line()))
				.append(" column: ")
				.append(Integer.toString(parentEdge.get_column()))
				.append(" length: ")
				.append(Integer.toString(parentEdge.get_length()))
				.append("\\n\"");
		if (definedRule != null) {
			appendable
					.append("\n\t\t\t\t+ \"at rule: \" + ")
					.append(GraphIO
							.toUtfString(definedRule.getType() == RuleType.DEFINED ? definedRule
									.getDefinedRepresentation()
									: "rule of "
											+ (definedRule.isContextFree() ? "context-free"
													: "lexical")
											+ " start-symbol: "
											+ definedRule.toString()))
					.append(" + \"\\n\"");
		}
		appendable.append(" + t.toString(), t);\n");
		appendable.append("\t\t}\n");
	}

	/*
	 * Following methods handle expressions
	 */

	private int uniqueUserCodeMethodId = 0;

	private final Map<UserCode, String> userCode2MethodName = new HashMap<UserCode, String>();

	public void generate(Appendable prettyPrinted, Appendable appendable,
			Expression expression, Module module, Rule definedRule,
			boolean isLhsOfAssignment, boolean isInDefaultValue)
			throws IOException {
		if (expression.isInstanceOf(UserCode.VC)) {
			generateUserCode(prettyPrinted, appendable, (UserCode) expression,
					module, definedRule, isInDefaultValue);
		} else if (expression.isInstanceOf(NullLiteral.VC)) {
			generateNullLiteral(prettyPrinted, appendable,
					(NullLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(BooleanLiteral.VC)) {
			generateBooleanLiteral(prettyPrinted, appendable,
					(BooleanLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(IntegerLiteral.VC)) {
			generateIntegerLiteral(prettyPrinted, appendable,
					(IntegerLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(LongLiteral.VC)) {
			generateLongLiteral(prettyPrinted, appendable,
					(LongLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(DoubleLiteral.VC)) {
			generateDoubleLiteral(prettyPrinted, appendable,
					(DoubleLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(StringLiteral.VC)) {
			generateStringLiteral(prettyPrinted, appendable,
					(StringLiteral) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(Assignment.VC)) {
			generateAssignment(prettyPrinted, appendable,
					(Assignment) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(VariableAccess.VC)) {
			Variable variable = ((VariableAccess) expression).get_variable();
			if (variable.isInstanceOf(HeadVariable.VC)) {
				generateHeadVariable(prettyPrinted, appendable,
						(HeadVariable) variable, module, definedRule,
						isLhsOfAssignment, isInDefaultValue);
			} else if (variable.isInstanceOf(BodyVariable.VC)) {
				generateBodyVariable(
						prettyPrinted,
						appendable,
						(BodyVariable) variable,
						module,
						definedRule,
						isLhsOfAssignment,
						((VariableAccess) expression)
								.getFirstIsAccessedVariableOfIncidence(EdgeDirection.IN),
						isInDefaultValue);
			} else if (variable.isInstanceOf(TemporaryVariable.VC)) {
				generateTemporaryVariable(prettyPrinted, appendable,
						(TemporaryVariable) variable, module, definedRule,
						isLhsOfAssignment, isInDefaultValue);
			} else if (variable.isInstanceOf(SymbolTableVariable.VC)) {
				generateSymbolTableVariable(prettyPrinted, appendable,
						(SymbolTableVariable) variable, module, definedRule,
						isLhsOfAssignment, isInDefaultValue);
			}
		} else if (expression.isInstanceOf(ListAccess.VC)) {
			generateListAccess(prettyPrinted, appendable,
					(ListAccess) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(DotAccess.VC)) {
			generateDotAccess(prettyPrinted, appendable,
					(DotAccess) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(EnumAccess.VC)) {
			generateEnumAccess(prettyPrinted, appendable,
					(EnumAccess) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(ConstructorCall.VC)) {
			generateConstructorCall(prettyPrinted, appendable,
					(ConstructorCall) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else if (expression.isInstanceOf(MethodCall.VC)) {
			generateMethodCall(prettyPrinted, appendable,
					(MethodCall) expression, module, definedRule,
					isLhsOfAssignment, isInDefaultValue);
		} else {
			throw new GrammarException("Unsupported Expression vertex: "
					+ expression);
		}
	}

	private void generateMethodCall(Appendable prettyPrinted,
			Appendable appendable, MethodCall methodCall, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		int numberOfParameter = methodCall.getDegree(IsParameterOfMethod.EC,
				EdgeDirection.IN);
		boolean createReflectionCall = true;
		String appendDelim = "";
		String nameOfMethod = methodCall.get_identifier().get_name();
		prettyPrinted.append(nameOfMethod).append("(");
		if (nameOfMethod.equals("graph") && numberOfParameter == 0) {
			appendable.append("getGraph()");
			prettyPrinted.append(")");
			return;
		} else if (nameOfMethod.equals("file") && numberOfParameter == 0) {
			appendable.append("currentElement.getNameOfParsedFile()");
			prettyPrinted.append(")");
			return;
		} else if (nameOfMethod.equals("file") && numberOfParameter == 0) {
			appendable.append("currentElement.getNameOfParsedFile()");
			prettyPrinted.append(")");
			return;
		} else if ((nameOfMethod.equals("offset")
				|| nameOfMethod.equals("line") || nameOfMethod.equals("column")
				|| nameOfMethod.equals("length") || nameOfMethod
					.equals("lexem"))
				&& (numberOfParameter == 1 || (numberOfParameter == 0 && isInDefaultValue))) {
			Expression param = numberOfParameter == 0 ? null : methodCall
					.getFirstIsParameterOfMethodIncidence(EdgeDirection.IN)
					.getAlpha();
			if (param != null && param.isInstanceOf(VariableAccess.VC)) {
				Variable variable = ((VariableAccess) param).get_variable();
				if (variable.isInstanceOf(BodyVariable.VC)) {
					int index = ((BodyVariable) variable).get_index();
					appendable.append("currentElement.getChild(")
							.append(Integer.toString(index)).append(").get");
					if (nameOfMethod.equals("line")
							|| nameOfMethod.equals("column")) {
						appendable.append("First");
					}
					appendable
							.append(Character.toUpperCase(nameOfMethod
									.charAt(0)))
							.append(nameOfMethod.substring(1)).append("()");
					prettyPrinted
							.append("$")
							.append(Integer.toString(adjustIndex(index,
									definedRule))).append(")");
					return;
				} else if (variable.isInstanceOf(HeadVariable.VC)) {
					appendable
							.append("currentElement.getParentApplicationOfDefinedRule().get");
					if (nameOfMethod.equals("line")
							|| nameOfMethod.equals("column")) {
						appendable.append("First");
					}
					appendable
							.append(Character.toUpperCase(nameOfMethod
									.charAt(0)))
							.append(nameOfMethod.substring(1)).append("()");
					prettyPrinted.append("$").append(")");
					return;
				}
			} else if (isInDefaultValue) {
				appendable.append("positionsMap.get(");
				if (param == null) {
					appendable.append("vertex");
				} else if (param.isInstanceOf(AlphaConstant.VC)) {
					appendable.append("edge.getAlpha()");
					prettyPrinted.append("alpha");
				} else if (param.isInstanceOf(OmegaConstant.VC)) {
					appendable.append("edge.getOmega()");
					prettyPrinted.append("omega");
				} else {
					generate(prettyPrinted, appendable, param, module,
							definedRule, false, isInDefaultValue);
				}
				prettyPrinted.append(")");
				appendable.append(").get");
				if (nameOfMethod.equals("line")
						|| nameOfMethod.equals("column")) {
					appendable.append("First");
				}
				appendable
						.append(Character.toUpperCase(nameOfMethod.charAt(0)))
						.append(nameOfMethod.substring(1)).append("()");
				return;
			}
		} else if (nameOfMethod.equals("list") || nameOfMethod.equals("set")
				|| (nameOfMethod.equals("map") && numberOfParameter % 2 == 0)) {
			appendable.append("create")
					.append(Character.toUpperCase(nameOfMethod.charAt(0)))
					.append(nameOfMethod.substring(1)).append("(");
			appendDelim = "";
			createReflectionCall = false;
		} else if (nameOfMethod.equals("lift") && numberOfParameter == 1) {
			appendable.append("currentElement.setResult(");
			generate(prettyPrinted, appendable, methodCall
					.getFirstIsParameterOfMethodIncidence(EdgeDirection.IN)
					.getAlpha(), module, definedRule, false, isInDefaultValue);
			appendable.append(")");
			prettyPrinted.append(")");
			return;
		} else if (nameOfMethod.equals("getWhitespaceBefore")
				&& numberOfParameter == 1) {
			Expression param = methodCall.getFirstIsParameterOfMethodIncidence(
					EdgeDirection.IN).getAlpha();
			if (param.isInstanceOf(VariableAccess.VC)) {
				Variable variable = ((VariableAccess) param).get_variable();
				if (variable.isInstanceOf(BodyVariable.VC)) {
					int index = ((BodyVariable) variable).get_index();
					appendable.append("currentElement.getWhitespaceBefore(")
							.append(Integer.toString(index)).append(")");
					prettyPrinted
							.append("$")
							.append(Integer.toString(adjustIndex(index,
									definedRule))).append(")");
					return;
				}
			}
		} else if (nameOfMethod.equals("getPrefixWhitespace")
				&& numberOfParameter == 0
				&& definedRule.getType() == RuleType.START) {
			appendable.append("currentElement.getChild(0).getResult()");
			prettyPrinted.append(")");
			return;
		} else if (nameOfMethod.equals("getSuffixWhitespace")
				&& numberOfParameter == 0
				&& definedRule.getType() == RuleType.START) {
			appendable.append("currentElement.getChild(2).getResult()");
			prettyPrinted.append(")");
			return;
		}

		if (createReflectionCall) {
			appendable.append("callMethod(this, \"").append(nameOfMethod)
					.append("\"");
			appendDelim = ", ";
		}

		String prettyPrintDelim = "";
		for (IsParameterOfMethod ipom : methodCall
				.getIsParameterOfMethodIncidences(EdgeDirection.IN)) {
			Expression param = (Expression) ipom.getThat();
			prettyPrinted.append(prettyPrintDelim);
			appendable.append(appendDelim);
			generate(prettyPrinted, appendable, param, module, definedRule,
					false, isInDefaultValue);
			prettyPrintDelim = ", ";
			appendDelim = ", ";
		}
		appendable.append(")");
		prettyPrinted.append(")");
	}

	private void generateConstructorCall(Appendable prettyPrinted,
			Appendable appendable, ConstructorCall constructorCall,
			Module module, Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		int numberOfParameter = constructorCall.getDegree(
				IsParameterOfConstructor.EC, EdgeDirection.IN);
		switch (numberOfParameter) {
		case 0:
			// vertex is created
			GraphElementClass vertexClass = constructorCall
					.get_graphElementClass();
			assert vertexClass != null;
			String nameOfVertexClass = vertexClass.get_identifier().get_name();
			prettyPrinted.append(nameOfVertexClass).append("()");
			appendable
					.append("createVertex(\"")
					.append(nameOfVertexClass)
					.append("\", currentElement.getParentApplicationOfDefinedRule().getPosition())");
			break;
		case 1:
			// record is created
			for (IsTypeOfCreatedRecord itocr : constructorCall
					.getIsTypeOfCreatedRecordIncidences(EdgeDirection.IN)) {
				Record record = (Record) itocr.getThat();
				String nameOfRecord = record.get_identifier().get_name();
				prettyPrinted.append(nameOfRecord).append("(");
				appendable.append("createRecord(\"").append(nameOfRecord)
						.append("\", ");
				for (IsParameterOfConstructor ipoc : constructorCall
						.getIsParameterOfConstructorIncidences(EdgeDirection.IN)) {
					Expression param = (Expression) ipoc.getThat();
					generate(prettyPrinted, appendable, param, module,
							definedRule, false, isInDefaultValue);
				}
				prettyPrinted.append(")");
				appendable.append(")");
			}
			break;
		case 2:
			// edge is created
			GraphElementClass edgeClass = constructorCall
					.get_graphElementClass();
			assert edgeClass != null;
			String nameOfEdgeClass = edgeClass.get_identifier().get_name();
			prettyPrinted.append(nameOfEdgeClass).append("(");
			appendable.append("createEdge(\"").append(nameOfEdgeClass)
					.append("\"");
			String delim = "";
			for (IsParameterOfConstructor ipoc : constructorCall
					.getIsParameterOfConstructorIncidences(EdgeDirection.IN)) {
				Expression param = (Expression) ipoc.getThat();
				prettyPrinted.append(delim);
				appendable.append(", ");
				generate(prettyPrinted, appendable, param, module, definedRule,
						false, isInDefaultValue);
				delim = ", ";
			}
			prettyPrinted.append(")");
			appendable.append(")");
			break;
		default:
			throw new GrammarException("Too many parameters for a constructor");
		}
	}

	private void generateEnumAccess(Appendable prettyPrinted,
			Appendable appendable, EnumAccess enumAccess, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		EnumConstant enumConstant = enumAccess.get_constant();
		Enumeration enumeration = enumConstant.get_enumeration();
		String enumerationName = enumeration.get_identifier().get_name();
		prettyPrinted.append(enumerationName).append(".")
				.append(enumConstant.get_value());
		appendable.append("getEnumConstant(\"")
				.append(schema.getPackagePrefix()).append(".")
				.append(enumerationName).append("\", \"")
				.append(enumConstant.get_value()).append("\")");
	}

	private void generateDotAccess(Appendable prettyPrinted,
			Appendable appendable, DotAccess dotAccess, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		Expression lhsOfDot = dotAccess.get_accesedElement();
		DotAccessible rhsOfDot = dotAccess.get_dotAccessible();
		if (rhsOfDot.isInstanceOf(AlphaConstant.VC)) {
			appendable.append("((Edge) ");
			generate(prettyPrinted, appendable, lhsOfDot, module, definedRule,
					false, isInDefaultValue);
			appendable.append(").");
			if (isLhsOfAssignment) {
				appendable.append("setAlpha((Vertex) ");
			} else {
				appendable.append("getAlpha()");
			}
			prettyPrinted.append(".alpha");
		} else if (rhsOfDot.isInstanceOf(OmegaConstant.VC)) {
			appendable.append("((Edge) ");
			generate(prettyPrinted, appendable, lhsOfDot, module, definedRule,
					false, isInDefaultValue);
			appendable.append(").");
			if (isLhsOfAssignment) {
				appendable.append("setOmega((Vertex) ");
			} else {
				appendable.append("getOmega()");
			}
			prettyPrinted.append(".omega");
		} else if (rhsOfDot.isInstanceOf(Field.VC)) {
			String nameOfField = ((Field) rhsOfDot).get_identifier().get_name();
			if (lhsOfDot.isInstanceOf(VariableAccess.VC)) {
				Variable variable = ((VariableAccess) lhsOfDot).get_variable();
				if (variable.isInstanceOf(SymbolTableVariable.VC)
						&& nameOfField.equals("namespace")) {
					generateSymbolTableVariable(prettyPrinted, appendable,
							(SymbolTableVariable) variable, module,
							definedRule, false, isInDefaultValue);
					prettyPrinted.append(".").append(nameOfField);
					appendable.append(".");
					if (isLhsOfAssignment) {
						appendable.append("setNameSpace((Vertex) ");
					} else {
						appendable.append("getNameSpace()");
					}
					return;
				}
			}
			if (isLhsOfAssignment) {
				appendable.append("setAttribute(");
			} else {
				appendable.append("getAttribute(");
			}
			generate(prettyPrinted, appendable, lhsOfDot, module, definedRule,
					false, isInDefaultValue);
			prettyPrinted.append(".").append(nameOfField);
			appendable.append(", \"");
			appendable.append(nameOfField);
			appendable.append("\"");
			if (isLhsOfAssignment) {
				appendable.append(", ");
			} else {
				appendable.append(")");
			}
		} else if (rhsOfDot.isInstanceOf(MethodCall.VC)) {
			String nameOfMethod = ((MethodCall) rhsOfDot).get_identifier()
					.get_name();
			if (lhsOfDot.isInstanceOf(VariableAccess.VC)) {
				Variable variable = ((VariableAccess) lhsOfDot).get_variable();
				if (variable.isInstanceOf(SymbolTableVariable.VC)) {
					int numberOfParameter = rhsOfDot.getDegree(
							IsParameterOfMethod.EC, EdgeDirection.IN);
					if ((nameOfMethod.equals("push") && numberOfParameter == 0)
							|| (nameOfMethod.equals("pop") && numberOfParameter == 0)
							|| (nameOfMethod.equals("getTemporaryVertices") && numberOfParameter == 0)
							|| (nameOfMethod.equals("getAllTemporaryVertices") && numberOfParameter == 0)) {
						generateSymbolTableVariable(prettyPrinted, appendable,
								(SymbolTableVariable) variable, module,
								definedRule, false, isInDefaultValue);
						prettyPrinted.append(".").append(nameOfMethod)
								.append("()");
						appendable.append(".").append(nameOfMethod)
								.append("()");
						return;
					} else if (nameOfMethod.equals("declare")
							&& numberOfParameter == 2) {
						generateSymbolTableVariable(prettyPrinted, appendable,
								(SymbolTableVariable) variable, module,
								definedRule, false, isInDefaultValue);
						prettyPrinted.append(".").append(nameOfMethod)
								.append("(");
						appendable.append(".").append(nameOfMethod).append("(");
						boolean isFirstParam = true;
						for (IsParameterOfMethod ipom : rhsOfDot
								.getIsParameterOfMethodIncidences(EdgeDirection.IN)) {
							Expression param = (Expression) ipom.getThat();
							if (!isFirstParam) {
								prettyPrinted.append(", ");
								appendable.append(", (Vertex) ");
							}
							generate(prettyPrinted, appendable, param, module,
									definedRule, false, isInDefaultValue);
							isFirstParam = false;
						}
						prettyPrinted.append(")");
						appendable.append(")");
						return;
					} else {
						IsParameterOfMethod isParameter = rhsOfDot
								.getFirstIsParameterOfMethodIncidence(EdgeDirection.IN);
						if (nameOfMethod.equals("useOrDeclare")
								&& numberOfParameter == 1) {
							generateSymbolTableVariable(prettyPrinted,
									appendable, (SymbolTableVariable) variable,
									module, definedRule, false,
									isInDefaultValue);
							prettyPrinted.append(".").append(nameOfMethod)
									.append("(");
							appendable.append(".").append(nameOfMethod)
									.append("(");
							generate(prettyPrinted, appendable,
									(Expression) isParameter.getThat(), module,
									definedRule, false, isInDefaultValue);
							prettyPrinted.append(")");
							appendable
									.append(", currentElement.getParentApplicationOfDefinedRule().getPosition())");
							return;
						} else if (nameOfMethod.equals("useOrDeclare")
								&& numberOfParameter == 2) {
							generateSymbolTableVariable(prettyPrinted,
									appendable, (SymbolTableVariable) variable,
									module, definedRule, false,
									isInDefaultValue);
							prettyPrinted.append(".").append(nameOfMethod)
									.append("(");
							appendable.append(".").append(nameOfMethod)
									.append("(");
							generate(prettyPrinted, appendable,
									(Expression) isParameter.getThat(), module,
									definedRule, false, isInDefaultValue);
							prettyPrinted.append(", ");
							appendable.append(", getVertexClass(");
							generate(
									prettyPrinted,
									appendable,
									(Expression) isParameter.getNextIncidence(
											EdgeDirection.IN).getThat(),
									module, definedRule, false,
									isInDefaultValue);
							prettyPrinted.append(")");
							appendable
									.append(") , currentElement.getParentApplicationOfDefinedRule().getPosition())");
							return;
						} else if (nameOfMethod.equals("use")
								&& numberOfParameter == 1) {
							generateSymbolTableVariable(prettyPrinted,
									appendable, (SymbolTableVariable) variable,
									module, definedRule, false,
									isInDefaultValue);
							prettyPrinted.append(".").append(nameOfMethod)
									.append("(");
							appendable.append(".").append(nameOfMethod)
									.append("(");
							generate(prettyPrinted, appendable,
									(Expression) isParameter.getThat(), module,
									definedRule, false, isInDefaultValue);
							prettyPrinted.append(")");
							appendable.append(")");
							return;
						}
					}
				}
			}

			appendable.append("callMethod(");
			generate(prettyPrinted, appendable, lhsOfDot, module, definedRule,
					false, isInDefaultValue);
			prettyPrinted.append(".").append(nameOfMethod).append("(");
			appendable.append(", \"").append(nameOfMethod).append("\"");
			String delim = "";
			for (IsParameterOfMethod ipom : rhsOfDot
					.getIsParameterOfMethodIncidences(EdgeDirection.IN)) {
				Expression param = (Expression) ipom.getThat();
				prettyPrinted.append(delim);
				appendable.append(", ");
				generate(prettyPrinted, appendable, param, module, definedRule,
						false, isInDefaultValue);
				delim = ", ";
			}
			prettyPrinted.append(")");
			appendable.append(")");
		}
	}

	private void generateListAccess(Appendable prettyPrinted,
			Appendable appendable, ListAccess listAccess, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		Expression list = listAccess.get_list();
		Expression index = listAccess.get_index();

		if (isLhsOfAssignment) {
			appendable.append("set_ElementOfList(");
		} else {
			appendable.append("get_ElementOfList(");
		}
		generate(prettyPrinted, appendable, list, module, definedRule, false,
				isInDefaultValue);
		prettyPrinted.append("[");
		appendable.append(", (Integer) ");
		generate(prettyPrinted, appendable, index, module, definedRule, false,
				isInDefaultValue);
		prettyPrinted.append("]");
		if (isLhsOfAssignment) {
			appendable.append(", ");
		} else {
			appendable.append(")");
		}
	}

	private void generateSymbolTableVariable(Appendable prettyPrinted,
			Appendable appendable, SymbolTableVariable symbolTableVariable,
			Module module, Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		SymbolTableDefinition symbolTable = symbolTableVariable
				.get_symbolTableDefinition();
		String fieldNameOfSymbolTable = symbolTable2fieldname.get(symbolTable);
		prettyPrinted.append(symbolTable.get_identifier().get_name());
		appendable.append(fieldNameOfSymbolTable);
	}

	private void generateTemporaryVariable(Appendable prettyPrinted,
			Appendable appendable, TemporaryVariable temporaryVariable,
			Module module, Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		String variableName = temporaryVariable.get_identifier().get_name();
		prettyPrinted.append("$").append(variableName);
		if (isLhsOfAssignment) {
			appendable.append("currentElement.setValueOfTemporaryVariable(\"")
					.append(variableName).append("\", ");
		} else {
			appendable.append("currentElement.getValueOfTemporaryVariable(\"")
					.append(variableName).append("\")");
		}
	}

	private void generateBodyVariable(Appendable prettyPrinted,
			Appendable appendable, BodyVariable bodyVariable, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			IsAccessedVariableOf isAccessedVariableOf, boolean isInDefaultValue)
			throws IOException {
		prettyPrinted.append("$").append(
				Integer.toString(adjustIndex(bodyVariable.get_index(),
						definedRule)));
		if (isLhsOfAssignment) {
			appendable.append("currentElement.getChild(")
					.append(Integer.toString(bodyVariable.get_index()))
					.append(").setResult(");
		} else {
			if (bodyVariable.get_index() == 0
					&& belongsSemanticActionToTheSecondTermOfAList(isAccessedVariableOf)) {
				appendable
						.append("((de.uni_koblenz.edl.parser.stack.elements.ApplicationOfListRule) currentElement).getResultOfLastT1()");
			} else {
				appendable.append("currentElement.getChild(")
						.append(Integer.toString(bodyVariable.get_index()))
						.append(").getResult()");
			}
		}
	}

	private int adjustIndex(int index, Rule definedRule) {
		// in desugaring of context-free rules the indices where adjusted
		// undo this
		if (definedRule.isContextFree()) {
			return index / 2;
		} else {
			return index;
		}
		// FIXME FunctionTerms have wrong index
		// the corresponding rule start with cf(T1...Tn => T) ... -> T
	}

	private boolean belongsSemanticActionToTheSecondTermOfAList(
			IsAccessedVariableOf isAccessedVariableOf) {
		IsSemanticActionOf iso = (IsSemanticActionOf) getParentIsSemanticActionOf(
				isAccessedVariableOf).getNormalEdge().getReversedEdge();
		Term term = iso.getOmega();
		if (term.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.List.VC)) {
			int numberOfChildTerms = 0;
			int numberOfSemanticActionBetweenTerms = 0;
			for (Edge edge : term.incidences(EdgeDirection.IN)) {
				if (edge.isInstanceOf(IsAttachedBy.EC)) {
					continue;
				} else if (edge.isInstanceOf(IsPartOfBinaryTerm.EC)) {
					numberOfChildTerms++;
				} else if (numberOfChildTerms == 1) {
					numberOfSemanticActionBetweenTerms++;
				}
				if (edge == iso) {
					return numberOfChildTerms == 2
							|| (numberOfChildTerms == 1 && numberOfSemanticActionBetweenTerms == 2);
				}
			}
			return false;
		} else {
			return false;
		}
	}

	private Edge getParentIsSemanticActionOf(
			IsAccessedVariableOf isAccessedVariableOf) {
		Vertex current = isAccessedVariableOf.getOmega();
		while (current != null) {
			Edge edge = current.getFirstIncidence(EdgeDirection.OUT);
			if (edge.isInstanceOf(IsSemanticActionOf.EC)) {
				return edge;
			}
			current = edge.getThat();
		}
		return null;
	}

	private void generateHeadVariable(Appendable prettyPrinted,
			Appendable appendable, HeadVariable headVariable, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append("$");
		if (isLhsOfAssignment) {
			appendable
					.append("currentElement.getParentApplicationOfDefinedRule().setResult(");
		} else {
			appendable
					.append("currentElement.getParentApplicationOfDefinedRule().getResult()");
		}
	}

	private void generateAssignment(Appendable prettyPrinted,
			Appendable appendable, Assignment assignment, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		generate(prettyPrinted, appendable, assignment.get_assigned(), module,
				definedRule, true, isInDefaultValue);
		prettyPrinted.append(" = ");
		generate(prettyPrinted, appendable, assignment.get_value(), module,
				definedRule, false, isInDefaultValue);
		appendable.append(")");
	}

	private void generateStringLiteral(Appendable prettyPrinted,
			Appendable appendable, StringLiteral stringLiteral, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append("\\\"").append(stringLiteral.get_value())
				.append("\\\"");
		appendable.append("\"").append(stringLiteral.get_value()).append("\"");
	}

	private void generateDoubleLiteral(Appendable prettyPrinted,
			Appendable appendable, DoubleLiteral doubleLiteral, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append(Double.toString(doubleLiteral.get_value()));
		appendable.append(Double.toString(doubleLiteral.get_value()));
	}

	private void generateLongLiteral(Appendable prettyPrinted,
			Appendable appendable, LongLiteral longLiteral, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append(Long.toString(longLiteral.get_value()))
				.append("L");
		appendable.append(Long.toString(longLiteral.get_value())).append("L");
	}

	private void generateIntegerLiteral(Appendable prettyPrinted,
			Appendable appendable, IntegerLiteral integerLiteral,
			Module module, Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append(Integer.toString(integerLiteral.get_value()));
		appendable.append(Integer.toString(integerLiteral.get_value()));
	}

	private void generateBooleanLiteral(Appendable prettyPrinted,
			Appendable appendable, BooleanLiteral booleanLiteral,
			Module module, Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append(Boolean.toString(booleanLiteral.is_value()));
		appendable.append(Boolean.toString(booleanLiteral.is_value()));
	}

	private void generateNullLiteral(Appendable prettyPrinted,
			Appendable appendable, NullLiteral nullLiteral, Module module,
			Rule definedRule, boolean isLhsOfAssignment,
			boolean isInDefaultValue) throws IOException {
		prettyPrinted.append("null");
		appendable.append("null");
	}

	private void generateUserCode(Appendable prettyPrinted,
			Appendable appendable, UserCode usercode, Module module,
			Rule definedRule, boolean isInDefaultValue) throws IOException {
		prettyPrinted.append("{...}");

		String methodName = userCode2MethodName.get(usercode);
		if (methodName == null) {
			methodName = "userCode_" + uniqueUserCodeMethodId++;
			userCode2MethodName.put(usercode, methodName);
			boolean needsReturn = usercode.is_containsReturn();

			StringBuilder userCodeMethod = new StringBuilder();
			userCodeMethod.append("\t/**\n");
			EDLEdge edlEdge = getParentEdge(usercode);
			userCodeMethod.append("\t * module: ")
					.append(module.get_identifier().get_name())
					.append("<br>\n");
			userCodeMethod.append("\t * line: ").append(edlEdge.get_line())
					.append(" column: ").append(edlEdge.get_column())
					.append(" length: ").append(edlEdge.get_length());
			if (definedRule != null) {
				userCodeMethod.append("<br>\n");
				userCodeMethod.append("\t * rule: ")
						.append(definedRule.toString()).append("\n");
			} else {
				userCodeMethod.append("\n");
			}
			userCodeMethod.append("\t */\n");
			userCodeMethod.append("\tprivate ")
					.append(needsReturn ? "Object" : "void").append(" ")
					.append(methodName)
					.append("(StackElement currentElement) {\n\t\t");
			for (Edge edge : usercode.incidences(EdgeDirection.IN)) {
				Vertex child = edge.getThat();
				if (child.isInstanceOf(JavaCode.VC)) {
					userCodeMethod.append(((JavaCode) child).get_content()
							.replace("\n", "\n\t\t"));
				} else {
					assert child.isInstanceOf(SemanticAction.VC);
					userCodeMethod.append(createSemanticActionMethod(
							(SemanticAction) child, module, definedRule,
							isInDefaultValue));
					userCodeMethod.append("(currentElement)");
					if (child.isInstanceOf(StatementSemanticAction.VC)) {
						userCodeMethod.append(";\n");
					}
				}
			}
			userCodeMethod.append("\n\t}");

			suffixMethods.add(userCodeMethod);
		}

		appendable.append(methodName).append("(currentElement)");
	}
}
