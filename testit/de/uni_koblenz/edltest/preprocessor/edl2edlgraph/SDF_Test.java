package de.uni_koblenz.edltest.preprocessor.edl2edlgraph;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class SDF_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sdf/";

	@Test
	public void emptyDefinition() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Definition-Empty");
	}

	@Test
	public void definitionWithSeveralModules()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Definition-WithModulesAndCyclicImport");
	}

	@Test
	public void emptyModule() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Module-Empty");
	}

}
