package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class SymbolTablesSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/symbolTablesSection/";

	@Test
	public void oneSymbolTable() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("OneSymbolTable");
	}

	@Test(expected = GrammarException.class)
	public void oneSymbolTableWithEdgeClassElementType()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EnumElement");
	}

	@Test(expected = GrammarException.class)
	public void oneSymbolTableWithEnumElementType()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EdgeElement");
	}

	@Test
	public void twoSymbolTablesWithSeveralElementTypes()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralElementTypes");
	}

	@Test(expected = GrammarException.class)
	public void twoSymbolTablesWithNameClash()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NameClash");
	}

	@Test
	public void twoSymbolTableSections() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoTableSections");
	}

	@Test
	public void symbolTableSectionsInTwoModulesWithHiding()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("HidingTable");
	}

	@Test(expected = GrammarException.class)
	public void symbolTableSectionsInTwoModulesWithHidingAndNameClash()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NameClashInModule");
	}

	@Test
	public void persistentSymbolTables() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PesistentTables");
	}

	@Test(expected = GrammarException.class)
	public void persistentSymbolTablesWithAbstractEdgeClass()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AbstractEdgeClass");
	}

	@Test(expected = GrammarException.class)
	public void persistentSymbolTablesWithWrongOmegaEdgeClass()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongOmega");
	}

	@Test(expected = GrammarException.class)
	public void persistentSymbolTablesWithWrongAlphaEdgeClass()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongAlpha");
	}

	@Test(expected = GrammarException.class)
	public void persistentSymbolNameClash() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("PersistentNameClash");
	}

}
