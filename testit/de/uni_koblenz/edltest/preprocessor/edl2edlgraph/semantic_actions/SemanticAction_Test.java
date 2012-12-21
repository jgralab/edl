package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class SemanticAction_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/semantic_actions/semanticaction/";

	@Test
	public void semanticActionTypes() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SemanticActionTypes");
	}

}
