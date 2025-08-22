package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.validator.SourceLocation;

/**
 * Represents source code context around an error location.
 */
public final class SourceContext {
    private final List<ContextLine> lines;
    private final SourceLocation errorLocation;

    public SourceContext(List<String> fileLines, int startLineNumber, SourceLocation errorLocation) {
        this.errorLocation = Objects.requireNonNull(errorLocation,
                "[" + getClass().getName() + "] errorLocation must not be null");
        this.lines = new ArrayList<>();

        int lineNum = startLineNumber;
        for (String content : fileLines) {
            boolean isErrorLine = lineNum >= errorLocation.getStartLine() && lineNum <= errorLocation.getEndLine();
            lines.add(new ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
    }

    /**
     * Constructor for pre-built context lines.
     */
    public SourceContext(List<ContextLine> contextLines, SourceLocation originalLocation) {
        this.lines = new ArrayList<>(contextLines);
        this.errorLocation = Objects.requireNonNull(originalLocation,
                "[" + getClass().getName() + "] originalLocation must not be null");
    }

    public List<ContextLine> getLines() {
        return new ArrayList<>(lines);
    }

    public SourceLocation getErrorLocation() {
        return errorLocation;
    }

    /**
     * Represents a single line in the source context.
     */
    public static final class ContextLine {
        private final int number;
        private final String content;
        private final boolean errorLine;

        public ContextLine(int number, String content, boolean errorLine) {
            this.number = number;
            this.content = Objects.requireNonNull(content, "[" + getClass().getName() + "] content must not be null");
            this.errorLine = errorLine;
        }

        public int getNumber() {
            return number;
        }

        public String getContent() {
            return content;
        }

        public boolean isErrorLine() {
            return errorLine;
        }
    }
}
