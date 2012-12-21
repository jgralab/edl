package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;

public class DefaultValuesSection_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/sections/defaultValuesSection/";

	@Test
	public void graphElementWithSimpleName()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SimpleName");
	}

	@Test
	public void graphElementWithQualifiedName()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("QualifiedName");
	}

	@Test(expected = GrammarException.class)
	public void unknownField() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownField");
	}

	@Test(expected = GrammarException.class)
	public void accessGraphClass() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AccessGraphClass");
	}

	@Test(expected = GrammarException.class)
	public void accessEnumDomain() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("EnumDomain");
	}

	@Test(expected = GrammarException.class)
	public void duplicateSimpleName() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("DuplicateSimpleName");
	}

	@Test(expected = GrammarException.class)
	public void secondElementUnknown() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SecondElementUnknown");
	}

	@Test(expected = GrammarException.class)
	public void secondDuplicateSimpleName() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SecondDuplicateSimpleName");
	}

	@Test
	public void twoAssignments() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TwoAssignments");
	}

	@Test
	public void combineTwoDefaultActions() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("CombineTwoDefaultValues");
	}

	@Test
	public void ignoreDefaultValuesInNonStartModule()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IgnoreInNonStartModule");
	}

	@Test(expected = GrammarException.class)
	public void variableUse() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("VariableUse");
	}

	@Test
	public void reuseOfGraphElementClass() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ReuseClass");
	}

	@Test
	public void reuseOfGraphElementClassAndField()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ReuseClassAndField");
	}

	@Test(expected = GrammarException.class)
	public void missingSchemaImport() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("MissingSchema");
	}

	@Test(expected = GrammarException.class)
	public void headVariableUse() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("HeadVariableUse");
	}

	@Test(expected = GrammarException.class)
	public void bodyVariableUse() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BodyVariableUse");
	}

	@Test(expected = GrammarException.class)
	public void temporaryVariableUse() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TemporaryVariableUse");
	}

	@Test(expected = GrammarException.class)
	public void tableVariableUse() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TableVariableUse");
	}

}
