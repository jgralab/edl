package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class IslandSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/islandSection/";

	@Test
	public void islandSections() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IslandSection");
	}

	@Test(expected = GrammarException.class)
	public void wrongExclusiveRegExp() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongStart");
	}

	@Test(expected = GrammarException.class)
	public void wrongInclusiveRegExp() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongEnd");
	}

	@Test
	public void missingIslandStart() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("MissingIslandStart");
	}

}
