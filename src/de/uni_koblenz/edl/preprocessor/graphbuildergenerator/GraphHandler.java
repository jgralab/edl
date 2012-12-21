package de.uni_koblenz.edl.preprocessor.graphbuildergenerator;

import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.GlobalAction;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.Schema;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTables;
import de.uni_koblenz.edl.preprocessor.schema.section.UserCodeSection;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;

public interface GraphHandler {

	public void enterModule(Module module);

	public void leaveModule();

	public void handleSchemaSection(Schema schema);

	public void handleDefaultValuesSection(DefaultValues defaultValues);

	public void handleGlobalActionsSection(GlobalAction globalAction);

	public void handleImportDeclarationsSection(
			ImportDeclarations importDeclarations);

	public void handleSymbolTablesSection(SymbolTables symbolTables);

	public void handleUserCodeSection(UserCodeSection userCodeSection);

	public void handleIslandSection(Island island);

	public void handleStartSymbol(Term startSymbol, GrammarType grammarType);

	public void handleProduction(Production production, GrammarType grammarType);

}
