package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.codegenerator.graphbuilderfile;

import java.io.IOException;

import de.uni_koblenz.edl.preprocessor.schema.common.Module;

public interface ExecuteMethodGenerator {

	public void generateCode(Appendable appendable, Module module,
			SemanticActionGenerator semanticActionGenerator) throws IOException;

	public boolean isEmpty();
}
