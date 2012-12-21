package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Basic_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/basic/";

	@Test
	public void basicTerms() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BasicTerms");
	}

}
