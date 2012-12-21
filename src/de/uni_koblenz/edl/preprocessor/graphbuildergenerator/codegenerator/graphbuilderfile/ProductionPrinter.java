package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.term.Alternative;
import de.uni_koblenz.edl.preprocessor.schema.term.BinaryCharacterClassOperation;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterClass;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterRange;
import de.uni_koblenz.edl.preprocessor.schema.term.CharacterRanges;
import de.uni_koblenz.edl.preprocessor.schema.term.Complement;
import de.uni_koblenz.edl.preprocessor.schema.term.ConstructedTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.Difference;
import de.uni_koblenz.edl.preprocessor.schema.term.FileStart;
import de.uni_koblenz.edl.preprocessor.schema.term.Function;
import de.uni_koblenz.edl.preprocessor.schema.term.Intersection;
import de.uni_koblenz.edl.preprocessor.schema.term.IsCharacterRangeOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsFunctionNameOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsParameterOfSort;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryCharacterClassOperation;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfBinaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.IsPartOfSequence;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.term.IsSyntaxElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.KernelTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.KleeneOperator;
import de.uni_koblenz.edl.preprocessor.schema.term.LAYOUT;
import de.uni_koblenz.edl.preprocessor.schema.term.Lifting;
import de.uni_koblenz.edl.preprocessor.schema.term.List;
import de.uni_koblenz.edl.preprocessor.schema.term.Literal;
import de.uni_koblenz.edl.preprocessor.schema.term.Option;
import de.uni_koblenz.edl.preprocessor.schema.term.PrefixFunction;
import de.uni_koblenz.edl.preprocessor.schema.term.Repetition;
import de.uni_koblenz.edl.preprocessor.schema.term.Sequence;
import de.uni_koblenz.edl.preprocessor.schema.term.Sort;
import de.uni_koblenz.edl.preprocessor.schema.term.Start;
import de.uni_koblenz.edl.preprocessor.schema.term.Strategy;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.edl.preprocessor.schema.term.Tuple;
import de.uni_koblenz.edl.preprocessor.schema.term.UnaryTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.Union;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class ProductionPrinter {

	private final Map<Term, StringBuffer> term2String = new HashMap<Term, StringBuffer>();

	public void printProduction(Appendable appendable, Production production,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		String delim = "";
		for (IsBodyTermOfProduction ibtop : production
				.getIsBodyTermOfProductionIncidences(EdgeDirection.IN)) {
			Sequence sequence = (Sequence) ibtop.getThat();
			for (IsPartOfSequence ipos : sequence
					.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
				Term term = (Term) ipos.getThat();
				appendable.append(delim);
				printTerm(appendable, term, grammarType, isInVariable);
				if (grammarType == GrammarType.CONTEXT_FREE) {
					delim = " cf(LAYOUT?) ";
				} else {
					delim = " ";
				}
			}
		}
		appendable.append(" -> ");
		printTerm(appendable, production.get_headTerm(), grammarType,
				isInVariable);
	}

	public void printStartSymbol(Appendable appendable, Term term,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		if (grammarType == GrammarType.CONTEXT_FREE) {
			appendable.append("cf(LAYOUT?) ");
		}
		printTerm(appendable, term, grammarType, isInVariable);
		if (grammarType == GrammarType.CONTEXT_FREE) {
			appendable.append(" cf(LAYOUT?)");
		}
		appendable.append(" -> <START>");
	}

	public void printRuleForFunction(Appendable appendable, Function function,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		printTerm(appendable, function, grammarType, isInVariable);
		String delim = " ";
		if (grammarType == GrammarType.CONTEXT_FREE) {
			delim = " cf(LAYOUT?) ";
		}
		appendable.append(delim).append("\"(\" ");
		boolean nextTermIsHead = false;
		for (Edge edge : function.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsSemanticActionOf.EC)) {
				continue;
			}
			appendable.append(delim);
			if (edge.isInstanceOf(IsSyntaxElementOf.EC)) {
				Literal literal = (Literal) edge.getThat();
				nextTermIsHead = literal.get_value().equals("=>");
			} else if (edge.isInstanceOf(IsElementOf.EC)) {
				if (nextTermIsHead) {
					appendable.append("\")\" -> ");
				}
				printTerm(appendable, (Term) edge.getThat(), grammarType,
						isInVariable);
			}
		}
	}

	public void printTermAsRuleHead(Appendable appendable, Term term,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		if (term.isInstanceOf(Function.VC)) {
			Edge edge = term.getLastIncidence();
			while (edge != null && edge.getOmega() != term
					&& !edge.isInstanceOf(IsElementOf.EC)) {
				edge = edge.getPrevIncidence();
			}
			assert edge != null;
			printTerm(appendable, (Term) edge.getThat(), grammarType,
					isInVariable);
		} else if (term
				.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
			// find the root of alternatives because BNF rules
			// have a head of type cf(a|b|c)
			Term parentTerm = term;
			IsPartOfBinaryTerm ipobt = parentTerm
					.getFirstIsPartOfBinaryTermIncidence(EdgeDirection.OUT);
			while (ipobt != null
					&& ipobt.getThat()
							.isInstanceOf(
									de.uni_koblenz.edl.preprocessor.schema.term.Alternative.VC)) {
				parentTerm = (Term) ipobt.getThat();
				ipobt = parentTerm
						.getFirstIsPartOfBinaryTermIncidence(EdgeDirection.OUT);
			}
			printTerm(appendable, parentTerm, grammarType, isInVariable);
		} else {
			printTerm(appendable, term, grammarType, isInVariable);
		}
	}

	private void printTerm(Appendable appendable, Term term,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		if (needsPrefix(term)) {
			switch (grammarType) {
			case CONTEXT_FREE:
				appendable.append("cf(");
				break;
			case LEXICAL:
				appendable.append("lex(");
				break;
			case KERNEL:
				if (isInVariable) {
					appendable.append("var(");
				}
				break;
			}
			printTerm(appendable, term, grammarType);
			if (grammarType != GrammarType.KERNEL || isInVariable) {
				appendable.append(")");
			}
		} else {
			printTerm(appendable, term, grammarType);
		}
	}

	private boolean needsPrefix(Term term) {
		VertexClass vc = term.getAttributedElementClass();
		return vc == LAYOUT.VC
				|| vc == Sort.VC
				|| vc == Sequence.VC
				|| vc.isSubClassOf(UnaryTerm.VC)
				|| vc == Alternative.VC
				|| vc == List.VC
				|| (vc.isSubClassOf(ConstructedTerm.VC) && vc != PrefixFunction.VC);
	}

	private void printTerm(Appendable appendable, Term term,
			GrammarType grammarType) throws IOException {
		StringBuffer sb = null;
		synchronized (term2String) {
			sb = term2String.get(term);
		}
		if (sb == null) {
			sb = new StringBuffer();
			synchronized (term2String) {
				term2String.put(term, sb);
			}
			if (term.isInstanceOf(Start.VC)) {
				sb.append("<START>");
			} else if (term.isInstanceOf(FileStart.VC)) {
				sb.append("<Start>");
			} else if (term.isInstanceOf(KernelTerm.VC)) {
				printKernelTerm(sb, (KernelTerm) term, grammarType);
			} else if (term.isInstanceOf(CharacterClass.VC)) {
				printCharacterClass(sb, (CharacterClass) term);
			} else if (term.isInstanceOf(Lifting.VC)) {
				printLifting(sb, (Lifting) term, grammarType);
			} else if (term.isInstanceOf(LAYOUT.VC)) {
				sb.append("LAYOUT");
			} else if (term.isInstanceOf(Literal.VC)) {
				printLiteral(sb, (Literal) term, grammarType);
			} else if (term.isInstanceOf(Sort.VC)) {
				printSort(sb, (Sort) term, grammarType);
			} else if (term.isInstanceOf(Sequence.VC)) {
				printSequence(sb, (Sequence) term, grammarType);
			} else if (term.isInstanceOf(UnaryTerm.VC)) {
				printUnaryTerm(sb, (UnaryTerm) term, grammarType);
			} else if (term.isInstanceOf(Alternative.VC)) {
				printAlternative(sb, (Alternative) term, grammarType);
			} else if (term.isInstanceOf(List.VC)) {
				printList(sb, (List) term, grammarType);
			} else if (term.isInstanceOf(ConstructedTerm.VC)) {
				printConsructedTerm(sb, (ConstructedTerm) term, grammarType);
			}
		}
		appendable.append(sb);
	}

	private void printKernelTerm(Appendable appendable, KernelTerm kernelTerm,
			GrammarType grammarType) throws IOException {
		switch (kernelTerm.get_type()) {
		case CF:
			appendable.append("cf(");
			break;
		case LEX:
			appendable.append("lex(");
			break;
		case VAR:
			appendable.append("var(");
			break;
		}
		printTerm(appendable, kernelTerm.get_term(), grammarType);
		appendable.append(")");
	}

	private void printLiteral(Appendable appendable, Literal literal,
			GrammarType grammarType) throws IOException {
		if (literal.is_isSingleQuoted()) {
			appendable.append("'");
		} else {
			appendable.append("\"");
		}
		appendable.append(literal.get_value());
		if (literal.is_isSingleQuoted()) {
			appendable.append("'");
		} else {
			appendable.append("\"");
		}
	}

	private void printSort(Appendable appendable, Sort sort,
			GrammarType grammarType) throws IOException {
		appendable.append(sort.get_identifier().get_name());

		// print parameter
		String delim = "[[";
		for (IsParameterOfSort ipos : sort
				.getIsParameterOfSortIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			printTerm(appendable, (Term) ipos.getThat(), grammarType);
			delim = ", ";
		}
		if (!delim.equals("[[")) {
			appendable.append("]]");
		}
	}

	private void printCharacterClass(Appendable appendable,
			CharacterClass characterClass) throws IOException {
		BitSet normalizedCharacterClass = handleCharacterClass(characterClass);
		assert normalizedCharacterClass != null;
		appendable.append("[");
		int startOfRange = -1;
		for (int i = 0; i <= 255; i++) {
			if (normalizedCharacterClass.get(i)) {
				if (startOfRange < 0) {
					startOfRange = i;
					printCharacter(appendable, (char) i);
				}
			} else {
				if (startOfRange >= 0) {
					if (i - 1 > startOfRange) {
						// only ranges with more than 1 element have a -End
						appendable.append("-");
						printCharacter(appendable, (char) (i - 1));
					}
					startOfRange = -1;
				}
			}
		}
		if (startOfRange >= 0 && startOfRange != 255) {
			appendable.append("-");
			printCharacter(appendable, (char) 255);
		}
		appendable.append("]");
	}

	private void printCharacter(Appendable appendable, char aChar)
			throws IOException {
		switch (aChar) {
		case '"':
			appendable.append("\\\"");
			break;
		case '\\':
			appendable.append("\\\\");
			break;
		case '\t':
			appendable.append("\\t");
			break;
		case '\r':
			appendable.append("\\r");
			break;
		case '\n':
			appendable.append("\\n");
			break;
		default:
			String content = String.valueOf(aChar);
			if (!content.matches("^[\\p{Print}]$")) {
				// aChar is not a printable character
				appendable.append("\\").append(Integer.toString(aChar));
			} else if (content.matches("^[_\\W]$")) {
				// in a character-class whitespace and _ must be quoted
				appendable.append("\\").append(content);
			} else {
				appendable.append(content);
			}
		}
	}

	private BitSet handleCharacterClass(CharacterClass characterClass) {
		BitSet normalizedCharacterClass = null;
		if (characterClass.isInstanceOf(CharacterRanges.VC)) {
			normalizedCharacterClass = handleCharacterRanges((CharacterRanges) characterClass);
		} else if (characterClass.isInstanceOf(Complement.VC)) {
			normalizedCharacterClass = handleComplement((Complement) characterClass);
		} else if (characterClass
				.isInstanceOf(BinaryCharacterClassOperation.VC)) {
			normalizedCharacterClass = handleBinaryCharacterClassOperation((BinaryCharacterClassOperation) characterClass);
		}
		return normalizedCharacterClass;
	}

	private BitSet handleCharacterRanges(CharacterRanges characterRanges) {
		BitSet charRange = new BitSet(256);
		for (IsCharacterRangeOf icro : characterRanges
				.getIsCharacterRangeOfIncidences(EdgeDirection.IN)) {
			CharacterRange characterRange = (CharacterRange) icro.getThat();
			char maxChar = extractCharacter(characterRange.get_maxCharacter());
			char minChar = extractCharacter(characterRange.get_minCharacter());
			charRange.set(minChar, maxChar + 1);
		}
		return charRange;
	}

	private char extractCharacter(String value) {
		if (value.length() == 1) {
			return value.charAt(0);
		} else if (value.matches("\\\\\\d+")) {
			return (char) Integer.parseInt(value.substring(1));
		} else if (value.equals("\\n")) {
			return '\n';
		} else if (value.equals("\\r")) {
			return '\r';
		} else if (value.equals("\\t")) {
			return '\t';
		} else {
			assert value.length() == 2;
			return value.charAt(1);
		}
	}

	private BitSet handleComplement(Complement complement) {
		BitSet charRange = handleCharacterClass(complement.get_characterClass());
		charRange.flip(0, 256);
		return charRange;
	}

	private BitSet handleBinaryCharacterClassOperation(
			BinaryCharacterClassOperation binaryCharacterClassOperation) {
		BitSet lhs = null;
		BitSet rhs = null;
		for (IsPartOfBinaryCharacterClassOperation ipobcco : binaryCharacterClassOperation
				.getIsPartOfBinaryCharacterClassOperationIncidences(EdgeDirection.IN)) {
			if (lhs == null) {
				lhs = handleCharacterClass((CharacterClass) ipobcco.getThat());
			} else {
				assert rhs == null;
				rhs = handleCharacterClass((CharacterClass) ipobcco.getThat());
			}
		}
		assert lhs != null && rhs != null;
		if (binaryCharacterClassOperation.isInstanceOf(Difference.VC)) {
			for (int i = 0; i <= 255; i++) {
				if (lhs.get(i)) {
					lhs.set(i, !rhs.get(i));
				}
			}
		} else if (binaryCharacterClassOperation.isInstanceOf(Intersection.VC)) {
			lhs.and(rhs);
		} else if (binaryCharacterClassOperation.isInstanceOf(Union.VC)) {
			lhs.or(rhs);
		}
		return lhs;
	}

	private void printSequence(Appendable appendable, Sequence sequence,
			GrammarType grammarType) throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOfSequence ipos : sequence
				.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			printTerm(appendable, (Term) ipos.getThat(), grammarType);
			delim = " ";
		}
		appendable.append(")");
	}

	private void printUnaryTerm(Appendable appendable, UnaryTerm unaryTerm,
			GrammarType grammarType) throws IOException {
		Term term = unaryTerm.get_term();
		if (term.isInstanceOf(Alternative.VC)) {
			appendable.append("(");
		}
		printTerm(appendable, term, grammarType);
		if (term.isInstanceOf(Alternative.VC)) {
			appendable.append(")");
		}
		if (unaryTerm.isInstanceOf(Option.VC)) {
			appendable.append("?");
		} else {
			assert unaryTerm.isInstanceOf(Repetition.VC);
			appendable.append(getKleeneOperator(((Repetition) unaryTerm)
					.get_type()));
		}
	}

	private String getKleeneOperator(KleeneOperator kleeneOperator) {
		switch (kleeneOperator) {
		case PLUS:
			return "+";
		case STAR:
			return "*";
		default:
			return "";
		}
	}

	private void printAlternative(Appendable appendable,
			Alternative alternative, GrammarType grammarType)
			throws IOException {
		String delim = "";
		for (IsPartOfBinaryTerm ipos : alternative
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			printTerm(appendable, (Term) ipos.getThat(), grammarType);
			delim = " | ";
		}
	}

	private void printList(Appendable appendable, List list,
			GrammarType grammarType) throws IOException {
		appendable.append("{");
		String delim = "";
		for (IsPartOfBinaryTerm ipos : list
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			printTerm(appendable, (Term) ipos.getThat(), grammarType);
			delim = " ";
		}
		appendable.append("}");
		appendable.append(getKleeneOperator(list.get_type()));
	}

	private void printLifting(Appendable appendable, Lifting lifting,
			GrammarType grammarType) throws IOException {
		appendable.append("´");
		printTerm(appendable, lifting.get_term(), grammarType);
		appendable.append("´");
	}

	private void printConsructedTerm(Appendable appendable,
			ConstructedTerm constructedTerm, GrammarType grammarType)
			throws IOException {
		if (constructedTerm.isInstanceOf(PrefixFunction.VC)) {
			printPrefixFunction(appendable, constructedTerm, grammarType);
		} else if (constructedTerm.isInstanceOf(Tuple.VC)) {
			printTuple(appendable, constructedTerm, grammarType);
		} else if (constructedTerm.isInstanceOf(Function.VC)
				|| constructedTerm.isInstanceOf(Strategy.VC)) {
			printFunctionOrStrategy(appendable, constructedTerm, grammarType);
		}
	}

	private void printTuple(Appendable appendable,
			ConstructedTerm constructedTerm, GrammarType grammarType)
			throws IOException {
		String delim = "";
		appendable.append("<");
		for (Edge edge : constructedTerm.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsElementOf.EC)) {
				appendable.append(delim);
				printTerm(appendable, (Term) edge.getThat(), grammarType);
				delim = ", ";
			}
		}
		appendable.append(">");
	}

	private void printPrefixFunction(Appendable appendable,
			ConstructedTerm constructedTerm, GrammarType grammarType)
			throws IOException {
		String delim = "";
		for (Edge edge : constructedTerm.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsFunctionNameOf.EC)
					|| edge.isInstanceOf(IsSyntaxElementOf.EC)
					|| edge.isInstanceOf(IsElementOf.EC)) {
				appendable.append(delim);
				printTerm(appendable, (Term) edge.getThat(), grammarType);
				if (grammarType == GrammarType.CONTEXT_FREE) {
					delim = " cf(LAYOUT?) ";
				} else {
					delim = " ";
				}
			}
		}
	}

	private void printFunctionOrStrategy(Appendable appendable,
			ConstructedTerm constructedTerm, GrammarType grammarType)
			throws IOException {
		String delim = "";
		for (Edge edge : constructedTerm.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsSyntaxElementOf.EC)) {
				String syntaxElement = ((Literal) edge.getThat()).get_value();
				if (syntaxElement.endsWith(">")) {
					appendable.append(" ");
				}
				appendable.append(syntaxElement);
				if (syntaxElement.endsWith(">")) {
					delim = " ";
				}
			} else if (edge.isInstanceOf(IsElementOf.EC)) {
				appendable.append(delim);
				printTerm(appendable, (Term) edge.getThat(), grammarType);
				delim = " ";
			}
		}
	}

}
