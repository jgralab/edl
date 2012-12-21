package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.edl.parser.symboltable.SymbolTableStack;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.GraphHandler;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher.PatternMatcher;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher.PatternValue;
import de.uni_koblenz.edl.preprocessor.schema.EDLGraph;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLEdge;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLVertex;
import de.uni_koblenz.edl.preprocessor.schema.common.GraphElementClass;
import de.uni_koblenz.edl.preprocessor.schema.common.Identifier;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAnnotatedSymbolTableOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsFollowingSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsHeadTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsStartSymbolOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.GlobalAction;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.IsPatternOf;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.Schema;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTables;
import de.uni_koblenz.edl.preprocessor.schema.section.UserCodeSection;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Assignment;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.BodyVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ConstructorCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.EnumAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Expression;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.HeadVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedBy;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsParameterOfMethod;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsReferencedHead;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsReferencedMaximalTerm;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsStatementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.MethodCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Statement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StatementSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SymbolTableVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.TemporaryVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.UserCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Variable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.VariableAccess;
import de.uni_koblenz.edl.preprocessor.schema.term.Alternative;
import de.uni_koblenz.edl.preprocessor.schema.term.BinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterClass;
import de.uni_koblenz.edl.preprocessor.schema.term.Function;
import de.uni_koblenz.edl.preprocessor.schema.term.IsElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsFunctionNameOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsParameterOfSort;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfSequence;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSyntaxElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.KernelTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.Literal;
import de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction;
import de.uni_koblenz.edl.preprocessor.schema.term.Sequence;
import de.uni_koblenz.edl.preprocessor.schema.term.Sort;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.edl.preprocessor.schema.term.Tuple;
import de.uni_koblenz.edl.preprocessor.schema.term.UnaryTerm;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class Desugarer implements GraphHandler {

	private PatternMatcher patternMatcher;

	private GreqlQuery tempVariablesQuery;

	private final Map<String, VertexClass> simpleName2GraphElementClass;

	private final SymbolTableStack schemaElementsTable;

	private final boolean disableDefaultMapping;

	public Desugarer(de.uni_koblenz.jgralab.schema.Schema targetSchema,
			SymbolTableStack schemaElementsTable, boolean disableDefaultMapping) {
		this.schemaElementsTable = schemaElementsTable;
		simpleName2GraphElementClass = new HashMap<String, VertexClass>();
		this.disableDefaultMapping = disableDefaultMapping;
		if (targetSchema != null) {
			for (VertexClass vc : targetSchema.getGraphClass()
					.getVertexClasses()) {
				if (!vc.isAbstract()) {
					String simpleName = vc.getSimpleName();
					if (simpleName2GraphElementClass.containsKey(simpleName)) {
						simpleName2GraphElementClass.put(simpleName, null);
					} else {
						simpleName2GraphElementClass.put(simpleName, vc);
					}
				}
			}
		}
	}

	@Override
	public void handleSchemaSection(Schema schema) {
	}

	@Override
	public void handleDefaultValuesSection(DefaultValues defaultValues) {
	}

	@Override
	public void handleImportDeclarationsSection(
			ImportDeclarations importDeclarations) {
	}

	@Override
	public void handleSymbolTablesSection(SymbolTables symbolTables) {
	}

	@Override
	public void handleUserCodeSection(UserCodeSection userCodeSection) {
	}

	@Override
	public void handleIslandSection(Island island) {
	}

	@Override
	public void enterModule(Module module) {
		patternMatcher = null;
	}

	@Override
	public void leaveModule() {
	}

	@Override
	public void handleGlobalActionsSection(GlobalAction globalAction) {
		patternMatcher = new PatternMatcher();
		for (IsPatternOf ipo : globalAction
				.getIsPatternOfIncidences(EdgeDirection.IN)) {
			patternMatcher.addPattern(ipo.getAlpha());
		}
	}

	@Override
	public void handleStartSymbol(Term startSymbol, GrammarType grammarType) {
		BodyVariable variable = startSymbol.get_bodyVariable();
		removeOneElementSequences(startSymbol);
		if (variable != null && grammarType == GrammarType.CONTEXT_FREE) {
			variable.set_index(1);
		}
	}

	@Override
	public void handleProduction(Production production, GrammarType grammarType) {
		// find all matching patterns
		List<PatternValue> patternValues = null;
		if (patternMatcher != null) {
			patternValues = patternMatcher.matches(production);
		}

		HeadVariable headVariable = null;
		// remove sequences consisting only of one term
		Term head = production.get_headTerm();
		IsReferencedHead irh = head
				.getFirstIsReferencedHeadIncidence(EdgeDirection.IN);
		if (irh != null) {
			headVariable = irh.getAlpha();
		}
		Sequence body = null;
		for (IsBodyTermOfProduction ibtop : production
				.getIsBodyTermOfProductionIncidences(EdgeDirection.IN)) {
			assert body == null;
			body = (Sequence) ibtop.getThat();
		}
		assert body != null;

		removeOneElementSequences(head);
		IsPartOfSequence ipos = body
				.getFirstIsPartOfSequenceIncidence(EdgeDirection.IN);
		while (ipos != null) {
			IsPartOfSequence nextIpos = ipos
					.getNextIsPartOfSequenceIncidence(EdgeDirection.IN);
			removeOneElementSequences(ipos.getAlpha());
			ipos = nextIpos;
		}

		// apply matched pattern
		BodyVariable[] bodyVariables = new BodyVariable[body.getDegree(
				IsPartOfSequence.EC, EdgeDirection.IN)];

		int index = 0;
		for (IsPartOfSequence ispartos : body
				.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
			Term term = ispartos.getAlpha();
			bodyVariables[index++] = term.get_bodyVariable();
		}
		Map<String, TemporaryVariable> temporaryVariables = getTemporaryVariables(production);

		Set<SymbolTableDefinition> annotatedSymbolTables = new HashSet<SymbolTableDefinition>();
		for (IsAnnotatedSymbolTableOf iasto : production
				.getIsAnnotatedSymbolTableOfIncidences(EdgeDirection.IN)) {
			annotatedSymbolTables.add(iasto.getAlpha());
		}
		boolean hasEmptyBody = bodyVariables.length == 0;

		if (patternValues != null && !patternValues.isEmpty()) {
			insertSemanticActionsOfPattern(production, patternValues,
					headVariable, body, bodyVariables, temporaryVariables,
					annotatedSymbolTables, hasEmptyBody);
		}

		// put following semantic actions at end of body
		putFollowingSemanticActionsAfterBody(production, body);

		// adapt indices of BodyVariables
		adaptIndicesOfBodyVariables(body,
				grammarType == GrammarType.CONTEXT_FREE, false);

		if (!disableDefaultMapping) {
			// apply default mapping
			applyDefaultMapping(body, production.get_headTerm());
		}

		// process annotated symbol tables
		processAnnotatedSymbolTables(production, body);
	}

	private void setPositionValues(EDLEdge position, EDLEdge edge) {
		edge.set_column(position.get_column());
		edge.set_length(position.get_length());
		edge.set_line(position.get_line());
		edge.set_offset(position.get_offset());
	}

	private void appendSemanticActions(SemanticAction prefix,
			SemanticAction suffix) {
		Edge firstEdge = suffix.getFirstIncidence(IsStatementOf.EC,
				EdgeDirection.IN);
		while (firstEdge != null) {
			Edge next = suffix.getFirstIncidence(IsStatementOf.EC,
					EdgeDirection.IN);
			firstEdge.setOmega(prefix);
			firstEdge = next;
		}
	}

	/*
	 * Following methods concern the removing of sequences consisting of only
	 * one element
	 */

	private int uniqueIdOfNewTempVar = 0;

	private void removeOneElementSequences(Term term) {
		Edge edge = term.getFirstIncidence(EdgeDirection.IN);
		while (edge != null) {
			Edge nextEdge = edge.getNextIncidence(EdgeDirection.IN);
			if (edge.getThat().isInstanceOf(Term.VC)
					&& !edge.isInstanceOf(IsParameterOfSort.EC)) {
				removeOneElementSequences((Term) edge.getThat());
			}
			edge = nextEdge;
		}
		if (term.isInstanceOf(Sequence.VC)
				&& term.getDegree(IsPartOfSequence.EC, EdgeDirection.IN) == 1) {
			removeSequence((Sequence) term);
		}
	}

	private void removeSequence(Sequence sequence) {
		assert sequence.getDegree(IsPartOfSequence.EC, EdgeDirection.IN) == 1;
		// sequence has the form ( #S1# T #S2# )
		Edge edgeToParent = sequence.getFirstIncidence(EdgeDirection.OUT);
		boolean isStartSymbol = edgeToParent.isInstanceOf(IsStartSymbolOf.EC);

		// subTerm == T
		Term subTerm = null;
		// prefixAction == S1
		SemanticAction prefixAction = null;
		// suffixAction == S2
		SemanticAction suffixAction = null;
		IsReferencedMaximalTerm referenceOfSequence = null;
		IsReferencedHead isReferencedHead = null;

		for (Edge edge : sequence.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsPartOfSequence.EC)) {
				subTerm = (Term) edge.getThat();
			} else if (edge.isInstanceOf(IsSemanticActionOf.EC)) {
				if (subTerm == null) {
					assert prefixAction == null;
					prefixAction = (SemanticAction) edge.getThat();
				} else {
					assert suffixAction == null;
					suffixAction = (SemanticAction) edge.getThat();
				}
			} else if (edge.isInstanceOf(IsReferencedMaximalTerm.EC)) {
				referenceOfSequence = (IsReferencedMaximalTerm) edge;
			} else if (edge.isInstanceOf(IsReferencedHead.EC)) {
				isReferencedHead = (IsReferencedHead) edge;
			}
		}
		assert subTerm != null;

		// update all variables referencing T
		Variable referenceOfSubTerm = updateVariables(edgeToParent, subTerm,
				referenceOfSequence, isReferencedHead);
		EDLEdge suffixActionPosition = null;
		// replace all lift() operations in S2
		if (suffixAction != null) {
			referenceOfSubTerm = replaceAllLiftInStatementSemanticAction(
					(StatementSemanticAction) suffixAction, referenceOfSubTerm,
					edgeToParent, true, null);
			suffixActionPosition = (EDLEdge) suffixAction
					.getFirstIncidence(EdgeDirection.OUT);
			suffixActionPosition.delete();
		}
		// replace all lift() operations in S1
		EDLEdge prefixActionPosition = null;
		if (prefixAction != null) {
			if (suffixAction == null) {
				suffixAction = ((EDLGraph) prefixAction.getGraph())
						.createStatementSemanticAction();
			}
			referenceOfSubTerm = replaceAllLiftInStatementSemanticAction(
					(StatementSemanticAction) prefixAction, referenceOfSubTerm,
					edgeToParent, false, (StatementSemanticAction) suffixAction);
			prefixActionPosition = (EDLEdge) prefixAction
					.getFirstIncidence(EdgeDirection.OUT);
			prefixActionPosition.delete();
			if (suffixAction.getDegree() == 0) {
				suffixAction.delete();
				suffixAction = null;
			}
		}

		// set T in position of sequence
		subTerm.getFirstIncidence(EdgeDirection.OUT).delete();
		edgeToParent.getNormalEdge().setAlpha(subTerm);
		// delete old sequence
		sequence.delete();
		// append S1 with semantic actions before T
		if (prefixAction != null) {
			Edge prevEdge = edgeToParent.getReversedEdge().getPrevIncidence();
			if (prevEdge != null
					&& prevEdge.isInstanceOf(IsSemanticActionOf.EC)) {
				appendSemanticActions((SemanticAction) prevEdge.getAlpha(),
						prefixAction);
				prefixAction.delete();
			} else {
				Edge edge = ((EDLGraph) edgeToParent.getGraph())
						.createTerm$IsSemanticActionOf(prefixAction,
								(Term) edgeToParent.getOmega());
				setPositionValues(prefixActionPosition, (EDLEdge) edge);
				edge.getReversedEdge().putIncidenceBefore(
						edgeToParent.getReversedEdge());
			}
		}
		// append S2 with semantic actions after T
		if (suffixAction != null) {
			Edge prevEdge = edgeToParent.getReversedEdge().getNextIncidence();
			if (prevEdge != null
					&& prevEdge.isInstanceOf(IsSemanticActionOf.EC)) {
				SemanticAction followingSemanticActions = (SemanticAction) prevEdge
						.getAlpha();
				appendSemanticActions(suffixAction, followingSemanticActions);
				prevEdge.setAlpha(suffixAction);
				followingSemanticActions.delete();

			} else {
				if (isStartSymbol) {
					Edge edge = ((EDLGraph) edgeToParent.getGraph())
							.createTerm$IsSemanticActionOf(suffixAction,
									subTerm);
					setPositionValues(suffixActionPosition, (EDLEdge) edge);
					edge.getReversedEdge().putIncidenceAfter(edgeToParent);
				} else {
					Edge edge = ((EDLGraph) edgeToParent.getGraph())
							.createTerm$IsSemanticActionOf(suffixAction,
									(Term) edgeToParent.getOmega());
					setPositionValues(suffixActionPosition, (EDLEdge) edge);
					edge.getReversedEdge().putIncidenceAfter(
							edgeToParent.getReversedEdge());
				}
			}
		}
	}

	private Variable replaceAllLiftInStatementSemanticAction(
			StatementSemanticAction suffixAction, Variable referenceOfSubTerm,
			Edge edgeToParent, boolean isBehindSubTerm,
			StatementSemanticAction sufixAction) {
		IsStatementOf iso = suffixAction
				.getFirstIsStatementOfIncidence(EdgeDirection.IN);
		while (iso != null) {
			IsStatementOf nextIso = iso
					.getNextIsStatementOfIncidence(EdgeDirection.IN);
			referenceOfSubTerm = replaceAllLiftInStatement(iso.getAlpha(),
					referenceOfSubTerm, edgeToParent, isBehindSubTerm,
					sufixAction);
			iso = nextIso;
		}
		return referenceOfSubTerm;
	}

	private Variable replaceAllLiftInStatement(Statement statement,
			Variable referenceOfSubTerm, Edge edgeToParent,
			boolean isBehindSubTerm, StatementSemanticAction sufixAction) {
		if (statement.isInstanceOf(ExpressionStatement.VC)) {
			referenceOfSubTerm = replaceAllLiftInExpression(
					((ExpressionStatement) statement).get_expression(),
					referenceOfSubTerm, edgeToParent, isBehindSubTerm,
					sufixAction);
		} else if (statement.isInstanceOf(UserCode.VC)) {
			referenceOfSubTerm = replaceAllLiftInUserCode((UserCode) statement,
					referenceOfSubTerm, edgeToParent, isBehindSubTerm,
					sufixAction);
		}
		return referenceOfSubTerm;
	}

	private Variable replaceAllLiftInExpression(Expression expression,
			Variable referenceOfSubTerm, Edge edgeToParent,
			boolean isBehindSubTerm, StatementSemanticAction sufixAction) {
		if (expression.isInstanceOf(MethodCall.VC)) {
			MethodCall methodCall = (MethodCall) expression;
			IsAccessedBy iab = methodCall
					.getFirstIsAccessedByIncidence(EdgeDirection.OUT);
			if (methodCall.get_identifier().get_name().equals("lift")
					&& methodCall.getDegree(IsParameterOfMethod.EC,
							EdgeDirection.IN) == 1 && iab == null) {
				expression = methodCall.getFirstIsParameterOfMethodIncidence(
						EdgeDirection.IN).getAlpha();
				referenceOfSubTerm = replaceLiftCall(methodCall,
						referenceOfSubTerm, edgeToParent, isBehindSubTerm,
						sufixAction);
			}
		}
		if (expression.isInstanceOf(UserCode.VC)) {
			referenceOfSubTerm = replaceAllLiftInUserCode(
					(UserCode) expression, referenceOfSubTerm, edgeToParent,
					isBehindSubTerm, sufixAction);
		} else {
			Edge currentEdge = expression.getFirstIncidence(EdgeDirection.IN);
			while (currentEdge != null) {
				Edge nextEdge = currentEdge.getNextIncidence(EdgeDirection.IN);
				if (currentEdge.getThat().isInstanceOf(Expression.VC)) {
					referenceOfSubTerm = replaceAllLiftInExpression(
							(Expression) currentEdge.getThat(),
							referenceOfSubTerm, edgeToParent, isBehindSubTerm,
							sufixAction);
				}
				currentEdge = nextEdge;
			}
		}
		return referenceOfSubTerm;
	}

	private Variable replaceAllLiftInUserCode(UserCode userCode,
			Variable referenceOfSubTerm, Edge edgeToParent,
			boolean isBehindSubTerm, StatementSemanticAction sufixAction) {
		for (de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsSemanticActionOf isao : userCode
				.getsemantic_actions$IsSemanticActionOfIncidences(EdgeDirection.IN)) {
			if (isao.getAlpha().isInstanceOf(StatementSemanticAction.VC)) {
				referenceOfSubTerm = replaceAllLiftInStatementSemanticAction(
						(StatementSemanticAction) isao.getAlpha(),
						referenceOfSubTerm, edgeToParent, isBehindSubTerm,
						sufixAction);
			} else {
				assert isao.getAlpha()
						.isInstanceOf(ExpressionSemanticAction.VC);
				referenceOfSubTerm = replaceAllLiftInExpression(
						((ExpressionSemanticAction) isao.getAlpha())
								.get_expression(),
						referenceOfSubTerm, edgeToParent, isBehindSubTerm,
						sufixAction);
			}
		}
		return referenceOfSubTerm;
	}

	private Variable replaceLiftCall(MethodCall methodCall,
			Variable referenceOfSubTerm, Edge edgeToParent,
			boolean isBehindSubTerm, StatementSemanticAction sufixAction) {
		EDLEdge positionOfMethodCall = methodCall
				.getFirstEDLEdgeIncidence(EdgeDirection.OUT);
		EDLGraph graph = (EDLGraph) methodCall.getGraph();
		if (referenceOfSubTerm == null) {
			BodyVariable bodyVariable = graph.createVertex(BodyVariable.VC);
			int newIndex = determineNewIndex(edgeToParent);
			assert newIndex >= 0;
			bodyVariable.set_index(newIndex);
			referenceOfSubTerm = bodyVariable;
		}
		IsParameterOfMethod positionOfParameter = methodCall
				.getFirstIsParameterOfMethodIncidence(EdgeDirection.IN);
		Expression parameterOfLift = positionOfParameter.getAlpha();
		if (isBehindSubTerm) {
			// replace lift(expr1)
			// by $i = expr1
			VariableAccess variableAccess = graph.createVariableAccess();
			EDLEdge edge = graph.createIsAccessedVariableOf(referenceOfSubTerm,
					variableAccess);
			setPositionValues(positionOfMethodCall, edge);
			Assignment assignment = graph.createAssignment();
			edge = graph.createIsAssignedElementOf(variableAccess, assignment);
			setPositionValues(positionOfMethodCall, edge);
			edge = graph.createIsAssignedValueOf(parameterOfLift, assignment);
			setPositionValues(positionOfParameter, edge);
			methodCall.getFirstIncidence(EdgeDirection.OUT)
					.setAlpha(assignment);
		} else {
			// replace lift(expr1)
			// by $new = expr1
			// and insert as first Statement in S2 behind T
			// $i = $new
			// create new temporary variable
			Identifier nameOfNewTempVar = graph.createIdentifier();
			nameOfNewTempVar.set_name("new__" + uniqueIdOfNewTempVar++);
			TemporaryVariable newTempVar = graph.createTemporaryVariable();
			EDLEdge edge = graph.createIsNameOfTemporaryVariable(
					nameOfNewTempVar, newTempVar);
			setPositionValues(positionOfMethodCall, edge);
			// create assignment $new = expr1
			VariableAccess oldLhs = graph.createVariableAccess();
			edge = graph.createIsAccessedVariableOf(newTempVar, oldLhs);
			setPositionValues(positionOfMethodCall, edge);
			Assignment assignmentOld = graph.createAssignment();
			edge = graph.createIsAssignedElementOf(oldLhs, assignmentOld);
			setPositionValues(positionOfMethodCall, edge);
			edge = graph
					.createIsAssignedValueOf(parameterOfLift, assignmentOld);
			setPositionValues(positionOfParameter, edge);
			methodCall.getFirstIncidence(EdgeDirection.OUT).setAlpha(
					assignmentOld);
			// $i = $new
			VariableAccess newLhs = graph.createVariableAccess();
			edge = graph.createIsAccessedVariableOf(referenceOfSubTerm, newLhs);
			setPositionValues(positionOfMethodCall, edge);
			VariableAccess newRhs = graph.createVariableAccess();
			edge = graph.createIsAccessedVariableOf(newTempVar, newRhs);
			setPositionValues(positionOfMethodCall, edge);
			Assignment assignment = graph.createAssignment();
			edge = graph.createIsAssignedElementOf(newLhs, assignment);
			setPositionValues(positionOfMethodCall, edge);
			edge = graph.createIsAssignedValueOf(newRhs, assignment);
			setPositionValues(positionOfMethodCall, edge);

			ExpressionStatement expressionStatement = graph
					.createExpressionStatement();
			edge = graph.createIsExpressionOfStatement(assignment,
					expressionStatement);
			setPositionValues(positionOfMethodCall, edge);
			IsStatementOf newFirstEdge = graph.createIsStatementOf(
					expressionStatement, sufixAction);
			setPositionValues(positionOfMethodCall, newFirstEdge);
			IsStatementOf oldFirstEdge = sufixAction
					.getFirstIsStatementOfIncidence(EdgeDirection.IN);
			newFirstEdge.getReversedEdge().putIncidenceBefore(oldFirstEdge);
		}
		// delete lift
		Identifier identifierOfLift = methodCall.get_identifier();
		if (identifierOfLift.getDegree() == 1) {
			identifierOfLift.delete();
		}
		Edge currentEdge = methodCall.getFirstIncidence();
		while (currentEdge != null) {
			currentEdge.delete();
			currentEdge = methodCall.getFirstIncidence();
		}
		methodCall.delete();
		return referenceOfSubTerm;
	}

	private Variable updateVariables(Edge edgeToParent, Term subTerm,
			IsReferencedMaximalTerm referenceOfSequence,
			IsReferencedHead isReferencedHead) {
		Variable result = null;
		if (isReferencedHead != null) {
			// the current sequence is the head
			// set reference of headVariables to T
			isReferencedHead.setOmega(subTerm);
			result = isReferencedHead.getAlpha();
		}
		IsReferencedMaximalTerm referenceOfSubTerm = subTerm
				.getFirstIsReferencedMaximalTermIncidence(EdgeDirection.IN);
		if (referenceOfSequence != null) {
			// set reference of sequence to T
			referenceOfSequence.setOmega(subTerm);
			result = referenceOfSequence.getAlpha();
		}
		if (referenceOfSubTerm != null) {
			// T was referenced by a $0 in S1 or S2
			BodyVariable oldBodyVariable = referenceOfSubTerm.getAlpha();
			if (referenceOfSequence != null) {
				// there exists a reference to sequence
				// now it references T
				// use merge it with the $0 in S1 or S2
				BodyVariable newBodyVariable = referenceOfSequence.getAlpha();
				Edge edge = oldBodyVariable.getFirstIncidence();
				while (edge != null) {
					Edge next = edge.getNextIncidence();
					if (!edge.isInstanceOf(IsReferencedMaximalTerm.EC)) {
						if (edge.getAlpha() == oldBodyVariable) {
							edge.setAlpha(newBodyVariable);
						} else {
							edge.setOmega(newBodyVariable);
						}
					}
					edge = next;
				}
				oldBodyVariable.delete();
			} else {
				// update index of body variable
				int newIndex = determineNewIndex(edgeToParent);
				assert newIndex >= 0;
				oldBodyVariable.set_index(newIndex);
				result = oldBodyVariable;
			}
		}
		return result;
	}

	private int determineNewIndex(Edge edgeToParent) {
		Term parentTerm = (Term) edgeToParent.getOmega();
		if (parentTerm.isInstanceOf(KernelTerm.VC)
				|| parentTerm.isInstanceOf(Sort.VC)
				|| parentTerm.isInstanceOf(CharacterClass.VC)) {
			return determineNewIndex(parentTerm
					.getFirstIncidence(EdgeDirection.OUT));
		} else if (parentTerm.isInstanceOf(Sequence.VC)) {
			int numberOfEdges = 0;
			for (Edge edge : parentTerm
					.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
				if (edge.getNormalEdge() == edgeToParent.getNormalEdge()) {
					return numberOfEdges;
				}
				numberOfEdges++;
			}
		} else if (parentTerm.isInstanceOf(UnaryTerm.VC)) {
			return 0;
		} else if (parentTerm.isInstanceOf(Alternative.VC)) {
			return 0;
		} else if (parentTerm.isInstanceOf(BinaryTerm.VC)) {
			int numberOfEdges = 0;
			for (Edge edge : parentTerm
					.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
				if (edge.getNormalEdge() == edgeToParent.getNormalEdge()) {
					return numberOfEdges;
				}
				numberOfEdges++;
			}
		} else if (parentTerm.isInstanceOf(PrefixFunction.VC)
				|| parentTerm.isInstanceOf(Tuple.VC)) {
			int numberOfEdges = 0;
			for (Edge edge : parentTerm.incidences(EdgeDirection.IN)) {
				if (edge.isInstanceOf(IsFunctionNameOf.EC)
						|| edge.isInstanceOf(IsSyntaxElementOf.EC)
						|| edge.isInstanceOf(IsElementOf.EC)) {
					if (edge.getNormalEdge() == edgeToParent.getNormalEdge()) {
						return numberOfEdges;
					}
					numberOfEdges++;
				}
			}
		} else if (parentTerm.isInstanceOf(Function.VC)) {
			int numberOfEdges = 0;
			for (Edge edge : parentTerm.incidences(EdgeDirection.IN)) {
				if (edge.isInstanceOf(IsElementOf.EC)) {
					if (edge.getNormalEdge() == edgeToParent.getNormalEdge()) {
						return numberOfEdges;
					}
					numberOfEdges++;
				} else if (edge.isInstanceOf(IsSyntaxElementOf.EC)) {
					Literal literal = (Literal) edge.getThat();
					if (literal.get_value().equals("=>")) {
						break;
					}
				}
			}
		}
		return -1;
	}

	/*
	 * Following methods concern the applying of patterns.
	 */

	public Map<String, TemporaryVariable> getTemporaryVariables(Production rule) {
		if (tempVariablesQuery == null) {
			tempVariablesQuery = GreqlQuery
					.createQuery("import semantic_actions.VariableAccess;"
							+ " using rule:rule<--+&{VariableAccess}");
		}
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();
		environment.setVariable("rule", rule);
		@SuppressWarnings("unchecked")
		Set<VariableAccess> variableAccess = (Set<VariableAccess>) tempVariablesQuery
				.evaluate(rule.getGraph(), environment);
		Map<String, TemporaryVariable> result = new HashMap<String, TemporaryVariable>();
		for (VariableAccess va : variableAccess) {
			Vertex variable = va.get_variable();
			if (variable.isInstanceOf(TemporaryVariable.VC)) {
				result.put(((TemporaryVariable) variable).get_identifier()
						.get_name(), (TemporaryVariable) variable);
			}
		}
		return result;
	}

	private void insertSemanticActionsOfPattern(Production production,
			List<PatternValue> patternValues, HeadVariable headVariable,
			Sequence body, BodyVariable[] bodyVariables,
			Map<String, TemporaryVariable> temporaryVariables,
			Set<SymbolTableDefinition> annotatedSymbolTables,
			boolean hasEmptyBody) {
		EDLGraph graph = (EDLGraph) production.getGraph();

		// determine prefix and suffix semantic actions
		StatementSemanticAction prefixSemanticAction = graph
				.createStatementSemanticAction();
		StatementSemanticAction suffixSemanticAction = graph
				.createStatementSemanticAction();

		Term[] bodyTerms = new Term[bodyVariables.length];
		int index = 0;
		for (IsPartOfSequence ipos : body
				.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
			bodyTerms[index++] = ipos.getAlpha();
		}

		// apply semantic actions of pattern
		applySemanticActionsOfPattern(production, bodyTerms,
				production.get_headTerm(), patternValues,
				new HeadVariable[] { headVariable }, bodyVariables,
				temporaryVariables, annotatedSymbolTables, graph,
				prefixSemanticAction, suffixSemanticAction);

		// put prefix semantic actions in front of body
		StatementSemanticAction oldPrefix = null;
		Edge firstIncidence = body.getFirstIncidence(EdgeDirection.IN);
		if (firstIncidence != null
				&& firstIncidence.isInstanceOf(IsSemanticActionOf.EC)) {
			oldPrefix = (StatementSemanticAction) firstIncidence.getThat();
		}

		if (oldPrefix != null) {
			appendSemanticActions(prefixSemanticAction, oldPrefix);
			firstIncidence.setAlpha(prefixSemanticAction);
			oldPrefix.delete();
		} else if (prefixSemanticAction.getDegree(EdgeDirection.IN) > 0) {
			Edge edge = graph.createTerm$IsSemanticActionOf(
					prefixSemanticAction, body);
			if (firstIncidence != null) {
				edge.getReversedEdge().putIncidenceBefore(firstIncidence);
			}
		} else {
			prefixSemanticAction.delete();
		}

		// put suffix semantic actions behind body
		StatementSemanticAction oldSuffix = null;
		Edge lastIncidence = body.getLastIncidence();
		while (lastIncidence != null && lastIncidence.getAlpha() == body) {
			lastIncidence = lastIncidence.getPrevIncidence();
		}
		if (lastIncidence != null
				&& lastIncidence.isInstanceOf(IsSemanticActionOf.EC)) {
			oldSuffix = (StatementSemanticAction) lastIncidence.getThat();
		}

		if (oldSuffix != null) {
			appendSemanticActions(oldSuffix, suffixSemanticAction);
			suffixSemanticAction.delete();
		} else if (suffixSemanticAction.getDegree(EdgeDirection.IN) > 0) {
			Edge edge = graph.createTerm$IsSemanticActionOf(
					suffixSemanticAction, body);
			if (lastIncidence != null) {
				edge.getReversedEdge().putIncidenceAfter(lastIncidence);
			}
		} else {
			suffixSemanticAction.delete();
		}
	}

	private void applySemanticActionsOfPattern(Production production,
			Term[] body, Term head, List<PatternValue> patternValues,
			HeadVariable[] headVariables, BodyVariable[] bodyVariables,
			Map<String, TemporaryVariable> temporaryVariables,
			Set<SymbolTableDefinition> annotatedSymbolTables, EDLGraph graph,
			StatementSemanticAction prefixSemanticAction,
			StatementSemanticAction suffixSemanticAction) {
		for (PatternValue patternValue : patternValues) {
			// handle symbol tables
			if (patternValue.getAnnotatedSymbolTables() != null) {
				for (SymbolTableDefinition symbolTable : patternValue
						.getAnnotatedSymbolTables()) {
					if (!annotatedSymbolTables.contains(symbolTable)) {
						graph.createIsAnnotatedSymbolTableOf(symbolTable,
								production);
						annotatedSymbolTables.add(symbolTable);
					}
				}
			}
			// copy semantic actions
			StatementSemanticAction statementsToCopy = (StatementSemanticAction) patternValue
					.getSemanticAction();
			if (statementsToCopy != null) {
				if (patternValue.executeBefore()) {
					// semantic action must be copied before prefix semantic
					// actions
					insertCopiedSemanticActions(statementsToCopy,
							prefixSemanticAction, body, head,
							temporaryVariables, bodyVariables, headVariables);
				} else {
					// semantic action must be copied after prefix semantic
					// actions
					insertCopiedSemanticActions(statementsToCopy,
							suffixSemanticAction, body, head,
							temporaryVariables, bodyVariables, headVariables);
				}
			}
		}
	}

	private void insertCopiedSemanticActions(
			StatementSemanticAction statementsToCopy,
			StatementSemanticAction semanticAction, Term[] body, Term head,
			Map<String, TemporaryVariable> temporaryVariables,
			BodyVariable[] bodyVariables, HeadVariable[] headVariables) {
		EDLGraph graph = (EDLGraph) statementsToCopy.getGraph();
		for (IsStatementOf iso : statementsToCopy
				.getIsStatementOfIncidences(EdgeDirection.IN)) {
			Statement statement = (Statement) copyStatement(iso.getAlpha(),
					body, head, temporaryVariables, bodyVariables,
					headVariables);
			EDLEdge edge = graph.createIsStatementOf(statement, semanticAction);
			setPositionValues(iso, edge);
		}
	}

	private EDLVertex copyStatement(EDLVertex vertexToCopy, Term[] body,
			Term head, Map<String, TemporaryVariable> temporaryVariables,
			BodyVariable[] bodyVariables, HeadVariable[] headVariables) {
		EDLGraph graph = (EDLGraph) vertexToCopy.getGraph();
		if (vertexToCopy.isInstanceOf(HeadVariable.VC)) {
			return copyHeadVariable(head, headVariables, graph);
		} else if (vertexToCopy.isInstanceOf(BodyVariable.VC)) {
			return copyBodyVariable(vertexToCopy, body, bodyVariables, graph);
		} else if (vertexToCopy.isInstanceOf(TemporaryVariable.VC)) {
			return copyTemporaryVariable(vertexToCopy, temporaryVariables,
					graph);
		} else if (vertexToCopy.isInstanceOf(SymbolTableVariable.VC)) {
			return copySymbolTableVariable(vertexToCopy, graph);
		} else if (vertexToCopy.isInstanceOf(EnumAccess.VC)) {
			return copyEnumAccess(vertexToCopy, graph);
		} else {
			EDLVertex vertex = graph.createVertex(vertexToCopy
					.getAttributedElementClass());
			copyAttributes(vertexToCopy, vertex);
			for (Edge edge : vertexToCopy.incidences(EdgeDirection.IN)) {
				Vertex child = edge.getThat();
				if (child.getAttributedElementClass().getPackageName()
						.equals("semantic_actions")) {
					child = copyStatement((EDLVertex) child, body, head,
							temporaryVariables, bodyVariables, headVariables);
				}
				Edge newEdge = graph.createEdge(
						edge.getAttributedElementClass(), child, vertex);
				copyAttributes(edge, newEdge);
			}
			return vertex;
		}
	}

	private void copyAttributes(AttributedElement<?, ?> elementToCopy,
			AttributedElement<?, ?> element) {
		assert elementToCopy.getAttributedElementClass() == element
				.getAttributedElementClass();
		for (Attribute attribute : elementToCopy.getAttributedElementClass()
				.getAttributeList()) {
			Object value = copyAttributeValue(elementToCopy
					.getAttribute(attribute.getName()));
			element.setAttribute(attribute.getName(), value);
		}
	}

	@SuppressWarnings("unchecked")
	private Object copyAttributeValue(Object value) {
		if (value instanceof Map) {
			return JGraLab.map().plusAll(
					(Map<? extends Object, ? extends Object>) value);
		} else if (value instanceof Set) {
			return JGraLab.set().plusAll((Collection<? extends Object>) value);
		} else if (value instanceof List) {
			return JGraLab.vector().plusAll(
					(Collection<? extends Object>) value);
		} else {
			// records do not exist in an EDLGraph
			return value;
		}
	}

	private EDLVertex copyEnumAccess(EDLVertex vertexToCopy, EDLGraph graph) {
		EnumAccess enumAccess = graph.createEnumAccess();
		graph.createIsAccessedEnumConstant(enumAccess,
				((EnumAccess) vertexToCopy).get_constant());
		return enumAccess;
	}

	private EDLVertex copySymbolTableVariable(EDLVertex vertexToCopy,
			EDLGraph graph) {
		SymbolTableVariable symbolTableVar = graph.createSymbolTableVariable();
		graph.createReferencesSymbolTable(symbolTableVar,
				((SymbolTableVariable) vertexToCopy)
						.get_symbolTableDefinition());
		return symbolTableVar;
	}

	private EDLVertex copyTemporaryVariable(EDLVertex vertexToCopy,
			Map<String, TemporaryVariable> temporaryVariables, EDLGraph graph) {
		String nameOfTempVar = ((TemporaryVariable) vertexToCopy)
				.get_identifier().get_name();
		TemporaryVariable tempVar = temporaryVariables.get(nameOfTempVar);
		if (tempVar == null) {
			tempVar = graph.createTemporaryVariable();
			Identifier identifier = graph.createIdentifier();
			identifier.set_name(nameOfTempVar);
			graph.createIsNameOfTemporaryVariable(identifier, tempVar);
			temporaryVariables.put(nameOfTempVar, tempVar);
		}
		return tempVar;
	}

	private EDLVertex copyBodyVariable(EDLVertex vertexToCopy, Term[] body,
			BodyVariable[] bodyVariables, EDLGraph graph) {
		int index = ((BodyVariable) vertexToCopy).get_index();
		assert index < body.length;
		if (bodyVariables[index] == null) {
			bodyVariables[index] = graph.createBodyVariable();
			bodyVariables[index].set_index(index);
			graph.createIsReferencedMaximalTerm(bodyVariables[index],
					body[index]);
		}
		return bodyVariables[index];
	}

	private EDLVertex copyHeadVariable(Term head, HeadVariable[] headVariables,
			EDLGraph graph) {
		if (headVariables[0] == null) {
			headVariables[0] = graph.createHeadVariable();
			graph.createIsReferencedHead(headVariables[0], head);
		}
		return headVariables[0];
	}

	/*
	 * The following methods are used to put the following semantic actions at
	 * the end of the body
	 */

	private void putFollowingSemanticActionsAfterBody(Production production,
			Sequence body) {
		IsFollowingSemanticActionOf ifsao = production
				.getFirstIsFollowingSemanticActionOfIncidence(EdgeDirection.IN);
		if (ifsao != null) {
			StatementSemanticAction followingActions = (StatementSemanticAction) ifsao
					.getThat();
			StatementSemanticAction lastActionsInBody = null;
			Edge lastIncidence = body.getLastIncidence();
			while (lastIncidence != null && lastIncidence.getAlpha() == body) {
				lastIncidence = lastIncidence.getPrevIncidence();
			}
			if (lastIncidence != null
					&& lastIncidence.isInstanceOf(IsSemanticActionOf.EC)) {
				lastActionsInBody = (StatementSemanticAction) lastIncidence
						.getThat();
			}

			if (lastActionsInBody != null) {
				appendSemanticActions(lastActionsInBody, followingActions);
			} else {
				((EDLGraph) production.getGraph())
						.createTerm$IsSemanticActionOf(followingActions, body);
			}
			ifsao.delete();
		}
	}

	/*
	 * The following method adapt the indices of body variables
	 */

	private void adaptIndicesOfBodyVariables(Term term, boolean isContextfree,
			boolean isInFunctionTerm) {
		for (Edge edge : term.incidences(EdgeDirection.IN)) {
			Vertex that = edge.getThat();
			if (that.isInstanceOf(Term.VC)) {
				Term child = (Term) that;
				BodyVariable variable = child.get_bodyVariable();
				if (variable != null) {
					if (isInFunctionTerm) {
						variable.set_index(variable.get_index() + 1);
					}
					if (isContextfree) {
						variable.set_index(variable.get_index() * 2);
					}
				}
				adaptIndicesOfBodyVariables(child, isContextfree,
						child.isInstanceOf(Function.VC));
			}
		}
	}

	/*
	 * The following methods apply the default mapping
	 */

	private void applyDefaultMapping(Sequence body, Term head) {
		EDLGraph graph = (EDLGraph) body.getGraph();
		if (head.isInstanceOf(Sort.VC)) {
			String nameOfHead = ((Sort) head).get_identifier().get_name();
			VertexClass vertexClass = simpleName2GraphElementClass
					.get(nameOfHead);
			if (vertexClass != null && !vertexClass.isAbstract()) {
				// apply default mapping
				GraphElementClass gec = (GraphElementClass) schemaElementsTable
						.use(vertexClass.getQualifiedName());
				if (gec == null) {
					gec = (GraphElementClass) schemaElementsTable.declare(
							vertexClass.getQualifiedName(),
							graph.createGraphElementClass());
				}
				if (gec.get_identifier() == null) {
					Identifier identifier = graph.createIdentifier();
					identifier.set_name(vertexClass.getQualifiedName());
					graph.createIsNameOfGraphElement(identifier, gec);
				}
				IsHeadTermOfProduction positionOfHead = head
						.getFirstIsHeadTermOfProductionIncidence(EdgeDirection.OUT);

				// create $ = VertexClass();
				HeadVariable headVariable = head.get_headVariable();
				if (headVariable == null) {
					headVariable = graph.createHeadVariable();
					graph.createIsReferencedHead(headVariable, head);
				}
				VariableAccess variableAccess = graph.createVariableAccess();
				graph.createIsAccessedVariableOf(headVariable, variableAccess);

				ConstructorCall constructorCall = graph.createConstructorCall();
				graph.createIsTypeOfCreatedGraphElement(gec, constructorCall);

				Assignment assignment = graph.createAssignment();
				graph.createIsAssignedElementOf(variableAccess, assignment);
				graph.createIsAssignedValueOf(constructorCall, assignment);

				ExpressionStatement expressionStatement = graph
						.createExpressionStatement();
				EDLEdge edge = graph.createIsExpressionOfStatement(assignment,
						expressionStatement);
				setPositionValues(positionOfHead, edge);

				// set default mapping as first statement in
				// body
				setStatementAsInitialStatement(graph, body,
						expressionStatement, positionOfHead);
			}
		}
	}

	private void setStatementAsInitialStatement(EDLGraph graph, Sequence body,
			Statement statement, EDLEdge positionOfStatement) {
		Edge firstIncidence = body.getFirstIncidence(EdgeDirection.IN);
		StatementSemanticAction prefixAction = null;
		if (firstIncidence != null
				&& firstIncidence.isInstanceOf(IsSemanticActionOf.EC)) {
			prefixAction = (StatementSemanticAction) firstIncidence.getThat();
		}

		if (prefixAction != null) {
			EDLEdge iso = graph.createIsStatementOf(statement, prefixAction);
			iso.getReversedEdge().putIncidenceBefore(
					prefixAction.getFirstIncidence(EdgeDirection.IN));
			setPositionValues(positionOfStatement, iso);
		} else {
			prefixAction = graph.createStatementSemanticAction();
			EDLEdge iso = graph.createIsStatementOf(statement, prefixAction);
			setPositionValues(positionOfStatement, iso);
			EDLEdge isao = graph.createTerm$IsSemanticActionOf(prefixAction,
					body);
			setPositionValues(positionOfStatement, isao);
			if (firstIncidence != null) {
				isao.getReversedEdge().putIncidenceBefore(firstIncidence);
			}
		}
	}

	private void setStatementAsFinalStatement(EDLGraph graph, Sequence body,
			Statement statement, EDLEdge positionOfStatement) {
		StatementSemanticAction lastActionsInBody = null;
		Edge lastIncidence = body.getLastIncidence();
		while (lastIncidence != null && lastIncidence.getAlpha() == body) {
			lastIncidence = lastIncidence.getPrevIncidence();
		}
		if (lastIncidence != null
				&& lastIncidence.isInstanceOf(IsSemanticActionOf.EC)) {
			lastActionsInBody = (StatementSemanticAction) lastIncidence
					.getThat();
		}

		if (lastActionsInBody != null) {
			EDLEdge iso = graph.createIsStatementOf(statement,
					lastActionsInBody);
			setPositionValues(positionOfStatement, iso);
		} else {
			lastActionsInBody = graph.createStatementSemanticAction();
			EDLEdge iso = graph.createIsStatementOf(statement,
					lastActionsInBody);
			setPositionValues(positionOfStatement, iso);
			EDLEdge isao = graph.createTerm$IsSemanticActionOf(
					lastActionsInBody, body);
			setPositionValues(positionOfStatement, isao);
			if (lastIncidence != null) {
				isao.getReversedEdge().putIncidenceAfter(lastIncidence);
			}
		}
	}

	/*
	 * Following methods are used to process annotated symbmol tables
	 */

	private void processAnnotatedSymbolTables(Production production,
			Sequence body) {
		IsAnnotatedSymbolTableOf iasto = production
				.getFirstIsAnnotatedSymbolTableOfIncidence(EdgeDirection.IN);
		while (iasto != null) {
			Statement push = createSymbolTableAction(iasto.getAlpha(), "push");
			setStatementAsInitialStatement((EDLGraph) body.getGraph(), body,
					push, iasto);
			Statement pop = createSymbolTableAction(iasto.getAlpha(), "pop");
			setStatementAsFinalStatement((EDLGraph) body.getGraph(), body, pop,
					iasto);
			iasto.delete();
			iasto = production
					.getFirstIsAnnotatedSymbolTableOfIncidence(EdgeDirection.IN);
		}
	}

	private Statement createSymbolTableAction(
			SymbolTableDefinition symbolTable, String methodName) {
		EDLGraph graph = (EDLGraph) symbolTable.getGraph();

		SymbolTableVariable symbolTableVariable = graph
				.createSymbolTableVariable();
		graph.createReferencesSymbolTable(symbolTableVariable, symbolTable);

		VariableAccess variableAccess = graph.createVariableAccess();
		graph.createIsAccessedVariableOf(symbolTableVariable, variableAccess);

		Identifier identifier = graph.createIdentifier();
		identifier.set_name(methodName);

		MethodCall methodCall = graph.createMethodCall();
		graph.createIsNameOfMethod(identifier, methodCall);

		DotAccess dotAccess = graph.createDotAccess();
		graph.createIsAccessedElementOf(variableAccess, dotAccess);
		graph.createIsAccessedBy(methodCall, dotAccess);

		ExpressionStatement statement = graph.createExpressionStatement();
		graph.createIsExpressionOfStatement(dotAccess, statement);

		return statement;
	}
}
