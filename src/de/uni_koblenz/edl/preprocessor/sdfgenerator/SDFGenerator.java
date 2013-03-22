package de.uni_koblenz.edl.preprocessor.sdfgenerator;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import de.uni_koblenz.edl.SemanticActionException;
import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.preprocessor.EDLPreprocessor;
import de.uni_koblenz.edl.preprocessor.GrammarException;
import de.uni_koblenz.edl.preprocessor.schema.EDLGraph;
import de.uni_koblenz.edl.preprocessor.schema.common.Definition;
import de.uni_koblenz.edl.preprocessor.schema.common.IsModuleOf;
import de.uni_koblenz.edl.preprocessor.schema.common.IsParameterOfModule;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Alias;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Aliases;
import de.uni_koblenz.edl.preprocessor.schema.grammar.AssocPriority;
import de.uni_koblenz.edl.preprocessor.schema.grammar.EmptyGrammar;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Grammar;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Group;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAliasOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAttributeOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfProduction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsListPartOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPartOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPriorityGroupOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsPriorityOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsProductionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsProductionOfGroup;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRenamingOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRestrictedTermOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsRestrictionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsSortOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsStartSymbolOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsVariableDefinitionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Lookahead;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Priorities;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Priority;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Renaming;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Restriction;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Restrictions;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Sorts;
import de.uni_koblenz.edl.preprocessor.schema.grammar.StartSymbols;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Syntax;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Variables;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.ATerm;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Associativity;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.AssociativityValue;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Attribute;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Bracket;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Id;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.Preference;
import de.uni_koblenz.edl.preprocessor.schema.grammar.attribute.PreferenceValue;
import de.uni_koblenz.edl.preprocessor.schema.section.Exports;
import de.uni_koblenz.edl.preprocessor.schema.section.Hiddens;
import de.uni_koblenz.edl.preprocessor.schema.section.Import;
import de.uni_koblenz.edl.preprocessor.schema.section.Imports;
import de.uni_koblenz.edl.preprocessor.schema.section.IsContainedInExports;
import de.uni_koblenz.edl.preprocessor.schema.section.IsContainedInHiddens;
import de.uni_koblenz.edl.preprocessor.schema.section.IsImportOf;
import de.uni_koblenz.edl.preprocessor.schema.section.IsSectionOf;
import de.uni_koblenz.edl.preprocessor.schema.section.Section;
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
import de.uni_koblenz.edl.preprocessor.schema.term.IsAttachedBy;
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
import de.uni_koblenz.edl.preprocessor.schema.term.Label;
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

public class SDFGenerator {

	public ParseTable generateSDF(EDLGraph graph, String outputPath,
			String fileName, String nameOfStartModule) {
		String outputFileName = fileName.toLowerCase().endsWith(".def") ? fileName
				: fileName + ".def";
		return generateSDF(graph, new File(outputPath + outputFileName),
				nameOfStartModule);
	}

	public ParseTable generateSDF(EDLGraph graph, File sdfOutput,
			String nameOfStartModule) {
		String searchSpace = sdfOutput.getAbsolutePath();
		searchSpace = searchSpace.substring(0,
				searchSpace.lastIndexOf(File.separatorChar) + 1);
		String outputName = sdfOutput.getName();
		outputName = outputName.substring(0, outputName.lastIndexOf('.'));
		// create .def file
		if (createSDFFile(graph, sdfOutput)) {
			// convert SDF to parse table
			return createParseTable(searchSpace, outputName, nameOfStartModule);
		}
		return null;
	}

	private boolean createSDFFile(EDLGraph graph, File sdfOutput) {
		if (EDLPreprocessor.printDebugInformationToTheConsole) {
			System.out.println("\tCreating .def file...");
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(sdfOutput));

			Definition definition = graph.getFirstDefinition();
			if (definition != null) {
				assert definition.getNextDefinition() == null : "An EDLGraph must have only one Definition vertex.";
				convertDefinition(bw, definition);
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * @return the name of the sdf2table executable
	 */
	public static String sdf2tableName() {
		if (System.getProperty("os.name", "-dunno-").toLowerCase()
				.startsWith("win")) {
			return "sdf2Table";
		}
		return "sdf2table";
	}

	private ParseTable createParseTable(String searchSpace, String outputName,
			String startModule) {
		if (EDLPreprocessor.printDebugInformationToTheConsole) {
			System.out.println("\tCreating parse table...");
		}
		String pathOfParseTable = searchSpace + outputName + ".tbl";
		String pathOfDefinition = searchSpace + outputName + ".def";
		try {
			Process process = Runtime
					.getRuntime()
					.exec(new String[] { sdf2tableName(), "-m", startModule,
							"-i", pathOfDefinition, "-o", pathOfParseTable /*
																			 * ,
																			 * "-t"
																			 */},
							null, new File("./"));
			BufferedInputStream errorstream = new BufferedInputStream(
					process.getErrorStream());
			BufferedInputStream inputstream = new BufferedInputStream(
					process.getInputStream());

			process.waitFor();

			int c = -1;
			String delim = "\n";
			while ((c = inputstream.read()) != -1) {
				System.out.print(delim + (char) c);
				delim = "";
			}

			if (process.exitValue() != 0) {
				int e = -1;
				StringBuilder sb = new StringBuilder();
				while ((e = errorstream.read()) != -1) {
					sb.append((char) e);
				}
				throw new SemanticActionException(sb.toString());
			}
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
		ParseTable parseTable = null;
		try {
			TermFactory factory = new TermFactory();
			IStrategoTerm tableTerm = new TermReader(factory)
					.parseFromFile(pathOfParseTable);
			parseTable = new ParseTable(tableTerm, factory);
		} catch (ParseError e) {
			throw new GrammarException("Loading of generated parse table \""
					+ pathOfParseTable + "\" caused an exception.", e);
		} catch (IOException e) {
			throw new GrammarException("Loading of generated parse table \""
					+ pathOfParseTable + "\" caused an exception.", e);
		} catch (InvalidParseTableException e) {
			throw new GrammarException("Loading of generated parse table \""
					+ pathOfParseTable + "\" caused an exception.", e);
		}
		return parseTable;
	}

	private void convertDefinition(Appendable appendable, Definition definition)
			throws IOException {
		appendable.append("definition\n");

		for (IsModuleOf imo : definition
				.getIsModuleOfIncidences(EdgeDirection.IN)) {
			Module module = (Module) imo.getThat();
			convertModule(appendable, module);
		}
	}

	private void convertModule(Appendable appendable, Module module)
			throws IOException {
		appendable.append("\n");
		appendable.append("module ");
		convertModuleName(appendable, module, false);
		appendable.append("\n");

		// create sections of module
		for (IsSectionOf iso : module
				.getIsSectionOfIncidences(EdgeDirection.IN)) {
			Section section = (Section) iso.getThat();
			convertSection(appendable, section);
		}

	}

	private void convertModuleName(Appendable appendable, Module module,
			boolean quote) throws IOException {
		String moduleName = module.get_identifier().get_name();
		appendable.append(moduleName);

		// create parameter of module
		String delim = "[";
		for (IsParameterOfModule ipom : module
				.getIsParameterOfModuleIncidences(EdgeDirection.IN)) {
			Term term = (Term) ipom.getThat();
			appendable.append(delim);
			convertTerm(appendable, term, quote);
			delim = " ";
		}
		if (delim.equals(" ")) {
			appendable.append("]");
		}
	}

	/*
	 * conversion of sections
	 */

	private void convertSection(Appendable appendable, Section section)
			throws IOException {
		if (section.isInstanceOf(Imports.VC)) {
			convertImports(appendable, (Imports) section, false);
		} else if (section.isInstanceOf(Exports.VC)) {
			convertExports(appendable, (Exports) section);
		} else if (section.isInstanceOf(Hiddens.VC)) {
			convertHiddens(appendable, (Hiddens) section);
		}
	}

	private void convertImports(Appendable appendable, Imports imports,
			boolean indentOnceMore) throws IOException {
		if (indentOnceMore) {
			appendable.append("\t");
		}
		appendable.append("\timports\n");

		// convert import
		for (IsImportOf iio : imports.getIsImportOfIncidences(EdgeDirection.IN)) {
			Import imp = (Import) iio.getThat();
			appendable.append("\t\t");
			if (indentOnceMore) {
				appendable.append("\t");
			}
			convertModuleName(appendable, imp.get_module(), false);

			// convert renamings of import
			String delim = "[";
			for (IsRenamingOf iro : imp
					.getIsRenamingOfIncidences(EdgeDirection.OUT)) {
				appendable.append(delim);
				convertRenaming(appendable, (Renaming) iro.getThat());
				delim = " ";
			}
			if (!delim.equals("[")) {
				appendable.append("]");
			}

			appendable.append("\n");
		}
	}

	private void convertExports(Appendable appendable, Exports exports)
			throws IOException {
		appendable.append("\texports\n");

		// convert contained grammar
		for (IsContainedInExports icie : exports
				.getIsContainedInExportsIncidences(EdgeDirection.IN)) {
			convertGrammar(appendable, (Grammar) icie.getThat());
		}
	}

	private void convertHiddens(Appendable appendable, Hiddens hiddens)
			throws IOException {
		appendable.append("\thiddens\n");

		// convert contained grammar
		for (IsContainedInHiddens icie : hiddens
				.getIsContainedInHiddensIncidences(EdgeDirection.IN)) {
			convertGrammar(appendable, (Grammar) icie.getThat());
		}
	}

	/*
	 * conversion of grammar
	 */

	private void convertGrammar(Appendable appendable, Grammar grammar)
			throws IOException {
		if (grammar.isInstanceOf(EmptyGrammar.VC)) {
			convertEmptyGrammar(appendable, (EmptyGrammar) grammar);
		} else if (grammar.isInstanceOf(Aliases.VC)) {
			convertAliases(appendable, (Aliases) grammar);
		} else if (grammar.isInstanceOf(Sorts.VC)) {
			convertSorts(appendable, (Sorts) grammar);
		} else if (grammar.isInstanceOf(StartSymbols.VC)) {
			convertStartSymbols(appendable, (StartSymbols) grammar);
		} else if (grammar.isInstanceOf(Priorities.VC)) {
			convertPriorities(appendable, (Priorities) grammar);
		} else if (grammar.isInstanceOf(Restrictions.VC)) {
			convertRestrictions(appendable, (Restrictions) grammar);
		} else if (grammar.isInstanceOf(Variables.VC)) {
			convertVariables(appendable, (Variables) grammar);
		} else if (grammar.isInstanceOf(Syntax.VC)) {
			convertSyntax(appendable, (Syntax) grammar);
		} else if (grammar.isInstanceOf(Imports.VC)) {
			convertImports(appendable, (Imports) grammar, true);
		}
	}

	private String getGrammarType(GrammarType grammarType) {
		switch (grammarType) {
		case CONTEXT_FREE:
			return "context-free ";
		case LEXICAL:
			return "lexical ";
		case KERNEL:
			return "";
		default:
			return "";
		}
	}

	private void convertEmptyGrammar(Appendable appendable,
			EmptyGrammar emptyGrammar) throws IOException {
		appendable.append("\t\t(/)\n");
	}

	private void convertAliases(Appendable appendable, Aliases aliases)
			throws IOException {
		appendable.append("\t\taliases\n");

		// convert Alias
		for (IsAliasOf iao : aliases.getIsAliasOfIncidences(EdgeDirection.IN)) {
			appendable.append("\t\t\t");
			Alias alias = (Alias) iao.getThat();
			convertTerm(appendable, alias.get_newTerm(), false);
			appendable.append(" -> ");
			convertTerm(appendable, alias.get_originalTerm(), false);
			appendable.append("\n");
		}
	}

	private void convertSorts(Appendable appendable, Sorts sorts)
			throws IOException {
		appendable.append("\t\tsorts");
		for (IsSortOf iso : sorts.getIsSortOfIncidences(EdgeDirection.IN)) {
			appendable.append(" ");
			convertTerm(appendable, (Term) iso.getThat(), false);
		}
		appendable.append("\n");
	}

	private void convertStartSymbols(Appendable appendable,
			StartSymbols startsymbols) throws IOException {
		appendable.append("\t\t");
		appendable.append(getGrammarType(startsymbols.get_type()));
		appendable.append("start-symbols");
		for (IsStartSymbolOf isso : startsymbols
				.getIsStartSymbolOfIncidences(EdgeDirection.IN)) {
			appendable.append(" ");
			convertTerm(appendable, (Term) isso.getThat(), false);
		}
		appendable.append("\n");
	}

	private void convertPriorities(Appendable appendable, Priorities priorities)
			throws IOException {
		appendable.append("\t\t");
		appendable.append(getGrammarType(priorities.get_type()));
		appendable.append("priorities\n");
		String delim = "\t\t\t";
		for (IsPriorityOf ipo : priorities
				.getIsPriorityOfIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertPriority(appendable, (Priority) ipo.getThat());
			delim = ",\n\n\t\t\t";
		}
		if (!delim.equals("\t\t\t")) {
			appendable.append("\n");
		}
	}

	private void convertPriority(Appendable appendable, Priority priority)
			throws IOException {
		String baseDelim = ">";
		if (priority.isInstanceOf(AssocPriority.VC)) {
			baseDelim = getAssociativityValue(((AssocPriority) priority)
					.get_assoc());
		}

		String delim = "";
		for (IsPriorityGroupOf ipgo : priority
				.getIsPriorityGroupOfIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			Group group = (Group) ipgo.getThat();
			convertGroup(appendable, group);

			delim = "\n\t\t\t";
			if ((ipgo.get_argumentIndicator() != null)
					&& !ipgo.get_argumentIndicator().isEmpty()) {
				delim += "<";
				String separator = "";
				for (Integer integer : ipgo.get_argumentIndicator()) {
					delim += separator;
					delim += integer;
					separator = ",";
				}
				delim += ">";
			}
			if (ipgo.is_isTransitive()) {
				delim += ".";
			}
			delim += baseDelim + "\n\t\t\t";
		}
	}

	private void convertGroup(Appendable appendable, Group group)
			throws IOException {
		String delim = "";
		appendable.append("{");
		if (group.get_assoc() != null) {
			appendable.append(getAssociativityValue(group.get_assoc())).append(
					":");
			delim = "\n\t\t\t ";
		}

		// convert productions in group
		for (IsProductionOfGroup ipog : group
				.getIsProductionOfGroupIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertProduction(appendable, (Production) ipog.getThat(), false,
					false);
			delim = "\n\t\t\t ";
		}
		appendable.append("}");
	}

	private void convertRestrictions(Appendable appendable,
			Restrictions restrictions) throws IOException {
		appendable.append("\t\t");
		appendable.append(getGrammarType(restrictions.get_type()));
		appendable.append("restrictions\n");
		for (IsRestrictionOf iro : restrictions
				.getIsRestrictionOfIncidences(EdgeDirection.IN)) {
			appendable.append("\t\t\t");
			convertRestriction(appendable, (Restriction) iro.getThat());
			appendable.append("\n");
		}
	}

	private void convertRestriction(Appendable appendable,
			Restriction restriction) throws IOException {
		// convert restricted terms
		String delim = "";
		for (IsRestrictedTermOf irto : restriction
				.getIsRestrictedTermOfIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) irto.getThat(), false);
			delim = " ";
		}

		appendable.append(delim).append("-/- ");

		// convert lookahead
		convertLookahead(appendable, restriction.get_lookahead());
	}

	private void convertLookahead(Appendable appendable, Lookahead lookahead)
			throws IOException {
		if (lookahead.isInstanceOf(CharacterClass.VC)) {
			convertCharacterClass(appendable, (CharacterClass) lookahead, false);
		} else if (lookahead
				.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.grammar.Alternative.VC)) {
			convertLookaheadAlternative(
					appendable,
					(de.uni_koblenz.edl.preprocessor.schema.grammar.Alternative) lookahead);
		} else if (lookahead
				.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.grammar.Sequence.VC)) {
			convertLookaheadSequence(
					appendable,
					(de.uni_koblenz.edl.preprocessor.schema.grammar.Sequence) lookahead);
		} else if (lookahead
				.isInstanceOf(de.uni_koblenz.edl.preprocessor.schema.grammar.List.VC)) {
			convertLookaheadList(
					appendable,
					(de.uni_koblenz.edl.preprocessor.schema.grammar.List) lookahead);
		}

		Lookahead following = lookahead.get_following();
		if (following != null) {
			appendable.append(".");
			convertLookahead(appendable, following);
		}
	}

	private void convertLookaheadAlternative(
			Appendable appendable,
			de.uni_koblenz.edl.preprocessor.schema.grammar.Alternative alternative)
			throws IOException {
		convertLookahead(appendable, alternative.get_lhs());
		appendable.append(" | ");
		convertLookahead(appendable, alternative.get_rhs());
	}

	private void convertLookaheadSequence(Appendable appendable,
			de.uni_koblenz.edl.preprocessor.schema.grammar.Sequence sequence)
			throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOf ipo : sequence.getIsPartOfIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertLookahead(appendable, (Lookahead) ipo.getThat());
			delim = " ";
		}
		appendable.append(")");
	}

	private void convertLookaheadList(Appendable appendable,
			de.uni_koblenz.edl.preprocessor.schema.grammar.List list)
			throws IOException {
		appendable.append("[[");
		String delim = "";
		for (IsListPartOf ilpo : list
				.getIsListPartOfIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertLookahead(appendable, (Lookahead) ilpo.getThat());
			delim = ", ";
		}
		appendable.append("]]");
	}

	private void convertVariables(Appendable appendable, Variables variables)
			throws IOException {
		appendable.append("\t\t");
		appendable.append(getGrammarType(variables.get_type()));
		appendable.append("variables\n");
		for (IsVariableDefinitionOf ivdo : variables
				.getIsVariableDefinitionOfIncidences(EdgeDirection.IN)) {
			appendable.append("\t\t\t");
			convertProduction(appendable, (Production) ivdo.getThat(), false,
					true);
			appendable.append("\n");
		}
	}

	private void convertSyntax(Appendable appendable, Syntax syntax)
			throws IOException {
		appendable.append("\t\t");
		appendable.append(getGrammarType(syntax.get_type()));
		appendable.append("syntax\n");
		for (IsProductionOf ipo : syntax
				.getIsProductionOfIncidences(EdgeDirection.IN)) {
			appendable.append("\t\t\t");
			convertProduction(appendable, (Production) ipo.getThat(), false,
					true);
			appendable.append("\n");
		}
	}

	private void convertRenaming(Appendable appendable, Renaming renaming)
			throws IOException {
		Term originalTerm = renaming.get_originalTerm();
		if (originalTerm != null) {
			convertTerm(appendable, originalTerm, false);
		} else {
			convertProduction(appendable, renaming.get_originalProduction(),
					false, false);
		}
		appendable.append(" => ");
		if (originalTerm != null) {
			convertTerm(appendable, renaming.get_newTerm(), false);
		} else {
			convertProduction(appendable, renaming.get_newProduction(), false,
					false);
		}
	}

	private void convertProduction(Appendable appendable,
			Production production, boolean quote,
			boolean createDefinedAsAttribute) throws IOException {
		// convert body
		Sequence body = null;
		for (IsBodyTermOfProduction ibtop : production
				.getIsBodyTermOfProductionIncidences(EdgeDirection.IN)) {
			assert body == null;
			body = (Sequence) ibtop.getThat();
		}
		if (body != null) {
			String delim = "";
			for (IsPartOfSequence ipos : body
					.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
				appendable.append(delim);
				convertTerm(appendable, (Term) ipos.getThat(), quote);
				delim = " ";
			}
			if (!delim.isEmpty()) {
				appendable.append(" ");
			}
		}

		appendable.append("-> ");

		// convert head
		convertTerm(appendable, production.get_headTerm(), quote);

		// convert attributes
		if (createDefinedAsAttribute
				|| (production.getFirstIsAttributeOfIncidence(EdgeDirection.IN) != null)) {
			appendable.append(" {");
			String delim = "";
			for (IsAttributeOf iao : production
					.getIsAttributeOfIncidences(EdgeDirection.IN)) {
				appendable.append(delim);
				convertAttribute(appendable, (Attribute) iao.getThat(), quote);
				delim = ", ";
			}
			if (createDefinedAsAttribute) {
				appendable.append(delim);
				appendable.append(Rule.ATTRIBUTE_DEFINED_AS).append("(\"");
				convertProduction(appendable, production, true, false);
				appendable.append("\")");
			}
			appendable.append("}");
		}
	}

	private void convertAttribute(Appendable appendable, Attribute attribute,
			boolean quote) throws IOException {
		if (attribute.isInstanceOf(Associativity.VC)) {
			appendable.append(getAssociativityValue(((Associativity) attribute)
					.get_value()));
		} else if (attribute.isInstanceOf(Preference.VC)) {
			appendable.append(getPreferenceValue(((Preference) attribute)
					.get_value()));
		} else if (attribute.isInstanceOf(ATerm.VC)) {
			String value = ((ATerm) attribute).get_value();
			if (quote) {
				value = quote(value);
			}
			appendable.append(value);
		} else if (attribute.isInstanceOf(Bracket.VC)) {
			appendable.append("bracket");
		} else if (attribute.isInstanceOf(Id.VC)) {
			appendable.append("id(");
			convertModuleName(appendable,
					((Id) attribute).get_referencedModule(), quote);
			appendable.append(")");
		}
	}

	private String getAssociativityValue(AssociativityValue associativityValue) {
		switch (associativityValue) {
		case ASSOC:
			return "assoc";
		case LEFT:
			return "left";
		case NON_ASSOC:
			return "non-assoc";
		case RIGHT:
			return "right";
		default:
			return "";
		}
	}

	private String getPreferenceValue(PreferenceValue preferenceValue) {
		switch (preferenceValue) {
		case AVOID:
			return "avoid";
		case PREFER:
			return "prefer";
		case REJECT:
			return "reject";
		default:
			return "";
		}
	}

	/*
	 * conversion of terms
	 */

	private void convertTerm(Appendable appendable, Term term, boolean quote)
			throws IOException {
		for (IsAttachedBy iab : term
				.getIsAttachedByIncidences(EdgeDirection.IN)) {
			Label label = (Label) iab.getThat();
			String labelName = label.get_identifier().get_name();
			if (quote) {
				labelName = quote(labelName);
			}
			appendable.append(labelName).append(":");
		}
		if (term.isInstanceOf(Start.VC)) {
			appendable.append("<START>");
		} else if (term.isInstanceOf(FileStart.VC)) {
			appendable.append("<Start>");
		} else if (term.isInstanceOf(LAYOUT.VC)) {
			appendable.append("LAYOUT");
		} else if (term.isInstanceOf(KernelTerm.VC)) {
			convertKernelTerm(appendable, (KernelTerm) term, quote);
		} else if (term.isInstanceOf(Literal.VC)) {
			convertLiteral(appendable, (Literal) term, quote);
		} else if (term.isInstanceOf(Sort.VC)) {
			convertSort(appendable, (Sort) term, quote);
		} else if (term.isInstanceOf(CharacterClass.VC)) {
			convertCharacterClass(appendable, (CharacterClass) term, quote);
		} else if (term.isInstanceOf(Sequence.VC)) {
			convertSequence(appendable, (Sequence) term, quote);
		} else if (term.isInstanceOf(UnaryTerm.VC)) {
			convertUnaryTerm(appendable, (UnaryTerm) term, quote);
		} else if (term.isInstanceOf(Alternative.VC)) {
			convertAlternative(appendable, (Alternative) term, quote);
		} else if (term.isInstanceOf(List.VC)) {
			convertList(appendable, (List) term, quote);
		} else if (term.isInstanceOf(Lifting.VC)) {
			convertLifting(appendable, (Lifting) term, quote);
		} else if (term.isInstanceOf(ConstructedTerm.VC)) {
			convertConsructedTerm(appendable, (ConstructedTerm) term, quote);
		}
	}

	private void convertKernelTerm(Appendable appendable,
			KernelTerm kernelTerm, boolean quote) throws IOException {
		appendable.append("<");
		convertTerm(appendable, kernelTerm.get_term(), quote);
		appendable.append("-");
		appendable.append(kernelTerm.get_type().toString().toUpperCase());
		appendable.append(">");
	}

	private void convertLiteral(Appendable appendable, Literal literal,
			boolean quote) throws IOException {
		if (literal.is_isSingleQuoted()) {
			appendable.append("'");
		} else {
			appendable.append(quote ? "\\\"" : "\"");
		}
		appendable.append(quote ? quote(literal.get_value()) : literal
				.get_value());
		if (literal.is_isSingleQuoted()) {
			appendable.append("'");
		} else {
			appendable.append(quote ? "\\\"" : "\"");
		}
	}

	private void convertSort(Appendable appendable, Sort sort, boolean quote)
			throws IOException {
		appendable.append(sort.get_identifier().get_name());

		// convert parameter
		String delim = "[[";
		for (IsParameterOfSort ipos : sort
				.getIsParameterOfSortIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat(), quote);
			delim = ", ";
		}
		if (!delim.equals("[[")) {
			appendable.append("]]");
		}
	}

	private void convertCharacterClass(Appendable appendable,
			CharacterClass characterClass, boolean quote) throws IOException {
		if (characterClass.isInstanceOf(CharacterRanges.VC)) {
			convertCharacterRanges(appendable,
					(CharacterRanges) characterClass, quote);
		} else if (characterClass.isInstanceOf(Complement.VC)) {
			convertComplement(appendable, (Complement) characterClass, quote);
		} else if (characterClass
				.isInstanceOf(BinaryCharacterClassOperation.VC)) {
			convertBinaryCharacterClassOperation(appendable,
					(BinaryCharacterClassOperation) characterClass, quote);
		}
	}

	private void convertCharacterRanges(Appendable appendable,
			CharacterRanges characterRanges, boolean quote) throws IOException {
		appendable.append("[");
		for (IsCharacterRangeOf icro : characterRanges
				.getIsCharacterRangeOfIncidences(EdgeDirection.IN)) {
			CharacterRange characterRange = (CharacterRange) icro.getThat();
			appendable.append(quote ? quote(characterRange.get_minCharacter())
					: characterRange.get_minCharacter());
			if (!characterRange.get_minCharacter().equals(
					characterRange.get_maxCharacter())) {
				appendable.append("-");
				appendable.append(quote ? quote(characterRange
						.get_maxCharacter()) : characterRange
						.get_maxCharacter());
			}
		}
		appendable.append("]");
	}

	private void convertComplement(Appendable appendable,
			Complement complement, boolean quote) throws IOException {
		appendable.append("~");
		convertCharacterClass(appendable, complement.get_characterClass(),
				quote);
	}

	private void convertBinaryCharacterClassOperation(Appendable appendable,
			BinaryCharacterClassOperation binaryCharacterClassOperation,
			boolean quote) throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOfBinaryCharacterClassOperation ipobcco : binaryCharacterClassOperation
				.getIsPartOfBinaryCharacterClassOperationIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertCharacterClass(appendable,
					(CharacterClass) ipobcco.getThat(), quote);
			if (delim.isEmpty()) {
				if (binaryCharacterClassOperation.isInstanceOf(Difference.VC)) {
					delim = "/";
				} else if (binaryCharacterClassOperation
						.isInstanceOf(Intersection.VC)) {
					delim = quote ? "/\\\\" : "/\\";
				} else if (binaryCharacterClassOperation.isInstanceOf(Union.VC)) {
					delim = quote ? "\\\\/" : "\\/";
				}
			}
		}
		appendable.append(")");
	}

	private void convertSequence(Appendable appendable, Sequence sequence,
			boolean quote) throws IOException {
		appendable.append("(");
		String delim = "";
		for (IsPartOfSequence ipos : sequence
				.getIsPartOfSequenceIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat(), quote);
			delim = " ";
		}
		appendable.append(")");
	}

	private void convertUnaryTerm(Appendable appendable, UnaryTerm unaryTerm,
			boolean quote) throws IOException {
		convertTerm(appendable, unaryTerm.get_term(), quote);
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
			Alternative alternative, boolean quote) throws IOException {
		String delim = "";
		for (IsPartOfBinaryTerm ipos : alternative
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat(), quote);
			delim = " | ";
		}
	}

	private void convertList(Appendable appendable, List list, boolean quote)
			throws IOException {
		appendable.append("{");
		String delim = "";
		for (IsPartOfBinaryTerm ipos : list
				.getIsPartOfBinaryTermIncidences(EdgeDirection.IN)) {
			appendable.append(delim);
			convertTerm(appendable, (Term) ipos.getThat(), quote);
			delim = " ";
		}
		appendable.append("}");
		appendable.append(getKleeneOperator(list.get_type()));
	}

	private void convertLifting(Appendable appendable, Lifting lifting,
			boolean quote) throws IOException {
		appendable.append("´");
		convertTerm(appendable, lifting.get_term(), quote);
		appendable.append("´");
	}

	private void convertConsructedTerm(Appendable appendable,
			ConstructedTerm constructedTerm, boolean quote) throws IOException {
		if (constructedTerm.isInstanceOf(PrefixFunction.VC)
				|| constructedTerm.isInstanceOf(Tuple.VC)) {
			convertPrefixFunctionOrTuple(appendable, constructedTerm, quote);
		} else if (constructedTerm.isInstanceOf(Function.VC)
				|| constructedTerm.isInstanceOf(Strategy.VC)) {
			convertFunctionOrStrategy(appendable, constructedTerm, quote);
		}
	}

	private void convertPrefixFunctionOrTuple(Appendable appendable,
			ConstructedTerm constructedTerm, boolean quote) throws IOException {
		String delim = "";
		for (Edge edge : constructedTerm.incidences(EdgeDirection.IN)) {
			if (edge.isInstanceOf(IsFunctionNameOf.EC)
					|| edge.isInstanceOf(IsSyntaxElementOf.EC)) {
				String term = ((Literal) edge.getThat()).get_value();
				appendable.append(quote ? quote(term) : term);
			} else if (edge.isInstanceOf(IsElementOf.EC)) {
				appendable.append(delim);
				convertTerm(appendable, (Term) edge.getThat(), quote);
				delim = " ";
			}
		}
	}

	private void convertFunctionOrStrategy(Appendable appendable,
			ConstructedTerm constructedTerm, boolean quote) throws IOException {
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
				convertTerm(appendable, (Term) edge.getThat(), quote);
				delim = " ";
			}
		}
	}

	private String quote(String string) {
		StringBuilder sb = new StringBuilder();
		for (char c : string.toCharArray()) {
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
