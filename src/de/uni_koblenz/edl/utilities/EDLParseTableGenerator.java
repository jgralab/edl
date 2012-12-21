package de.uni_koblenz.edl.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;

import de.uni_koblenz.edl.SemanticActionException;
import de.uni_koblenz.edl.preprocessor.sdfgenerator.SDFGenerator;

public class EDLParseTableGenerator {

	public static void main(String[] args) {
		String parseTable = "./src/de/uni_koblenz/edl/preprocessor/edl2edlgraph/edl.tbl";
		createTable("grammar/Main", parseTable, "./");
	}

	public static void createTable(String qualifiedStartModuleName,
			String parseTable, String moduleSearchSpace) {
		try {
			Process process = Runtime.getRuntime().exec(
					SDFGenerator.sdf2tableName() + " -c -m "
							+ qualifiedStartModuleName + " -p "
							+ moduleSearchSpace + " -o " + parseTable);
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
				int e = -1;
				while ((e = errorstream.read()) != -1) {
					System.err.print((char) e);
				}
				throw new SemanticActionException(SDFGenerator.sdf2tableName()
						+ " has errored");
			}
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

}
