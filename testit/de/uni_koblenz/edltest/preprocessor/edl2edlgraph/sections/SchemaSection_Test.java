package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class SchemaSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/schemaSection/";

	@Test
	public void knownSchema() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("KnownSchema");
	}

	@Test
	public void ignoredSchema() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IgnoredSchema");
	}

	@Test(expected = GrammarException.class)
	public void unknownSchema() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownSchema");
	}

}
