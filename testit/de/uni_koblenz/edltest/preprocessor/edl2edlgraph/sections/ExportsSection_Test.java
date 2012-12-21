package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class ExportsSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/exportSections/";

	@Test
	public void emptyExports() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyExports");
	}

	@Test
	public void nonEmptyExports() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NonEmptyExports");
	}

}
