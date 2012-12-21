package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Sort_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/sort/";

	@Test
	public void sort() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Sort");
	}

}
