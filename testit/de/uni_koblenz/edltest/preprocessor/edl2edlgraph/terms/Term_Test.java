package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Term_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/terms/term/";

	@Test
	public void bodyTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BodyTest");
	}

	@Test
	public void sequenceTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SequenceTest");
	}

	@Test(expected = GrammarException.class)
	public void sequenceBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SequenceBodyVar");
	}

	@Test
	public void optionTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OptionTest");
	}

	@Test(expected = GrammarException.class)
	public void optionBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OptionBodyVar");
	}

	@Test
	public void repetitionStarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("RepetitionStarTest");
	}

	@Test(expected = GrammarException.class)
	public void repetitionStarBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("RepetitionStarBodyVar");
	}

	@Test
	public void repetitionPlusTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("RepetitionPlusTest");
	}

	@Test(expected = GrammarException.class)
	public void repetitionPlusBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("RepetitionPlusBodyVar");
	}

	@Test
	public void alternativeTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AlternativeTest");
	}

	@Test(expected = GrammarException.class)
	public void alternativeBodyVarTest1() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AlternativeBodyVar1");
	}

	@Test(expected = GrammarException.class)
	public void alternativeBodyVarTest2() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AlternativeBodyVar2");
	}

	@Test
	public void listPlusTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListPlusTest");
	}

	@Test(expected = GrammarException.class)
	public void listPlusTestForbiddenAssignment()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListPlusForbiddenAssignment");
	}

	@Test(expected = GrammarException.class)
	public void listPlusTestBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListPlusBodyVar");
	}

	@Test
	public void listStarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListStarTest");
	}

	@Test(expected = GrammarException.class)
	public void listStarTestForbiddenAssignment()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListStarForbiddenAssignment");
	}

	@Test(expected = GrammarException.class)
	public void listStarTestBodyVarTest() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ListStarBodyVar");
	}

}
