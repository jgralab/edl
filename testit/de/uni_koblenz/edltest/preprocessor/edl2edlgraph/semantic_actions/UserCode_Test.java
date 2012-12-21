package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class UserCode_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/semantic_actions/usercode/";

	@Test
	public void usercode() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UserCode");
	}

	@Test
	public void flattenUsercode() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("FlattenUserCode");
	}

	@Test(expected = GrammarException.class)
	public void usercodeAsExpressionWithNoReturn()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UserCodeExpression");
	}

}
