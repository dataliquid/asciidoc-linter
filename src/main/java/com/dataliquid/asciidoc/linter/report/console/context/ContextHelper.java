package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;

/**
 * Helper class with common operations for context strategies.
 */
public final class ContextHelper {

    private ContextHelper() {
        // Utility class
    }

    /**
     * Checks if a string is empty or contains only whitespace. Returns false for
     * null strings to maintain compatibility.
     */
    public static boolean isEmptyOrWhitespace(String str) {
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a SourceContext with an inserted line marked as error.
     *
     * @param  contextLines  the context lines (with inserted line)
     * @param  startLine     the starting line number
     * @param  insertedIndex the index of the inserted line to mark as error
     * @param  loc           the source location
     *
     * @return               the source context
     */
    public static SourceContext createContextWithInsertedLine(List<String> contextLines, int startLine,
            int insertedIndex, SourceLocation loc) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = (i == insertedIndex);
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }

    /**
     * Creates a SourceContext for caption/title line insertions.
     *
     * @param  contextLines the context lines
     * @param  startLine    the starting line number
     * @param  loc          the source location
     *
     * @return              the source context
     */
    public static SourceContext createContextWithCaptionLine(List<String> contextLines, int startLine,
            SourceLocation loc) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;
        int videoLineIndex = loc.getStartLine() - startLine;

        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = (i == videoLineIndex && content.isEmpty());
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }

    /**
     * Creates a SourceContext with extra lines marked as error lines.
     *
     * @param  contextLines   the context lines
     * @param  startLine      the starting line number
     * @param  loc            the source location
     * @param  extraLineCount number of extra lines at the end to mark as error
     *
     * @return                the source context
     */
    public static SourceContext createContextWithExtraLines(List<String> contextLines, int startLine,
            SourceLocation loc, int extraLineCount) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;

        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = false;

            if (lineNum >= loc.getStartLine() && lineNum <= loc.getEndLine()) {
                isErrorLine = true;
            }

            if (i >= contextLines.size() - extraLineCount && content.isEmpty()) {
                isErrorLine = true;
            }

            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }

    /**
     * Finds a delimiter in context lines and inserts an empty line after it.
     *
     * @param  contextLines the context lines
     * @param  searchStart  the index to start searching from
     * @param  delimiter    the delimiter to find
     *
     * @return              the index where the line was inserted, or -1 if not
     *                      found
     */
    public static int insertAfterDelimiter(List<String> contextLines, int searchStart, String delimiter) {
        for (int i = searchStart; i < contextLines.size(); i++) {
            if (delimiter.equals(contextLines.get(i).trim())) {
                contextLines.add(i + 1, "");
                return i + 1;
            }
        }
        return -1;
    }
}
