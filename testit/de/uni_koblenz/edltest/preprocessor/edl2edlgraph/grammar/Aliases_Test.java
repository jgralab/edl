package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Aliases_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/aliases/";

	@Test
	public void emptyAliases() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyAliases");
	}

	@Test
	public void oneAliases() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OneAliases");
	}

	@Test
	public void twoAliases() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoAliases");
	}

}
