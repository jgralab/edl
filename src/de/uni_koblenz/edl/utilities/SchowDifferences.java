package de.uni_koblenz.edl.utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchowDifferences {

	public void process() {
		String path = "./src/de/uni_koblenz/edl/preprocessor/edl2edlgraph/";
		ArrayList<String> parserFileContent = new ArrayList<String>();
		ArrayList<String> generatedFileContent = new ArrayList<String>();
		readFileContent(parserFileContent, path + "EDL2EDLGraph.java");
		readFileContent(generatedFileContent, path
				+ "EDL2EDLGraphBaseImpl.java");

		Map<String, String> method2JavaDoc = new HashMap<String, String>();
		Map<String, String> javaDoc2Method = new HashMap<String, String>();
		for (int i = 0; i < generatedFileContent.size(); i++) {
			String methodname = null;
			String javadoc = null;

			String line = generatedFileContent.get(i).trim();
			Pattern pattern = Pattern
					.compile("^protected\\svoid\\sexecute_Rule");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				methodname = line.substring(0, line.lastIndexOf("(") + 1);
				javadoc = extractJavaDoc(generatedFileContent, i - 2);
				method2JavaDoc.put(methodname, javadoc);
				javaDoc2Method.put(
						javadoc.replaceAll("Rule \\d+\\:", "Rule :"),
						methodname);
			}
		}

		for (int i = 0; i < parserFileContent.size(); i++) {
			String methodname = null;
			String javadoc = null;

			String line = parserFileContent.get(i).trim();
			Pattern pattern = Pattern
					.compile("^protected\\svoid\\sexecute_Rule");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				methodname = line.substring(0, line.lastIndexOf("(") + 1);
				javadoc = extractJavaDoc(parserFileContent, i - 3);
				String newJavaDoc = method2JavaDoc.get(methodname);
				if (newJavaDoc == null) {
					System.out.println("NOT IN NEW FILE: " + methodname + " "
							+ javadoc);
					String newMethodName = javaDoc2Method.get(javadoc
							.replaceAll("Rule \\d+\\:", "Rule :"));
					if (newMethodName != null) {
						System.out.println("Now the old JavaDoc is at method: "
								+ newMethodName);
					}
					System.out.println();
				} else {
					if (!newJavaDoc.replaceAll("\\s+", "").equals(
							javadoc.replaceAll("\\s+", ""))) {
						System.out.println("DIFFERENCE: " + methodname
								+ "\nold: " + javadoc + "\nnew: " + newJavaDoc);
						String newMethodName = javaDoc2Method.get(javadoc
								.replaceAll("Rule \\d+\\:", "Rule :"));
						if (newMethodName != null) {
							System.out
									.println("Now the old JavaDoc is at method: "
											+ newMethodName);
						}
						System.out.println();
					}
				}
			}
		}
	}

	private String extractJavaDoc(ArrayList<String> parserFileContent,
			int lastLineWithJavaDocContent) {
		String javadoc;
		int lineOfJavadoc = lastLineWithJavaDocContent;
		StringBuilder javaDocs = new StringBuilder();
		String javaDocLine = parserFileContent.get(lineOfJavadoc--).trim();
		while (javaDocLine.startsWith("*")) {
			if (!javaDocLine.startsWith("*/")) {
				javaDocs.insert(0, javaDocLine.substring(1));
			}
			javaDocLine = parserFileContent.get(lineOfJavadoc--).trim();
		}
		javadoc = javaDocs.toString();
		return javadoc;
	}

	private void readFileContent(ArrayList<String> fileContent, String fileName) {
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new FileReader(fileName));
			String line = null;
			while ((line = lnr.readLine()) != null) {
				fileContent.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (lnr != null) {
				try {
					lnr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new SchowDifferences().process();
	}

}
