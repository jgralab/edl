package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Literal_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/literal/";

	@Test
	public void literal() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Literal");
	}

}
