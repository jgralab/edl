package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class GlobalActionsSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/globalActionsSection/";

	@Test
	public void simpleGlobalAction() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SimpleGlobalAction");
	}

	@Test
	public void twoPatternsInGlobalAction() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoPatternsGlobalAction");
	}

	@Test
	public void twoGlobalActionsInOneModule()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoGlobalActionInOneModule");
	}

	@Test
	public void globalActionsInTwoModules() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("GlobalActionInTwoModules");
	}

	@Test
	public void removedGlobalActions() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("RemovedGlobalActions");
	}

}
