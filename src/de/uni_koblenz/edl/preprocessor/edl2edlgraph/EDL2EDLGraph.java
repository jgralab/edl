package de.uni_koblenz.edl.preprocessor.edl2edlgraph;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.pcollections.PVector;

import de.uni_koblenz.edl.SemanticActionException;
import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.stack.elements.ApplicationOfListRule;
import de.uni_koblenz.edl.parser.stack.elements.StackElement;
import de.uni_koblenz.edl.parser.symboltable.SymbolTableException;
import de.uni_koblenz.edl.parser.symboltable.SymbolTableStack;
import de.uni_koblenz.edl.preprocessor.EDLPreprocessor;
import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.schema.EDLGraph;
import de.uni_koblenz.edl.preprocessor.schema.EDLSchema;
import de.uni_koblenz.edl.preprocessor.schema.common.Definition;
import de.uni_koblenz.edl.preprocessor.schema.common.EnumConstant;
import de.uni_koblenz.edl.preprocessor.schema.common.Enumeration;
import de.uni_koblenz.edl.preprocessor.schema.common.GraphElementClass;
import de.uni_koblenz.edl.preprocessor.schema.common.Identifier;
import de.uni_koblenz.edl.preprocessor.schema.common.IsConstantOf;
import de.uni_koblenz.edl.preprocessor.schema.common.IsModuleOf;
import de.uni_koblenz.edl.preprocessor.schema.common.IsNameOf;
import de.uni_koblenz.edl.preprocessor.schema.common.IsNameOfEnumeration;
import de.uni_koblenz.edl.preprocessor.schema.common.IsNameOfGraphElement;
import de.uni_koblenz.edl.preprocessor.schema.common.IsNameOfModule;
import de.uni_koblenz.edl.preprocessor.schema.common.IsNameOfRecord;
import de.uni_koblenz.edl.preprocessor.schema.common.IsParameterOfModule;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.common.Record;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Alias;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Aliases;
import de.uni_koblenz.edl.preprocessor.schema.grammar.AssocPriority;
import de.uni_koblenz.edl.preprocessor.schema.grammar.ChainPriority;
import de.uni_koblenz.edl.preprocessor.schema.grammar.EmptyGrammar;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Group;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAliasOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAnnotatedSymbolTableOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAttributeOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfPattern;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsFollowingSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsHeadTermOfPattern;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsHeadTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsLeftAlternativeOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsListPartOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsLookaheadOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsNewNameOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsNewProductionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsNewTermOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsOriginalNameOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsOriginalProductionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsOriginalTermOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPreviousLookahead;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPriorityGroupOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPriorityOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsProductionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsProductionOfGroup;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRenamingOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRestrictedTermOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRestrictionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRightAlternativeOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsSortOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsStartSymbolOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsVariableDefinitionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Lookahead;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Pattern;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Priorities;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Renaming;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Restriction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Restrictions;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Sorts;
import de.uni_koblenz.edl.preprocessor.schema.grammar.StartSymbols;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Syntax;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Variables;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.ATerm;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Associativity;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.AssociativityValue;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Attribute;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Bracket;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Id;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.IsModuleOfId;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Preference;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.PreferenceValue;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.Element;
import de.uni_koblenz.edl.preprocessor.schema.section.Exports;
import de.uni_koblenz.edl.preprocessor.schema.section.GlobalAction;
import de.uni_koblenz.edl.preprocessor.schema.section.Hiddens;
import de.uni_koblenz.edl.preprocessor.schema.section.Import;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.Imports;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportsModule;
import de.uni_koblenz.edl.preprocessor.schema.section.InitialSection;
import de.uni_koblenz.edl.preprocessor.schema.section.IsContainedInExports;
import de.uni_koblenz.edl.preprocessor.schema.section.IsContainedInHiddens;
import de.uni_koblenz.edl.preprocessor.schema.section.IsDefaultStatementOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsDefinedIn;
import de.uni_koblenz.edl.preprocessor.schema.section.IsElementIn;
import de.uni_koblenz.edl.preprocessor.schema.section.IsImportOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsJavaImportOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsNameOfSymbolTable;
import de.uni_koblenz.edl.preprocessor.schema.section.IsPatternOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsPersistentEdgeClassOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsPersistentVertexClassOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsSectionOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsUserCodeOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsVertexClassOf;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.IslandEnd;
import de.uni_koblenz.edl.preprocessor.schema.section.IslandStart;
import de.uni_koblenz.edl.preprocessor.schema.section.JavaImport;
import de.uni_koblenz.edl.preprocessor.schema.section.Schema;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTables;
import de.uni_koblenz.edl.preprocessor.schema.section.UserCodeSection;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.AlphaConstant;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Assignment;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.BodyVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.BooleanLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ConstructorCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DoubleLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.EnumAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Expression;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Field;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.HeadVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IntegerLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedBy;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedElementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedEnumConstant;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedGraphElementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedListOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAccessedVariableOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAssignedElementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsAssignedValueOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsContentOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsExpressionOfSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsExpressionOfStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsIndexOfList;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsJavaCodeOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsNameOfField;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsNameOfMethod;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsNameOfTemporaryVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsParameterOfConstructor;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsParameterOfMethod;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsReferencedHead;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsReferencedMaximalTerm;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsStatementOf;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsTypeOfCreatedGraphElement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsTypeOfCreatedRecord;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.JavaCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ListAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.LongLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.MethodCall;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.NullLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.OmegaConstant;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ReferencesSymbolTable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Statement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StatementSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StringLiteral;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SymbolTableVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.TemporaryVariable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.UserCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Variable;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.VariableAccess;
import de.uni_koblenz.edl.preprocessor.schema.term.Alternative;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterRange;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterRanges;
import de.uni_koblenz.edl.preprocessor.schema.term.Complement;
import de.uni_koblenz.edl.preprocessor.schema.term.Difference;
import de.uni_koblenz.edl.preprocessor.schema.term.FileStart;
import de.uni_koblenz.edl.preprocessor.schema.term.Function;
import de.uni_koblenz.edl.preprocessor.schema.term.Intersection;
import de.uni_koblenz.edl.preprocessor.schema.term.IsAttachedBy;
import de.uni_koblenz.edl.preprocessor.schema.term.IsCharacterClassOfComplement;
import de.uni_koblenz.edl.preprocessor.schema.term.IsCharacterRangeOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsFunctionNameOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsMultiplicityOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsNameOfLabel;
import de.uni_koblenz.edl.preprocessor.schema.term.IsNameOfSort;
import de.uni_koblenz.edl.preprocessor.schema.term.IsParameterOfSort;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryCharacterClassOperation;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfSequence;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfUnaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSyntaxElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsTermOfKernelTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.KernelTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.KernelTermType;
import de.uni_koblenz.edl.preprocessor.schema.term.KleeneOperator;
import de.uni_koblenz.edl.preprocessor.schema.term.LAYOUT;
import de.uni_koblenz.edl.preprocessor.schema.term.Label;
import de.uni_koblenz.edl.preprocessor.schema.term.Literal;
import de.uni_koblenz.edl.preprocessor.schema.term.Multiplicity;
import de.uni_koblenz.edl.preprocessor.schema.term.Option;
import de.uni_koblenz.edl.preprocessor.schema.term.PatternTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.Repetition;
import de.uni_koblenz.edl.preprocessor.schema.term.Sequence;
import de.uni_koblenz.edl.preprocessor.schema.term.Sort;
import de.uni_koblenz.edl.preprocessor.schema.term.Start;
import de.uni_koblenz.edl.preprocessor.schema.term.Strategy;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.edl.preprocessor.schema.term.Tuple;
import de.uni_koblenz.edl.preprocessor.schema.term.Union;
import de.uni_koblenz.edl.preprocessor.schema.term.Wildcard;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.MapVertexMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.GreqlQueryCache;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class EDL2EDLGraph extends EDL2EDLGraphBaseImpl {

	private final String searchPath;

	private boolean isSDFDefinition;

	private Definition definition;

	private Module startModule;

	private Module currentModule;

	private final List<String> modulesToParse = new ArrayList<String>();

	private int nextModule = 0;

	private final BooleanGraphMarker isTransitiveGroup = new BooleanGraphMarker(
			getGraph());

	private final MapVertexMarker<List<Integer>> argumentIndicatorOfGroup = new MapVertexMarker<List<Integer>>(
			getGraph());

	private final GreqlQueryCache queryCache = new GreqlQueryCache();

	private de.uni_koblenz.jgralab.schema.Schema targetSchema;

	private final Map<String, String> name2QualifiedName = new HashMap<String, String>();

	private Vertex nullLiteral;

	private Vertex trueLiteral;

	private Vertex falseLiteral;

	private Vertex omegaConstant;

	private Vertex alphaConstant;

	private Vertex headVariable = null;

	private Vertex emptyGrammar;

	private boolean isInLhsOfAssignment = false;

	private boolean isInDefaultValues = false;

	private boolean isDefaultValueOfEdgeClass = false;

	/**
	 * For each visibility stage a new {@link List} is put on this {@link Stack}
	 * .<br>
	 * Each maximal {@link PatternTerm} is added to the current {@link List}.
	 * The index i in this list is the same as $i.
	 */
	private final Stack<List<PatternTerm>> termsInVisibilityStage = new Stack<List<PatternTerm>>();

	/**
	 * For each visibility stage a new {@link Map} is put on this {@link Stack}.<br>
	 * Current {@link Map}: (index -&gt; BodyVariable)
	 */
	private final Stack<Map<Integer, BodyVariable>> bodyVarsInVisibilityStage = new Stack<Map<Integer, BodyVariable>>();

	/**
	 * For each visibility stage a new {@link Map} is put on this {@link Stack}.<br>
	 * Each labeled Term is put in the current {@link Map} (nameOfLabel -&gt;
	 * labeldTerm)
	 */
	private final Stack<Map<String, Term>> labelsInVisibilityStage = new Stack<Map<String, Term>>();

	/**
	 * For each visibility stage a new {@link Set} is put on this {@link Stack}
	 * .<br>
	 * {@link String} which is the name of used {@link TemporaryVariable} is
	 * added to the current {@link Set}.
	 */
	private final Stack<Set<String>> namesOfUsedTempVarsInVisibilityStage = new Stack<Set<String>>();

	private Pattern currentPattern = null;

	private boolean areSemanticActionsAllowed = true;

	private boolean isInHeadOfPattern = false;

	private boolean isABeforePattern = false;

	private boolean isContextFree = false;

	private boolean isStartSymbol = false;

	/**
	 * In S3 of {T1 T2 #S3#}+ or {T1 T2 #S3#}* $0 must not be assigned a new
	 * value. This field is only true if T2 has already been parsed and } not.
	 */
	private boolean isBehindSecondTermInList = false;

	private Island inclusiveIslandStart = null;

	private Island exclusiveIslandStart = null;

	private Island inclusiveIslandEnd = null;

	private Island exclusiveIslandEnd = null;

	public EDL2EDLGraph(String searchPath) {
		super("edl.tbl", EDLSchema.instance());
		this.searchPath = searchPath;
	}

	public EDL2EDLGraph(String searchPath,
			de.uni_koblenz.jgralab.schema.Schema schema) {
		super("edl.tbl", EDLSchema.instance());
		this.searchPath = searchPath;
		this.targetSchema = schema;
	}

	public EDL2EDLGraph(String searchPath, EDLGraph graph) {
		super("edl.tbl", graph);
		this.searchPath = searchPath;
	}

	public de.uni_koblenz.jgralab.schema.Schema getTargetSchema() {
		return targetSchema;
	}

	@Override
	public Graph parse(String[] inputFiles, String encoding, boolean debugMode,
			boolean verboseMode, boolean dotMode, String dotOutputFormat) {
		sort2position = new HashMap<String, StackElement>();
		for (String inputFile : inputFiles) {
			modulesToParse.add(inputFile);
			nextModule++;

			try {
				if (EDLPreprocessor.printDebugInformationToTheConsole) {
					System.out.println("\tParsing "
							+ (searchPath + inputFile + ".edl") + "...");
				}
				super.parse(new String[] { searchPath + inputFile + ".edl" },
						encoding, debugMode, verboseMode, dotMode,
						dotOutputFormat);
			} catch (SemanticActionException e) {
				if (e.getCause() instanceof FileNotFoundException) {
					throw new GrammarException(
							"\n"
									+ e.getMessage()
									+ "\nCheck if you gave only the start module name as input and NOT the path to the file.",
							e.getCause());
				} else {
					throw e;
				}
			}

			if (!isSDFDefinition) {
				while (nextModule < modulesToParse.size()) {
					String nextInputFile = searchPath
							+ modulesToParse.get(nextModule++) + ".edl";
					if (EDLPreprocessor.printDebugInformationToTheConsole) {
						System.out
								.println("\tParsing " + nextInputFile + "...");
					}
					super.parse(new String[] { nextInputFile }, encoding,
							debugMode, verboseMode, dotMode, dotOutputFormat);
				}
			}

			StringBuilder sb = new StringBuilder();
			String delim = "The following modules have been imported but were not defined:\n";
			for (Module m : ((EDLGraph) graph).getModuleVertices()) {
				if (m.getDegree(IsModuleOf.EC) == 0) {
					sb.append(delim).append(m.get_identifier().get_name());
					delim = " ";
				}
			}
			String errorMessage = sb.toString();
			if (!errorMessage.isEmpty()) {
				throw new GrammarException(errorMessage);
			}

			if (((EDLGraph) graph).getFirstIslandEnd() != null
					&& ((EDLGraph) graph).getFirstIslandStart() == null) {
				System.out
						.println("WARNING: There were only island ends defined."
								+ " Without defining an island start this grammar will not be an island grammar"
								+ " and the whole input files will be parsed.");
			}
		}
		for (Entry<String, StackElement> entry : sort2position.entrySet()) {
			StackElement currentElement = entry.getValue();
			if (currentElement != null) {
				System.out.println(createMessageString(
						currentElement,
						"WARNING: The syntactical variable (sort) \""
								+ entry.getKey()
								+ "\" was used but never defined."));
			}
		}
		return getGraph();
	}

	private boolean isStartModule() {
		return startModule == null || startModule == currentModule;
	}

	@SuppressWarnings("unchecked")
	private Set<Vertex> getFirstSectionOfCurrentModule(VertexClass vc) {
		GreqlQuery query = queryCache.getQuery("import "
				+ vc.getQualifiedName() + "; import section.IsSectionOf;"
				+ "using module: module <--{section.IsSectionOf}&{"
				+ vc.getQualifiedName() + "}");
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();
		environment.setVariable("module", currentModule);
		return (Set<Vertex>) query.evaluate(getGraph(), environment);
	}

	@SuppressWarnings("rawtypes")
	private List<SemanticAction> appendAllSemanticActions(Object semActs) {
		List<SemanticAction> ergList = new LinkedList<SemanticAction>();
		if (semActs instanceof Vertex
				&& ((Vertex) semActs).isInstanceOf(SemanticAction.VC)) {
			SemanticAction semActs2 = (SemanticAction) semActs;
			if (semActs2.getDegree() > 0) {
				ergList.add(semActs2);
			} else {
				deleteTree(semActs2);
			}

		} else if (semActs instanceof List) {
			List semanticActions = (List) semActs;
			if (!semanticActions.isEmpty()) {
				SemanticAction semanticAction = (SemanticAction) semanticActions
						.get(0);
				for (Object o : semanticActions) {
					SemanticAction current = (SemanticAction) o;
					if (current == semanticAction) {
						continue;
					}
					IsStatementOf iso = (IsStatementOf) current
							.getFirstIncidence(IsStatementOf.EC,
									EdgeDirection.IN);
					while (iso != null) {
						iso.setOmega(semanticAction);
						iso = (IsStatementOf) current.getFirstIncidence(
								IsStatementOf.EC, EdgeDirection.IN);
					}
					IsExpressionOfSemanticAction ieo = (IsExpressionOfSemanticAction) current
							.getFirstIncidence(IsExpressionOfSemanticAction.EC,
									EdgeDirection.IN);
					while (ieo != null) {
						ieo.setOmega(semanticAction);
						ieo = (IsExpressionOfSemanticAction) current
								.getFirstIncidence(
										IsExpressionOfSemanticAction.EC,
										EdgeDirection.IN);
					}
					deleteTree(current);
				}
				if (semanticAction.getDegree() > 0) {
					ergList.add(semanticAction);
				} else {
					deleteTree(semanticAction);
				}
			}
		}
		return ergList;
	}

	private Vertex getSchemaElementClass(String name,
			StackElement currentElement) {
		if (targetSchema == null) {
			throw new GrammarException(createMessageString(currentElement,
					"No schema has been defined yet."));
		}
		String qualifiedName = name;
		if (!name.contains(".")) {
			qualifiedName = name2QualifiedName.get(name);
			if (qualifiedName == null && !name2QualifiedName.containsKey(name)) {
				qualifiedName = initializeName2QualifiedName(currentElement,
						name, qualifiedName);
			}
		}
		if (qualifiedName == null) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							(!name2QualifiedName.containsKey(name) ? "No named element"
									: "Several named elements")
									+ " with name \""
									+ name
									+ "\" could be found in the schema."
									+ (name2QualifiedName.containsKey(name) ? " Use the fully quallified name."
											: "")));
		}
		Vertex schemaElement = schemaElementsTable.use(qualifiedName);
		if (schemaElement == null) {
			NamedElement namedElement = targetSchema
					.getNamedElement(qualifiedName);
			if (namedElement == null) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"\""
										+ name
										+ "\" is neither the name of an GraphElementClass,"
										+ " EnumDomain nor of a RecordDomain."));
			} else if (namedElement instanceof AttributedElementClass
					&& !(namedElement instanceof GraphClass)) {
				schemaElement = createVertex(GraphElementClass.VC,
						currentElement.getPosition());
			} else if (namedElement instanceof EnumDomain) {
				schemaElement = createVertex(Enumeration.VC,
						currentElement.getPosition());
			} else if (namedElement instanceof RecordDomain) {
				schemaElement = createVertex(Record.VC,
						currentElement.getPosition());
			} else {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"\""
										+ name
										+ "\" is neither the name of an GraphElementClass,"
										+ " EnumDomain nor of a RecordDomain."));
			}
			schemaElementsTable.declare(qualifiedName, schemaElement);
		}
		createIdentifierOfSchemaElementIfNeeded(currentElement, qualifiedName,
				schemaElement);
		return schemaElement;
	}

	private String initializeName2QualifiedName(StackElement currentElement,
			String name, String qualifiedName) {
		try {
			java.lang.reflect.Field namedElementsField = SchemaImpl.class
					.getDeclaredField("namedElements");
			namedElementsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, NamedElement> namedElements = (Map<String, NamedElement>) namedElementsField
					.get(targetSchema);
			for (String qualName : namedElements.keySet()) {
				String simpleName = qualName.substring(qualName
						.lastIndexOf(".") + 1);
				if (isInternal(namedElements.get(qualName))) {
					continue;
				}
				if (name2QualifiedName.containsKey(simpleName)) {
					// a duplicate simple name is found
					if (name.equals(simpleName)) {
						// the duplicate simple name is requested
						throw new GrammarException(
								createMessageString(
										currentElement,
										"\""
												+ name
												+ "\" does not identify an named element uniquely. Concerning named elements are: "
												+ name2QualifiedName
														.get(simpleName)
												+ " and "
												+ qualName
												+ " (Possibly more). Use the fully quallified name."));
					}
					name2QualifiedName.put(simpleName, null);
				} else {
					name2QualifiedName.put(simpleName, qualName);
					if (qualifiedName == null && name.equals(simpleName)) {
						qualifiedName = qualName;
					}
				}
			}
		} catch (NoSuchFieldException e) {
			qualifiedName = name;
			throw new GrammarException(e);
		} catch (SecurityException e) {
			qualifiedName = name;
			throw new GrammarException(e);
		} catch (IllegalArgumentException e) {
			qualifiedName = name;
			throw new GrammarException(e);
		} catch (IllegalAccessException e) {
			qualifiedName = name;
			throw new GrammarException(e);
		}
		return qualifiedName;
	}

	private boolean isInternal(NamedElement namedElement) {
		if (namedElement instanceof BasicDomain
				|| namedElement instanceof CollectionDomain
				|| namedElement instanceof MapDomain
				|| namedElement instanceof GraphClass) {
			return true;
		} else if (namedElement instanceof de.uni_koblenz.jgralab.schema.GraphElementClass) {
			return ((de.uni_koblenz.jgralab.schema.GraphElementClass<?, ?>) namedElement)
					.isDefaultGraphElementClass();
		} else {
			return false;
		}
	}

	private void createIdentifierOfSchemaElementIfNeeded(
			StackElement currentElement, String qualifiedName,
			Vertex schemaElement) {
		if (schemaElement.getFirstIncidence(IsNameOf.EC) == null) {
			Identifier identifier = (Identifier) createVertex(Identifier.VC,
					currentElement.getPosition());
			identifier.set_name(qualifiedName);
			if (schemaElement.isInstanceOf(Record.VC)) {
				createEdge(IsNameOfRecord.EC, identifier, schemaElement);
			} else if (schemaElement.isInstanceOf(Enumeration.VC)) {
				createEdge(IsNameOfEnumeration.EC, identifier, schemaElement);
			} else {
				createEdge(IsNameOfGraphElement.EC, identifier, schemaElement);
			}
		}
	}

	private boolean containsReturnStatement(String lexem) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
				.compile("(^|\\{|\\}|\\;|\\/|\\s)return(\\s|$)");
		Matcher matcher = pattern.matcher(lexem);
		return matcher.find();
	}

	private void deleteTree(Vertex vertex) {
		if ((vertex.isInstanceOf(Identifier.VC)
				|| vertex.isInstanceOf(Variable.VC)
				|| vertex.isInstanceOf(SymbolTableDefinition.VC)
				|| vertex.isInstanceOf(GraphElementClass.VC)
				|| vertex.isInstanceOf(Record.VC) || vertex
					.isInstanceOf(Enumeration.VC))
				&& vertex.getDegree(EdgeDirection.OUT) > 1) {
			// this vertex is used several times
			return;
		}
		Edge edge = vertex.getFirstIncidence(EdgeDirection.IN);
		while (edge != null) {
			if (edge.isInstanceOf(IsModuleOfId.EC)) {
				edge.delete();
			} else {
				Vertex that = edge.getThat();
				deleteTree(that);
				if (edge.isValid()) {
					edge.delete();
				}
			}
			edge = vertex.getFirstIncidence(EdgeDirection.IN);
		}
		vertex.delete();
	}

	private BodyVariable createBodyVariable(StackElement currentElement,
			PatternTerm referencedTerm, int indexOfTerm) {
		if (isABeforePattern) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"In a pattern which is executed before the matching rules the elements in the body could not be accessed because they have not been parsed yet."));
		}
		if (isBehindSecondTermInList && isInLhsOfAssignment && indexOfTerm == 0) {
			throw new GrammarException(
					createMessageString(currentElement,
							"In S3 of {T1 T2 #S3#}+ or {T1 T2 #S3#}* $0 must not be assigned a new value."));
		}
		BodyVariable bodyVar = bodyVarsInVisibilityStage.peek()
				.get(indexOfTerm);
		if (bodyVar == null) {
			bodyVar = (BodyVariable) createVertex(BodyVariable.VC,
					currentElement.getPosition());
			bodyVar.set_index(indexOfTerm);
			bodyVarsInVisibilityStage.peek().put(indexOfTerm, bodyVar);
		} else {
			positionsMap.put(bodyVar, currentElement.getPosition());
		}
		if (referencedTerm != null) {
			IsReferencedMaximalTerm irmt = bodyVar
					.getFirstIsReferencedMaximalTermIncidence();
			if (irmt == null) {
				createEdge(IsReferencedMaximalTerm.EC, bodyVar, referencedTerm);
			} else {
				assert irmt.getThat() == referencedTerm;
			}
		}
		return bodyVar;
	}

	private int getMaximumIndexOfTermsInCurrentVisibiltyStage() {
		if (currentPattern == null) {
			if (isStartSymbol) {
				return isContextFree ? 0 : 0;
			} else {
				// this is a production
				return termsInVisibilityStage.peek().size() - 1;
			}
		} else {
			// this is a pattern count maximum multiplicities
			int maxIndex = -1;
			for (PatternTerm patternTerm : termsInVisibilityStage.peek()) {
				int nextStep = 1;
				Multiplicity multiplicity = patternTerm.get_multiplicity();
				if (multiplicity != null) {
					nextStep = multiplicity.get_max();
				}
				// be aware of overrun
				maxIndex = maxIndex + nextStep >= maxIndex ? maxIndex
						+ nextStep : Integer.MAX_VALUE;
				if (maxIndex == Integer.MAX_VALUE) {
					break;
				}
			}
			return maxIndex;
		}
	}

	private void enterVisibilityStage() {
		termsInVisibilityStage.push(new ArrayList<PatternTerm>());
		labelsInVisibilityStage.push(new HashMap<String, Term>());
		bodyVarsInVisibilityStage.push(new HashMap<Integer, BodyVariable>());
		namesOfUsedTempVarsInVisibilityStage.push(new HashSet<String>());
	}

	private void leaveVisibilityStage() {
		termsInVisibilityStage.pop();
		labelsInVisibilityStage.pop();
		bodyVarsInVisibilityStage.pop();
		namesOfUsedTempVarsInVisibilityStage.pop();
	}

	private void nextTermInVisibilityStage(PatternTerm patternTerm) {
		termsInVisibilityStage.peek().add(patternTerm);
	}

	@Override
	protected void setDefaultValues(Edge edge) {
		Position alphaPos = positionsMap.get(edge.getAlpha());
		assert alphaPos != null : edge.getAlpha() + " is not in positionsMap: "
				+ positionsMap;
		edge.setAttribute("column", alphaPos.getFirstColumn());
		edge.setAttribute("line", alphaPos.getFirstLine());
		edge.setAttribute("offset", alphaPos.getOffset());
		edge.setAttribute("length", alphaPos.getLength());
	}

	@Override
	protected void setDefaultValues(Vertex vertex) {
	}

	/**
	 * "QualifiedModuleName" is mapped on Module
	 */
	public final SymbolTableStack moduleTable = new SymbolTableStack(
			"moduleTable", this, new VertexClass[] { Module.VC });

	/**
	 * "GraphElementClassName.FieldName" is mapped on Identifier
	 */
	public final SymbolTableStack fieldTable = new SymbolTableStack(
			"fieldTable", this, new VertexClass[] { Identifier.VC });

	/**
	 * "SymbolTableName" is mapped on SymbolTableDefinition
	 */
	public final SymbolTableStack symbolTableTable = new SymbolTableStack(
			"symbolTableTable", this,
			new VertexClass[] { SymbolTableDefinition.VC });

	/**
	 * "sortName" is mapped on Identifier of sort
	 */
	public final SymbolTableStack sortTable = new SymbolTableStack("sortTable",
			this, new VertexClass[] { Identifier.VC });

	/**
	 * "qualifiedName" is mapped on GraphElementClass Enumeration or Record
	 * vertex
	 */
	public final SymbolTableStack schemaElementsTable = new SymbolTableStack(
			"schemaElementsTable", this, new VertexClass[] {
					GraphElementClass.VC, Enumeration.VC, Record.VC });

	/**
	 * "qualifiedNameOfEnumeration.EnumConstant" is mapped on EnumConstant
	 */
	public final SymbolTableStack enumerationConstantsTable = new SymbolTableStack(
			"enumerationConstantsTable", this,
			new VertexClass[] { EnumConstant.VC });

	/**
	 * "nameOfTemporaryVariable" is mapped on TemporaryVariable
	 */
	public final SymbolTableStack tempVarTable = new SymbolTableStack(
			"tempVarTable", this, new VertexClass[] { TemporaryVariable.VC });

	// #####################
	// grammar/Main
	// #####################

	// #####################
	// grammar/SDF
	// #####################

	/*
	 * Rule 804: "definition" Module* -> Definition {cons("definition")}
	 */

	/**
	 * Rule 785: "definition" cf(LAYOUT?) cf(Module*) -&gt; cf(Definition)
	 * {cons("definition"), definedAs(
	 * "\"definition\" Module* -&gt; Definition {cons(\"definition\")}")}<br>
	 */
	@Override
	protected void execute_Rule785_Position0(StackElement currentElement) {
		isSDFDefinition = true;
		definition = (Definition) createVertex(Definition.VC,
				currentElement.getPosition());
		currentElement.setResult(definition);
	}

	/**
	 * Rule 785: "definition" cf(LAYOUT?) cf(Module*) -&gt; cf(Definition)
	 * {cons("definition"), definedAs(
	 * "\"definition\" Module* -&gt; Definition {cons(\"definition\")}")}<br>
	 * Rule 369: cf(Module) -&gt; cf(Module+)<br>
	 */
	@Override
	protected void execute_Rule785_Term2_Rule369_Position1(
			StackElement currentElement) {
		createEdge(IsModuleOf.EC, currentElement.getChild(0).getResult(),
				definition);
	}

	/*
	 * Rule 805: Module -> SDF
	 */

	/**
	 * Rule 786: cf(Module) -&gt; cf(SDF) {definedAs("Module -&gt; SDF")}<br>
	 */
	@Override
	protected void execute_Rule786_Position0(StackElement currentElement) {
		isSDFDefinition = false;
	}

	/**
	 * Rule 786: cf(Module) -&gt; cf(SDF) {definedAs("Module -&gt; SDF")}<br>
	 */
	@Override
	protected void execute_Rule786_Position1(StackElement currentElement) {
		if (definition == null) {
			definition = (Definition) createVertex(Definition.VC,
					currentElement.getPosition());
		}
		createEdge(IsModuleOf.EC, currentElement.getChild(0).getResult(),
				definition);
	}

	// #####################
	// grammar/Module
	// #####################

	/*
	 * Rule 817: "module" ModuleName InitialSection* Section* -> Module
	 * {cons("module")}
	 */

	/**
	 * Rule 798: "module" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?)
	 * cf(InitialSection*) cf(LAYOUT?) cf(Section*) -&gt; cf(Module)
	 * {cons("module"), definedAs(
	 * "\"module\" ModuleName InitialSection* Section* -&gt; Module {cons(\"module\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule798_Position3(StackElement currentElement) {
		Module module = (Module) currentElement.getChild(2).getResult();
		positionsMap.put(module, currentElement.getPosition());
		currentElement.setResult(module);
		currentModule = module;
		if (!isStartModule()) {
			symbolTableTable.push();
		} else {
			startModule = module;
		}
		String nameOfModule = module.get_identifier().get_name();
		if (isStartModule() && !modulesToParse.get(0).equals(nameOfModule)) {
			modulesToParse.set(0, nameOfModule);
		}
	}

	/**
	 * Rule 798: "module" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?)
	 * cf(InitialSection*) cf(LAYOUT?) cf(Section*) -&gt; cf(Module)
	 * {cons("module"), definedAs(
	 * "\"module\" ModuleName InitialSection* Section* -&gt; Module {cons(\"module\")}"
	 * )}<br>
	 * Rule 408: cf(InitialSection) -&gt; cf(InitialSection+)<br>
	 */
	@Override
	protected void execute_Rule798_Term4_Rule408_Position1(
			StackElement currentElement) {
		Object result = currentElement.getChild(0).getResult();
		if (result != null) {
			if (result instanceof List) {
				@SuppressWarnings("unchecked")
				List<InitialSection> list = (List<InitialSection>) result;
				for (InitialSection i : list) {
					handleInitialSection(currentElement, i);
				}
			} else {
				handleInitialSection(currentElement, (InitialSection) result);
			}
		}
	}

	private void handleInitialSection(StackElement currentElement,
			InitialSection initialSection) {
		if (!(initialSection.isInstanceOf(Island.VC) || initialSection
				.isInstanceOf(Schema.VC))
				&& initialSection.getDegree(EdgeDirection.IN) == 0) {
			deleteTree(initialSection);
		} else {
			Vertex module = (Vertex) currentElement
					.getParentApplicationOfDefinedRule().getResult();
			if (!initialSection.isInstanceOf(ImportDeclarations.VC)
					&& !initialSection.isInstanceOf(UserCodeSection.VC)
					&& !initialSection.isInstanceOf(DefaultValues.VC)) {
				Edge edge = initialSection.getFirstIncidence(IsSectionOf.EC,
						EdgeDirection.OUT);
				if (edge == null || edge.getThat() != module) {
					createEdge(IsSectionOf.EC, initialSection, module);
				}
			}
		}
	}

	/**
	 * Rule 798: "module" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?)
	 * cf(InitialSection*) cf(LAYOUT?) cf(Section*) -&gt; cf(Module)
	 * {cons("module"), definedAs(
	 * "\"module\" ModuleName InitialSection* Section* -&gt; Module {cons(\"module\")}"
	 * )}<br>
	 * Rule 401: cf(Section) -&gt; cf(Section+)<br>
	 */
	@Override
	protected void execute_Rule798_Term6_Rule401_Position1(
			StackElement currentElement) {
		Vertex section = (Vertex) currentElement.getChild(0).getResult();
		if (section != null) {
			if (section.getDegree(EdgeDirection.IN) == 0) {
				deleteTree(section);
			} else {
				Vertex module = (Vertex) currentElement
						.getParentApplicationOfDefinedRule().getResult();
				Edge edge = section.getFirstIncidence(IsSectionOf.EC,
						EdgeDirection.OUT);
				if (edge == null || edge.getThat() != module) {
					createEdge(IsSectionOf.EC, section, module);
				}
			}
		}
	}

	/**
	 * Rule 798: "module" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?)
	 * cf(InitialSection*) cf(LAYOUT?) cf(Section*) -&gt; cf(Module)
	 * {cons("module"), definedAs(
	 * "\"module\" ModuleName InitialSection* Section* -&gt; Module {cons(\"module\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule798_Position7(StackElement currentElement) {
		Module module = (Module) currentElement.getResult();
		for (Edge edge : module.incidences(IsModuleOf.EC)) {
			IsModuleOf imo = (IsModuleOf) edge;
			imo.set_column(currentElement.getFirstColumn());
			imo.set_length(currentElement.getLength());
			imo.set_line(currentElement.getFirstLine());
			imo.set_offset(currentElement.getOffset());
		}
		if (!isStartModule()
				&& symbolTableTable.getTop() != symbolTableTable.getBottom()) {
			symbolTableTable.pop();
		}
	}

	/*
	 * Rule 815: ModuleId "[" Term* "]" -> ModuleName {cons("parameterized")}
	 */

	/**
	 * Rule 796: cf(ModuleId) cf(LAYOUT?) "[" cf(LAYOUT?) cf(Term*) cf(LAYOUT?)
	 * "]" -&gt; cf(ModuleName) {cons("parameterized"), definedAs(
	 * "ModuleId \"[\" Term* \"]\" -&gt; ModuleName {cons(\"parameterized\")}")}<br>
	 */
	@Override
	protected void execute_Rule796_Position7(StackElement currentElement) {
		String message = "WARNING: It can not be guarenteed, that parameterized modules work correctly with EDL.";
		System.out.println(createMessageString(currentElement, message));

		String nameOfModule = currentElement.getChild(0).getLexem();
		Vertex module = moduleTable.use(nameOfModule);
		if (module == null) {
			module = moduleTable.declare(nameOfModule,
					createVertex(Module.VC, currentElement.getPosition()));
		}
		if (module.getDegree(IsNameOfModule.EC) == 0) {
			Identifier identifier = (Identifier) createVertex(Identifier.VC,
					currentElement.getChild(0).getPosition());
			identifier.set_name(nameOfModule);
			createEdge(IsNameOfModule.EC, identifier, module);
		}
		createEdge(IsParameterOfModule.EC, currentElement.getChild(4)
				.getResult(), module);
		currentElement.setResult(module);
	}

	/*
	 * Rule 816: ModuleId -> ModuleName {cons("unparameterized")}
	 */

	/**
	 * Rule 797: cf(ModuleId) -&gt; cf(ModuleName) {cons("unparameterized"),
	 * definedAs("ModuleId -&gt; ModuleName {cons(\"unparameterized\")}")}<br>
	 */
	@Override
	protected void execute_Rule797_Position1(StackElement currentElement) {
		String nameOfModule = currentElement.getChild(0).getLexem();
		Vertex module = moduleTable.use(nameOfModule);
		if (module == null) {
			module = moduleTable.declare(nameOfModule,
					createVertex(Module.VC, currentElement.getPosition()));
		}
		if (module.getDegree(IsNameOfModule.EC) == 0) {
			Identifier identifier = (Identifier) createVertex(Identifier.VC,
					currentElement.getChild(0).getPosition());
			identifier.set_name(nameOfModule);
			createEdge(IsNameOfModule.EC, identifier, module);
		}
		currentElement.setResult(module);
	}

	// #####################
	// grammar/sections/DefaultValuesSection
	// #####################

	/*
	 * Rule 835: DefaultValuesSection -> InitialSection
	 * {cons("default-values-section")}
	 */

	/**
	 * Rule 816: cf(DefaultValuesSection) -&gt; cf(InitialSection)
	 * {cons("default-values-section"), definedAs(
	 * "DefaultValuesSection -&gt; InitialSection {cons(\"default-values-section\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule816_Position1(StackElement currentElement) {
		if (isStartModule()) {
			currentElement.setResult(currentElement.getChild(0).getResult());
		} else {
			System.out
					.println(createMessageString(
							currentElement,
							"WARNING Only default values section of the start module will be recognized."
									+ " The following default section will be ignored."));
		}
	}

	/*
	 * Rule 834: "default" "values" DefaultStatement+ -> DefaultValuesSection
	 * {cons("default-values")}
	 */

	/**
	 * Rule 815: "default" cf(LAYOUT?) "values" cf(LAYOUT?)
	 * cf(DefaultStatement+) -&gt; cf(DefaultValuesSection)
	 * {cons("default-values"), definedAs(
	 * "\"default\" \"values\" DefaultStatement+ -&gt; DefaultValuesSection {cons(\"default-values\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule815_Position0(StackElement currentElement) {
		if (isStartModule()) {
			// append all default values declarations to one definition
			Vertex defaultValues = graph.getFirstVertex(DefaultValues.VC);
			if (defaultValues == null) {
				defaultValues = createVertex(DefaultValues.VC,
						currentElement.getPosition());
				createEdge(IsSectionOf.EC, defaultValues, startModule);
			}
			currentElement.setResult(defaultValues);
		}
	}

	/**
	 * Rule 815: "default" cf(LAYOUT?) "values" cf(LAYOUT?)
	 * cf(DefaultStatement+) -&gt; cf(DefaultValuesSection)
	 * {cons("default-values"), definedAs(
	 * "\"default\" \"values\" DefaultStatement+ -&gt; DefaultValuesSection {cons(\"default-values\")}"
	 * )}<br>
	 * Rule 431: cf(DefaultStatement) -&gt; cf(DefaultStatement+)<br>
	 */
	@Override
	protected void execute_Rule815_Term4_Rule431_Position1(
			StackElement currentElement) {
		if (isStartModule()) {
			createEdge(IsDefaultStatementOf.EC, currentElement.getChild(0)
					.getResult(), currentElement
					.getParentApplicationOfDefinedRule().getResult());
		}
	}

	/*
	 * GraphElementClass "." JavaId "=" Expression ";" -> DefaultStatement
	 * {cons("default-statement")}
	 */

	/**
	 * Rule 814: cf(GraphElementClass) cf(LAYOUT?) "." cf(LAYOUT?) cf(JavaId)
	 * cf(LAYOUT?) "=" cf(LAYOUT?) cf(Expression) cf(LAYOUT?) ";" -&gt;
	 * cf(DefaultStatement) {cons("default-statement"), definedAs(
	 * "GraphElementClass \".\" JavaId \"=\" Expression \";\" -&gt; DefaultStatement {cons(\"default-statement\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule814_Position0(StackElement currentElement) {
		isInDefaultValues = true;
	}

	/**
	 * Rule 814: cf(GraphElementClass) cf(LAYOUT?) "." cf(LAYOUT?) cf(JavaId)
	 * cf(LAYOUT?) "=" cf(LAYOUT?) cf(Expression) cf(LAYOUT?) ";" -&gt;
	 * cf(DefaultStatement) {cons("default-statement"), definedAs(
	 * "GraphElementClass \".\" JavaId \"=\" Expression \";\" -&gt; DefaultStatement {cons(\"default-statement\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule814_Position1(StackElement currentElement) {
		Vertex graphElementClass = (Vertex) currentElement.getChild(0)
				.getResult();
		if (!graphElementClass.isInstanceOf(GraphElementClass.VC)) {
			throw new GrammarException(
					createMessageString(
							currentElement.getChild(0),
							currentElement.getChild(0).getLexem()
									+ " is no GraphElementClass."
									+ " In an default values section only attributes of"
									+ " GraphElements can be defined."));
		}
		Identifier identiferOfGraphElementClass = (Identifier) graphElementClass
				.getFirstIncidence(IsNameOfGraphElement.EC).getThat();
		String nameOfGraphElementClass = identiferOfGraphElementClass
				.get_name();
		isDefaultValueOfEdgeClass = (targetSchema
				.getAttributedElementClass(nameOfGraphElementClass) instanceof EdgeClass);
	}

	/**
	 * Rule 814: cf(GraphElementClass) cf(LAYOUT?) "." cf(LAYOUT?) cf(JavaId)
	 * cf(LAYOUT?) "=" cf(LAYOUT?) cf(Expression) cf(LAYOUT?) ";" -&gt;
	 * cf(DefaultStatement) {cons("default-statement"), definedAs(
	 * "GraphElementClass \".\" JavaId \"=\" Expression \";\" -&gt; DefaultStatement {cons(\"default-statement\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule814_Position11(StackElement currentElement) {
		Vertex graphElementClass = (Vertex) currentElement.getChild(0)
				.getResult();
		if (!graphElementClass.isInstanceOf(GraphElementClass.VC)) {
			throw new GrammarException(
					createMessageString(
							currentElement.getChild(0),
							currentElement.getChild(0).getLexem()
									+ " is no GraphElementClass."
									+ " In an default values section only attributes of"
									+ " GraphElements can be defined."));
		}
		Identifier identiferOfGraphElementClass = (Identifier) graphElementClass
				.getFirstIncidence(IsNameOfGraphElement.EC).getThat();
		String nameOfGraphElementClass = identiferOfGraphElementClass
				.get_name();
		if (isStartModule()) {

			// create field
			String nameOfField = currentElement.getChild(4).getLexem();
			checkExcistenceOfField(currentElement.getChild(4),
					identiferOfGraphElementClass.get_name(), nameOfField);
			Vertex vertex = fieldTable.use(nameOfGraphElementClass + "."
					+ currentElement.getChild(4).getLexem());
			if (vertex == null) {
				vertex = fieldTable.declare(
						nameOfGraphElementClass + "."
								+ currentElement.getChild(4).getLexem(),
						createVertex(Identifier.VC, currentElement.getChild(4)
								.getPosition()));
			}
			Identifier fieldIdentifier = (Identifier) vertex;
			if (fieldIdentifier.get_name() == null) {
				fieldIdentifier.set_name(nameOfField);
			}
			Vertex field = createVertex(Field.VC, currentElement.getChild(4)
					.getPosition());
			createEdge(IsNameOfField.EC, fieldIdentifier, field);

			// create dot Access
			Vertex dotAccess = createVertex(DotAccess.VC, new Position(
					currentElement.getOffset(), currentElement.getChild(0)
							.getLength()
							+ currentElement.getChild(1).getLength()
							+ currentElement.getChild(2).getLength()
							+ currentElement.getChild(3).getLength()
							+ currentElement.getChild(4).getLength(),
					currentElement.getFirstLine(), currentElement.getChild(4)
							.getLastLine(), currentElement.getFirstColumn(),
					currentElement.getChild(4).getLastColumn()));
			createEdge(IsAccessedGraphElementOf.EC, currentElement.getChild(0)
					.getResult(), dotAccess);
			createEdge(IsAccessedBy.EC, field, dotAccess);

			// create assignment
			Vertex assignment = createVertex(Assignment.VC,
					currentElement.getPosition());
			createEdge(IsAssignedElementOf.EC, dotAccess, assignment);
			createEdge(IsAssignedValueOf.EC, currentElement.getChild(8)
					.getResult(), assignment);

			// create expression statement
			Vertex expressionStatement = createVertex(ExpressionStatement.VC,
					currentElement.getPosition());
			createEdge(IsExpressionOfStatement.EC, assignment,
					expressionStatement);

			currentElement.setResult(expressionStatement);
		} else {
			if (graphElementClass.getDegree() == 1
					&& graphElementClass.getFirstIncidence().isInstanceOf(
							IsNameOfGraphElement.EC)) {
				deleteTree(graphElementClass);
			}
			Expression expression = (Expression) currentElement.getChild(8)
					.getResult();
			if (expression.getDegree(EdgeDirection.OUT) == 0) {
				deleteTree(expression);
			}
		}
		isDefaultValueOfEdgeClass = false;
		isInDefaultValues = false;
	}

	private void checkExcistenceOfField(StackElement currentElement,
			String nameOfGraphElementClass, String nameOfField) {
		AttributedElementClass<?, ?> aec = targetSchema
				.getAttributedElementClass(nameOfGraphElementClass);
		if (aec.getAttribute(nameOfField) == null) {
			throw new GrammarException(createMessageString(currentElement,
					nameOfGraphElementClass
							+ " does not have an attribute with name \""
							+ nameOfField + "\"."));
		}
	}

	// #####################
	// grammar/sections/ExportsSection
	// #####################

	/*
	 * Rule 865: "exports" Grammar -> Section {cons("exports")}
	 */

	/**
	 * Rule 846: "exports" cf(LAYOUT?) cf(Grammar) -&gt; cf(Section)
	 * {cons("exports"),
	 * definedAs("\"exports\" Grammar -&gt; Section {cons(\"exports\")}")}<br>
	 */
	@Override
	protected void execute_Rule846_Position3(StackElement currentElement) {
		Vertex exports = createVertex(Exports.VC, currentElement.getPosition());
		createEdge(IsContainedInExports.EC, currentElement.getChild(2)
				.getResult(), exports);
		currentElement.setResult(exports);
	}

	// #####################
	// grammar/sections/GlobalActionsSection
	// #####################

	/*
	 * Rule 830: GlobalActionsSection -> InitialSection
	 * {cons("global-actions-section")}
	 */

	/**
	 * Rule 811: cf(GlobalActionsSection) -&gt; cf(InitialSection)
	 * {cons("global-actions-section"), definedAs(
	 * "GlobalActionsSection -&gt; InitialSection {cons(\"global-actions-section\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule811_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "global" "actions" Pattern+ -> GlobalActionsSection
	 * {cons("global-actions")}
	 */

	/**
	 * Rule 810: "global" cf(LAYOUT?) "actions" cf(LAYOUT?) cf(Pattern+) -&gt;
	 * cf(GlobalActionsSection) {cons("global-actions"), definedAs(
	 * "\"global\" \"actions\" Pattern+ -&gt; GlobalActionsSection {cons(\"global-actions\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule810_Position0(StackElement currentElement) {
		// append all global actions of a module to one definition
		Set<Vertex> result = getFirstSectionOfCurrentModule(GlobalAction.VC);
		Vertex globalActions = null;
		if (result == null || result.isEmpty()) {
			globalActions = createVertex(GlobalAction.VC,
					currentElement.getPosition());
		} else {
			assert result.size() == 1;
			Iterator<Vertex> iterator = result.iterator();
			assert iterator.hasNext();
			if (iterator.hasNext()) {
				globalActions = iterator.next();
			}
			assert !iterator.hasNext();
		}
		assert globalActions != null;
		currentElement.setResult(globalActions);
	}

	/**
	 * Rule 810: "global" cf(LAYOUT?) "actions" cf(LAYOUT?) cf(Pattern+) -&gt;
	 * cf(GlobalActionsSection) {cons("global-actions"), definedAs(
	 * "\"global\" \"actions\" Pattern+ -&gt; GlobalActionsSection {cons(\"global-actions\")}"
	 * )}<br>
	 * Rule 424: cf(Pattern) -&gt; cf(Pattern+)<br>
	 */
	@Override
	protected void execute_Rule810_Term4_Rule424_Position1(
			StackElement currentElement) {
		Vertex pattern = (Vertex) currentElement.getChild(0).getResult();
		if (pattern.getDegree(IsFollowingSemanticActionOf.EC) != 0) {
			createEdge(IsPatternOf.EC, pattern, currentElement
					.getParentApplicationOfDefinedRule().getResult());
		} else {
			deleteTree(pattern);
		}
	}

	// #####################
	// grammar/sections/HiddensSection
	// #####################

	/*
	 * "hiddens" Grammar -> Section {cons("hiddens")}
	 */

	/**
	 * Rule 844: "hiddens" cf(LAYOUT?) cf(Grammar) -&gt; cf(Section)
	 * {cons("hiddens"),
	 * definedAs("\"hiddens\" Grammar -&gt; Section {cons(\"hiddens\")}")}<br>
	 */
	@Override
	protected void execute_Rule844_Position3(StackElement currentElement) {
		Vertex hiddens = createVertex(Hiddens.VC, currentElement.getPosition());
		createEdge(IsContainedInHiddens.EC, currentElement.getChild(2)
				.getResult(), hiddens);
		currentElement.setResult(hiddens);
	}

	// #####################
	// grammar/sections/ImportDeclarationsSection
	// #####################

	/*
	 * ImportDeclarationsSection -> InitialSection {cons("import-decl-section")}
	 */

	/**
	 * Rule 829: cf(ImportDeclarationsSection) -&gt; cf(InitialSection)
	 * {cons("import-decl-section"), definedAs(
	 * "ImportDeclarationsSection -&gt; InitialSection {cons(\"import-decl-section\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule829_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "import" "declarations" ImportDec+ -> ImportDeclarationsSection
	 * {cons("import-decl")}
	 */

	/**
	 * Rule 828: "import" cf(LAYOUT?) "declarations" cf(LAYOUT?) cf(ImportDec+)
	 * -&gt; cf(ImportDeclarationsSection) {cons("import-decl"), definedAs(
	 * "\"import\" \"declarations\" ImportDec+ -&gt; ImportDeclarationsSection {cons(\"import-decl\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule828_Position0(StackElement currentElement) {
		// append all import declarations to the first import declarations
		Vertex importDeclarations = graph.getFirstVertex(ImportDeclarations.VC);
		if (importDeclarations == null) {
			importDeclarations = createVertex(ImportDeclarations.VC,
					currentElement.getPosition());
			createEdge(IsSectionOf.EC, importDeclarations, startModule);
		}
		currentElement.setResult(importDeclarations);
	}

	/**
	 * Rule 828: "import" cf(LAYOUT?) "declarations" cf(LAYOUT?) cf(ImportDec+)
	 * -&gt; cf(ImportDeclarationsSection) {cons("import-decl"), definedAs(
	 * "\"import\" \"declarations\" ImportDec+ -&gt; ImportDeclarationsSection {cons(\"import-decl\")}"
	 * )}<br>
	 * Rule 445: cf(ImportDec) -&gt; cf(ImportDec+)<br>
	 */
	@Override
	protected void execute_Rule828_Term4_Rule445_Position1(
			StackElement currentElement) {
		JavaImport javaImport = (JavaImport) currentElement.getChild(0)
				.getResult();
		ImportDeclarations importDeclarations = (ImportDeclarations) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		for (IsJavaImportOf ijio : importDeclarations
				.getIsJavaImportOfIncidences()) {
			JavaImport existingJavaImport = (JavaImport) ijio.getThat();
			if (javaImport.is_isStatic() == existingJavaImport.is_isStatic()
					&& javaImport.get_importDeclaration().equals(
							existingJavaImport.get_importDeclaration())) {
				// an equal import was already declared
				deleteTree(javaImport);
				return;
			}
		}
		createEdge(IsJavaImportOf.EC, javaImport, importDeclarations);
	}

	/*
	 * TypeName ";" -> ImportDec {cons("TypeImportDec")}
	 */

	/**
	 * Rule 827: cf(TypeName) cf(LAYOUT?) ";" -&gt; cf(ImportDec)
	 * {cons("TypeImportDec"),
	 * definedAs("TypeName \";\" -&gt; ImportDec {cons(\"TypeImportDec\")}")}<br>
	 */
	@Override
	protected void execute_Rule827_Position3(StackElement currentElement) {
		String importString = (String) currentElement.getChild(0).getResult();
		JavaImport javaImport = (JavaImport) createVertex(JavaImport.VC,
				currentElement.getPosition());
		javaImport.set_isStatic(false);
		javaImport.set_importDeclaration(importString);
		currentElement.setResult(javaImport);
	}

	/*
	 * PackageName "." "*" ";" -> ImportDec {cons("TypeImportOnDemandDec")}
	 */

	/**
	 * Rule 826: cf(PackageName) cf(LAYOUT?) "." cf(LAYOUT?) "*" cf(LAYOUT?) ";"
	 * -&gt; cf(ImportDec) {cons("TypeImportOnDemandDec"), definedAs(
	 * "PackageName \".\" \"*\" \";\" -&gt; ImportDec {cons(\"TypeImportOnDemandDec\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule826_Position7(StackElement currentElement) {
		String importString = ((String) currentElement.getChild(0).getResult())
				+ ".*";
		JavaImport javaImport = (JavaImport) createVertex(JavaImport.VC,
				currentElement.getPosition());
		javaImport.set_isStatic(false);
		javaImport.set_importDeclaration(importString);
		currentElement.setResult(javaImport);
	}

	/*
	 * "static" TypeName "." JavaId ";" -> ImportDec {cons("StaticImportDec")}
	 */

	/**
	 * Rule 825: "static" cf(LAYOUT?) cf(TypeName) cf(LAYOUT?) "." cf(LAYOUT?)
	 * cf(JavaId) cf(LAYOUT?) ";" -&gt; cf(ImportDec) {cons("StaticImportDec"),
	 * definedAs(
	 * "\"static\" TypeName \".\" JavaId \";\" -&gt; ImportDec {cons(\"StaticImportDec\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule825_Position9(StackElement currentElement) {
		String importString = ((String) currentElement.getChild(2).getResult())
				+ "." + currentElement.getChild(6).getLexem();
		JavaImport javaImport = (JavaImport) createVertex(JavaImport.VC,
				currentElement.getPosition());
		javaImport.set_isStatic(true);
		javaImport.set_importDeclaration(importString);
		currentElement.setResult(javaImport);
	}

	/*
	 * "static" TypeName "." "*" ";" -> ImportDec
	 * {cons("StaticImportOnDemandDec")}
	 */

	/**
	 * Rule 824: "static" cf(LAYOUT?) cf(TypeName) cf(LAYOUT?) "." cf(LAYOUT?)
	 * "*" cf(LAYOUT?) ";" -&gt; cf(ImportDec) {cons("StaticImportOnDemandDec"),
	 * definedAs(
	 * "\"static\" TypeName \".\" \"*\" \";\" -&gt; ImportDec {cons(\"StaticImportOnDemandDec\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule824_Position9(StackElement currentElement) {
		String importString = ((String) currentElement.getChild(2).getResult())
				+ ".*";
		JavaImport javaImport = (JavaImport) createVertex(JavaImport.VC,
				currentElement.getPosition());
		javaImport.set_isStatic(true);
		javaImport.set_importDeclaration(importString);
		currentElement.setResult(javaImport);
	}

	// #####################
	// grammar/sections/ImportsSection
	// #####################

	/*
	 * ImportsSection -> InitialSection {cons("imp-section")}
	 */

	/**
	 * Rule 853: cf(ImportsSection) -&gt; cf(InitialSection)
	 * {cons("imp-section"),
	 * definedAs("ImportsSection -&gt; InitialSection {cons(\"imp-section\")}")}<br>
	 */
	@Override
	protected void execute_Rule853_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * ImportsSection -> Grammar {cons("imp-section")}
	 */

	/**
	 * Rule 852: cf(ImportsSection) -&gt; cf(Grammar) {cons("imp-section"),
	 * definedAs("ImportsSection -&gt; Grammar {cons(\"imp-section\")}")}<br>
	 */
	@Override
	protected void execute_Rule852_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "imports" Import* -> ImportsSection {cons("imports")}
	 */

	/**
	 * Rule 851: "imports" cf(LAYOUT?) cf(Import*) -&gt; cf(ImportsSection)
	 * {cons("imports"), definedAs(
	 * "\"imports\" Import* -&gt; ImportsSection {cons(\"imports\")}")}<br>
	 */
	@Override
	protected void execute_Rule851_Position0(StackElement currentElement) {
		currentElement.setResult(createVertex(Imports.VC,
				currentElement.getPosition()));
	}

	/**
	 * Rule 851: "imports" cf(LAYOUT?) cf(Import*) -&gt; cf(ImportsSection)
	 * {cons("imports"), definedAs(
	 * "\"imports\" Import* -&gt; ImportsSection {cons(\"imports\")}")}<br>
	 * Rule 468: cf(Import) -&gt; cf(Import+)<br>
	 */
	@Override
	protected void execute_Rule851_Term2_Rule468_Position1(
			StackElement currentElement) {
		createEdge(IsImportOf.EC, currentElement.getChild(0).getResult(),
				currentElement.getParentApplicationOfDefinedRule().getResult());
	}

	/*
	 * ModuleName -> Import {prefer,cons(\"module\")}
	 */

	/**
	 * Rule 850: cf(ModuleName) -&gt; cf(Import) {prefer, cons("module"),
	 * definedAs("ModuleName -&gt; Import {prefer,cons(\"module\")}")}<br>
	 */
	@Override
	protected void execute_Rule850_Position1(StackElement currentElement) {
		Vertex importVertex = createVertex(Import.VC,
				currentElement.getPosition());
		currentElement.setResult(importVertex);

		Module module = (Module) currentElement.getChild(0).getResult();
		createEdge(ImportsModule.EC, importVertex, module);

		// add imported module to be parsed
		String nameOfImportedModule = module.get_identifier().get_name();
		if (!modulesToParse.contains(nameOfImportedModule)) {
			modulesToParse.add(nameOfImportedModule);
		}
	}

	/*
	 * "(" ModuleName ")" -> Import {bracket}
	 */

	/**
	 * Rule 848: "(" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?) ")" -&gt; cf(Import)
	 * {bracket, definedAs("\"(\" ModuleName \")\" -&gt; Import {bracket}")}<br>
	 */
	@Override
	protected void execute_Rule848_Position5(StackElement currentElement) {
		Vertex importVertex = createVertex(Import.VC,
				currentElement.getPosition());
		currentElement.setResult(importVertex);

		Module module = (Module) currentElement.getChild(2).getResult();
		createEdge(ImportsModule.EC, importVertex, module);

		// add imported module to be parsed
		String nameOfImportedModule = module.get_identifier().get_name();
		if (!modulesToParse.contains(nameOfImportedModule)) {
			modulesToParse.add(nameOfImportedModule);
		}
	}

	/*
	 * ModuleName Renamings -> Import {cons("renamed-module")}
	 */

	/**
	 * Rule 849: cf(ModuleName) cf(LAYOUT?) cf(Renamings) -&gt; cf(Import)
	 * {cons("renamed-module"), definedAs(
	 * "ModuleName Renamings -&gt; Import {cons(\"renamed-module\")}")}<br>
	 */
	@Override
	protected void execute_Rule849_Position3(StackElement currentElement) {
		System.out
				.println(createMessageString(
						currentElement,
						"WARNING: Renamings are not supported by EDL. The use of renamings happen on own risk."));
		Vertex importVertex = createVertex(Import.VC,
				currentElement.getPosition());
		currentElement.setResult(importVertex);

		Module module = (Module) currentElement.getChild(0).getResult();
		createEdge(ImportsModule.EC, importVertex, module);

		// add imported module to be parsed
		String nameOfImportedModule = module.get_identifier().get_name();
		if (!modulesToParse.contains(nameOfImportedModule)) {
			modulesToParse.add(nameOfImportedModule);
		}

		// TODO check semantic of renamings
		createEdge(IsRenamingOf.EC, importVertex, currentElement.getChild(2)
				.getResult());
	}

	// #####################
	// grammar/sections/IslandSection
	// #####################

	/*
	 * IslandSection -> InitialSection {cons("island-section")}
	 */

	/**
	 * Rule 837: cf(IslandSection) -&gt; cf(InitialSection)
	 * {cons("island-section"), definedAs(
	 * "IslandSection -&gt; InitialSection {cons(\"island-section\")}")}<br>
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void execute_Rule837_Position1(StackElement currentElement) {
		if (isStartModule()) {
			List<Island> resultList = new ArrayList<Island>(4);
			for (Island island : (List<Island>) currentElement.getChild(0)
					.getResult()) {
				if (island != null) {
					resultList.add(island);
				}
			}
			currentElement.setResult(resultList);
		} else {
			System.out
					.println(createMessageString(
							currentElement,
							"WARNING: Island sections may only be defined in the start module. The following start module will be ignored:"));
		}
	}

	/*
	 * "island" "start" IslandEntry+ -> IslandSection {cons("island-start")}
	 */

	/**
	 * Rule 836: "island" cf(LAYOUT?) "start" cf(LAYOUT?) cf(IslandEntry+) -&gt;
	 * cf(IslandSection) {cons("island-start"), definedAs(
	 * "\"island\" \"start\" IslandEntry+ -&gt; IslandSection {cons(\"island-start\")}"
	 * )}<br>
	 * Rule 452: cf(IslandEntry) -&gt; cf(IslandEntry+)<br>
	 */
	@Override
	protected void execute_Rule836_Term4_Rule452_Position1(
			StackElement currentElement) {
		if (isStartModule()) {
			TemporaryVertex island = (TemporaryVertex) currentElement.getChild(
					0).getResult();
			Position oldPosition = positionsMap.get(island);
			Island islandStart = (Island) island.bless(IslandStart.VC);
			if (islandStart.is_isExclusive()) {
				if (exclusiveIslandStart != null) {
					String regExp = concatRegExp(
							exclusiveIslandStart.get_regExp(),
							islandStart.get_regExp());
					exclusiveIslandStart.set_regExp(regExp);
					islandStart.delete();
					currentElement.setResult(null);
				} else {
					exclusiveIslandStart = islandStart;
					positionsMap.put(islandStart, oldPosition);
					currentElement.setResult(islandStart);
				}
			} else {
				if (inclusiveIslandStart != null) {
					String regExp = concatRegExp(
							inclusiveIslandStart.get_regExp(),
							islandStart.get_regExp());
					inclusiveIslandStart.set_regExp(regExp);
					islandStart.delete();
					currentElement.setResult(null);
				} else {
					inclusiveIslandStart = islandStart;
					positionsMap.put(islandStart, oldPosition);
					currentElement.setResult(islandStart);
				}
			}
		}
	}

	private String concatRegExp(String firstRegExp, String secondRegExp) {
		String regExp = firstRegExp;
		if (!(regExp.startsWith("(") && regExp.endsWith(")"))) {
			regExp = "(" + regExp + ")";
		}
		regExp += "|(" + secondRegExp + ")";
		return regExp;
	}

	/**
	 * Rule 836: "island" cf(LAYOUT?) "start" cf(LAYOUT?) cf(IslandEntry+) -&gt;
	 * cf(IslandSection) {cons("island-start"), definedAs(
	 * "\"island\" \"start\" IslandEntry+ -&gt; IslandSection {cons(\"island-start\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule836_Position5(StackElement currentElement) {
		if (isStartModule()) {
			currentElement.setResult(currentElement.getChild(4).getResult());
		}
	}

	/*
	 * "island" "end" IslandEntry+ -> IslandSection {cons("island-end")}
	 */

	/**
	 * Rule 835: "island" cf(LAYOUT?) "end" cf(LAYOUT?) cf(IslandEntry+) -&gt;
	 * cf(IslandSection) {cons("island-end"), definedAs(
	 * "\"island\" \"end\" IslandEntry+ -&gt; IslandSection {cons(\"island-end\")}"
	 * )}<br>
	 * Rule 452: cf(IslandEntry) -&gt; cf(IslandEntry+)<br>
	 */
	@Override
	protected void execute_Rule835_Term4_Rule452_Position1(
			StackElement currentElement) {
		if (isStartModule()) {
			TemporaryVertex island = (TemporaryVertex) currentElement.getChild(
					0).getResult();
			Position oldPosition = positionsMap.get(island);
			Island islandEnd = (Island) island.bless(IslandEnd.VC);
			if (islandEnd.is_isExclusive()) {
				if (exclusiveIslandEnd != null) {
					String regExp = concatRegExp(
							exclusiveIslandEnd.get_regExp(),
							islandEnd.get_regExp());
					exclusiveIslandEnd.set_regExp(regExp);
					islandEnd.delete();
					currentElement.setResult(null);
				} else {
					exclusiveIslandEnd = islandEnd;
					positionsMap.put(islandEnd, oldPosition);
					currentElement.setResult(islandEnd);
				}
			} else {
				if (inclusiveIslandEnd != null) {
					String regExp = concatRegExp(
							inclusiveIslandEnd.get_regExp(),
							islandEnd.get_regExp());
					inclusiveIslandEnd.set_regExp(regExp);
					islandEnd.delete();
					currentElement.setResult(null);
				} else {
					inclusiveIslandEnd = islandEnd;
					positionsMap.put(islandEnd, oldPosition);
					currentElement.setResult(islandEnd);
				}
			}
		}
	}

	/**
	 * Rule 835: "island" cf(LAYOUT?) "end" cf(LAYOUT?) cf(IslandEntry+) -&gt;
	 * cf(IslandSection) {cons("island-end"), definedAs(
	 * "\"island\" \"end\" IslandEntry+ -&gt; IslandSection {cons(\"island-end\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule835_Position5(StackElement currentElement) {
		if (isStartModule()) {
			currentElement.setResult(currentElement.getChild(4).getResult());
		}
	}

	/*
	 * "exclusive" JavaRegExp -> IslandEntry {cons("exclusive")}
	 */

	/**
	 * Rule 842: "exclusive" lex(JavaRegExp) -&gt; lex(IslandEntry)
	 * {cons("exclusive"), definedAs(
	 * "\"exclusive\" JavaRegExp -&gt; IslandEntry {cons(\"exclusive\")}")}<br>
	 */
	@Override
	protected void execute_Rule842_Position2(StackElement currentElement) {
		if (isStartModule()) {
			TemporaryVertex vertex = createTemporaryVertex(currentElement
					.getPosition());
			vertex.setAttribute("isExclusive", true);
			String regExp = currentElement.getChild(1).getLexem().trim();
			try {
				java.util.regex.Pattern.compile(regExp);
			} catch (PatternSyntaxException e) {
				throw new GrammarException(createMessageString(
						currentElement.getChild(1), "\"" + regExp
								+ "\" is no valid regular expression."), e);
			}
			vertex.setAttribute("regExp", regExp);
			currentElement.setResult(vertex);
		}
	}

	/*
	 * "inclusive" JavaRegExp -> IslandEntry {cons("inclusive")}
	 */

	/**
	 * Rule 840: "inclusive" lex(JavaRegExp) -&gt; lex(IslandEntry)
	 * {cons("inclusive"), definedAs(
	 * "\"inclusive\" JavaRegExp -&gt; IslandEntry {cons(\"inclusive\")}")}<br>
	 */
	@Override
	protected void execute_Rule840_Position2(StackElement currentElement) {
		if (isStartModule()) {
			TemporaryVertex vertex = createTemporaryVertex(currentElement
					.getPosition());
			vertex.setAttribute("isExclusive", false);
			String regExp = currentElement.getChild(1).getLexem().trim();
			try {
				java.util.regex.Pattern.compile(regExp);
			} catch (PatternSyntaxException e) {
				throw new GrammarException(createMessageString(
						currentElement.getChild(1), "\"" + regExp
								+ "\" is no valid regular expression."), e);
			}
			vertex.setAttribute("regExp", regExp);
			currentElement.setResult(vertex);
		}
	}

	// #####################
	// grammar/sections/SchemaSection
	// #####################

	/*
	 * SchemaSection -> InitialSection {cons("schema-section")}
	 */

	/**
	 * Rule 807: cf(SchemaSection) -&gt; cf(InitialSection)
	 * {cons("schema-section"), definedAs(
	 * "SchemaSection -&gt; InitialSection {cons(\"schema-section\")}")}<br>
	 */
	@Override
	protected void execute_Rule807_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "schema" TypeName -> SchemaSection {cons("schema")}
	 */

	/**
	 * Rule 806: "schema" cf(LAYOUT?) cf(TypeName) -&gt; cf(SchemaSection)
	 * {cons("schema"),
	 * definedAs("\"schema\" TypeName  -&gt; SchemaSection {cons(\"schema\")}")}<br>
	 */
	@Override
	protected void execute_Rule806_Position3(StackElement currentElement) {
		de.uni_koblenz.edl.preprocessor.schema.section.Schema schema = (Schema) graph
				.getFirstVertex(de.uni_koblenz.edl.preprocessor.schema.section.Schema.VC);
		String newSchemaName = (String) currentElement.getChild(2).getResult();
		if (schema == null && isStartModule()) {
			schema = (Schema) createVertex(
					de.uni_koblenz.edl.preprocessor.schema.section.Schema.VC,
					currentElement.getPosition());
			schema.set_name(newSchemaName);
			currentElement.setResult(schema);
			if (targetSchema == null) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends SchemaImpl> schemaClass = (Class<? extends SchemaImpl>) Class
							.forName(newSchemaName);
					java.lang.reflect.Method instanceMethod = schemaClass
							.getMethod("instance");
					targetSchema = (de.uni_koblenz.jgralab.schema.Schema) instanceMethod
							.invoke(null);
				} catch (ClassNotFoundException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The implementation of the schema \""
											+ newSchemaName
											+ "\" could not be found."), e);
				} catch (NoSuchMethodException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The instance() method of the schema \""
											+ newSchemaName
											+ "\" could not be found."), e);
				} catch (SecurityException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The instance() method of the schema \""
											+ newSchemaName
											+ "\" could not be found."), e);
				} catch (IllegalAccessException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The instance() method of the schema \""
											+ newSchemaName
											+ "\" could not be called."), e);
				} catch (IllegalArgumentException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The instance() method of the schema \""
											+ newSchemaName
											+ "\" could not be called."), e);
				} catch (InvocationTargetException e) {
					throw new GrammarException(
							createMessageString(currentElement,
									"The instance() method of the schema \""
											+ newSchemaName
											+ "\" could not be called."), e);
				}
			}
		} else {
			System.out
					.println(createMessageString(
							currentElement,
							isStartModule() ? "WARNING: There already exists a schema declaration \""
									+ schema.get_name()
									+ "\". That is why the schema declaration \""
									+ newSchemaName + "\" will be ignored."
									: "WARNING: \""
											+ newSchemaName
											+ "\" will be ignored because it is not defined in the start module."));
		}
	}

	// #####################
	// grammar/sections/SymbolTablesSection
	// #####################

	/*
	 * SymbolTablesSection -> InitialSection {cons("symbol-tables-section")}
	 */

	/**
	 * Rule 1174: cf(SymbolTablesSection) -&gt; cf(InitialSection)
	 * {cons("symbol-tables-section"), definedAs(
	 * "SymbolTablesSection -&gt; InitialSection {cons(\"symbol-tables-section\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1174_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "symbol" "tables" STableDef+ -> SymbolTablesSection
	 * {cons("symbol-tables")}
	 */

	/**
	 * Rule 1173: "symbol" cf(LAYOUT?) "tables" cf(LAYOUT?) cf(STableDef+) -&gt;
	 * cf(SymbolTablesSection) {cons("symbol-tables"), definedAs(
	 * "\"symbol\" \"tables\" STableDef+ -&gt; SymbolTablesSection {cons(\"symbol-tables\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1173_Position0(StackElement currentElement) {
		// append all symbol table declarations of a module to one definition
		Set<Vertex> result = getFirstSectionOfCurrentModule(SymbolTables.VC);
		Vertex symbolTables = null;
		if (result == null || result.isEmpty()) {
			symbolTables = createVertex(SymbolTables.VC,
					currentElement.getPosition());
		} else {
			assert result.size() == 1;
			Iterator<Vertex> iterator = result.iterator();
			assert iterator.hasNext();
			if (iterator.hasNext()) {
				symbolTables = iterator.next();
			}
			assert !iterator.hasNext();
		}
		assert symbolTables != null;
		currentElement.setResult(symbolTables);
	}

	/**
	 * Rule 1173: "symbol" cf(LAYOUT?) "tables" cf(LAYOUT?) cf(STableDef+) -&gt;
	 * cf(SymbolTablesSection) {cons("symbol-tables"), definedAs(
	 * "\"symbol\" \"tables\" STableDef+ -&gt; SymbolTablesSection {cons(\"symbol-tables\")}"
	 * )}<br>
	 * Rule 759: cf(STableDef) -&gt; cf(STableDef+)<br>
	 */
	@Override
	protected void execute_Rule1173_Term4_Rule759_Position1(
			StackElement currentElement) {
		createEdge(IsDefinedIn.EC, currentElement.getChild(0).getResult(),
				currentElement.getParentApplicationOfDefinedRule().getResult());
	}

	/*
	 * STableName "<" STableElems ">" -> STableDef {cons("symbol-tables")}
	 */

	/**
	 * Rule 1172: cf(STableName) cf(LAYOUT?) "&lt;" cf(LAYOUT?) cf(STableElems)
	 * cf(LAYOUT?) "&gt;" -&gt; cf(STableDef) {cons("symbol-tables"), definedAs(
	 * "STableName \"&lt;\" STableElems \"&gt;\" -&gt; STableDef {cons(\"symbol-tables\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1172_Position7(StackElement currentElement) {
		String symbolTableName = currentElement.getChild(0).getLexem();
		try {
			SymbolTableDefinition symbolTable = (SymbolTableDefinition) symbolTableTable
					.declare(
							symbolTableName,
							createVertex(SymbolTableDefinition.VC,
									currentElement.getPosition()));
			currentElement.setResult(symbolTable);
			createEdge(IsNameOfSymbolTable.EC, currentElement.getChild(0)
					.getResult(), symbolTable);
			createEdge(IsElementIn.EC, currentElement.getChild(4).getResult(),
					symbolTable);
		} catch (SymbolTableException e) {
			throw new GrammarException(createMessageString(currentElement,
					"There already exists a symbol table with name \""
							+ symbolTableName + "\" in the current module."), e);
		}
	}

	/*
	 * STableName "<" PersistentSTableElems ">" ":" GraphElementClass ->
	 * STableDef {cons("symbol-tables")}
	 */

	/**
	 * Rule 1171: cf(STableName) cf(LAYOUT?) "&lt;" cf(LAYOUT?)
	 * cf(PersistentSTableElems) cf(LAYOUT?) "&gt;" cf(LAYOUT?) ":" cf(LAYOUT?)
	 * cf(GraphElementClass) -&gt; cf(STableDef) {cons("symbol-tables"),
	 * definedAs(
	 * "STableName \"&lt;\" PersistentSTableElems \"&gt;\" \":\" GraphElementClass -&gt; STableDef {cons(\"symbol-tables\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1171_Position11(StackElement currentElement) {
		String symbolTableName = currentElement.getChild(0).getLexem();
		try {
			SymbolTableDefinition symbolTable = (SymbolTableDefinition) symbolTableTable
					.declare(
							symbolTableName,
							createVertex(SymbolTableDefinition.VC,
									currentElement.getPosition()));
			currentElement.setResult(symbolTable);
			createEdge(IsNameOfSymbolTable.EC, currentElement.getChild(0)
					.getResult(), symbolTable);
			createEdge(IsElementIn.EC, currentElement.getChild(4).getResult(),
					symbolTable);
			Vertex vertex = (Vertex) currentElement.getChild(10).getResult();
			String nameOfVertexClass = currentElement.getChild(10).getLexem();
			checkVertexOrEdgeClass(currentElement.getChild(10), vertex,
					nameOfVertexClass, true);
			createEdge(IsPersistentVertexClassOf.EC, vertex, symbolTable);

			checkIncidenceTypesOfPersistentEdgeClasses(currentElement,
					symbolTableName, symbolTable, vertex);
		} catch (SymbolTableException e) {
			throw new GrammarException(createMessageString(currentElement,
					"There already exists a symbol table with name \""
							+ symbolTableName + "\" in the current module."), e);
		}
	}

	private void checkIncidenceTypesOfPersistentEdgeClasses(
			StackElement currentElement, String symbolTableName,
			SymbolTableDefinition symbolTable, Vertex vertex) {
		VertexClass persistentVertexClass = targetSchema
				.getAttributedElementClass(((GraphElementClass) vertex)
						.get_identifier().get_name());
		for (IsElementIn edge : symbolTable.getIsElementInIncidences()) {
			Element element = edge.getAlpha();
			VertexClass elementType = targetSchema
					.getAttributedElementClass(element
							.getFirstIsVertexClassOfIncidence().getAlpha()
							.get_identifier().get_name());
			IsPersistentEdgeClassOf ipeco = element
					.getFirstIsPersistentEdgeClassOfIncidence();
			EdgeClass persistentEdgeClass = targetSchema
					.getAttributedElementClass(ipeco.getAlpha()
							.get_identifier().get_name());
			if (persistentEdgeClass.isAbstract()) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"Symbol table \""
										+ symbolTableName
										+ "\": The persistent EdgeClass \""
										+ persistentEdgeClass
												.getQualifiedName()
										+ "\" of element type \""
										+ elementType.getQualifiedName()
										+ "\" is abstract and can not be instanciated to persist element in the graph."));
			}
			VertexClass possibleAlpha = persistentEdgeClass.getFrom()
					.getVertexClass();
			if (ipeco.is_isElementAlpha() ? possibleAlpha != elementType
					&& !elementType.isSubClassOf(possibleAlpha)
					: persistentVertexClass != possibleAlpha
							&& !persistentVertexClass
									.isSubClassOf(possibleAlpha)) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"Symbol table \""
										+ symbolTableName
										+ "\": The persistent EdgeClass \""
										+ persistentEdgeClass
												.getQualifiedName()
										+ "\" can only have vertices of type \""
										+ possibleAlpha
										+ "\" as alpha vertices. The defined alpha vertex type is \""
										+ (ipeco.is_isElementAlpha() ? elementType
												.getQualifiedName()
												: persistentVertexClass
														.getQualifiedName())
										+ "\"."));
			}
			VertexClass possibleOmega = persistentEdgeClass.getTo()
					.getVertexClass();
			if (!ipeco.is_isElementAlpha() ? possibleOmega != elementType
					&& !elementType.isSubClassOf(possibleOmega)
					: persistentVertexClass != possibleOmega
							&& !persistentVertexClass
									.isSubClassOf(possibleOmega)) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"Symbol table \""
										+ symbolTableName
										+ "\": The persistent EdgeClass \""
										+ persistentEdgeClass
												.getQualifiedName()
										+ "\" can only have vertices of type \""
										+ possibleOmega
										+ "\" as omega vertices. The defined omega vertex type is \""
										+ (!ipeco.is_isElementAlpha() ? elementType
												.getQualifiedName()
												: persistentVertexClass
														.getQualifiedName())
										+ "\"."));
			}
		}
	}

	/*
	 * JavaId -> STableName {cons("symbol-table-name")}
	 */

	/**
	 * Rule 1170: cf(JavaId) -&gt; cf(STableName) {cons("symbol-table-name"),
	 * definedAs("JavaId -&gt; STableName {cons(\"symbol-table-name\")}")}<br>
	 */
	@Override
	protected void execute_Rule1170_Position1(StackElement currentElement) {
		Identifier identifier = (Identifier) createVertex(Identifier.VC,
				currentElement.getPosition());
		identifier.set_name(currentElement.getChild(0).getLexem());
		currentElement.setResult(identifier);
	}

	/*
	 * {GraphElementClass ","}+ -> STableElems {cons("table-elements")}
	 */

	/**
	 * Rule 1167: cf({GraphElementClass ","}+) -&gt; cf(STableElems)
	 * {cons("table-elements"), definedAs(
	 * "{GraphElementClass \",\"}+ -&gt; STableElems {cons(\"table-elements\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1167_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/**
	 * Rule 1167: cf({GraphElementClass ","}+) -&gt; cf(STableElems)
	 * {cons("table-elements"), definedAs(
	 * "{GraphElementClass \",\"}+ -&gt; STableElems {cons(\"table-elements\")}"
	 * )}<br>
	 * Rule 745: cf(GraphElementClass) -&gt; cf({GraphElementClass ","}+)<br>
	 */
	@Override
	protected void execute_Rule1167_Term0_Rule745_Position1(
			StackElement currentElement) {
		Element element = (Element) createVertex(Element.VC, currentElement
				.getChild(0).getPosition());
		Vertex vertex = (Vertex) currentElement.getChild(0).getResult();
		String nameOfVertexClass = currentElement.getChild(0).getLexem();
		checkVertexOrEdgeClass(currentElement.getChild(0), vertex,
				nameOfVertexClass, true);
		createEdge(IsVertexClassOf.EC, vertex, element);
		currentElement.setResult(element);
	}

	private void checkVertexOrEdgeClass(StackElement currentElement,
			Vertex schemaElement, String nameOfGraphElementClass,
			boolean isVertexClassWished) {
		if (!schemaElement.isInstanceOf(GraphElementClass.VC)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							nameOfGraphElementClass
									+ (isVertexClassWished ? " is no VertexClass."
											+ " Elements of symbol tables must be an instance of a VertexClass."
											: " is no EdgeClass."
													+ " Persistent edges of elements of symbol tables must be an instance of an EdgeClass.")));
		}
		nameOfGraphElementClass = ((GraphElementClass) schemaElement)
				.get_identifier().get_name();
		if (!(isVertexClassWished ? targetSchema
				.getAttributedElementClass(nameOfGraphElementClass) instanceof VertexClass
				: targetSchema
						.getAttributedElementClass(nameOfGraphElementClass) instanceof EdgeClass)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							nameOfGraphElementClass
									+ (isVertexClassWished ? " is no VertexClass."
											+ " Elements of symbol tables must be an instance of a VertexClass."
											: " is no EdgeClass."
													+ " Persistent edges of elements of symbol tables must be an instance of an EdgeClass.")));
		}
	}

	/*
	 * {PersistentSTableElem ","}+ -> PersistentSTableElems
	 * {cons("pers-table-elements")}
	 */

	/**
	 * Rule 1166: cf({PersistentSTableElem ","}+) -&gt;
	 * cf(PersistentSTableElems) {cons("pers-table-elements"), definedAs(
	 * "{PersistentSTableElem \",\"}+ -&gt; PersistentSTableElems {cons(\"pers-table-elements\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1166_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/**
	 * Rule 1166: cf({PersistentSTableElem ","}+) -&gt;
	 * cf(PersistentSTableElems) {cons("pers-table-elements"), definedAs(
	 * "{PersistentSTableElem \",\"}+ -&gt; PersistentSTableElems {cons(\"pers-table-elements\")}"
	 * )}<br>
	 * Rule 738: cf(PersistentSTableElem) -&gt; cf({PersistentSTableElem ","}+)<br>
	 */
	@Override
	protected void execute_Rule1166_Term0_Rule738_Position1(
			StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * GraphElementClass "-->" GraphElementClass -> PersistentSTableElem
	 * {cons("pers-element-OUT")}
	 */

	/**
	 * Rule 1165: cf(GraphElementClass) cf(LAYOUT?) "--&gt;" cf(LAYOUT?)
	 * cf(GraphElementClass) -&gt; cf(PersistentSTableElem)
	 * {cons("pers-element-OUT"), definedAs(
	 * "GraphElementClass \"--&gt;\" GraphElementClass -&gt; PersistentSTableElem {cons(\"pers-element-OUT\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1165_Position5(StackElement currentElement) {
		Element element = (Element) createVertex(Element.VC,
				currentElement.getPosition());
		Vertex vertex = (Vertex) currentElement.getChild(0).getResult();
		String nameOfVertexClass = currentElement.getChild(0).getLexem();
		checkVertexOrEdgeClass(currentElement.getChild(0), vertex,
				nameOfVertexClass, true);
		createEdge(IsVertexClassOf.EC, vertex, element);

		vertex = (Vertex) currentElement.getChild(4).getResult();
		String nameOfEdgeClass = currentElement.getChild(4).getLexem();
		checkVertexOrEdgeClass(currentElement.getChild(4), vertex,
				nameOfEdgeClass, false);
		IsPersistentEdgeClassOf edge = (IsPersistentEdgeClassOf) createEdge(
				IsPersistentEdgeClassOf.EC, vertex, element);
		edge.set_isElementAlpha(true);
		currentElement.setResult(element);
	}

	/*
	 * GraphElementClass "<--" GraphElementClass -> PersistentSTableElem
	 * {cons("pers-element-IN")}
	 */

	/**
	 * Rule 1164: cf(GraphElementClass) cf(LAYOUT?) "&lt;--" cf(LAYOUT?)
	 * cf(GraphElementClass) -&gt; cf(PersistentSTableElem)
	 * {cons("pers-element-IN"), definedAs(
	 * "GraphElementClass \"&lt;--\" GraphElementClass -&gt; PersistentSTableElem {cons(\"pers-element-IN\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1164_Position5(StackElement currentElement) {
		Element element = (Element) createVertex(Element.VC,
				currentElement.getPosition());
		Vertex vertex = (Vertex) currentElement.getChild(0).getResult();
		String nameOfVertexClass = currentElement.getChild(0).getLexem();
		checkVertexOrEdgeClass(currentElement.getChild(0), vertex,
				nameOfVertexClass, true);
		createEdge(IsVertexClassOf.EC, vertex, element);

		vertex = (Vertex) currentElement.getChild(4).getResult();
		String nameOfEdgeClass = currentElement.getChild(4).getLexem();
		checkVertexOrEdgeClass(currentElement.getChild(4), vertex,
				nameOfEdgeClass, false);
		IsPersistentEdgeClassOf edge = (IsPersistentEdgeClassOf) createEdge(
				IsPersistentEdgeClassOf.EC, vertex, element);
		edge.set_isElementAlpha(false);
		currentElement.setResult(element);
	}

	// #####################
	// grammar/sections/UserCodeSection
	// #####################

	/*
	 * UserCodeSection -> InitialSection {cons("user-code-section")}
	 */

	/**
	 * Rule 820: cf(UserCodeSection) -&gt; cf(InitialSection)
	 * {cons("user-code-section"), definedAs(
	 * "UserCodeSection -&gt; InitialSection {cons(\"user-code-section\")}")}<br>
	 */
	@Override
	protected void execute_Rule820_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "user" "code" UserCode+ -> UserCodeSection {cons("user-code-sec")}
	 */

	/**
	 * Rule 819: "user" cf(LAYOUT?) "code" cf(LAYOUT?) cf(UserCode+) -&gt;
	 * cf(UserCodeSection) {cons("user-code-sec"), definedAs(
	 * "\"user\" \"code\" UserCode+ -&gt; UserCodeSection {cons(\"user-code-sec\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule819_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 819: "user" cf(LAYOUT?) "code" cf(LAYOUT?) cf(UserCode+) -&gt;
	 * cf(UserCodeSection) {cons("user-code-sec"), definedAs(
	 * "\"user\" \"code\" UserCode+ -&gt; UserCodeSection {cons(\"user-code-sec\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule819_Position5(StackElement currentElement) {
		@SuppressWarnings("unchecked")
		List<UserCode> userCodeContent = (List<UserCode>) currentElement
				.getChild(4).getResult();
		if (!userCodeContent.isEmpty()) {
			UserCode userCode = appendUserCode(userCodeContent);
			if (userCode == null) {
				return;
			}

			// append all usercode to the first UserCode vertex
			UserCodeSection userCodeSection = (UserCodeSection) graph
					.getFirstVertex(UserCodeSection.VC);
			if (userCodeSection == null) {
				userCodeSection = (UserCodeSection) createVertex(
						UserCodeSection.VC, currentElement.getPosition());
				currentElement.setResult(userCodeSection);
				createEdge(IsSectionOf.EC, userCodeSection, startModule);
			}

			if (userCodeSection.getDegree(EdgeDirection.IN) > 0) {
				List<UserCode> concatUserCode = new ArrayList<UserCode>();
				concatUserCode.add(userCodeSection
						.getFirstIsUserCodeOfIncidence().getAlpha());
				concatUserCode.add(userCode);
				userCode = appendUserCode(concatUserCode);
			}

			if (userCode.getDegree(IsUserCodeOf.EC) == 0) {
				createEdge(IsUserCodeOf.EC, userCode, userCodeSection);
			} else {
				assert userCode.getFirstIsUserCodeOfIncidence().getThat() == userCodeSection;
			}
		}
		leaveVisibilityStage();
	}

	private UserCode appendUserCode(List<UserCode> userCodeContent) {
		UserCode userCode = null;
		for (UserCode elem : userCodeContent) {
			if (elem.getDegree(EdgeDirection.IN) > 0) {
				if (userCode == null) {
					userCode = elem;
					continue;
				} else {
					Edge edge = elem.getFirstIncidence(EdgeDirection.IN);
					while (edge != null) {
						edge.setOmega(userCode);
						edge = elem.getFirstIncidence(EdgeDirection.IN);
					}
				}
			}
			deleteTree(elem);
		}
		return userCode;
	}

	// #####################
	// grammar/grammar/Grammar
	// #####################

	/*
	 * "(/)" -> Grammar {cons("empty-grammar")}
	 */

	/**
	 * Rule 1197: "(/)" -&gt; cf(Grammar) {cons("empty-grammar"),
	 * definedAs("\"(/)\" -&gt; Grammar {cons(\"empty-grammar\")}")}<br>
	 */
	@Override
	protected void execute_Rule1197_Position1(StackElement currentElement) {
		if (emptyGrammar == null) {
			emptyGrammar = createVertex(EmptyGrammar.VC,
					currentElement.getPosition());
		} else {
			positionsMap.put(emptyGrammar, currentElement.getPosition());
		}
		currentElement.setResult(emptyGrammar);
	}

	/*
	 * Grammar Grammar -> Grammar {assoc,cons("conc-grammars")}
	 */

	/**
	 * Rule 1196: cf(Grammar) cf(LAYOUT?) cf(Grammar) -&gt; cf(Grammar) {assoc,
	 * cons("conc-grammars"), definedAs(
	 * "Grammar Grammar -&gt; Grammar {assoc,cons(\"conc-grammars\")}")}<br>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void execute_Rule1196_Position3(StackElement currentElement) {
		Object firstResult = currentElement.getChild(0).getResult();
		Object secondResult = currentElement.getChild(2).getResult();
		if (firstResult instanceof List) {
			((List) firstResult).add(secondResult);
			currentElement.setResult(firstResult);
		} else {
			List result = new ArrayList();
			result.add(firstResult);
			result.add(secondResult);
			currentElement.setResult(result);
		}
	}

	/*
	 * "(" Grammar ")" -> Grammar {bracket}
	 */

	/**
	 * Rule 1195: "(" cf(LAYOUT?) cf(Grammar) cf(LAYOUT?) ")" -&gt; cf(Grammar)
	 * {bracket, definedAs("\"(\" Grammar \")\" -&gt; Grammar {bracket}")}<br>
	 */
	@Override
	protected void execute_Rule1195_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(2).getResult());
	}

	// #####################
	// grammar/grammar/Aliases
	// #####################

	/*
	 * "aliases" Alias* -> Grammar {cons("aliases")}
	 */

	/**
	 * Rule 897: "aliases" cf(LAYOUT?) cf(Alias*) -&gt; cf(Grammar)
	 * {cons("aliases"),
	 * definedAs("\"aliases\" Alias* -&gt; Grammar {cons(\"aliases\")}")}<br>
	 */
	@Override
	protected void execute_Rule897_Position0(StackElement currentElement) {
		currentElement.setResult(createVertex(Aliases.VC,
				currentElement.getPosition()));
		// TODO check semantic of aliases
	}

	/**
	 * Rule 897: "aliases" cf(LAYOUT?) cf(Alias*) -&gt; cf(Grammar)
	 * {cons("aliases"),
	 * definedAs("\"aliases\" Alias* -&gt; Grammar {cons(\"aliases\")}")}<br>
	 * Rule 540: cf(Alias) -&gt; cf(Alias+)<br>
	 */
	@Override
	protected void execute_Rule897_Term2_Rule540_Position1(
			StackElement currentElement) {
		createEdge(IsAliasOf.EC, currentElement.getChild(0).getResult(),
				currentElement.getParentApplicationOfDefinedRule().getResult());
	}

	/**
	 * Rule 897: "aliases" cf(LAYOUT?) cf(Alias*) -&gt; cf(Grammar)
	 * {cons("aliases"),
	 * definedAs("\"aliases\" Alias* -&gt; Grammar {cons(\"aliases\")}")}<br>
	 */
	@Override
	protected void execute_Rule897_Position3(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING Aliases are not supported by EDL. Use on own risk."));
	}

	/*
	 * Term "->" Term -> Alias {cons("alias")}
	 */

	/**
	 * Rule 896: cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) -&gt;
	 * cf(Alias) {cons("alias"),
	 * definedAs("Term \"-&gt;\" Term -&gt; Alias {cons(\"alias\")}")}<br>
	 */
	@Override
	protected void execute_Rule896_Position5(StackElement currentElement) {
		Vertex alias = createVertex(Alias.VC, currentElement.getPosition());
		currentElement.setResult(alias);
		createEdge(IsNewNameOf.EC, currentElement.getChild(0).getResult(),
				alias);
		createEdge(IsOriginalNameOf.EC, currentElement.getChild(4).getResult(),
				alias);
	}

	// #####################
	// grammar/grammar/ATerms
	// #####################

	/*
	 * IdCon -> AFun {cons("unquoted")}
	 */

	/**
	 * Rule 983: cf(IdCon) -&gt; cf(AFun) {cons("unquoted"),
	 * definedAs("IdCon -&gt; AFun {cons(\"unquoted\")}")}<br>
	 */
	@Override
	protected void execute_Rule983_Position1(StackElement currentElement) {
		if (currentElement.getChild(0).getLexem().trim()
				.equals(Rule.ATTRIBUTE_DEFINED_AS)) {
			throw new GrammarException(createMessageString(currentElement, "\""
					+ Rule.ATTRIBUTE_DEFINED_AS
					+ "\" is used from EDL internally."
					+ " Please choose another ATerm name."));
		}
	}

	// #####################
	// grammar/grammar/Attribute
	// #####################

	/*
	 * ATermAttribute -> Attribute {cons("term")}
	 */

	/**
	 * Rule 978: cf(ATermAttribute) -&gt; cf(Attribute) {cons("term"),
	 * definedAs("ATermAttribute -&gt; Attribute {cons(\"term\")}")}<br>
	 */
	@Override
	protected void execute_Rule978_Position1(StackElement currentElement) {
		ATerm aterm = (ATerm) createVertex(ATerm.VC,
				currentElement.getPosition());
		String atermString = currentElement.getChild(0).getLexem()
				.replaceAll("\\s+", "");
		aterm.set_value(atermString);
		currentElement.setResult(aterm);
	}

	/*
	 * "id" "(" ModuleName ")" -> Attribute {cons("id")}
	 */

	/**
	 * Rule 977: "id" cf(LAYOUT?) "(" cf(LAYOUT?) cf(ModuleName) cf(LAYOUT?) ")"
	 * -&gt; cf(Attribute) {cons("id"), definedAs(
	 * "\"id\" \"(\" ModuleName \")\" -&gt; Attribute {cons(\"id\")}")}<br>
	 */
	@Override
	protected void execute_Rule977_Position7(StackElement currentElement) {
		Vertex id = createVertex(Id.VC, currentElement.getPosition());
		createEdge(IsModuleOfId.EC, currentElement.getChild(4).getResult(), id);
		currentElement.setResult(id);
	}

	/*
	 * "reject" -> Attribute {cons("reject")}
	 */

	/**
	 * Rule 976: "reject" -&gt; cf(Attribute) {cons("reject"),
	 * definedAs("\"reject\" -&gt; Attribute {cons(\"reject\")}")}<br>
	 */
	@Override
	protected void execute_Rule976_Position1(StackElement currentElement) {
		Preference preference = (Preference) createVertex(Preference.VC,
				currentElement.getPosition());
		preference.set_value(PreferenceValue.REJECT);
		currentElement.setResult(preference);
	}

	/*
	 * "prefer" -> Attribute {cons("prefer\")}
	 */

	/**
	 * Rule 975: "prefer" -&gt; cf(Attribute) {cons("prefer"),
	 * definedAs("\"prefer\" -&gt; Attribute {cons(\"prefer\")}")}<br>
	 */
	@Override
	protected void execute_Rule975_Position1(StackElement currentElement) {
		Preference preference = (Preference) createVertex(Preference.VC,
				currentElement.getPosition());
		preference.set_value(PreferenceValue.PREFER);
		currentElement.setResult(preference);
	}

	/*
	 * "avoid" -> Attribute {cons("avoid")}
	 */

	/**
	 * Rule 974: "avoid" -&gt; cf(Attribute) {cons("avoid"),
	 * definedAs("\"avoid\" -&gt; Attribute {cons(\"avoid\")}")}<br>
	 */
	@Override
	protected void execute_Rule974_Position1(StackElement currentElement) {
		Preference preference = (Preference) createVertex(Preference.VC,
				currentElement.getPosition());
		preference.set_value(PreferenceValue.AVOID);
		currentElement.setResult(preference);
	}

	/*
	 * "bracket" -> Attribute {cons("bracket")}
	 */

	/**
	 * Rule 973: "bracket" -&gt; cf(Attribute) {cons("bracket"),
	 * definedAs("\"bracket\" -&gt; Attribute {cons(\"bracket\")}")}<br>
	 */
	@Override
	protected void execute_Rule973_Position1(StackElement currentElement) {
		currentElement.setResult(createVertex(Bracket.VC,
				currentElement.getPosition()));
	}

	/*
	 * Associativity -> Attribute {cons("assoc")}
	 */

	/**
	 * Rule 972: cf(Associativity) -&gt; cf(Attribute) {cons("assoc"),
	 * definedAs("Associativity -&gt; Attribute {cons(\"assoc\")}")}<br>
	 */
	@Override
	protected void execute_Rule972_Position1(StackElement currentElement) {
		String assoc = currentElement.getChild(0).getLexem().trim()
				.toUpperCase().replace('-', '_');
		Associativity associativity = (Associativity) createVertex(
				Associativity.VC, currentElement.getPosition());
		associativity.set_value(AssociativityValue.valueOf(assoc));
		currentElement.setResult(associativity);
	}

	// #####################
	// grammar/grammar/Pattern
	// #####################

	/*
	 * ("@Before"|("@Symboltable" "{" AnnotatedTables "}")) Pattern -> Pattern
	 * {cons("annotated-patter")}
	 */

	/**
	 * Rule 916: cf("@Before" | ("@Symboltable" "{" AnnotatedTables "}"))
	 * cf(LAYOUT?) cf(Pattern) -&gt; cf(Pattern) {cons("annotated-patter"),
	 * definedAs(
	 * "(\"@Before\"|(\"@Symboltable\" \"{\" AnnotatedTables \"}\")) Pattern -&gt; Pattern {cons(\"annotated-patter\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule916_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("executeBefore", false);
		currentElement.setValueOfTemporaryVariable("symbolTableSet",
				new HashSet<SymbolTableDefinition>());
	}

	/**
	 * Rule 916: cf("@Before" | ("@Symboltable" "{" AnnotatedTables "}"))
	 * cf(LAYOUT?) cf(Pattern) -&gt; cf(Pattern) {cons("annotated-patter"),
	 * definedAs(
	 * "(\"@Before\"|(\"@Symboltable\" \"{\" AnnotatedTables \"}\")) Pattern -&gt; Pattern {cons(\"annotated-patter\")}"
	 * )}<br>
	 * Rule 566: "@Before" -&gt; cf("@Before" | ("@Symboltable" "{"
	 * AnnotatedTables "}"))<br>
	 */
	@Override
	protected void execute_Rule916_Term0_Rule566_Position1(
			StackElement currentElement) {
		isABeforePattern = true;
		currentElement.setValueOfTemporaryVariable("executeBefore", true);
	}

	/**
	 * Rule 916: cf("@Before" | ("@Symboltable" "{" AnnotatedTables "}"))
	 * cf(LAYOUT?) cf(Pattern) -&gt; cf(Pattern) {cons("annotated-patter"),
	 * definedAs(
	 * "(\"@Before\"|(\"@Symboltable\" \"{\" AnnotatedTables \"}\")) Pattern -&gt; Pattern {cons(\"annotated-patter\")}"
	 * )}<br>
	 * Rule 565: cf(("@Symboltable" "{" AnnotatedTables "}")) -&gt; cf("@Before"
	 * | ("@Symboltable" "{" AnnotatedTables "}"))<br>
	 * Rule 564: "@Symboltable" cf(LAYOUT?) "{" cf(LAYOUT?) cf(AnnotatedTables)
	 * cf(LAYOUT?) "}" -&gt; cf(("@Symboltable" "{" AnnotatedTables "}"))<br>
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute_Rule916_Term0_Rule565_Term0_Rule564_Position7(
			StackElement currentElement) {
		Set<SymbolTableDefinition> symbolTableList = (Set<SymbolTableDefinition>) currentElement
				.getValueOfTemporaryVariable("symbolTableSet");
		symbolTableList
				.addAll((Collection<SymbolTableDefinition>) currentElement
						.getChild(4).getResult());
		currentElement.setValueOfTemporaryVariable("symbolTableSet",
				symbolTableList);
	}

	/**
	 * Rule 916: cf("@Before" | ("@Symboltable" "{" AnnotatedTables "}"))
	 * cf(LAYOUT?) cf(Pattern) -&gt; cf(Pattern) {cons("annotated-patter"),
	 * definedAs(
	 * "(\"@Before\"|(\"@Symboltable\" \"{\" AnnotatedTables \"}\")) Pattern -&gt; Pattern {cons(\"annotated-patter\")}"
	 * )}<br>
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute_Rule916_Position3(StackElement currentElement) {
		boolean executeBefore = (Boolean) currentElement
				.getValueOfTemporaryVariable("executeBefore");
		Set<SymbolTableDefinition> symbolTableList = (Set<SymbolTableDefinition>) currentElement
				.getValueOfTemporaryVariable("symbolTableSet");

		Pattern pattern = (Pattern) currentElement.getChild(2).getResult();
		pattern.set_executeBefore(executeBefore);
		for (SymbolTableDefinition symbolTable : pattern.get_symbolTable()) {
			symbolTableList.remove(symbolTable);
		}
		createEdge(IsAnnotatedSymbolTableOf.EC, symbolTableList, pattern);
		currentElement.setResult(pattern);
		positionsMap.put(pattern, currentElement.getPosition());
		if (executeBefore) {
			isABeforePattern = false;
		}
	}

	/*
	 * "pattern" PatternTerm* "->" PatternTerm SemanticAction+ -> Pattern
	 * {cons("pattern")}
	 */

	/**
	 * Rule 915: "pattern" cf(LAYOUT?) cf(PatternTerm*) cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {cons("pattern"), definedAs(
	 * "\"pattern\" PatternTerm* \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {cons(\"pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule915_Position0(StackElement currentElement) {
		enterVisibilityStage();
		tempVarTable.push();
		Pattern pattern = (Pattern) createVertex(Pattern.VC,
				currentElement.getPosition());
		pattern.set_executeBefore(false);
		currentElement.setResult(pattern);
		currentPattern = pattern;
		areSemanticActionsAllowed = false;
	}

	/**
	 * Rule 915: "pattern" cf(LAYOUT?) cf(PatternTerm*) cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {cons("pattern"), definedAs(
	 * "\"pattern\" PatternTerm* \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {cons(\"pattern\")}"
	 * )}<br>
	 * Rule 563: cf(PatternTerm) -&gt; cf(PatternTerm+)<br>
	 */
	@Override
	protected void execute_Rule915_Term2_Rule563_Position1(
			StackElement currentElement) {
		PatternTerm term = (PatternTerm) currentElement.getChild(0).getResult();
		nextTermInVisibilityStage(term);
		createEdge(IsBodyTermOfPattern.EC, term, currentElement
				.getParentApplicationOfDefinedRule().getResult());
	}

	/**
	 * Rule 915: "pattern" cf(LAYOUT?) cf(PatternTerm*) cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {cons("pattern"), definedAs(
	 * "\"pattern\" PatternTerm* \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {cons(\"pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule915_Position5(StackElement currentElement) {
		isInHeadOfPattern = true;
	}

	/**
	 * Rule 915: "pattern" cf(LAYOUT?) cf(PatternTerm*) cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {cons("pattern"), definedAs(
	 * "\"pattern\" PatternTerm* \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {cons(\"pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule915_Position7(StackElement currentElement) {
		createEdge(IsHeadTermOfPattern.EC, currentElement.getChild(6)
				.getResult(), currentElement.getResult());
		areSemanticActionsAllowed = true;
		isInHeadOfPattern = false;
	}

	/**
	 * Rule 915: "pattern" cf(LAYOUT?) cf(PatternTerm*) cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {cons("pattern"), definedAs(
	 * "\"pattern\" PatternTerm* \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {cons(\"pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule915_Position9(StackElement currentElement) {
		List<SemanticAction> semanticAction = appendAllSemanticActions(currentElement
				.getChild(8).getResult());
		createEdge(IsFollowingSemanticActionOf.EC, semanticAction,
				currentElement.getResult());
		leaveVisibilityStage();
		tempVarTable.pop();
		headVariable = null;
		currentPattern = null;
	}

	/*
	 * Term -> PatternTerm
	 */

	/**
	 * Rule 914: cf(Term) -&gt; cf(PatternTerm)
	 * {definedAs("Term -&gt; PatternTerm")}<br>
	 */
	@Override
	protected void execute_Rule914_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "_" -> PatternTerm {cons("wildcard\")}
	 */

	/**
	 * Rule 913: "_" -&gt; cf(PatternTerm) {cons("wildcard"),
	 * definedAs("\"_\" -&gt; PatternTerm {cons(\"wildcard\")}")}<br>
	 */
	@Override
	protected void execute_Rule913_Position1(StackElement currentElement) {
		Wildcard wildcard = (Wildcard) createVertex(Wildcard.VC,
				currentElement.getPosition());
		currentElement.setResult(wildcard);
	}

	/*
	 * PatternTerm "(" Multiplicity ")" -> PatternTerm
	 * {cons("single-multiplicity")}
	 */

	/**
	 * Rule 912: cf(PatternTerm) cf(LAYOUT?) "(" cf(LAYOUT?) cf(Multiplicity)
	 * cf(LAYOUT?) ")" -&gt; cf(PatternTerm) {cons("single-multiplicity"),
	 * definedAs(
	 * "PatternTerm \"(\" Multiplicity \")\" -&gt; PatternTerm {cons(\"single-multiplicity\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule912_Position7(StackElement currentElement) {
		Vertex patternTerm = (Vertex) currentElement.getChild(0).getResult();
		currentElement.setResult(patternTerm);
		int maxValue = (Integer) currentElement.getChild(4).getResult();
		if (maxValue != 1) {
			Multiplicity multiplicity = (Multiplicity) createVertex(
					Multiplicity.VC, currentElement.getPosition());
			if (maxValue == 0) {
				System.out.println(createMessageString(currentElement,
						"WARNING: A multiplicity of exactly 0 was detected."));
			}
			if (isInHeadOfPattern) {
				throw new GrammarException(
						createMessageString(currentElement,
								"The head of a pattern must consist of excatly one term."));
			}
			multiplicity.set_max(maxValue);
			multiplicity.set_min(maxValue == Integer.MAX_VALUE ? 0 : maxValue);

			createEdge(IsMultiplicityOf.EC, multiplicity, patternTerm);
		}
	}

	/*
	 * PatternTerm "(" Multiplicity ".." Multiplicity ")" -> PatternTerm
	 * {cons("min-max-multiplicity")}
	 */

	/**
	 * Rule 911: cf(PatternTerm) cf(LAYOUT?) "(" cf(LAYOUT?) cf(Multiplicity)
	 * cf(LAYOUT?) ".." cf(LAYOUT?) cf(Multiplicity) cf(LAYOUT?) ")" -&gt;
	 * cf(PatternTerm) {cons("min-max-multiplicity"), definedAs(
	 * "PatternTerm \"(\" Multiplicity \"..\" Multiplicity \")\" -&gt; PatternTerm {cons(\"min-max-multiplicity\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule911_Position11(StackElement currentElement) {
		Vertex patternTerm = (Vertex) currentElement.getChild(0).getResult();
		currentElement.setResult(patternTerm);
		Integer minValue = (Integer) currentElement.getChild(4).getResult();
		Integer maxValue = (Integer) currentElement.getChild(8).getResult();
		if (minValue != 1 || maxValue != 1) {
			Multiplicity multiplicity = (Multiplicity) createVertex(
					Multiplicity.VC, currentElement.getPosition());
			multiplicity.set_min(minValue);
			multiplicity.set_max(maxValue);
			if (multiplicity.get_max() == 0) {
				System.out.println(createMessageString(currentElement,
						"WARNING: A multiplicity of exactly 0 was detected."));
			}
			if (isInHeadOfPattern) {
				throw new GrammarException(
						createMessageString(currentElement,
								"The head of a pattern must consist of excatly one term."));
			}

			if (multiplicity.get_min() > multiplicity.get_max()) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"The maximum multiplicity "
										+ multiplicity.get_max()
										+ " must not be smaller than the minimum multiplicity "
										+ multiplicity.get_min() + "."));
			}
			createEdge(IsMultiplicityOf.EC, multiplicity, patternTerm);
		}
	}

	/*
	 * NatCon -> Multiplicity
	 */

	/**
	 * Rule 910: cf(NatCon) -&gt; cf(Multiplicity)
	 * {definedAs("NatCon -&gt; Multiplicity")}<br>
	 */
	@Override
	protected void execute_Rule910_Position1(StackElement currentElement) {
		currentElement.setResult(Integer.parseInt(currentElement.getChild(0)
				.getLexem()));
	}

	/*
	 * "*" -> Multiplicity {cons("star")}
	 */

	/**
	 * Rule 909: "*" -&gt; cf(Multiplicity) {cons("star"),
	 * definedAs("\"*\" -&gt; Multiplicity {cons(\"star\")}")}<br>
	 */
	@Override
	protected void execute_Rule909_Position1(StackElement currentElement) {
		currentElement.setResult(Integer.MAX_VALUE);
	}

	// #####################
	// grammar/grammar/Production
	// #####################

	/*
	 * "@Symboltable" "{" AnnotatedTables "}" Production -> Production
	 * {cons("symtable-anno-prod")}
	 */

	/**
	 * Rule 924: "@Symboltable" cf(LAYOUT?) "{" cf(LAYOUT?) cf(AnnotatedTables)
	 * cf(LAYOUT?) "}" cf(LAYOUT?) cf(Production) -&gt; cf(Production)
	 * {cons("symtable-anno-prod"), definedAs(
	 * "\"@Symboltable\" \"{\" AnnotatedTables \"}\" Production -&gt; Production {cons(\"symtable-anno-prod\")}"
	 * )}<br>
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void execute_Rule924_Position9(StackElement currentElement) {
		Production production = (Production) currentElement.getChild(8)
				.getResult();
		Set<SymbolTableDefinition> symbolTables = new HashSet<SymbolTableDefinition>();
		symbolTables.addAll((List<SymbolTableDefinition>) currentElement
				.getChild(4).getResult());
		for (IsAnnotatedSymbolTableOf iasto : production
				.getIsAnnotatedSymbolTableOfIncidences()) {
			symbolTables.remove(iasto.getAlpha());
		}
		for (SymbolTableDefinition std : symbolTables) {
			createEdge(IsAnnotatedSymbolTableOf.EC, std, production);
		}
		currentElement.setResult(production);
	}

	/*
	 * "rule" Terms "->" Term Attributes SemanticAction* -> Production
	 * {cons("prod")}
	 */

	/**
	 * Rule 923: "rule" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?) cf(SemanticAction*) -&gt;
	 * cf(Production) {cons("prod"), definedAs(
	 * "\"rule\" Terms \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {cons(\"prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule923_Position0(StackElement currentElement) {
		enterVisibilityStage();
		tempVarTable.push();
		isInBodyOfRule = true;
	}

	/**
	 * Rule 923: "rule" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?) cf(SemanticAction*) -&gt;
	 * cf(Production) {cons("prod"), definedAs(
	 * "\"rule\" Terms \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {cons(\"prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule923_Position5(StackElement currentElement) {
		areSemanticActionsAllowed = false;
		isInBodyOfRule = false;
		isInHeadOfRule = true;
	}

	/**
	 * Rule 923: "rule" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?) cf(SemanticAction*) -&gt;
	 * cf(Production) {cons("prod"), definedAs(
	 * "\"rule\" Terms \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {cons(\"prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule923_Position7(StackElement currentElement) {
		areSemanticActionsAllowed = true;
		isInHeadOfRule = false;
	}

	/**
	 * Rule 923: "rule" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?) cf(SemanticAction*) -&gt;
	 * cf(Production) {cons("prod"), definedAs(
	 * "\"rule\" Terms \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {cons(\"prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule923_Position11(StackElement currentElement) {
		Production production = (Production) createVertex(Production.VC,
				currentElement.getPosition());
		currentElement.setResult(production);
		createEdge(IsBodyTermOfProduction.EC, currentElement.getChild(2)
				.getResult(), production);

		Object head = currentElement.getChild(6).getResult();
		createEdge(IsHeadTermOfProduction.EC, head, production);
		if (headVariable != null) {
			createEdge(IsReferencedHead.EC, headVariable, head);
		}
		createEdge(IsAttributeOf.EC, currentElement.getChild(8).getResult(),
				production);
		List<SemanticAction> list = appendAllSemanticActions(currentElement
				.getChild(10).getResult());
		createEdge(IsFollowingSemanticActionOf.EC, list, production);
		leaveVisibilityStage();
		tempVarTable.pop();
		headVariable = null;
	}

	/*
	 * Terms "->" Term Attributes -> OldProduction {cons("prod")}
	 */

	/**
	 * Rule 922: cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {cons("prod"), definedAs(
	 * "Terms \"-&gt;\" Term Attributes -&gt; OldProduction {cons(\"prod\")}")}<br>
	 */
	@Override
	protected void execute_Rule922_Position0(StackElement currentElement) {
		areSemanticActionsAllowed = false;
		enterVisibilityStage();
	}

	/**
	 * Rule 922: cf(Terms) cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {cons("prod"), definedAs(
	 * "Terms \"-&gt;\" Term Attributes -&gt; OldProduction {cons(\"prod\")}")}<br>
	 */
	@Override
	protected void execute_Rule922_Position7(StackElement currentElement) {
		Production production = (Production) createVertex(Production.VC,
				currentElement.getPosition());
		currentElement.setResult(production);
		createEdge(IsBodyTermOfProduction.EC, currentElement.getChild(0)
				.getResult(), production);
		createEdge(IsHeadTermOfProduction.EC, currentElement.getChild(4)
				.getResult(), production);
		createEdge(IsAttributeOf.EC, currentElement.getChild(6).getResult(),
				production);
		leaveVisibilityStage();
		areSemanticActionsAllowed = true;
	}

	/*
	 * {JavaId ","}+ -> AnnotatedTables {cons("annotated-symbol-tables")}
	 */

	/**
	 * Rule 921: cf({JavaId ","}+) -&gt; cf(AnnotatedTables)
	 * {cons("annotated-symbol-tables"), definedAs(
	 * "{JavaId \",\"}+ -&gt; AnnotatedTables {cons(\"annotated-symbol-tables\")}"
	 * )}<br>
	 * Rule 580: cf(JavaId) -&gt; cf({JavaId ","}+)<br>
	 */
	@Override
	protected void execute_Rule921_Term0_Rule580_Position1(
			StackElement currentElement) {
		Vertex symbolTableDefinition = symbolTableTable.use(currentElement
				.getChild(0).getLexem());
		if (symbolTableDefinition == null) {
			throw new GrammarException(createMessageString(
					currentElement.getChild(0),
					"The following symbol table is used but never defined:"));
		}
		positionsMap.put(symbolTableDefinition, currentElement.getChild(0)
				.getPosition());
		currentElement.setResult(symbolTableDefinition);
	}

	/**
	 * Rule 921: cf({JavaId ","}+) -&gt; cf(AnnotatedTables)
	 * {cons("annotated-symbol-tables"), definedAs(
	 * "{JavaId \",\"}+ -&gt; AnnotatedTables {cons(\"annotated-symbol-tables\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule921_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "{" {Attribute ","}* "}" -> Attributes {cons("attrs")}
	 */

	/**
	 * Rule 920: "{" cf(LAYOUT?) cf({Attribute ","}*) cf(LAYOUT?) "}" -&gt;
	 * cf(Attributes) {cons("attrs"), definedAs(
	 * "\"{\" {Attribute \",\"}* \"}\" -&gt; Attributes {cons(\"attrs\")}")}<br>
	 * Rule 573: cf(Attribute) -&gt; cf({Attribute ","}+)<br>
	 */
	@Override
	protected void execute_Rule920_Term2_Rule573_Position1(
			StackElement currentElement) {
		Object result = currentElement.getChild(0).getResult();
		currentElement.setResult(result);
	}

	/**
	 * Rule 920: "{" cf(LAYOUT?) cf({Attribute ","}*) cf(LAYOUT?) "}" -&gt;
	 * cf(Attributes) {cons("attrs"), definedAs(
	 * "\"{\" {Attribute \",\"}* \"}\" -&gt; Attributes {cons(\"attrs\")}")}<br>
	 */
	@Override
	protected void execute_Rule920_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(2).getResult());
	}

	/*
	 * -> Attributes {cons("no-attrs")}
	 */

	/**
	 * Rule 919: -&gt; cf(Attributes) {cons("no-attrs"),
	 * definedAs("-&gt; Attributes {cons(\"no-attrs\")}")}<br>
	 */
	@Override
	protected void execute_Rule919_Position0(StackElement currentElement) {
		currentElement.setResult(new ArrayList<Attribute>());
	}

	// #####################
	// grammar/grammar/Priorities
	// #####################

	/*
	 * "priorities" Priorities -> Grammar {cons("priorities")}
	 */

	/**
	 * Rule 880: "priorities" cf(LAYOUT?) cf(Priorities) -&gt; cf(Grammar)
	 * {cons("priorities"), definedAs(
	 * "\"priorities\" Priorities -&gt; Grammar {cons(\"priorities\")}")}<br>
	 */
	@Override
	protected void execute_Rule880_Position3(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING: Check if you did not forget to add \"lexcial\" or \"context-free\""
						+ " to the following priorities block."));
		Priorities priorities = (Priorities) createVertex(Priorities.VC,
				currentElement.getPosition());
		priorities.set_type(GrammarType.KERNEL);
		createEdge(IsPriorityOf.EC, currentElement.getChild(2).getResult(),
				priorities);
		currentElement.setResult(priorities);
	}

	/*
	 * "lexical" "priorities" Priorities -> Grammar {cons("lexical-priorities")}
	 */

	/**
	 * Rule 879: "lexical" cf(LAYOUT?) "priorities" cf(LAYOUT?) cf(Priorities)
	 * -&gt; cf(Grammar) {cons("lexical-priorities"), definedAs(
	 * "\"lexical\" \"priorities\" Priorities -&gt; Grammar {cons(\"lexical-priorities\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule879_Position5(StackElement currentElement) {
		Priorities priorities = (Priorities) createVertex(Priorities.VC,
				currentElement.getPosition());
		priorities.set_type(GrammarType.LEXICAL);
		createEdge(IsPriorityOf.EC, currentElement.getChild(4).getResult(),
				priorities);
		currentElement.setResult(priorities);
	}

	/*
	 * "context-free" "priorities" Priorities -> Grammar
	 * {cons("context-free-priorities")}
	 */

	/**
	 * Rule 878: "context-free" cf(LAYOUT?) "priorities" cf(LAYOUT?)
	 * cf(Priorities) -&gt; cf(Grammar) {cons("context-free-priorities"),
	 * definedAs(
	 * "\"context-free\" \"priorities\" Priorities -&gt; Grammar {cons(\"context-free-priorities\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule878_Position0(StackElement currentElement) {
		isContextFree = true;
	}

	/**
	 * Rule 878: "context-free" cf(LAYOUT?) "priorities" cf(LAYOUT?)
	 * cf(Priorities) -&gt; cf(Grammar) {cons("context-free-priorities"),
	 * definedAs(
	 * "\"context-free\" \"priorities\" Priorities -&gt; Grammar {cons(\"context-free-priorities\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule878_Position5(StackElement currentElement) {
		Priorities priorities = (Priorities) createVertex(Priorities.VC,
				currentElement.getPosition());
		priorities.set_type(GrammarType.CONTEXT_FREE);
		createEdge(IsPriorityOf.EC, currentElement.getChild(4).getResult(),
				priorities);
		currentElement.setResult(priorities);
		isContextFree = false;
	}

	/*
	 * {Priority ","}* -> Priorities
	 */

	/**
	 * Rule 877: cf({Priority ","}*) -&gt; cf(Priorities)
	 * {definedAs("{Priority \",\"}* -&gt; Priorities")}<br>
	 * Rule 517: cf(Priority) -&gt; cf({Priority ","}+)<br>
	 */
	@Override
	protected void execute_Rule877_Term0_Rule517_Position1(
			StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/**
	 * Rule 877: cf({Priority ","}*) -&gt; cf(Priorities)
	 * {definedAs("{Priority \",\"}* -&gt; Priorities")}<br>
	 */
	@Override
	protected void execute_Rule877_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * "<" {NatCon ","}+ ">" -> ArgumentIndicator {cons("default")}
	 */

	/**
	 * Rule 869: "&lt;" cf(LAYOUT?) cf({NatCon ","}+) cf(LAYOUT?) "&gt;" -&gt;
	 * cf(ArgumentIndicator) {cons("default"), definedAs(
	 * "\"&lt;\" {NatCon \",\"}+ \"&gt;\" -&gt; ArgumentIndicator {cons(\"default\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule869_Position0(StackElement currentElement) {
		List<Integer> list = JGraLab.vector();
		currentElement.setValueOfTemporaryVariable("list", list);
	}

	/**
	 * Rule 869: "&lt;" cf(LAYOUT?) cf({NatCon ","}+) cf(LAYOUT?) "&gt;" -&gt;
	 * cf(ArgumentIndicator) {cons("default"), definedAs(
	 * "\"&lt;\" {NatCon \",\"}+ \"&gt;\" -&gt; ArgumentIndicator {cons(\"default\")}"
	 * )}<br>
	 * Rule 496: cf(NatCon) -&gt; cf({NatCon ","}+)<br>
	 */
	@Override
	protected void execute_Rule869_Term2_Rule496_Position1(
			StackElement currentElement) {
		@SuppressWarnings("unchecked")
		PVector<Integer> list = (PVector<Integer>) currentElement
				.getValueOfTemporaryVariable("list");
		currentElement.setValueOfTemporaryVariable("list", list.plus(Integer
				.parseInt(currentElement.getChild(0).getLexem())));
	}

	/**
	 * Rule 869: "&lt;" cf(LAYOUT?) cf({NatCon ","}+) cf(LAYOUT?) "&gt;" -&gt;
	 * cf(ArgumentIndicator) {cons("default"), definedAs(
	 * "\"&lt;\" {NatCon \",\"}+ \"&gt;\" -&gt; ArgumentIndicator {cons(\"default\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule869_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement
				.getValueOfTemporaryVariable("list"));
	}

	/*
	 * OldProduction -> Group {cons("simple-group")}
	 */

	/**
	 * Rule 874: cf(OldProduction) -&gt; cf(Group) {cons("simple-group"),
	 * definedAs("OldProduction -&gt; Group {cons(\"simple-group\")}")}<br>
	 */
	@Override
	protected void execute_Rule874_Position1(StackElement currentElement) {
		Vertex group = createVertex(Group.VC, currentElement.getPosition());
		createEdge(IsProductionOfGroup.EC, currentElement.getChild(0)
				.getResult(), group);
		currentElement.setResult(group);
	}

	/*
	 * "{" OldProduction* "}" -> Group {cons("prods-group")}
	 */

	/**
	 * Rule 873: "{" cf(LAYOUT?) cf(OldProduction*) cf(LAYOUT?) "}" -&gt;
	 * cf(Group) {cons("prods-group"), definedAs(
	 * "\"{\" OldProduction* \"}\" -&gt; Group {cons(\"prods-group\")}")}<br>
	 */
	@Override
	protected void execute_Rule873_Position5(StackElement currentElement) {
		Vertex group = createVertex(Group.VC, currentElement.getPosition());
		createEdge(IsProductionOfGroup.EC, currentElement.getChild(2)
				.getResult(), group);
		currentElement.setResult(group);
	}

	/*
	 * "{" Associativity ":" OldProduction* "}" -> Group {cons("assoc-group")}
	 */

	/**
	 * Rule 872: "{" cf(LAYOUT?) cf(Associativity) cf(LAYOUT?) ":" cf(LAYOUT?)
	 * cf(OldProduction*) cf(LAYOUT?) "}" -&gt; cf(Group) {cons("assoc-group"),
	 * definedAs(
	 * "\"{\" Associativity \":\" OldProduction* \"}\" -&gt; Group {cons(\"assoc-group\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule872_Position9(StackElement currentElement) {
		Group group = (Group) createVertex(Group.VC,
				currentElement.getPosition());
		String assoc = currentElement.getChild(2).getLexem().trim()
				.toUpperCase().replace('-', '_');
		group.set_assoc(AssociativityValue.valueOf(assoc));
		createEdge(IsProductionOfGroup.EC, currentElement.getChild(6)
				.getResult(), group);
		currentElement.setResult(group);
	}

	/*
	 * Group "." -&gt; Group {non-assoc,cons("non-transitive")}
	 */

	/**
	 * Rule 871: cf(Group) cf(LAYOUT?) "." -&gt; cf(Group) {non-assoc,
	 * cons("non-transitive"), definedAs(
	 * "Group \".\" -&gt; Group {non-assoc,cons(\"non-transitive\")}")}<br>
	 */
	@Override
	protected void execute_Rule871_Position3(StackElement currentElement) {
		Group group = (Group) currentElement.getChild(0).getResult();
		isTransitiveGroup.mark(group);
		currentElement.setResult(group);
	}

	/*
	 * Group ArgumentIndicator -> Group {non-assoc,cons("with-arguments")}
	 */

	/**
	 * Rule 870: cf(Group) cf(LAYOUT?) cf(ArgumentIndicator) -&gt; cf(Group)
	 * {non-assoc, cons("with-arguments"), definedAs(
	 * "Group ArgumentIndicator -&gt; Group {non-assoc,cons(\"with-arguments\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule870_Position3(StackElement currentElement) {
		@SuppressWarnings("unchecked")
		List<Integer> argumentIndicator = (List<Integer>) currentElement
				.getChild(2).getResult();
		Group group = (Group) currentElement.getChild(0).getResult();
		argumentIndicatorOfGroup.mark(group, argumentIndicator);
		currentElement.setResult(group);
	}

	/*
	 * Group Associativity Group -> Priority {cons("assoc")}
	 */

	/**
	 * Rule 875: cf(Group) cf(LAYOUT?) cf(Associativity) cf(LAYOUT?) cf(Group)
	 * -&gt; cf(Priority) {cons("assoc"),
	 * definedAs("Group Associativity Group -&gt; Priority {cons(\"assoc\")}")}<br>
	 */
	@Override
	protected void execute_Rule875_Position5(StackElement currentElement) {
		AssocPriority assocPriority = (AssocPriority) createVertex(
				AssocPriority.VC, currentElement.getPosition());
		String assoc = currentElement.getChild(2).getLexem().trim()
				.toUpperCase().replace('-', '_');
		assocPriority.set_assoc(AssociativityValue.valueOf(assoc));
		Vertex leftGroup = (Vertex) currentElement.getChild(0).getResult();
		IsPriorityGroupOf ipgo = (IsPriorityGroupOf) createEdge(
				IsPriorityGroupOf.EC, leftGroup, assocPriority);
		ipgo.set_isTransitive(isTransitiveGroup.isMarked(leftGroup));
		if (argumentIndicatorOfGroup.isMarked(leftGroup)) {
			ipgo.set_argumentIndicator((PVector<Integer>) argumentIndicatorOfGroup
					.get(leftGroup));
		}
		Vertex rightGroup = (Vertex) currentElement.getChild(4).getResult();
		ipgo = (IsPriorityGroupOf) createEdge(IsPriorityGroupOf.EC, rightGroup,
				assocPriority);
		ipgo.set_isTransitive(isTransitiveGroup.isMarked(rightGroup));
		if (argumentIndicatorOfGroup.isMarked(rightGroup)) {
			ipgo.set_argumentIndicator((PVector<Integer>) argumentIndicatorOfGroup
					.get(rightGroup));
		}
		currentElement.setResult(assocPriority);
	}

	/*
	 * {Group ">"}+ -> Priority {cons("chain")}
	 */

	/**
	 * Rule 876: cf({Group "&gt;"}+) -&gt; cf(Priority) {cons("chain"),
	 * definedAs("{Group \"&gt;\"}+ -&gt; Priority {cons(\"chain\")}")}<br>
	 */
	@Override
	protected void execute_Rule876_Position0(StackElement currentElement) {
		currentElement.setResult(createVertex(ChainPriority.VC,
				currentElement.getPosition()));
	}

	/**
	 * Rule 876: cf({Group "&gt;"}+) -&gt; cf(Priority) {cons("chain"),
	 * definedAs("{Group \"&gt;\"}+ -&gt; Priority {cons(\"chain\")}")}<br>
	 * Rule 510: cf(Group) -&gt; cf({Group "&gt;"}+)<br>
	 */
	@Override
	protected void execute_Rule876_Term0_Rule510_Position1(
			StackElement currentElement) {
		Group group = (Group) currentElement.getChild(0).getResult();
		IsPriorityGroupOf edge = (IsPriorityGroupOf) createEdge(
				IsPriorityGroupOf.EC, group, currentElement
						.getParentApplicationOfDefinedRule().getResult());
		if (argumentIndicatorOfGroup.isDefined(group)) {
			edge.set_argumentIndicator((PVector<Integer>) argumentIndicatorOfGroup
					.get(group));
		}
		edge.set_isTransitive(isTransitiveGroup.isMarked(group));
	}

	// #####################
	// grammar/grammar/Renaming
	// #####################

	/*
	 * "[" Renaming* "]" -> Renamings {cons("renamings")}
	 */

	/**
	 * Rule 856: "[" cf(LAYOUT?) cf(Renaming*) cf(LAYOUT?) "]" -&gt;
	 * cf(Renamings) {cons("renamings"),
	 * definedAs("\"[\" Renaming* \"]\" -&gt; Renamings {cons(\"renamings\")}")}<br>
	 */
	@Override
	protected void execute_Rule856_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(2).getResult());
	}

	/*
	 * Term "=>" Term -> Renaming {cons("symbol")}
	 */

	/**
	 * Rule 855: cf(Term) cf(LAYOUT?) "=&gt;" cf(LAYOUT?) cf(Term) -&gt;
	 * cf(Renaming) {cons("symbol"),
	 * definedAs("Term \"=&gt;\" Term -&gt; Renaming {cons(\"symbol\")}")}<br>
	 */
	@Override
	protected void execute_Rule855_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("areSemanticActionsAllowed",
				areSemanticActionsAllowed);
		areSemanticActionsAllowed = false;
	}

	/**
	 * Rule 855: cf(Term) cf(LAYOUT?) "=&gt;" cf(LAYOUT?) cf(Term) -&gt;
	 * cf(Renaming) {cons("symbol"),
	 * definedAs("Term \"=&gt;\" Term -&gt; Renaming {cons(\"symbol\")}")}<br>
	 */
	@Override
	protected void execute_Rule855_Position5(StackElement currentElement) {
		Vertex renaming = createVertex(Renaming.VC,
				currentElement.getPosition());
		createEdge(IsOriginalTermOf.EC, currentElement.getChild(0).getResult(),
				renaming);
		createEdge(IsNewTermOf.EC, currentElement.getChild(4).getResult(),
				renaming);
		currentElement.setResult(renaming);
		areSemanticActionsAllowed = (Boolean) currentElement
				.getValueOfTemporaryVariable("areSemanticActionsAllowed");
	}

	/*
	 * OldProduction "=>" OldProduction -> Renaming {cons("production")}
	 */

	/**
	 * Rule 854: cf(OldProduction) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(OldProduction) -&gt; cf(Renaming) {cons("production"), definedAs(
	 * "OldProduction \"=&gt;\" OldProduction -&gt; Renaming {cons(\"production\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule854_Position0(StackElement currentElement) {
		areSemanticActionsAllowed = false;
	}

	/**
	 * Rule 854: cf(OldProduction) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(OldProduction) -&gt; cf(Renaming) {cons("production"), definedAs(
	 * "OldProduction \"=&gt;\" OldProduction -&gt; Renaming {cons(\"production\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule854_Position5(StackElement currentElement) {
		Vertex renaming = createVertex(Renaming.VC,
				currentElement.getPosition());
		createEdge(IsOriginalProductionOf.EC, currentElement.getChild(0)
				.getResult(), renaming);
		createEdge(IsNewProductionOf.EC,
				currentElement.getChild(4).getResult(), renaming);
		currentElement.setResult(renaming);
		areSemanticActionsAllowed = true;
	}

	// #####################
	// grammar/grammar/Restrictions
	// #####################

	/*
	 * "restrictions" Restriction* -> Grammar {cons("restrictions")}
	 */

	/**
	 * Rule 867: "restrictions" cf(LAYOUT?) cf(Restriction*) -&gt; cf(Grammar)
	 * {cons("restrictions"), definedAs(
	 * "\"restrictions\" Restriction* -&gt; Grammar {cons(\"restrictions\")}")}<br>
	 */
	@Override
	protected void execute_Rule867_Position3(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING: Check if you did not forget to add \"lexcial\" or \"context-free\""
						+ " to the following restrictions block."));
		Restrictions restrictions = (Restrictions) createVertex(
				Restrictions.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.KERNEL);
		createEdge(IsRestrictionOf.EC, currentElement.getChild(2).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
	}

	/*
	 * "lexical" "restrictions" Restriction* -> Grammar
	 * {cons("lexical-restrictions")}
	 */

	/**
	 * Rule 866: "lexical" cf(LAYOUT?) "restrictions" cf(LAYOUT?)
	 * cf(Restriction*) -&gt; cf(Grammar) {cons("lexical-restrictions"),
	 * definedAs(
	 * "\"lexical\" \"restrictions\" Restriction* -&gt; Grammar {cons(\"lexical-restrictions\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule866_Position5(StackElement currentElement) {
		Restrictions restrictions = (Restrictions) createVertex(
				Restrictions.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.LEXICAL);
		createEdge(IsRestrictionOf.EC, currentElement.getChild(4).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
	}

	/*
	 * "context-free" "restrictions" Restriction* -> Grammar
	 * {cons("context-free-restrictions")}
	 */

	/**
	 * Rule 865: "context-free" cf(LAYOUT?) "restrictions" cf(LAYOUT?)
	 * cf(Restriction*) -&gt; cf(Grammar) {cons("context-free-restrictions"),
	 * definedAs(
	 * "\"context-free\" \"restrictions\" Restriction* -&gt; Grammar {cons(\"context-free-restrictions\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule865_Position0(StackElement currentElement) {
		isContextFree = true;
	}

	/**
	 * Rule 865: "context-free" cf(LAYOUT?) "restrictions" cf(LAYOUT?)
	 * cf(Restriction*) -&gt; cf(Grammar) {cons("context-free-restrictions"),
	 * definedAs(
	 * "\"context-free\" \"restrictions\" Restriction* -&gt; Grammar {cons(\"context-free-restrictions\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule865_Position5(StackElement currentElement) {
		Restrictions restrictions = (Restrictions) createVertex(
				Restrictions.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.CONTEXT_FREE);
		createEdge(IsRestrictionOf.EC, currentElement.getChild(4).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
		isContextFree = false;
	}

	/*
	 * Term* "-/-" Lookaheads -> Restriction {cons("follow")}
	 */

	/**
	 * Rule 863: cf(Term*) cf(LAYOUT?) "-/-" cf(LAYOUT?) cf(Lookaheads) -&gt;
	 * cf(Restriction) {cons("follow"), definedAs(
	 * "Term* \"-/-\" Lookaheads -&gt; Restriction {cons(\"follow\")}")}<br>
	 */
	@Override
	protected void execute_Rule863_Position5(StackElement currentElement) {
		Vertex restriction = createVertex(Restriction.VC,
				currentElement.getPosition());
		createEdge(IsRestrictedTermOf.EC, currentElement.getChild(0)
				.getResult(), restriction);
		createEdge(IsLookaheadOf.EC, currentElement.getChild(4).getResult(),
				restriction);
		currentElement.setResult(restriction);
	}

	/*
	 * Lookahead -> Lookaheads {cons("single")}
	 */

	/**
	 * Rule 862: cf(Lookahead) -&gt; cf(Lookaheads) {cons("single"),
	 * definedAs("Lookahead -&gt; Lookaheads {cons(\"single\")}")}<br>
	 */
	@Override
	protected void execute_Rule862_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * Lookaheads "|" Lookaheads -> Lookaheads {right,cons("alt")}
	 */

	/**
	 * Rule 861: cf(Lookaheads) cf(LAYOUT?) "|" cf(LAYOUT?) cf(Lookaheads) -&gt;
	 * cf(Lookaheads) {cons("alt"), definedAs(
	 * "Lookaheads \"|\" Lookaheads -&gt; Lookaheads {right,cons(\"alt\")}"),
	 * right}<br>
	 */
	@Override
	protected void execute_Rule861_Position5(StackElement currentElement) {
		Vertex alternative = createVertex(
				de.uni_koblenz.edl.preprocessor.schema.grammar.Alternative.VC,
				currentElement.getPosition());
		currentElement.setResult(alternative);
		createEdge(IsLeftAlternativeOf.EC, currentElement.getChild(0)
				.getResult(), alternative);
		createEdge(IsRightAlternativeOf.EC, currentElement.getChild(4)
				.getResult(), alternative);
	}

	/*
	 * "(" Lookaheads ")" -> Lookaheads {bracket}
	 */

	/**
	 * Rule 860: "(" cf(LAYOUT?) cf(Lookaheads) cf(LAYOUT?) ")" -&gt;
	 * cf(Lookaheads) {bracket,
	 * definedAs("\"(\" Lookaheads \")\" -&gt; Lookaheads {bracket}")}<br>
	 */
	@Override
	protected void execute_Rule860_Position5(StackElement currentElement) {
		Vertex sequence = createVertex(
				de.uni_koblenz.edl.preprocessor.schema.grammar.Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(de.uni_koblenz.edl.preprocessor.schema.grammar.IsPartOf.EC,
				currentElement.getChild(2).getResult(), sequence);
	}

	/*
	 * "[[" {Lookahead ","}* "]]" -> Lookaheads {cons("list")}
	 */

	/**
	 * Rule 859: "[[" cf(LAYOUT?) cf({Lookahead ","}*) cf(LAYOUT?) "]]" -&gt;
	 * cf(Lookaheads) {cons("list"), definedAs(
	 * "\"[[\" {Lookahead \",\"}* \"]]\" -&gt; Lookaheads {cons(\"list\")}")}<br>
	 */
	@Override
	protected void execute_Rule859_Position0(StackElement currentElement) {
		currentElement.setResult(createVertex(
				de.uni_koblenz.edl.preprocessor.schema.grammar.List.VC,
				currentElement.getPosition()));
	}

	/**
	 * Rule 859: "[[" cf(LAYOUT?) cf({Lookahead ","}*) cf(LAYOUT?) "]]" -&gt;
	 * cf(Lookaheads) {cons("list"), definedAs(
	 * "\"[[\" {Lookahead \",\"}* \"]]\" -&gt; Lookaheads {cons(\"list\")}")}<br>
	 * Rule 482: cf(Lookahead) -&gt; cf({Lookahead ","}+)<br>
	 */
	@Override
	protected void execute_Rule859_Term2_Rule482_Position1(
			StackElement currentElement) {
		createEdge(IsListPartOf.EC, currentElement.getChild(0).getResult(),
				currentElement.getParentApplicationOfDefinedRule().getResult());
	}

	/*
	 * CharClass -> Lookahead {cons("char-class")}
	 */

	/**
	 * Rule 858: cf(CharClass) -&gt; cf(Lookahead) {cons("char-class"),
	 * definedAs("CharClass -&gt; Lookahead {cons(\"char-class\")}")}<br>
	 */
	@Override
	protected void execute_Rule858_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * CharClass "." Lookaheads -> Lookahead {cons("seq")}
	 */

	/**
	 * Rule 857: cf(CharClass) cf(LAYOUT?) "." cf(LAYOUT?) cf(Lookaheads) -&gt;
	 * cf(Lookahead) {cons("seq"),
	 * definedAs("CharClass \".\" Lookaheads -&gt; Lookahead {cons(\"seq\")}")}<br>
	 */
	@Override
	protected void execute_Rule857_Position5(StackElement currentElement) {
		Lookahead charClass = (Lookahead) currentElement.getChild(0)
				.getResult();
		Object lookaheads = currentElement.getChild(4).getResult();
		createEdge(IsPreviousLookahead.EC, lookaheads, charClass);
		currentElement.setResult(charClass);
	}

	// #####################
	// grammar/grammar/Sorts
	// #####################

	/*
	 * "sorts" Term* -> Grammar {cons("sorts")}
	 */

	/**
	 * Rule 894: "sorts" cf(LAYOUT?) cf(Term*) -&gt; cf(Grammar) {cons("sorts"),
	 * definedAs("\"sorts\" Term* -&gt; Grammar {cons(\"sorts\")}")}<br>
	 */
	@Override
	protected void execute_Rule894_Position3(StackElement currentElement) {
		Vertex sort = createVertex(Sorts.VC, currentElement.getPosition());
		currentElement.setResult(sort);
		createEdge(IsSortOf.EC, currentElement.getChild(2).getResult(), sort);
	}

	// #####################
	// grammar/grammar/StartSymbols
	// #####################

	/*
	 * "start-symbols" StartDef* -> Grammar {cons("kernel-start-symbols")}
	 */

	/**
	 * Rule 892: "start-symbols" cf(LAYOUT?) cf(StartDef*) -&gt; cf(Grammar)
	 * {cons("kernel-start-symbols"), definedAs(
	 * "\"start-symbols\" StartDef* -&gt; Grammar {cons(\"kernel-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule892_Position0(StackElement currentElement) {
		isStartSymbol = true;
	}

	/**
	 * Rule 892: "start-symbols" cf(LAYOUT?) cf(StartDef*) -&gt; cf(Grammar)
	 * {cons("kernel-start-symbols"), definedAs(
	 * "\"start-symbols\" StartDef* -&gt; Grammar {cons(\"kernel-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule892_Position3(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING: Check if you did not forget to add \"lexcial\" or \"context-free\""
						+ " to the following start-symbols block."));
		StartSymbols restrictions = (StartSymbols) createVertex(
				StartSymbols.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.KERNEL);
		createEdge(IsStartSymbolOf.EC, currentElement.getChild(2).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
		isStartSymbol = false;
	}

	/*
	 * "lexical" "start-symbols" StartDef* -> Grammar
	 * {cons("lexical-start-symbols")}
	 */

	/**
	 * Rule 891: "lexical" cf(LAYOUT?) "start-symbols" cf(LAYOUT?) cf(StartDef*)
	 * -&gt; cf(Grammar) {cons("lexical-start-symbols"), definedAs(
	 * "\"lexical\" \"start-symbols\" StartDef* -&gt; Grammar {cons(\"lexical-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule891_Position0(StackElement currentElement) {
		isStartSymbol = true;
	}

	/**
	 * Rule 891: "lexical" cf(LAYOUT?) "start-symbols" cf(LAYOUT?) cf(StartDef*)
	 * -&gt; cf(Grammar) {cons("lexical-start-symbols"), definedAs(
	 * "\"lexical\" \"start-symbols\" StartDef* -&gt; Grammar {cons(\"lexical-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule891_Position5(StackElement currentElement) {
		StartSymbols restrictions = (StartSymbols) createVertex(
				StartSymbols.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.LEXICAL);
		createEdge(IsStartSymbolOf.EC, currentElement.getChild(4).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
		isStartSymbol = false;
	}

	/*
	 * "context-free" "start-symbols" StartDef* -> Grammar
	 * {cons("context-free-start-symbols")}
	 */

	/**
	 * Rule 890: "context-free" cf(LAYOUT?) "start-symbols" cf(LAYOUT?)
	 * cf(StartDef*) -&gt; cf(Grammar) {cons("context-free-start-symbols"),
	 * definedAs(
	 * "\"context-free\" \"start-symbols\" StartDef* -&gt; Grammar {cons(\"context-free-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule890_Position0(StackElement currentElement) {
		isContextFree = true;
		isStartSymbol = true;
	}

	/**
	 * Rule 890: "context-free" cf(LAYOUT?) "start-symbols" cf(LAYOUT?)
	 * cf(StartDef*) -&gt; cf(Grammar) {cons("context-free-start-symbols"),
	 * definedAs(
	 * "\"context-free\" \"start-symbols\" StartDef* -&gt; Grammar {cons(\"context-free-start-symbols\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule890_Position5(StackElement currentElement) {
		StartSymbols restrictions = (StartSymbols) createVertex(
				StartSymbols.VC, currentElement.getPosition());
		restrictions.set_type(GrammarType.CONTEXT_FREE);
		createEdge(IsStartSymbolOf.EC, currentElement.getChild(4).getResult(),
				restrictions);
		currentElement.setResult(restrictions);
		isContextFree = false;
		isStartSymbol = false;
	}

	/*
	 * Term SemanticAction? -> StartDef
	 */

	/**
	 * Rule 889: cf(Term) cf(LAYOUT?) cf(SemanticAction?) -&gt; cf(StartDef)
	 * {definedAs("Term SemanticAction? -&gt; StartDef")}<br>
	 */
	@Override
	protected void execute_Rule889_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 889: cf(Term) cf(LAYOUT?) cf(SemanticAction?) -&gt; cf(StartDef)
	 * {definedAs("Term SemanticAction? -&gt; StartDef")}<br>
	 */
	@Override
	protected void execute_Rule889_Position1(StackElement currentElement) {
		Term term = (Term) currentElement.getChild(0).getResult();
		currentElement.setResult(term);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 889: cf(Term) cf(LAYOUT?) cf(SemanticAction?) -&gt; cf(StartDef)
	 * {definedAs("Term SemanticAction? -&gt; StartDef")}<br>
	 * Rule 525: cf(SemanticAction) -&gt; cf(SemanticAction?)<br>
	 */
	@Override
	protected void execute_Rule889_Term2_Rule525_Position1(
			StackElement currentElement) {
		createEdge(IsSemanticActionOf.EC, currentElement.getChild(0)
				.getResult(), currentElement
				.getParentApplicationOfDefinedRule().getResult());
	}

	/**
	 * Rule 889: cf(Term) cf(LAYOUT?) cf(SemanticAction?) -&gt; cf(StartDef)
	 * {definedAs("Term SemanticAction? -&gt; StartDef")}<br>
	 */
	@Override
	protected void execute_Rule889_Position3(StackElement currentElement) {
		leaveVisibilityStage();
		headVariable = null;
	}

	// #####################
	// grammar/grammar/Syntax
	// #####################

	/*
	 * "syntax" Production* -> Grammar {cons("syntax")}
	 */

	/**
	 * Rule 887: "syntax" cf(LAYOUT?) cf(Production*) -&gt; cf(Grammar)
	 * {cons("syntax"),
	 * definedAs("\"syntax\" Production* -&gt; Grammar {cons(\"syntax\")}")}<br>
	 */
	@Override
	protected void execute_Rule887_Position3(StackElement currentElement) {
		System.out
				.println(createMessageString(
						currentElement,
						"WARNING: It is recommanded to add \"lexcial\" or \"context-free\""
								+ " to the following syntax block. Otherwise it could cause exceptions."));
		Syntax syntax = (Syntax) createVertex(Syntax.VC,
				currentElement.getPosition());
		syntax.set_type(GrammarType.KERNEL);
		createEdge(IsProductionOf.EC, currentElement.getChild(2).getResult(),
				syntax);
		currentElement.setResult(syntax);
	}

	/*
	 * "lexical" "syntax" Production* -> Grammar {cons("lexical-syntax")}
	 */

	/**
	 * Rule 886: "lexical" cf(LAYOUT?) "syntax" cf(LAYOUT?) cf(Production*)
	 * -&gt; cf(Grammar) {cons("lexical-syntax"), definedAs(
	 * "\"lexical\" \"syntax\" Production* -&gt; Grammar {cons(\"lexical-syntax\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule886_Position5(StackElement currentElement) {
		Syntax syntax = (Syntax) createVertex(Syntax.VC,
				currentElement.getPosition());
		syntax.set_type(GrammarType.LEXICAL);
		createEdge(IsProductionOf.EC, currentElement.getChild(4).getResult(),
				syntax);
		currentElement.setResult(syntax);
	}

	/*
	 * "context-free" "syntax" Production* -> Grammar
	 * {cons("context-free-syntax")}
	 */

	/**
	 * Rule 885: "context-free" cf(LAYOUT?) "syntax" cf(LAYOUT?) cf(Production*)
	 * -&gt; cf(Grammar) {cons("context-free-syntax"), definedAs(
	 * "\"context-free\" \"syntax\" Production* -&gt; Grammar {cons(\"context-free-syntax\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule885_Position0(StackElement currentElement) {
		isContextFree = true;
	}

	/**
	 * Rule 885: "context-free" cf(LAYOUT?) "syntax" cf(LAYOUT?) cf(Production*)
	 * -&gt; cf(Grammar) {cons("context-free-syntax"), definedAs(
	 * "\"context-free\" \"syntax\" Production* -&gt; Grammar {cons(\"context-free-syntax\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule885_Position5(StackElement currentElement) {
		Syntax syntax = (Syntax) createVertex(Syntax.VC,
				currentElement.getPosition());
		syntax.set_type(GrammarType.CONTEXT_FREE);
		createEdge(IsProductionOf.EC, currentElement.getChild(4).getResult(),
				syntax);
		currentElement.setResult(syntax);
		isContextFree = false;
	}

	// #####################
	// grammar/grammar/Variables
	// #####################

	/*
	 * "variables" OldProduction* -> Grammar {cons("variables")}
	 */

	/**
	 * Rule 883: "variables" cf(LAYOUT?) cf(OldProduction*) -&gt; cf(Grammar)
	 * {cons("variables"), definedAs(
	 * "\"variables\" OldProduction* -&gt; Grammar {cons(\"variables\")}")}<br>
	 */
	@Override
	protected void execute_Rule883_Position3(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING: Check if you did not forget to add \"lexcial\" or \"context-free\""
						+ " to the following variables block."));
		System.out
				.println(createMessageString(currentElement,
						"WARNING: Variables are not supported by EDL. Use on own risk."));
		Variables variables = (Variables) createVertex(Variables.VC,
				currentElement.getPosition());
		variables.set_type(GrammarType.KERNEL);
		createEdge(IsVariableDefinitionOf.EC, currentElement.getChild(2)
				.getResult(), variables);
		currentElement.setResult(variables);
	}

	/*
	 * "lexical" "variables" OldProduction* -> Grammar
	 * {cons("lexical-variables")}
	 */

	/**
	 * Rule 882: "lexical" cf(LAYOUT?) "variables" cf(LAYOUT?)
	 * cf(OldProduction*) -&gt; cf(Grammar) {cons("lexical-variables"),
	 * definedAs(
	 * "\"lexical\" \"variables\" OldProduction* -&gt; Grammar {cons(\"lexical-variables\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule882_Position5(StackElement currentElement) {
		Variables variables = (Variables) createVertex(Variables.VC,
				currentElement.getPosition());
		System.out
				.println(createMessageString(currentElement,
						"WARNING: Variables are not supported by EDL. Use on own risk."));
		variables.set_type(GrammarType.LEXICAL);
		createEdge(IsVariableDefinitionOf.EC, currentElement.getChild(4)
				.getResult(), variables);
		currentElement.setResult(variables);
	}

	// #####################
	// grammar/terms/AbbreviationTerm
	// #####################

	/*
	 * "<" SemanticAction* Term SemanticAction* "," {(SemanticAction* Term
	 * SemanticAction*) ","}+ ">" -> Term {cons("tuple")}
	 */

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule900_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule900_Position1(StackElement currentElement) {
		Vertex tuple = createVertex(Tuple.VC, currentElement.getPosition());
		currentElement.setResult(tuple);

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(0).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("<");
		createEdge(IsSyntaxElementOf.EC, literal, tuple);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule900_Position5(StackElement currentElement) {
		nextTermInVisibilityStage((Term) currentElement.getChild(4).getResult());
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule900_Position9(StackElement currentElement) {
		Vertex tuple = (Vertex) currentElement.getResult();

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				tuple);

		createEdge(IsElementOf.EC, currentElement.getChild(4).getResult(),
				tuple);

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(6).getResult()),
				tuple);

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(8).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(",");
		createEdge(IsSyntaxElementOf.EC, literal, tuple);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 * Rule 547: cf((SemanticAction* Term SemanticAction*)) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule900_Term10_Rule547_Term0_Rule627_Position3(
			StackElement currentElement) {
		Vertex tuple = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				tuple);

		Term term = (Term) currentElement.getChild(2).getResult();
		createEdge(IsElementOf.EC, term, tuple);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 * Rule 547: cf((SemanticAction* Term SemanticAction*)) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule900_Term10_Rule547_Term0_Rule627_Position5(
			StackElement currentElement) {
		Vertex tuple = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(4).getResult()),
				tuple);
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 * Rule 546: cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?)
	 * "," cf(LAYOUT?) cf({(SemanticAction* Term SemanticAction*) ","}+) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) {left}<br>
	 */
	@Override
	protected void execute_Rule900_Term10_Rule546_Position3(
			StackElement currentElement) {
		Vertex tuple = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(2).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(",");
		createEdge(IsSyntaxElementOf.EC, literal, tuple);
		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 900: "&lt;" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?) "&gt;"
	 * -&gt; cf(Term) {cons("tuple"), definedAs(
	 * "\"&lt;\" SemanticAction* Term SemanticAction* \",\" {(SemanticAction* Term SemanticAction*) \",\"}+ \"&gt;\" -&gt; Term {cons(\"tuple\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule900_Position13(StackElement currentElement) {
		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(12).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(">");
		createEdge(IsSyntaxElementOf.EC, literal, currentElement
				.getParentApplicationOfDefinedRule().getResult());

		nextTermInVisibilityStage(literal);
		leaveVisibilityStage();
	}

	/*
	 * "(" Terms "=>" Term ")" -> Term {cons("func")}
	 */

	/**
	 * Rule 899: "(" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("func"), definedAs(
	 * "\"(\" Terms \"=&gt;\" Term \")\" -&gt; Term {cons(\"func\")}")}<br>
	 */
	@Override
	protected void execute_Rule899_Position0(StackElement currentElement) {
		enterVisibilityStage();

		Vertex function = createVertex(Function.VC,
				currentElement.getPosition());
		currentElement.setResult(function);
	}

	/**
	 * Rule 899: "(" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("func"), definedAs(
	 * "\"(\" Terms \"=&gt;\" Term \")\" -&gt; Term {cons(\"func\")}")}<br>
	 */
	@Override
	protected void execute_Rule899_Position1(StackElement currentElement) {
		Vertex function = (Vertex) currentElement.getResult();

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(0).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("(");
		createEdge(IsSyntaxElementOf.EC, literal, function);
	}

	/**
	 * Rule 899: "(" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("func"), definedAs(
	 * "\"(\" Terms \"=&gt;\" Term \")\" -&gt; Term {cons(\"func\")}")}<br>
	 */
	@Override
	protected void execute_Rule899_Position5(StackElement currentElement) {
		Vertex function = (Vertex) currentElement.getResult();

		Sequence sequence = (Sequence) currentElement.getChild(2).getResult();
		Edge edge = sequence.getFirstIncidence(EdgeDirection.IN);
		while (edge != null) {
			if (edge.isInstanceOf(IsPartOfSequence.EC)) {
				createEdge(IsElementOf.EC, edge.getThat(), function);
			} else {
				assert edge.isInstanceOf(IsSemanticActionOf.EC);
				createEdge(IsSemanticActionOf.EC, edge.getThat(), function);
			}
			Edge oldEdge = edge;
			edge = edge.getNextIncidence(EdgeDirection.IN);
			oldEdge.delete();
		}
		deleteTree(sequence);

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(4).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("=>");
		createEdge(IsSyntaxElementOf.EC, literal, function);
		currentElement.setValueOfTemporaryVariable("areSemanticActionsAllowed",
				areSemanticActionsAllowed);
		areSemanticActionsAllowed = false;
	}

	/**
	 * Rule 899: "(" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("func"), definedAs(
	 * "\"(\" Terms \"=&gt;\" Term \")\" -&gt; Term {cons(\"func\")}")}<br>
	 */
	@Override
	protected void execute_Rule899_Position7(StackElement currentElement) {
		Vertex function = (Vertex) currentElement.getResult();

		Term term = (Term) currentElement.getChild(6).getResult();
		createEdge(IsElementOf.EC, term, function);
	}

	/**
	 * Rule 899: "(" cf(LAYOUT?) cf(Terms) cf(LAYOUT?) "=&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("func"), definedAs(
	 * "\"(\" Terms \"=&gt;\" Term \")\" -&gt; Term {cons(\"func\")}")}<br>
	 */
	@Override
	protected void execute_Rule899_Position9(StackElement currentElement) {
		Vertex function = (Vertex) currentElement.getResult();

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(8).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(")");
		createEdge(IsSyntaxElementOf.EC, literal, function);

		leaveVisibilityStage();
		areSemanticActionsAllowed = (Boolean) currentElement
				.getValueOfTemporaryVariable("areSemanticActionsAllowed");

		System.out
				.println(createMessageString(currentElement,
						"WARNING: Function terms are not supported by EDL. Use on own risk."));
	}

	/*
	 * "(" Term "->" Term ")" -> Term {cons("strategy")}
	 */

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("areSemanticActionsAllowed",
				areSemanticActionsAllowed);
		areSemanticActionsAllowed = false;
	}

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position1(StackElement currentElement) {
		enterVisibilityStage();
		Vertex strategy = createVertex(Strategy.VC,
				currentElement.getPosition());
		currentElement.setResult(strategy);

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(0).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("(");
		createEdge(IsSyntaxElementOf.EC, literal, strategy);
	}

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position3(StackElement currentElement) {
		Vertex strategy = (Vertex) currentElement.getResult();

		Term term = (Term) currentElement.getChild(2).getResult();
		createEdge(IsElementOf.EC, term, strategy);
	}

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position5(StackElement currentElement) {
		Vertex strategy = (Vertex) currentElement.getResult();

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(4).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("->");
		createEdge(IsSyntaxElementOf.EC, literal, strategy);
	}

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position7(StackElement currentElement) {
		Vertex strategy = (Vertex) currentElement.getResult();

		Term term = (Term) currentElement.getChild(6).getResult();
		createEdge(IsElementOf.EC, term, strategy);
	}

	/**
	 * Rule 898: "(" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-&gt;" cf(LAYOUT?)
	 * cf(Term) cf(LAYOUT?) ")" -&gt; cf(Term) {cons("strategy"), definedAs(
	 * "\"(\" Term \"-&gt;\" Term \")\" -&gt; Term {cons(\"strategy\")}")}<br>
	 */
	@Override
	protected void execute_Rule898_Position9(StackElement currentElement) {
		Vertex strategy = (Vertex) currentElement.getResult();

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(8).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(")");
		createEdge(IsSyntaxElementOf.EC, literal, strategy);
		leaveVisibilityStage();
		areSemanticActionsAllowed = (Boolean) currentElement
				.getValueOfTemporaryVariable("areSemanticActionsAllowed");

		System.out
				.println(createMessageString(currentElement,
						"WARNING: Strategies are not supported by EDL. Use on own risk."));
	}

	// #####################
	// grammar/terms/Basic
	// #####################

	/*
	 * "<START>" -> Term {cons("start")}
	 */

	/**
	 * Rule 1027: "&lt;START&gt;" -&gt; cf(Term) {cons("start"),
	 * definedAs("\"&lt;START&gt;\" -&gt; Term {cons(\"start\")}")}<br>
	 */
	@Override
	protected void execute_Rule1027_Position1(StackElement currentElement) {
		currentElement.setResult(createVertex(Start.VC,
				currentElement.getPosition()));
	}

	/*
	 * "<Start>" -> Term {cons("file-start")}
	 */

	/**
	 * Rule 1026: "&lt;Start&gt;" -&gt; cf(Term) {cons("file-start"),
	 * definedAs("\"&lt;Start&gt;\" -&gt; Term {cons(\"file-start\")}")}<br>
	 */
	@Override
	protected void execute_Rule1026_Position1(StackElement currentElement) {
		currentElement.setResult(createVertex(FileStart.VC,
				currentElement.getPosition()));
	}

	/*
	 * "<" Term "-CF" ">" -> Term {cons("cf")}
	 */

	/**
	 * Rule 1025: "&lt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-CF" cf(LAYOUT?)
	 * "&gt;" -&gt; cf(Term) {cons("cf"),
	 * definedAs("\"&lt;\" Term \"-CF\" \"&gt;\" -&gt; Term {cons(\"cf\")}")}<br>
	 */
	@Override
	protected void execute_Rule1025_Position7(StackElement currentElement) {
		KernelTerm kernelTerm = (KernelTerm) createVertex(KernelTerm.VC,
				currentElement.getPosition());
		kernelTerm.set_type(KernelTermType.CF);
		createEdge(IsTermOfKernelTerm.EC, currentElement.getChild(2)
				.getResult(), kernelTerm);
		currentElement.setResult(kernelTerm);
	}

	/*
	 * "<" Term "-LEX" ">" -> Term {cons("lex\")}
	 */

	/**
	 * Rule 1024: "&lt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-LEX" cf(LAYOUT?)
	 * "&gt;" -&gt; cf(Term) {cons("lex"),
	 * definedAs("\"&lt;\" Term \"-LEX\" \"&gt;\" -&gt; Term {cons(\"lex\")}")}<br>
	 */
	@Override
	protected void execute_Rule1024_Position7(StackElement currentElement) {
		KernelTerm kernelTerm = (KernelTerm) createVertex(KernelTerm.VC,
				currentElement.getPosition());
		kernelTerm.set_type(KernelTermType.LEX);
		createEdge(IsTermOfKernelTerm.EC, currentElement.getChild(2)
				.getResult(), kernelTerm);
		currentElement.setResult(kernelTerm);
	}

	/*
	 * "<" Term "-VAR" ">" -> Term {cons("varsym")}
	 */

	/**
	 * Rule 1023: "&lt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) "-VAR" cf(LAYOUT?)
	 * "&gt;" -&gt; cf(Term) {cons("varsym"), definedAs(
	 * "\"&lt;\" Term \"-VAR\" \"&gt;\" -&gt; Term {cons(\"varsym\")}")}<br>
	 */
	@Override
	protected void execute_Rule1023_Position7(StackElement currentElement) {
		System.out
				.println(createMessageString(currentElement,
						"WARNING: Variable terms are not supported by EDL. Use on own risk."));
		KernelTerm kernelTerm = (KernelTerm) createVertex(KernelTerm.VC,
				currentElement.getPosition());
		kernelTerm.set_type(KernelTermType.VAR);
		createEdge(IsTermOfKernelTerm.EC, currentElement.getChild(2)
				.getResult(), kernelTerm);
		currentElement.setResult(kernelTerm);
	}

	/*
	 * "LAYOUT" -> Term {cons("layout")}
	 */

	/**
	 * Rule 1022: "LAYOUT" -&gt; cf(Term) {cons("layout"),
	 * definedAs("\"LAYOUT\" -&gt; Term {cons(\"layout\")}")}<br>
	 */
	@Override
	protected void execute_Rule1022_Position1(StackElement currentElement) {
		currentElement.setResult(createVertex(LAYOUT.VC,
				currentElement.getPosition()));
	}

	// #####################
	// grammar/terms/CharacterClass
	// #####################

	/*
	 * Character "-" Character -> CharRange {cons("range")}
	 */

	/**
	 * Rule 925: cf(Character) cf(LAYOUT?) "-" cf(LAYOUT?) cf(Character) -&gt;
	 * cf(CharRange) {cons("range"),
	 * definedAs("Character \"-\" Character -&gt; CharRange {cons(\"range\")}")}<br>
	 */
	@Override
	protected void execute_Rule925_Position5(StackElement currentElement) {
		CharacterRange characterRange = (CharacterRange) createVertex(
				CharacterRange.VC, currentElement.getPosition());
		characterRange.set_minCharacter(currentElement.getChild(0).getLexem());
		characterRange.set_maxCharacter(currentElement.getChild(4).getLexem());
		currentElement.setResult(characterRange);
	}

	/*
	 * Character -> CharRange
	 */

	/**
	 * Rule 926: cf(Character) -&gt; cf(CharRange)
	 * {definedAs("Character -&gt; CharRange")}<br>
	 */
	@Override
	protected void execute_Rule926_Position1(StackElement currentElement) {
		CharacterRange characterRange = (CharacterRange) createVertex(
				CharacterRange.VC, currentElement.getPosition());
		characterRange.set_minCharacter(currentElement.getChild(0).getLexem());
		characterRange.set_maxCharacter(currentElement.getChild(0).getLexem());
		currentElement.setResult(characterRange);
	}

	/*
	 * "(" CharRanges ")" -> CharRanges {bracket}
	 */

	/**
	 * Rule 927: "(" cf(LAYOUT?) cf(CharRanges) cf(LAYOUT?) ")" -&gt;
	 * cf(CharRanges) {bracket,
	 * definedAs("\"(\" CharRanges \")\" -&gt; CharRanges {bracket}")}<br>
	 */
	@Override
	protected void execute_Rule927_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(2).getResult());
	}

	/*
	 * CharRanges CharRanges -> CharRanges {right,cons("conc")}")}
	 */

	/**
	 * Rule 928: cf(CharRanges) cf(LAYOUT?) cf(CharRanges) -&gt; cf(CharRanges)
	 * {right, cons("conc"), definedAs(
	 * "CharRanges CharRanges -&gt; CharRanges {right,cons(\"conc\")}")}<br>
	 */
	@Override
	protected void execute_Rule928_Position3(StackElement currentElement) {
		@SuppressWarnings("unchecked")
		List<CharacterRange> charRanges1 = (List<CharacterRange>) currentElement
				.getChild(0).getResult();
		@SuppressWarnings("unchecked")
		List<CharacterRange> charRanges2 = (List<CharacterRange>) currentElement
				.getChild(2).getResult();
		charRanges1.addAll(charRanges2);
		currentElement.setResult(charRanges1);
	}

	/*
	 * CharRange -> CharRanges
	 */

	/**
	 * Rule 929: cf(CharRange) -&gt; cf(CharRanges)
	 * {definedAs("CharRange -&gt; CharRanges")}<br>
	 */
	@Override
	protected void execute_Rule929_Position1(StackElement currentElement) {
		List<CharacterRange> characterRanges = new LinkedList<CharacterRange>();
		characterRanges.add((CharacterRange) currentElement.getChild(0)
				.getResult());
		currentElement.setResult(characterRanges);
	}

	/*
	 * "[" CharRanges? "]" -> CharClass {cons("simple-charclass\")}
	 */

	/**
	 * Rule 935: "[" cf(LAYOUT?) cf(CharRanges?) cf(LAYOUT?) "]" -&gt;
	 * cf(CharClass) {cons("simple-charclass"), definedAs(
	 * "\"[\" CharRanges? \"]\" -&gt; CharClass {cons(\"simple-charclass\")}")}<br>
	 */
	@Override
	protected void execute_Rule935_Position0(StackElement currentElement) {
		Vertex charRanges = createVertex(CharacterRanges.VC,
				currentElement.getPosition());
		currentElement.setResult(charRanges);
	}

	/**
	 * Rule 935: "[" cf(LAYOUT?) cf(CharRanges?) cf(LAYOUT?) "]" -&gt;
	 * cf(CharClass) {cons("simple-charclass"), definedAs(
	 * "\"[\" CharRanges? \"]\" -&gt; CharClass {cons(\"simple-charclass\")}")}<br>
	 * Rule 581: cf(CharRanges) -&gt; cf(CharRanges?)<br>
	 */
	@Override
	protected void execute_Rule935_Term2_Rule581_Position1(
			StackElement currentElement) {
		createEdge(IsCharacterRangeOf.EC, currentElement.getChild(0)
				.getResult(), currentElement
				.getParentApplicationOfDefinedRule().getResult());
	}

	/*
	 * "~" CharClass -> CharClass {cons("comp")}
	 */

	/**
	 * Rule 934: "~" cf(LAYOUT?) cf(CharClass) -&gt; cf(CharClass)
	 * {cons("comp"),
	 * definedAs("\"~\" CharClass -&gt; CharClass {cons(\"comp\")}")}<br>
	 */
	@Override
	protected void execute_Rule934_Position3(StackElement currentElement) {
		Vertex complement = createVertex(Complement.VC,
				currentElement.getPosition());
		createEdge(IsCharacterClassOfComplement.EC, currentElement.getChild(2)
				.getResult(), complement);
		currentElement.setResult(complement);
	}

	/*
	 * CharClass "/" CharClass -> CharClass {left,cons("diff")}
	 */

	/**
	 * Rule 933: cf(CharClass) cf(LAYOUT?) "/" cf(LAYOUT?) cf(CharClass) -&gt;
	 * cf(CharClass) {cons("diff"), definedAs(
	 * "CharClass \"/\" CharClass -&gt; CharClass {left,cons(\"diff\")}"), left}<br>
	 */
	@Override
	protected void execute_Rule933_Position5(StackElement currentElement) {
		Vertex difference = createVertex(Difference.VC,
				currentElement.getPosition());
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(0).getResult(), difference);
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(4).getResult(), difference);
		currentElement.setResult(difference);
	}

	/*
	 * CharClass "/\\" CharClass -> CharClass {left,cons("isect")}
	 */

	/**
	 * Rule 932: cf(CharClass) cf(LAYOUT?) "/\\" cf(LAYOUT?) cf(CharClass) -&gt;
	 * cf(CharClass) {cons("isect"), definedAs(
	 * "CharClass \"/\\\\\" CharClass -&gt; CharClass {left,cons(\"isect\")}"),
	 * left}<br>
	 */
	@Override
	protected void execute_Rule932_Position5(StackElement currentElement) {
		Vertex intersection = createVertex(Intersection.VC,
				currentElement.getPosition());
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(0).getResult(), intersection);
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(4).getResult(), intersection);
		currentElement.setResult(intersection);
	}

	/*
	 * CharClass "\\/" CharClass -> CharClass {left,cons("union")}
	 */

	/**
	 * Rule 931: cf(CharClass) cf(LAYOUT?) "\\/" cf(LAYOUT?) cf(CharClass) -&gt;
	 * cf(CharClass) {cons("union"), definedAs(
	 * "CharClass \"\\\\/\" CharClass -&gt; CharClass {left,cons(\"union\")}"),
	 * left}<br>
	 */
	@Override
	protected void execute_Rule931_Position5(StackElement currentElement) {
		Vertex union = createVertex(Union.VC, currentElement.getPosition());
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(0).getResult(), union);
		createEdge(IsPartOfBinaryCharacterClassOperation.EC, currentElement
				.getChild(4).getResult(), union);
		currentElement.setResult(union);
	}

	/*
	 * "(" CharClass ")" -> CharClass {bracket, avoid}
	 */

	/**
	 * Rule 930: "(" cf(LAYOUT?) cf(CharClass) cf(LAYOUT?) ")" -&gt;
	 * cf(CharClass) {bracket, avoid,
	 * definedAs("\"(\" CharClass \")\" -&gt; CharClass {bracket, avoid}")}<br>
	 */
	@Override
	protected void execute_Rule930_Position5(StackElement currentElement) {
		Vertex charClass = (Vertex) currentElement.getChild(2).getResult();
		currentElement.setResult(charClass);
		positionsMap.put(charClass, currentElement.getPosition());
	}

	/*
	 * CharClass -> Term {cons("char-class")}
	 */

	/**
	 * Rule 936: cf(CharClass) -&gt; cf(Term) {cons("char-class"),
	 * definedAs("CharClass -&gt; Term {cons(\"char-class\")}")}<br>
	 */
	@Override
	protected void execute_Rule936_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	// #####################
	// grammar/terms/Label
	// #####################

	/*
	 * IdCon -> Label {cons("unquoted")}
	 */

	/**
	 * Rule 949: cf(IdCon) -&gt; cf(Label) {cons("unquoted"),
	 * definedAs("IdCon -&gt; Label {cons(\"unquoted\")}")}<br>
	 */
	@Override
	protected void execute_Rule949_Position1(StackElement currentElement) {
		String nameOfLabel = currentElement.getChild(0).getLexem();
		Vertex label = createVertex(Label.VC, currentElement.getPosition());
		currentElement.setResult(label);
		Identifier identifier = (Identifier) createVertex(Identifier.VC,
				currentElement.getPosition());
		identifier.set_name(nameOfLabel);
		createEdge(IsNameOfLabel.EC, identifier, label);
	}

	/*
	 * StrCon -> Label {cons("quoted")}
	 */

	/**
	 * Rule 950: cf(StrCon) -&gt; cf(Label) {cons("quoted"),
	 * definedAs("StrCon -&gt; Label {cons(\"quoted\")}")}<br>
	 */
	@Override
	protected void execute_Rule950_Position1(StackElement currentElement) {
		String nameOfLabel = currentElement.getChild(0).getLexem();
		Vertex label = createVertex(Label.VC, currentElement.getPosition());
		currentElement.setResult(label);
		Identifier identifier = (Identifier) createVertex(Identifier.VC,
				currentElement.getPosition());
		identifier.set_name(nameOfLabel);
		createEdge(IsNameOfLabel.EC, identifier, label);
	}

	/*
	 * Label ":" Term -> Term {cons("label")}
	 */

	/**
	 * Rule 951: cf(Label) cf(LAYOUT?) ":" cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("label"),
	 * definedAs("Label \":\" Term -&gt; Term {cons(\"label\")}")}<br>
	 */
	@Override
	protected void execute_Rule951_Position5(StackElement currentElement) {
		Label label = (Label) currentElement.getChild(0).getResult();
		String labelName = label.get_identifier().get_name();

		if (namesOfUsedTempVarsInVisibilityStage.peek().contains(labelName)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"Label \""
									+ labelName
									+ "\" is referenced before it was declared. Possibly a name clash with a temporary variable."));
		}

		Term term = (Term) currentElement.getChild(4).getResult();
		Edge edge = createEdge(IsAttachedBy.EC, label, term);
		Edge first = term.getFirstIncidence(EdgeDirection.IN);
		if (edge.getNormalEdge() != first.getNormalEdge()) {
			(edge.isNormal() ? edge.getReversedEdge() : edge)
					.putIncidenceBefore(first);
		}
		currentElement.setResult(term);

		if (labelsInVisibilityStage.peek().containsKey(labelName)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The label \""
									+ labelName
									+ "\" is not unique in the current visibility stage."));
		}
		labelsInVisibilityStage.peek().put(labelName, term);
	}

	// #####################
	// grammar/terms/Literal
	// #####################

	/*
	 * SingleQuotedStrCon -> Term {cons("ci-lit")}
	 */

	/**
	 * Rule 996: cf(SingleQuotedStrCon) -&gt; cf(Term) {cons("ci-lit"),
	 * definedAs("SingleQuotedStrCon -&gt; Term {cons(\"ci-lit\")}")}<br>
	 */
	@Override
	protected void execute_Rule996_Position1(StackElement currentElement) {
		Literal literal = (Literal) createVertex(Literal.VC,
				currentElement.getPosition());
		literal.set_isSingleQuoted(true);
		String lexem = currentElement.getChild(0).getLexem();
		lexem = lexem.substring(1, lexem.length() - 1);
		literal.set_value(lexem);
		currentElement.setResult(literal);
	}

	/*
	 * StrCon -> Term {cons("lit")}
	 */

	/**
	 * Rule 997: cf(StrCon) -&gt; cf(Term) {cons("lit"),
	 * definedAs("StrCon -&gt; Term {cons(\"lit\")}")}<br>
	 */
	@Override
	protected void execute_Rule997_Position1(StackElement currentElement) {
		Literal literal = (Literal) createVertex(Literal.VC,
				currentElement.getPosition());
		literal.set_isSingleQuoted(false);
		String lexem = currentElement.getChild(0).getLexem();
		lexem = lexem.substring(1, lexem.length() - 1);
		literal.set_value(lexem);
		currentElement.setResult(literal);
	}

	// #####################
	// grammar/terms/PrefixFunction
	// #####################

	/*
	 * FunctionName "(" {Term ","}* ")" "->" Term Attributes -> OldProduction
	 * {avoid, cons("prefix-fun prod")}
	 */

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule902_Position0(StackElement currentElement) {
		areSemanticActionsAllowed = false;
		tempVarTable.push();
		enterVisibilityStage();
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule902_Position1(StackElement currentElement) {
		Literal functionName = (Literal) createVertex(Literal.VC,
				currentElement.getChild(0).getPosition());
		functionName.set_isSingleQuoted(false);
		functionName.set_value(currentElement.getChild(0).getLexem());
		nextTermInVisibilityStage(functionName);

		Vertex prefixFunction = createVertex(
				de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction.VC,
				currentElement.getPosition());
		currentElement.setValueOfTemporaryVariable("prefixFunction",
				prefixFunction);

		createEdge(IsFunctionNameOf.EC, functionName, prefixFunction);
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule902_Position3(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(2).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("(");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 626: cf(Term) -&gt; cf({Term ","}+)<br>
	 */
	@Override
	protected void execute_Rule902_Term4_Rule626_Position1(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsElementOf.EC, term, prefixFunction);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 625: cf({Term ","}+) cf(LAYOUT?) "," cf(LAYOUT?) cf({Term ","}+)
	 * -&gt; cf({Term ","}+) {left}<br>
	 */
	@Override
	protected void execute_Rule902_Term4_Rule625_Position3(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(2).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(",");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule902_Position7(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(6).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(")");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 902: cf(FunctionName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Term ","}*)
	 * cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(Attributes) -&gt; cf(OldProduction) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "FunctionName \"(\" {Term \",\"}* \")\" \"-&gt;\" Term Attributes -&gt; OldProduction {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule902_Position13(StackElement currentElement) {
		Position newPosition = new Position(currentElement.getChild(0)
				.getOffset(), currentElement.getChild(0).getLength()
				+ currentElement.getChild(1).getLength()
				+ currentElement.getChild(2).getLength()
				+ currentElement.getChild(3).getLength()
				+ currentElement.getChild(4).getLength()
				+ currentElement.getChild(5).getLength()
				+ currentElement.getChild(6).getLength(), currentElement
				.getChild(0).getFirstLine(), currentElement.getChild(6)
				.getLastLine(), currentElement.getChild(0).getFirstColumn(),
				currentElement.getChild(6).getLastColumn());
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		positionsMap.put(prefixFunction, newPosition);
		Vertex sequence = createVertex(Sequence.VC, newPosition);
		createEdge(IsPartOfSequence.EC, prefixFunction, sequence);

		Vertex production = createVertex(Production.VC,
				currentElement.getPosition());
		currentElement.setResult(production);

		createEdge(IsBodyTermOfProduction.EC, sequence, production);
		createEdge(IsHeadTermOfProduction.EC, currentElement.getChild(10)
				.getResult(), production);

		createEdge(IsAttributeOf.EC, currentElement.getChild(12).getResult(),
				production);
		leaveVisibilityStage();
		tempVarTable.pop();
		headVariable = null;
		areSemanticActionsAllowed = true;
	}

	/*
	 * "rule" SemanticAction* FunctionName SemanticAction* "(" (SemanticAction*|
	 * {(SemanticAction* Term SemanticAction*) ","}+) ")" SemanticAction* "->"
	 * Term Attributes SemanticAction* -> Production {avoid,
	 * cons("prefix-fun prod")}
	 */

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position0(StackElement currentElement) {
		enterVisibilityStage();
		tempVarTable.push();
		isInBodyOfRule = true;
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position5(StackElement currentElement) {
		Literal functionName = (Literal) createVertex(Literal.VC,
				currentElement.getChild(4).getPosition());
		functionName.set_isSingleQuoted(false);
		functionName.set_value(currentElement.getChild(4).getLexem());

		Vertex prefixFunction = createVertex(
				de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction.VC,
				currentElement.getPosition());
		currentElement.setValueOfTemporaryVariable("prefixFunction",
				prefixFunction);

		createEdge(IsFunctionNameOf.EC, functionName, prefixFunction);
		nextTermInVisibilityStage(functionName);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position9(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(6).getResult()),
				prefixFunction);

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(8).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("(");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 549: cf(SemanticAction*) -&gt; cf(SemanticAction* |
	 * {(SemanticAction* Term SemanticAction*) ","}+)<br>
	 */
	@Override
	protected void execute_Rule903_Term10_Rule549_Position1(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				prefixFunction);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 548: cf({(SemanticAction* Term SemanticAction*) ","}+) -&gt;
	 * cf(SemanticAction* | {(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 547: cf((SemanticAction* Term SemanticAction*)) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule903_Term10_Rule548_Term0_Rule547_Term0_Rule627_Position3(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				prefixFunction);

		Term term = (Term) currentElement.getChild(2).getResult();
		createEdge(IsElementOf.EC, term, prefixFunction);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 548: cf({(SemanticAction* Term SemanticAction*) ","}+) -&gt;
	 * cf(SemanticAction* | {(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 547: cf((SemanticAction* Term SemanticAction*)) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule903_Term10_Rule548_Term0_Rule547_Term0_Rule627_Position5(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(4).getResult()),
				prefixFunction);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 * Rule 548: cf({(SemanticAction* Term SemanticAction*) ","}+) -&gt;
	 * cf(SemanticAction* | {(SemanticAction* Term SemanticAction*) ","}+)<br>
	 * Rule 546: cf({(SemanticAction* Term SemanticAction*) ","}+) cf(LAYOUT?)
	 * "," cf(LAYOUT?) cf({(SemanticAction* Term SemanticAction*) ","}+) -&gt;
	 * cf({(SemanticAction* Term SemanticAction*) ","}+) {left}<br>
	 */
	@Override
	protected void execute_Rule903_Term10_Rule548_Term0_Rule546_Position3(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(2).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(",");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position13(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(12).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(")");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);
		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position17(StackElement currentElement) {
		areSemanticActionsAllowed = false;
		isInBodyOfRule = false;
		isInHeadOfRule = true;
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position19(StackElement currentElement) {
		areSemanticActionsAllowed = true;
		isInHeadOfRule = false;
	}

	/**
	 * Rule 903: "rule" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * cf(FunctionName) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf(SemanticAction* | {(SemanticAction* Term SemanticAction*)
	 * ","}+) cf(LAYOUT?) ")" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?)
	 * "-&gt;" cf(LAYOUT?) cf(Term) cf(LAYOUT?) cf(Attributes) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf(Production) {avoid, cons("prefix-fun prod"),
	 * definedAs(
	 * "\"rule\" SemanticAction* FunctionName SemanticAction* \"(\" (SemanticAction*| {(SemanticAction* Term SemanticAction*) \",\"}+) \")\" SemanticAction* \"-&gt;\" Term Attributes SemanticAction* -&gt; Production {avoid, cons(\"prefix-fun prod\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule903_Position23(StackElement currentElement) {
		Position newPosition = new Position(currentElement.getChild(4)
				.getOffset(), currentElement.getChild(4).getLength()
				+ currentElement.getChild(5).getLength()
				+ currentElement.getChild(6).getLength()
				+ currentElement.getChild(7).getLength()
				+ currentElement.getChild(8).getLength()
				+ currentElement.getChild(9).getLength()
				+ currentElement.getChild(10).getLength()
				+ currentElement.getChild(11).getLength()
				+ currentElement.getChild(12).getLength(), currentElement
				.getChild(4).getFirstLine(), currentElement.getChild(12)
				.getLastLine(), currentElement.getChild(4).getFirstColumn(),
				currentElement.getChild(12).getLastColumn());
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		positionsMap.put(prefixFunction, newPosition);

		Vertex production = createVertex(Production.VC,
				currentElement.getPosition());
		currentElement.setResult(production);

		newPosition = new Position(currentElement.getChild(2).getOffset(),
				currentElement.getChild(2).getLength()
						+ currentElement.getChild(3).getLength()
						+ currentElement.getChild(4).getLength()
						+ currentElement.getChild(5).getLength()
						+ currentElement.getChild(6).getLength()
						+ currentElement.getChild(7).getLength()
						+ currentElement.getChild(8).getLength()
						+ currentElement.getChild(9).getLength()
						+ currentElement.getChild(10).getLength()
						+ currentElement.getChild(11).getLength()
						+ currentElement.getChild(12).getLength()
						+ currentElement.getChild(13).getLength()
						+ currentElement.getChild(14).getLength(),
				currentElement.getChild(2).getFirstLine(), currentElement
						.getChild(14).getLastLine(), currentElement.getChild(2)
						.getFirstColumn(), currentElement.getChild(14)
						.getLastColumn());

		Vertex sequence = createVertex(Sequence.VC, newPosition);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
		createEdge(IsPartOfSequence.EC, prefixFunction, sequence);
		createEdge(IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(14)
						.getResult()), sequence);
		createEdge(IsBodyTermOfProduction.EC, sequence, production);

		Object head = currentElement.getChild(18).getResult();
		createEdge(IsHeadTermOfProduction.EC, head, production);
		if (headVariable != null) {
			createEdge(IsReferencedHead.EC, headVariable, head);
		}

		createEdge(IsAttributeOf.EC, currentElement.getChild(20).getResult(),
				production);

		List<SemanticAction> list = appendAllSemanticActions(currentElement
				.getChild(22).getResult());
		createEdge(IsFollowingSemanticActionOf.EC, list, production);
		leaveVisibilityStage();
		tempVarTable.pop();
		headVariable = null;
	}

	/*
	 * "pattern" FunctionName "(" {PatternTerm ",\}* ")" "->" PatternTerm
	 * SemanticAction+ -> Pattern {avoid, cons("prefix-fun pattern")}
	 */

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position0(StackElement currentElement) {
		areSemanticActionsAllowed = false;
		Pattern pattern = (Pattern) createVertex(Pattern.VC,
				currentElement.getPosition());
		pattern.set_executeBefore(false);
		currentElement.setResult(pattern);

		currentPattern = pattern;
		enterVisibilityStage();
		tempVarTable.push();
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position3(StackElement currentElement) {
		Literal functionName = (Literal) createVertex(Literal.VC,
				currentElement.getChild(2).getPosition());
		functionName.set_isSingleQuoted(false);
		functionName.set_value(currentElement.getChild(2).getLexem());

		Vertex prefixFunction = createVertex(
				de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction.VC,
				currentElement.getPosition());
		currentElement.setValueOfTemporaryVariable("prefixFunction",
				prefixFunction);

		createEdge(IsFunctionNameOf.EC, functionName, prefixFunction);

		nextTermInVisibilityStage(functionName);
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position5(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(4).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value("(");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 * Rule 556: cf(PatternTerm) -&gt; cf({PatternTerm ","}+)<br>
	 */
	@Override
	protected void execute_Rule904_Term6_Rule556_Position1(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsElementOf.EC, term, prefixFunction);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 * Rule 555: cf({PatternTerm ","}+) cf(LAYOUT?) "," cf(LAYOUT?)
	 * cf({PatternTerm ","}+) -&gt; cf({PatternTerm ","}+) {left}<br>
	 */
	@Override
	protected void execute_Rule904_Term6_Rule555_Position3(
			StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(2).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(",");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position9(StackElement currentElement) {
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");

		Literal literal = (Literal) createVertex(Literal.VC, currentElement
				.getChild(8).getPosition());
		literal.set_isSingleQuoted(false);
		literal.set_value(")");
		createEdge(IsSyntaxElementOf.EC, literal, prefixFunction);

		nextTermInVisibilityStage(literal);
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position11(StackElement currentElement) {
		isInHeadOfPattern = true;
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position13(StackElement currentElement) {
		areSemanticActionsAllowed = true;
		isInHeadOfPattern = false;
	}

	/**
	 * Rule 904: "pattern" cf(LAYOUT?) cf(FunctionName) cf(LAYOUT?) "("
	 * cf(LAYOUT?) cf({PatternTerm ","}*) cf(LAYOUT?) ")" cf(LAYOUT?) "-&gt;"
	 * cf(LAYOUT?) cf(PatternTerm) cf(LAYOUT?) cf(SemanticAction+) -&gt;
	 * cf(Pattern) {avoid, cons("prefix-fun pattern"), definedAs(
	 * "\"pattern\" FunctionName \"(\" {PatternTerm \",\"}* \")\" \"-&gt;\" PatternTerm SemanticAction+ -&gt; Pattern {avoid, cons(\"prefix-fun pattern\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule904_Position15(StackElement currentElement) {
		Position newPosition = new Position(currentElement.getChild(2)
				.getOffset(), currentElement.getChild(2).getLength()
				+ currentElement.getChild(3).getLength()
				+ currentElement.getChild(4).getLength()
				+ currentElement.getChild(5).getLength()
				+ currentElement.getChild(6).getLength()
				+ currentElement.getChild(7).getLength()
				+ currentElement.getChild(8).getLength(), currentElement
				.getChild(2).getFirstLine(), currentElement.getChild(8)
				.getLastLine(), currentElement.getChild(2).getFirstColumn(),
				currentElement.getChild(8).getLastColumn());
		Vertex prefixFunction = (Vertex) currentElement
				.getValueOfTemporaryVariable("prefixFunction");
		positionsMap.put(prefixFunction, newPosition);

		Vertex pattern = (Vertex) currentElement.getResult();

		createEdge(IsBodyTermOfPattern.EC, prefixFunction, pattern);
		createEdge(IsHeadTermOfPattern.EC, currentElement.getChild(12)
				.getResult(), pattern);

		createEdge(IsFollowingSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(14)
						.getResult()), pattern);
		leaveVisibilityStage();
		tempVarTable.pop();
		headVariable = null;
		currentPattern = null;
	}

	// #####################
	// grammar/terms/Sort
	// #####################

	/*
	 * Sort -> Term {cons("sort")}
	 */

	/**
	 * used to detect undefined Sorts<br>
	 * String -&gt; StackElement if variable is used but not defined <br>
	 * String -&gt; <code>null</code> if a variable was defined
	 */
	private Map<String, StackElement> sort2position;

	private boolean isInBodyOfRule = false;
	private boolean isInHeadOfRule = false;

	/**
	 * Rule 1017: cf(Sort) -&gt; cf(Term) {cons("sort"),
	 * definedAs("Sort -&gt; Term {cons(\"sort\")}")}<br>
	 */
	@Override
	protected void execute_Rule1017_Position1(StackElement currentElement) {
		Vertex sort = createVertex(Sort.VC, currentElement.getPosition());
		String sortName = currentElement.getChild(0).getLexem().trim();
		Vertex vertex = sortTable.use(sortName);
		if (vertex == null) {
			vertex = sortTable.declare(
					sortName,
					createVertex(Identifier.VC, currentElement.getChild(0)
							.getPosition()));
		}
		Identifier identifier = (Identifier) vertex;
		if (identifier.get_name() == null || identifier.get_name().isEmpty()) {
			identifier.set_name(sortName);
		} else {
			assert identifier.get_name().equals(sortName);
		}
		createEdge(IsNameOfSort.EC, identifier, sort);
		currentElement.setResult(sort);
		if (isInBodyOfRule || isStartSymbol) {
			// is in body of production
			if (!sort2position.containsKey(sortName)) {
				sort2position.put(sortName, currentElement);
			}
		} else if (isInHeadOfRule) {
			// is in head of production
			sort2position.put(sortName, null);
		}
	}

	/*
	 * Sort "[[" {Term ","}+ "]]" -> Term {cons("parameterized-sort")}
	 */

	/**
	 * Rule 1016: cf(Sort) cf(LAYOUT?) "[[" cf(LAYOUT?) cf({Term ","}+)
	 * cf(LAYOUT?) "]]" -&gt; cf(Term) {cons("parameterized-sort"), definedAs(
	 * "Sort \"[[\" {Term \",\"}+ \"]]\" -&gt; Term {cons(\"parameterized-sort\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1016_Position1(StackElement currentElement) {
		Vertex sort = createVertex(Sort.VC, currentElement.getPosition());
		String sortName = currentElement.getChild(0).getLexem().trim();
		Vertex vertex = sortTable.use(sortName);
		if (vertex == null) {
			vertex = sortTable.declare(
					sortName,
					createVertex(Identifier.VC, currentElement.getChild(0)
							.getPosition()));
		}
		Identifier identifier = (Identifier) vertex;
		if (identifier.get_name() == null || identifier.get_name().isEmpty()) {
			identifier.set_name(sortName);
		} else {
			assert identifier.get_name().equals(sortName);
		}
		createEdge(IsNameOfSort.EC, identifier, sort);
		currentElement.setResult(sort);
		if (isInBodyOfRule) {
			// is in body of production
			if (!sort2position.containsKey(sortName)) {
				sort2position.put(sortName, currentElement);
			}
		} else if (isInHeadOfRule) {
			// is in head of production
			sort2position.put(sortName, null);
		}
	}

	/**
	 * Rule 1016: cf(Sort) cf(LAYOUT?) "[[" cf(LAYOUT?) cf({Term ","}+)
	 * cf(LAYOUT?) "]]" -&gt; cf(Term) {cons("parameterized-sort"), definedAs(
	 * "Sort \"[[\" {Term \",\"}+ \"]]\" -&gt; Term {cons(\"parameterized-sort\")}"
	 * )}<br>
	 * Rule 626: cf(Term) -&gt; cf({Term ","}+)<br>
	 */
	@Override
	protected void execute_Rule1016_Term4_Rule626_Position1(
			StackElement currentElement) {
		createEdge(IsParameterOfSort.EC,
				currentElement.getChild(0).getResult(), currentElement
						.getParentApplicationOfDefinedRule().getResult());
	}

	/**
	 * Rule 1016: cf(Sort) cf(LAYOUT?) "[[" cf(LAYOUT?) cf({Term ","}+)
	 * cf(LAYOUT?) "]]" -&gt; cf(Term) {cons("parameterized-sort"), definedAs(
	 * "Sort \"[[\" {Term \",\"}+ \"]]\" -&gt; Term {cons(\"parameterized-sort\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1016_Position7(StackElement currentElement) {
		System.out.println(createMessageString(currentElement,
				"WARNING: Parameterized sorts are not supported by EDL."
						+ " Use on own risk."));
	}

	// #####################
	// grammar/terms/Term
	// #####################

	/*
	 * Term SemanticAction* "|" SemanticAction* Term -> Term {right,cons("alt")}
	 */

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position1(StackElement currentElement) {
		Vertex alternative = createVertex(Alternative.VC,
				currentElement.getPosition());
		currentElement.setResult(alternative);
		Term term = (Term) currentElement.getChild(0).getResult();
		checkTermInAlternative(term, currentElement.getChild(0));
		createEdge(IsPartOfBinaryTerm.EC, term, alternative);
		nextTermInVisibilityStage(term);
	}

	private void checkTermInAlternative(Term term, StackElement currentElement) {
		if (term.isInstanceOf(Sequence.VC)) {
			Sequence sequence = (Sequence) term;
			if (sequence.getDegree(IsPartOfSequence.EC, EdgeDirection.IN) == 1) {
				Term termInSequence = sequence
						.getFirstIsPartOfSequenceIncidence(EdgeDirection.IN)
						.getAlpha();
				if (termInSequence.isInstanceOf(Alternative.VC)) {
					throw new GrammarException(
							createMessageString(
									currentElement,
									"An alternative which is part of an alternative must not be encapsulated in brackets, e.g. A|(B|C) is not allowed."
											+ " Use A|B|C instead."));
				} else if (termInSequence.isInstanceOf(Sequence.VC)) {
					checkTermInAlternative(termInSequence, currentElement);
				}
			}

		}
	}

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position3(StackElement currentElement) {
		Vertex alternative = (Vertex) currentElement.getResult();
		List<SemanticAction> semanticActions = appendAllSemanticActions(currentElement
				.getChild(2).getResult());
		if (semanticActions.isEmpty()) {
			semanticActions.add((SemanticAction) createVertex(
					StatementSemanticAction.VC, currentElement.getChild(2)
							.getPosition()));
		}
		createEdge(IsSemanticActionOf.EC, semanticActions, alternative);
	}

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position4(StackElement currentElement) {
		leaveVisibilityStage();
	}

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position5(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1028: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "|"
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term) -&gt; cf(Term)
	 * {cons("alt"), definedAs(
	 * "Term SemanticAction* \"|\" SemanticAction* Term -&gt; Term {right,cons(\"alt\")}"
	 * ), right}<br>
	 */
	@Override
	protected void execute_Rule1028_Position9(StackElement currentElement) {
		Vertex alternative = (Vertex) currentElement.getResult();
		List<SemanticAction> semanticActions = appendAllSemanticActions(currentElement
				.getChild(6).getResult());
		Term term = (Term) currentElement.getChild(8).getResult();
		checkTermInAlternative(term, currentElement.getChild(8));
		if (term.isInstanceOf(Alternative.VC)) {
			if (!semanticActions.isEmpty()) {
				// get semantic action S in (a|#S#(b|c))
				// and put it to (a|(#S#b|c))
				Edge edge = term.getFirstIncidence(EdgeDirection.IN);
				while (edge != null && edge.isInstanceOf(IsAttachedBy.EC)) {
					edge = edge.getNextIncidence(EdgeDirection.IN);
				}
				if (edge != null && edge.isInstanceOf(IsSemanticActionOf.EC)) {
					Edge nextEdge = edge.getNextIncidence(EdgeDirection.IN);
					SemanticAction semanticAction = (SemanticAction) edge
							.getThat();
					semanticActions.add(semanticAction);
					semanticActions = appendAllSemanticActions(semanticActions);
					assert semanticActions.size() == 1;
					semanticAction = semanticActions.get(0);
					IsSemanticActionOf isao = semanticAction
							.getFirstTerm$IsSemanticActionOfIncidence(EdgeDirection.OUT);
					isao.setOmega(term);
					if (nextEdge != null) {
						isao.getReversedEdge().putIncidenceBefore(nextEdge);
					}
				} else {
					assert semanticActions.size() == 1;
					Edge isSemanticActionOf = createEdge(IsSemanticActionOf.EC,
							semanticActions.get(0), term);
					if (edge != null) {
						isSemanticActionOf.getReversedEdge()
								.putIncidenceBefore(edge);
					}
				}
			}
			createEdge(
					IsSemanticActionOf.EC,
					createVertex(StatementSemanticAction.VC, currentElement
							.getChild(6).getPosition()), alternative);
		} else {
			if (semanticActions.isEmpty()) {
				semanticActions.add((SemanticAction) createVertex(
						StatementSemanticAction.VC, currentElement.getChild(6)
								.getPosition()));
			}
			createEdge(IsSemanticActionOf.EC, semanticActions, alternative);
		}
		createEdge(IsPartOfBinaryTerm.EC, term, alternative);
		nextTermInVisibilityStage(term);
		leaveVisibilityStage();
	}

	/*
	 * "{" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) "}" "*"
	 * -> Term {cons("iter-star-sep")}
	 */

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1029_Position0(StackElement currentElement) {
		de.uni_koblenz.edl.preprocessor.schema.term.List list = (de.uni_koblenz.edl.preprocessor.schema.term.List) createVertex(
				de.uni_koblenz.edl.preprocessor.schema.term.List.VC,
				currentElement.getPosition());
		list.set_type(KleeneOperator.STAR);
		currentElement.setResult(list);
		enterVisibilityStage();
	}

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1029_Term2_Rule627_Position3(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				list);
		Term term = (Term) currentElement.getChild(2).getResult();
		createEdge(IsPartOfBinaryTerm.EC, term, list);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1029_Term2_Rule627_Position5(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		List<SemanticAction> semanticActions = appendAllSemanticActions(currentElement
				.getChild(4).getResult());
		if (semanticActions.isEmpty()) {
			semanticActions.add((SemanticAction) createVertex(
					StatementSemanticAction.VC, currentElement.getChild(4)
							.getPosition()));
		}
		createEdge(IsSemanticActionOf.EC, semanticActions, list);
	}

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1029_Term4_Rule635_Position1(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		SemanticAction semanticAction = (SemanticAction) createVertex(
				StatementSemanticAction.VC, currentElement.getChild(0)
						.getPosition());
		createEdge(IsSemanticActionOf.EC, semanticAction, list);
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfBinaryTerm.EC, term, list);
		nextTermInVisibilityStage(term);
		isBehindSecondTermInList = true;
	}

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1029_Term4_Rule635_Position3(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				list);
		isBehindSecondTermInList = false;
	}

	/**
	 * Rule 1029: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "*"
	 * -&gt; cf(Term) {cons("iter-star-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"*\" -&gt; Term {cons(\"iter-star-sep\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1029_Position9(StackElement currentElement) {
		leaveVisibilityStage();
	}

	/*
	 * "{" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) "}" "+"
	 * -> Term {cons("iter-sep")}
	 */

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1030_Position0(StackElement currentElement) {
		de.uni_koblenz.edl.preprocessor.schema.term.List list = (de.uni_koblenz.edl.preprocessor.schema.term.List) createVertex(
				de.uni_koblenz.edl.preprocessor.schema.term.List.VC,
				currentElement.getPosition());
		list.set_type(KleeneOperator.PLUS);
		currentElement.setResult(list);
		enterVisibilityStage();
	}

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1030_Term2_Rule627_Position3(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				list);
		Term term = (Term) currentElement.getChild(2).getResult();
		createEdge(IsPartOfBinaryTerm.EC, term, list);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 * Rule 627: cf(SemanticAction*) cf(LAYOUT?) cf(Term) cf(LAYOUT?)
	 * cf(SemanticAction*) -&gt; cf((SemanticAction* Term SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1030_Term2_Rule627_Position5(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		List<SemanticAction> semanticActions = appendAllSemanticActions(currentElement
				.getChild(4).getResult());
		if (semanticActions.isEmpty()) {
			semanticActions.add((SemanticAction) createVertex(
					StatementSemanticAction.VC, currentElement.getChild(4)
							.getPosition()));
		}
		createEdge(IsSemanticActionOf.EC, semanticActions, list);
	}

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1030_Term4_Rule635_Position1(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		SemanticAction semanticAction = (SemanticAction) createVertex(
				StatementSemanticAction.VC, currentElement.getChild(0)
						.getPosition());
		createEdge(IsSemanticActionOf.EC, semanticAction, list);
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfBinaryTerm.EC, term, list);
		nextTermInVisibilityStage(term);
		isBehindSecondTermInList = true;
	}

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1030_Term4_Rule635_Position3(
			StackElement currentElement) {
		Vertex list = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				list);
		isBehindSecondTermInList = false;
	}

	/**
	 * Rule 1030: "{" cf(LAYOUT?) cf((SemanticAction* Term SemanticAction*))
	 * cf(LAYOUT?) cf((Term SemanticAction*)) cf(LAYOUT?) "}" cf(LAYOUT?) "+"
	 * -&gt; cf(Term) {cons("iter-sep"), definedAs(
	 * "\"{\" (SemanticAction* Term SemanticAction*) (Term SemanticAction*) \"}\" \"+\" -&gt; Term {cons(\"iter-sep\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1030_Position7(StackElement currentElement) {
		leaveVisibilityStage();
	}

	/*
	 * Term SemanticAction* "*" -> Term {cons("iter-star")}
	 */

	/**
	 * Rule 1031: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "*" -&gt;
	 * cf(Term) {cons("iter-star"),
	 * definedAs("Term SemanticAction* \"*\" -&gt; Term {cons(\"iter-star\")}")}<br>
	 */
	@Override
	protected void execute_Rule1031_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1031: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "*" -&gt;
	 * cf(Term) {cons("iter-star"),
	 * definedAs("Term SemanticAction* \"*\" -&gt; Term {cons(\"iter-star\")}")}<br>
	 */
	@Override
	protected void execute_Rule1031_Position1(StackElement currentElement) {
		Repetition repetition = (Repetition) createVertex(Repetition.VC,
				currentElement.getPosition());
		repetition.set_type(KleeneOperator.STAR);
		currentElement.setResult(repetition);
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfUnaryTerm.EC, term, repetition);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1031: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "*" -&gt;
	 * cf(Term) {cons("iter-star"),
	 * definedAs("Term SemanticAction* \"*\" -&gt; Term {cons(\"iter-star\")}")}<br>
	 */
	@Override
	protected void execute_Rule1031_Position5(StackElement currentElement) {
		Repetition repetition = (Repetition) currentElement.getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				repetition);
		leaveVisibilityStage();
	}

	/*
	 * Term SemanticAction* "+" -> Term {cons("iter")}
	 */

	/**
	 * Rule 1032: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "+" -&gt;
	 * cf(Term) {cons("iter"),
	 * definedAs("Term SemanticAction* \"+\" -&gt; Term {cons(\"iter\")}")}<br>
	 */
	@Override
	protected void execute_Rule1032_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1032: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "+" -&gt;
	 * cf(Term) {cons("iter"),
	 * definedAs("Term SemanticAction* \"+\" -&gt; Term {cons(\"iter\")}")}<br>
	 */
	@Override
	protected void execute_Rule1032_Position1(StackElement currentElement) {
		Repetition repetition = (Repetition) createVertex(Repetition.VC,
				currentElement.getPosition());
		repetition.set_type(KleeneOperator.PLUS);
		currentElement.setResult(repetition);
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfUnaryTerm.EC, term, repetition);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1032: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "+" -&gt;
	 * cf(Term) {cons("iter"),
	 * definedAs("Term SemanticAction* \"+\" -&gt; Term {cons(\"iter\")}")}<br>
	 */
	@Override
	protected void execute_Rule1032_Position5(StackElement currentElement) {
		Repetition repetition = (Repetition) currentElement.getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				repetition);
		leaveVisibilityStage();
	}

	/*
	 * Term SemanticAction* "?" -> Term {cons("opt")}
	 */

	/**
	 * Rule 1033: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "?" -&gt;
	 * cf(Term) {cons("opt"),
	 * definedAs("Term SemanticAction* \"?\" -&gt; Term {cons(\"opt\")}")}<br>
	 */
	@Override
	protected void execute_Rule1033_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1033: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "?" -&gt;
	 * cf(Term) {cons("opt"),
	 * definedAs("Term SemanticAction* \"?\" -&gt; Term {cons(\"opt\")}")}<br>
	 */
	@Override
	protected void execute_Rule1033_Position1(StackElement currentElement) {
		Vertex option = createVertex(Option.VC, currentElement.getPosition());
		currentElement.setResult(option);
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfUnaryTerm.EC, term, option);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1033: cf(Term) cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) "?" -&gt;
	 * cf(Term) {cons("opt"),
	 * definedAs("Term SemanticAction* \"?\" -&gt; Term {cons(\"opt\")}")}<br>
	 */
	@Override
	protected void execute_Rule1033_Position5(StackElement currentElement) {
		Vertex option = (Vertex) currentElement.getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				option);
		leaveVisibilityStage();
	}

	/*
	 * "(" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ ")" ->
	 * Term {cons("seq")}
	 */

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1034_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1034_Position5(StackElement currentElement) {
		Vertex sequence = createVertex(Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
		Term term = (Term) currentElement.getChild(4).getResult();
		createEdge(IsPartOfSequence.EC, term, sequence);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1034_Position7(StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement.getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(6).getResult()),
				sequence);
	}

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 * Rule 642: cf((Term SemanticAction*)) -&gt; cf((Term SemanticAction*)+)<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1034_Term8_Rule642_Term0_Rule635_Position1(
			StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfSequence.EC, term, sequence);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 * Rule 642: cf((Term SemanticAction*)) -&gt; cf((Term SemanticAction*)+)<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1034_Term8_Rule642_Term0_Rule635_Position3(
			StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
	}

	/**
	 * Rule 1034: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * cf(LAYOUT?) ")" -&gt; cf(Term) {cons("seq"), definedAs(
	 * "\"(\" SemanticAction* Term SemanticAction* (Term SemanticAction*)+ \")\" -&gt; Term {cons(\"seq\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1034_Position11(StackElement currentElement) {
		leaveVisibilityStage();
	}

	/*
	 * "(" SemanticAction* Term SemanticAction* ")" -> Term {bracket}
	 */

	/**
	 * Rule 1036: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) ")" -&gt; cf(Term) {bracket,
	 * definedAs (
	 * "\"(\" SemanticAction* Term SemanticAction* \")\" -&gt; Term {bracket}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1036_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1036: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) ")" -&gt; cf(Term) {bracket,
	 * definedAs (
	 * "\"(\" SemanticAction* Term SemanticAction* \")\" -&gt; Term {bracket}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1036_Position5(StackElement currentElement) {
		Vertex sequence = createVertex(Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
		Term term = (Term) currentElement.getChild(4).getResult();
		createEdge(IsPartOfSequence.EC, term, sequence);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1036: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) cf(Term)
	 * cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) ")" -&gt; cf(Term) {bracket,
	 * definedAs (
	 * "\"(\" SemanticAction* Term SemanticAction* \")\" -&gt; Term {bracket}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1036_Position9(StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement.getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(6).getResult()),
				sequence);
		leaveVisibilityStage();
	}

	/*
	 * "(" SemanticAction* ")" -> Term {cons("empty")}
	 */

	/**
	 * Rule 1035: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) ")" -&gt;
	 * cf(Term) {cons("empty"),
	 * definedAs("\"(\" SemanticAction*  \")\" -&gt; Term {cons(\"empty\")}")}<br>
	 */
	@Override
	protected void execute_Rule1035_Position0(StackElement currentElement) {
		enterVisibilityStage();
	}

	/**
	 * Rule 1035: "(" cf(LAYOUT?) cf(SemanticAction*) cf(LAYOUT?) ")" -&gt;
	 * cf(Term) {cons("empty"),
	 * definedAs("\"(\" SemanticAction*  \")\" -&gt; Term {cons(\"empty\")}")}<br>
	 */
	@Override
	protected void execute_Rule1035_Position5(StackElement currentElement) {
		Vertex sequence = createVertex(Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
		leaveVisibilityStage();
	}

	/*
	 * SemanticAction* -> Terms {cons("empty-terms")}
	 */

	/**
	 * Rule 1037: cf(SemanticAction*) -&gt; cf(Terms) {cons("empty-terms"),
	 * definedAs("SemanticAction* -&gt; Terms {cons(\"empty-terms\")}")}<br>
	 */
	@Override
	protected void execute_Rule1037_Position1(StackElement currentElement) {
		Vertex sequence = createVertex(Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				sequence);
	}

	/*
	 * SemanticAction* (Term SemanticAction*)+ -> Terms {cons("terms")}
	 */

	/**
	 * Rule 1038: cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * -&gt; cf(Terms) {cons("terms"), definedAs(
	 * "SemanticAction* (Term SemanticAction*)+ -&gt; Terms {cons(\"terms\")}")}<br>
	 */
	@Override
	protected void execute_Rule1038_Position1(StackElement currentElement) {
		Vertex sequence = createVertex(Sequence.VC,
				currentElement.getPosition());
		currentElement.setResult(sequence);
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(0).getResult()),
				sequence);
	}

	/**
	 * Rule 1038: cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * -&gt; cf(Terms) {cons("terms"), definedAs(
	 * "SemanticAction* (Term SemanticAction*)+ -&gt; Terms {cons(\"terms\")}")}<br>
	 * Rule 642: cf((Term SemanticAction*)) -&gt; cf((Term SemanticAction*)+)<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1038_Term2_Rule642_Term0_Rule635_Position1(
			StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		Term term = (Term) currentElement.getChild(0).getResult();
		createEdge(IsPartOfSequence.EC, term, sequence);
		nextTermInVisibilityStage(term);
	}

	/**
	 * Rule 1038: cf(SemanticAction*) cf(LAYOUT?) cf((Term SemanticAction*)+)
	 * -&gt; cf(Terms) {cons("terms"), definedAs(
	 * "SemanticAction* (Term SemanticAction*)+ -&gt; Terms {cons(\"terms\")}")}<br>
	 * Rule 642: cf((Term SemanticAction*)) -&gt; cf((Term SemanticAction*)+)<br>
	 * Rule 635: cf(Term) cf(LAYOUT?) cf(SemanticAction*) -&gt; cf((Term
	 * SemanticAction*))<br>
	 */
	@Override
	protected void execute_Rule1038_Term2_Rule642_Term0_Rule635_Position3(
			StackElement currentElement) {
		Vertex sequence = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		createEdge(
				IsSemanticActionOf.EC,
				appendAllSemanticActions(currentElement.getChild(2).getResult()),
				sequence);
	}

	// #####################
	// grammar/basics/JavaIdentifier
	// #####################

	/*
	 * InitialJavaId ("." JavaId)* -> PackageName {cons("PackageName")}
	 */

	/**
	 * Rule 1181: cf(InitialJavaId) cf(LAYOUT?) cf(("." JavaId)*) -&gt;
	 * cf(PackageName) {cons("PackageName"), definedAs(
	 * "InitialJavaId (\".\" JavaId)* -&gt; PackageName {cons(\"PackageName\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1181_Position1(StackElement currentElement) {
		String initialJavaId = currentElement.getChild(0).getLexem();
		currentElement.setResult(initialJavaId);
	}

	/**
	 * Rule 1181: cf(InitialJavaId) cf(LAYOUT?) cf(("." JavaId)*) -&gt;
	 * cf(PackageName) {cons("PackageName"), definedAs(
	 * "InitialJavaId (\".\" JavaId)* -&gt; PackageName {cons(\"PackageName\")}"
	 * )}<br>
	 * Rule 767: cf(("." JavaId)) -&gt; cf(("." JavaId)+)<br>
	 * Rule 760: "." cf(LAYOUT?) cf(JavaId) -&gt; cf(("." JavaId))<br>
	 */
	@Override
	protected void execute_Rule1181_Term2_Rule767_Term0_Rule760_Position3(
			StackElement currentElement) {
		String prefix = (String) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		prefix += "." + currentElement.getChild(2).getLexem();
		currentElement.getParentApplicationOfDefinedRule().setResult(prefix);
	}

	/*
	 * InitialJavaId -> TypeName {cons("TypeName")}
	 */

	/**
	 * Rule 1180: cf(InitialJavaId) -&gt; cf(TypeName) {cons("TypeName"),
	 * definedAs("InitialJavaId -&gt; TypeName {cons(\"TypeName\")}")}<br>
	 */
	@Override
	protected void execute_Rule1180_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getLexem());
	}

	/*
	 * PackageName "." JavaId -> TypeName {cons("TypeName")}
	 */

	/**
	 * Rule 1179: cf(PackageName) cf(LAYOUT?) "." cf(LAYOUT?) cf(JavaId) -&gt;
	 * cf(TypeName) {cons("TypeName"), definedAs(
	 * "PackageName \".\" JavaId -&gt; TypeName {cons(\"TypeName\")}")}<br>
	 */
	@Override
	protected void execute_Rule1179_Position5(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult() + "."
				+ currentElement.getChild(4).getLexem());
	}

	/*
	 * TypeName -> GraphElementClass
	 */

	/**
	 * Rule 1178: cf(TypeName) -&gt; cf(GraphElementClass)
	 * {definedAs("TypeName -&gt; GraphElementClass")}<br>
	 */
	@Override
	protected void execute_Rule1178_Position1(StackElement currentElement) {
		String nameOfSchemaElement = (String) currentElement.getChild(0)
				.getResult();
		currentElement.setResult(getSchemaElementClass(nameOfSchemaElement,
				currentElement));
	}

	// #####################
	// grammar/semantic-actions/SemanticAction
	// #####################

	/*
	 * "#" "#" -> SemanticAction {cons("empty sem-act")}
	 */

	/**
	 * Rule 1041: "#" cf(LAYOUT?) "#" -&gt; cf(SemanticAction)
	 * {cons("empty sem-act"),
	 * definedAs("\"#\" \"#\" -&gt; SemanticAction {cons(\"empty sem-act\")}")}<br>
	 */
	@Override
	protected void execute_Rule1041_Position3(StackElement currentElement) {
		if (!areSemanticActionsAllowed) {
			throw new GrammarException(
					createMessageString(currentElement,
							"There are no semantic actions allowed at the current position."));
		}
		currentElement.setResult(createVertex(StatementSemanticAction.VC,
				currentElement.getPosition()));
	}

	/*
	 * "#" Statement+ "#" -> SemanticAction {cons("sem-act")}
	 */

	/**
	 * Rule 1040: "#" cf(LAYOUT?) cf(Statement+) cf(LAYOUT?) "#" -&gt;
	 * cf(SemanticAction) {cons("sem-act"), definedAs(
	 * "\"#\" Statement+ \"#\" -&gt; SemanticAction {cons(\"sem-act\")}")}<br>
	 */
	@Override
	protected void execute_Rule1040_Position0(StackElement currentElement) {
		currentElement.setResult(createVertex(StatementSemanticAction.VC,
				currentElement.getPosition()));
	}

	/**
	 * Rule 1040: "#" cf(LAYOUT?) cf(Statement+) cf(LAYOUT?) "#" -&gt;
	 * cf(SemanticAction) {cons("sem-act"), definedAs(
	 * "\"#\" Statement+ \"#\" -&gt; SemanticAction {cons(\"sem-act\")}")}<br>
	 * Rule 649: cf(Statement) -&gt; cf(Statement+)<br>
	 */
	@Override
	protected void execute_Rule1040_Term2_Rule649_Position1(
			StackElement currentElement) {
		Object statement = currentElement.getChild(0).getResult();
		if (statement != null) {
			createEdge(IsStatementOf.EC, statement, currentElement
					.getParentApplicationOfDefinedRule().getResult());
		}
	}

	/**
	 * Rule 1040: "#" cf(LAYOUT?) cf(Statement+) cf(LAYOUT?) "#" -&gt;
	 * cf(SemanticAction) {cons("sem-act"), definedAs(
	 * "\"#\" Statement+ \"#\" -&gt; SemanticAction {cons(\"sem-act\")}")}<br>
	 */
	@Override
	protected void execute_Rule1040_Position5(StackElement currentElement) {
		if (!areSemanticActionsAllowed) {
			throw new GrammarException(
					createMessageString(currentElement,
							"There are no semantic actions allowed at the current position."));
		}
	}

	/*
	 * "#" Expression "#" -> ExpressionSemanticAction
	 * {cons("expression sem-act")}
	 */

	/**
	 * Rule 1039: "#" cf(LAYOUT?) cf(Expression) cf(LAYOUT?) "#" -&gt;
	 * cf(ExpressionSemanticAction) {cons("expression sem-act"), definedAs(
	 * "\"#\" Expression \"#\" -&gt; ExpressionSemanticAction {cons(\"expression sem-act\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1039_Position5(StackElement currentElement) {
		Vertex expression = (Vertex) currentElement.getChild(2).getResult();
		Vertex expressionStatement = createVertex(ExpressionSemanticAction.VC,
				currentElement.getPosition());
		currentElement.setResult(expressionStatement);
		if (!expression.isInstanceOf(UserCode.VC)
				|| ((UserCode) expression).getDegree() > 0) {
			createEdge(IsExpressionOfSemanticAction.EC, expression,
					expressionStatement);
		} else {
			deleteTree(expression);
		}
	}

	// #####################
	// grammar/semantic-actions/Statements
	// #####################

	/*
	 * StatementExpression ";" -> Statement {cons("expression statement")}
	 */

	/**
	 * Rule 1043: cf(StatementExpression) cf(LAYOUT?) ";" -&gt; cf(Statement)
	 * {cons("expression statement"), definedAs(
	 * "StatementExpression \";\" -&gt; Statement {cons(\"expression statement\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1043_Position3(StackElement currentElement) {
		Vertex expression = (Vertex) currentElement.getChild(0).getResult();
		if (expression.isInstanceOf(Assignment.VC)
				|| expression.isInstanceOf(MethodCall.VC)
				|| expression.isInstanceOf(DotAccess.VC)
				|| expression.isInstanceOf(ConstructorCall.VC)) {
			Vertex expressionStatement = createVertex(ExpressionStatement.VC,
					currentElement.getPosition());
			currentElement.setResult(expressionStatement);
			createEdge(IsExpressionOfStatement.EC, expression,
					expressionStatement);
		} else if (expression.isInstanceOf(UserCode.VC)) {
			if (((UserCode) expression).getDegree() > 0) {
				currentElement.setResult(expression);
			} else {
				deleteTree(expression);
			}
		} else {
			deleteTree(expression);
		}
	}

	/*
	 * UserCode -> Statement {cons("user-code statement")}
	 */

	/**
	 * Rule 1042: cf(UserCode) -&gt; cf(Statement) {cons("user-code statement"),
	 * definedAs("UserCode -&gt; Statement {cons(\"user-code statement\")}")}<br>
	 */
	@Override
	protected void execute_Rule1042_Position1(StackElement currentElement) {
		Vertex userCode = (Vertex) currentElement.getChild(0).getResult();
		if (userCode.getDegree() > 0) {
			currentElement.setResult(userCode);
		} else {
			deleteTree(userCode);
		}
	}

	// #####################
	// grammar/semantic-actions/UserCode
	// #####################

	/*
	 * ~[\{\}\#]* -> JavaCode
	 */

	/**
	 * Rule 1192: lex([\0-"\$-z\|\~-\255]*) -&gt; lex(JavaCode)
	 * {definedAs("~[\\{\\}\\#]* -&gt; JavaCode")}<br>
	 */
	@Override
	protected void execute_Rule1192_Position1(StackElement currentElement) {
		String content = currentElement.getLexem();
		// if (!content.matches("^\\s*$")) {
		JavaCode javaCode = (JavaCode) createVertex(JavaCode.VC,
				currentElement.getPosition());
		javaCode.set_content(content);
		currentElement.setResult(javaCode);
		// }
	}

	/*
	 * "{" "}" -> UserCode {prefer,cons("empty user-code")}
	 */

	/**
	 * Rule 1190: "{" cf(LAYOUT?) "}" -&gt; cf(UserCode) {prefer,
	 * cons("empty user-code"), definedAs(
	 * "\"{\" \"}\" -&gt; UserCode {prefer,cons(\"empty user-code\")}")}<br>
	 */
	@Override
	protected void execute_Rule1190_Position3(StackElement currentElement) {
		UserCode userCode = (UserCode) createVertex(UserCode.VC,
				currentElement.getPosition());
		userCode.set_containsReturn(false);
		currentElement.setResult(userCode);
	}

	/*
	 * "{" {JavaCode Content}+ "}" -> UserCode {cons("user-code")}
	 */

	/**
	 * Rule 1189: "{" cf(LAYOUT?) cf({JavaCode Content}+) cf(LAYOUT?) "}" -&gt;
	 * cf(UserCode) {cons("user-code"), definedAs(
	 * "\"{\" {JavaCode Content}+ \"}\" -&gt; UserCode {cons(\"user-code\")}")}<br>
	 */
	@Override
	protected void execute_Rule1189_Position0(StackElement currentElement) {
		UserCode userCode = (UserCode) createVertex(UserCode.VC,
				currentElement.getPosition());
		userCode.set_containsReturn(false);
		currentElement.setResult(userCode);
	}

	/**
	 * Rule 1189: "{" cf(LAYOUT?) cf({JavaCode Content}+) cf(LAYOUT?) "}" -&gt;
	 * cf(UserCode) {cons("user-code"), definedAs(
	 * "\"{\" {JavaCode Content}+ \"}\" -&gt; UserCode {cons(\"user-code\")}")}<br>
	 * Rule 774: cf(JavaCode) -&gt; cf({JavaCode Content}+)<br>
	 */
	@Override
	protected void execute_Rule1189_Term2_Rule774_Position1(
			StackElement currentElement) {
		if (currentElement.getChild(0).getResult() != null) {
			JavaCode javaCode = (JavaCode) currentElement.getChild(0)
					.getResult();
			currentElement.setResult(javaCode);
			UserCode userCode = (UserCode) currentElement
					.getParentApplicationOfDefinedRule().getResult();
			boolean containsReturn = containsReturnStatement(javaCode
					.get_content()) || userCode.is_containsReturn();
			userCode.set_containsReturn(containsReturn);
			createEdge(IsJavaCodeOf.EC, javaCode, userCode);
		}
	}

	/**
	 * Rule 1189: "{" cf(LAYOUT?) cf({JavaCode Content}+) cf(LAYOUT?) "}" -&gt;
	 * cf(UserCode) {cons("user-code"), definedAs(
	 * "\"{\" {JavaCode Content}+ \"}\" -&gt; UserCode {cons(\"user-code\")}")}<br>
	 * Rule 773: cf({JavaCode Content}+) cf(LAYOUT?) cf(Content) cf(LAYOUT?)
	 * cf({JavaCode Content}+) -&gt; cf({JavaCode Content}+) {left}<br>
	 */
	@Override
	protected void execute_Rule1189_Term2_Rule773_Position3(
			StackElement currentElement) {
		JavaCode oldJavaCode = (JavaCode) ((ApplicationOfListRule) currentElement)
				.getResultOfLastT1();
		oldJavaCode.set_content(oldJavaCode.get_content()
				+ currentElement.getChild(1).getLexem());
		UserCode userCode = (UserCode) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		Vertex content = (Vertex) currentElement.getChild(2).getResult();
		if (content.isInstanceOf(UserCode.VC)) {
			// flatten tree
			UserCode contentUserCode = (UserCode) content;
			userCode.set_containsReturn(userCode.is_containsReturn()
					|| contentUserCode.is_containsReturn());
			// if (contentUserCode.getDegree(IsContentOf.EC, EdgeDirection.IN) >
			// 0) {
			JavaCode javaCode = (JavaCode) createVertex(JavaCode.VC,
					positionsMap.get(contentUserCode));
			javaCode.set_content("{");
			createEdge(IsJavaCodeOf.EC, javaCode, userCode);

			Edge edge = contentUserCode.getFirstIncidence(IsContentOf.EC,
					EdgeDirection.IN);
			while (edge != null) {
				edge.setOmega(userCode);
				edge = contentUserCode.getFirstIncidence(IsContentOf.EC,
						EdgeDirection.IN);
			}

			javaCode = (JavaCode) createVertex(JavaCode.VC,
					positionsMap.get(contentUserCode));
			javaCode.set_content("}");
			createEdge(IsJavaCodeOf.EC, javaCode, userCode);
			// }
			deleteTree(contentUserCode);
		} else if (content.isInstanceOf(StatementSemanticAction.VC)) {
			StatementSemanticAction stmtSemAct = (StatementSemanticAction) content;
			if (stmtSemAct.getDegree(IsStatementOf.EC, EdgeDirection.IN) == 1) {
				Statement stm = (Statement) stmtSemAct
						.getFirstIsStatementOfIncidence().getThat();
				if (stm.isInstanceOf(UserCode.VC)) {
					UserCode stmUserCode = (UserCode) stm;
					if (stmUserCode.is_containsReturn()) {
						// this stmUserCode was wrongly detected as Statement
						stmtSemAct.getFirstIsStatementOfIncidence().delete();
						deleteTree(stmtSemAct);
						// create ExpressionSemanticAction
						Vertex expressionSemanticAction = createVertex(
								ExpressionSemanticAction.VC, currentElement
										.getChild(2).getPosition());
						createEdge(IsExpressionOfSemanticAction.EC,
								stmUserCode, expressionSemanticAction);
						content = expressionSemanticAction;
					}
				}
			}
			if (!stmtSemAct.isValid()
					|| stmtSemAct.getDegree(IsStatementOf.EC, EdgeDirection.IN) >= 1) {
				createEdge(
						de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsSemanticActionOf.EC,
						content, userCode);
			} else {
				deleteTree(stmtSemAct);
			}
		} else {
			assert content.isInstanceOf(ExpressionSemanticAction.VC);
			if (content.getDegree() > 0) {
				createEdge(
						de.uni_koblenz.edl.preprocessor.schema.semantic_actions.IsSemanticActionOf.EC,
						content, userCode);
			} else {
				deleteTree(content);
			}
		}
	}

	/**
	 * Rule 1189: "{" cf(LAYOUT?) cf({JavaCode Content}+) cf(LAYOUT?) "}" -&gt;
	 * cf(UserCode) {cons("user-code"), definedAs(
	 * "\"{\" {JavaCode Content}+ \"}\" -&gt; UserCode {cons(\"user-code\")}")}<br>
	 * Rule 773: cf({JavaCode Content}+) cf(LAYOUT?) cf(Content) cf(LAYOUT?)
	 * cf({JavaCode Content}+) -&gt; cf({JavaCode Content}+) {left}<br>
	 */
	@Override
	protected void execute_Rule1189_Term2_Rule773_Position5(
			StackElement currentElement) {
		JavaCode oldJavaCode = (JavaCode) ((LinkedList<?>) currentElement
				.getChild(4).getResult()).getLast();
		oldJavaCode.set_content(currentElement.getChild(3).getLexem()
				+ oldJavaCode.get_content());
	}

	/**
	 * Rule 1189: "{" cf(LAYOUT?) cf({JavaCode Content}+) cf(LAYOUT?) "}" -&gt;
	 * cf(UserCode) {cons("user-code"), definedAs(
	 * "\"{\" {JavaCode Content}+ \"}\" -&gt; UserCode {cons(\"user-code\")}")}<br>
	 */
	@Override
	protected void execute_Rule1189_Position5(StackElement currentElement) {
		UserCode userCode = (UserCode) currentElement.getResult();
		JavaCode initialWhiteSpace = (JavaCode) createVertex(JavaCode.VC,
				currentElement.getChild(1).getPosition());
		initialWhiteSpace.set_content(currentElement.getChild(1).getLexem());
		Edge edge = createEdge(IsJavaCodeOf.EC, initialWhiteSpace, userCode);
		Edge first = userCode.getFirstIncidence(EdgeDirection.IN);
		if (first != null) {
			edge.getReversedEdge().putIncidenceBefore(first);
		}
		JavaCode finalWhiteSpace = (JavaCode) createVertex(JavaCode.VC,
				currentElement.getChild(3).getPosition());
		finalWhiteSpace.set_content(currentElement.getChild(3).getLexem());
		createEdge(IsJavaCodeOf.EC, finalWhiteSpace, userCode);
	}

	/*
	 * UserCode -> Content
	 */

	/**
	 * Rule 1188: cf(UserCode) -&gt; cf(Content)
	 * {definedAs("UserCode -&gt; Content")}<br>
	 */
	@Override
	protected void execute_Rule1188_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * SemanticAction -> Content
	 */

	/**
	 * Rule 1187: cf(SemanticAction) -&gt; cf(Content)
	 * {definedAs("SemanticAction -&gt; Content")}<br>
	 */
	@Override
	protected void execute_Rule1187_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * ExpressionSemanticAction -> Content {avoid}
	 */

	/**
	 * Rule 1186: cf(ExpressionSemanticAction) -&gt; cf(Content) {avoid,
	 * definedAs("ExpressionSemanticAction -&gt; Content {avoid}")}<br>
	 */
	@Override
	protected void execute_Rule1186_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	// #####################
	// grammar/semantic-actions/Expressions
	// #####################

	/*
	 * JavaId -> Field {cons("Field")}
	 */

	/**
	 * Rule 1052: cf(JavaId) -&gt; cf(Field) {cons("Field"),
	 * definedAs("JavaId -&gt; Field {cons(\"Field\")}")}<br>
	 */
	@Override
	protected void execute_Rule1052_Position1(StackElement currentElement) {
		Vertex field = createVertex(Field.VC, currentElement.getPosition());
		Identifier identifier = (Identifier) createVertex(Identifier.VC,
				currentElement.getPosition());
		identifier.set_name(currentElement.getChild(0).getLexem());
		createEdge(IsNameOfField.EC, identifier, field);
		currentElement.setResult(field);
	}

	/*
	 * ObjectExpression "=" Expression -> Assignment {cons("assignment")}
	 */

	/**
	 * Rule 1055: cf(ObjectExpression) cf(LAYOUT?) "=" cf(LAYOUT?)
	 * cf(Expression) -&gt; cf(Assignment) {cons("assignment"), definedAs(
	 * "ObjectExpression \"=\" Expression -&gt; Assignment {cons(\"assignment\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1055_Position0(StackElement currentElement) {
		isInLhsOfAssignment = true;
	}

	/**
	 * Rule 1055: cf(ObjectExpression) cf(LAYOUT?) "=" cf(LAYOUT?)
	 * cf(Expression) -&gt; cf(Assignment) {cons("assignment"), definedAs(
	 * "ObjectExpression \"=\" Expression -&gt; Assignment {cons(\"assignment\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1055_Position2(StackElement currentElement) {
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1055: cf(ObjectExpression) cf(LAYOUT?) "=" cf(LAYOUT?)
	 * cf(Expression) -&gt; cf(Assignment) {cons("assignment"), definedAs(
	 * "ObjectExpression \"=\" Expression -&gt; Assignment {cons(\"assignment\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1055_Position5(StackElement currentElement) {
		Vertex assignment = createVertex(Assignment.VC,
				currentElement.getPosition());
		createEdge(IsAssignedElementOf.EC, currentElement.getChild(0)
				.getResult(), assignment);
		createEdge(IsAssignedValueOf.EC,
				currentElement.getChild(4).getResult(), assignment);
		currentElement.setResult(assignment);
	}

	/*
	 * GraphElementClass "(" {Expression ","}* ")" -> ConstructorCall
	 * {cons("constructor call")}
	 */

	/**
	 * Rule 1054: cf(GraphElementClass) cf(LAYOUT?) "(" cf(LAYOUT?)
	 * cf({Expression ","}*) cf(LAYOUT?) ")" -&gt; cf(ConstructorCall)
	 * {cons("constructor call"), definedAs(
	 * "GraphElementClass \"(\" {Expression \",\"}* \")\" -&gt; ConstructorCall {cons(\"constructor call\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1054_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"A constructor call must not be assigned a value."));
		}
		Vertex constructorCall = createVertex(ConstructorCall.VC,
				currentElement.getPosition());
		currentElement.setResult(constructorCall);

		Vertex schemaElement = (Vertex) currentElement.getChild(0).getResult();
		positionsMap.put(schemaElement, currentElement.getPosition());
		if (schemaElement.isInstanceOf(Enumeration.VC)) {
			throw new GrammarException(createMessageString(currentElement,
					"An enumeration domain cannot be instanciated."));
		} else if (schemaElement.isInstanceOf(Record.VC)) {
			createEdge(IsTypeOfCreatedRecord.EC, schemaElement, constructorCall);
			currentElement.setValueOfTemporaryVariable("isRecordDomain",
					Boolean.TRUE);
			currentElement.setValueOfTemporaryVariable("nameOfVertexClass",
					null);
		} else {
			assert schemaElement.isInstanceOf(GraphElementClass.VC);
			GraphElementClass graphElementClass = (GraphElementClass) schemaElement;
			String nameOfGraphElementClass = graphElementClass.get_identifier()
					.get_name();
			AttributedElementClass<?, ?> attributedElementClass = targetSchema
					.getAttributedElementClass(nameOfGraphElementClass);
			if (attributedElementClass.isAbstract()) {
				throw new GrammarException(
						createMessageString(
								currentElement,
								"The abstract "
										+ (attributedElementClass instanceof VertexClass ? "VertexClass"
												: "EdgeClass") + " \""
										+ nameOfGraphElementClass
										+ "\" can't be instanciated."));
			}
			if (attributedElementClass instanceof VertexClass) {
				currentElement.setValueOfTemporaryVariable("nameOfVertexClass",
						nameOfGraphElementClass);
			} else {
				currentElement.setValueOfTemporaryVariable("nameOfVertexClass",
						null);
			}
			currentElement.setValueOfTemporaryVariable("isRecordDomain",
					Boolean.FALSE);
			createEdge(IsTypeOfCreatedGraphElement.EC, schemaElement,
					constructorCall);
		}
	}

	/**
	 * Rule 1054: cf(GraphElementClass) cf(LAYOUT?) "(" cf(LAYOUT?)
	 * cf({Expression ","}*) cf(LAYOUT?) ")" -&gt; cf(ConstructorCall)
	 * {cons("constructor call"), definedAs(
	 * "GraphElementClass \"(\" {Expression \",\"}* \")\" -&gt; ConstructorCall {cons(\"constructor call\")}"
	 * )}<br>
	 * Rule 656: cf(Expression) -&gt; cf({Expression ","}+)<br>
	 */
	@Override
	protected void execute_Rule1054_Term4_Rule656_Position1(
			StackElement currentElement) {
		if (currentElement.getValueOfTemporaryVariable("nameOfVertexClass") != null) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The constructor call of vertex class \""
									+ currentElement
											.getValueOfTemporaryVariable("nameOfVertexClass")
									+ "\" must be called without parameters."));
		}
		Vertex constructorCall = (Vertex) currentElement
				.getParentApplicationOfDefinedRule().getResult();
		int numberOfPreviousParameters = constructorCall.getDegree(
				IsParameterOfConstructor.EC, EdgeDirection.IN);
		if ((Boolean) currentElement
				.getValueOfTemporaryVariable("isRecordDomain")
				&& numberOfPreviousParameters >= 1) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The constructor call of a record domain must not have more than one parameter."));
		} else if (numberOfPreviousParameters >= 2) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The constructor call of an edge class must not have more than two parameters."));
		} else {
			createEdge(IsParameterOfConstructor.EC, currentElement.getChild(0)
					.getResult(), constructorCall);
		}
	}

	/**
	 * Rule 1054: cf(GraphElementClass) cf(LAYOUT?) "(" cf(LAYOUT?)
	 * cf({Expression ","}*) cf(LAYOUT?) ")" -&gt; cf(ConstructorCall)
	 * {cons("constructor call"), definedAs(
	 * "GraphElementClass \"(\" {Expression \",\"}* \")\" -&gt; ConstructorCall {cons(\"constructor call\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1054_Position7(StackElement currentElement) {
		Vertex constructorCall = (Vertex) currentElement.getResult();
		int numberOfPreviousParameters = constructorCall.getDegree(
				IsParameterOfConstructor.EC, EdgeDirection.IN);
		if ((Boolean) currentElement
				.getValueOfTemporaryVariable("isRecordDomain")
				&& numberOfPreviousParameters != 1) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The constructor call of a record domain must not have less than one parameter."));
		} else if (currentElement
				.getValueOfTemporaryVariable("nameOfVertexClass") == null
				&& numberOfPreviousParameters < 2
				&& !(Boolean) currentElement
						.getValueOfTemporaryVariable("isRecordDomain")) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The constructor call of an edge class must not have less than two parameters."));
		}
	}

	/*
	 * MethodName "(" {Expression ","}* ")" -> MethodCall {cons("method")}
	 */

	/**
	 * Rule 1053: cf(MethodName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Expression
	 * ","}*) cf(LAYOUT?) ")" -&gt; cf(MethodCall) {cons("method"), definedAs(
	 * "MethodName \"(\" {Expression \",\"}* \")\" -&gt; MethodCall {cons(\"method\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1053_Position1(StackElement currentElement) {
		String nameOfMethod = currentElement.getChild(0).getLexem();
		if (nameOfMethod.equals("getWhitespaceBefore") && !isContextFree) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The method \""
									+ nameOfMethod
									+ "\" must only be called in context-free rules or start-symbols."));
		}
		if ((nameOfMethod.equals("getPrefixWhitespace") || nameOfMethod
				.equals("getSuffixWhitespace"))
				&& !(isStartSymbol && isContextFree)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The method \""
									+ nameOfMethod
									+ "\" must only be called after context-free start-symbols."));
		}
		Vertex methodCall = createVertex(MethodCall.VC,
				currentElement.getPosition());
		Identifier methodName = (Identifier) createVertex(Identifier.VC,
				currentElement.getChild(0).getPosition());
		methodName.set_name(nameOfMethod);
		createEdge(IsNameOfMethod.EC, methodName, methodCall);
		currentElement.setResult(methodCall);
	}

	/**
	 * Rule 1053: cf(MethodName) cf(LAYOUT?) "(" cf(LAYOUT?) cf({Expression
	 * ","}*) cf(LAYOUT?) ")" -&gt; cf(MethodCall) {cons("method"), definedAs(
	 * "MethodName \"(\" {Expression \",\"}* \")\" -&gt; MethodCall {cons(\"method\")}"
	 * )}<br>
	 * Rule 656: cf(Expression) -&gt; cf({Expression ","}+)<br>
	 */
	@Override
	protected void execute_Rule1053_Term4_Rule656_Position1(
			StackElement currentElement) {
		createEdge(IsParameterOfMethod.EC, currentElement.getChild(0)
				.getResult(), currentElement
				.getParentApplicationOfDefinedRule().getResult());
	}

	/*
	 * ObjectExpression "." MethodCall -> StatementExpression
	 */

	/**
	 * Rule 1076: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?)
	 * cf(MethodCall) -&gt; cf(StatementExpression) {definedAs(
	 * "ObjectExpression \".\" MethodCall -&gt; StatementExpression")}<br>
	 */
	@Override
	protected void execute_Rule1076_Position5(StackElement currentElement) {
		Vertex dotAccess = createVertex(DotAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedElementOf.EC, currentElement.getChild(0)
				.getResult(), dotAccess);
		createEdge(IsAccessedBy.EC, currentElement.getChild(4).getResult(),
				dotAccess);
		currentElement.setResult(dotAccess);
	}

	/*
	 * MethodCall -> StatementExpression
	 */

	/**
	 * Rule 1077: cf(MethodCall) -&gt; cf(StatementExpression)
	 * {definedAs("MethodCall -&gt; StatementExpression")}<br>
	 */
	@Override
	protected void execute_Rule1077_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * ConstructorCall -> StatementExpression
	 */

	/**
	 * Rule 1078: cf(ConstructorCall) -&gt; cf(StatementExpression)
	 * {definedAs("ConstructorCall -&gt; StatementExpression")}<br>
	 */
	@Override
	protected void execute_Rule1078_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * Assignment -> StatementExpression
	 */

	/**
	 * Rule 1079: cf(Assignment) -&gt; cf(StatementExpression)
	 * {definedAs("Assignment -&gt; StatementExpression")}<br>
	 */
	@Override
	protected void execute_Rule1079_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * ObjectExpression "[" Expression "]" -> ObjectExpression
	 * {cons("list element access")}
	 */

	/**
	 * Rule 1056: cf(ObjectExpression) cf(LAYOUT?) "[" cf(LAYOUT?)
	 * cf(Expression) cf(LAYOUT?) "]" -&gt; cf(ObjectExpression)
	 * {cons("list element access"), definedAs(
	 * "ObjectExpression \"[\" Expression \"]\" -&gt; ObjectExpression {cons(\"list element access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1056_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("isInLhsOfAssignment",
				isInLhsOfAssignment);
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1056: cf(ObjectExpression) cf(LAYOUT?) "[" cf(LAYOUT?)
	 * cf(Expression) cf(LAYOUT?) "]" -&gt; cf(ObjectExpression)
	 * {cons("list element access"), definedAs(
	 * "ObjectExpression \"[\" Expression \"]\" -&gt; ObjectExpression {cons(\"list element access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1056_Position7(StackElement currentElement) {
		ListAccess listAccess = (ListAccess) createVertex(ListAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedListOf.EC, currentElement.getChild(0).getResult(),
				listAccess);
		createEdge(IsIndexOfList.EC, currentElement.getChild(4).getResult(),
				listAccess);
		currentElement.setResult(listAccess);
		isInLhsOfAssignment = (Boolean) currentElement
				.getValueOfTemporaryVariable("isInLhsOfAssignment");
	}

	/*
	 * ConstructorCall -> ObjectExpression
	 */

	/**
	 * Rule 1057: cf(ConstructorCall) -&gt; cf(ObjectExpression)
	 * {definedAs("ConstructorCall -&gt; ObjectExpression")}<br>
	 */
	@Override
	protected void execute_Rule1057_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * GraphElementClass "." EnumCons -> ObjectExpression
	 * {prefer,cons("enum access")}
	 */

	/**
	 * Rule 1058: cf(GraphElementClass) cf(LAYOUT?) "." cf(LAYOUT?) cf(EnumCons)
	 * -&gt; cf(ObjectExpression) {prefer, cons("enum access"), definedAs(
	 * "GraphElementClass \".\" EnumCons -&gt; ObjectExpression {prefer,cons(\"enum access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1058_Position5(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Enumeration constants must not be assigned a value."));
		}
		Vertex enumAccess = createVertex(EnumAccess.VC,
				currentElement.getPosition());
		currentElement.setResult(enumAccess);
		String enumConstantName = currentElement.getChild(4).getLexem();
		Vertex schemaElement = (Vertex) currentElement.getChild(0).getResult();
		if (!schemaElement.isInstanceOf(Enumeration.VC)) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"The enumeration constant \""
									+ enumConstantName
									+ "\" is not accessible at "
									+ (schemaElement.isInstanceOf(Record.VC) ? "RecordDomain"
											: "GraphElementClass") + " \""
									+ currentElement.getChild(0).getLexem()
									+ "\"."));
		}
		Enumeration enumeration = (Enumeration) schemaElement;
		checkExistenceOfField(currentElement, enumeration, enumConstantName);
		EnumConstant enumConstant = (EnumConstant) enumerationConstantsTable
				.use(enumeration.get_identifier().get_name() + "."
						+ enumConstantName);
		if (enumConstant == null) {
			enumConstant = (EnumConstant) createVertex(EnumConstant.VC,
					currentElement.getChild(4).getPosition());
			enumerationConstantsTable.declare(enumeration.get_identifier()
					.get_name() + "." + enumConstantName, enumConstant);
		}
		enumConstant.set_value(enumConstantName);

		if (enumConstant.getFirstIsConstantOfIncidence() == null) {
			createEdge(IsConstantOf.EC, enumConstant, enumeration);
		}
		createEdge(IsAccessedEnumConstant.EC, enumAccess, enumConstant);
	}

	private void checkExistenceOfField(StackElement currentElement,
			Enumeration enumeration, String enumConstantName) {
		EnumDomain enumDomain = (EnumDomain) targetSchema
				.getNamedElement(enumeration.get_identifier().get_name());
		boolean wasEnumConsFound = false;
		for (String enumCons : enumDomain.getConsts()) {
			if (enumCons.equals(enumConstantName)) {
				wasEnumConsFound = true;
				break;
			}
		}
		if (!wasEnumConsFound) {
			throw new GrammarException(createMessageString(currentElement,
					"The enum domain \"" + enumDomain.getQualifiedName()
							+ "\" does not have an enum constant with name \""
							+ enumConstantName + "\"."));
		}
	}

	/*
	 * ObjectExpression "." "omega" -> ObjectExpression {cons("omega access")}
	 */

	/**
	 * Rule 1059: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "omega" -&gt;
	 * cf(ObjectExpression) {cons("omega access"), definedAs(
	 * "ObjectExpression \".\" \"omega\" -&gt; ObjectExpression {cons(\"omega access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1059_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("isInLhsOfAssignment",
				isInLhsOfAssignment);
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1059: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "omega" -&gt;
	 * cf(ObjectExpression) {cons("omega access"), definedAs(
	 * "ObjectExpression \".\" \"omega\" -&gt; ObjectExpression {cons(\"omega access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1059_Position3(StackElement currentElement) {
		isInLhsOfAssignment = (Boolean) currentElement
				.getValueOfTemporaryVariable("isInLhsOfAssignment");
	}

	/**
	 * Rule 1059: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "omega" -&gt;
	 * cf(ObjectExpression) {cons("omega access"), definedAs(
	 * "ObjectExpression \".\" \"omega\" -&gt; ObjectExpression {cons(\"omega access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1059_Position5(StackElement currentElement) {
		if (omegaConstant == null) {
			omegaConstant = createVertex(OmegaConstant.VC, currentElement
					.getChild(4).getPosition());
		} else {
			positionsMap.put(omegaConstant, currentElement.getChild(4)
					.getPosition());
		}

		Vertex dotAccess = createVertex(DotAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedElementOf.EC, currentElement.getChild(0)
				.getResult(), dotAccess);
		createEdge(IsAccessedBy.EC, omegaConstant, dotAccess);
		currentElement.setResult(dotAccess);
	}

	/*
	 * ObjectExpression "." "alpha" -> ObjectExpression {cons("alpha access")}
	 */

	/**
	 * Rule 1060: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "alpha" -&gt;
	 * cf(ObjectExpression) {cons("alpha access"), definedAs(
	 * "ObjectExpression \".\" \"alpha\" -&gt; ObjectExpression {cons(\"alpha access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1060_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("isInLhsOfAssignment",
				isInLhsOfAssignment);
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1060: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "alpha" -&gt;
	 * cf(ObjectExpression) {cons("alpha access"), definedAs(
	 * "ObjectExpression \".\" \"alpha\" -&gt; ObjectExpression {cons(\"alpha access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1060_Position3(StackElement currentElement) {
		isInLhsOfAssignment = (Boolean) currentElement
				.getValueOfTemporaryVariable("isInLhsOfAssignment");
	}

	/**
	 * Rule 1060: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) "alpha" -&gt;
	 * cf(ObjectExpression) {cons("alpha access"), definedAs(
	 * "ObjectExpression \".\" \"alpha\" -&gt; ObjectExpression {cons(\"alpha access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1060_Position5(StackElement currentElement) {
		if (alphaConstant == null) {
			alphaConstant = createVertex(AlphaConstant.VC, currentElement
					.getChild(4).getPosition());
		} else {
			positionsMap.put(alphaConstant, currentElement.getChild(4)
					.getPosition());
		}

		Vertex dotAccess = createVertex(DotAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedElementOf.EC, currentElement.getChild(0)
				.getResult(), dotAccess);
		createEdge(IsAccessedBy.EC, alphaConstant, dotAccess);
		currentElement.setResult(dotAccess);
	}

	/*
	 * ObjectExpression "." Field -> ObjectExpression {cons("field access")}
	 */

	/**
	 * Rule 1061: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) cf(Field)
	 * -&gt; cf(ObjectExpression) {cons("field access"), definedAs(
	 * "ObjectExpression \".\" Field -&gt; ObjectExpression {cons(\"field access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1061_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("isInLhsOfAssignment",
				isInLhsOfAssignment);
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1061: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) cf(Field)
	 * -&gt; cf(ObjectExpression) {cons("field access"), definedAs(
	 * "ObjectExpression \".\" Field -&gt; ObjectExpression {cons(\"field access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1061_Position3(StackElement currentElement) {
		isInLhsOfAssignment = (Boolean) currentElement
				.getValueOfTemporaryVariable("isInLhsOfAssignment");
	}

	/**
	 * Rule 1061: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?) cf(Field)
	 * -&gt; cf(ObjectExpression) {cons("field access"), definedAs(
	 * "ObjectExpression \".\" Field -&gt; ObjectExpression {cons(\"field access\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1061_Position5(StackElement currentElement) {
		Vertex dotAccess = createVertex(DotAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedElementOf.EC, currentElement.getChild(0)
				.getResult(), dotAccess);
		createEdge(IsAccessedBy.EC, currentElement.getChild(4).getResult(),
				dotAccess);
		currentElement.setResult(dotAccess);
	}

	/*
	 * ObjectExpression "." MethodCall -> ObjectExpression
	 * {cons("dynamic method call")}
	 */

	/**
	 * Rule 1062: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?)
	 * cf(MethodCall) -&gt; cf(ObjectExpression) {cons("dynamic method call"),
	 * definedAs(
	 * "ObjectExpression \".\" MethodCall -&gt; ObjectExpression {cons(\"dynamic method call\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1062_Position0(StackElement currentElement) {
		currentElement.setValueOfTemporaryVariable("isInLhsOfAssignment",
				isInLhsOfAssignment);
		isInLhsOfAssignment = false;
	}

	/**
	 * Rule 1062: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?)
	 * cf(MethodCall) -&gt; cf(ObjectExpression) {cons("dynamic method call"),
	 * definedAs(
	 * "ObjectExpression \".\" MethodCall -&gt; ObjectExpression {cons(\"dynamic method call\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1062_Position3(StackElement currentElement) {
		isInLhsOfAssignment = (Boolean) currentElement
				.getValueOfTemporaryVariable("isInLhsOfAssignment");
	}

	/**
	 * Rule 1062: cf(ObjectExpression) cf(LAYOUT?) "." cf(LAYOUT?)
	 * cf(MethodCall) -&gt; cf(ObjectExpression) {cons("dynamic method call"),
	 * definedAs(
	 * "ObjectExpression \".\" MethodCall -&gt; ObjectExpression {cons(\"dynamic method call\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1062_Position5(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Method calls must not be assigned a value."));
		}
		Vertex dotAccess = createVertex(DotAccess.VC,
				currentElement.getPosition());
		createEdge(IsAccessedElementOf.EC, currentElement.getChild(0)
				.getResult(), dotAccess);
		createEdge(IsAccessedBy.EC, currentElement.getChild(4).getResult(),
				dotAccess);
		currentElement.setResult(dotAccess);
	}

	/*
	 * MethodCall -> ObjectExpression {cons("method call")}
	 */

	/**
	 * Rule 1063: cf(MethodCall) -&gt; cf(ObjectExpression)
	 * {cons("method call"),
	 * definedAs("MethodCall -&gt; ObjectExpression {cons(\"method call\")}")}<br>
	 */
	@Override
	protected void execute_Rule1063_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Method calls must not be assigned a value."));
		}
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * Var -> ObjectExpression {cons("variable")}
	 */

	/**
	 * Rule 1064: cf(Var) -&gt; cf(ObjectExpression) {cons("variable"),
	 * definedAs("Var -&gt; ObjectExpression {cons(\"variable\")}")}<br>
	 */
	@Override
	protected void execute_Rule1064_Position1(StackElement currentElement) {
		Vertex variableAccess = createVertex(VariableAccess.VC,
				currentElement.getPosition());
		currentElement.setResult(variableAccess);
		createEdge(IsAccessedVariableOf.EC, currentElement.getChild(0)
				.getResult(), variableAccess);
	}

	/*
	 * UserCode -> ObjectExpression {cons("user-code Exp")}
	 */

	/**
	 * Rule 1065: cf(UserCode) -&gt; cf(ObjectExpression)
	 * {cons("user-code Exp"),
	 * definedAs("UserCode -&gt; ObjectExpression {cons(\"user-code Exp\")}")}<br>
	 */
	@Override
	protected void execute_Rule1065_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"UserCode must not be assigned a value."));
		}
		UserCode usercode = (UserCode) currentElement.getChild(0).getResult();
		if (!usercode.is_containsReturn()) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"User specific code which is used as an expression must contain a return statement."));
		}
		currentElement.setResult(usercode);
	}

	/*
	 * "omega" -> ObjectExpression {cons("omega")}
	 */

	/**
	 * Rule 1066: "omega" -&gt; cf(ObjectExpression) {cons("omega"),
	 * definedAs("\"omega\" -&gt; ObjectExpression {cons(\"omega\")}")}<br>
	 */
	@Override
	protected void execute_Rule1066_Position1(StackElement currentElement) {
		if (!isInDefaultValues) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The use of omega is restricted to default values sections or after a \".\"."));
		}
		if (isInDefaultValues && !isDefaultValueOfEdgeClass) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The use of omega is restricted to default values of EdgeClasses."));
		}
		if (omegaConstant == null) {
			omegaConstant = createVertex(OmegaConstant.VC, currentElement
					.getChild(0).getPosition());
		} else {
			positionsMap.put(omegaConstant, currentElement.getChild(0)
					.getPosition());
		}
		currentElement.setResult(omegaConstant);
	}

	/*
	 * "alpha" -> ObjectExpression {cons("alpha")}
	 */

	/**
	 * Rule 1067: "alpha" -&gt; cf(ObjectExpression) {cons("alpha"),
	 * definedAs("\"alpha\" -&gt; ObjectExpression {cons(\"alpha\")}")}<br>
	 */
	@Override
	protected void execute_Rule1067_Position1(StackElement currentElement) {
		if (!isInDefaultValues) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The use of alpha is restricted to default values sections or after a \".\"."));
		}
		if (isInDefaultValues && !isDefaultValueOfEdgeClass) {
			throw new GrammarException(
					createMessageString(currentElement,
							"The use of alpha is restricted to default values of EdgeClasses."));
		}
		if (alphaConstant == null) {
			alphaConstant = createVertex(AlphaConstant.VC, currentElement
					.getChild(0).getPosition());
		} else {
			positionsMap.put(alphaConstant, currentElement.getChild(0)
					.getPosition());
		}
		currentElement.setResult(alphaConstant);
	}

	/*
	 * Assignment -> Expression
	 */

	/**
	 * Rule 1068: cf(Assignment) -&gt; cf(Expression)
	 * {definedAs("Assignment -&gt; Expression")}<br>
	 */
	@Override
	protected void execute_Rule1068_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * StringLiteral -> Expression {cons("string")}
	 */

	/**
	 * Rule 1069: cf(StringLiteral) -&gt; cf(Expression) {cons("string"),
	 * definedAs("StringLiteral -&gt; Expression {cons(\"string\")}")}<br>
	 */
	@Override
	protected void execute_Rule1069_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Literals must not be assigned a value."));
		}
		StringLiteral stringLiteral = (StringLiteral) createVertex(
				StringLiteral.VC, currentElement.getPosition());
		String stringContent = currentElement.getChild(0).getLexem();
		stringLiteral.set_value(stringContent.substring(1,
				stringContent.length() - 1));
		currentElement.setResult(stringLiteral);
	}

	/*
	 * FloatLiteral -> Expression {cons("double")}
	 */

	/**
	 * Rule 1070: cf(FloatLiteral) -&gt; cf(Expression) {cons("double"),
	 * definedAs("FloatLiteral -&gt; Expression {cons(\"double\")}")}<br>
	 */
	@Override
	protected void execute_Rule1070_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Literals must not be assigned a value."));
		}
		String numberString = currentElement.getChild(0).getLexem()
				.replaceAll("\\s+", "").toLowerCase();
		if (numberString.endsWith("f") || numberString.endsWith("d")) {
			numberString = numberString.substring(0, numberString.length() - 1);
		}
		DoubleLiteral doubleLiteral = (DoubleLiteral) createVertex(
				DoubleLiteral.VC, currentElement.getPosition());
		doubleLiteral.set_value(Double.parseDouble(numberString));
		currentElement.setResult(doubleLiteral);
	}

	/*
	 * IntLiteral -> Expression {cons("int")}
	 */

	/**
	 * Rule 1071: cf(IntLiteral) -&gt; cf(Expression) {cons("int"),
	 * definedAs("IntLiteral -&gt; Expression {cons(\"int\")}")}<br>
	 */
	@Override
	protected void execute_Rule1071_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"Literals must not be assigned a value."));
		}
		String numberString = currentElement.getChild(0).getLexem()
				.replaceAll("\\s+", "").toLowerCase();
		try {
			int intValue = Integer.decode(numberString);
			IntegerLiteral integerLiteral = (IntegerLiteral) createVertex(
					IntegerLiteral.VC, currentElement.getPosition());
			integerLiteral.set_value(intValue);
			currentElement.setResult(integerLiteral);
		} catch (NumberFormatException e) {
			if (numberString.endsWith("l")) {
				numberString = numberString.substring(0,
						numberString.length() - 1);
			}
			long longValue = Long.decode(numberString);
			LongLiteral longLiteral = (LongLiteral) createVertex(
					LongLiteral.VC, currentElement.getPosition());
			longLiteral.set_value(longValue);
			currentElement.setResult(longLiteral);
		}
	}

	/*
	 * "false" -> Expression {cons("false")}
	 */

	/**
	 * Rule 1072: "false" -&gt; cf(Expression) {cons("false"),
	 * definedAs("\"false\" -&gt; Expression {cons(\"false\")}")}<br>
	 */
	@Override
	protected void execute_Rule1072_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"\"false\" must not be assigned a value."));
		}
		if (falseLiteral == null) {
			falseLiteral = createVertex(BooleanLiteral.VC, currentElement
					.getChild(0).getPosition());
			((BooleanLiteral) falseLiteral).set_value(false);
		} else {
			positionsMap.put(falseLiteral, currentElement.getChild(0)
					.getPosition());
		}
		currentElement.setResult(falseLiteral);
	}

	/*
	 * "true" -> Expression {cons("true")}
	 */

	/**
	 * Rule 1073: "true" -&gt; cf(Expression) {cons("true"),
	 * definedAs("\"true\" -&gt; Expression {cons(\"true\")}")}<br>
	 */
	@Override
	protected void execute_Rule1073_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"\"true\" must not be assigned a value."));
		}
		if (trueLiteral == null) {
			trueLiteral = createVertex(BooleanLiteral.VC, currentElement
					.getChild(0).getPosition());
			((BooleanLiteral) trueLiteral).set_value(true);
		} else {
			positionsMap.put(trueLiteral, currentElement.getChild(0)
					.getPosition());
		}
		currentElement.setResult(trueLiteral);
	}

	/*
	 * "null" -> Expression {cons("null")}
	 */

	/**
	 * Rule 1074: "null" -&gt; cf(Expression) {cons("null"),
	 * definedAs("\"null\" -&gt; Expression {cons(\"null\")}")}<br>
	 */
	@Override
	protected void execute_Rule1074_Position1(StackElement currentElement) {
		if (isInLhsOfAssignment) {
			throw new GrammarException(createMessageString(currentElement,
					"\"null\" must not be assigned a value."));
		}
		if (nullLiteral == null) {
			nullLiteral = createVertex(NullLiteral.VC,
					currentElement.getChild(0).getPosition());
		} else {
			positionsMap.put(nullLiteral, currentElement.getChild(0)
					.getPosition());
		}
		currentElement.setResult(nullLiteral);
	}

	/*
	 * ObjectExpression -> Expression
	 */

	/**
	 * Rule 1075: cf(ObjectExpression) -&gt; cf(Expression)
	 * {definedAs("ObjectExpression -&gt; Expression")}<br>
	 */
	@Override
	protected void execute_Rule1075_Position1(StackElement currentElement) {
		currentElement.setResult(currentElement.getChild(0).getResult());
	}

	/*
	 * STableName -> Var {avoid,cons("symbol table or user defined var")}
	 */

	/**
	 * Rule 1082: cf(STableName) -&gt; cf(Var) {avoid,
	 * cons("symbol table or user defined var"), definedAs(
	 * "STableName -&gt; Var {avoid,cons(\"symbol table or user defined var\")}"
	 * )}<br>
	 */
	@Override
	protected void execute_Rule1082_Position1(StackElement currentElement) {
		if (isInDefaultValues) {
			throw new GrammarException(createMessageString(currentElement,
					"Variables are not allowed in default velues sections."));
		}
		if (isInLhsOfAssignment) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"A symbol table must not be assigned a value."
									+ " Perhaps you forgot a $ in front of the name of a temporary variable."));
		}
		String symbolTableName = currentElement.getChild(0).getLexem();
		Vertex symbolTableDefinition = symbolTableTable.use(symbolTableName);
		if (symbolTableDefinition == null) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"Symbol table \""
									+ symbolTableName
									+ "\" is undefined."
									+ " Perhaps you forgot a $ in front of the name of a temporary variable."));
		}

		Vertex symbolTableVariable = createVertex(SymbolTableVariable.VC,
				currentElement.getPosition());
		createEdge(ReferencesSymbolTable.EC, symbolTableVariable,
				symbolTableDefinition);
		currentElement.setResult(symbolTableVariable);
	}

	/*
	 * TempVar -> Var {cons("var")}
	 */

	/**
	 * Rule 1083: cf(TempVar) -&gt; cf(Var) {cons("var"),
	 * definedAs("TempVar -&gt; Var {cons(\"var\")}")}<br>
	 */
	@Override
	protected void execute_Rule1083_Position1(StackElement currentElement) {
		if (isInDefaultValues) {
			throw new GrammarException(createMessageString(currentElement,
					"Variables are not allowed in default velues sections."));
		}
		String varName = currentElement.getChild(0).getLexem().substring(1);
		Term labeledTerm = labelsInVisibilityStage.peek().get(varName);
		if (labeledTerm != null) {
			if (currentPattern != null) {
				throw new GrammarException(createMessageString(currentElement,
						"References to labeled terms are not allowed in patterns. Concerning label: \""
								+ varName + "\"."));
			}
			// a label with this name is defined in the current visibility stage
			// possibly defined temporary variables are hidden
			int indexOfLabeldTerm = termsInVisibilityStage.peek().indexOf(
					labeledTerm);
			currentElement.setResult(createBodyVariable(currentElement,
					labeledTerm, indexOfLabeldTerm));
		} else {
			// this is a temporary variable
			Vertex temporaryVariable = tempVarTable.getTop().getMap()
					.get(varName);
			if (temporaryVariable == null) {
				temporaryVariable = tempVarTable.use(varName);
				if (temporaryVariable == null) {
					temporaryVariable = tempVarTable.declare(
							varName,
							createVertex(TemporaryVariable.VC,
									currentElement.getPosition()));
				}
			}
			positionsMap.put(temporaryVariable, currentElement.getPosition());
			currentElement.setResult(temporaryVariable);
			if (temporaryVariable
					.getFirstIncidence(IsNameOfTemporaryVariable.EC) == null) {
				Identifier identifier = (Identifier) createVertex(
						Identifier.VC, currentElement.getPosition());
				identifier.set_name(varName);
				createEdge(IsNameOfTemporaryVariable.EC, identifier,
						temporaryVariable);
			}
			namesOfUsedTempVarsInVisibilityStage.peek().add(varName);
		}
	}

	/*
	 * BodyVar -> Var {cons("body var")}
	 */

	/**
	 * Rule 1084: cf(BodyVar) -&gt; cf(Var) {cons("body var"),
	 * definedAs("BodyVar -&gt; Var {cons(\"body var\")}")}<br>
	 */
	@Override
	protected void execute_Rule1084_Position1(StackElement currentElement) {
		if (isInDefaultValues) {
			throw new GrammarException(createMessageString(currentElement,
					"Variables are not allowed in default velues sections."));
		}
		int indexOfTerm = Integer.parseInt(currentElement.getChild(0)
				.getLexem().substring(1));
		int maxTermsInStage = getMaximumIndexOfTermsInCurrentVisibiltyStage();
		if (indexOfTerm > maxTermsInStage) {
			throw new GrammarException(
					createMessageString(
							currentElement,
							"There does not exist a term with index "
									+ indexOfTerm
									+ " in the current visibilty stage."
									+ " At the current parsing step the last maximal term has the index "
									+ maxTermsInStage));
		}
		PatternTerm referencedTerm = currentPattern != null ? null
				: termsInVisibilityStage.peek().get(indexOfTerm);
		currentElement.setResult(createBodyVariable(currentElement,
				referencedTerm, indexOfTerm));
	}

	/*
	 * HeadVar -> Var {cons("head var")}
	 */

	/**
	 * Rule 1085: cf(HeadVar) -&gt; cf(Var) {cons("head var"),
	 * definedAs("HeadVar -&gt; Var {cons(\"head var\")}")}<br>
	 */
	@Override
	protected void execute_Rule1085_Position1(StackElement currentElement) {
		if (isInDefaultValues) {
			throw new GrammarException(createMessageString(currentElement,
					"Variables are not allowed in default velues sections."));
		}
		if (headVariable == null) {
			headVariable = createVertex(HeadVariable.VC,
					currentElement.getPosition());
		} else {
			positionsMap.put(headVariable, currentElement.getPosition());
		}
		currentElement.setResult(headVariable);
	}
}
