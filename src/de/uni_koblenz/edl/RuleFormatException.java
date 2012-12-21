package de.uni_koblenz.edl;

/**
 * This exception is thrown if a rule defined in an EDL grammar has a wrong
 * format like two <code>definedAs("...")</code> attributes.
 */
public class RuleFormatException extends RuntimeException {

	private static final long serialVersionUID = -5231769826378275938L;

	public RuleFormatException() {
	}

	public RuleFormatException(String msg) {
		super(msg);
	}

	public RuleFormatException(Throwable t) {
		super(t);
	}

	public RuleFormatException(String msg, Throwable t) {
		super(msg, t);
	}

}
