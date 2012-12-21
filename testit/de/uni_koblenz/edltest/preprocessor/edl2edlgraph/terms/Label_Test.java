package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Label_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/label/";

	@Test
	public void label() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Label");
	}

	@Test(expected = GrammarException.class)
	public void labelSeveralTime() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralEqualLabels");
	}

	@Test(expected = GrammarException.class)
	public void labelTempVarNameClash() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("LabelTempVarClash");
	}

}
