package de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.edl2edlgraph.EDL2EDLGraph;
import de.uni_koblenz.jgralab.Graph;

public class Expressions_Test {

	private static final String SEARCH_PATH = "./testit/testmodules/edl2edlgraphtests/semantic_actions/expressions/";

	@Test
	public void nullLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("NullLiteral");
	}

	@Test
	public void booleanLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("BooleanLiteral");
	}

	@Test
	public void intLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("IntLiteral");
	}

	@Test
	public void longLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("LongLiteral");
	}

	@Test
	public void doubleLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("DoubleLiteral");
	}

	@Test
	public void stringLiteral() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("StringLiteral");
	}

	@Test
	public void tempVar() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("TemporaryVariable");
	}

	@Test(expected = GrammarException.class)
	public void unknownSymbolTable() throws UnsupportedEncodingException {
		Graph graph = new EDL2EDLGraph(SEARCH_PATH).parse("UnknownSymbolTable");
		assertTrue(graph.hasTemporaryElements());
	}

	@Test
	public void assignment() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("Assignment");
	}

	@Test
	public void methodCall() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("MethodCall");
	}

	@Test
	public void severalExpressions() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("SeveralExpressions");
	}

	@Test(expected = GrammarException.class)
	public void wrongUseOfAlpha() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongUseOfAlpha");
	}

	@Test(expected = GrammarException.class)
	public void wrongUseOfOmega() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongUseOfOmega");
	}

	@Test(expected = GrammarException.class)
	public void unknownEnumerationField() throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("UnknownEnumerationField");
	}

	@Test(expected = GrammarException.class)
	public void accessOfEnumerationFieldAtGraphElementClass()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("AccessOfEnumerationFieldAtGraphElementClass");
	}

	@Test(expected = GrammarException.class)
	public void callOfAbstractVertexClassConstructor()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("AbstractVertexClassConstructorCall");
	}

	@Test(expected = GrammarException.class)
	public void wrongNumberOfVertexConstructorParameters()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("WrongVertexConstructorCall");
	}

	@Test(expected = GrammarException.class)
	public void callOfAbstractEdgeClassConstructor()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("AbstractEdgeClassConstructorCall");
	}

	@Test(expected = GrammarException.class)
	public void wrongNumberOfEdgeConstructorParameters0()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("EdgeClassConstructorCallWith0Parameters");
	}

	@Test(expected = GrammarException.class)
	public void wrongNumberOfEdgeConstructorParameters1()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("EdgeClassConstructorCallWith1Parameter");
	}

	@Test(expected = GrammarException.class)
	public void wrongNumberOfEdgeConstructorParameters3()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH)
				.parse("EdgeClassConstructorCallWith3Parameters");
	}

	@Test(expected = GrammarException.class)
	public void constructorCallOfEnumeration()
			throws UnsupportedEncodingException {
		new EDL2EDLGraph(SEARCH_PATH).parse("ConstructorCallOfEnumeration");
	}

}
