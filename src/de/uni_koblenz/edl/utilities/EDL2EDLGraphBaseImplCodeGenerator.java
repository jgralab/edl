package de.uni_koblenz.edl.utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.Label;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import de.uni_koblenz.edl.parser.Rule;
import de.uni_koblenz.edl.parser.RuleType;

public class EDL2EDLGraphBaseImplCodeGenerator {

	private static final String NAME_OF_GENERATED_CLASS = "EDL2EDLGraphBaseImpl";

	private static final String PATH = "./src/de/uni_koblenz/edl/preprocessor/edl2edlgraph/";

	public static void main(String[] args) throws ParseError,
			UnsupportedEncodingException, IOException,
			InvalidParseTableException {
		final TermFactory factory = new TermFactory();
		final IStrategoTerm tableTerm = new TermReader(factory)
				.parseFromFile(PATH + "edl.tbl");
		final ParseTable pt = new ParseTable(tableTerm, factory);

		Rule[] rules = null;
		int currentIndex = 0;
		int indexOfFirstLabel = 0;
		List<Label> labels = pt.getLabels();
		for (Label label : labels) {
			if (label == null) {
				currentIndex++;
				indexOfFirstLabel++;
				continue;
			}
			if (rules == null) {
				rules = new Rule[labels.size() - indexOfFirstLabel];
			}
			rules[currentIndex - indexOfFirstLabel] = new Rule(currentIndex,
					label.getProduction());
			currentIndex++;
		}

		new EDL2EDLGraphBaseImplCodeGenerator().generateCode(rules);
	}

	static class RuleTree {
		public Rule rule;
		private RuleTree parent;
		private final List<Set<RuleTree>> children;

		public RuleTree(Rule rule) {
			this.rule = rule;
			children = new ArrayList<Set<RuleTree>>(
					rule.getNumberOfTermsInBody());
		}

		public void addChild(Set<RuleTree> child) {
			children.add(child);
		}

		public RuleTree getParent() {
			return parent;
		}

		public List<Set<RuleTree>> getChildren() {
			return children;
		}

		public Set<RuleTree> getChild(int index) {
			return children.get(index);
		}

		public boolean hasHaedInPath(Rule r) {
			if (rule == r) {
				return true;
			} else if (r.getRule().getSubterm(1) == rule.getRule()
					.getSubterm(1)) {
				return true;
			} else if (parent == null) {
				return false;
			} else {
				return parent.hasHaedInPath(r);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			print(sb, 0);
			return sb.toString();
		}

		private void print(StringBuilder sb, int indent) {
			indent(sb, indent);
			sb.append(rule + "\n");
			int numberOfChild = 0;
			for (Set<RuleTree> ruleTrees : children) {
				if (ruleTrees.isEmpty()) {
					numberOfChild++;
					continue;
				}
				indent(sb, indent + 1);
				sb.append("child " + numberOfChild++ + ":\n");
				for (RuleTree rt : ruleTrees) {
					rt.print(sb, indent + 1);
				}
			}
		}

		private void indent(StringBuilder sb, int indent) {
			for (int i = 0; i < indent; i++) {
				sb.append("\t");
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((rule == null) ? 0 : rule.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			RuleTree other = (RuleTree) obj;
			if (rule == null) {
				if (other.rule != null) {
					return false;
				}
			} else if (!rule.equals(other.rule)) {
				return false;
			}
			return true;
		}
	}

	static abstract class MethodCreator {
		public RuleTree ruleTree;
		public String methodName;

		public MethodCreator(RuleTree rt, String mName) {
			ruleTree = rt;
			methodName = mName;
		}

		public abstract void generateCode(BufferedWriter bw) throws IOException;

		protected void generateJavaDoc(BufferedWriter bw) throws IOException {
			bw.write("\t/**\n");
			generateJavaDocContent(bw, ruleTree);
			bw.write("\t */\n");
		}

		private void generateJavaDocContent(BufferedWriter bw, RuleTree ruleTree)
				throws IOException {
			if (ruleTree.getParent() != null) {
				generateJavaDocContent(bw, ruleTree.getParent());
			}
			if (ruleTree.rule.canHaveSemanticAciton()) {
				bw.write("\t * Rule " + ruleTree.rule.getNumber() + ": ");
				quote(bw, ruleTree.rule.toString());
				bw.write("<br>\n");
			}
		}

		private void quote(BufferedWriter bw, String string) throws IOException {
			for (char c : string.toCharArray()) {
				switch (c) {
				case '<':
					bw.write("&lt;");
					break;
				case '>':
					bw.write("&gt;");
					break;
				default:
					bw.write(c);
				}
			}
		}
	}

	static class ExecuteRuleCreator extends MethodCreator {

		public ExecuteRuleCreator(RuleTree rt, String mName) {
			super(rt, mName);
		}

		@Override
		public void generateCode(BufferedWriter bw) throws IOException {
			bw.write("\n");
			generateJavaDoc(bw);
			bw.write("\tprivate void " + methodName
					+ "(StackElement currentElement, StackElement parent) {\n");

			List<MethodCreator> methods = new ArrayList<MethodCreator>();

			bw.write("\t\tint currentPos = parent.getCurrentSemanticActionPosition();\n");
			bw.write("\t\tif(currentElement==parent){\n");
			// the current rule is reached
			// execute semantic actions at the current position
			bw.write("\t\t\tswitch(currentPos) {\n");
			Rule rule = ruleTree.rule;
			for (int i = 0; i <= rule.getNumberOfTermsInBody(); i++) {
				String newMethodName = methodName + "_Position" + i;
				methods.add(new ExecutePositionCreator(ruleTree, newMethodName));
				bw.write("\t\t\tcase " + i + ":\n");
				bw.write("\t\t\t\t" + newMethodName + "(currentElement);\n");
				bw.write("\t\t\t\tbreak;\n");
			}
			bw.write("\t\t\t}\n");
			bw.write("\t\t}");
			List<Set<RuleTree>> children = ruleTree.getChildren();
			// switch by the currently executed Term
			boolean isElsePartCreated = false;
			int currentChild = 0;
			for (Set<RuleTree> child : children) {
				if (!child.isEmpty()) {
					if (!isElsePartCreated) {
						isElsePartCreated = true;
						bw.write(" else {\n");
						// the current Rule has further children
						bw.write("\t\t\tswitch(currentPos - 1) {\n");
					}
					String newMethodName = methodName + "_Term" + currentChild;
					methods.add(new ExecuteTermCreator(ruleTree, newMethodName,
							child));
					bw.write("\t\t\tcase " + currentChild + ":\n");
					bw.write("\t\t\t\t" + newMethodName
							+ "(currentElement, parent);\n");
					bw.write("\t\t\t\tbreak;\n");
				}
				currentChild++;
			}
			if (isElsePartCreated) {
				bw.write("\t\t\t}\n");
				bw.write("\t\t}\n");
			} else {
				// close if
				bw.write("\n");
			}
			bw.write("\t}\n");

			for (MethodCreator method : methods) {
				method.generateCode(bw);
			}
		}

	}

	static class ExecuteTermCreator extends MethodCreator {

		private final Set<RuleTree> possibleRulesForTerm;

		public ExecuteTermCreator(RuleTree rt, String mName, Set<RuleTree> rules) {
			super(rt, mName);
			possibleRulesForTerm = rules;
		}

		@Override
		public void generateCode(BufferedWriter bw) throws IOException {
			bw.write("\n");
			generateJavaDoc(bw);
			bw.write("\tprivate void " + methodName
					+ "(StackElement currentElement, StackElement parent) {\n");
			// find next parent by skipping all rules which
			// cannot execute any semantic action
			bw.write("\t\tStackElement nextParent = skipElementsWithNoSemanticAction(parent, currentElement);\n");
			bw.write("\t\tswitch(nextParent.getAppliedRule().getNumber()) {\n");

			List<MethodCreator> methods = new ArrayList<MethodCreator>();
			for (RuleTree rt : possibleRulesForTerm) {
				int currentRuleNumber = rt.rule.getNumber();
				String newMethodName = methodName + "_Rule" + currentRuleNumber;
				methods.add(new ExecuteRuleCreator(rt, newMethodName));

				bw.write("\t\tcase " + currentRuleNumber + ":\n");
				bw.write("\t\t\t// " + rt.rule + "\n");
				bw.write("\t\t\t" + newMethodName
						+ "(currentElement, nextParent);\n");
				bw.write("\t\t\tbreak;\n");
			}

			bw.write("\t\t}\n");
			bw.write("\t}\n");

			for (MethodCreator method : methods) {
				method.generateCode(bw);
			}
		}

	}

	static class ExecutePositionCreator extends MethodCreator {

		public ExecutePositionCreator(RuleTree rt, String mName) {
			super(rt, mName);
		}

		@Override
		public void generateCode(BufferedWriter bw) throws IOException {
			bw.write("\n");
			generateJavaDoc(bw);
			bw.write("\tprotected void " + methodName
					+ "(StackElement currentElement) {\n");
			bw.write("\t}\n");
		}
	}

	private final Map<String, Set<Rule>> rulesWithHead = new HashMap<String, Set<Rule>>();

	private final List<RuleTree> definedRules = new ArrayList<RuleTree>();

	public void generateCode(Rule[] rules) {
		sortRulesByHead(rules);
		for (Rule rule : rules) {
			if ((rule.getType() == RuleType.DEFINED || rule.getType() == RuleType.START)
					&& !isARejectRule(rule)) {
				definedRules.add(buildRuleTree(new RuleTree(rule)));
			}
		}
		// for (RuleTree rt : definedRules) {
		// System.out.println(rt);
		// }

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(PATH
					+ NAME_OF_GENERATED_CLASS + ".java"));
			generateHead(bw);
			generateBody(bw);
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

	private boolean isARejectRule(Rule rule) {
		IStrategoTerm attribute = rule.getRule().getSubterm(2);
		IStrategoTerm[] children = attribute.getAllSubterms();
		if (children.length > 0) {
			// attributes are present
			// attribute = attrs(...)
			attribute = children[0];
			// attribute = [...]
			children = attribute.getAllSubterms();
			if (children.length == 0) {
				return false;
			}
			for (IStrategoTerm child : children) {
				StrategoAppl term = (StrategoAppl) child;
				if (term.getName().equals("reject")) {
					return true;
				}
			}
		}

		return false;
	}

	private void sortRulesByHead(Rule[] rules) {
		for (Rule rule : rules) {
			StringBuilder head = new StringBuilder();
			try {
				rule.getRule().getSubterm(1)
						.writeAsString(head, IStrategoTerm.INFINITE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String headString = head.toString();
			Set<Rule> ruleWithHead = rulesWithHead.get(headString);
			if (ruleWithHead == null) {
				ruleWithHead = new HashSet<Rule>();
				rulesWithHead.put(headString, ruleWithHead);
			}
			ruleWithHead.add(rule);
		}
	}

	private RuleTree buildRuleTree(RuleTree rt) {
		IStrategoTerm[] termsOfBody = rt.rule.getRule().getSubterm(0)
				.getAllSubterms();
		for (IStrategoTerm term : termsOfBody) {
			StringBuilder termSB = new StringBuilder();
			try {
				term.writeAsString(termSB, IStrategoTerm.INFINITE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String termString = termSB.toString();
			Set<RuleTree> ruleTrees = new HashSet<RuleTree>();
			if (termString.startsWith("cf") || termString.startsWith("lex")) {
				Set<Rule> childRules = rulesWithHead.get(termString);
				if (childRules != null) {
					for (Rule r : childRules) {
						RuleTree newRuleTree = new RuleTree(r);
						newRuleTree.parent = rt;
						if (!rt.hasHaedInPath(r)
								&& r.getType() != RuleType.DEFINED
								&& r.getType() != RuleType.WHITESPACE
								&& r.getType() != RuleType.EPSILON2STAR) {
							if (r.getType() == RuleType.EPSILON) {
								// -> cf(T?) can be skipped
								StringBuilder sb = new StringBuilder();
								try {
									r.getRule()
											.getSubterm(1)
											.writeAsString(sb,
													IStrategoTerm.INFINITE);
								} catch (IOException e) {
									e.printStackTrace();
								}
								String contentString = sb.toString();
								if (contentString.startsWith("cf(opt(")
										|| contentString.startsWith("lex(opt(")) {
									continue;
								}
							}
							buildRuleTree(newRuleTree);
							if (r.getType() == RuleType.LEX2CF
									|| r.getType() == RuleType.EPSILON2STAR
									|| r.getType() == RuleType.PLUS2STAR
									|| r.getType() == RuleType.REPETITION) {
								// this elements do not have any semantic
								// actions
								for (Set<RuleTree> childSet : newRuleTree
										.getChildren()) {
									ruleTrees.addAll(childSet);
								}
							} else {
								ruleTrees.add(newRuleTree);
							}
						}
					}
				}
			}
			rt.addChild(ruleTrees);
		}
		return rt;
	}

	private void generateHead(BufferedWriter bw) throws IOException {
		bw.write("package de.uni_koblenz.edl.preprocessor.edl2edlgraph;\n");
		bw.write("\n");
		bw.write("import de.uni_koblenz.edl.GraphBuilderBaseImpl;\n");
		bw.write("import de.uni_koblenz.edl.parser.stack.Stack;\n");
		bw.write("import de.uni_koblenz.edl.parser.stack.elements.StackElement;\n");
		bw.write("import de.uni_koblenz.jgralab.Graph;\n");
		bw.write("import de.uni_koblenz.jgralab.schema.Schema;\n");
		bw.write("\n");
	}

	private void generateBody(BufferedWriter bw) throws IOException {
		bw.write("public abstract class " + NAME_OF_GENERATED_CLASS
				+ " extends GraphBuilderBaseImpl {\n");
		bw.write("\n");
		createConstructors(bw);
		createExecuteMethods(bw);
		bw.write("}");
	}

	private void createConstructors(BufferedWriter bw) throws IOException {
		bw.write("\n");
		bw.write("\tpublic " + NAME_OF_GENERATED_CLASS
				+ "(String parseTable, Graph graph) {\n");
		bw.write("\t\tsuper(parseTable, graph);\n");
		bw.write("\t}\n");
		bw.write("\n");
		bw.write("\tpublic " + NAME_OF_GENERATED_CLASS
				+ "(String parseTable, Schema schema) {\n");
		bw.write("\t\tsuper(parseTable, schema);\n");
		bw.write("\t}\n");
	}

	private void createExecuteMethods(BufferedWriter bw) throws IOException {
		List<MethodCreator> methods = new ArrayList<MethodCreator>();
		bw.write("\n");
		bw.write("\t@Override\n");
		bw.write("\tpublic void execute(Stack stack) {\n");
		bw.write("\t\tStackElement currentElement = stack.getCurrentElement();\n");
		bw.write("\t\tassert currentElement.getAppliedRule() != null : \"There must not be called\"\n");
		bw.write("\t\t\t\t+ \" any semantic actions for leafs which recognize an input character.\";\n");
		bw.write("\t\tStackElement parent = currentElement.getParentApplicationOfDefinedRule();\n");
		bw.write("\t\tswitch(parent.getAppliedRule().getNumber()) {\n");
		for (RuleTree rt : definedRules) {
			if (rt.rule.toString().contains(" -> cf(Keyword) {")) {
				// These rules are not part of a parse forest
				continue;
			}
			int ruleNumber = rt.rule.getNumber();
			bw.write("\t\tcase " + ruleNumber + ":\n");
			String methodName = "execute_Rule" + ruleNumber;
			methods.add(new ExecuteRuleCreator(rt, methodName));
			bw.write("\t\t\t// "
					+ (rt.rule.isContextFree() ? "cf: " : "lex: ")
					+ (rt.rule.getType() == RuleType.DEFINED ? rt.rule
							.getDefinedRepresentation() : rt.rule.toString())
					+ "\n");
			bw.write("\t\t\t" + methodName + "(currentElement, parent);\n");
			bw.write("\t\t\tbreak;\n");
		}
		bw.write("\t\t}\n");
		bw.write("\t}\n");

		for (MethodCreator mc : methods) {
			bw.write("\n");
			bw.write("\t/*\n");
			bw.write("\t * Rule " + mc.ruleTree.rule.getNumber() + ": "
					+ mc.ruleTree.rule + "\n");
			bw.write("\t */\n");
			mc.generateCode(bw);
		}
	}
}
