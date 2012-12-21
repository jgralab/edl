package de.uni_koblenz.edl.parser.symboltable;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * May only be internally used by a {@link PersistentSymbolTableStack}. Maps an
 * identifying {@link Object} to a {@link Vertex}.
 */
public class PersistentMap extends Map {

	/**
	 * Represents the namespace for all {@link Vertex} in {@link Map#map}.
	 */
	private Vertex nameSpace;

	/**
	 * @return {@link Vertex} which represents the namespace for this
	 *         {@link PersistentMap}
	 */
	public Vertex getNameSpace() {
		return nameSpace;
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
	 * @param nameOfSymbolTable
	 *            {@link String}This is the name of the current instance of this
	 *            {@link SymbolTableStack} . It is the same name as it was
	 *            defined in an EDL grammar. It is only used for debug reasons
	 *            in {@link Exception} messages.
	 */
	public void setNameSpace(Vertex nameSpace, String nameOfSymbolTable) {
		if (this.nameSpace != null) {
			throw new SymbolTableException(
					"The namespace for the current Map of the symboltable \""
							+ nameOfSymbolTable + "\" was already set.");
		}
		this.nameSpace = nameSpace;
	}

	/**
	 * @param key
	 *            {@link Object} which is checked of containment
	 * @return <code>boolean</code> <code>true</code> if <code>key</code> is
	 *         contained in this {@link Map#map} or one of its parents.
	 */
	protected boolean containsKey(Object key) {
		return getMap().containsKey(key)
				|| (getParent() != null && ((PersistentMap) getParent())
						.containsKey(key));
	}
}
