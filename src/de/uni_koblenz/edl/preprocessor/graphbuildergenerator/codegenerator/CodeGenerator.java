package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.spoofax.jsglr.client.Label;
import org.spoofax.jsglr.client.ParseTable;

import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.preprocessor.EDLPreprocessor;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.GraphHandler;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile.GraphBuilderFile;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.GlobalAction;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.IsDefinedIn;
import de.uni_koblenz.edl.preprocessor.schema.section.IsUserCodeOf;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.Schema;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTables;
import de.uni_koblenz.edl.preprocessor.schema.section.UserCodeSection;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryClassFile;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;

public class CodeGenerator implements GraphHandler {

	private final GraphBuilderFile graphBuilderFile;

	private Module currentModule;

	private final boolean compile;

	private final boolean disableSemanticActions;

	public CodeGenerator(ParseTable parseTable, String nameOfGraphBuilder,
			String packagePrefix, String schemaPath, boolean compile,
			boolean disableSemanticActions) {
		Map<String, Set<Rule>> head2rules = new HashMap<String, Set<Rule>>();
		int indexOfRule = 0;
		for (Label label : parseTable.getLabels()) {
			if (label != null) {
				Rule rule = new Rule(indexOfRule, label.getProduction());
				if (rule.canHaveSemanticAciton()) {
					String headRepresentation = rule.getHeadRepresentation();
					Set<Rule> rulesForHead = head2rules.get(headRepresentation);
					if (rulesForHead == null) {
						rulesForHead = new HashSet<Rule>();
						head2rules.put(headRepresentation, rulesForHead);

					}
					rulesForHead.add(rule);
				}
			}
			indexOfRule++;
		}
		graphBuilderFile = new GraphBuilderFile(packagePrefix,
				nameOfGraphBuilder, nameOfGraphBuilder, head2rules);
		if ((schemaPath != null) && !schemaPath.isEmpty()) {
			graphBuilderFile.setSchemaFile(schemaPath);
		}
		this.compile = compile;
		this.disableSemanticActions = disableSemanticActions;
	}

	public String generateFile(String outputPath) {
		try {
			if (EDLPreprocessor.printDebugInformationToTheConsole) {
				System.out.println("\tWriting java file...");
			}
			StringBuffer fileContent = new StringBuffer();
			final String fileName = graphBuilderFile.createFile(outputPath,
					fileContent);
			if (compile) {
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				if (compiler == null) {
					System.out
							.println("WARNING: The generated GraphBuilder \""
									+ fileName
									+ "\" could not be compiled because not compiler is present."
									+ " Call javac manually.");
				} else {
					if (EDLPreprocessor.printDebugInformationToTheConsole) {
						System.out.println("\tCompiling...");
					}
					StandardJavaFileManager jfm = compiler
							.getStandardFileManager(null, null, null);
					ClassFileManager manager = new ClassFileManager(jfm,
							outputPath);
					Vector<SimpleJavaFileObject> javaSources = new Vector<SimpleJavaFileObject>(
							1);
					String fileNameWithoutSuffix = fileName.substring(0,
							fileName.lastIndexOf("."));
					javaSources.add(new InMemoryJavaSourceFile(URLEncoder
							.encode(fileNameWithoutSuffix.replace("\\", "/"),
									"utf-8"), fileContent.toString()));
					compiler.getTask(null, manager, null, null, null,
							javaSources).call();

					InMemoryClassFile classFile = manager.getOutputFile();
					writeClassFile(classFile.getBytecode(),
							fileNameWithoutSuffix + ".class");
				}
			}
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("GraphBuilder could not be created.", e);
		}
	}

	private void writeClassFile(byte[] bytecode, String className) {
		BufferedOutputStream bw = null;
		try {
			bw = new BufferedOutputStream(new FileOutputStream(className));
			bw.write(bytecode);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}

	@Override
	public void enterModule(Module module) {
		currentModule = module;
	}

	@Override
	public void leaveModule() {
		currentModule = null;
	}

	@Override
	public void handleSchemaSection(Schema schema) {
		if (!disableSemanticActions) {
			try {
				graphBuilderFile.setSchema(GraphBuilderBaseImpl
						.instantiateSchema(GraphBuilderBaseImpl
								.getSchemaClass(schema.get_name())));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void handleDefaultValuesSection(DefaultValues defaultValues) {
		if (!disableSemanticActions) {
			graphBuilderFile.setDefaultValues(defaultValues, currentModule);
		}
	}

	@Override
	public void handleGlobalActionsSection(GlobalAction globalAction) {
	}

	@Override
	public void handleImportDeclarationsSection(
			ImportDeclarations importDeclarations) {
		if (!disableSemanticActions) {
			graphBuilderFile.addImport(importDeclarations);
		}
	}

	@Override
	public void handleSymbolTablesSection(SymbolTables symbolTables) {
		if (!disableSemanticActions) {
			for (IsDefinedIn idi : symbolTables
					.getIsDefinedInIncidences(EdgeDirection.IN)) {
				graphBuilderFile.addSymbolTableDefinition(idi.getAlpha(),
						currentModule);
			}
		}
	}

	@Override
	public void handleUserCodeSection(UserCodeSection userCodeSection) {
		if (!disableSemanticActions) {
			for (IsUserCodeOf iuco : userCodeSection
					.getIsUserCodeOfIncidences(EdgeDirection.IN)) {
				graphBuilderFile.addUserCode(iuco.getAlpha(), currentModule);
			}
		}
	}

	@Override
	public void handleIslandSection(Island island) {
		graphBuilderFile.addIsland(island);
	}

	@Override
	public void handleStartSymbol(Term startSymbol, GrammarType grammarType) {
		if (!disableSemanticActions) {
			graphBuilderFile.handleStartSymbol(currentModule, startSymbol,
					grammarType);
		}
	}

	@Override
	public void handleProduction(Production production, GrammarType grammarType) {
		if (!disableSemanticActions) {
			graphBuilderFile.handleProduction(production, grammarType,
					currentModule);
		}
	}

}
