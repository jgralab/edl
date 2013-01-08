package de.uni_koblenz.edl;

import de.uni_koblenz.jgralab.Graph;

/**
 * This interface describes methods of a {@link GraphBuilder} which the user
 * should use.
 */
public interface GraphBuilder {

	/**
	 * Parses <code>inputFile</code>. The default encoding of the JVM is used.
	 * The generated {@link Graph} is stored at <code>outputFile</code>. Instead
	 * of this the internal parse forest is printed to the console, if an
	 * ambiguity was detected.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param inputFile
	 *            {@link String}[] the input files which will be parsed
	 * @param outputFile
	 *            {@link String} the file where the generated {@link Graph} will
	 *            be stored
	 */
	public void parse(String[] inputFiles, String outputFile);

	/**
	 * Parses <code>inputFile</code>. The generated {@link Graph} is stored at
	 * <code>outputFile</code>.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param inputFile
	 *            {@link String}[] the input files which will be parsed
	 * @param outputFile
	 *            {@link String} the file where the generated {@link Graph} will
	 *            be stored
	 * @param encoding
	 *            {@link String} which determines the encoding for
	 *            <code>inputFile</code>
	 * @param debugMode
	 *            <code>boolean</code> <code>true</code> if the debug mode is
	 *            activated. In the debug mode the internal parse forest is
	 *            always printed. If an {@link Exception} occurs the causing
	 *            rule application is highlighted. Otherwise the forest is only
	 *            printed in case of ambiguity.
	 * @param verboseMode
	 *            <code>boolean</code> <code>true</code> if the verbose mode is
	 *            activated. In the verbose mode all rules are printed as they
	 *            were generated during the BNF transformation. Otherwise only
	 *            the rule defined in the grammar are printed as they have been
	 *            written by the user.
	 * @param dotMode
	 *            <code>boolean</code> <code>true</code> if the dot mode is
	 *            activated. In the dot mode the internal parse forest is
	 *            printed as a graphic of type {@link #getDotOutputFormat()}.
	 *            Otherwise a {@link String} representation of the forest is
	 *            printed to the console.
	 * @param dotOutputFormat
	 *            {@link String} which is the output format for the created
	 *            graphical representation of the internal parse forest. Each
	 *            format is allowed which is supported by dot.
	 */
	public void parse(String[] inputFiles, String outputFile, String encoding,
			boolean debugMode, boolean verboseMode, boolean dotMode,
			String dotOutputFormat);

	/**
	 * Parses <code>inputFile</code>. The default encoding of the JVM is used.
	 * The generated {@link Graph} is returned. Instead of this the internal
	 * parse forest is printed to the console, if an ambiguity was detected.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param inputFile
	 *            {@link String} the input file which will be parsed
	 * @return {@link Graph} which is generated
	 */
	public Graph parse(String inputFile);

	/**
	 * Parses <code>inputFile</code>. The default encoding of the JVM is used.
	 * The generated {@link Graph} is returned. Instead of this the internal
	 * parse forest is printed to the console, if an ambiguity was detected.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param inputFile
	 *            {@link String}[] the input files which will be parsed
	 * @return {@link Graph} which is generated
	 */
	public Graph parse(String[] inputFiles);

	/**
	 * Parses <code>inputFile</code>. The generated {@link Graph} is returned.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param inputFile
	 *            {@link String}[] the input file which will be parsed
	 * @param encoding
	 *            {@link String} which determines the encoding for
	 *            <code>inputFile</code>
	 * @param debugMode
	 *            <code>boolean</code> <code>true</code> if the debug mode is
	 *            activated. In the debug mode the internal parse forest is
	 *            always printed. If an {@link Exception} occurs the causing
	 *            rule application is highlighted. Otherwise the forest is only
	 *            printed in case of ambiguity.
	 * @param verboseMode
	 *            <code>boolean</code> <code>true</code> if the verbose mode is
	 *            activated. In the verbose mode all rules are printed as they
	 *            were generated during the BNF transformation. Otherwise only
	 *            the rule defined in the grammar are printed as they have been
	 *            written by the user.
	 * @param dotMode
	 *            <code>boolean</code> <code>true</code> if the dot mode is
	 *            activated. In the dot mode the internal parse forest is
	 *            printed as a graphic of type {@link #getDotOutputFormat()}.
	 *            Otherwise a {@link String} representation of the forest is
	 *            printed to the console.
	 * @param dotOutputFormat
	 *            {@link String} which is the output format for the created
	 *            graphical representation of the internal parse forest. Each
	 *            format is allowed which is supported by dot.
	 * @return {@link Graph} which is generated
	 */
	public Graph parse(String[] inputFiles, String encoding, boolean debugMode,
			boolean verboseMode, boolean dotMode, String dotOutputFormat);

	/**
	 * Parses <code>input</code>. The generated {@link Graph} is returned.<br>
	 * If at least one island start pattern is set, then only the found islands
	 * of <code>inputFile</code> are parsed into one {@link Graph}.
	 * 
	 * @param input
	 *            {@link String} the input which will be parsed
	 * @param debugMode
	 *            <code>boolean</code> <code>true</code> if the debug mode is
	 *            activated. In the debug mode the internal parse forest is
	 *            always printed. If an {@link Exception} occurs the causing
	 *            rule application is highlighted. Otherwise the forest is only
	 *            printed in case of ambiguity.
	 * @param verboseMode
	 *            <code>boolean</code> <code>true</code> if the verbose mode is
	 *            activated. In the verbose mode all rules are printed as they
	 *            were generated during the BNF transformation. Otherwise only
	 *            the rule defined in the grammar are printed as they have been
	 *            written by the user.
	 * @param dotMode
	 *            <code>boolean</code> <code>true</code> if the dot mode is
	 *            activated. In the dot mode the internal parse forest is
	 *            printed as a graphic of type {@link #getDotOutputFormat()}.
	 *            Otherwise a {@link String} representation of the forest is
	 *            printed to the console.
	 * @param dotOutputFormat
	 *            {@link String} which is the output format for the created
	 *            graphical representation of the internal parse forest. Each
	 *            format is allowed which is supported by dot.
	 * @return {@link Graph} which is generated
	 */
	public Graph parseInput(String inputFile, String input, boolean debugMode,
			boolean verboseMode, boolean dotMode, String dotOutputFormat);

	/**
	 * @param inclusiveStartPattern
	 *            {@link String} a Java regular expression which detects the
	 *            beginning of an island, e.g. the part of the input which will
	 *            be parsed. The matched part of the input is part of the
	 *            island. All found islands are parsed into one {@link Graph}.
	 * @return {@link GraphBuilder} this
	 */
	public GraphBuilder addInclusiveStartPattern(String inclusiveStartPattern);

	/**
	 * @param exclusiveStartPattern
	 *            {@link String} a Java regular expression which detects the
	 *            beginning of an island, e.g. the part of the input which will
	 *            be parsed. The matched part of the input is not part of the
	 *            island. All found islands are parsed into one {@link Graph}.
	 * @return {@link GraphBuilder} this
	 */
	public GraphBuilder addExclusiveStartPattern(String exclusiveStartPattern);

	/**
	 * To activate island parsing, at least one island start pattern must be
	 * set, too. The end of the input is an island end, too.
	 * 
	 * @param inclusiveEndPattern
	 *            {@link String} a Java regular expression which detects the end
	 *            of an island, e.g. the part of the input which will be parsed.
	 *            The matched part of the input is part of the island. All found
	 *            islands are parsed into one {@link Graph}.
	 * @return {@link GraphBuilder} this
	 */
	public GraphBuilder addInclusiveEndPattern(String inclusiveEndPattern);

	/**
	 * To activate island parsing, at least one island start pattern must be
	 * set, too. The end of the input is an island end, too.
	 * 
	 * @param exclusiveEnPattern
	 *            {@link String} a Java regular expression which detects the end
	 *            of an island, e.g. the part of the input which will be parsed.
	 *            The matched part of the input is not part of the island. All
	 *            found islands are parsed into one {@link Graph}.
	 * @return {@link GraphBuilder} this
	 */
	public GraphBuilder addExclusiveEndPattern(String exclusiveEndPattern);

	/**
	 * @return {@link Graph} which is build by the semantic actions
	 */
	public Graph getGraph();

	/**
	 * @return nano sec
	 */
	public long getParseTime();

	/**
	 * @return nano sec
	 */
	public long getSemanticActionExecutionTime();

	/**
	 * @return nano sec
	 */
	public long getTotalTime();

	public long getGraphSize();

	public long getInputSize();

	public long getSizeOfInternalParseForest();
}
