package de.uni_koblenz.edltest.parser;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.Label;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;
import de.uni_koblenz.edl.preprocessor.sdfgenerator.SDFGenerator;

public class RuleTest {

	private final static String outputPathForTestTables;
	static {
		String tmp = System.getProperty("java.io.tmpdir");
		if (!tmp.endsWith(File.separator)) {
			tmp += File.separator;
		}
		outputPathForTestTables = tmp;
	}

	private final String searchSpace = "./";

	private String generateTable(String sdfModuleName)
			throws InterruptedException, IOException {
		String outputpath = outputPathForTestTables
				+ sdfModuleName.replace('/', '_') + ".tbl";

		Process process = Runtime.getRuntime().exec(
				SDFGenerator.sdf2tableName() + " -c -m " + sdfModuleName
						+ " -p " + searchSpace + " -o " + outputpath);
		BufferedInputStream errorstream = new BufferedInputStream(
				process.getErrorStream());
		BufferedInputStream inputstream = new BufferedInputStream(
				process.getInputStream());

		process.waitFor();

		int c = -1;
		while ((c = inputstream.read()) != -1) {
			System.out.print((char) c);
		}

		if (process.exitValue() != 0) {
			StringBuilder sb = new StringBuilder();
			int e = -1;
			while ((e = errorstream.read()) != -1) {
				sb.append((char) e);
			}
			throw new ParseError(SDFGenerator.sdf2tableName() + " has error:\n"
					+ sb.toString() + "\n");
		}

		return outputpath;
	}

	private void checkRule(int number, IStrategoTerm production, RuleType type,
			String derivedRepresentation, String definedRepresentation,
			boolean isContextFree) {
		Rule rule = new Rule(number, production);
		assertEquals("Wrong number of rule: " + rule.toString(), number,
				rule.getNumber());
		assertEquals("Wrong rule: " + rule.toString(), production,
				rule.getRule());
		assertEquals("Wrong type of rule: " + rule.toString(), type,
				rule.getType());
		assertEquals(
				"Wrong derived representation of rule: " + rule.toString(),
				derivedRepresentation, rule.toString());
		assertEquals(
				"Wrong defined representation of rule: " + rule.toString(),
				definedRepresentation, rule.getDefinedRepresentation());
		assertEquals("The rule " + rule.toString() + " was "
				+ (isContextFree ? "" : "not") + "expected as context free.",
				isContextFree, rule.isContextFree());
	}

	@Test
	public void testTerms() throws InterruptedException, IOException,
			InvalidParseTableException {
		String parseTablePath = generateTable("testit/testmodules/ruletests/Terms");
		final TermFactory factory = new TermFactory();
		final IStrategoTerm tableTerm = new TermReader(factory)
				.parseFromFile(parseTablePath);
		final ParseTable parseTable = new ParseTable(tableTerm, factory);

		List<Label> labels = parseTable.getLabels();
		assertEquals(415, labels.size() - 1);

		checkRule(257, labels.get(257).getProduction(), RuleType.LITERAL,
				"[\\)] -> \")\"", null, false);
		checkRule(258, labels.get(258).getProduction(), RuleType.LITERAL,
				"[\\(] -> \"(\"", null, false);
		checkRule(259, labels.get(259).getProduction(), RuleType.LITERAL,
				"[\\>] -> \">\"", null, false);
		checkRule(260, labels.get(260).getProduction(), RuleType.LITERAL,
				"[\\,] -> \",\"", null, false);
		checkRule(261, labels.get(261).getProduction(), RuleType.LITERAL,
				"[\\<] -> \"<\"", null, false);
		checkRule(262, labels.get(262).getProduction(), RuleType.LITERAL,
				"[f] [u] [n] [c] [3] -> \"func3\"", null, false);
		checkRule(263, labels.get(263).getProduction(), RuleType.LITERAL,
				"[f] [u] [n] [c] [2] -> \"func2\"", null, false);
		checkRule(264, labels.get(264).getProduction(), RuleType.LITERAL,
				"[f] [u] [n] [c] [1] -> \"func1\"", null, false);
		checkRule(265, labels.get(265).getProduction(), RuleType.WHITESPACE,
				"cf(LAYOUT) -> cf(LAYOUT?)", null, true);
		checkRule(266, labels.get(266).getProduction(), RuleType.WHITESPACE,
				" -> cf(LAYOUT?)", null, true);
		checkRule(
				267,
				labels.get(267).getProduction(),
				RuleType.FUNCTION,
				"cf(([x] [y] => [z])) cf(LAYOUT?) \"(\" cf(LAYOUT?) [x] cf(LAYOUT?) [y] cf(LAYOUT?) \")\" -> [z]",
				null, true);
		checkRule(
				268,
				labels.get(268).getProduction(),
				RuleType.FUNCTION,
				"cf(([v] => [w])) cf(LAYOUT?) \"(\" cf(LAYOUT?) [v] cf(LAYOUT?) \")\" -> [w]",
				null, true);
		checkRule(269, labels.get(269).getProduction(), RuleType.FUNCTION,
				"cf(( => [u])) cf(LAYOUT?) \"(\" cf(LAYOUT?) \")\" -> [u]",
				null, true);
		checkRule(
				270,
				labels.get(270).getProduction(),
				RuleType.TUPLE,
				"\"<\" cf(LAYOUT?) [r] cf(LAYOUT?) \",\" cf(LAYOUT?) [s] cf(LAYOUT?) \",\" cf(LAYOUT?) [t] cf(LAYOUT?) \">\" -> cf(<[r], [s], [t]>)",
				null, true);
		checkRule(
				271,
				labels.get(271).getProduction(),
				RuleType.TUPLE,
				"\"<\" cf(LAYOUT?) [p] cf(LAYOUT?) \",\" cf(LAYOUT?) [q] cf(LAYOUT?) \">\" -> cf(<[p], [q]>)",
				null, true);
		checkRule(272, labels.get(272).getProduction(), RuleType.ALTERNATIVE,
				"[o] -> cf([m] | [n] | [o])", null, true);
		checkRule(273, labels.get(273).getProduction(), RuleType.ALTERNATIVE,
				"[n] -> cf([m] | [n] | [o])", null, true);
		checkRule(274, labels.get(274).getProduction(), RuleType.ALTERNATIVE,
				"[m] -> cf([m] | [n] | [o])", null, true);
		checkRule(275, labels.get(275).getProduction(), RuleType.ALTERNATIVE,
				"[l] -> cf([k] | [l])", null, true);
		checkRule(276, labels.get(276).getProduction(), RuleType.ALTERNATIVE,
				"[k] -> cf([k] | [l])", null, true);
		checkRule(277, labels.get(277).getProduction(), RuleType.PLUS2STAR,
				"cf({[i] [j]}+) -> cf({[i] [j]}*)", null, true);
		checkRule(278, labels.get(278).getProduction(), RuleType.EPSILON2STAR,
				" -> cf({[i] [j]}*)", null, true);
		checkRule(
				279,
				labels.get(279).getProduction(),
				RuleType.LIST,
				"cf({[i] [j]}*) cf(LAYOUT?) [j] cf(LAYOUT?) cf({[i] [j]}*) -> cf({[i] [j]}*) {left}",
				null, true);
		checkRule(
				280,
				labels.get(280).getProduction(),
				RuleType.LIST,
				"cf({[i] [j]}*) cf(LAYOUT?) [j] cf(LAYOUT?) cf({[i] [j]}+) -> cf({[i] [j]}+)",
				null, true);
		checkRule(
				281,
				labels.get(281).getProduction(),
				RuleType.LIST,
				"cf({[i] [j]}+) cf(LAYOUT?) [j] cf(LAYOUT?) cf({[i] [j]}*) -> cf({[i] [j]}+)",
				null, true);
		checkRule(
				282,
				labels.get(282).getProduction(),
				RuleType.LIST,
				"cf({[i] [j]}+) cf(LAYOUT?) [j] cf(LAYOUT?) cf({[i] [j]}+) -> cf({[i] [j]}+) {left}",
				null, true);
		checkRule(283, labels.get(283).getProduction(), RuleType.LIST_PROD,
				"[i] -> cf({[i] [j]}+)", null, true);
		checkRule(284, labels.get(284).getProduction(), RuleType.PLUS2STAR,
				"cf({[g] [h]}+) -> cf({[g] [h]}*)", null, true);
		checkRule(285, labels.get(285).getProduction(), RuleType.EPSILON2STAR,
				" -> cf({[g] [h]}*)", null, true);
		checkRule(
				286,
				labels.get(286).getProduction(),
				RuleType.LIST,
				"cf({[g] [h]}*) cf(LAYOUT?) [h] cf(LAYOUT?) cf({[g] [h]}*) -> cf({[g] [h]}*) {left}",
				null, true);
		checkRule(
				287,
				labels.get(287).getProduction(),
				RuleType.LIST,
				"cf({[g] [h]}*) cf(LAYOUT?) [h] cf(LAYOUT?) cf({[g] [h]}+) -> cf({[g] [h]}+)",
				null, true);
		checkRule(
				288,
				labels.get(288).getProduction(),
				RuleType.LIST,
				"cf({[g] [h]}+) cf(LAYOUT?) [h] cf(LAYOUT?) cf({[g] [h]}*) -> cf({[g] [h]}+)",
				null, true);
		checkRule(
				289,
				labels.get(289).getProduction(),
				RuleType.LIST,
				"cf({[g] [h]}+) cf(LAYOUT?) [h] cf(LAYOUT?) cf({[g] [h]}+) -> cf({[g] [h]}+) {left}",
				null, true);
		checkRule(290, labels.get(290).getProduction(), RuleType.LIST_PROD,
				"[g] -> cf({[g] [h]}+)", null, true);
		checkRule(291, labels.get(291).getProduction(), RuleType.PLUS2STAR,
				"cf([f]+) -> cf([f]*)", null, true);
		checkRule(292, labels.get(292).getProduction(), RuleType.EPSILON2STAR,
				" -> cf([f]*)", null, true);
		checkRule(293, labels.get(293).getProduction(), RuleType.REPETITION,
				"cf([f]*) cf(LAYOUT?) cf([f]*) -> cf([f]*) {left}", null, true);
		checkRule(294, labels.get(294).getProduction(), RuleType.REPETITION,
				"cf([f]*) cf(LAYOUT?) cf([f]+) -> cf([f]+)", null, true);
		checkRule(295, labels.get(295).getProduction(), RuleType.REPETITION,
				"cf([f]+) cf(LAYOUT?) cf([f]*) -> cf([f]+)", null, true);
		checkRule(296, labels.get(296).getProduction(), RuleType.REPETITION,
				"cf([f]+) cf(LAYOUT?) cf([f]+) -> cf([f]+) {left}", null, true);
		checkRule(297, labels.get(297).getProduction(),
				RuleType.REPETITION_PROD, "[f] -> cf([f]+)", null, true);
		checkRule(298, labels.get(298).getProduction(), RuleType.PLUS2STAR,
				"cf([e]+) -> cf([e]*)", null, true);
		checkRule(299, labels.get(299).getProduction(), RuleType.EPSILON2STAR,
				" -> cf([e]*)", null, true);
		checkRule(300, labels.get(300).getProduction(), RuleType.REPETITION,
				"cf([e]*) cf(LAYOUT?) cf([e]*) -> cf([e]*) {left}", null, true);
		checkRule(301, labels.get(301).getProduction(), RuleType.REPETITION,
				"cf([e]*) cf(LAYOUT?) cf([e]+) -> cf([e]+)", null, true);
		checkRule(302, labels.get(302).getProduction(), RuleType.REPETITION,
				"cf([e]+) cf(LAYOUT?) cf([e]*) -> cf([e]+)", null, true);
		checkRule(303, labels.get(303).getProduction(), RuleType.REPETITION,
				"cf([e]+) cf(LAYOUT?) cf([e]+) -> cf([e]+) {left}", null, true);
		checkRule(304, labels.get(304).getProduction(),
				RuleType.REPETITION_PROD, "[e] -> cf([e]+)", null, true);
		checkRule(305, labels.get(305).getProduction(), RuleType.SEQUENCE,
				"[c] cf(LAYOUT?) [d] -> cf(([c] [d]))", null, true);
		checkRule(306, labels.get(306).getProduction(), RuleType.EPSILON,
				" -> cf(())", null, true);
		checkRule(307, labels.get(307).getProduction(), RuleType.OPTION,
				"[a] -> cf([a]?)", null, true);
		checkRule(308, labels.get(308).getProduction(), RuleType.EPSILON,
				" -> cf([a]?)", null, true);
		checkRule(309, labels.get(309).getProduction(), RuleType.FUNCTION,
				"lex(([x] [y] => [z])) \"(\" [x] [y] \")\" -> [z]", null, false);
		checkRule(310, labels.get(310).getProduction(), RuleType.FUNCTION,
				"lex(([v] => [w])) \"(\" [v] \")\" -> [w]", null, false);
		checkRule(311, labels.get(311).getProduction(), RuleType.FUNCTION,
				"lex(( => [u])) \"(\" \")\" -> [u]", null, false);
		checkRule(312, labels.get(312).getProduction(), RuleType.LEX2CF,
				"lex(<[r], [s], [t]>) -> cf(<[r], [s], [t]>)", null, true);
		checkRule(313, labels.get(313).getProduction(), RuleType.TUPLE,
				"\"<\" [r] \",\" [s] \",\" [t] \">\" -> lex(<[r], [s], [t]>)",
				null, false);
		checkRule(314, labels.get(314).getProduction(), RuleType.LEX2CF,
				"lex(<[p], [q]>) -> cf(<[p], [q]>)", null, true);
		checkRule(315, labels.get(315).getProduction(), RuleType.TUPLE,
				"\"<\" [p] \",\" [q] \">\" -> lex(<[p], [q]>)", null, false);
		checkRule(316, labels.get(316).getProduction(), RuleType.ALTERNATIVE,
				"[o] -> lex([m] | [n] | [o])", null, false);
		checkRule(317, labels.get(317).getProduction(), RuleType.ALTERNATIVE,
				"[n] -> lex([m] | [n] | [o])", null, false);
		checkRule(318, labels.get(318).getProduction(), RuleType.LEX2CF,
				"lex([m] | [n] | [o]) -> cf([m] | [n] | [o])", null, true);
		checkRule(319, labels.get(319).getProduction(), RuleType.ALTERNATIVE,
				"[m] -> lex([m] | [n] | [o])", null, false);
		checkRule(320, labels.get(320).getProduction(), RuleType.ALTERNATIVE,
				"[l] -> lex([k] | [l])", null, false);
		checkRule(321, labels.get(321).getProduction(), RuleType.LEX2CF,
				"lex([k] | [l]) -> cf([k] | [l])", null, true);
		checkRule(322, labels.get(322).getProduction(), RuleType.ALTERNATIVE,
				"[k] -> lex([k] | [l])", null, false);
		checkRule(323, labels.get(323).getProduction(), RuleType.PLUS2STAR,
				"lex({[i] [j]}+) -> lex({[i] [j]}*)", null, false);
		checkRule(324, labels.get(324).getProduction(), RuleType.EPSILON2STAR,
				" -> lex({[i] [j]}*)", null, false);
		checkRule(325, labels.get(325).getProduction(), RuleType.LEX2CF,
				"lex({[i] [j]}*) -> cf({[i] [j]}*)", null, true);
		checkRule(
				326,
				labels.get(326).getProduction(),
				RuleType.LIST,
				"lex({[i] [j]}*) [j] lex({[i] [j]}*) -> lex({[i] [j]}*) {left}",
				null, false);
		checkRule(327, labels.get(327).getProduction(), RuleType.LIST,
				"lex({[i] [j]}*) [j] lex({[i] [j]}+) -> lex({[i] [j]}+)", null,
				false);
		checkRule(328, labels.get(328).getProduction(), RuleType.LIST,
				"lex({[i] [j]}+) [j] lex({[i] [j]}*) -> lex({[i] [j]}+)", null,
				false);
		checkRule(
				329,
				labels.get(329).getProduction(),
				RuleType.LIST,
				"lex({[i] [j]}+) [j] lex({[i] [j]}+) -> lex({[i] [j]}+) {left}",
				null, false);
		checkRule(330, labels.get(330).getProduction(), RuleType.LEX2CF,
				"lex({[i] [j]}+) -> cf({[i] [j]}+)", null, true);
		checkRule(331, labels.get(331).getProduction(), RuleType.LIST_PROD,
				"[i] -> lex({[i] [j]}+)", null, false);
		checkRule(332, labels.get(332).getProduction(), RuleType.PLUS2STAR,
				"lex({[g] [h]}+) -> lex({[g] [h]}*)", null, false);
		checkRule(333, labels.get(333).getProduction(), RuleType.EPSILON2STAR,
				" -> lex({[g] [h]}*)", null, false);
		checkRule(334, labels.get(334).getProduction(), RuleType.LEX2CF,
				"lex({[g] [h]}*) -> cf({[g] [h]}*)", null, true);
		checkRule(
				335,
				labels.get(335).getProduction(),
				RuleType.LIST,
				"lex({[g] [h]}*) [h] lex({[g] [h]}*) -> lex({[g] [h]}*) {left}",
				null, false);
		checkRule(336, labels.get(336).getProduction(), RuleType.LIST,
				"lex({[g] [h]}*) [h] lex({[g] [h]}+) -> lex({[g] [h]}+)", null,
				false);
		checkRule(337, labels.get(337).getProduction(), RuleType.LIST,
				"lex({[g] [h]}+) [h] lex({[g] [h]}*) -> lex({[g] [h]}+)", null,
				false);
		checkRule(
				338,
				labels.get(338).getProduction(),
				RuleType.LIST,
				"lex({[g] [h]}+) [h] lex({[g] [h]}+) -> lex({[g] [h]}+) {left}",
				null, false);
		checkRule(339, labels.get(339).getProduction(), RuleType.LEX2CF,
				"lex({[g] [h]}+) -> cf({[g] [h]}+)", null, true);
		checkRule(340, labels.get(340).getProduction(), RuleType.LIST_PROD,
				"[g] -> lex({[g] [h]}+)", null, false);
		checkRule(341, labels.get(341).getProduction(), RuleType.PLUS2STAR,
				"lex([f]+) -> lex([f]*)", null, false);
		checkRule(342, labels.get(342).getProduction(), RuleType.EPSILON2STAR,
				" -> lex([f]*)", null, false);
		checkRule(343, labels.get(343).getProduction(), RuleType.LEX2CF,
				"lex([f]*) -> cf([f]*)", null, true);
		checkRule(344, labels.get(344).getProduction(), RuleType.REPETITION,
				"lex([f]*) lex([f]*) -> lex([f]*) {left}", null, false);
		checkRule(345, labels.get(345).getProduction(), RuleType.REPETITION,
				"lex([f]*) lex([f]+) -> lex([f]+)", null, false);
		checkRule(346, labels.get(346).getProduction(), RuleType.REPETITION,
				"lex([f]+) lex([f]*) -> lex([f]+)", null, false);
		checkRule(347, labels.get(347).getProduction(), RuleType.REPETITION,
				"lex([f]+) lex([f]+) -> lex([f]+) {left}", null, false);
		checkRule(348, labels.get(348).getProduction(), RuleType.LEX2CF,
				"lex([f]+) -> cf([f]+)", null, true);
		checkRule(349, labels.get(349).getProduction(),
				RuleType.REPETITION_PROD, "[f] -> lex([f]+)", null, false);
		checkRule(350, labels.get(350).getProduction(), RuleType.PLUS2STAR,
				"lex([e]+) -> lex([e]*)", null, false);
		checkRule(351, labels.get(351).getProduction(), RuleType.EPSILON2STAR,
				" -> lex([e]*)", null, false);
		checkRule(352, labels.get(352).getProduction(), RuleType.LEX2CF,
				"lex([e]*) -> cf([e]*)", null, true);
		checkRule(353, labels.get(353).getProduction(), RuleType.REPETITION,
				"lex([e]*) lex([e]*) -> lex([e]*) {left}", null, false);
		checkRule(354, labels.get(354).getProduction(), RuleType.REPETITION,
				"lex([e]*) lex([e]+) -> lex([e]+)", null, false);
		checkRule(355, labels.get(355).getProduction(), RuleType.REPETITION,
				"lex([e]+) lex([e]*) -> lex([e]+)", null, false);
		checkRule(356, labels.get(356).getProduction(), RuleType.REPETITION,
				"lex([e]+) lex([e]+) -> lex([e]+) {left}", null, false);
		checkRule(357, labels.get(357).getProduction(), RuleType.LEX2CF,
				"lex([e]+) -> cf([e]+)", null, true);
		checkRule(358, labels.get(358).getProduction(),
				RuleType.REPETITION_PROD, "[e] -> lex([e]+)", null, false);
		checkRule(359, labels.get(359).getProduction(), RuleType.LEX2CF,
				"lex(([c] [d])) -> cf(([c] [d]))", null, true);
		checkRule(360, labels.get(360).getProduction(), RuleType.SEQUENCE,
				"[c] [d] -> lex(([c] [d]))", null, false);
		checkRule(361, labels.get(361).getProduction(), RuleType.LEX2CF,
				"lex(()) -> cf(())", null, true);
		checkRule(362, labels.get(362).getProduction(), RuleType.EPSILON,
				" -> lex(())", null, false);
		checkRule(363, labels.get(363).getProduction(), RuleType.OPTION,
				"[a] -> lex([a]?)", null, false);
		checkRule(364, labels.get(364).getProduction(), RuleType.LEX2CF,
				"lex([a]?) -> cf([a]?)", null, true);
		checkRule(365, labels.get(365).getProduction(), RuleType.EPSILON,
				" -> lex([a]?)", null, false);
		checkRule(
				366,
				labels.get(366).getProduction(),
				RuleType.DEFINED,
				"\"func3\" cf(LAYOUT?) \"(\" cf(LAYOUT?) [D] cf(LAYOUT?) \",\" cf(LAYOUT?) [E] cf(LAYOUT?) \")\" -> cf(C) {definedAs(\"func3([D],[E])->C\")}",
				"func3([D],[E])->C", true);
		checkRule(
				367,
				labels.get(367).getProduction(),
				RuleType.DEFINED,
				"\"func2\" cf(LAYOUT?) \"(\" cf(LAYOUT?) [C] cf(LAYOUT?) \")\" -> cf(C) {definedAs(\"func2([C])->C\")}",
				"func2([C])->C", true);
		checkRule(
				368,
				labels.get(368).getProduction(),
				RuleType.DEFINED,
				"\"func1\" cf(LAYOUT?) \"(\" cf(LAYOUT?) \")\" -> cf(C) {definedAs(\"func1()->C\")}",
				"func1()->C", true);
		checkRule(69, labels.get(369).getProduction(), RuleType.DEFINED,
				"cf(([A] -> [B])) -> cf(C) {definedAs(\"([A] -> [B])->C\")}",
				"([A] -> [B])->C", true);
		checkRule(
				370,
				labels.get(370).getProduction(),
				RuleType.DEFINED,
				"cf(([x] [y] => [z])) -> cf(C) {definedAs(\"([x] [y] => [z])->C\")}",
				"([x] [y] => [z])->C", true);
		checkRule(371, labels.get(371).getProduction(), RuleType.DEFINED,
				"cf(([v] => [w])) -> cf(C) {definedAs(\"([v] => [w])->C\")}",
				"([v] => [w])->C", true);
		checkRule(372, labels.get(372).getProduction(), RuleType.DEFINED,
				"cf(( => [u])) -> cf(C) {definedAs(\"( => [u])->C\")}",
				"( => [u])->C", true);
		checkRule(
				373,
				labels.get(373).getProduction(),
				RuleType.DEFINED,
				"cf(<[r], [s], [t]>) -> cf(C) {definedAs(\"<[r], [s], [t]>->C\")}",
				"<[r], [s], [t]>->C", true);
		checkRule(374, labels.get(374).getProduction(), RuleType.DEFINED,
				"cf(<[p], [q]>) -> cf(C) {definedAs(\"<[p], [q]>->C\")}",
				"<[p], [q]>->C", true);
		checkRule(
				375,
				labels.get(375).getProduction(),
				RuleType.DEFINED,
				"cf([m] | [n] | [o]) -> cf(C) {definedAs(\"[m] | [n] | [o]->C\")}",
				"[m] | [n] | [o]->C", true);
		checkRule(376, labels.get(376).getProduction(), RuleType.DEFINED,
				"cf([k] | [l]) -> cf(C) {definedAs(\"[k] | [l]->C\")}",
				"[k] | [l]->C", true);
		checkRule(377, labels.get(377).getProduction(), RuleType.DEFINED,
				"cf({[i] [j]}+) -> cf(C) {definedAs(\"{[i] [j]}+->C\")}",
				"{[i] [j]}+->C", true);
		checkRule(378, labels.get(378).getProduction(), RuleType.DEFINED,
				"cf({[g] [h]}*) -> cf(C) {definedAs(\"{[g] [h]}*->C\")}",
				"{[g] [h]}*->C", true);
		checkRule(379, labels.get(379).getProduction(), RuleType.DEFINED,
				"cf([f]+) -> cf(C) {definedAs(\"[f]+->C\")}", "[f]+->C", true);
		checkRule(380, labels.get(380).getProduction(), RuleType.DEFINED,
				"cf([e]*) -> cf(C) {definedAs(\"[e]*->C\")}", "[e]*->C", true);
		checkRule(381, labels.get(381).getProduction(), RuleType.DEFINED,
				"cf(([c] [d])) -> cf(C) {definedAs(\"([c] [d])->C\")}",
				"([c] [d])->C", true);
		checkRule(382, labels.get(382).getProduction(), RuleType.DEFINED,
				"[b] -> cf(D) {definedAs(\"(([b]))->D\")}", "(([b]))->D", true);
		checkRule(383, labels.get(383).getProduction(), RuleType.DEFINED,
				"[b] -> cf(C) {definedAs(\"([b])->C\")}", "([b])->C", true);
		checkRule(384, labels.get(384).getProduction(), RuleType.DEFINED,
				"cf(()) -> cf(D) {definedAs(\"(())->D\")}", "(())->D", true);
		checkRule(385, labels.get(385).getProduction(), RuleType.DEFINED,
				"cf(()) -> cf(C) {definedAs(\"()->C\")}", "()->C", true);
		checkRule(386, labels.get(386).getProduction(), RuleType.DEFINED,
				"cf([a]?) -> cf(C) {definedAs(\"[a]?->C\")}", "[a]?->C", true);
		checkRule(387, labels.get(387).getProduction(), RuleType.DEFINED,
				" -> cf(C) {definedAs(\"->C\")}", "->C", true);
		checkRule(
				388,
				labels.get(388).getProduction(),
				RuleType.DEFINED,
				"\"func3\" \"(\" [D] \",\" [E] \")\" -> lex(A) {definedAs(\"func3([D],[E])->A\")}",
				"func3([D],[E])->A", false);
		checkRule(
				389,
				labels.get(389).getProduction(),
				RuleType.DEFINED,
				"\"func2\" \"(\" [C] \")\" -> lex(A) {definedAs(\"func2([C])->A\")}",
				"func2([C])->A", false);
		checkRule(390, labels.get(390).getProduction(), RuleType.DEFINED,
				"\"func1\" \"(\" \")\" -> lex(A) {definedAs(\"func1()->A\")}",
				"func1()->A", false);
		checkRule(391, labels.get(391).getProduction(), RuleType.DEFINED,
				"lex(([A] -> [B])) -> lex(A) {definedAs(\"([A] -> [B])->A\")}",
				"([A] -> [B])->A", false);
		checkRule(
				392,
				labels.get(392).getProduction(),
				RuleType.DEFINED,
				"lex(([x] [y] => [z])) -> lex(A) {definedAs(\"([x] [y] => [z])->A\")}",
				"([x] [y] => [z])->A", false);
		checkRule(393, labels.get(393).getProduction(), RuleType.DEFINED,
				"lex(([v] => [w])) -> lex(A) {definedAs(\"([v] => [w])->A\")}",
				"([v] => [w])->A", false);
		checkRule(394, labels.get(394).getProduction(), RuleType.DEFINED,
				"lex(( => [u])) -> lex(A) {definedAs(\"( => [u])->A\")}",
				"( => [u])->A", false);
		checkRule(
				395,
				labels.get(395).getProduction(),
				RuleType.DEFINED,
				"lex(<[r], [s], [t]>) -> lex(A) {definedAs(\"<[r], [s], [t]>->A\")}",
				"<[r], [s], [t]>->A", false);
		checkRule(396, labels.get(396).getProduction(), RuleType.DEFINED,
				"lex(<[p], [q]>) -> lex(A) {definedAs(\"<[p], [q]>->A\")}",
				"<[p], [q]>->A", false);
		checkRule(
				397,
				labels.get(397).getProduction(),
				RuleType.DEFINED,
				"lex([m] | [n] | [o]) -> lex(A) {definedAs(\"[m] | [n] | [o]->A\")}",
				"[m] | [n] | [o]->A", false);
		checkRule(398, labels.get(398).getProduction(), RuleType.DEFINED,
				"lex([k] | [l]) -> lex(A) {definedAs(\"[k] | [l]->A\")}",
				"[k] | [l]->A", false);
		checkRule(399, labels.get(399).getProduction(), RuleType.DEFINED,
				"lex({[i] [j]}+) -> lex(A) {definedAs(\"{[i] [j]}+->A\")}",
				"{[i] [j]}+->A", false);
		checkRule(400, labels.get(400).getProduction(), RuleType.DEFINED,
				"lex({[g] [h]}*) -> lex(A) {definedAs(\"{[g] [h]}*->A\")}",
				"{[g] [h]}*->A", false);
		checkRule(401, labels.get(401).getProduction(), RuleType.DEFINED,
				"lex([f]+) -> lex(A) {definedAs(\"[f]+->A\")}", "[f]+->A",
				false);
		checkRule(402, labels.get(402).getProduction(), RuleType.DEFINED,
				"lex([e]*) -> lex(A) {definedAs(\"[e]*->A\")}", "[e]*->A",
				false);
		checkRule(403, labels.get(403).getProduction(), RuleType.DEFINED,
				"lex(([c] [d])) -> lex(A) {definedAs(\"([c] [d])->A\")}",
				"([c] [d])->A", false);
		checkRule(404, labels.get(404).getProduction(), RuleType.DEFINED,
				"[b] -> lex(B) {definedAs(\"(([b]))->B\")}", "(([b]))->B",
				false);
		checkRule(405, labels.get(405).getProduction(), RuleType.DEFINED,
				"[b] -> lex(A) {definedAs(\"([b])->A\")}", "([b])->A", false);
		checkRule(406, labels.get(406).getProduction(), RuleType.LEX2CF,
				"lex(B) -> cf(B)", null, true);
		checkRule(407, labels.get(407).getProduction(), RuleType.DEFINED,
				"lex(()) -> lex(B) {definedAs(\"(())->B\")}", "(())->B", false);
		checkRule(408, labels.get(408).getProduction(), RuleType.DEFINED,
				"lex(()) -> lex(A) {definedAs(\"()->A\")}", "()->A", false);
		checkRule(409, labels.get(409).getProduction(), RuleType.DEFINED,
				"lex([a]?) -> lex(A) {definedAs(\"[a]?->A\")}", "[a]?->A",
				false);
		checkRule(410, labels.get(410).getProduction(), RuleType.LEX2CF,
				"lex(A) -> cf(A)", null, true);
		checkRule(411, labels.get(411).getProduction(), RuleType.DEFINED,
				" -> lex(A) {definedAs(\"->A\")}", "->A", false);
		checkRule(412, labels.get(412).getProduction(), RuleType.START,
				"cf(LAYOUT?) cf(C) cf(LAYOUT?) -> <START>", null, false);
		checkRule(413, labels.get(413).getProduction(), RuleType.START,
				"lex(A) -> <START>", null, false);
		checkRule(414, labels.get(414).getProduction(), RuleType.WHITESPACE,
				"cf(LAYOUT) cf(LAYOUT) -> cf(LAYOUT) {left}", null, true);
		checkRule(415, labels.get(415).getProduction(), RuleType.FILE_START,
				"<START> [\\256] -> <Start>", null, false);
	}

}
