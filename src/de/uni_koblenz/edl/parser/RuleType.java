package de.uni_koblenz.edl.parser;

/**
 * Defines the type of a rule.
 */
public enum RuleType {

	/**
	 * This type describes rules which are defined in an EDL grammar and do not
	 * have the type {@link #ONE_ELEMENT_SEQUENCE}.
	 */
	DEFINED,

	/**
	 * Rules of this type are the start rule to parse a whole file. They can be
	 * identified by the head <code>&lt;Start&gt;</code>.
	 */
	FILE_START,

	/**
	 * Rules of this type are used to start the parsing of the input. They can
	 * be identified by the head <code>&lt;START&gt;</code>.
	 */
	START,

	/**
	 * Rules of this type are generated to recognize the automatically added
	 * <code>cf(LAYOUT?)</code>: <code>
	 * <ul>
	 * <li>cf(LAYOUT) -&gt; cf(LAYOUT?)</li>
	 * <li>-&gt; cf(LAYOUT?)</li>
	 * <li>cf(LAYOUT) cf(LAYOUT) -&gt; cf(LAYOUT) {left}</li>
	 * </ul>
	 * </code>
	 */
	WHITESPACE,

	/**
	 * This type describes rules of the form:<br>
	 * <code>lex(V) -&gt; cf(V)</code><br>
	 * where <code>V</code> is a variable.
	 */
	LEX2CF,

	/**
	 * This type describes rule, which are epsilon productions but not part of
	 * {@link #WHITESPACE}. They can be identified via an empty body.
	 * Corresponding rules:<br>
	 * <ul>
	 * <li>-&gt; cf(T?)</li>
	 * <li>-&gt; lex(T?)</li>
	 * <li>-&gt; cf(())</li>
	 * <li>-&gt; lex(())</li>
	 * </ul>
	 */
	EPSILON,

	/**
	 * Rules of this type are generated for literals of the form
	 * <code>"a"</code> an <code>'a'</code>. They can be identified by the head
	 * <code>"a"</code> or <code>'a'</code>.
	 */
	LITERAL,

	/**
	 * Rules of this type are generated to reach the semantic of the option T?
	 * via BNF rules. They can be identified by the head <code>cf(T?)</code> or
	 * <code>lex(T?)</code> if the rule is not of type {@link #WHITESPACE} or
	 * {@link #LEX2CF}. Corresponding rules:<br>
	 * <ul>
	 * <li>cf(T) -&gt; cf(T?)</li>
	 * <li>lex(T) -&gt; lex(T?)</li>
	 * </ul>
	 */
	OPTION,

	/**
	 * Rules of this type are generated to reach the semantic of the sequence
	 * (T1 ... Tn) via BNF rules. They can be identified by the head
	 * <code>cf((T1 ... Tn))</code>, <code>lex((T1 ... Tn))</code>,
	 * <code>cf(())</code> or <code>lex(())</code> if the rule is not of type
	 * {@link #LEX2CF}. Corresponding rules:<br>
	 * <ul>
	 * <li>T1 cf(LAYOUT?) ... cf(LAYOUT?) Tn -&gt; cf((T1 ... Tn))</li>
	 * <li>T1 ... Tn -&gt; lex((T1 ... Tn))</li>
	 * </ul>
	 */
	SEQUENCE,

	/**
	 * Rules of this type are generated to reach the semantic of the alternative
	 * T1 | ... | Tn via BNF rules. They can be identified by the head
	 * <code>cf(T1 | ... | Tn)</code> or <code>lex(T1 | ... | Tn)</code> if the
	 * rule is not of type {@link #LEX2CF}. Corresponding rules:<br>
	 * <ul>
	 * <li>cf(T1) -&gt; cf(T1 | ... | Tn)</li>
	 * <li>...</li>
	 * <li>cf(Tn) -&gt; cf(T1 | ... | Tn)</li>
	 * <li>lex(T1) -&gt; lex(T1 | ... | Tn)</li>
	 * <li>...</li>
	 * <li>lex(Tn) -&gt; lex(T1 | ... | Tn)</li>
	 * </ul>
	 */
	ALTERNATIVE,

	/**
	 * This type describes rules which are generated out of Lists or Repetitions
	 * and are epsilon productions but not part of {@link #WHITESPACE}. They can
	 * be identified via an empty body and a head consisting of a star list or
	 * repetition. Corresponding rules:<br>
	 * <ul>
	 * <li>-&gt; cf(T*)</li>
	 * <li>-&gt; lex(T*)</li>
	 * <li>-&gt; cf({T1 T2}*)</li>
	 * <li>-&gt; lex({T1 T2}*)</li>
	 * </ul>
	 */
	EPSILON2STAR,

	/**
	 * This type describes rules which are generated out of Lists or Repetitions
	 * to get from a plus term T+ or {T1 T2}+ to a star term T* or {T1 T2}*.
	 * They can be identified by the head <code>cf(T*)</code>,
	 * <code>lex(T*)</code>, <code>cf({T1 T2}*)</code> or
	 * <code>lex({T1 T2}*)</code> and an body of length one which is different
	 * from {@link #LEX2CF}. Corresponding rules are:<br>
	 * <ul>
	 * <li>cf(T+) -&gt; cf(T*)</li>
	 * <li>lex(T+) -&gt; lex(T*)</li>
	 * <li>cf({T1 T2}+) -&gt; cf({T1 T2}*)</li>
	 * <li>lex({T1 T2}+) -&gt; lex({T1 T2}*)</li>
	 * </ul>
	 */
	PLUS2STAR,

	/**
	 * Rules of this type are generated to reach the semantic of the repetitions
	 * T* and T+ via BNF rules. The generated rule which is used to recognize a
	 * T has the type {@link #REPETITION_PROD}. They can be identified by the
	 * head <code>cf(T*)</code>, <code>cf(T+)</code>, <code>lex(T*)</code> or
	 * <code>lex(T+)</code> and a body of length two. Corresponding rules:<br>
	 * <ul>
	 * <li>cf(T*) cf(LAYOUT?) cf(T*) -&gt; cf(T*) {left}</li>
	 * <li>cf(T*) cf(LAYOUT?) cf(T+) -&gt; cf(T+)</li>
	 * <li>cf(T+) cf(LAYOUT?) cf(T*) -&gt; cf(T+)</li>
	 * <li>cf(T+) cf(LAYOUT?) cf(T+) -&gt; cf(T+) {left}</li>
	 * <li>lex(T*) lex(T*) -&gt; lex(T*) {left}</li>
	 * <li>lex(T*) lex(T+) -&gt; lex(T+)</li>
	 * <li>lex(T+) lex(T*) -&gt; lex(T+)</li>
	 * <li>lex(T+) lex(T+) -&gt; lex(T+) {left}</li>
	 * </ul>
	 */
	REPETITION,

	/**
	 * Rules of this type are generated to reach the semantic of the repetitions
	 * T* and T+ via BNF rules. The generated rule of this type is used to
	 * recognize a T. They can be identified by the head <code>cf(T+)</code> or
	 * <code>lex(T+)</code> and the body T. Corresponding rules:<br>
	 * <ul>
	 * <li>T -&gt; cf(T+)</li>
	 * <li>T -&gt; lex(T+)</li>
	 * </ul>
	 */
	REPETITION_PROD,

	/**
	 * Rules of this type are generated to reach the semantic of the repetitions
	 * {T1 T2}* and {T1 T2}+ via BNF rules. The generated rule which is used to
	 * recognize a T1 has the type {@link #LIST_PROD}. They can be identified by
	 * the head <code>cf({T1 T2}*)</code>, <code>cf({T1 T2}+)</code>,
	 * <code>lex({T1 T2}*)</code> or <code>lex({T1 T2}+)</code> and a body of
	 * length three. Corresponding rules:<br>
	 * <ul>
	 * <li>cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) -&gt; cf({T1
	 * T2}*) {left}</li>
	 * <li>cf({T1 T2}*) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) -&gt; cf({T1
	 * T2}+)</li>
	 * <li>cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}*) -&gt; cf({T1
	 * T2}+)</li>
	 * <li>cf({T1 T2}+) cf(LAYOUT?) T2 cf(LAYOUT?) cf({T1 T2}+) -&gt; cf({T1
	 * T2}+) {left}</li>
	 * <li>lex({T1 T2}*) T2 lex({T1 T2}*) -&gt; lex({T1 T2}*) {left}</li>
	 * <li>lex({T1 T2}*) T2 lex({T1 T2}+) -&gt; lex({T1 T2}+)</li>
	 * <li>lex({T1 T2}+) T2 lex({T1 T2}*) -&gt; lex({T1 T2}+)</li>
	 * <li>lex({T1 T2}+) T2 lex({T1 T2}+) -&gt; lex({T1 T2}+) {left}</li>
	 * </ul>
	 */
	LIST,

	/**
	 * Rules of this type are generated to reach the semantic of the repetitions
	 * {T1 T2}* and {T1 T2}+ via BNF rules. The generated rule of this type is
	 * used to recognize a T1. They can be identified by the head
	 * <code>cf({T1 T2}+)</code> or <code>lex({T1 T2}+)</code> and the body T1.
	 * Corresponding rules:<br>
	 * <ul>
	 * <li>T1 -&gt; cf({T1 T2}+)</li>
	 * <li>T1 -&gt; lex({T1 T2}+)</li>
	 * </ul>
	 */
	LIST_PROD,

	/**
	 * Rules of this type are generated to reach the semantic of the tuple
	 * &lt;T1, ..., Tn&gt; via BNF rules. They can be identified by the head
	 * <code>cf(&lt;T1, ..., Tn&gt;)</code> or
	 * <code>lex(&lt;T1, ..., Tn&gt;)</code> if the rule is not of type
	 * {@link #LEX2CF}. Corresponding rules:<br>
	 * <ul>
	 * <li>"&lt;" cf(LAYOUT?) T1 cf(LAYOUT?) "," cf(LAYOUT?) ... cf(LAYOUT?) ","
	 * cf(LAYOUT?) Tn cf(LAYOUT?) "&gt;" -&gt; cf(&lt;T1, ..., Tn&gt;)</li>
	 * <li>"&lt;" T1 "," ... "," Tn "&gt;" -&gt; lex(&lt;T1, ..., Tn&gt;)</li>
	 * </ul>
	 */
	TUPLE,

	/**
	 * Rules of this type are generated to reach the semantic of the function
	 * (T1 ... Tn => T) via BNF rules. They can be identified by the first
	 * element of the body <code>(T1 ... Tn => T)</code>.<br>
	 * <ul>
	 * <li>cf((T1 ... Tn => T)) cf(LAYOUT?) "(" cf(LAYOUT?) T1 cf(LAYOUT?) ...
	 * cf(LAYOUT?) Tn cf(LAYOUT?) ")" -&gt; T</li>
	 * <li>lex((T1 ... Tn => T)) "(" T1 ... Tn ")" -&gt; T</li>
	 * </ul>
	 */
	FUNCTION;
}
