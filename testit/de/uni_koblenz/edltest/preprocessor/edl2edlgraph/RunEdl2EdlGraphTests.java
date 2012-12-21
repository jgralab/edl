package de.uni_koblenz.edltest.preprocessor.edl2edlgraph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.ATerms_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Aliases_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Attribute_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Grammar_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Pattern_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Priorities_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Production_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Renaming_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Restrictions_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Sorts_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.StartSymbols_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Syntax_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.grammar.Variables_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.DefaultValuesSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.ExportsSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.GlobalActionsSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.HiddensSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.ImportDeclarationsSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.ImportsSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.IslandSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.SchemaSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.SymbolTablesSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.sections.UserCodeSection_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions.Expressions_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions.SemanticAction_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions.Statements_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.semantic_actions.UserCode_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.AbbreviationTerm_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.Basic_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.CharacterClass_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.Label_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.Literal_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.PrefixFunction_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.Sort_Test;
import de.uni_koblenz.edltest.preprocessor.edl2edlgraph.terms.Term_Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SDF_Test.class, Module_Test.class,
		DefaultValuesSection_Test.class, ExportsSection_Test.class,
		GlobalActionsSection_Test.class, HiddensSection_Test.class,
		ImportDeclarationsSection_Test.class, ImportsSection_Test.class,
		IslandSection_Test.class, SchemaSection_Test.class,
		SymbolTablesSection_Test.class, UserCodeSection_Test.class,
		Grammar_Test.class, Aliases_Test.class, ATerms_Test.class,
		Attribute_Test.class, Pattern_Test.class, Production_Test.class,
		Priorities_Test.class, Renaming_Test.class, Restrictions_Test.class,
		Sorts_Test.class, StartSymbols_Test.class, Syntax_Test.class,
		Variables_Test.class, AbbreviationTerm_Test.class,
		PrefixFunction_Test.class, Basic_Test.class, CharacterClass_Test.class,
		Label_Test.class, Literal_Test.class, Sort_Test.class, Term_Test.class,
		SemanticAction_Test.class, Statements_Test.class, UserCode_Test.class,
		Expressions_Test.class })
public class RunEdl2EdlGraphTests {

}
