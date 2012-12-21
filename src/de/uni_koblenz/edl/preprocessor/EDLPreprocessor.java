package de.uni_koblenz.edl.preprocessor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.spoofax.jsglr.client.ParseTable;

import de.uni_koblenz.edl.GraphBuilderBaseImpl;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.GraphBuilderGenerator;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.CodeGenerator;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.Desugarer;
import de.uni_koblenz.edl.preprocessor.schema.EDLGraph;
import de.uni_koblenz.edl.preprocessor.sdfgenerator.SDFGenerator;
import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;

public class EDLPreprocessor {

	/**
	 * If set to <code>true</code> debug information is printed to the console.
	 * They tell which parsing step is currently processed.
	 */
	public static boolean printDebugInformationToTheConsole = false;

	public static void main(String[] args) throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			GraphIOException {
		processCommandLineOptions(args);
	}

	/**
	 * Processes all command line parameters and parses the input.
	 * {@link #printDebugInformationToTheConsole} is set to <code>true</code>.
	 * 
	 * @param args
	 *            {@link String}[] the command line parameters.
	 * @throws GraphIOException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static void processCommandLineOptions(String[] args)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, GraphIOException {

		printDebugInformationToTheConsole = true;

		// Creates a OptionHandler.
		String toolString = "java " + EDLPreprocessor.class.getName();
		String versionString = "Extractor Description Language v1.0";

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option input = new Option(
				"i",
				"input",
				true,
				"(required): defines the base directory in which the start module and all imported modules will be searched.");
		input.setRequired(true);
		input.setArgName("SearchPath");
		oh.addOption(input);

		Option startModule = new Option(
				"m",
				"module",
				true,
				"(required): the fully qualified name of the start module."
						+ " The corresponding file must be found under <SearchPath><StartModule>.edl.");
		startModule.setRequired(true);
		startModule.setArgName("StartModule");
		oh.addOption(startModule);

		Option output = new Option("o", "output", true,
				"(required): determines the directory where the file should be generated.");
		output.setRequired(true);
		output.setArgName("OutputPath");
		oh.addOption(output);

		Option prefix = new Option("p", "packagePrefix", true,
				"(required): defines the package prefix of the generated GraphBuilder.");
		prefix.setRequired(true);
		prefix.setArgName("PackagePrefix");
		oh.addOption(prefix);

		Option schemaOption = new Option(
				"s",
				"schema",
				true,
				"(optional): determines the schema of the graph, which will be created by the generated GraphBuilder."
						+ " If this option is not set the schema class defined in the edl module must be known to the ClassLoader of the JVM.");
		schemaOption.setRequired(false);
		schemaOption.setArgName("Schema.tg");
		oh.addOption(schemaOption);

		Option compile = new Option("c", "compile", false,
				"(optional): if set the generated GraphBuilder is compiled automatically.");
		compile.setRequired(false);
		oh.addOption(compile);

		Option noDefaultMapping = new Option("n", "no_default_mapping", false,
				"(optional): if set no default mapping is executed.");
		noDefaultMapping.setRequired(false);
		oh.addOption(noDefaultMapping);

		Option disableSemanticAction = new Option("ds",
				"disable_semantic_actions", false,
				"(optional): if set no default mapping is executed.");
		disableSemanticAction.setRequired(false);
		oh.addOption(disableSemanticAction);

		// Parses the given command line parameters with all created Option.
		CommandLine cml = oh.parse(args);
		String searchSpace = cml.getOptionValue("i");
		if (!searchSpace.endsWith("/") && !searchSpace.endsWith("\\")) {
			searchSpace += File.separatorChar;
		}
		String nameOfStartModule = cml.getOptionValue("m");
		if (nameOfStartModule.toLowerCase().endsWith(".edl")) {
			nameOfStartModule = nameOfStartModule.substring(0,
					nameOfStartModule.length() - 4);
		}
		String outputPath = cml.getOptionValue("o");
		if (!outputPath.endsWith("/") && !outputPath.endsWith("\\")) {
			outputPath += File.separatorChar;
		}
		String packagePrefix = cml.getOptionValue("p");
		Schema schema = null;
		String schemaPath = null;
		if (cml.hasOption("s")) {
			schemaPath = cml.getOptionValue("s");
			schema = GraphBuilderBaseImpl
					.instantiateSchema(GraphBuilderBaseImpl
							.loadSchema(schemaPath));
		}

		if (printDebugInformationToTheConsole) {
			System.out.println("###################");
			System.out.println("Starting preprocessing of EDL modules");
		}

		// parse edl modules
		preprocess(searchSpace, nameOfStartModule, outputPath, packagePrefix,
				schema, schemaPath, cml.hasOption("c"), cml.hasOption("n"),
				cml.hasOption("ds"));
		if (printDebugInformationToTheConsole) {
			System.out.println("Finished preprocessing of EDL modules");
			System.out.println("###################");
		}
	}

	private static void preprocess(String searchSpace,
			String nameOfStartModule, String outputPath, String packagePrefix,
			Schema schema, String schemaPath, boolean compile,
			boolean disableDefaultMapping, boolean disableSemanticActions) {
		EDL2EDLGraph edl2edlGraph = new EDL2EDLGraph(searchSpace, schema);
		EDLGraph graph = (EDLGraph) edl2edlGraph
				.parse(new String[] { nameOfStartModule });
		if (schema == null) {
			schema = edl2edlGraph.getTargetSchema();
		}
		String outputName = null;
		if (schema != null) {
			outputName = schema.getGraphClass().getQualifiedName();
		} else {
			outputName = determineNameOfMainModule(graph).replaceAll("\\W+",
					"_");
		}

		// generate parse table
		ParseTable parseTable = new SDFGenerator().generateSDF(graph,
				outputPath, outputName, nameOfStartModule);

		// generate GraphBuilder
		CodeGenerator codeGenerator = new CodeGenerator(parseTable, outputName,
				packagePrefix, schemaPath, compile, disableSemanticActions);
		new GraphBuilderGenerator().generateCode(graph, outputPath,
				new Desugarer(schema, edl2edlGraph.schemaElementsTable,
						disableDefaultMapping), codeGenerator);
	}

	private static String determineNameOfMainModule(EDLGraph graph) {
		return graph.getFirstModule().get_identifier().get_name()
				.replaceAll("\\W+", "_");
	}

}
