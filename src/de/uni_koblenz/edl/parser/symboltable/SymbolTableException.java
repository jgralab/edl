package de.uni_koblenz.edl.parser.symboltable;

/**
 * This {@link RuntimeException} is thrown by {@link SymbolTableStack}s.
 */
public class SymbolTableException extends RuntimeException {

	private static final long serialVersionUID = 2679227897570386729L;

	public SymbolTableException() {
	}

	public SymbolTableException(String msg) {
		super(msg);
	}

	public SymbolTableException(Throwable t) {
		super(t);
	}

	public SymbolTableException(String msg, Throwable t) {
		super(msg, t);
	}

}
