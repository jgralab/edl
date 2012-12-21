package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class Renaming_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/grammar/renaming/";

	@Test
	public void renamingOfTerms() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TermRenaming");
	}

	@Test
	public void renamingOfProductions() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ProductionRenaming");
	}

	@Test
	public void severalRenamings() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ProductionAndTermRenaming");
	}
}
