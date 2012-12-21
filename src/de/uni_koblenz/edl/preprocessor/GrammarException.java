package de.uni_koblenz.edl.preprocessor;

public class GrammarException extends RuntimeException {

	private static final long serialVersionUID = 8528595680954126252L;

	public GrammarException() {
	}

	public GrammarException(String message) {
		super(message);
	}

	public GrammarException(Throwable cause) {
		super(cause);
	}

	public GrammarException(String message, Throwable cause) {
		super(message, cause);
	}

	public GrammarException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
