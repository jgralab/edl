package de.uni_koblenz.edl.parser.symboltable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_koblenz.edl.InternalGraphBuilder;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;

/**
 * May only be internally used by a {@link SymbolTableStack}. Maps an
 * identifying {@link Object} to a {@link Vertex}.
 */
public class Map {

	/**
	 * Maps an identifying {@link Object} to a {@link Vertex}.
	 */
	private final java.util.Map<Object, Vertex> map = new HashMap<Object, Vertex>();

	/**
	 * {@link Map} which is below the current {@link Map} in the current
	 * {@link SymbolTableStack}.
	 */
	private Map parent;

	/**
	 * All {@link Map}s which are or were above this {@link Map} in the current
	 * {@link SymbolTableStack}.
	 */
	private List<Map> children;

	/**
	 * @return {@link java.util.Map} {@link #map}
	 */
	public java.util.Map<Object, Vertex> getMap() {
		return map;
	}

	/**
	 * @param parent
	 *            {@link Map} which is set to {@link #parent}
	 */
	public void setParent(Map parent) {
		this.parent = parent;
	}

	/**
	 * @return {@link Map} which is below in the current
	 *         {@link SymbolTableStack}
	 */
	public Map getParent() {
		return parent;
	}

	/**
	 * @param child
	 *            {@link Map} which is appended to {@link #children}. Further
	 *            more this {@link Map} is set as the {@link #parent} of
	 *            <code>child</code>.
	 */
	public void addChild(Map child) {
		if (children == null) {
			children = new ArrayList<Map>();
		}
		children.add(child);
		child.setParent(this);
	}

	/**
	 * @return {@link List} of all child {@link Map}s of this {@link Map}
	 */
	public List<Map> getChildren() {
		return children;
	}

	/**
	 * @param index
	 *            <code>int</code> the index of the requested child {@link Map}
	 * @return {@link Map} which is the child with index <code>index</code> of
	 *         this {@link Map}
	 */
	public Map getChild(int index) {
		return children.get(index);
	}

	/**
	 * Puts <code>key</code> with its corresponding <code>value</code> in the
	 * top {@link Map} of this {@link SymbolTableStack}. If there has existed a
	 * {@link Vertex} <code>v</code> in the current {@link Map} which was
	 * identified by <code>key</code>, then:
	 * <ul>
	 * <li>if <code>v</code> is a {@link TemporaryVertex}, it is blessed to a
	 * {@link Vertex} of the same type as <code>value</code>. After that it is
	 * {@link Map#mergeVertices(java.util.Map, TemporaryVertex, Vertex)} with
	 * <code>value</code> and after that deleted. <code>positionMap</code> is
	 * updated.</li>
	 * <li>otherwise there existed a regular {@link Vertex} and an
	 * {@link SymbolTableException} is thrown.</li>
	 * </ul>
	 * 
	 * @param nameOfSymbolTable
	 *            {@link String}This is the name of the current instance of this
	 *            {@link SymbolTableStack} . It is the same name as it was
	 *            defined in an EDL grammar. It is only used for debug reasons
	 *            in {@link Exception} messages.
	 * @param graphBuilder
	 *            {@link InternalGraphBuilder} is used to merge vertices in case
	 *            of an previous {@link TemporaryVertex}.
	 * @param key
	 *            {@link Object} which identifies <code>value</code>
	 * @param value
	 *            {@link Vertex} which is identified by <code>key</code>
	 * @return {@link Vertex} <code>value</code>
	 */
	public Vertex declare(String nameOfSymbolTable,
			InternalGraphBuilder graphBuilder, Object key, Vertex value) {
		Vertex oldValue = map.put(key, value);
		if (oldValue != null) {
			if (oldValue.isTemporary()) {
				graphBuilder.mergeVertices(oldValue, value);
			} else {
				throw new SymbolTableException("In symboltable \""
						+ nameOfSymbolTable
						+ "\" already exists a vertex identified by key \""
						+ key.toString() + "\".");
			}
		}
		return value;
	}

	/**
	 * If <code>key</code> is known in {@link #map}, the identified
	 * {@link Vertex} is returned. If <code>key</code> is unknown and a
	 * {@link #parent} is present, it is asked. Otherwise <code>null</code> is
	 * returned.
	 * 
	 * @param key
	 *            {@link Object} which identifies the requested {@link Vertex}
	 * @return {@link Vertex}
	 */
	public Vertex use(Object key) {
		Vertex result = map.get(key);
		if (result == null && parent != null) {
			// in the current Map key could not be found
			// look in the parent map
			return parent.use(key);
		}
		return result;
	}

	/**
	 * Adds all {@link TemporaryVertex} of this Map followed by the
	 * {@link TemporaryVertex} of all parents of this {@link Map} into
	 * <code>listOfTempVertices</code>.
	 * 
	 * @param listOfTempVertices
	 *            {@link List}&lt;{@link TemporaryVertex}&gt; in which all
	 *            {@link TemporaryVertex} will be added
	 */
	public void getTemporaryVertices(List<TemporaryVertex> listOfTempVertices) {
		for (Vertex v : map.values()) {
			if (v.isTemporary()) {
				listOfTempVertices.add((TemporaryVertex) v);
			}
		}
		if (parent != null) {
			parent.getTemporaryVertices(listOfTempVertices);
		}
	}

	/**
	 * Adds all {@link TemporaryVertex} of this Map followed by the
	 * {@link TemporaryVertex} of all children of this {@link Map} into
	 * <code>listOfTempVertices</code>.
	 * 
	 * @param listOfTempVertices
	 *            {@link List}&lt;{@link TemporaryVertex}&gt; in which all
	 *            {@link TemporaryVertex} will be added
	 */
	public void getAllTemporaryVertices(List<TemporaryVertex> listOfTempVertices) {
		for (Vertex v : map.values()) {
			if (v.isTemporary()) {
				listOfTempVertices.add((TemporaryVertex) v);
			}
		}
		if (children != null) {
			for (Map child : children) {
				if (child != null) {
					child.getTemporaryVertices(listOfTempVertices);
				}
			}
		}
	}

	@Override
	public String toString() {
		return map.toString();
	}

}
