package de.uni_koblenz.edl.parser;

import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoString;

import de.uni_koblenz.edl.RuleFormatException;

/**
 * This is the wrapper class for rules which the {@link TreeTraverser} receives.
 * It supports methods to extract the needed informations from the rule tree.
 */
public class Rule {

	/**
	 * Name of the <code>definedAs</code> attribute, which saves the syntax of a
	 * rule the way it was defined by the user in an EDL grammar.
	 */
	public static final String ATTRIBUTE_DEFINED_AS = "definedAs";

	/**
	 * This {@link IStrategoAppl} is the root node of the rule tree.
	 */
	protected final IStrategoTerm rule;

	/**
	 * This <code>int</code> value is the identifier number of {@link #rule}.
	 */
	private final int number;

	/**
	 * This is the {@link String} representation of {@link #rule}. It is
	 * generated only by the first call of {@link #toString()}.
	 */
	private String derivedRepresentation;

	/**
	 * This is the {@link String} representation of the head {@link #rule}. It
	 * is generated only by the first call of {@link #toString()} or the
	 * corresponding getter.
	 */
	private String headRepresentation;

	/**
	 * This is the {@link String} representation of {@link #rule} how it was
	 * defined in an EDL grammar.
	 */
	private String definedRepresentation;

	/**
	 * This is the {@link RuleType} of {@link #rule}. It is generated only by
	 * the first call of {@link #getType()}.
	 */
	private RuleType type;

	/**
	 * Tells whether the head of {@link #rule} is context-free
	 * <code>cf(...)</code>. In case of {@link RuleType#FUNCTION} the first
	 * element of the body must start with <code>cf(...)</code>.
	 */
	private boolean isContextFree;

	/**
	 * Is <code>true</code> if {@link #type} is {@link RuleType#EPSILON},
	 * {@link RuleType#EPSILON2STAR} or {@link #rule} is of following form:<br>
	 * -&gt; cf(LAYOUT?)
	 */
	private boolean isEpsilonRule;

	/**
	 * Is <code>true</code> iff this rule can lead to the execution of semantic
	 * actions
	 */
	private boolean canHaveSemanticAction = true;

	/**
	 * Creates a wrapper object for <code>rule</code>.
	 * 
	 * @param number
	 *            <code>int</code> the identifier of <code>rule</code>
	 * @param rule
	 *            {@link IStrategoTerm} which is the root of a rule tree
	 */
	public Rule(int number, IStrategoTerm rule) {
		this.rule = rule;
		this.number = number;
		assert rule.getSubtermCount() == 3 : "A rule must have exactly three children. Rule "
				+ number + ": " + toString();
		String nameOfHead = ((StrategoAppl) rule.getSubterm(1)).getName();
		if (nameOfHead.equals("lex")) {
			isContextFree = false;
		} else if (nameOfHead.equals("cf")) {
			isContextFree = true;
		} else if (getHeadRepresentation().contains("<START>")) {
			isContextFree = toString().startsWith("cf(");
		} else {
			// in case of function it can be identified via the first element
			isContextFree = false;
			if (rule.getSubterm(0).getSubtermCount() > 0) {
				checkTypeFunction(rule.getSubterm(0).getSubterm(0));
				if (type == RuleType.FUNCTION) {
					isContextFree = ((StrategoAppl) rule.getSubterm(0)
							.getSubterm(0)).getName().equals("cf");
				}
			}
		}
	}

	/**
	 * @return {@link IStrategoTerm} {@link #rule} which is the root of the rule
	 *         node
	 */
	public IStrategoTerm getRule() {
		return rule;
	}

	/**
	 * @return <code>int</code> {@link #number} which is the identifier of
	 *         {@link #rule}
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @return <code>boolean</code> <code>true</code> if the head of
	 *         {@link #rule} starts with <code>cf(...)</code>
	 */
	public boolean isContextFree() {
		return isContextFree;
	}

	/**
	 * @return <code>boolen</code> <code>true</code> iff {@link #type} is
	 *         {@link RuleType#EPSILON}, {@link RuleType#EPSILON2STAR} or
	 *         {@link #rule} is of following form:<br>
	 *         -&gt; cf(LAYOUT?)
	 */
	public boolean isEpsilonRule() {
		getType();
		return isEpsilonRule;
	}

	/**
	 * @return <code>boolean</code> <code>true</code> iff this rule can lead to
	 *         the execution of semantic actions
	 */
	public boolean canHaveSemanticAciton() {
		getType();
		return canHaveSemanticAction;
	}

	/**
	 * @return <code>int</code> the number of terms concatenated in the body of
	 *         {@link #rule}
	 */
	public int getNumberOfTermsInBody() {
		return rule.getSubterm(0).getSubtermCount();
	}

	/*
	 * Method to implement #getType()
	 */

	/**
	 * @return {@link RuleType} which is the type of {@link #rule}.
	 */
	public RuleType getType() {
		if (type != null) {
			return type;
		}
		// check for type DEFINED
		getDefinedRepresentation();

		if (type != null) {
			return type;
		}

		assert rule.getSubtermCount() == 3 : "A rule must have exactly three children. Rule "
				+ number + ": " + toString();
		StrategoAppl head = (StrategoAppl) rule.getSubterm(1);
		IStrategoTerm[] body = rule.getSubterm(0).getAllSubterms();
		assert head.getSubtermCount() == 1 : "The head "
				+ prettyPrintSymbol(head, " ")
				+ " must have exactly one child.";
		checkTypesStart_FileStart_Literal(head);
		if (type != null) {
			return type;
		}

		if (body.length == 0) {
			checkTypesWhitespace_Epsilon_Epsilon2Star(head);
		}
		if (type != null) {
			return type;
		}

		checkTypeFunction(body[0]);
		if (type != null) {
			return type;
		}

		if (body.length == 1 && body[0] instanceof StrategoAppl) {
			checkTypeLex2Cf(head, (StrategoAppl) body[0]);
			if (type != null) {
				return type;
			}
		}

		// all the missing types can be identified via a head cf(...) or
		// lex(...)

		boolean isContextfreeHead = head.getName().equals("cf");
		if (isContextfreeHead || head.getName().equals("lex")
				|| head.getName().equals("var")) {
			head = (StrategoAppl) head.getSubterm(0);
		}
		String nameOfHead = head.getName();

		if (nameOfHead.equals("opt")) {
			if (!isContextfreeHead) {
				// T -> lex(T?)
				type = RuleType.OPTION;
				return type;
			}
			IStrategoTerm childOfOpt = head.getSubterm(0);
			if (childOfOpt instanceof StrategoAppl
					&& ((StrategoAppl) childOfOpt).getName().equals("layout")) {
				// cf(LAYOUT) -> cf(LAYOUT?)
				type = RuleType.WHITESPACE;
				canHaveSemanticAction = false;
			} else {
				// T -> cf(T?)
				type = RuleType.OPTION;
			}
			return type;
		} else if (nameOfHead.equals("seq")) {
			// T1 ... Tn -> cf((T1 ... Tn))
			// T1 ... Tn -> lex((T1 ... Tn))
			type = RuleType.SEQUENCE;
			return type;
		} else if (nameOfHead.equals("alt")) {
			// Ti -> cf(T1|...|Tn)
			// Ti -> lex(T1|...|Tn)
			type = RuleType.ALTERNATIVE;
			return type;
		} else if (nameOfHead.equals("tuple")) {
			// "<" T1 "," ... "," Tn ">" -> cf(<T1, ..., Tn>)
			// "<" T1 "," ... "," Tn ">" -> lex(<T1, ..., Tn>)
			type = RuleType.TUPLE;
			return type;
		} else if (nameOfHead.startsWith("iter")) {
			if (body.length == 5) {
				// cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) ->
				// cf({T1 T2}*) {left}
				// cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) ->
				// cf({T1 T2}+)
				// cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) ->
				// cf({T1 T2}+)
				// cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) ->
				// cf({T1 T2}+) {left}
				type = RuleType.LIST;
				return type;
			} else if (body.length == 3) {
				if (nameOfHead.endsWith("-sep")) {
					// lex({T1 T2}*) T2 lex({T1 T2}*) -> lex({T1 T2}*) {left}
					// lex({T1 T2}*) T2 lex({T1 T2}+) -> lex({T1 T2}+)
					// lex({T1 T2}+) T2 lex({T1 T2}*) -> lex({T1 T2}+)
					// lex({T1 T2}+) T2 lex({T1 T2}+) -> lex({T1 T2}+) {left}
					type = RuleType.LIST;
					return type;
				} else {
					// cf(T*) cf(LAYOUT?) cf(T*) -> cf(T*) {left}
					// cf(T*) cf(LAYOUT?) cf(T+) -> cf(T+)
					// cf(T+) cf(LAYOUT?) cf(T*) -> cf(T+)
					// cf(T+) cf(LAYOUT?) cf(T+) -> cf(T+) {left}
					type = RuleType.REPETITION;
					canHaveSemanticAction = false;
					return type;
				}
			} else if (body.length == 2) {
				// lex(T*) lex(T*) -> lex(T*) {left}
				// lex(T*) lex(T+) -> lex(T+)
				// lex(T+) lex(T*) -> lex(T+)
				// lex(T+) lex(T+) -> lex(T+) {left}
				type = RuleType.REPETITION;
				canHaveSemanticAction = false;
				return type;
			} else if (body.length == 1) {
				if (nameOfHead.startsWith("iter-star")) {
					// cf(T+) -> cf(T*)
					// lex(T+) -> lex(T*)
					// cf({T1 T2}+) -> cf({T1 T2}*)
					// lex({T1 T2}+) -> lex({T1 T2}*)
					type = RuleType.PLUS2STAR;
					canHaveSemanticAction = false;
					return type;
				}
				if (nameOfHead.endsWith("-sep")) {
					// T1 -> cf({T1 T2}+)
					// T1 -> lex({T1 T2}+)
					type = RuleType.LIST_PROD;
					return type;
				} else {
					// T -> cf(T+)
					// T -> lex(T+)
					type = RuleType.REPETITION_PROD;
					return type;
				}
			}
		}

		if (nameOfHead.equals("layout") && body.length == 2
				&& body[0].equals(rule.getSubterm(1))
				&& body[1].equals(rule.getSubterm(1))) {
			// cf(LAYOUT) cf(LAYOUT) -> cf(LAYOUT) {left}
			type = RuleType.WHITESPACE;
			canHaveSemanticAction = false;
			return type;
		}

		if (type == null) {
			throw new RuleFormatException(
					"The rule has an unknown type. Corresponding rule:\n"
							+ toString());
		}

		return type;
	}

	/**
	 * If <code>head</code> matches cf(T) and <code>bodyElemnt</code> matches
	 * lex(T) then {@link #type} is set to {@link RuleType#LEX2CF}
	 * 
	 * @param head
	 *            {@link StrategoAppl} the head of {@link #rule}
	 * @param bodyElement
	 *            {@link StrategoAppl} the body of {@link #rule}
	 */
	private void checkTypeLex2Cf(StrategoAppl head, StrategoAppl bodyElement) {
		IStrategoTerm childOfHead = null;
		if (head.getName().equals("cf") || head.getName().equals("lex")
				|| head.getName().equals("var")) {
			childOfHead = head.getSubterm(0);
		} else {
			childOfHead = head;
		}
		IStrategoTerm childOfBody = bodyElement.getSubterm(0);
		if (head.getName().equals("cf") && bodyElement.getName().equals("lex")
				&& childOfHead.equals(childOfBody)) {
			// lex(T) -> cf(T)
			type = RuleType.LEX2CF;
			canHaveSemanticAction = false;
		}
	}

	/**
	 * If the first element of the body of {@link #rule} matches cf((T1 ... Tn
	 * => T)) or lex((T1 ... Tn => T)) than {@link #type} is set to
	 * {@link RuleType#FUNCTION}.
	 * 
	 * @param firstTermOfBody
	 *            {@link IStrategoTerm} which is the first element of the body
	 *            of {@link #rule}
	 */
	private void checkTypeFunction(IStrategoTerm firstTermOfBody) {
		if (firstTermOfBody.getSubtermCount() == 1
				&& firstTermOfBody.getSubterm(0) instanceof StrategoAppl) {
			StrategoAppl elem = (StrategoAppl) firstTermOfBody.getSubterm(0);
			if (elem.getName().equals("func")) {
				// cf((T1 ... Tn => T)) cf(LAYOUT?) "(" cf(LAYOUT?) T1
				// cf(LAYOUT?) ... cf(LAYOUT?) Tn cf(LAYOUT?) ")" -> T
				// lex((T1 ... Tn => T)) "(" T1 ... Tn ")" -> T
				type = RuleType.FUNCTION;
			}
		}
	}

	/**
	 * If the body of {@link #rule} is empty this method checks
	 * <code>head</code> to set {@link #type} to either
	 * {@link RuleType#EPSILON2STAR}, {@link RuleType#EPSILON} or
	 * {@link RuleType#WHITESPACE}. Futher more {@link #isEpsilonRule()} is
	 * updated.
	 * 
	 * @param head
	 *            {@link StrategoAppl} which is the head of {@link #rule}
	 */
	private void checkTypesWhitespace_Epsilon_Epsilon2Star(StrategoAppl head) {
		boolean isContextFreeHead = head.getName().equals("cf");
		assert head.getSubtermCount() == 1 : "The head of a rule with empty body must have exactly one child. "
				+ prettyPrintSymbol(head, " ");
		IStrategoTerm contentOfHead = null;
		if (isContextFreeHead || head.getName().equals("lex")
				|| head.getName().equals("var")) {
			contentOfHead = head.getSubterm(0);
		} else {
			contentOfHead = head;
		}
		if (contentOfHead instanceof StrategoAppl) {
			String currentName = ((StrategoAppl) contentOfHead).getName();
			if (currentName.equals("iter-star-sep")
					|| currentName.equals("iter-star")) {
				// -> cf(T*)
				// -> lex(T*)
				// -> cf({T1 T2}*)
				// -> lex({T1 T2}*)
				type = RuleType.EPSILON2STAR;
				isEpsilonRule = true;
				canHaveSemanticAction = false;
				return;
			} else if (currentName.equals("empty")) {
				// -> cf(())
				// -> lex(())
				type = RuleType.EPSILON;
				isEpsilonRule = true;
				return;
			} else if (currentName.equals("opt")) {
				if (!isContextFreeHead) {
					// -> lex(T?)
					type = RuleType.EPSILON;
					isEpsilonRule = true;
					canHaveSemanticAction = false;
					return;
				}
				assert contentOfHead.getSubtermCount() == 1 : "An Option may only have one child. "
						+ prettyPrintSymbol(head, " ");
				IStrategoAppl child = (IStrategoAppl) contentOfHead
						.getSubterm(0);
				if (child.getName().equals("layout")) {
					// -> cf(LAYOUT?)
					type = RuleType.WHITESPACE;
					isEpsilonRule = true;
					canHaveSemanticAction = false;
					return;
				} else {
					// -> cf(T?) with T != LAYOUT
					type = RuleType.EPSILON;
					isEpsilonRule = true;
					canHaveSemanticAction = false;
					return;
				}
			}
		}
	}

	/**
	 * Uses <code>head</code> to determine the {@link #type} of {@link #rule} in
	 * the case of {@link RuleType#FILE_START}, {@link RuleType#START} and
	 * {@link RuleType#LITERAL}.
	 * 
	 * @param head
	 *            {@link StrategoAppl} which is the head of {@link #rule}
	 */
	private void checkTypesStart_FileStart_Literal(StrategoAppl head) {
		if (head.getName().equals("lit") || head.getName().equals("cilit")) {
			// ... -> "T"
			// ... -> 'T'
			type = RuleType.LITERAL;
			canHaveSemanticAction = false;
			return;
		}
		IStrategoTerm contentOfHead = head.getSubterm(0);
		if (contentOfHead instanceof StrategoString) {
			String name = ((StrategoString) contentOfHead).stringValue();
			if (name.equals("<Start>")) {
				// <START> [\256] -> <Start>
				type = RuleType.FILE_START;
				canHaveSemanticAction = false;
			} else if (name.equals("<START>")) {
				// cf(LAYOUT?) cf(T) cf(LAYOUT?) -> <START>
				// lex(T) -> <START>
				type = RuleType.START;
			}
		}
	}

	/*
	 * The following methods are used for implementation of
	 * #getDefinedRepresentation()
	 */

	/**
	 * Extracts and returns the {@link String} representation of a rule like it
	 * is defined in an EDL grammar by the user. It is stored in the
	 * <code>definedAs</code> attribute. Returns <code>null</code> if no such
	 * attribute is found.
	 * 
	 * @see #handleAttribute(StrategoAppl)
	 * @return {@link String} which gives the rule like it was defined in the
	 *         EDL grammar
	 */
	public String getDefinedRepresentation() {
		if (definedRepresentation == null
				&& (type == null || type != RuleType.DEFINED)) {
			IStrategoTerm[] children = rule.getAllSubterms();
			assert children.length == 3 : "The rule\n" + toString()
					+ "\n must have three children, but it has "
					+ children.length + " one.";
			IStrategoTerm attribute = children[2];
			children = attribute.getAllSubterms();
			if (children.length > 0) {
				// attributes are present
				// attribute = attrs(...)
				assert children.length == 1 : "attributes expected but was: "
						+ prettyPrintParentAttribute(rule.getSubterm(2));
				attribute = children[0];
				// attribute = [...]
				children = attribute.getAllSubterms();
				assert children.length > 0 : "attributes expected but no attributes existed";
				for (IStrategoTerm child : children) {
					StrategoAppl term = (StrategoAppl) child;
					if (term.getName().equals("term")) {
						handleAttribute(term);
					}
				}
			}
		}
		return definedRepresentation;
	}

	/**
	 * Handles the user defined attribute <code>term</code> of the form
	 * <code>term(anAttribute)</code> or <code>term(anAttribute("..."))</code>.
	 * If the attribute <code>definedAs("...")</code> is found, then
	 * {@link #type} is set to {@link RuleType#DEFINED} and
	 * {@link #definedRepresentation} is set to the value of the attribute
	 * <code>definedAs</code>.<br>
	 * In the following cases an {@link RuleFormatException} is thrown:
	 * <ul>
	 * <li>two <code>definedAs</code> attributes are found</li>
	 * <li>a <code>definedAs</code> attribute has no argument</li>
	 * </ul>
	 * 
	 * @param term
	 *            {@link StrategoAppl} which represents an user defined
	 *            attribute
	 */
	private void handleAttribute(StrategoAppl term) {
		// term = term(...)
		IStrategoTerm[] allSubterms = term.getAllSubterms();
		assert allSubterms.length == 1 : "the attribute "
				+ prettyPrintAttribute(term) + " should have one child.";

		term = (StrategoAppl) allSubterms[0];
		String name = term.getName();
		if (name.equals(ATTRIBUTE_DEFINED_AS)) {
			// term = definedAs(...)
			if (type == RuleType.DEFINED) {
				// several definedAs attributes are defined
				throw new RuleFormatException(
						"There exists two definedAs(\"...\") attributes in rule "
								+ number
								+ ":\n"
								+ toString()
								+ "\nThis happened because the following two rules were semantically equivalent:\n"
								+ definedRepresentation
								+ "\n"
								+ ((StrategoString) term.getAllSubterms()[0])
										.stringValue().replace("\\\\", "\\"));
			}
			if (type == null) {
				type = RuleType.DEFINED;
				allSubterms = term.getAllSubterms();
				if (allSubterms.length != 1) {
					throw new RuleFormatException(
							"The attribute "
									+ term.getName()
									+ " should have exactly one agrument (definedAs(\"arg\")). Corresponding rule "
									+ number + ":\n" + toString());
				}
				definedRepresentation = ((StrategoString) allSubterms[0])
						.stringValue();
			}
		}
	}

	/*
	 * Methods to create a String representation of a rule tree
	 */

	@Override
	public String toString() {
		if (derivedRepresentation == null) {
			derivedRepresentation = prettyPrintRule(rule);
		}
		return derivedRepresentation;
	}

	/**
	 * @param position
	 *            <code>int</code> where <code>##</code> will be inserted in the
	 *            body
	 * @return {@link String} where &gt; and &lt; are replaced;
	 * @see #toString()
	 */
	public String toStringWithMarkedPosition(int position) {
		IStrategoTerm[] children = rule.getAllSubterms();
		assert children.length == 3;
		StringBuilder content = new StringBuilder();
		IStrategoTerm body = children[0];
		StringBuilder sb = new StringBuilder();
		try {
			body.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String con = sb.toString();
		assert con.equals("[...]") || con.equals("[]");
		IStrategoTerm[] bodyChildren = body.getAllSubterms();
		for (int i = 0; i <= bodyChildren.length; i++) {
			if (i == position) {
				content.append(" ## ");
			}
			if (i == bodyChildren.length) {
				break;
			}
			content.append(i == 0 ? "" : " ").append(
					prettyPrintSymbol(bodyChildren[i], " "));
		}
		content.append(" -> ");
		content.append(getHeadRepresentation());
		content.append(prettyPrintParentAttribute(children[2]));
		return content.toString().replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * @param term
	 *            <code>int</code> index of term which will be highlighted by
	 *            <code>&lt;u&gt;&lt;/u&gt;</code> in the body
	 * @return {@link String} where &gt; and &lt; are replaced;
	 * @see #toString()
	 */
	public String toStringWithMarkedTerm(int term) {
		IStrategoTerm[] children = rule.getAllSubterms();
		assert children.length == 3;
		StringBuilder content = new StringBuilder();
		IStrategoTerm body = children[0];
		StringBuilder sb = new StringBuilder();
		try {
			body.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String con = sb.toString();
		assert con.equals("[...]");
		IStrategoTerm[] bodyChildren = body.getAllSubterms();
		for (int i = 0; i < bodyChildren.length; i++) {
			if (i == term) {
				content.append(" <u>");
			}
			content.append(i == 0 ? "" : " ").append(
					prettyPrintSymbol(bodyChildren[i], " ")
							.replace("<", "&lt;").replace(">", "&gt;"));
			if (i == term) {
				content.append("</u> ");
			}
		}
		content.append(" -&gt; ");
		content.append(getHeadRepresentation().replace("<", "&lt;").replace(
				">", "&gt;"));
		content.append(prettyPrintParentAttribute(children[2]).replace("<",
				"&lt;").replace(">", "&gt;"));
		return content.toString();
	}

	/**
	 * @return {@link String} representation of the rule head
	 */
	public String getHeadRepresentation() {
		if (headRepresentation == null) {
			IStrategoTerm[] children = rule.getAllSubterms();
			headRepresentation = prettyPrintSymbol(children[1], " ");
		}
		return headRepresentation;
	}

	/**
	 * Generates the {@link String} representation of a rule given as a tree
	 * with root <code>term</code>.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which is the root of the rule tree
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintRule(IStrategoTerm term) {
		IStrategoTerm[] children = term.getAllSubterms();
		assert children.length == 3;
		StringBuilder content = new StringBuilder();
		content.append(prettyPrintSymbol(children[0], " "));
		content.append(" -> ");
		content.append(getHeadRepresentation());
		content.append(prettyPrintParentAttribute(children[2]));
		return content.toString();
	}

	/**
	 * Creates the {@link String} representation of the term tree
	 * <code>term</code>. If it is a list <code>delim</code> is used as
	 * separator.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which is a term tree
	 * @param delim
	 *            {@link String} which is the delimiter
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintSymbol(IStrategoTerm term, String delim) {
		StringBuilder sb = new StringBuilder();
		try {
			term.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = sb.toString();
		if (content.equals("[...]")) {
			return prettyPrintSymbols(term, delim);
		} else if (content.equals("[]")) {
			assert term.getSubtermCount() == 0;
			return "";
		} else if (content.equals("cf(...)")) {
			assert term.getSubtermCount() == 1;
			return "cf(" + prettyPrintSymbol(term.getSubterm(0), delim) + ")";
			// return "<" + prettyPrintSymbol(term.getSubterm(0)) + "-CF>";
		} else if (content.equals("lex(...)")) {
			assert term.getSubtermCount() == 1;
			return "lex(" + prettyPrintSymbol(term.getSubterm(0), delim) + ")";
			// return "<" + prettyPrintSymbol(term.getSubterm(0)) + "-LEX>";
		} else if (content.equals("var(...)")) {
			assert term.getSubtermCount() == 1;
			return "var(" + prettyPrintSymbol(term.getSubterm(0), delim) + ")";
			// return "<" + prettyPrintSymbol(term.getSubterm(0)) + "-VAR>";
		} else if (content.equals("opt(...)")) {
			assert term.getSubtermCount() == 1;
			boolean needsBrackets = needsBrackets(term.getSubterm(0));
			return (needsBrackets ? "(" : "")
					+ prettyPrintSymbol(term.getSubterm(0), delim)
					+ (needsBrackets ? ")" : "") + "?";
		} else if (content.equals("seq(...)")) {
			assert term.getSubtermCount() == 1;
			return "(" + prettyPrintSymbol(term.getSubterm(0), delim) + ")";
		} else if (content.equals("iter(...)")) {
			boolean needsBrackets = needsBrackets(term.getSubterm(0));
			assert term.getSubtermCount() == 1;
			return (needsBrackets ? "(" : "")
					+ prettyPrintSymbol(term.getSubterm(0), delim)
					+ (needsBrackets ? ")" : "") + "+";
		} else if (content.equals("iter-star(...)")) {
			boolean needsBrackets = needsBrackets(term.getSubterm(0));
			assert term.getSubtermCount() == 1;
			return (needsBrackets ? "(" : "")
					+ prettyPrintSymbol(term.getSubterm(0), delim)
					+ (needsBrackets ? ")" : "") + "*";
		} else if (content.equals("iter-sep(...)")) {
			assert term.getSubtermCount() == 2;
			return "{" + prettyPrintSymbol(term.getSubterm(0), delim) + " "
					+ prettyPrintSymbol(term.getSubterm(1), delim) + "}+";
		} else if (content.equals("iter-star-sep(...)")) {
			assert term.getSubtermCount() == 2;
			return "{" + prettyPrintSymbol(term.getSubterm(0), delim) + " "
					+ prettyPrintSymbol(term.getSubterm(1), delim) + "}*";
		} else if (content.equals("alt(...)")) {
			assert term.getSubtermCount() == 2;
			return prettyPrintSymbol(term.getSubterm(0), delim) + " | "
					+ prettyPrintSymbol(term.getSubterm(1), delim);
		} else if (content.equals("tuple(...)")) {
			assert term.getSubtermCount() == 2;
			return "<" + prettyPrintSymbol(term.getSubterm(0), delim) + ", "
					+ prettyPrintSymbols(term.getSubterm(1), ", ") + ">";
		} else if (content.equals("func(...)")) {
			assert term.getSubtermCount() >= 1;
			return prettyPrintFunction(term);
		} else if (content.equals("strategy(...)")) {
			assert term.getSubtermCount() == 2;
			return "(" + prettyPrintSymbol(term.getSubterm(0), delim) + " -> "
					+ prettyPrintSymbol(term.getSubterm(1), delim) + ")";
		} else if (content.equals("sort(...)")) {
			assert term.getSubtermCount() == 1;
			return prettyPrintSymbol(term.getSubterm(0), delim);
		} else if (content.equals("char-class(...)")) {
			assert term.getSubtermCount() == 1;
			String erg = "[" + prettyPrintSymbol(term.getSubterm(0), "") + "]";
			return erg;
		} else if (content.equals("range(...)")) {
			assert term.getSubtermCount() == 2;
			return prettyPrintSymbol(term.getSubterm(0), delim) + "-"
					+ prettyPrintSymbol(term.getSubterm(1), delim);
		} else if (content.equals("lit(...)")) {
			assert term.getSubtermCount() == 1;
			return "\"" + prettyPrintSymbol(term.getSubterm(0), delim) + "\"";
		} else if (content.equals("cilit(...)")) {
			assert term.getSubtermCount() == 1;
			return "'" + prettyPrintSymbol(term.getSubterm(0), delim) + "'";
		} else if (content.equals("layout")) {
			assert term.getSubtermCount() == 0;
			return "LAYOUT";
		} else if (content.equals("empty")) {
			assert term.getSubtermCount() == 0;
			return "()";
		} else {
			// a leaf was detected
			assert term.getSubtermCount() == 0;
			if (content.trim().matches("^\\d+$")) {
				// the content of the leaf is a decimal value
				// try to convert it in a readable String
				return quote((char) Integer.parseInt(content), delim.isEmpty());
			} else if (content.startsWith("\"") && content.endsWith("\"")) {
				// cut surrounding "" of
				return content.substring(1, content.length() - 1);
			} else {
				return content;
			}
		}
	}

	private boolean needsBrackets(IStrategoTerm term) {
		StringBuilder sb = new StringBuilder();
		try {
			term.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = sb.toString();
		return content.equals("alt(...)");
	}

	/**
	 * Creates a {@link String} representation of the term list
	 * <code>term</code>. <code>delim</code> is used as separator.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which represents a list of terms
	 * @param delim
	 *            {@link String} which is used as separator
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintSymbols(IStrategoTerm term, String delim) {
		StringBuilder content = new StringBuilder();
		String del = "";
		for (IStrategoTerm subTerm : term.getAllSubterms()) {
			content.append(del).append(prettyPrintSymbol(subTerm, delim));
			del = delim;
		}
		return content.toString();
	}

	/**
	 * Return <code>aChar</code> if it is printable. If not it is tried to quote
	 * <code>aChar</code> like <code>\n</code>. If both does not work, the
	 * decimal value like <code>\253</code> is returned.<br>
	 * Further more <code>isInCharacterClass</code> is <code>true</code>
	 * <code>_</code> and whitespace is quoted like <code>\ </code> too. This is
	 * required to reach a valid SDF representation.
	 * 
	 * @param aChar
	 *            <code>char</code> which might be quoted
	 * @param isInCharacterClass
	 *            <code>boolean</code> if <code>true</code> whitespace and
	 *            underscore are quoted
	 * @return {@link String} which is a valid more human readable SDF
	 *         representation of <code>aChar</code>
	 */
	private String quote(char aChar, boolean isInCharacterClass) {
		switch (aChar) {
		case '"':
			return "\\\"";
		case '\\':
			return "\\\\";
		case '\t':
			return "\\t";
		case '\r':
			return "\\r";
		case '\n':
			return "\\n";
		default:
			String content = String.valueOf(aChar);
			if (!content.matches("^[\\p{Print}]$")) {
				// aChar is not a printable character
				return "\\" + (int) aChar;
			}
			if (isInCharacterClass && content.matches("^[_\\W]$")) {
				// in a character-class whitespace and _ must be quoted
				content = "\\" + content;
			}
			return content;
		}
	}

	/**
	 * Creates the {@link String} representation of the function term
	 * <code>term</code>.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which is the tree representation of a
	 *            function term
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintFunction(IStrategoTerm term) {
		IStrategoTerm[] children = term.getAllSubterms();
		assert children.length == 2;
		StringBuilder content = new StringBuilder("(");

		IStrategoTerm body = children[0];
		if (body.getSubtermCount() > 0) {
			IStrategoTerm[] bodyelements = body.getAllSubterms();
			String delim = "";
			for (int i = 0; i < bodyelements.length; i++) {
				content.append(delim).append(
						prettyPrintSymbol(bodyelements[i], " "));
				delim = " ";
			}
		}
		content.append(" => ")
				.append(prettyPrintSymbol(children[children.length - 1], " "))
				.append(")");
		return content.toString();
	}

	/**
	 * This method starts the conversion of the attribute tree <code>term</code>
	 * . It is responsible for<br>
	 * <ul>
	 * <li>creating an empty {@link String} if no attributes are defined</li>
	 * <li>creating the surrounding <code>{}</code> if attributes are present</li>
	 * </ul>
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which is the root in tree representation
	 *            of all attributes of a rule
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintParentAttribute(IStrategoTerm term) {
		StringBuilder sb = new StringBuilder();
		try {
			term.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = sb.toString();
		if (content.equals("no-attrs")) {
			assert term.getSubtermCount() == 0;
			return "";
		} else if (content.equals("attrs(...)")) {
			assert term.getSubtermCount() == 1;
			return prettyPrintParentAttribute(term.getSubterm(0));
		} else {
			assert content.equals("[...]") : content;
			return " {" + prettyPrintAttributes(term) + "}";
		}
	}

	/**
	 * Creates a {@link String} in which the representation of all subterms of
	 * <code>term</code> are separated by commas.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which is part of the attributes tree and
	 *            represents a list
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintAttributes(IStrategoTerm term) {
		StringBuilder content = new StringBuilder();
		String del = "";
		for (IStrategoTerm subTerm : term.getAllSubterms()) {
			String attrString = prettyPrintAttribute(subTerm);
			if (attrString != null && !attrString.isEmpty()) {
				content.append(del).append(attrString);
				del = ", ";
			}
		}
		return content.toString();
	}

	/**
	 * Creates the {@link String} representation of the attribute
	 * <code>term</code>.
	 * 
	 * @param term
	 *            {@link IStrategoTerm} which represents an attribute
	 * @return {@link String} representation of <code>term</code>
	 */
	private String prettyPrintAttribute(IStrategoTerm term) {
		StringBuilder sb = new StringBuilder();
		try {
			term.writeAsString(sb, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = sb.toString();
		if (content.equals("[...]")) {
			return prettyPrintAttributes(term);
		} else if (content.equals("assoc(...)")) {
			assert term.getSubtermCount() == 1;
			return prettyPrintAttribute(term.getSubterm(0));
		} else if (content.equals("avoid") || content.equals("prefer")
				|| content.equals("reject") || content.equals("bracket")
				|| content.equals("left") || content.equals("right")
				|| content.equals("assoc") || content.equals("non-assoc")) {
			assert term.getSubtermCount() == 0;
			return content.toString();
		} else if (content.equals("term(...)")) {
			assert term.getSubtermCount() == 1;
			StringBuilder sb2 = new StringBuilder();
			try {
				term.getSubterm(0).writeAsString(sb2, IStrategoTerm.INFINITE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb2.toString();
		} else if (content.equals("id(...)")) {
			// this attribute is created by hiddens
			return null;
		} else {
			assert term.getSubtermCount() == 0;
			return content.toString();
		}
	}
}
