package de.uni_koblenz.edltest.preprocessor.edl2edlgraph;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Module_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/module/";

	@Test
	public void emptyModule() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Module-Empty");
	}

	@Test
	public void emptyModuleWithComplexName()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Module-ComplexName");
	}

	@Test
	public void moduleWithParameter() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Module-WithParameter");
	}

	@Test
	public void moduleWithSections() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("Module-WithSections");
	}

}
