package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class ImportDeclarationsSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/importDeclarationsSection/";

	@Test
	public void importDeclarations() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ImportDeclarations");
	}

}
