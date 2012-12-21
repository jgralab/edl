package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Pattern_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/pattern/";

	@Test
	public void patternWithEmptySemanticAction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptySemanticAction");
	}

	@Test
	public void withDifferentTermsAndMultiplicities()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralTermsAndMultiplicities");
	}

	@Test
	public void withDifferentVariableUses() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("VariableUse");
	}

	@Test
	public void patternWithAnnotations() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Annotations");
	}

	@Test
	public void patternWithSeveralFollowingSemanticActions()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralSemanticActions");
	}

	@Test(expected = GrammarException.class)
	public void withSemanticActionInBody() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed1");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionWithSemanticActionInBody()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed2");
	}

	@Test(expected = GrammarException.class)
	public void withMultiplicityAtHead() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("MultiplicityAtHead1");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionWithMultiplicityAtHead()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("MultiplicityAtHead2");
	}

	@Test(expected = GrammarException.class)
	public void wrongMultiplicity() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongMultiplicity");
	}

	@Test
	public void zeroMultiplicity() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ZeroMultiplicity");
	}

	@Test(expected = GrammarException.class)
	public void bodyVarReferencesANonExistingTerm()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ReferenceOfNonExistingTerm1");
	}

	@Test(expected = GrammarException.class)
	public void bodyVarReferencesANonExistingTermInPrefixFunction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ReferenceOfNonExistingTerm2");
	}

	@Test(expected = GrammarException.class)
	public void bodyVarUseInBeforePattern() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BodyVarInBefore1");
	}

	@Test(expected = GrammarException.class)
	public void bodyVarUseInBeforePatternInPrefixFunction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BodyVarInBefore2");
	}

	@Test(expected = GrammarException.class)
	public void labeledBodyVarUseInPattern()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("LabeledVarUse1");
	}

	@Test(expected = GrammarException.class)
	public void labeledBodyVarUseInBeforePatternInPrefixFunction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("LabeledVarUse2");
	}

}
