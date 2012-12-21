package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class ImportsSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/importSections/";

	@Test
	public void importInDefinition() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ImportsInDefinition");
	}

	@Test
	public void importInModuleFile() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Test1");
	}

	@Test(expected = GrammarException.class)
	public void importOfUnknownModuleInModuleFile()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownModuleImport");
	}

	@Test(expected = GrammarException.class)
	public void importOfUnknownModuleInDefinition()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownModuleImportInDefinition");
	}

	@Test
	public void importWithParenthesis() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ImportWithParenthesis");
	}

	@Test
	public void importWithParenthesis2() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Test11");
	}

	@Test
	public void importWithRenamings() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ImportWithRenamings");
	}

}
