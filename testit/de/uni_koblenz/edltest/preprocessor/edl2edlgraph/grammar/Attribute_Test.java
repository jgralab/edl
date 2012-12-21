package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Attribute_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/attribute/";

	@Test
	public void noAttributes() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoAttributes");
	}

	@Test
	public void emptyAttributes() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyAttributes");
	}

	@Test
	public void attributes() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Attributes");
	}

	@Test
	public void idSelfModule() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IdSelf");
	}

	@Test
	public void idNextModule() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IdNext");
	}

	@Test
	public void idPreviousModule() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IdPrevious");
	}

}
