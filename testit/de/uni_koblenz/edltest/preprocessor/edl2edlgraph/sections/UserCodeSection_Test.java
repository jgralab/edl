package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class UserCodeSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/userCodeSection/";

	@Test
	public void emptyUserCode() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptyUserCode");
	}

	@Test
	public void oneUserCode() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OneUserCode");
	}

	@Test
	public void sevaralUserCodesInDifferentModules()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralUserCode");
	}

}
