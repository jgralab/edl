package de.uni_koblenz.edl.parser.symboltable;

import java.util.Map;

import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class is the realization of the persistent symbol tables in EDL. It
 * represents a stack of {@link PersistentMap}s.
 */
public class PersistentSymbolTableStack extends SymbolTableStack {

	/**
	 * This array stores the {@link EdgeClass}es which instances make the
	 * vertices in this map persistent in the {@link Graph}.
	 */
	protected final EdgeClass[] persistentEdgeClasses;

	/**
	 * This array donates for each {@link EdgeClass} in
	 * {@link #persistentEdgeClasses} if the {@link Vertex} inserted in this
	 * {@link PersistentSymbolTableStack} is the alpha or the omega vertex of
	 * the created edge.<br>
	 * {@link EdgeDirection#IN} stands for <code>AVertexClass&lt;-</code><br>
	 * {@link EdgeDirection#OUT} stands for <code>AVertexClass-&gt;</code>
	 */
	protected final EdgeDirection[] directionOfEdgeClasses;

	/**
	 * The allowed type of the namespace of a {@link PersistentMap}
	 */
	private final VertexClass nameSpace;

	/**
	 * Creates a new {@link PersistentSymbolTableStack} with an empty
	 * {@link Map} as bottom element. For each {@link VertexClass} in
	 * <code>elementTypes</code> there must exist an {@link EdgeClass} in
	 * <code>persistentEdgeClasses</code> and end {@link EdgeDirection} in
	 * <code>directionOfEdgeClasses</code>.
	 * 
	 * @param nameOfSymbolTable
	 *            {@link String}This is the name of the current instance of this
	 *            {@link SymbolTableStack} . It is the same name as it was
	 *            defined in an EDL grammar. It is only used for debug reasons
	 *            in {@link Exception} messages.
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} is used for the creation of
	 *            temporary vertices and in case of a
	 *            {@link PersistentSymbolTableStack} to create edges which make
	 *            the table persistent in the {@link Graph} built by the
	 *            {@link InternalGraphBuilder}.
	 * @param elementTypes
	 *            {@link VertexClass}[] This array stores the allowed
	 *            {@link VertexClass}es. A {@link Vertex} may only be inserted
	 *            in this symbol table, if its type ore one of its supertypes is
	 *            contained in this array.
	 * @param persistentEdgeClasses
	 *            {@link EdgeClass}[] This array stores the {@link EdgeClass}es
	 *            which instances make the vertices in this map persistent in
	 *            the {@link Graph}.
	 * @param directionOfEdgeClasses
	 *            {@link EdgeDirection}[] This array donates for each
	 *            {@link EdgeClass} in <code>persistentEdgeClasses</code> if the
	 *            {@link Vertex} inserted in this
	 *            {@link PersistentSymbolTableStack} is the alpha or the omega
	 *            vertex of the created edge.
	 * @param nameSpace
	 *            {@link VertexClass} which donates the allowed type of the
	 *            namespace of a {@link PersistentMap}
	 */
	public PersistentSymbolTableStack(String nameOfSymbolTable,
			InternalGraphBuilder graphBuilder, VertexClass[] elementTypes,
			EdgeClass[] persistentEdgeClasses,
			EdgeDirection[] directionOfEdgeClasses, VertexClass nameSpace) {
		super(nameOfSymbolTable, graphBuilder, elementTypes);
		assert elementTypes.length == persistentEdgeClasses.length : "For each element type must be a persistend edgeclass present.";
		assert persistentEdgeClasses.length == directionOfEdgeClasses.length : "For each edgeclass must be a direction present.";
		this.persistentEdgeClasses = persistentEdgeClasses;
		this.directionOfEdgeClasses = directionOfEdgeClasses;
		this.nameSpace = nameSpace;
	}

	/**
	 * Creates a new {@link PersistentMap} and pushes it on this
	 * {@link PersistentSymbolTableStack}.
	 */
	@Override
	public void push() {
		PersistentMap newMap = new PersistentMap();
		pushNewMap(newMap);
	}

	/**
	 * If {@link #nameSpace} of the top {@link PersistentMap} is set, the
	 * possibly newly created {@link TemporaryVertex} is connected to
	 * {@link PersistentMap#nameSpace} via an edge whose type corresponds to the
	 * type of <code>value</code>.
	 */
	@Override
	public Vertex use(Object key) {
		Vertex result = super.use(key);
		if (result != null && ((PersistentMap) top).getNameSpace() != null) {
			connectWithNameSpace(result);
		}
		return result;
	}

	/**
	 * If {@link #nameSpace} is set, additionally <code>value</code> is
	 * connected to it via an edge whose type corresponds to the type of
	 * <code>value</code>, if <code>value</code> is not temporary.
	 */
	@Override
	public Vertex declare(Object key, Vertex value) {
		Vertex result = super.declare(key, value);
		if (((PersistentMap) top).getNameSpace() != null
				&& !value.isTemporary()) {
			connectWithNameSpace(result);
		}
		return result;
	}

	/**
	 * Determines the index of the most specific {@link VertexClass} in
	 * {@link SymbolTableStack#elementTypes} which is a superclass of the
	 * {@link VertexClass} of <code>vertex</code>. This index is used to create
	 * an {@link Edge} of type {@link #persistentEdgeClasses}[index] to connect
	 * <code>vertex</code> with {@link PersistentMap#nameSpace}. The direction
	 * of this edge is determined by {@link #directionOfEdgeClasses}[index].
	 * 
	 * @param vertex
	 *            {@link Vertex} which should be connected to {@link #nameSpace}
	 */
	private void connectWithNameSpace(Vertex vertex) {
		Vertex nameSpace = ((PersistentMap) top).getNameSpace();
		assert nameSpace != null : "The nameSpace of the current PersistentMap has not been set, yet.";
		int indexOfVertexClass = findIndexOfVertexClass(vertex
				.getAttributedElementClass());
		assert indexOfVertexClass >= 0
				&& indexOfVertexClass < elementTypes.length : "The vertexclass \""
				+ vertex.getAttributedElementClass().getQualifiedName()
				+ "\" is no valid element type.";
		if (directionOfEdgeClasses[indexOfVertexClass] == EdgeDirection.IN) {
			getGraphBuilder().createEdge(
					persistentEdgeClasses[indexOfVertexClass], nameSpace,
					vertex);
		} else {
			assert directionOfEdgeClasses[indexOfVertexClass] == EdgeDirection.OUT;
			getGraphBuilder().createEdge(
					persistentEdgeClasses[indexOfVertexClass], vertex,
					nameSpace);
		}
	}

	/**
	 * @param vertexClass
	 *            {@link VertexClass} which index is requested
	 * @return <code>int</code> the index of the most specific
	 *         {@link VertexClass} in {@link SymbolTableStack#elementTypes}
	 *         which is a superclass of <code>vertexClass</code>.
	 */
	private int findIndexOfVertexClass(VertexClass vertexClass) {
		int indexOfVertexClass = -1;
		VertexClass previouslyFoundVertexClass = null;
		for (int i = 0; i < elementTypes.length; i++) {
			VertexClass currentlyFoundVertexClass = elementTypes[i];
			if (vertexClass == currentlyFoundVertexClass) {
				// the exact vertexclass was found
				indexOfVertexClass = i;
				break;
			} else if (currentlyFoundVertexClass.isSuperClassOf(vertexClass)) {
				// this is a superclass of vertexclass
				if (previouslyFoundVertexClass == null
						|| previouslyFoundVertexClass
								.isSuperClassOf(currentlyFoundVertexClass)) {
					// this is the first superclass OR
					// there already exists a superclass of vertexclass
					// but the new one is more specific
					previouslyFoundVertexClass = currentlyFoundVertexClass;
					indexOfVertexClass = i;
				}
			}
		}
		return indexOfVertexClass;
	}

	/**
	 * @return {@link Vertex} which represents the namespace for the top
	 *         {@link PersistentMap}
	 */
	public Vertex getNameSpace() {
		return ((PersistentMap) getTop()).getNameSpace();
	}

	/**
	 * All {@link Vertex} in the top {@link PersistentMap} will be connected to
	 * <code>nameSpace</code> via an corresponding {@link Edge} whose type is
	 * stored in {@link #persistentEdgeClasses} and its direction is stored in
	 * {@link #directionOfEdgeClasses}.
	 * 
	 * @param nameSpace
	 *            {@link Vertex} which represents the namespace for the top
	 *            {@link PersistentMap}. An {@link SymbolTableException} is
	 *            thrown if a namespace is already present.
	 * @return {@link Vertex} <code>nameSpace</code>
	 */
	public Vertex setNameSpace(Vertex nameSpace) {
		if (!nameSpace.isInstanceOf(this.nameSpace)) {
			throw new SymbolTableException("The vertexclass \""
					+ nameSpace.getAttributedElementClass().getQualifiedName()
					+ "\" of vertex " + nameSpace
					+ " is not an allowed namespace for symboltable \""
					+ nameOfSymbolTable + "\". Allowed vertexclass is \""
					+ this.nameSpace.getQualifiedName() + "\".");
		}

		((PersistentMap) getTop()).setNameSpace(nameSpace, nameOfSymbolTable);

		// connect all inserted vertices
		for (Vertex v : top.getMap().values()) {
			connectWithNameSpace(v);
		}

		return nameSpace;
	}
}
