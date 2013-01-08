package de.uni_koblenz.edl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.parser.TreeTraverser;
import de.uni_koblenz.edl.parser.stack.elements.StackElement;
import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

/**
 * This class is the base implementation of a {@link GraphBuilder}. It should
 * simplify the code of the generated {@link GraphBuilder}.
 */
public abstract class GraphBuilderBaseImpl implements InternalGraphBuilder {

	/*
	 * The following method helps to simplify the main method
	 */

	/**
	 * If set to <code>true</code> debug information is printed to the console.
	 * They tell which parsing step is currently processed.
	 */
	public static boolean printDebugInformationToTheConsole = false;

	/**
	 * Processes all command line parameters and parses the input.
	 * {@link #printDebugInformationToTheConsole} is set to <code>true</code>.
	 * 
	 * @param args
	 *            {@link String}[] the command line parameters.
	 */
	public static void processCommandLineOptions(String[] args,
			GraphBuilder graphBuilder, boolean needsOutput) {

		printDebugInformationToTheConsole = true;

		// Creates a OptionHandler.
		String toolString = "java " + graphBuilder.getClass().getName();
		String versionString = "Extractor Description Language v1.0";

		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option output = new Option("o", "output", true, "("
				+ (needsOutput ? "required" : "optional")
				+ "): writes a TG-file of the Schema to the given filename.");
		output.setRequired(needsOutput);
		output.setArgName("filename");
		oh.addOption(output);

		Option encoding = new Option("e", "encoding", true,
				"(optional): determines the encoding of the input file."
						+ " Default encoding is: "
						+ Charset.defaultCharset().displayName());
		encoding.setRequired(false);
		encoding.setArgName("encoding");
		oh.addOption(encoding);

		Option debug = new Option(
				"debug",
				false,
				"(optional): activates the debug mode."
						+ " In this mode the internal parse forest is always visualized.");
		debug.setRequired(false);
		oh.addOption(debug);

		Option dot = new Option(
				"d",
				"dot",
				true,
				"(optional): activates the dot mode."
						+ " In this mode the internal parse forest is visualized graphically."
						+ " This option requires that the external program dot is known to the command line.");
		dot.setRequired(false);
		dot.setArgName("dotOutputFormat");
		oh.addOption(dot);

		Option verbose = new Option(
				"vb",
				"verbose",
				false,
				"(optional): activates the verbose mode."
						+ " In this mode all applied rules in the internal parse forest are visualized."
						+ " All rules are printed in the form they were generated by the BNF transformation.");
		verbose.setRequired(false);
		oh.addOption(verbose);

		oh.setArgumentCount(Option.UNLIMITED_VALUES);
		oh.setArgumentName("inputFiles");

		// Parses the given command line parameters with all created Option.
		CommandLine cml = oh.parse(args);
		boolean debugMode = cml.hasOption("debug");
		boolean verboseMode = cml.hasOption("verbose");
		boolean dotMode = false;
		String dotOutputFormat = "png";
		if (cml.hasOption("dot")) {
			dotMode = true;
			dotOutputFormat = cml.getOptionValue("dot");
		}
		String enc = Charset.defaultCharset().name();
		if (cml.hasOption("encoding")) {
			enc = cml.getOptionValue("encoding");
		}
		String[] inputFiles = cml.getArgs();
		String outputFile = cml.getOptionValue("output");
		graphBuilder.parse(inputFiles, outputFile, enc, debugMode, verboseMode,
				dotMode, dotOutputFormat);
	}

	/*
	 * The following fields, constructors and methods initialize the parsing
	 * process
	 */

	/**
	 * Stores the position of the parse table.
	 */
	private final String parseTablePosition;

	private ParseTable parseTable;

	/**
	 * Stores the initial position of the currently parsed input.
	 */
	private Position initialPosition;

	protected TreeTraverser treeTraverser;

	private SGLR sglr;

	/**
	 * Creates a new {@link GraphBuilderBaseImpl}.
	 * 
	 * @param parseTable
	 *            {@link String} the position of the parse table
	 * @param schema
	 *            {@link Schema} which is used to create a new graph
	 */
	protected GraphBuilderBaseImpl(String parseTable, Schema schema) {
		this.schema = schema;
		graph = schema.createGraph(ImplementationType.STANDARD);
		positionsMap = new HashMap<Vertex, Position>();
		this.parseTablePosition = parseTable;
	}

	/**
	 * Creates a new {@link GraphBuilderBaseImpl}.
	 * 
	 * @param parseTable
	 *            {@link String} the position of the parse table
	 * @param graph
	 *            {@link Graph} which will be extended by the parsing process
	 */
	protected GraphBuilderBaseImpl(String parseTable, Graph graph) {
		this.graph = graph;
		schema = graph.getSchema();
		positionsMap = new HashMap<Vertex, Position>();
		this.parseTablePosition = parseTable;
	}

	/**
	 * Creates a new {@link GraphBuilderBaseImpl}.
	 * 
	 * @param parseTable
	 *            {@link String} the position of the parse table
	 */
	protected GraphBuilderBaseImpl(String parseTable) {
		positionsMap = new HashMap<Vertex, Position>();
		this.parseTablePosition = parseTable;
	}

	@Override
	public void parse(String[] inputFiles, String outputFile) {
		parse(inputFiles, outputFile, Charset.defaultCharset().name(), false,
				false, false, "png");
	}

	@Override
	public void parse(String[] inputFiles, String outputFile, String encoding,
			boolean debugMode, boolean verboseMode, boolean dotMode,
			String dotOutputFormat) {
		Graph graph = parse(inputFiles, encoding, debugMode, verboseMode,
				dotMode, dotOutputFormat);
		if (outputFile != null) {
			if (printDebugInformationToTheConsole) {
				System.out.println("Saving graph...");
			}
			try {
				GraphIO.saveGraphToFile(
						graph,
						outputFile,
						printDebugInformationToTheConsole ? new ConsoleProgressFunction()
								: null);
			} catch (GraphIOException e) {
				throw new SemanticActionException(
						"The graph could not be saved to " + outputFile + ".",
						e);
			}
		}
	}

	@Override
	public Graph parse(String inputFile) {
		return parse(new String[] { inputFile }, Charset.defaultCharset()
				.name(), false, false, false, "png");
	}

	@Override
	public Graph parse(String[] inputFiles) {
		return parse(inputFiles, Charset.defaultCharset().name(), false, false,
				false, "png");
	}

	@Override
	public Graph parse(String[] inputFiles, String encoding, boolean debugMode,
			boolean verboseMode, boolean dotMode, String dotOutputFormat) {
		if (printDebugInformationToTheConsole) {
			System.out.println("###################");
			System.out.println("Starting EDL parser");
		}
		String delim = "";
		for (String inputFile : inputFiles) {
			if (printDebugInformationToTheConsole) {
				System.out.println(delim + "\tCurrent file: " + inputFile);
			}
			String input;
			try {
				input = readInput(inputFile, encoding);
			} catch (IOException e) {
				throw new GrammarException("Exception during reading of input "
						+ inputFile + ".", e);
			}
			parseInput(inputFile, input, debugMode, verboseMode, dotMode,
					dotOutputFormat);
			delim = "\n";
		}
		finalize();
		if (printDebugInformationToTheConsole) {
			System.out.println("Finished EDL parser");
			System.out.println("###################");
		}
		return getGraph();
	}

	/**
	 * Reads and returns the input of <code>inputFile</code>.
	 * <code>encoding</code> is the used encoding to read <code>inputFile</code>
	 * .
	 * 
	 * @param inputFile
	 *            {@link String} the read input file
	 * @param encoding
	 *            {@link String} encoding to read <code>inputFile</code>
	 * @return {@link String} content of <code>inputFile</code>
	 * @throws IOException
	 */
	private String readInput(String inputFile, String encoding)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(inputFile));
			byte[] input = new byte[4069];
			int read;
			while ((read = bis.read(input)) > 0) {
				sb.append(new String(input, 0, read, Charset.forName(encoding)));
			}
			return sb.toString();
		} finally {
			if (bis != null) {
				bis.close();
			}
			inputSize += sb.length();
		}
	}

	@Override
	public Graph parseInput(String inputFile, String input, boolean debugMode,
			boolean verboseMode, boolean dotMode, String dotOutputFormat) {
		initializeModi(debugMode, verboseMode, dotMode, dotOutputFormat);
		if (treeTraverser == null) {
			treeTraverser = new TreeTraverser(this);
		}
		try {
			if (isIslandGrammar()) {
				for (Position pos : determineIslands(inputFile, input)) {
					if (printDebugInformationToTheConsole) {
						System.out.println("\tisland: " + pos);
					}
					initialPosition = pos;
					String inputSubstring = input.substring(pos.getOffset(),
							pos.getOffset() + pos.getLength());
					parseInput(inputFile, inputSubstring, treeTraverser);
				}
				return getGraph();
			} else {
				initialPosition = new Position(0, input.length(), 1, 0);
				Graph returnGraph = parseInput(inputFile, input, treeTraverser);
				return returnGraph;

			}
		} catch (ParseError e) {
			throw new GrammarException("Exception during parsing of input "
					+ inputFile + ".", e);
		} catch (SGLRException e) {
			throw new GrammarException("Exception during parsing of input "
					+ inputFile + ".", e);
		} catch (IOException e) {
			throw new GrammarException("Exception during parsing of input "
					+ inputFile + ".", e);
		} catch (InvalidParseTableException e) {
			throw new GrammarException("Exception during parsing of input "
					+ inputFile + ".", e);
		}
	}

	/**
	 * Uses {@link #parseTablePosition} and <code>treeTraverser</code> to create
	 * a new "Stratego/XT" interpreter to parse <code>input</code>. The created
	 * {@link Graph} is returned.
	 * 
	 * @param inputFile
	 *            {@link String} name of the file where <code>input</code> is
	 *            the content of
	 * @param input
	 *            {@link String} parsed content of <code>inputFile</code>
	 * @param treeTraverser
	 *            {@link TreeTraverser} which is used to traverse the internal
	 *            parse forest created by the interpreter and to execute the
	 *            semantic actions
	 * @return {@link Graph} which is created by this {@link GraphBuilder}
	 * @throws SGLRException
	 * @throws ParseError
	 * @throws IOException
	 * @throws InvalidParseTableException
	 */
	private Graph parseInput(String inputFile, String input,
			TreeTraverser treeTraverser) throws SGLRException, ParseError,
			IOException, InvalidParseTableException {
		resetMeasuredValues();
		InputStream parseTableStream = null;
		try {
			if (parseTable == null) {
				if (new File(parseTablePosition).exists()) {
					parseTableStream = new BufferedInputStream(
							new FileInputStream(parseTablePosition));
				} else {
					parseTableStream = this.getClass().getResourceAsStream(
							parseTablePosition);
				}
				final TermFactory factory = new TermFactory();
				final IStrategoTerm tableTerm = new TermReader(factory)
						.parseFromStream(parseTableStream);
				parseTable = new ParseTable(tableTerm, factory);
			}
			if (sglr == null) {
				sglr = new SGLR(treeTraverser, parseTable);
			}

			sglr.setUseStructureRecovery(false);
			sglr.getDisambiguator().setFilterCycles(true);
			sglr.getDisambiguator().setFilterAny(true);
			sglr.getDisambiguator().setHeuristicFilters(false);
			sglr.getDisambiguator().setAmbiguityIsError(false);

			if (printDebugInformationToTheConsole) {
				System.out.println("\tParsing...");
			}
			Graph resultGraph = (Graph) sglr.parse(input, inputFile, null);
			return resultGraph;
		} finally {
			if (parseTableStream != null) {
				parseTableStream.close();
			}
		}
	}

	@Override
	public Position getInitialPosition() {
		return initialPosition;
	}

	@Override
	protected void finalize() {

	}

	protected char[] getFileContent() {
		return treeTraverser.getStack().getFileContent();
	}

	/*
	 * The following fields and methods handle island grammars.
	 */

	/**
	 * Java regular expressions which detect the beginning of an island, e.g.
	 * the part of the input which will be parsed. The matched part of the input
	 * is part of the island. All found islands are parsed into one
	 * {@link Graph}.
	 */
	protected List<String> inclusiveStartPattern;

	/**
	 * Java regular expressions which detect the beginning of an island, e.g.
	 * the part of the input which will be parsed. The matched part of the input
	 * is not part of the island. All found islands are parsed into one
	 * {@link Graph}.
	 */
	protected List<String> exclusiveStartPattern;

	/**
	 * Java regular expressions which detect the end of an island, e.g. the part
	 * of the input which will be parsed. The matched part of the input is part
	 * of the island. All found islands are parsed into one {@link Graph}.
	 */
	protected List<String> inclusiveEndPattern;

	/**
	 * Java regular expressions which detect the end of an island, e.g. the part
	 * of the input which will be parsed. The matched part of the input is not
	 * part of the island. All found islands are parsed into one {@link Graph}.
	 */
	protected List<String> exclusiveEndPattern;

	@Override
	public GraphBuilder addInclusiveStartPattern(String inclusiveStartPattern) {
		if (this.inclusiveStartPattern == null) {
			this.inclusiveStartPattern = new ArrayList<String>();
		}
		this.inclusiveStartPattern.add(inclusiveStartPattern);
		return this;
	}

	@Override
	public GraphBuilder addExclusiveStartPattern(String exclusiveStartPattern) {
		if (this.exclusiveStartPattern == null) {
			this.exclusiveStartPattern = new ArrayList<String>();
		}
		this.exclusiveStartPattern.add(exclusiveStartPattern);
		return this;
	}

	@Override
	public GraphBuilder addInclusiveEndPattern(String inclusiveEndPattern) {
		if (this.inclusiveEndPattern == null) {
			this.inclusiveEndPattern = new ArrayList<String>();
		}
		this.inclusiveEndPattern.add(inclusiveEndPattern);
		return this;
	}

	@Override
	public GraphBuilder addExclusiveEndPattern(String exclusiveEndPattern) {
		if (this.exclusiveEndPattern == null) {
			this.exclusiveEndPattern = new ArrayList<String>();
		}
		this.exclusiveEndPattern.add(exclusiveEndPattern);
		return this;
	}

	/**
	 * @return <code>boolean</code> <code>true</code> if at least one island
	 *         start pattern is set
	 */
	private boolean isIslandGrammar() {
		return ((inclusiveStartPattern != null) && !inclusiveStartPattern
				.isEmpty())
				|| ((exclusiveStartPattern != null) && !exclusiveStartPattern
						.isEmpty());
	}

	/**
	 * Detects the islands of <code>input</code>. Each is represented by an
	 * {@link Position} object. An island begins at the first island start in
	 * <code>input</code> or behind an island end. An island ends at the end of
	 * <code>input</code> or at the first island end after a detected island
	 * start. For example:<br>
	 * If <code>s</code> is the inclusive start of an island, <code>e</code> is
	 * the exclusive end of an island and <code>input</code> is
	 * <code>ccscscseccccesccc</code> then there are two islands detected:
	 * <code>scscs</code> and <code>sccc</code>.
	 * 
	 * @param inputFile
	 *            {@link String} name of parsed file
	 * @param input
	 *            {@link String} content of <code>inputFile</code>
	 * @return {@link List} of {@link Position} object which represents the
	 *         detected islands
	 */
	private List<Position> determineIslands(String inputFile, String input) {
		System.out.println("\tDetermining islands...");
		List<Position> islandsList = new ArrayList<Position>();
		// contains indices of first island character
		List<Integer> islandStarts = new ArrayList<Integer>();
		// contains indices of character behind the last island character
		List<Integer> islandEnds = new ArrayList<Integer>();

		determinIslandStartAndEndIndices(input, islandStarts, islandEnds);
		if (islandStarts.isEmpty()) {
			System.out.println("No islands were found.");
			return islandsList;
		}

		int line = 1;
		int column = 0;

		int currentIslandStartsIndex = 0;
		int currentIslandStart = islandStarts.get(currentIslandStartsIndex);

		int currentIslandEndsIndex = 0;
		int currentIslandEnd = islandEnds.get(currentIslandEndsIndex);

		Position island = null;

		for (int i = 0; i <= input.length(); i++) {
			int lastIslandEnd = -1;
			if (i == currentIslandEnd) {
				// an island end was found
				if (island != null) {
					// an island is ended
					int lengthOfIsland = currentIslandEnd - island.getOffset();
					island.setLength(lengthOfIsland);
					island.setLastLine(line);
					island.setLastColumn(column);
					if (lengthOfIsland == 0) {
						System.out
								.println("WARNING: An empty island was detected:\n\tline: "
										+ island.getFirstLine()
										+ " column: "
										+ island.getFirstColumn()
										+ " length: "
										+ island.getLength()
										+ " in file: "
										+ inputFile);
					}
					island = null;
					lastIslandEnd = currentIslandEnd;
				}
				// update current island end information
				currentIslandEndsIndex++;
				if (currentIslandEndsIndex < islandEnds.size()) {
					currentIslandEnd = islandEnds.get(currentIslandEndsIndex);
				}
			}
			if (i == currentIslandStart) {
				// an island start was found
				if ((island == null) && (currentIslandStart != lastIslandEnd)) {
					// a new island was found
					island = new Position(currentIslandStart, 0, line, column);
					islandsList.add(island);
				}
				// update current island starts information
				currentIslandStartsIndex++;
				if (currentIslandStartsIndex < islandStarts.size()) {
					currentIslandStart = islandStarts
							.get(currentIslandStartsIndex);
				}
			}
			if (i < input.length()) {
				if (input.charAt(i) == '\n') {
					line++;
					column = 0;
				} else {
					column++;
				}
			}
		}

		return islandsList;
	}

	/**
	 * Determines all beginnings and ends of islands which could be found in
	 * <code>input</code>.
	 * 
	 * @param input
	 *            {@link String} where islands should be searched in
	 * @param islandStarts
	 *            {@link List} where the index of the first character of each
	 *            possible beginning of an island is appended to.
	 * @param islandEnds
	 *            {@link List} where the index of the first character behind the
	 *            last character of a possible end of an island is appended to.
	 */
	private void determinIslandStartAndEndIndices(String input,
			List<Integer> islandStarts, List<Integer> islandEnds) {
		StringBuilder inclusiveStartPatternString = createPattern(inclusiveStartPattern);
		StringBuilder exclusiveStartPatternString = createPattern(exclusiveStartPattern);
		StringBuilder inclusiveEndPatternString = createPattern(inclusiveEndPattern);
		StringBuilder exclusiveEndPatternString = createPattern(exclusiveEndPattern);

		Pattern inclusiveStartPattern = Pattern
				.compile(inclusiveStartPatternString.toString());
		Pattern exclusiveStartPattern = Pattern
				.compile(exclusiveStartPatternString.toString());
		Pattern inclusiveEndPattern = Pattern.compile(inclusiveEndPatternString
				.toString());
		Pattern exclusiveEndPattern = Pattern.compile(exclusiveEndPatternString
				.toString());

		String completePatternString = inclusiveStartPatternString.toString()
				.equals("()") ? "" : inclusiveStartPatternString.toString();
		completePatternString += exclusiveStartPatternString.toString().equals(
				"()") ? "" : (completePatternString.isEmpty() ? "" : "|")
				+ exclusiveStartPatternString.toString();
		completePatternString += inclusiveEndPatternString.toString().equals(
				"()") ? "" : (completePatternString.isEmpty() ? "" : "|")
				+ inclusiveEndPatternString.toString();
		completePatternString += exclusiveEndPatternString.toString().equals(
				"()") ? "" : (completePatternString.isEmpty() ? "" : "|")
				+ exclusiveEndPatternString.toString();

		Pattern islandBordersPattern = Pattern.compile(completePatternString);
		Matcher islandBordersMatcher = islandBordersPattern.matcher(input);
		while (islandBordersMatcher.find()) {
			String islandBorder = islandBordersMatcher.group();
			if (matches(inclusiveStartPattern.matcher(islandBorder),
					islandBorder.length())) {
				addIntIfNotExists(islandStarts, islandBordersMatcher.start());
			} else if (matches(exclusiveStartPattern.matcher(islandBorder),
					islandBorder.length())) {
				addIntIfNotExists(islandStarts, islandBordersMatcher.end());
			} else if (matches(inclusiveEndPattern.matcher(islandBorder),
					islandBorder.length())) {
				addIntIfNotExists(islandEnds, islandBordersMatcher.end());
			} else if (matches(exclusiveEndPattern.matcher(islandBorder),
					islandBorder.length())) {
				addIntIfNotExists(islandEnds, islandBordersMatcher.start());
			}
		}
		// the end of the input is an island end, too
		islandEnds.add(input.length());
	}

	/**
	 * Appends <code>value</code> to <code>list</code> if its last element is
	 * different from <code>value</code>.
	 * 
	 * @param list
	 *            {@link List} of {@link Integer}s where <code>value</code> is
	 *            possibly added
	 * @param value
	 *            <code>int</code> which might be added to <code>list</code>
	 */
	private void addIntIfNotExists(List<Integer> list, int value) {
		if (list.isEmpty() || (list.get(list.size() - 1) != value)) {
			list.add(value);
		}
	}

	/**
	 * @param matcher
	 *            {@link Matcher} which first match is tested
	 * @param length
	 *            <code>int</code> which determines the end of the first match
	 *            of <code>matcher</code>
	 * @return <code>boolean</code> <code>true</code> if the first match of
	 *         <code>matcher</code> starts at index 0 and ends at index
	 *         <code>length</code>
	 */
	private boolean matches(Matcher matcher, int length) {
		if (matcher.find()) {
			return (matcher.start() == 0) && (matcher.end() == length);

		} else {
			return false;
		}
	}

	/**
	 * If <code>patternStrings</code> contains the three patterns
	 * <code>a b c</code>, then <code>((a)|(b)|(c))</code> is returned.
	 * 
	 * @param patternStrings
	 *            {@link List} of the patterns which should be ORed
	 * @return {@link StringBuilder} which contains the ORed patter of all
	 *         patterns contained in <code>patternString</code>
	 */
	private StringBuilder createPattern(List<String> patternStrings) {
		if (patternStrings == null) {
			return new StringBuilder("()");
		}
		StringBuilder orPatternString = new StringBuilder("(");
		String delim = "";
		for (String singlePatternString : patternStrings) {
			orPatternString.append(delim).append("(")
					.append(singlePatternString).append(")");
			delim = "|";
		}
		orPatternString.append(")");
		return orPatternString;
	}

	/*
	 * These fields and methods concern the different modes of parsing.
	 */

	/**
	 * <code>true</code> if the debug mode is activated. In the debug mode the
	 * internal parse forest is always printed. If an {@link Exception} occurs
	 * the causing rule application is highlighted. Otherwise the forest is only
	 * printed in case of ambiguity.
	 */
	protected boolean isDebugMode;

	/**
	 * <code>true</code> if the verbose mode is activated. In the verbose mode
	 * all rules are printed as they were generated during the BNF
	 * transformation. Otherwise only the rule defined in the grammar are
	 * printed as they have been written by the user.
	 */
	protected boolean isVerboseMode;

	/**
	 * <code>true</code> if the dot mode is activated. In the dot mode the
	 * internal parse forest is printed as a graphic of type
	 * {@link #getDotOutputFormat()}. Otherwise a {@link String} representation
	 * of the forest is printed to the console.
	 */
	protected boolean isDotMode;

	/**
	 * {@link String} which is the output format for the created graphical
	 * representation of the internal parse forest. Each format is allowed which
	 * is supported by dot.
	 */
	protected String dotOutputFormat;

	/**
	 * Sets:
	 * <ul>
	 * <li>{@link #isDebugMode} to <code>debugMode</code></li>
	 * <li>{@link #isVerboseMode} to <code>verboseMode</code></li>
	 * <li>{@link #isDotMode} to <code>dotMode</code></li>
	 * <li>{@link #dotOutputFormat} to <code>dotOutputFormat</code></li>
	 * </ul>
	 * 
	 * @param debugMode
	 *            <code>boolean</code>
	 * @param verboseMode
	 *            <code>boolean</code>
	 * @param dotMode
	 *            <code>boolean</code>
	 * @param dotOutputFormat
	 *            {@link String}
	 */
	private void initializeModi(boolean debugMode, boolean verboseMode,
			boolean dotMode, String dotOutputFormat) {
		setDebugMode(debugMode);
		setVerboseMode(verboseMode);
		setDotMode(dotMode);
		if (dotMode) {
			assert dotOutputFormat != null;
			setDotOutputFormat(dotOutputFormat);
		}
	}

	@Override
	public boolean isDebugMode() {
		return isDebugMode;
	}

	@Override
	public GraphBuilder setDebugMode(boolean debugMode) {
		isDebugMode = debugMode;
		return this;
	}

	@Override
	public boolean isVerboseMode() {
		return isVerboseMode;
	}

	@Override
	public GraphBuilder setVerboseMode(boolean verboseMode) {
		isVerboseMode = verboseMode;
		return this;
	}

	@Override
	public boolean isDotMode() {
		return isDotMode;
	}

	@Override
	public GraphBuilder setDotMode(boolean dotMode) {
		isDotMode = dotMode;
		return this;
	}

	@Override
	public String getDotOutputFormat() {
		return dotOutputFormat;
	}

	@Override
	public GraphBuilder setDotOutputFormat(String outputFormat) {
		outputFormat = outputFormat.trim();
		if (outputFormat.startsWith(".")) {
			outputFormat = outputFormat.substring(1);
		}
		dotOutputFormat = outputFormat;
		return this;
	}

	/*
	 * The following fields and methods handle the graph modification.
	 */

	/**
	 * {@link Map} which maps a {@link Vertex} to its {@link Position} in the
	 * input.
	 */
	protected Map<Vertex, Position> positionsMap;

	/**
	 * {@link Schema} which is used to get {@link VertexClass}es,
	 * {@link EdgeClass}es, {@link EnumDomain}s and {@link RecordDomain}s from.
	 */
	protected Schema schema;

	/**
	 * {@link Graph} which is built by this {@link GraphBuilder}.
	 */
	protected Graph graph;

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public Map<Vertex, Position> getPositionsMap() {
		return positionsMap;
	}

	@Override
	public Edge createEdge(EdgeClass edgeClass, Vertex alpha, Vertex omega) {
		Edge createdEdge = null;
		if ((alpha instanceof TemporaryVertex)
				|| (omega instanceof TemporaryVertex)) {
			createdEdge = getGraph().createTemporaryEdge(edgeClass, alpha,
					omega);
		} else {
			createdEdge = getGraph().createEdge(edgeClass, alpha, omega);
		}
		setDefaultValues(createdEdge);
		return createdEdge;
	}

	/**
	 * Creates an {@link Edge} of type <code>edgeClass</code> from each
	 * {@link Vertex} in <code>alpha</code> to <code>omega</code>.
	 * 
	 * @param edgeClass
	 *            {@link EdgeClass} the type of the newly created edges
	 * @param alpha
	 *            {@link Iterable} of all alpha vertices
	 * @param omega
	 *            {@link Vertex} the omega {@link Vertex} of all newly created
	 *            edges
	 */
	public void createEdge(EdgeClass edgeClass, Iterable<Vertex> alpha,
			Vertex omega) {
		for (Vertex v : alpha) {
			createEdge(edgeClass, v, omega);
		}
	}

	/**
	 * Creates an {@link Edge} of type <code>edgeClass</code> from
	 * <code>alpha</code> to each {@link Vertex} in <code>omega</code>.
	 * 
	 * @param edgeClass
	 *            {@link EdgeClass} the type of the newly created edges
	 * @param alpha
	 *            {@link List} of all alpha vertices
	 * @param omega
	 *            {@link Iterable} of all omega vertices
	 */
	public void createEdge(EdgeClass edgeClass, Vertex alpha,
			Iterable<Vertex> omega) {
		for (Vertex v : omega) {
			createEdge(edgeClass, alpha, v);
		}
	}

	/**
	 * Creates an {@link Edge} of type <code>edgeClass</code> from each
	 * {@link Vertex} in <code>alpha</code> to each {@link Vertex} in
	 * <code>omega</code>.
	 * 
	 * @param edgeClass
	 *            {@link EdgeClass} the type of the newly created edges
	 * @param alpha
	 *            {@link Iterable} of all alpha vertices
	 * @param omega
	 *            {@link Iterable} of all omega vertices
	 */
	public void createEdge(EdgeClass edgeClass, Iterable<Vertex> alpha,
			Iterable<Vertex> omega) {
		for (Vertex v : alpha) {
			createEdge(edgeClass, v, omega);
		}
	}

	/**
	 * Delegates to {@link #createEdge(EdgeClass, Vertex, Vertex)},
	 * {@link #createEdge(EdgeClass, Vertex, List)},
	 * {@link #createEdge(EdgeClass, List, Vertex)} or
	 * {@link #createEdge(EdgeClass, List, List)}.
	 * 
	 * @param edgeClass
	 * @param alpha
	 * @param omega
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Edge createEdge(EdgeClass edgeClass, Object alpha, Object omega) {
		if ((alpha instanceof Vertex) && (omega instanceof Vertex)) {
			return createEdge(edgeClass, (Vertex) alpha, (Vertex) omega);
		} else if ((alpha instanceof Iterable) && (omega instanceof Vertex)) {
			createEdge(edgeClass, (Iterable<Vertex>) alpha, (Vertex) omega);
		} else if ((alpha instanceof Vertex) && (omega instanceof Iterable)) {
			createEdge(edgeClass, (Vertex) alpha, (Iterable<Vertex>) omega);
		} else if ((alpha instanceof Iterable) && (omega instanceof Iterable)) {
			createEdge(edgeClass, (Iterable<Vertex>) alpha,
					(Iterable<Vertex>) omega);
		} else {
			throw new ClassCastException(
					"createEdge must be called with parameter types"
							+ " (EdgeClass,Vertex,Vertex), (EdgeClass,Iterable<Vertex>,Vertex),"
							+ " (EdgeClass,Vertex,Iterable<Vertex>) or (EdgeClass,Iterable<Vertex>,Iterable<Vertex>)"
							+ " but it was called with parameter types ("
							+ edgeClass.getClass().getSimpleName() + ","
							+ alpha.getClass().getSimpleName() + ","
							+ omega.getClass().getSimpleName() + ").");
		}
		return null;
	}

	/**
	 * Executes the default values actions defined in an EDL grammar for the
	 * newly created <code>edge</code>.
	 * 
	 * @param edge
	 *            {@link Edge} which was newly created.
	 */
	protected abstract void setDefaultValues(Edge edge);

	@Override
	public Vertex createVertex(VertexClass vertexClass, Position position) {
		Vertex createdVertex = getGraph().createVertex(vertexClass);
		positionsMap.put(createdVertex, position);
		setDefaultValues(createdVertex);
		return createdVertex;
	}

	@Override
	public TemporaryVertex createTemporaryVertex(VertexClass vertexClass,
			Position position) {
		TemporaryVertex createdVertex = getGraph().createTemporaryVertex(
				vertexClass);
		positionsMap.put(createdVertex, position);
		return createdVertex;
	}

	/**
	 * Executes the default values actions defined in an EDL grammar for the
	 * newly created <code>vertex</code>.
	 * 
	 * @param vertex
	 *            {@link Vertex} which was newly created.
	 */
	protected abstract void setDefaultValues(Vertex vertex);

	@Override
	public TemporaryVertex createTemporaryVertex(Position position) {
		TemporaryVertex result = getGraph().createTemporaryVertex();
		getPositionsMap().put(result, position);
		return result;
	}

	@Override
	public void mergeVertices(Vertex oldVertex, Vertex newVertex) {
		Position oldPosition = positionsMap.get(oldVertex);
		if (oldVertex.isTemporary() && !newVertex.isTemporary()) {
			oldVertex = ((TemporaryVertex) oldVertex).bless(newVertex
					.getAttributedElementClass());
		}
		Edge firstEdgeOfNewVertex = newVertex.getFirstIncidence();
		// set all edges to newVertex
		Edge edge = oldVertex.getFirstIncidence();
		while (edge != null) {
			try {
				if (edge.getAlpha() == oldVertex) {
					edge.setAlpha(newVertex);
				} else {
					edge.setOmega(newVertex);
				}
				if (firstEdgeOfNewVertex != null) {
					edge.putIncidenceBefore(firstEdgeOfNewVertex);
				}
			} catch (GraphException e) {
				// the edge could not be transfered, so it is deleted
				edge.delete();
			}
			edge = oldVertex.getFirstIncidence();
		}

		// delete old vertex
		if (positionsMap.get(newVertex) == null) {
			getPositionsMap().put(newVertex, oldPosition);
		}
		oldVertex.delete();
		positionsMap.remove(oldVertex);
	}

	/*
	 * The following fields and methods support time measurement
	 */

	private long parseTime;

	private long executionTime;

	private long inputSize;

	private long sizeOfInternalParseForest;

	@Override
	public long getParseTime() {
		return parseTime;
	}

	public void addToParseTime(long value) {
		parseTime += value;
	}

	@Override
	public long getSemanticActionExecutionTime() {
		return executionTime;
	}

	public void addToSemanticActionExecutionTime(long value) {
		executionTime += value;
	}

	@Override
	public long getTotalTime() {
		return parseTime + executionTime;
	}

	@Override
	public long getGraphSize() {
		return (long) graph.getVCount() + (long) graph.getECount();
	}

	@Override
	public long getInputSize() {
		return inputSize;
	}

	public void addToInputSize(long value) {
		inputSize += value;
	}

	@Override
	public long getSizeOfInternalParseForest() {
		return sizeOfInternalParseForest;
	}

	public void addToSizeOfInternalParseForest(long value) {
		sizeOfInternalParseForest += value;
	}

	private void resetMeasuredValues() {
		parseTime = 0;
		executionTime = 0;
		inputSize = 0;
		sizeOfInternalParseForest = 0;
	}

	/*
	 * The following methods simplify the generated code.
	 */

	public static Schema loadSchema(String pathToSchema)
			throws GraphIOException {
		return GraphIO.loadSchemaFromFile(pathToSchema);
	}

	public static Schema instantiateSchema(Schema schema)
			throws GraphIOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		String schemaQName = schema.getQualifiedName();
		Class<?> schemaClass = null;
		try {
			schemaClass = getSchemaClass(schemaQName);
		} catch (ClassNotFoundException e) {
			// schema class not found, try compile schema in-memory
			schema.compile(CodeGeneratorConfiguration.NORMAL);
			try {
				schemaClass = getSchemaClass(schemaQName);
			} catch (ClassNotFoundException e1) {
				throw new GraphIOException(
						"Unable to load a graph which belongs to the schema because the Java-classes for this schema can not be created.",
						e1);
			}
		}
		// create an instance of the compiled schema class
		Method instanceMethod = schemaClass.getMethod("instance",
				(Class<?>[]) null);
		return (Schema) instanceMethod.invoke(null, new Object[0]);
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Schema> getSchemaClass(String schemaClassName)
			throws ClassNotFoundException {
		return (Class<? extends Schema>) Class.forName(schemaClassName, true,
				SchemaClassManager.instance(schemaClassName));
	}

	public static Schema instantiateSchema(Class<? extends Schema> schema)
			throws GraphIOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		// create an instance of the compiled schema class
		Method instanceMethod = schema.getMethod("instance", (Class<?>[]) null);
		return (Schema) instanceMethod.invoke(null, new Object[0]);
	}

	/**
	 * From <code>oldParent</code> always the last child is searched until a
	 * {@link StackElement} is found, which stands for a leaf or the application
	 * of a rule which can execute semantic actions. If no such StackElement
	 * could be found, <code>null</code> is returned.
	 * 
	 * @param oldParent
	 *            {@link StackElement}
	 * @param currentElement
	 *            {@link StackElement}
	 * @return {@link StackElement}
	 */
	protected StackElement skipElementsWithNoSemanticAction(
			StackElement oldParent, StackElement currentElement) {
		StackElement nextParent = oldParent.getChild(oldParent
				.getCurrentSemanticActionPosition() - 1);
		while (((nextParent != null) && (nextParent.getAppliedRule() != null) && !nextParent
				.getAppliedRule().canHaveSemanticAciton())
				|| (nextParent.getAppliedRule().getType() == RuleType.LIST)) {
			while ((currentElement != nextParent)
					&& (nextParent.getAppliedRule().getType() == RuleType.LIST)) {
				nextParent = nextParent.getChild(nextParent
						.getCurrentSemanticActionPosition() - 1);
			}
			if ((currentElement == nextParent)
					|| (nextParent.getAppliedRule().getType() == RuleType.LIST_PROD)) {
				return nextParent;
			}
			nextParent = nextParent.getChild(nextParent
					.getCurrentSemanticActionPosition() - 1);
		}
		return nextParent;
	}

	/**
	 * @param currentElement
	 *            {@link StackElement}
	 * @param message
	 *            {@link String}
	 * @return {@link String} <code>message</code> appended by the currently
	 *         recognized lexem of <code>currentElement</code> and the current
	 *         position values
	 */
	protected String createMessageString(StackElement currentElement,
			String message) {
		return message + " Concerning lexem:\n" + currentElement.getLexem()
				+ "\n(line: " + currentElement.getFirstLine() + ", column: "
				+ currentElement.getFirstColumn() + ", length: "
				+ currentElement.getLength() + ", file: "
				+ currentElement.getNameOfParsedFile() + ")";
	}

	/**
	 * Delegates to {@link #createVertex(VertexClass, Position)}.
	 * 
	 * @param vertexClass
	 * @param position
	 * @return
	 */
	protected Vertex createVertex(Object vertexClass, Position position) {
		assert position != null;
		return createVertex(getVertexClass(vertexClass), position);
	}

	protected Vertex createTemporaryVertex(Object vertexClass, Position position) {
		return createTemporaryVertex(getVertexClass(vertexClass), position);
	}

	protected VertexClass getVertexClass(Object vertexClass) {
		VertexClass vc = (VertexClass) schema
				.getAttributedElementClass((String) vertexClass);
		if (vc != null) {
			return vc;
		}
		throw new SemanticActionException("No such VertexClass: " + vertexClass);
	}

	/**
	 * Delegates to {@link #createEdge(EdgeClass, Vertex, Vertex)},
	 * {@link #createEdge(EdgeClass, Vertex, List)},
	 * {@link #createEdge(EdgeClass, List, Vertex)} or
	 * {@link #createEdge(EdgeClass, List, List)}.
	 * 
	 * @param edgeClass
	 * @param alpha
	 * @param omega
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Edge createEdge(Object edgeClass, Object alpha, Object omega) {
		if ((alpha instanceof Vertex) && (omega instanceof Vertex)) {
			return createEdge(getEdgeClass(edgeClass), (Vertex) alpha,
					(Vertex) omega);
		} else if ((alpha instanceof List) && (omega instanceof Vertex)) {
			createEdge(getEdgeClass(edgeClass), (List<Vertex>) alpha,
					(Vertex) omega);
		} else if ((alpha instanceof Vertex) && (omega instanceof List)) {
			createEdge(getEdgeClass(edgeClass), (Vertex) alpha,
					(List<Vertex>) omega);
		} else if ((alpha instanceof List) && (omega instanceof List)) {
			createEdge(getEdgeClass(edgeClass), (List<Vertex>) alpha,
					(List<Vertex>) omega);
		} else {
			throw new ClassCastException(
					"createEdge must be called with parameter types"
							+ " (EdgeClass,Vertex,Vertex), (EdgeClass,List<Vertex>,Vertex),"
							+ " (EdgeClass,Vertex,List<Vertex>) or (EdgeClass,List<Vertex>,List<Vertex>)"
							+ " but it was called with parameter types ("
							+ edgeClass.getClass().getSimpleName() + ","
							+ alpha.getClass().getSimpleName() + ","
							+ omega.getClass().getSimpleName() + ").");
		}
		return null;
	}

	protected EdgeClass getEdgeClass(Object edgeClass) {
		EdgeClass ec = (EdgeClass) schema
				.getAttributedElementClass((String) edgeClass);
		if (ec != null) {
			return ec;
		}
		throw new SemanticActionException("No such EdgeClass: " + edgeClass);
	}

	protected Vertex blessTemporaryVertex(Vertex tempVertex,
			VertexClass targetVertexClass) {
		if (tempVertex.isTemporary()) {
			Position pos = positionsMap.get(tempVertex);
			tempVertex = tempVertex.bless(targetVertexClass);
			positionsMap.put(tempVertex, pos);
		}
		return tempVertex;
	}

	private final Map<String, RecordDomain> recordDomains = new HashMap<String, RecordDomain>();

	/**
	 * Creates a record with the values <code>values</code>.
	 * 
	 * @param recordDomain
	 *            {@link Object} which is the {@link RecordDomain} which should
	 *            be instantiated
	 * @param values
	 *            {@link Object} which is a {@link Map}&lt;{@link String},
	 *            {@link Object}&gt; and maps an attribute to a specific value
	 * @return {@link Record} which is created
	 */
	@SuppressWarnings("unchecked")
	protected Record createRecord(Object recordDomain, Object values) {
		RecordDomain recordDom = null;
		if (recordDomain instanceof String) {
			recordDom = recordDomains.get(recordDomain);
			if (recordDom == null) {
				for (RecordDomain rd : schema.getRecordDomains()) {
					if (rd.getQualifiedName().equals(recordDomain)) {
						recordDom = rd;
					}
					recordDomains.put(rd.getQualifiedName(), rd);
				}
			}
		} else {
			recordDom = (RecordDomain) recordDomain;
		}
		return graph.createRecord(recordDom, (Map<String, Object>) values);
	}

	/**
	 * Sets the attribute <code>attrName</code> to <code>value</code> if
	 * <code>attrElem</code> is an {@link AttributedElement}. Otherwise the
	 * field <code>attrName</code> is set to <code>value</code>.
	 * 
	 * @param attrElem
	 *            {@link Object} whose attribute with name <code>attrName</code>
	 *            should be set
	 * @param attrName
	 *            {@link Object} which stands for a {@link String}
	 * @param attrValue
	 *            {@link Object}
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected void setAttribute(Object attrElem, Object attrName,
			Object attrValue) {
		if (attrElem instanceof AttributedElement) {
			AttributedElement<?, ?> attrElement = ((AttributedElement<?, ?>) attrElem);
			attrElement.setAttribute(
					(String) attrName,
					convertAttributeType(attrElement, (String) attrName,
							attrValue));
		} else if (attrElem instanceof Record) {
			throw new SemanticActionException(
					"A component of a record could not be set.");
		} else {
			try {
				Class<?> classOfAttrElem = attrElem.getClass();
				String fieldName = classOfAttrElem.toString() + "#" + attrName;
				Field field = string2Field.get(fieldName);
				if (field == null) {
					field = classOfAttrElem.getField((String) attrName);
					string2Field.put(fieldName, field);
				}
				field.set(attrElem, attrValue);
			} catch (NoSuchFieldException e) {
				throw new SemanticActionException(e);
			} catch (SecurityException e) {
				throw new SemanticActionException(e);
			} catch (IllegalArgumentException e) {
				throw new SemanticActionException(e);
			} catch (IllegalAccessException e) {
				throw new SemanticActionException(e);
			}
		}
	}

	/**
	 * If the {@link Domain} of attribute <code>attrName</code> of
	 * <code>attrElement</code> is
	 * <ul>
	 * <li>{@link BooleanDomain}, {@link IntegerDomain}, {@link LongDomain} or
	 * {@link DoubleDomain} and <code>attrValue</code> is instance of
	 * {@link String}, then it is tried to convert vie the corresponding parse
	 * method of the corresponding wrapper class.</li>
	 * <li>{@link StringDomain}, then <code>attrValue.toString()</code> is
	 * returned.</li>
	 * <li>{@link ListDomain} and <code>attrValue</code> is instance of
	 * {@link Collection} than a new {@link PVector} is returned which contains
	 * all elements of <code>attrValue</code>.</li>
	 * <li>{@link SetDomain} and <code>attrValue</code> is instance of
	 * {@link Collection} than a new {@link PSet} is returned which contains all
	 * elements of <code>attrValue</code>.</li>
	 * <li>{@link MapDomain} and <code>attrValue</code> is instance of
	 * {@link Map} than a new {@link PMap} is returned which contains all
	 * entries of <code>attrValue</code>.</li>
	 * </ul>
	 * Otherwise <code>attrValue</code> is returned.
	 * 
	 * @param attrElement
	 * @param attrName
	 * @param attrValue
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convertAttributeType(AttributedElement<?, ?> attrElement,
			String attrName, Object attrValue) {
		if ((attrElement instanceof TemporaryVertex)
				|| (attrElement instanceof TemporaryEdge)) {
			if (attrValue instanceof Map) {
				PMap map = JGraLab.map();
				return map.plusAll((Map) attrValue);
			}
			if (attrValue instanceof Set) {
				PSet set = JGraLab.set();
				return set.plusAll((Collection) attrValue);
			}
			if (attrValue instanceof List) {
				PVector vector = JGraLab.vector();
				return vector.plusAll((Collection) attrValue);
			}
			return attrValue;
		}
		Attribute attribute = attrElement.getAttributedElementClass()
				.getAttribute(attrName);
		if (attribute == null) {
			throw new GrammarException("The attribute \"" + attrName
					+ "\" is undefined for GraphElement " + attrElement + ".");
		}
		Domain domain = attribute.getDomain();
		if (domain instanceof BooleanDomain) {
			if (attrValue instanceof String) {
				return Boolean.parseBoolean((String) attrValue);
			}
		} else if (domain instanceof IntegerDomain) {
			if (attrValue instanceof String) {
				return Integer.parseInt((String) attrValue);
			}
		} else if (domain instanceof LongDomain) {
			if (attrValue instanceof String) {
				return Long.parseLong((String) attrValue);
			}
		} else if (domain instanceof DoubleDomain) {
			if (attrValue instanceof String) {
				return Double.parseDouble((String) attrValue);
			}
		} else if (domain instanceof StringDomain) {
			return attrValue.toString();
		} else if (domain instanceof ListDomain) {
			if (attrValue instanceof Collection) {
				PVector vector = JGraLab.vector();
				return vector.plusAll((Collection) attrValue);
			}
		} else if (domain instanceof SetDomain) {
			if (attrValue instanceof Collection) {
				PSet set = JGraLab.set();
				return set.plusAll((Collection) attrValue);
			}
		} else if (domain instanceof MapDomain) {
			if (attrValue instanceof Map) {
				PMap map = JGraLab.map();
				return map.plusAll((Map) attrValue);
			}
		}
		return attrValue;
	}

	/**
	 * If <code>attrElem</code> is an instance of {@link AttributedElement} then
	 * the attribute with name <code>attrName</code> is returned. Otherwise the
	 * value of field <code>attrName</code> of <code>attrElem</code> is
	 * returned.
	 * 
	 * @param attrElem
	 *            {@link Object} whose attribute with name <code>attrName</code>
	 *            is requested
	 * @param attrName
	 *            {@link Object} which stands for a {@link String}
	 * @return {@link Object} the value of the attribute <code>attrName</code>
	 *         of {@link AttributedElement} <code>attrElem</code>
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	protected Object getAttribute(Object attrElem, Object attrName) {
		if (attrElem instanceof AttributedElement) {
			return ((AttributedElement<?, ?>) attrElem)
					.getAttribute((String) attrName);
		} else if (attrElem instanceof Record) {
			return ((Record) attrElem).getComponent((String) attrName);
		} else {
			try {
				Class<?> classOfAttrElem = attrElem.getClass();
				String fieldName = classOfAttrElem.toString() + "#" + attrName;
				Field field = string2Field.get(fieldName);
				if (field == null) {
					field = classOfAttrElem.getField((String) attrName);
					string2Field.put(fieldName, field);
				}
				return field.get(attrElem);
			} catch (NoSuchFieldException e) {
				throw new SemanticActionException(e);
			} catch (SecurityException e) {
				throw new SemanticActionException(e);
			} catch (IllegalArgumentException e) {
				throw new SemanticActionException(e);
			} catch (IllegalAccessException e) {
				throw new SemanticActionException(e);
			}
		}
	}

	private final Map<String, Field> string2Field = new HashMap<String, Field>();

	/**
	 * @param listObject
	 * @param position
	 * @return {@link Object} which is at index <code>position</code> of
	 *         {@link List} <code>listObject</code>
	 */
	@SuppressWarnings("rawtypes")
	protected Object get_ElementOfList(Object listObject, int position) {
		if (!(listObject instanceof List)) {
			throw new SemanticActionException(
					"The first argument must be of typ java.util.List but was "
							+ (listObject == null ? "null" : "of type "
									+ listObject.getClass().getName()) + ".");
		} else {
			return ((List) listObject).get(position);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object set_ElementOfList(Object listObject, int position,
			Object value) {
		if (!(listObject instanceof List)) {
			throw new SemanticActionException(
					"The first argument must be of typ java.util.List but was "
							+ (listObject == null ? "null" : "of type "
									+ listObject.getClass().getName()) + ".");
		} else {
			List list = (List) listObject;
			if (position >= list.size()) {
				for (int i = list.size(); i < position; i++) {
					list.add(null);
				}
				assert list.size() == position;
				list.add(value);
			} else {
				list.set(position, value);
			}
			return value;
		}
	}

	private final Map<String, Object> enumerationConstants = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	protected <T> T getEnumConstant(String enumName, String enumConsName) {
		T result = (T) enumerationConstants.get(enumName + "#" + enumConsName);
		if (result == null) {
			Class<T> enumeration = null;
			for (EnumDomain enumDom : schema.getEnumDomains()) {
				if (enumName.endsWith(enumDom.getQualifiedName())) {
					enumeration = (Class<T>) enumDom.getSchemaClass();
				}
			}
			if (enumeration == null) {
				try {
					enumeration = (Class<T>) Class.forName(enumName);
				} catch (ClassNotFoundException e) {
					throw new SemanticActionException(e);
				}
			}
			assert enumeration.isEnum();
			for (T enumConstant : enumeration.getEnumConstants()) {
				if (enumConstant.toString().equals(enumConsName)) {
					result = enumConstant;
				}
				enumerationConstants.put(enumName + "#" + enumConstant,
						enumConstant);
			}
		}
		return result;
	}

	private final Map<String, Method> string2Method = new HashMap<String, Method>();

	public Object callMethod(Object calledObject, String methodname,
			Object... parameters) {
		Class<?> classOfCalledObject = calledObject.getClass();

		StringBuilder uniqueMethodName = new StringBuilder();
		uniqueMethodName.append(classOfCalledObject.toString()).append("#")
				.append(methodname).append("(");
		String delim = "";
		Class<?>[] paramTypes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Class<?> paramType = parameters[i].getClass();
			paramTypes[i] = paramType;
			uniqueMethodName.append(delim).append(paramTypes[i].toString());
			delim = ",";
		}
		uniqueMethodName.append(")");

		try {
			Method method = string2Method.get(uniqueMethodName.toString());
			if (method == null) {
				if (this == calledObject) {
					method = getMethod(classOfCalledObject, true, methodname,
							parameters, paramTypes);
					method.setAccessible(true);
				} else {
					method = getMethod(classOfCalledObject, false, methodname,
							parameters, paramTypes);
				}
			}
			return method.invoke(calledObject, parameters);
		} catch (IllegalAccessException e) {
			throw new SemanticActionException(e);
		} catch (IllegalArgumentException e) {
			throw new SemanticActionException(e);
		} catch (InvocationTargetException e) {
			throw new SemanticActionException(e);
		}
	}

	private Method getMethod(Class<?> classOfCalledObject, boolean isThis,
			String methodname, Object[] parameters, Class<?>[] paramTypes) {
		for (Method m : isThis ? classOfCalledObject.getDeclaredMethods()
				: classOfCalledObject.getMethods()) {
			if (m.getName().equals(methodname)) {
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length == parameters.length) {
					boolean wasFound = true;
					for (int i = 0; i < parameterTypes.length; i++) {
						Class<?> definedParameter = parameterTypes[i];
						Class<?> neededParameter = parameters[i].getClass();
						if (definedParameter.isInstance(parameters[i])) {
							continue;
						} else if ((definedParameter == boolean.class)
								&& (neededParameter == Boolean.class)) {
							continue;
						} else if ((definedParameter == byte.class)
								&& (neededParameter == Byte.class)) {
							continue;
						} else if ((definedParameter == short.class)
								&& (neededParameter == Short.class)) {
							continue;
						} else if ((definedParameter == int.class)
								&& (neededParameter == Integer.class)) {
							continue;
						} else if ((definedParameter == long.class)
								&& (neededParameter == Long.class)) {
							continue;
						} else if ((definedParameter == float.class)
								&& (neededParameter == Float.class)) {
							continue;
						} else if ((definedParameter == double.class)
								&& (neededParameter == Double.class)) {
							continue;
						} else if ((definedParameter == char.class)
								&& (neededParameter == Character.class)) {
							continue;
						} else if ((definedParameter == Boolean.class)
								&& (neededParameter == boolean.class)) {
							continue;
						} else if ((definedParameter == Byte.class)
								&& (neededParameter == byte.class)) {
							continue;
						} else if ((definedParameter == Short.class)
								&& (neededParameter == short.class)) {
							continue;
						} else if ((definedParameter == Integer.class)
								&& (neededParameter == int.class)) {
							continue;
						} else if ((definedParameter == Long.class)
								&& (neededParameter == long.class)) {
							continue;
						} else if ((definedParameter == Float.class)
								&& (neededParameter == float.class)) {
							continue;
						} else if ((definedParameter == Double.class)
								&& (neededParameter == double.class)) {
							continue;
						} else if ((definedParameter == Character.class)
								&& (neededParameter == char.class)) {
							continue;
						} else {
							wasFound = false;
						}
					}
					if (wasFound) {
						if (isThis) {
							m.setAccessible(true);
						}
						return m;
					}
				}
			}
		}
		String paramString = Arrays.toString(paramTypes);
		paramString = paramString.substring(1, paramString.length() - 1);
		try {
			throw new NoSuchMethodException(classOfCalledObject.toString()
					+ "." + methodname + "(" + paramString + ")");
		} catch (NoSuchMethodException e) {
			throw new SemanticActionException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List createList(Object... elements) {
		List list = new ArrayList(elements.length);
		for (Object o : elements) {
			list.add(o);
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set createSet(Object... elements) {
		Set set = new HashSet(elements.length);
		for (Object o : elements) {
			set.add(o);
		}
		return set;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map createMap(Object... elements) {
		if ((elements.length % 2) == 1) {
			throw new SemanticActionException("The last defined key \""
					+ elements[elements.length - 1]
					+ "\" does not have an value.");
		}
		Map map = new HashMap(elements.length / 2);
		for (int i = 0; i < elements.length; i = i + 2) {
			map.put(elements[i], elements[i + 1]);
		}
		return map;
	}
}
