package de.uni_koblenz.edl.preprocessor.graphbuildergenerator;

import de.uni_koblenz.edl.preprocessor.EDLPreprocessor;
import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.CodeGenerator;
import de.uni_koblenz.edl.preprocessor.schema.EDLGraph;
import de.uni_koblenz.edl.preprocessor.schema.common.Module;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Grammar;
import de.uni_koblenz.edl.preprocessor.schema.grammar.GrammarType;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsStartSymbolOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Production;
import de.uni_koblenz.edl.preprocessor.schema.grammar.StartSymbols;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Syntax;
import de.uni_koblenz.edl.preprocessor.schema.section.DefaultValues;
import de.uni_koblenz.edl.preprocessor.schema.section.GlobalAction;
import de.uni_koblenz.edl.preprocessor.schema.section.Hiddens;
import de.uni_koblenz.edl.preprocessor.schema.section.ImportDeclarations;
import de.uni_koblenz.edl.preprocessor.schema.section.InitialSection;
import de.uni_koblenz.edl.preprocessor.schema.section.Island;
import de.uni_koblenz.edl.preprocessor.schema.section.Schema;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTables;
import de.uni_koblenz.edl.preprocessor.schema.section.UserCodeSection;
import de.uni_koblenz.edl.preprocessor.schema.term.Term;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;

public class GraphBuilderGenerator {

	public void generateCode(EDLGraph graph, String outputPath,
			GraphHandler... graphHandler) {
		if (EDLPreprocessor.printDebugInformationToTheConsole) {
			System.out.println("\tCreating GraphBuilder...");
		}
		traverse(graph.getFirstDefinition(), graphHandler);
		for (GraphHandler handler : graphHandler) {
			if (handler instanceof CodeGenerator) {
				((CodeGenerator) handler).generateFile(outputPath);
			}
		}
	}

	public void traverse(Vertex vertex, GraphHandler[] graphHandler) {
		if (vertex.isInstanceOf(InitialSection.VC)) {
			if (vertex.isInstanceOf(Schema.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleSchemaSection((Schema) vertex);
				}
			} else if (vertex.isInstanceOf(DefaultValues.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleDefaultValuesSection((DefaultValues) vertex);
				}
			} else if (vertex.isInstanceOf(GlobalAction.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleGlobalActionsSection((GlobalAction) vertex);
				}
			} else if (vertex.isInstanceOf(ImportDeclarations.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleImportDeclarationsSection((ImportDeclarations) vertex);
				}
			} else if (vertex.isInstanceOf(SymbolTables.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleSymbolTablesSection((SymbolTables) vertex);
				}
			} else if (vertex.isInstanceOf(UserCodeSection.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleUserCodeSection((UserCodeSection) vertex);
				}
			} else if (vertex.isInstanceOf(Island.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.handleIslandSection((Island) vertex);
				}
			}
		} else if (vertex.isInstanceOf(Grammar.VC)) {
			if (vertex.isInstanceOf(StartSymbols.VC)) {
				StartSymbols startSymbols = (StartSymbols) vertex;
				GrammarType grammarType = startSymbols.get_type();
				Edge edge = startSymbols
						.getFirstIsStartSymbolOfIncidence(EdgeDirection.IN);
				while (edge != null) {
					Edge next = edge.getNextIncidence(IsStartSymbolOf.EC,
							EdgeDirection.IN);
					for (GraphHandler handler : graphHandler) {
						handler.handleStartSymbol((Term) edge.getThat(),
								grammarType);
					}
					edge = next;
				}
			} else if (vertex.isInstanceOf(Syntax.VC)) {
				Syntax syntax = (Syntax) vertex;
				GrammarType grammarType = syntax.get_type();
				for (Edge edge : syntax
						.getIsProductionOfIncidences(EdgeDirection.IN)) {
					for (GraphHandler handler : graphHandler) {
						handler.handleProduction((Production) edge.getThat(),
								grammarType);
					}
				}
			}
		} else {
			if (vertex.isInstanceOf(Module.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.enterModule((Module) vertex);
				}
			}
			if (!vertex.isInstanceOf(Hiddens.VC)
					|| isInStartModule((Hiddens) vertex)) {
				for (Edge edge : vertex.incidences(EdgeDirection.IN)) {
					traverse(edge.getThat(), graphHandler);
				}
			}
			if (vertex.isInstanceOf(Module.VC)) {
				for (GraphHandler handler : graphHandler) {
					handler.leaveModule();
				}
			}
		}
	}

	private boolean isInStartModule(Hiddens hiddens) {
		return hiddens.get_module() == hiddens.getGraph().getFirstVertex(
				Module.VC);
	}

}
