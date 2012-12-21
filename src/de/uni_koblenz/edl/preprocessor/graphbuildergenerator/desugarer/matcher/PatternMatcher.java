package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.TermPrinter;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Pattern;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfSequence;
import de.uni_koblenz.edl.preprocessor.schema.term.PatternTerm;
import de.uni_koblenz.jgralab.EdgeDirection;

public class PatternMatcher {

	private final List<FiniteStateAutomaton> automatons = new ArrayList<FiniteStateAutomaton>();

	public void addPattern(Pattern pattern) {
		FiniteStateAutomaton finiteAutomaton = new FiniteStateAutomaton(pattern);
		automatons.add(finiteAutomaton);
	}

	public List<PatternValue> matches(Production rule) {
		List<String> terms = new ArrayList<String>();
		TermPrinter termPrinter = new TermPrinter();
		StringBuffer sb = new StringBuffer();
		try {
			termPrinter.convertTermWithoutPrefix(sb, rule.get_headTerm());
			terms.add(sb.toString());
			for (IsPartOfSequence ipos : rule
					.getFirstIsBodyTermOfProductionIncidence(EdgeDirection.IN)
					.getAlpha().getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
				sb = new StringBuffer();
				termPrinter.convertTermWithoutPrefix(sb,
						(PatternTerm) ipos.getThat());
				terms.add(sb.toString());
			}
		} catch (IOException e) {
			// this should not happen
			e.printStackTrace();
		}

		List<PatternValue> patternValues = new ArrayList<PatternValue>();
		for (FiniteStateAutomaton automaton : automatons) {
			if (automaton.matches(terms)) {
				patternValues.add(automaton.getPatternValue());
			}
		}
		return patternValues;
	}
}
