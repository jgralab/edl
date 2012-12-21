package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

import java.util.Set;

import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;

public class PatternValue {

	private final boolean executeBefore;

	private final SemanticAction semanticAction;

	private final Set<SymbolTableDefinition> annotatedSymbolTables;

	public PatternValue(boolean executeBefore, SemanticAction semanticAction,
			Set<SymbolTableDefinition> annotatedSymbolTables) {
		this.executeBefore = executeBefore;
		this.semanticAction = semanticAction;
		this.annotatedSymbolTables = annotatedSymbolTables;
	}

	public boolean executeBefore() {
		return executeBefore;
	}

	public SemanticAction getSemanticAction() {
		return semanticAction;
	}

	public Set<SymbolTableDefinition> getAnnotatedSymbolTables() {
		return annotatedSymbolTables;
	}

}
