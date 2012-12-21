package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator;

import java.io.IOException;

public class MultipleAppendable implements Appendable {

	private final Appendable[] appendables;

	public MultipleAppendable(Appendable... appendables) {
		this.appendables = appendables;
	}

	// private long start;

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		for (Appendable appendable : appendables) {
			appendable.append(csq);
		}
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		for (Appendable appendable : appendables) {
			appendable.append(csq, start, end);
		}
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		for (Appendable appendable : appendables) {
			appendable.append(c);
		}
		return this;
	}

}
