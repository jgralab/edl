package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class ATerms_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/aterms/";

	@Test
	public void oneATerm() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OneATerm");
	}

	@Test
	public void twoATerms() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoATerms");
	}

	@Test(expected = GrammarException.class)
	public void definedAsATerm() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("DefinedAs");
	}

}
