package de.uni_koblenz.edl;

public class SemanticActionException extends RuntimeException {

	private static final long serialVersionUID = -840195339284248872L;

	public SemanticActionException(String message) {
		super(message);
	}

	public SemanticActionException(String message, Exception e) {
		super(message, e);
	}

	public SemanticActionException(String message, Throwable t) {
		super(message, t);
	}

	public SemanticActionException(Throwable t) {
		super(t);
	}

}
