package de.uni_koblenz.edl.parser.symboltable;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.edl.GraphBuilder;
import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.edl.parser.Position;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class is the realization of the symbol tables in EDL. It represents a
 * stack of {@link Map}s.
 */
public class SymbolTableStack {

	/**
	 * This {@link InternalGraphBuilder} is used for the creation of temporary
	 * vertices and in case of a {@link PersistentSymbolTableStack} to create
	 * edges which make the table persistent in the {@link Graph} built by this
	 * {@link GraphBuilder}.
	 */
	private final InternalGraphBuilder graphBuilder;

	/**
	 * This {@link Map} is the bottom of the symbol table stack. It must never
	 * be <code>null</code>.
	 */
	protected Map bottom;

	/**
	 * This {@link Map} is the top of the symbol table stack. It must never be
	 * <code>null</code>.
	 */
	protected Map top;

	/**
	 * This array stores the allowed {@link VertexClass}es. A {@link Vertex} may
	 * only be inserted in this symbol table, if its type ore one of its
	 * supertypes is contained in this array.
	 */
	protected VertexClass[] elementTypes;

	/**
	 * This is the name of the current instance of this {@link SymbolTableStack}
	 * . It is the same name as it was defined in an EDL grammar. It is only
	 * used for debug reasons in {@link Exception} messages.
	 */
	protected String nameOfSymbolTable;

	/**
	 * Creates a new {@link SymbolTableStack} with an empty {@link Map} as
	 * bottom element.
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
	 *            the table persistent in the {@link Graph} built by this
	 *            {@link GraphBuilder}.
	 * @param elementTypes
	 *            {@link VertexClass}[] This array stores the allowed
	 *            {@link VertexClass}es. A {@link Vertex} may only be inserted
	 *            in this symbol table, if its type ore one of its supertypes is
	 *            contained in this array.
	 */
	public SymbolTableStack(String nameOfSymbolTable,
			InternalGraphBuilder graphBuilder, VertexClass[] elementTypes) {
		this.nameOfSymbolTable = nameOfSymbolTable;
		this.graphBuilder = graphBuilder;
		this.elementTypes = elementTypes;
		push();
	}

	public void reset() {
		bottom = null;
		top = null;
		push();
	}

	/**
	 * @return {@link InternalGraphBuilder} {@link #graphBuilder}
	 */
	InternalGraphBuilder getGraphBuilder() {
		return graphBuilder;
	}

	/**
	 * @return {@link Map} which is the {@link #bottom} in this
	 *         {@link SymbolTableStack}.
	 */
	public Map getBottom() {
		return bottom;
	}

	/**
	 * @return {@link Map} which is the {@link #top} in this
	 *         {@link SymbolTableStack}.
	 */
	public Map getTop() {
		return top;
	}

	/**
	 * Creates a new {@link Map} and pushes it on this {@link SymbolTableStack}.
	 */
	public void push() {
		Map newMap = new Map();
		pushNewMap(newMap);
	}

	/**
	 * Pushes <code>newMap</code> on this {@link SymbolTableStack}.
	 * 
	 * @param newMap
	 *            {@link Map} which will be pushed on this
	 *            {@link SymbolTableStack}
	 */
	protected void pushNewMap(Map newMap) {
		if (bottom == null) {
			// this SymbolTableStack is empty
			bottom = newMap;
			top = newMap;
		} else {
			top.addChild(newMap);
			top = newMap;
		}
	}

	/**
	 * Removes the {@link #top} from this {@link SymbolTableStack}.
	 */
	public void pop() {
		assert top != bottom : "The bottom Map of the SymbolTableStack \""
				+ nameOfSymbolTable + "\" must not be removed.";
		top = top.getParent();
	}

	/**
	 * If the {@link VertexClass} of <code>value</code> is not allowed for this
	 * {@link SymbolTableStack} then a {@link SymbolTableException} is thrown.<br>
	 * Otherwise this method puts <code>key</code> with its corresponding
	 * <code>value</code> in the top {@link Map} of this
	 * {@link SymbolTableStack}. If there has existed a {@link Vertex}
	 * <code>v</code> in the current {@link Map} which was identified by
	 * <code>key</code>, then:
	 * <ul>
	 * <li>if <code>v</code> is a {@link TemporaryVertex}, it is blessed to a
	 * {@link Vertex} of the same type as <code>value</code>. After that it is
	 * {@link Map#mergeVertices(java.util.Map, TemporaryVertex, Vertex)} with
	 * <code>value</code> and after that deleted.</li>
	 * <li>otherwise there existed a regular {@link Vertex} and an
	 * {@link SymbolTableException} is thrown.</li>
	 * </ul>
	 * 
	 * @param key
	 *            {@link Object} which identifies <code>value</code>
	 * @param value
	 *            {@link Vertex} which is identified by <code>key</code>
	 * @return {@link Vertex} <code>value</code>
	 */
	public Vertex declare(Object key, Vertex value) {
		if (value.isTemporary()
				|| isAllowedElementType(value.getAttributedElementClass())) {
			return top.declare(nameOfSymbolTable, graphBuilder, key, value);
		} else {
			throw new SymbolTableException("The vertexclass \""
					+ value.getAttributedElementClass().getQualifiedName()
					+ "\" of vertex " + value
					+ " is not an allowed vertexclass for symboltable \""
					+ nameOfSymbolTable + "\". Allowed vertexclases are:\n"
					+ printVertexClasses());
		}
	}

	/**
	 * Tries to find a {@link Vertex} in the top {@link Map} of this
	 * {@link SymbolTableStack}. If it could not be found there, this method
	 * tries to find it in the {@link Map} below the current one and so on. The
	 * first found {@link Vertex} identified by <code>key</code> is returned.
	 * Otherwise <code>null</code> is returned.
	 * 
	 * @param key
	 *            {@link Object} which identifies the wished {@link Vertex}
	 * @return {@link Vertex} which is identified by <code>key</code>
	 */
	public Vertex use(Object key) {
		return top.use(key);
	}

	/**
	 * This method checks if a non temporary {@link Vertex} is already
	 * identified with <code>key</code> (see {@link #use(Object, Position)}).<br>
	 * If no such {@link Vertex} could be found, a {@link TemporaryVertex} is
	 * created and registered under <code>key</code>. The position of its
	 * recognized lexem is <code>position</code>.
	 * 
	 * @param key
	 *            {@link Object} which identifies <code>value</code>
	 * @param position
	 *            {@link Position}If a {@link TemporaryVertex} is newly created,
	 *            it is put in {@link #positionMap} with <code>position</code>
	 *            as value.
	 * @return {@link Vertex}
	 */
	public Vertex useOrDeclare(Object key, Position position) {
		Vertex result = use(key);
		if (result != null) {
			return result;
		}
		// key could not be found
		// create a temporary vertex and insert it into the map
		TemporaryVertex tempVertex = graphBuilder
				.createTemporaryVertex(position);
		return declare(key, tempVertex);
	}

	/**
	 * This method checks if a non temporary {@link Vertex} is already
	 * identified with <code>key</code> (see {@link #use(Object, Position)}).<br>
	 * If no such {@link Vertex} could be found, a new <code>vertexClass</code>
	 * instance is created and registered under <code>key</code>. The position
	 * of its recognized lexem is <code>position</code>.
	 * 
	 * @param key
	 *            {@link Object} which identifies <code>value</code>
	 * @param vertexClass
	 *            {@link VertexClass}
	 * @param position
	 *            {@link Position}If a {@link TemporaryVertex} is newly created,
	 *            it is put in {@link #positionMap} with <code>position</code>
	 *            as value.
	 * @return {@link Vertex}
	 */
	public Vertex useOrDeclare(Object key, VertexClass vertexClass,
			Position position) {
		Vertex result = use(key);
		if (result != null) {
			return result;
		}
		// key could not be found
		// create a temporary vertex and insert it into the map
		result = graphBuilder.createVertex(vertexClass, position);
		return declare(key, result);
	}

	/**
	 * Returns a {@link List} of {@link TemporaryVertex} which are contained in
	 * all {@link Map}s from {@link #top} to {@link #bottom}.
	 * 
	 * @return {@link List}&lt;{@link TemporaryVertex}&gt;
	 */
	public List<TemporaryVertex> getTemporaryVertices() {
		ArrayList<TemporaryVertex> listOfTempVertices = new ArrayList<TemporaryVertex>();
		top.getTemporaryVertices(listOfTempVertices);
		return listOfTempVertices;
	}

	/**
	 * Returns a {@link List} of {@link TemporaryVertex} which are contained in
	 * all {@link Map}s.
	 * 
	 * @return {@link List}&lt;{@link TemporaryVertex}&gt;
	 */
	public List<TemporaryVertex> getAllTemporaryVertices() {
		ArrayList<TemporaryVertex> listOfTempVertices = new ArrayList<TemporaryVertex>();
		bottom.getAllTemporaryVertices(listOfTempVertices);
		return listOfTempVertices;
	}

	/**
	 * Returns <code>true</code> if <code>vertexClass</code> or one of its
	 * superclasses is part of {@link #elementTypes}.
	 * 
	 * @param vertexClass
	 *            {@link VertexClass} which is checked
	 * @return <code>boolean</code>
	 */
	private boolean isAllowedElementType(VertexClass vertexClass) {
		for (VertexClass elementType : elementTypes) {
			if (elementType == vertexClass
					|| elementType.isSuperClassOf(vertexClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return {@link String} which contains the qualified name of all
	 *         {@link VertexClass}es contained in {@link #elementTypes}
	 *         separated by commas
	 */
	private String printVertexClasses() {
		StringBuilder namesOfVertexClasses = new StringBuilder();
		String delim = "";
		for (VertexClass elementType : elementTypes) {
			namesOfVertexClasses.append(delim).append(
					elementType.getQualifiedName());
			delim = ", ";
		}
		return nameOfSymbolTable.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOfSymbolTable
				+ " from top to bottom:");
		for (Map map = top; map != null; map = map.getParent()) {
			sb.append("\n").append(map);
		}
		return sb.toString();
	}
}
