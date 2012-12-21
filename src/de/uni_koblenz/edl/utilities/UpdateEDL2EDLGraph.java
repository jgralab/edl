package de.uni_koblenz.edl.utilities;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateEDL2EDLGraph {

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
				javaDoc2Method.put(javadoc.replaceAll("Rule \\d+\\:", "Rule :")
						.replaceAll("\\s+", ""), methodname);
			}
		}

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(path + "EDL2EDLGraph2.java"));
			int lastWrittenContentLine = -1;

			boolean isJavaDoc = false;
			int indexOfJavaDocEnd = 0;

			for (int i = 0; i < parserFileContent.size(); i++) {
				String methodname = null;
				String javadoc = null;

				String line = parserFileContent.get(i).trim();
				if (line.startsWith("/**")) {
					isJavaDoc = true;
				} else if (line.startsWith("*/")) {
					indexOfJavaDocEnd = i;
				}

				Pattern pattern = Pattern
						.compile("^protected\\svoid\\sexecute_Rule");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					methodname = line.substring(0, line.lastIndexOf("(") + 1);
					javadoc = extractJavaDoc(parserFileContent, i - 3);
					String newJavaDoc = method2JavaDoc.get(methodname);
					if (newJavaDoc == null
							|| !newJavaDoc.replaceAll("\\s+", "").equals(
									javadoc.replaceAll("\\s+", ""))) {
						// the current rule is not any more in the newly
						// generated parser or the numbers have changed
						String newMethodName = javaDoc2Method.get(javadoc
								.replaceAll("Rule \\d+\\:", "Rule :")
								.replaceAll("\\s+", ""));
						if (newMethodName != null) {
							// the new method could be found
							newMethodName = newMethodName.substring(0,
									newMethodName.lastIndexOf('_'))
									+ methodname.substring(line
											.lastIndexOf('_'));
							String updatedJavaDoc = method2JavaDoc
									.get(newMethodName);
							assert updatedJavaDoc != null;
							// update JavaDoc
							bw.write("\t/**\n");
							bw.write("\t * " + updatedJavaDoc + "\n");
							bw.write("\t */\n");
							// write Annotations
							writeLines(parserFileContent, bw,
									indexOfJavaDocEnd + 1, i);
							// write current contentLine
							bw.write("\t"
									+ newMethodName
									+ (line.lastIndexOf('(') + 1 < line
											.length() ? line.substring(line
											.lastIndexOf('(') + 1) : "") + "\n");
						} else {
							// no updated method could be found
							// take method as it is
							bw.write("\t// FIXME update current method manually\n");
							writeLines(parserFileContent, bw,
									lastWrittenContentLine + 1, i + 1);
						}
					} else {
						// current method is unchanged
						assert newJavaDoc.replaceAll("\\s+", "").equals(
								javadoc.replaceAll("\\s+", ""));
						writeLines(parserFileContent, bw,
								lastWrittenContentLine + 1, i + 1);
					}
					lastWrittenContentLine = i;
					isJavaDoc = false;
				} else {
					if (line.startsWith("protected")
							|| line.startsWith("public")
							|| line.startsWith("private")) {
						isJavaDoc = false;
					}
					if (!isJavaDoc) {
						writeLines(parserFileContent, bw,
								lastWrittenContentLine + 1, i + 1);
						lastWrittenContentLine = i;
					}
				}
			}
			writeLines(parserFileContent, bw, lastWrittenContentLine + 1,
					parserFileContent.size());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param parserFileContent
	 * @param bw
	 * @param startIndex
	 * @param endIndex
	 *            index of Line which will not be written any more
	 * @throws IOException
	 */
	private void writeLines(ArrayList<String> parserFileContent,
			BufferedWriter bw, int startIndex, int endIndex) throws IOException {
		for (int i = startIndex; i < endIndex; i++) {
			bw.write(parserFileContent.get(i) + "\n");
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
		new UpdateEDL2EDLGraph().process();
	}

}
