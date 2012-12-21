package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Production_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/production/";

	@Test
	public void productionWithoutSemanticAction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WithoutSemanticAction");
	}

	@Test
	public void productionWithEmptySemanticAction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EmptySemanticAction");
	}

	@Test
	public void withDifferentTerms() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralTerms");
	}

	@Test
	public void productionWithAnnotationInSeveralClasses()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Annotations");
	}

	@Test
	public void productionWithSeveralSemanticActions()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralSemanticActions");
	}

	@Test(expected = GrammarException.class)
	public void productionWithSemanticActionInHead()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed1");
	}

	@Test(expected = GrammarException.class)
	public void prefixFunctionWithSemanticActionInHead()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NoSemanticActionAllowed2");
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
	public void unknownSymbolTableAnnotation()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AnnotationOfNonExistingTable1");
	}

	@Test(expected = GrammarException.class)
	public void unknownSymbolTableAnnotationInPrefixFunction()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AnnotationOfNonExistingTable2");
	}

}
