package de.uni_koblenz.edl.parser;

/**
 * Stores the position values, which are {@link #offset}, {@link #length},
 * {@link #firstLine}, {@link #lastLine}, {@link #firstColumn} and
 * {@link #lastColumn}. The characters can be identified via:<br>
 * <ul>
 * <li><code>new String(aCharArray,offset,length)</code></li>
 * <li>all chars from (line={@link #firstLine}, column={@link #firstColumn}) to
 * (line={@link #lastLine}, column={@link #lastColumn})</li>
 * </ul>
 * 
 * @see String#String(char[], int, int)
 */
public class Position {

	/**
	 * Stores the index &gt;=0 of the first character. If it is not set, it has
	 * the value <code>-1</code>.
	 */
	private int offset = -1;

	/**
	 * Stores the number of characters &gt;=0 . If it is not set, it has the
	 * value <code>-1</code>.
	 */
	private int length = -1;

	/**
	 * Stores in which line &gt;=1 the first character is. If it is not set, it
	 * has the value <code>-1</code>.
	 */
	private int firstLine = -1;

	/**
	 * Stores in which line &gt;=1 the last character is. If it is not set, it
	 * has the value <code>-1</code>.
	 */
	private int lastLine = -1;

	/**
	 * Stores in which column &gt;=0 the first character is. If it is not set,
	 * it has the value <code>-1</code>. A value of <code>0</code> tells that no
	 * character in front of a line is identified.
	 */
	private int firstColumn = -1;

	/**
	 * Stores in which column &gt;=0 the last character is. If it is not set, it
	 * has the value <code>-1</code>.
	 */
	private int lastColumn = -1;

	/**
	 * Creates a new {@link Position} object. All fields have the value
	 * <code>-1</code>.
	 */
	public Position() {

	}

	/**
	 * Creates a new {@link Position} object and sets:<br>
	 * <ul>
	 * <li>{@link #offset} = <code>offset</code></li>
	 * <li>{@link #length} = <code>length</code></li>
	 * <li>{@link #firstLine} = <code>line</code></li>
	 * <li>{@link #lastLine} = <code>line</code></li>
	 * <li>{@link #firstColumn} = <code>column</code></li>
	 * <li>{@link #lastColumn} = <code>column</code></li>
	 * </ul>
	 * 
	 * @param offset
	 *            <code>int</code> &gt;=0 which donates the first index of a
	 *            character
	 * @param length
	 *            <code>int</code> &gt;=0 which donates the number of characters
	 * @param line
	 *            <code>int</code> &gt;=1 which donates the line in which the
	 *            first character can be found
	 * @param column
	 *            <code>int</code> &gt;=0 which donates the column in which the
	 *            first character can be found
	 */
	public Position(int offset, int length, int line, int column) {
		setOffset(offset);
		setLength(length);
		setFirstLine(line);
		setLastLine(line);
		setFirstColumn(column);
		setLastColumn(column);
	}

	/**
	 * Creates a new {@link Position} object and sets:<br>
	 * <ul>
	 * <li>{@link #offset} = <code>offset</code></li>
	 * <li>{@link #length} = <code>length</code></li>
	 * <li>{@link #firstLine} = <code>firstLine</code></li>
	 * <li>{@link #lastLine} = <code>lastLine</code></li>
	 * <li>{@link #firstColumn} = <code>firstColumn</code></li>
	 * <li>{@link #lastColumn} = <code>lastColumn</code></li>
	 * </ul>
	 * 
	 * @param offset
	 *            <code>int</code> &gt;=0 which donates the first index of a
	 *            character
	 * @param length
	 *            <code>int</code> &gt;=0 which donates the number of characters
	 * @param firstLine
	 *            <code>int</code> &gt;=1 which donates the line in which the
	 *            first character can be found
	 * @param lastLine
	 *            <code>int</code> &gt;=1 which donates the line in which the
	 *            last character can be found
	 * @param firstColumn
	 *            <code>int</code> &gt;=0 which donates the column in which the
	 *            first character can be found
	 * @param lastColumn
	 *            <code>int</code> &gt;=0 which donates the line in which the
	 *            last character can be found
	 */
	public Position(int offset, int length, int firstLine, int lastLine,
			int firstColumn, int lastColumn) {
		this(offset, length, firstLine, firstColumn);
		setLastLine(lastLine);
		setLastColumn(lastColumn);
	}

	/**
	 * @return <code>int</code> which is the index of the first character. If
	 *         <code>-1</code> is returned, this value was not set.
	 * @see #offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the index of the first character.
	 * 
	 * @param offset
	 *            <code>int</code> &gt;=0
	 * @see #offset
	 */
	public void setOffset(int offset) {
		assert offset >= 0 : "offset must be >=0 but is: " + offset;
		this.offset = offset;
	}

	/**
	 * @return <code>int</code> which is the number of the identified character.
	 *         If <code>-1</code> is returned, this value was not set.
	 * @see #length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets the length of the identified characters.
	 * 
	 * @param length
	 *            <code>int</code> &gt;=0
	 * @see #length
	 */
	public void setLength(int length) {
		assert length >= 0 : "length must be >=0 but is: " + length;
		this.length = length;
	}

	/**
	 * @return <code>int</code> which is the line of the first character. If
	 *         <code>-1</code> is returned, this value was not set.
	 * @see #firstLine
	 */
	public int getFirstLine() {
		return firstLine;
	}

	/**
	 * Sets the line of the first character.
	 * 
	 * @param firstLine
	 *            <code>int</code> {@link #lastLine} &gt;=
	 *            <code>firstLine</code> &gt;=0
	 * @see #firstLine
	 */
	public void setFirstLine(int firstLine) {
		this.firstLine = firstLine;
	}

	/**
	 * @return <code>int</code> which is the line of the last character. If
	 *         <code>-1</code> is returned, this value was not set.
	 * @see #lastLine
	 */
	public int getLastLine() {
		return lastLine;
	}

	/**
	 * Sets the line of the last character.
	 * 
	 * @param lastLine
	 *            <code>int</code> <code>lastLine</code> &gt;=
	 *            {@link #firstLine} &gt;=0
	 * @see #lastLine
	 */
	public void setLastLine(int lastLine) {
		this.lastLine = lastLine;
	}

	/**
	 * @return <code>int</code> which is the column of the first character. If
	 *         <code>-1</code> is returned, this value was not set.
	 * @see #firstColumn
	 */
	public int getFirstColumn() {
		return firstColumn;
	}

	/**
	 * Sets the column of the first character.
	 * 
	 * @param firstColumn
	 *            <code>int</code> &gt;=0
	 * @see #firstColumn
	 */
	public void setFirstColumn(int firstColumn) {
		this.firstColumn = firstColumn;
	}

	/**
	 * @return <code>int</code> which is the column of the last character. If
	 *         <code>-1</code> is returned, this value was not set.
	 * @see #lastColumn
	 */
	public int getLastColumn() {
		return lastColumn;
	}

	/**
	 * Sets the column of the last character.
	 * 
	 * @param lastColumn
	 *            <code>int</code> &gt;=0
	 * @see #lastColumn
	 */
	public void setLastColumn(int lastColumn) {
		this.lastColumn = lastColumn;
	}

	@Override
	public String toString() {
		return "(offset: " + getOffset() + ", length: " + getLength()
				+ ", firstLine: " + getFirstLine() + ", lastLine: "
				+ getLastLine() + ", firstColumn: " + getFirstColumn()
				+ ", lastColumn: " + getLastColumn() + ")";
	}

}
