package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
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
import de.uni_koblenz.edl.preprocessor.schema.term.IsSyntaxElementOf;
import de.uni_koblenz.edl.preprocessor.schema.term.KernelTerm;
import de.uni_koblenz.edl.preprocessor.schema.term.KleeneOperator;
import de.uni_koblenz.edl.preprocessor.schema.term.LAYOUT;
import de.uni_koblenz.edl.preprocessor.schema.term.Lifting;
import de.uni_koblenz.edl.preprocessor.schema.term.List;
import de.uni_koblenz.edl.preprocessor.schema.term.Literal;
import de.uni_koblenz.edl.preprocessor.schema.term.Option;
import de.uni_koblenz.edl.preprocessor.schema.term.PatternTerm;
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
import de.uni_koblenz.edl.preprocessor.schema.term.Wildcard;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TermPrinter {

	private final Map<Term, StringBuffer> term2String = new HashMap<Term, StringBuffer>();

	public void convertTerm(Appendable appendable, PatternTerm term,
			GrammarType grammarType, boolean isInVariable) throws IOException {
		if (term.isInstanceOf(Wildcard.VC)) {
			appendable.append("_");
		} else {
			if (needsPrefix((Term) term)) {
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
				convertTerm(appendable, (Term) term);
				if (grammarType != GrammarType.KERNEL || isInVariable) {
					appendable.append(")");
				}
			} else {
				convertTerm(appendable, (Term) term);
			}
		}
	}

	private boolean needsPrefix(Term term) {
		VertexClass vc = term.getAttributedElementClass();
		return vc == LAYOUT.VC || vc == Sort.VC || vc == Sequence.VC
				|| vc.isSubClassOf(UnaryTerm.VC) || vc == Alternative.VC
				|| vc == List.VC || vc.isSubClassOf(ConstructedTerm.VC);
	}

	public void convertTermWithoutPrefix(Appendable appendable, PatternTerm term)
			throws IOException {
		if (term.isInstanceOf(Wildcard.VC)) {
			appendable.append("_");
		} else if (term.isInstanceOf(KernelTerm.VC)) {
			convertTerm(appendable, ((KernelTerm) term).get_term());
		} else {
			convertTerm(appendable, (Term) term);
		}
	}

	private void convertTerm(Appendable appendable, Term term)
			throws IOException {
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
				convertKernelTerm(sb, (KernelTerm) term);
			} else if (term.isInstanceOf(CharacterClass.VC)) {
				convertCharacterClass(sb, (CharacterClass) term);
			} else if (term.isInstanceOf(Lifting.VC)) {
				convertLifting(sb, (Lifting) term);
			} else if (term.isInstanceOf(LAYOUT.VC)) {
				sb.append("LAYOUT");
			} else if (term.isInstanceOf(Literal.VC)) {
				convertLiteral(sb, (Literal) term);
			} else if (term.isInstanceOf(Sort.VC)) {
				convertSort(sb, (Sort) term);
			} else if (term.isInstanceOf(Sequence.VC)) {
				convertSequence(sb, (Sequence) term);
			} else if (term.isInstanceOf(UnaryTerm.VC)) {
				convertUnaryTerm(sb, (UnaryTerm) term);
			} else if (term.isInstanceOf(Alternative.VC)) {
				convertAlternative(sb, (Alternative) term);
			} else if (term.isInstanceOf(List.VC)) {
				convertList(sb, (List) term);
			} else if (term.isInstanceOf(ConstructedTerm.VC)) {
				convertConsructedTerm(sb, (ConstructedTerm) term);
			}
		}
		appendable.append(sb);
	}

	private void convertKernelTerm(Appendable appendable, KernelTerm kernelTerm)
			throws IOException {
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
		convertTerm(appendable, kernelTerm.get_term());
		appendable.append(")");
	}

	private void convertLiteral(Appendable appendable, Literal literal)
			throws IOException {
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

	private void convertSort(Appendable appendable, Sort sort)
			throws IOException {
		appendable.append(sort.get_identifier().get_name());

		// convert parameter
		String delim = "[[";
		for (IsParameterOfSort ipos : sort
				.getIsParameterOfSortIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat());
			delim = ", ";
		}
		if (!delim.equals("[[")) {
			appendable.append("]]");
		}
	}

	private void convertCharacterClass(Appendable appendable,
			CharacterClass characterClass) throws IOException {
		if (characterClass.isInstanceOf(CharacterRanges.VC)) {
			convertCharacterRanges(appendable, (CharacterRanges) characterClass);
		} else if (characterClass.isInstanceOf(Complement.VC)) {
			convertComplement(appendable, (Complement) characterClass);
		} else if (characterClass
				.isInstanceOf(BinaryCharacterClassOperation.VC)) {
			convertBinaryCharacterClassOperation(appendable,
					(BinaryCharacterClassOperation) characterClass);
		}
	}

	private void convertCharacterRanges(Appendable appendable,
			CharacterRanges characterRanges) throws IOException {
		appendable.append("[");
		for (IsCharacterRangeOf icro : characterRanges
				.getIsCharacterRangeOfIncidences(EdgeDirection.IN)) {
			CharacterRange characterRange = (CharacterRange) icro.getThat();
			appendable.append(characterRange.get_minCharacter());
			if (!characterRange.get_minCharacter().equals(
					characterRange.get_maxCharacter())) {
				appendable.append("-");
				appendable.append(characterRange.get_maxCharacter());
			}
		}
		appendable.append("]");
	}

	private void convertComplement(Appendable appendable, Complement complement)
			throws IOException {
		appendable.append("~");
		convertCharacterClass(appendable, complement.get_characterClass());
	}

	private void convertBinaryCharacterClassOperation(Appendable appendable,
			BinaryCharacterClassOperation binaryCharacterClassOperation)
			throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOfBinaryCharacterClassOperation ipobcco : binaryCharacterClassOperation
				.getIsPartOfBinaryCharacterClassOperationIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertCharacterClass(appendable,
					(CharacterClass) ipobcco.getThat());
			if (delim.isEmpty()) {
				if (binaryCharacterClassOperation.isInstanceOf(Difference.VC)) {
					delim = "/";
				} else if (binaryCharacterClassOperation
						.isInstanceOf(Intersection.VC)) {
					delim = "/\\";
				} else if (binaryCharacterClassOperation.isInstanceOf(Union.VC)) {
					delim = "\\/";
				}
			}
		}
		appendable.append(")");
	}

	private void convertSequence(Appendable appendable, Sequence sequence)
			throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOfSequence ipos : sequence
				.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat());
			delim = " ";
		}
		appendable.append(")");
	}

	private void convertUnaryTerm(Appendable appendable, UnaryTerm unaryTerm)
			throws IOException {
		Term term = unaryTerm.get_term();
		if (term.isInstanceOf(Alternative.VC)) {
			appendable.append("(");
		}
		convertTerm(appendable, term);
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

	private void convertAlternative(Appendable appendable,
			Alternative alternative) throws IOException {
		String delim = "";
		for (IsPartOfBinaryTerm ipos : alternative
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat());
			delim = " | ";
		}
	}

	private void convertList(Appendable appendable, List list)
			throws IOException {
		appendable.append("{");
		String delim = "";
		for (IsPartOfBinaryTerm ipos : list
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat());
			delim = " ";
		}
		appendable.append("}");
		appendable.append(getKleeneOperator(list.get_type()));
	}

	private void convertLifting(Appendable appendable, Lifting lifting)
			throws IOException {
		appendable.append("´");
		convertTerm(appendable, lifting.get_term());
		appendable.append("´");
	}

	private void convertConsructedTerm(Appendable appendable,
			ConstructedTerm constructedTerm) throws IOException {
		if (constructedTerm.isInstanceOf(PrefixFunction.VC)
				|| constructedTerm.isInstanceOf(Tuple.VC)) {
			convertPrefixFunctionOrTuple(appendable, constructedTerm);
		} else if (constructedTerm.isInstanceOf(Function.VC)
				|| constructedTerm.isInstanceOf(Strategy.VC)) {
			convertFunctionOrStrategy(appendable, constructedTerm);
		}
	}

	private void convertPrefixFunctionOrTuple(Appendable appendable,
			ConstructedTerm constructedTerm) throws IOException {
		String delim = "";
		for (Edge edge : constructedTerm.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsFunctionNameOf.EC)
					|| edge.isInstanceOf(IsSyntaxElementOf.EC)) {
				appendable.append(((Literal) edge.getThat()).get_value());
			} else if (edge.isInstanceOf(IsElementOf.EC)) {
				appendable.append(delim);
				convertTerm(appendable, (Term) edge.getThat());
				delim = " ";
			}
		}
	}

	private void convertFunctionOrStrategy(Appendable appendable,
			ConstructedTerm constructedTerm) throws IOException {
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
				convertTerm(appendable, (Term) edge.getThat());
				delim = " ";
			}
		}
	}

}
