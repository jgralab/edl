package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Sorts_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/sorts/";

	@Test
	public void typesOfSorts() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TypesOfSorts");
	}

}
