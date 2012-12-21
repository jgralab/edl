package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class AbbreviationTerm_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/abbreviationterm/";

	@Test
	public void tuple() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Tuples");
	}

	@Test(expected = GrammarException.class)
	public void tupleBodyVar() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TuplesUnknownBodyVar");
	}

	@Test
	public void function() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Function");
	}

	@Test(expected = GrammarException.class)
	public void functionForbiddenSemanticActionPosition()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("FunctionForbidden");
	}

	@Test
	public void strategy() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Strategy");
	}

	@Test(expected = GrammarException.class)
	public void strategyForbiddenSemanticActionPosition()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("StrategyForbidden");
	}

}
