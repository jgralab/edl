package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Priorities_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/priorities/";

	@Test
	public void typesOfPriorities() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TypesOfPriorities");
	}

	@Test
	public void assocPriorities() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AssocPriorities");
	}

	@Test
	public void chainPriorities() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ChainPriorities");
	}

	@Test
	public void groupsOfPriorities() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("GroupTypes");
	}

	@Test
	public void argumentIndicator() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ArgumentIndicator");
	}

	@Test
	public void argumentIndicatorInAssocPriority()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ArgumentIndicatorForbidden");
	}

	@Test
	public void intransitivit() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Intransitivity");
	}

	@Test
	public void intransitivityInAssocPriority()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IntransitivityForbidden");
	}

	@Test(expected = GrammarException.class)
	public void priorityWithSemanticActionInHead()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed1");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionPriorityWithSemanticActionInHead()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed2");
	}

	@Test(expected = GrammarException.class)
	public void priorityWithSemanticActionInBody()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed3");
	}

}
