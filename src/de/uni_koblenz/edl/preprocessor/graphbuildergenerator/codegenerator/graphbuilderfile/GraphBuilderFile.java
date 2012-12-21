package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.MultipleAppendable;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLEdge;
import de.uni_koblenz.edl.preprocessor.schema.common.EDLVertex;
import de.uni_koblenz.edl.preprocessor.schema.common.GraphElementClass;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.Element;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.IsDefaultStatementOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsElementIn;
import de.uni_koblenz.edl.preprocessor.schema.section.IsJavaImportOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsPersistentEdgeClassOf;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.IslandStart;
import de.uni_koblenz.edl.preprocessor.schema.section.JavaImport;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Assignment;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccess;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.DotAccessible;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Expression;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ExpressionStatement;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.Field;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.JavaCode;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.StatementSemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.UserCode;
import de.uni_koblenz.edl.preprocessor.schema.term.BinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterClass;
import de.uni_koblenz.edl.preprocessor.schema.term.FileStart;
import de.uni_koblenz.edl.preprocessor.schema.term.Function;
import de.uni_koblenz.edl.preprocessor.schema.term.IsAttachedBy;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.term.LAYOUT;
import de.uni_koblenz.edl.preprocessor.schema.term.Label;
import de.uni_koblenz.edl.preprocessor.schema.term.Literal;
import de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction;
import de.uni_koblenz.edl.preprocessor.schema.term.Sequence;
import de.uni_koblenz.edl.preprocessor.schema.term.Sort;
import de.uni_koblenz.edl.preprocessor.schema.term.Start;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GraphBuilderFile {

	private final String className;

	private final String parseTableName;

	private String pathOfSchema;

	private Schema schema;

	private final Map<EDLVertex, Module> vertex2Module = new HashMap<EDLVertex, Module>();

	private final SemanticActionGenerator semanticActionGenerator;

	public GraphBuilderFile(String packagePrefix, String className,
			String parseTableName, Map<String, Set<Rule>> head2rules) {
		this.packagePrefix = packagePrefix;
		this.className = className + "Builder";
		if (!parseTableName.toLowerCase().endsWith(".tbl")) {
			parseTableName += ".tbl";
		}
		this.parseTableName = parseTableName;
		this.head2rules = head2rules;
		semanticActionGenerator = new SemanticActionGenerator(
				symbolTable2fieldname);
		addImport("de.uni_koblenz.edl.GraphBuilderBaseImpl");
		addImport("de.uni_koblenz.edl.parser.stack.Stack");
		addImport("de.uni_koblenz.jgralab.Vertex");
		addImport("de.uni_koblenz.jgralab.Edge");
	}

	public void setSchemaFile(String pathOfSchema) {
		// add imports for constructor parameter
		addImport("de.uni_koblenz.jgralab.schema.Schema");
		addImport("de.uni_koblenz.jgralab.Graph");
		this.pathOfSchema = pathOfSchema;
		try {
			schema = GraphBuilderBaseImpl
					.instantiateSchema(GraphBuilderBaseImpl
							.loadSchema(pathOfSchema));
			semanticActionGenerator.setSchema(schema);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSchema(Schema schema) {
		// add imports for constructor parameter
		addImport("de.uni_koblenz.jgralab.schema.Schema");
		addImport("de.uni_koblenz.jgralab.Graph");
		this.schema = schema;
		semanticActionGenerator.setSchema(schema);
	}

	public String createFile(String outputPath, Appendable... appendables)
			throws IOException {
		if (!outputPath.endsWith("/") && !outputPath.endsWith("\\")) {
			outputPath = outputPath + File.separator;
		}
		BufferedWriter bw = null;
		try {
			File file = new File(outputPath + className + ".java");
			bw = new BufferedWriter(new FileWriter(file));

			Appendable output = bw;
			if (appendables.length > 0) {
				Appendable[] apps = new Appendable[appendables.length + 1];
				apps[0] = bw;
				for (int i = 0; i < appendables.length; i++) {
					apps[i + 1] = appendables[i];
				}
				output = new MultipleAppendable(apps);
			}

			createFileContent(output);
			return file.getAbsolutePath();
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createFileContent(Appendable appendable) throws IOException {
		createPackagePrefix(appendable);

		createImports(appendable);

		appendable.append("\npublic class ").append(className)
				.append(" extends GraphBuilderBaseImpl{\n");

		createSymbolTableDefinitions(appendable);

		createMainMethod(appendable);

		createConstructors(appendable);

		createActivateIslandGrammarMethod(appendable);

		createUserCodeDeclaration(appendable);

		createDefaultValuesSection(appendable);

		createExecuteMethods(appendable);

		semanticActionGenerator.createSemanticActionsMethods(appendable);

		appendable.append("}");
	}

	private void createMainMethod(Appendable appendable) throws IOException {
		boolean usesSchema = (schema != null) || (pathOfSchema != null);

		appendable
				.append("\n\tpublic static void main(String[] args) throws Exception {\n");

		if (usesSchema) {
			appendable.append("\t\tSchema schema = instantiateSchema(");
			if (pathOfSchema != null) {
				appendable.append("loadSchema(")
						.append(GraphIO.toUtfString(pathOfSchema)).append(")");
			} else {
				appendable.append("getSchemaClass(\"")
						.append(schema.getQualifiedName()).append("\")");
			}
			appendable.append(");\n");
		}

		appendable.append("\t\t").append(className)
				.append(" graphBuilder = new ").append(className).append("(");
		if (usesSchema) {
			appendable.append("schema");
		}
		appendable.append(");\n");

		if (!islandDefinitions.isEmpty()) {
			appendable.append("\t\tgraphBuilder.activateIslandGrammar();\n");
		}

		appendable.append("\t\tprocessCommandLineOptions(args, graphBuilder, ")
				.append(usesSchema ? "true" : "false").append(");\n");

		appendable.append("\t}\n");
	}

	private void createConstructors(Appendable appendable) throws IOException {
		if ((schema == null) && (pathOfSchema == null)) {
			// create default constructor
			appendable.append("\n\tpublic ").append(className).append("() {\n");
			appendable.append("\t\tsuper(\"").append(parseTableName)
					.append("\");\n");
			appendable.append("\t}\n");
		} else {
			appendable.append("\n\tpublic ").append(className)
					.append("(Schema schema) {\n");
			appendable.append("\t\tsuper(\"").append(parseTableName)
					.append("\", schema);\n");
			appendable.append("\t}\n");

			appendable.append("\n\tpublic ").append(className)
					.append("(Graph graph) {\n");
			appendable.append("\t\tsuper(\"").append(parseTableName)
					.append("\", graph);\n");
			appendable.append("\t}\n");
		}
	}

	/*
	 * Following methods concern package prefix
	 */

	private final String packagePrefix;

	private void createPackagePrefix(Appendable appendable) throws IOException {
		appendable.append("package ").append(packagePrefix).append(";\n");
	}

	/*
	 * Following methods concern imports
	 */

	private final Set<String> importDeclarations = new HashSet<String>();

	private final Set<String> definedImportDeclarations = new HashSet<String>();

	public void addImport(String importString) {
		importString = importString.trim();
		if (importString.endsWith(";")) {
			importString = importString.substring(0, importString.length() - 1)
					.trim();
		}
		if (!definedImportDeclarations.contains(importString)) {
			importDeclarations.add(importString);
		}
	}

	public void addImport(ImportDeclarations importDeclarations) {
		for (IsJavaImportOf ijio : importDeclarations
				.getIsJavaImportOfIncidences(EdgeDirection.IN)) {
			JavaImport javaImport = ijio.getAlpha();
			String importString = (javaImport.is_isStatic() ? "static " : "")
					+ javaImport.get_importDeclaration();
			importString = importString.trim();
			if (importString.endsWith(";")) {
				importString = importString.substring(0,
						importString.length() - 1).trim();
			}
			definedImportDeclarations.add(importString);
			if (this.importDeclarations.contains(importString)) {
				this.importDeclarations.remove(importString);
			}
		}
	}

	private void createImports(Appendable appendable) throws IOException {
		appendable.append("\n");
		appendAllImports(appendable, importDeclarations);
		if (!definedImportDeclarations.isEmpty()) {
			appendable.append("\n");
			appendable
					.append("// following imports were defined in an \"import declarations\" sections\n");
			appendAllImports(appendable, definedImportDeclarations);
		}
	}

	private void appendAllImports(Appendable appendable, Set<String> imports)
			throws IOException {
		for (String importString : imports) {
			appendable.append("import ").append(importString).append(";\n");
		}
	}

	/*
	 * Following methods concern symbol table sections
	 */

	private final List<StringBuilder> symbolTableDefinitions = new ArrayList<StringBuilder>();

	private int symbolTableNumber = 0;

	private final Map<SymbolTableDefinition, String> symbolTable2fieldname = new HashMap<SymbolTableDefinition, String>();

	private String getUniqueSymbolTableIdentifier(
			SymbolTableDefinition symbolTable) {
		String symbolTableIdentifier = symbolTable2fieldname.get(symbolTable);
		if (symbolTableIdentifier == null) {
			symbolTableIdentifier = symbolTable.get_identifier().get_name()
					+ "_" + symbolTableNumber++;
			symbolTable2fieldname.put(symbolTable, symbolTableIdentifier);
		}
		return symbolTableIdentifier;
	}

	public void addSymbolTableDefinition(SymbolTableDefinition symbolTable,
			Module module) {
		EDLEdge edgeToParent = symbolTable
				.getFirstIsDefinedInIncidence(EdgeDirection.OUT);

		StringBuilder symbolTableCode = new StringBuilder();
		symbolTableCode.append("\t/**\n");
		symbolTableCode.append("\t * module: ")
				.append(module.get_identifier().get_name()).append(" line: ")
				.append(edgeToParent.get_line()).append(" column: ")
				.append(edgeToParent.get_column()).append(" length: ")
				.append(edgeToParent.get_length()).append("<br>\n");
		symbolTableCode.append("\t * ")
				.append(symbolTable.get_identifier().get_name()).append("<");

		String typeOfSymbolTable = null;
		boolean isPersistentSymbolTable = symbolTable.get_namespace() != null;
		if (!isPersistentSymbolTable) {
			typeOfSymbolTable = "SymbolTableStack";
		} else {
			typeOfSymbolTable = "PersistentSymbolTableStack";
			addImport("de.uni_koblenz.jgralab.schema.EdgeClass");
			addImport("de.uni_koblenz.jgralab.EdgeDirection");
		}
		addImport("de.uni_koblenz.edl.parser.symboltable." + typeOfSymbolTable);
		addImport("de.uni_koblenz.jgralab.schema.VertexClass");

		// create content of arrays
		StringBuilder elementTypes = new StringBuilder();
		StringBuilder persistentEdges = new StringBuilder();
		StringBuilder persistentEdgeDirections = new StringBuilder();
		String delim = "";
		for (IsElementIn iei : symbolTable
				.getIsElementInIncidences(EdgeDirection.IN)) {
			Element element = iei.getAlpha();
			String nameOfElement = element.get_vertexClass().get_identifier()
					.get_name();
			symbolTableCode.append(delim).append(nameOfElement);
			elementTypes.append(delim).append("getVertexClass(\"")
					.append(nameOfElement).append("\")");
			if (isPersistentSymbolTable) {
				IsPersistentEdgeClassOf ipeco = element
						.getFirstIsPersistentEdgeClassOfIncidence(EdgeDirection.IN);
				String nameOfPersistentEdge = ipeco.getAlpha().get_identifier()
						.get_name();
				persistentEdges.append(delim).append("getEdgeClass(\"")
						.append(nameOfPersistentEdge).append("\")");
				persistentEdgeDirections.append(delim).append("EdgeDirection.")
						.append(ipeco.is_isElementAlpha() ? "OUT" : "IN");
				symbolTableCode.append(
						ipeco.is_isElementAlpha() ? "-->" : "<--").append(
						nameOfPersistentEdge);
			}
			delim = ", ";
		}

		symbolTableCode.append(">");
		if (isPersistentSymbolTable) {
			symbolTableCode.append(":").append(
					symbolTable.get_namespace().get_identifier().get_name());
		}
		symbolTableCode.append("\n");
		symbolTableCode.append("\t */\n");

		generateCodeForSymbolTable(symbolTable, typeOfSymbolTable,
				isPersistentSymbolTable, elementTypes, persistentEdges,
				persistentEdgeDirections, module, symbolTableCode);
		symbolTableDefinitions.add(symbolTableCode);
	}

	private void generateCodeForSymbolTable(SymbolTableDefinition symbolTable,
			String typeOfSymbolTable, boolean isPersistentSymbolTable,
			StringBuilder elementTypes, StringBuilder persistentEdges,
			StringBuilder persistentEdgeDirections, Module module,
			StringBuilder symbolTableCode) {
		symbolTableCode.append("\tpublic final ");
		symbolTableCode.append(typeOfSymbolTable).append(" ");
		symbolTableCode.append(getUniqueSymbolTableIdentifier(symbolTable))
				.append(" =\n");

		symbolTableCode.append("\t\tnew ").append(typeOfSymbolTable)
				.append("(\"").append(symbolTable.get_identifier().get_name())
				.append("\", this,\n");
		symbolTableCode.append("\t\t\tnew VertexClass[]{").append(elementTypes)
				.append("}");
		if (isPersistentSymbolTable) {
			symbolTableCode.append(",\n");
			symbolTableCode.append("\t\t\tnew EdgeClass[]{")
					.append(persistentEdges).append("},\n");
			symbolTableCode.append("\t\t\tnew EdgeDirection[]{")
					.append(persistentEdgeDirections).append("},\n");
			symbolTableCode
					.append("\t\t\tgetVertexClass(\"")
					.append(symbolTable.get_namespace().get_identifier()
							.get_name()).append("\")");
		}
		symbolTableCode.append(");");
	}

	private void createSymbolTableDefinitions(Appendable appendable)
			throws IOException {
		for (StringBuilder sb : symbolTableDefinitions) {
			appendable.append("\n").append(sb).append("\n");
		}
	}

	/*
	 * Following methods concern islands
	 */

	private final List<Island> islandDefinitions = new ArrayList<Island>();

	public void addIsland(Island island) {
		islandDefinitions.add(island);
	}

	private void createActivateIslandGrammarMethod(Appendable appendable)
			throws IOException {
		if (!islandDefinitions.isEmpty()) {
			appendable.append("\n\tpublic void activateIslandGrammar() {\n");
			for (Island island : islandDefinitions) {
				appendable.append("\t\tadd");
				appendable.append(island.is_isExclusive() ? "Exclusive"
						: "Inclusive");
				appendable.append(island.isInstanceOf(IslandStart.VC) ? "Start"
						: "End");
				appendable.append("Pattern(");
				appendable.append(GraphIO.toUtfString(island.get_regExp()));
				appendable.append(");\n");
			}
			appendable.append("\t}\n");
		}
	}

	/*
	 * Following methods concern usercode
	 */

	private final List<UserCode> userCodeDefinition = new ArrayList<UserCode>();

	public void addUserCode(UserCode usercode, Module module) {
		userCodeDefinition.add(usercode);
		vertex2Module.put(usercode, module);
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();
		environment.setVariable("usercode", usercode);
		@SuppressWarnings("unchecked")
		Set<SemanticAction> semanticActions = (Set<SemanticAction>) GreqlQuery
				.createQuery("using usercode:usercode<--{semanticAction}")
				.evaluate(usercode.getGraph(), environment);
		if (!semanticActions.isEmpty()) {
			addImport("de.uni_koblenz.edl.parser.stack.elements.StackElement");
			for (SemanticAction semanticAction : semanticActions) {
				if (semanticAction.isInstanceOf(StatementSemanticAction.VC)) {
					addImport("de.uni_koblenz.edl.SemanticActionException");
					break;
				}
			}
		}
	}

	private void createUserCodeDeclaration(Appendable appendable)
			throws IOException {
		if (!userCodeDefinition.isEmpty()) {
			appendable.append("\n");
			appendable.append("\t// #################################\n");
			appendable.append("\t// #################################\n");
			appendable.append("\t// ## start of user specific code ##\n");
			appendable.append("\t// #################################\n");
			appendable.append("\t// #################################\n");
			for (UserCode usercode : userCodeDefinition) {
				Module module = vertex2Module.get(usercode);
				appendable.append("\n");
				for (Edge edge : usercode.incidences(EdgeDirection.IN)) {
					Vertex child = edge.getThat();
					if (child.isInstanceOf(JavaCode.VC)) {
						appendable.append(((JavaCode) child).get_content());
					} else {
						assert child.isInstanceOf(SemanticAction.VC);
						appendable.append(semanticActionGenerator
								.createSemanticActionMethod(
										(SemanticAction) child, module, null,
										false));
						appendable.append("(null)");
						if (child.isInstanceOf(StatementSemanticAction.VC)) {
							appendable.append(";\n");
						}
					}
				}
				appendable.append("\n");
			}
			appendable.append("\t// #################################\n");
			appendable.append("\t// #################################\n");
			appendable.append("\t// ##  end of user specific code  ##\n");
			appendable.append("\t// #################################\n");
			appendable.append("\t// #################################\n");
			appendable.append("\n");
		}
	}

	/*
	 * Following methods concern default values
	 */

	private DefaultValues defaultValuesDefinition;

	private Module moduleOfDefaultValues;

	public void setDefaultValues(DefaultValues defaultValues,
			Module moduleOfDefaultValues) {
		addImport("de.uni_koblenz.edl.SemanticActionException");
		if (containsDefaultValueForEdgeClass(defaultValues)) {
			addImport("de.uni_koblenz.jgralab.TemporaryEdge");
		}
		defaultValuesDefinition = defaultValues;
		this.moduleOfDefaultValues = moduleOfDefaultValues;
	}

	private boolean containsDefaultValueForEdgeClass(
			DefaultValues defaultValuesDefinition) {
		if (defaultValuesDefinition == null) {
			return false;
		}
		for (IsDefaultStatementOf idso : defaultValuesDefinition
				.getIsDefaultStatementOfIncidences(EdgeDirection.IN)) {
			assert idso.getThat().isInstanceOf(ExpressionStatement.VC);
			ExpressionStatement statement = (ExpressionStatement) idso
					.getThat();
			Expression expression = statement.get_expression();
			assert expression.isInstanceOf(Assignment.VC);

			Assignment assignment = (Assignment) expression;
			Expression lhsOfAssignment = assignment.get_assigned();

			assert lhsOfAssignment.isInstanceOf(DotAccess.VC);
			DotAccess dotAccess = (DotAccess) lhsOfAssignment;
			GraphElementClass graphElementClass = dotAccess
					.get_graphElementClass();
			assert graphElementClass != null;
			String nameOfGraphElementClass = graphElementClass.get_identifier()
					.get_name();
			if (!(schema.getAttributedElementClass(nameOfGraphElementClass) instanceof VertexClass)) {
				return true;
			}
		}
		return false;
	}

	private void createDefaultValuesSection(Appendable appendable)
			throws IOException {
		StringBuilder defaultValuesForVertex = new StringBuilder();
		StringBuilder defaultValuesForEdge = new StringBuilder();

		if (defaultValuesDefinition != null) {
			for (IsDefaultStatementOf idso : defaultValuesDefinition
					.getIsDefaultStatementOfIncidences(EdgeDirection.IN)) {
				assert idso.getThat().isInstanceOf(ExpressionStatement.VC);
				ExpressionStatement statement = (ExpressionStatement) idso
						.getThat();
				Expression expression = statement.get_expression();
				assert expression.isInstanceOf(Assignment.VC);

				Assignment assignment = (Assignment) expression;
				Expression lhsOfAssignment = assignment.get_assigned();
				Expression rhsOfAssignment = assignment.get_value();

				assert lhsOfAssignment.isInstanceOf(DotAccess.VC);
				DotAccess dotAccess = (DotAccess) lhsOfAssignment;
				GraphElementClass graphElementClass = dotAccess
						.get_graphElementClass();
				assert graphElementClass != null;
				String nameOfGraphElementClass = graphElementClass
						.get_identifier().get_name();
				boolean isVertexClass = schema
						.getAttributedElementClass(nameOfGraphElementClass) instanceof VertexClass;

				DotAccessible dotAccessible = dotAccess.get_dotAccessible();
				assert dotAccessible.isInstanceOf(Field.VC);
				String nameOfField = ((Field) dotAccessible).get_identifier()
						.get_name();

				StringBuilder prettyPrinted = new StringBuilder();

				StringBuilder content = isVertexClass ? defaultValuesForVertex
						: defaultValuesForEdge;
				content.append("\t\tif (")
						.append(isVertexClass ? "vertex" : "edge")
						.append(".isInstanceOf(get")
						.append(isVertexClass ? "Vertex" : "Edge")
						.append("Class(\"")
						.append(nameOfGraphElementClass)
						.append("\"))")
						.append(" || (edge.isInstanceOf(graph.getGraphClass().getTemporaryEdgeClass()) && ((TemporaryEdge) edge).getPreliminaryType().isSubClassOf(getEdgeClass(\"")
						.append(nameOfGraphElementClass).append("\")))) {\n");
				content.append("\t\t\ttry {\n");

				prettyPrinted.append(nameOfGraphElementClass).append(".")
						.append(nameOfField).append(" = ");
				content.append("\t\t\t\tsetAttribute(")
						.append(isVertexClass ? "vertex" : "edge")
						.append(", \"").append(nameOfField).append("\", ");
				semanticActionGenerator.generate(prettyPrinted, content,
						rhsOfAssignment, moduleOfDefaultValues, null, false,
						true);
				prettyPrinted.append(";");
				content.append(");\n");

				content.append("\t\t\t} catch (Throwable t) {\n");
				content.append(
						"\t\t\t\tthrow new SemanticActionException(\"default value: ")
						.append(prettyPrinted).append("\\n\"\n");
				content.append("\t\t\t\t\t+ \"module: ")
						.append(moduleOfDefaultValues.get_identifier()
								.get_name()).append(" line: ")
						.append(Integer.toString(idso.get_line()))
						.append(" column: ")
						.append(Integer.toString(idso.get_column()))
						.append(" length: ")
						.append(Integer.toString(idso.get_length()))
						.append("\\n\"\n");
				content.append("\t\t\t\t\t+ t.toString(), t);\n");
				content.append("\t\t\t}\n");
				content.append("\t\t}\n");
			}
		}

		appendable.append("\n\t@Override\n");
		appendable
				.append("\tprotected void setDefaultValues(Vertex vertex) {\n");
		appendable.append(defaultValuesForVertex);
		appendable.append("\t}\n");

		appendable.append("\n\t@Override\n");
		appendable.append("\tprotected void setDefaultValues(Edge edge) {\n");
		appendable.append(defaultValuesForEdge);
		appendable.append("\t}\n");

	}

	/*
	 * Following methods concern the execute methods
	 */

	private final MainExecuteMethod executeMethod = new MainExecuteMethod();

	private final Map<String, Set<Rule>> head2rules;

	private final ProductionPrinter productionPrinter = new ProductionPrinter();

	public void handleStartSymbol(Module module, Term startSymbol,
			GrammarType grammarType) {
		for (IsSemanticActionOf isao : startSymbol
				.getterm$IsSemanticActionOfIncidences(EdgeDirection.IN)) {
			// a start symbol has at this point 1 or 0 semantic actions
			StringBuilder termString = new StringBuilder();
			try {
				productionPrinter.printStartSymbol(termString, startSymbol,
						grammarType, false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			Set<Rule> startRules = new HashSet<Rule>();
			for (Rule rule : head2rules.get("<START>")) {
				if (rule.toString().startsWith(termString.toString())) {
					startRules.add(rule);
				}
			}

			SemanticAction semanticAction = isao.getAlpha();
			if (semanticAction != null) {
				for (Rule startRule : startRules) {
					addSemanticActionImport();
					int position = startRule.isContextFree() ? 3 : 1;
					ExecutePositionMethod positionMethod = new ExecutePositionMethod(
							position, semanticAction, startRule);
					if (!positionMethod.isEmpty()) {
						addSemanticActionImport();
					}

					ExecuteRuleMethod ruleMethod = new ExecuteRuleMethod(
							startRule);
					ruleMethod.addExecuteMethod(positionMethod);

					executeMethod.addExecuteMethod(module, startRule,
							ruleMethod);
				}
			}
		}
	}

	public void handleProduction(Production production,
			GrammarType grammarType, Module module) {
		Term head = production.get_headTerm();
		StringBuilder headString = new StringBuilder();
		try {
			productionPrinter.printTermAsRuleHead(headString, head,
					grammarType, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Set<Rule> headRules = head2rules.get(headString.toString());
		assert headRules != null;
		assert !headRules.isEmpty();

		List<ExecuteRuleMethod> executeRuleMethods = new ArrayList<ExecuteRuleMethod>();

		StringBuilder ruleString = new StringBuilder();
		try {
			productionPrinter.printProduction(ruleString, production,
					grammarType, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for (Rule rule : headRules) {
			if (rule.toString().startsWith(ruleString.toString())) {
				ExecuteRuleMethod executeRuleMethod = new ExecuteRuleMethod(
						rule);
				executeRuleMethods.add(executeRuleMethod);
			}
		}
		assert executeRuleMethods.size() == 1 : "Rules found for head of rule \""
				+ ruleString + "\": " + executeRuleMethods + "\n" + headRules;

		for (IsBodyTermOfProduction ibtop : production
				.getIsBodyTermOfProductionIncidences(EdgeDirection.IN)) {
			Sequence body = (Sequence) ibtop.getThat();
			handleSemanticActionsInTerm(body, grammarType, 0,
					executeRuleMethods, executeRuleMethods.get(0).getRule());
		}

		for (ExecuteRuleMethod executeRuleMethod : executeRuleMethods) {
			if (!executeRuleMethod.isEmpty()) {
				executeMethod.addExecuteMethod(module,
						executeRuleMethod.getRule(), executeRuleMethod);
			}
		}
	}

	private void handleSemanticActionsInTerm(Term term,
			GrammarType grammarType, int indexOfTerm,
			List<ExecuteRuleMethod> executeMethods, Rule definedBaseRule) {
		if (!existsRulesForTerm(term)) {
			return;
		} else if (term.isInstanceOf(BinaryTerm.VC)) {
			handleSemanticActionInBinaryTerm((BinaryTerm) term, grammarType,
					indexOfTerm, executeMethods, definedBaseRule);
		} else {
			int indexOfNextChildTerm = 0;
			Edge edge = term.getFirstIncidence(EdgeDirection.IN);
			Edge edgeAfterPrefixFunction = null;
			while (edge != null) {
				Vertex that = edge.getThat();
				if (that.isInstanceOf(PrefixFunction.VC)) {
					edgeAfterPrefixFunction = edge
							.getNextIncidence(EdgeDirection.IN);
					edge = that.getFirstIncidence(EdgeDirection.IN);
				}
				int currentIndex = indexOfNextChildTerm;
				if (that.isInstanceOf(Term.VC)) {
					ExecuteTermMethod executeTermMethod = new ExecuteTermMethod(
							currentIndex);
					List<ExecuteRuleMethod> ruleMethods = new ArrayList<ExecuteRuleMethod>();
					Set<Rule> currentRules = determineSetOfRules((Term) that,
							grammarType);
					for (Rule rule : currentRules) {
						ExecuteRuleMethod executeRuleMethod = new ExecuteRuleMethod(
								rule);
						ruleMethods.add(executeRuleMethod);
					}
					// set X --> childRule
					handleSemanticActionsInTerm((Term) that, grammarType,
							currentIndex, ruleMethods, definedBaseRule);
					// set childRule --> term
					for (ExecuteRuleMethod ruleMethod : ruleMethods) {
						if (!ruleMethod.isEmpty()) {
							executeTermMethod.addExecuteMethod(
									ruleMethod.getRule(), ruleMethod);
						}
					}
					// set term --> parentRule
					if (!executeTermMethod.isEmpty()) {
						for (ExecuteRuleMethod executeRuleMethod : executeMethods) {
							executeTermMethod.setTermIndex(adjustTermIndex(
									term, executeRuleMethod.getRule(),
									grammarType, currentIndex));
							executeRuleMethod
									.addExecuteMethod(executeTermMethod);
						}
					}
					indexOfNextChildTerm++;
				} else if (that.isInstanceOf(SemanticAction.VC)) {
					// set position --> rule
					for (ExecuteRuleMethod executeRuleMethod : executeMethods) {
						int index = adjustPositionValue(term,
								executeRuleMethod.getRule(), grammarType,
								currentIndex);
						ExecutePositionMethod executePositionMethod = new ExecutePositionMethod(
								index, (SemanticAction) that, definedBaseRule);
						if (!executePositionMethod.isEmpty()) {
							addSemanticActionImport();
						}
						executeRuleMethod
								.addExecuteMethod(executePositionMethod);
					}
				}
				edge = edge.getNextIncidence(EdgeDirection.IN);
				if ((edge == null) && (edgeAfterPrefixFunction != null)) {
					edge = edgeAfterPrefixFunction;
					edgeAfterPrefixFunction = null;
				}
			}
		}
	}

	private boolean existsRulesForTerm(Term term) {
		return !term.isInstanceOf(Start.VC) && !term.isInstanceOf(FileStart.VC)
				&& !term.isInstanceOf(LAYOUT.VC)
				&& !term.isInstanceOf(FileStart.VC)
				&& !term.isInstanceOf(Sort.VC)
				&& !term.isInstanceOf(Literal.VC)
				&& !term.isInstanceOf(CharacterClass.VC)
				&& !term.isInstanceOf(Label.VC);
	}

	private void handleSemanticActionInBinaryTerm(BinaryTerm term,
			GrammarType grammarType, int indexOfTerm,
			List<ExecuteRuleMethod> executeMethods, Rule definedRule) {
		SemanticAction beforeFirstTerm = null, afterFirstTerm, beforeSecondTerm, afterSecondTerm = null;
		Term firstTerm, secondTerm;

		// determine children
		if (term.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
			IsPartOfBinaryTerm edgeToParent = term
					.getFirstIsPartOfBinaryTermIncidence(EdgeDirection.OUT);
			if (edgeToParent != null) {
				BinaryTerm parent = edgeToParent.getOmega();
				if (parent
						.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
					// term is an alternative which is the right child of an
					// alternative
					Edge prevIncidence = edgeToParent.getReversedEdge()
							.getPrevIncidence();
					assert prevIncidence.isInstanceOf(IsSemanticActionOf.EC);
					// get semantic action S in (a|#S#(b|c))
					beforeSecondTerm = (SemanticAction) prevIncidence
							.getAlpha();
				}
			}
		}

		Edge edge = term.getFirstIncidence(EdgeDirection.IN);
		// skip labels
		while ((edge != null) && edge.isInstanceOf(IsAttachedBy.EC)) {
			edge = edge.getNextIncidence(EdgeDirection.IN);
		}
		assert edge != null;
		if (edge.isInstanceOf(IsSemanticActionOf.EC)) {
			beforeFirstTerm = (SemanticAction) edge.getThat();
			edge = edge.getNextIncidence(EdgeDirection.IN);
		}
		assert edge.isInstanceOf(IsPartOfBinaryTerm.EC);
		firstTerm = (Term) edge.getThat();
		edge = edge.getNextIncidence(EdgeDirection.IN);
		assert edge.isInstanceOf(IsSemanticActionOf.EC);
		afterFirstTerm = (SemanticAction) edge.getThat();
		edge = edge.getNextIncidence(EdgeDirection.IN);
		assert edge.isInstanceOf(IsSemanticActionOf.EC);
		beforeSecondTerm = (SemanticAction) edge.getThat();
		edge = edge.getNextIncidence(EdgeDirection.IN);
		assert edge.isInstanceOf(IsPartOfBinaryTerm.EC) : "expected "
				+ IsPartOfBinaryTerm.EC.getQualifiedName() + " found "
				+ edge.getAttributedElementClass().getQualifiedName();
		secondTerm = (Term) edge.getThat();
		edge = edge.getNextIncidence(EdgeDirection.IN);
		if ((edge != null) && edge.isInstanceOf(IsSemanticActionOf.EC)) {
			afterSecondTerm = (SemanticAction) edge.getThat();
		}

		// create methods
		List<ExecuteRuleMethod> ruleMethodsForFirstTerm = new ArrayList<ExecuteRuleMethod>();
		List<ExecuteRuleMethod> ruleMethodsForSecondTerm = new ArrayList<ExecuteRuleMethod>();
		if (term.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.List.VC)) {
			for (ExecuteRuleMethod executeRuleMethod : executeMethods) {
				if (executeRuleMethod.getRule().getType() == RuleType.LIST_PROD) {
					assert ruleMethodsForFirstTerm.isEmpty();
					ruleMethodsForFirstTerm.add(executeRuleMethod);
				} else {
					assert executeRuleMethod.getRule().getType() == RuleType.LIST;
					ruleMethodsForSecondTerm.add(executeRuleMethod);
				}
			}
		} else {
			// an alternative was detected
			if (!firstTerm
					.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
				// find rule for first term
				StringBuilder termString = new StringBuilder();
				try {
					productionPrinter.printTermAsRuleHead(termString,
							firstTerm, grammarType, false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				for (ExecuteRuleMethod executeRuleMethod : executeMethods) {
					if (executeRuleMethod.getRule().toString()
							.startsWith(termString.toString() + " -> ")) {
						assert ruleMethodsForFirstTerm.isEmpty();
						ruleMethodsForFirstTerm.add(executeRuleMethod);
					}
				}
			} else {
				handleSemanticActionInBinaryTerm((BinaryTerm) firstTerm,
						grammarType, indexOfTerm, executeMethods, definedRule);
			}
			if (!secondTerm
					.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
				// find rule for second term
				StringBuilder termString = new StringBuilder();
				try {
					productionPrinter.printTermAsRuleHead(termString,
							secondTerm, grammarType, false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				for (ExecuteRuleMethod executeRuleMethod : executeMethods) {
					if (executeRuleMethod.getRule().toString()
							.startsWith(termString.toString() + " -> ")) {
						assert ruleMethodsForSecondTerm.isEmpty();
						ruleMethodsForSecondTerm.add(executeRuleMethod);
					}
				}
			} else {
				handleSemanticActionInBinaryTerm((BinaryTerm) secondTerm,
						grammarType, indexOfTerm, executeMethods, definedRule);
			}
		}

		handleSemanticActionInBinaryTerm(beforeFirstTerm, firstTerm,
				afterFirstTerm, grammarType, indexOfTerm,
				ruleMethodsForFirstTerm, definedRule);
		handleSemanticActionInBinaryTerm(beforeSecondTerm, secondTerm,
				afterSecondTerm, grammarType, indexOfTerm,
				ruleMethodsForSecondTerm, definedRule);

	}

	private void handleSemanticActionInBinaryTerm(SemanticAction beforeTerm,
			Term term, SemanticAction afterTerm, GrammarType grammarType,
			int indexOfParentTerm, List<ExecuteRuleMethod> ruleMethodsForTerm,
			Rule definedRule) {
		if (ruleMethodsForTerm.isEmpty()) {
			return;
		}
		Rule rule = ruleMethodsForTerm.get(0).getRule();

		// create methods for semantic actions before term
		ExecutePositionMethod executePositionMethodBefore = null;
		if (beforeTerm != null) {
			executePositionMethodBefore = new ExecutePositionMethod(
					rule.getType() == RuleType.LIST ? 1 : 0, beforeTerm,
					definedRule);
			if (!executePositionMethodBefore.isEmpty()) {
				addSemanticActionImport();
			}
		}

		// create methods for semantic actions before term
		ExecutePositionMethod executePositionMethodAfter = null;
		if (afterTerm != null) {
			executePositionMethodAfter = new ExecutePositionMethod(
					rule.getType() == RuleType.LIST ? rule.isContextFree() ? 3
							: 2 : 1, afterTerm, definedRule);
			if (!executePositionMethodAfter.isEmpty()) {
				addSemanticActionImport();
			}
		}

		int indexOfTerm = rule.getType() == RuleType.LIST ? rule
				.isContextFree() ? 2 : 1 : 0;
		ExecuteTermMethod executeTermMethod = new ExecuteTermMethod(indexOfTerm);
		// create method for term
		List<ExecuteRuleMethod> ruleMethods = new ArrayList<ExecuteRuleMethod>();
		Set<Rule> currentRules = determineSetOfRules(term, grammarType);
		for (Rule r : currentRules) {
			ExecuteRuleMethod executeRuleMethod = new ExecuteRuleMethod(r);
			ruleMethods.add(executeRuleMethod);
		}
		// set X --> childRule
		handleSemanticActionsInTerm(term, grammarType, indexOfParentTerm,
				ruleMethods, definedRule);
		// set childRule --> term
		for (ExecuteRuleMethod ruleMethod : ruleMethods) {
			if (!ruleMethod.isEmpty()) {
				executeTermMethod.addExecuteMethod(ruleMethod.getRule(),
						ruleMethod);
			}
		}

		// set term --> parentRule
		for (ExecuteRuleMethod executeRuleMethod : ruleMethodsForTerm) {
			if ((executePositionMethodBefore != null)
					&& !executePositionMethodBefore.isEmpty()) {
				executeRuleMethod.addExecuteMethod(executePositionMethodBefore);
			}
			if ((executePositionMethodAfter != null)
					&& !executePositionMethodAfter.isEmpty()) {
				executeRuleMethod.addExecuteMethod(executePositionMethodAfter);
			}
			if (!executeTermMethod.isEmpty()) {
				executeRuleMethod.addExecuteMethod(executeTermMethod);
			}
		}
	}

	private int adjustPositionValue(Term term, Rule rule,
			GrammarType grammarType, int positionValue) {
		if (term.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.List.VC)) {
			if (rule.getType() == RuleType.LIST) {
				return grammarType == GrammarType.CONTEXT_FREE ? 3 : 2;
			} else {
				return positionValue;
			}
		} else if (term.isInstanceOf(Function.VC)) {
			if (grammarType == GrammarType.CONTEXT_FREE) {
				return ((positionValue + 1) * 2) - 1;
			} else {
				return positionValue + 1;
			}
		} else {
			if (grammarType == GrammarType.CONTEXT_FREE) {
				if (positionValue <= 1) {
					return positionValue;
				} else {
					return ((positionValue - 1) * 2) + 1;
				}
			} else {
				return positionValue;
			}
		}
	}

	private int adjustTermIndex(Term term, Rule rule, GrammarType grammarType,
			int indexOfTerm) {
		if (term.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.List.VC)) {
			return grammarType == GrammarType.CONTEXT_FREE ? 2 : 1;
		} else if (term.isInstanceOf(Function.VC)) {
			if (grammarType == GrammarType.CONTEXT_FREE) {
				return (indexOfTerm + 1) * 2;
			} else {
				return indexOfTerm + 1;
			}
		} else {
			if (grammarType == GrammarType.CONTEXT_FREE) {
				return indexOfTerm * 2;
			} else {
				return indexOfTerm;
			}
		}
	}

	private Set<Rule> determineSetOfRules(Term term, GrammarType grammarType) {
		String functionString = null;
		if (term.isInstanceOf(Function.VC)) {
			StringBuilder sb = new StringBuilder();
			try {
				productionPrinter.printRuleForFunction(sb, (Function) term,
						grammarType, false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			functionString = sb.toString();
		}
		StringBuilder termString = new StringBuilder();
		try {
			productionPrinter.printTermAsRuleHead(termString, term,
					grammarType, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Set<Rule> resultSet = new HashSet<Rule>();
		Set<Rule> current = head2rules.get(termString.toString());
		if (current != null) {
			if (functionString != null) {
				for (Rule rule : current) {
					if (rule.toString().startsWith(functionString)) {
						resultSet.add(rule);
					}
				}
			} else {
				resultSet.addAll(current);
			}
		}
		if (termString.toString().endsWith("*)")) {
			current = head2rules.get(termString.substring(0,
					termString.length() - 2)
					+ "+)");
			if (current != null) {
				resultSet.addAll(current);
			}
		} else if (termString.toString().endsWith("+)")) {
			current = head2rules.get(termString.substring(0,
					termString.length() - 2)
					+ "*)");
			if (current != null) {
				resultSet.addAll(current);
			}
		}
		return resultSet;
	}

	void addSemanticActionImport() {
		addImport("de.uni_koblenz.edl.parser.stack.elements.StackElement");
		addImport("de.uni_koblenz.edl.SemanticActionException");
	}

	private void createExecuteMethods(Appendable appendable) throws IOException {
		executeMethod.generateCode(appendable, semanticActionGenerator);
	}
}
