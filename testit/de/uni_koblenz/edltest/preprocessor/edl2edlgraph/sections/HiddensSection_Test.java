package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class HiddensSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/hiddenSections/";

	@Test
	public void emptyHiddens() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyHiddens");
	}

	@Test
	public void nonEmptyHiddens() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NonEmptyHiddens");
	}

}
