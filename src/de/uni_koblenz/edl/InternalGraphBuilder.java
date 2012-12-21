package de.uni_koblenz.edl;

import java.util.Map;

import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.edl.parser.stack.Stack;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This interface describes methods of a {@link GraphBuilder} which are used by
 * the EDL-parser.
 */
public interface InternalGraphBuilder extends GraphBuilder {

	/**
	 * Executes the current semantic action.
	 * 
	 * @param stack
	 *            {@link Stack} the stack containing X and Y
	 */
	public void execute(Stack stack);

	/**
	 * @return {@link Map} which maps a {@link Vertex} to its position in the
	 *         input
	 */
	public Map<Vertex, Position> getPositionsMap();

	/**
	 * Creates and returns a new instance of <code>edgeClass</code> with
	 * <code>alpha</code> as alpha vertex and <code>omega</code> as omega
	 * vertex. The default values defined in an EDL grammar are executed.
	 * 
	 * @param edgeClass
	 *            {@link EdgeClass} which will be instantiated
	 * @param alpha
	 *            {@link Vertex} the alpha {@link Vertex} of the newly created
	 *            edge
	 * @param omega
	 *            {@link Vertex} the omega {@link Vertex} of the newly created
	 *            edge
	 * @return {@link Edge} which was newly created
	 */
	public Edge createEdge(EdgeClass edgeClass, Vertex alpha, Vertex omega);

	/**
	 * Creates and returns a new instance of <code>vertexClass</code>. The
	 * default values defined in an EDL grammar are executed.
	 * 
	 * @param vertexClass
	 *            {@link VertexClass} which will be instantiated
	 * @param position
	 *            {@link Position} of the recognized lexem when a new
	 *            {@link Vertex} is created
	 * @return {@link Vertex} which was newly created
	 */
	public Vertex createVertex(VertexClass vertexClass, Position position);

	/**
	 * Creates a new {@link TemporaryVertex} and registers it in the position
	 * {@link Map} with <code>position</code>.
	 * 
	 * @param vertexClass
	 *            {@link VertexClass} the preliminary vertex class of the
	 *            created {@link TemporaryVertex}
	 * @param position
	 *            {@link Position} of the newly created {@link TemporaryVertex}
	 * @return {@link TemporaryVertex} which was newly created
	 */
	public TemporaryVertex createTemporaryVertex(VertexClass vertexClass,
			Position position);

	/**
	 * Creates a new {@link TemporaryVertex} and registers it in the position
	 * {@link Map} with <code>position</code>.
	 * 
	 * @param position
	 *            {@link Position} of the newly created {@link TemporaryVertex}
	 * @return {@link TemporaryVertex} which was newly created
	 */
	public TemporaryVertex createTemporaryVertex(Position position);

	/**
	 * If <code>oldVertex</code> is an {@link TemporaryVertex} and
	 * <code>newVertex</code> not, the first one is blessed to the
	 * {@link VertexClass} of the second one. All incident edges of
	 * <code>oldVertex</code> are connected to <code>newVertex</code>. If this
	 * does not work for an edge because of the schema type, the edge is
	 * deleted. Further more the <code>oldVertex</code> is deleted and the
	 * {@link GraphBuilderBaseImpl#positionsMap} is updated.
	 * 
	 * @param oldVertex
	 *            {@link Vertex} which will be deleted after the merge
	 * @param newVertex
	 *            {@link Vertex} which will receive the edges and attribute
	 *            values of <code>oldVertex</code>
	 */
	public void mergeVertices(Vertex oldVertex, Vertex newVertex);

	/**
	 * @return {@link Position} initial position for the current input
	 */
	public Position getInitialPosition();

	/**
	 * In the debug mode the internal parse forest is always printed. If an
	 * {@link Exception} occurs the causing rule application is highlighted.
	 * Otherwise the forest is only printed in case of ambiguity.
	 * 
	 * @return <code>boolean</code> <code>true</code> if the debug mode is
	 *         activated
	 */
	public boolean isDebugMode();

	/**
	 * In the debug mode the internal parse forest is always printed. If an
	 * {@link Exception} occurs the causing rule application is highlighted.
	 * Otherwise the forest is only printed in case of ambiguity.
	 * 
	 * @param debugMode
	 *            <code>boolean</code> <code>true</code> if the debug mode
	 *            should be activated
	 * @return {@link GraphBuilder}
	 */
	public GraphBuilder setDebugMode(boolean debugMode);

	/**
	 * In the verbose mode all rules are printed as they were generated during
	 * the BNF transformation. Otherwise only the rule defined in the grammar
	 * are printed as they have been written by the user.
	 * 
	 * @return <code>boolean</code> <code>true</code> if the verbose mode is
	 *         activated
	 */
	public boolean isVerboseMode();

	/**
	 * In the verbose mode all rules are printed as they were generated during
	 * the BNF transformation. Otherwise only the rule defined in the grammar
	 * are printed as they have been written by the user.
	 * 
	 * @param verboseMode
	 *            <code>boolean</code> <code>true</code> if the verbose mode
	 *            should be activated
	 * @return {@link GraphBuilder}
	 */
	public GraphBuilder setVerboseMode(boolean verboseMode);

	/**
	 * In the dot mode the internal parse forest is printed as a graphic of type
	 * {@link #getDotOutputFormat()}. Otherwise a {@link String} representation
	 * of the forest is printed to the console.
	 * 
	 * @return <code>boolean</code> <code>true</code> if the dot mode is
	 *         activated
	 */
	public boolean isDotMode();

	/**
	 * In the dot mode the internal parse forest is printed as a graphic of type
	 * {@link #getDotOutputFormat()}. Otherwise a {@link String} representation
	 * of the forest is printed to the console.<br>
	 * To use the dot mode the external program dot which is part of graphviz
	 * must be installed and be known at the command line. You can find graphviz
	 * under: <a href="http://www.graphviz.org/">http://www.graphviz.org/</a>
	 * 
	 * @param dotMode
	 *            <code>boolean</code> <code>true</code> if the dot mode should
	 *            be activated
	 * @return {@link GraphBuilder}
	 */
	public GraphBuilder setDotMode(boolean dotMode);

	/**
	 * @return {@link String} which is the output format for the created
	 *         graphical representation of the internal parse forest. Each
	 *         format is allowed which is supported by dot.
	 */
	public String getDotOutputFormat();

	/**
	 * @param outputFormat
	 *            {@link String} which is the output format for the created
	 *            graphical representation of the internal parse forest. Each
	 *            format is allowed which is supported by dot.
	 * @return {@link GraphBuilder}
	 */
	public GraphBuilder setDotOutputFormat(String outputFormat);

}
