package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class StartSymbols_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/startsymbols/";

	@Test
	public void typesOfStartSymbols() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TypesOfStartSymbols");
	}

	@Test
	public void semanticActionsAtStartSymbols()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SemanticActionsAtStartSymbols");
	}

	@Test(expected = GrammarException.class)
	public void unknownBodyVarAtStartSymbols()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownBodyVarAtStartSymbols");
	}

}
