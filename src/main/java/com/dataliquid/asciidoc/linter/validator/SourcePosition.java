package com.dataliquid.asciidoc.linter.validator;

/**
 * Represents the position of an element in the source text. This class is used
 * to precisely locate elements (URLs, titles, content, etc.) within AsciiDoc
 * source files for accurate error reporting.
 * <p>
 * All positions use 1-based indexing (first column/line is 1, not 0).
 * </p>
 */
public class SourcePosition {
    /**
     * The starting column position (1-based).
     */
    public final int startColumn;

    /**
     * The ending column position (1-based, inclusive).
     */
    public final int endColumn;

    /**
     * The line number where the element is located (1-based).
     */
    public final int lineNumber;

    /**
     * Creates a new SourcePosition.
     *
     * @param startColumn the starting column (1-based)
     * @param endColumn   the ending column (1-based, inclusive)
     * @param lineNumber  the line number (1-based)
     */
    public SourcePosition(int startColumn, int endColumn, int lineNumber) {
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.lineNumber = lineNumber;
    }

    /**
     * Creates a SourcePosition for a single point (start and end are the same).
     *
     * @param  column     the column position (1-based)
     * @param  lineNumber the line number (1-based)
     *
     * @return            a new SourcePosition
     */
    public static SourcePosition point(int column, int lineNumber) {
        return new SourcePosition(column, column, lineNumber);
    }

    /**
     * Creates a SourcePosition spanning an entire line.
     *
     * @param  lineLength the length of the line
     * @param  lineNumber the line number (1-based)
     *
     * @return            a new SourcePosition
     */
    public static SourcePosition entireLine(int lineLength, int lineNumber) {
        return new SourcePosition(1, lineLength, lineNumber);
    }

    @Override
    public String toString() {
        return String.format("SourcePosition[line=%d, columns=%d-%d]", lineNumber, startColumn, endColumn);
    }
}
