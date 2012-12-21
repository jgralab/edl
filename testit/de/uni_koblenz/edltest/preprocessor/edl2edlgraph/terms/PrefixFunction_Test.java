package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class PrefixFunction_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/prefixfunction/";

	@Test
	public void prefixFunctions() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PrefixFunctions");
	}

	@Test
	public void emptyPrefixFunctionWithSemanticActions()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyPrefixFunction");
	}

	@Test
	public void prefixFunctionWithSemanticActions()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PrefixFunctionWithSemanticAction");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionBodyVar() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PrefixFunctionUnknownBodyVar");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionForbiddenSemanticActionPosition()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PrefixFunctionForbidden");
	}

}
