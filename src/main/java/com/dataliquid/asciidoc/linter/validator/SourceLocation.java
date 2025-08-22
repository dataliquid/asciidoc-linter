package com.dataliquid.asciidoc.linter.validator;

import java.util.Objects;

public final class SourceLocation {
    private final String filename;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    private final String sourceLine;

    private SourceLocation(Builder builder) {
        this.filename = Objects.requireNonNull(builder.filename,
                "[" + getClass().getName() + "] filename must not be null");
        this.startLine = builder.startLine;
        this.startColumn = builder.startColumn;
        this.endLine = builder.endLine;
        this.endColumn = builder.endColumn;
        this.sourceLine = builder.sourceLine;
    }

    public String getFilename() {
        return filename;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getSourceLine() {
        return sourceLine;
    }

    public boolean isMultiLine() {
        return startLine != endLine;
    }

    public String formatLocation() {
        if (isMultiLine()) {
            return String.format("%s:%d-%d", filename, startLine, endLine);
        } else if (startColumn > 0 && endColumn > 0 && startColumn != endColumn) {
            return String.format("%s:%d:%d-%d", filename, startLine, startColumn, endColumn);
        } else if (startColumn > 0) {
            return String.format("%s:%d:%d", filename, startLine, startColumn);
        } else {
            return String.format("%s:%d", filename, startLine);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SourceLocation that = (SourceLocation) o;
        return startLine == that.startLine && startColumn == that.startColumn && endLine == that.endLine
                && endColumn == that.endColumn && Objects.equals(filename, that.filename)
                && Objects.equals(sourceLine, that.sourceLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, startLine, startColumn, endLine, endColumn, sourceLine);
    }

    @Override
    public String toString() {
        return formatLocation();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String filename;
        private int startLine = 1;
        private int startColumn = 1;
        private int endLine = 1;
        private int endColumn = 1;
        private String sourceLine;

        private Builder() {
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder startLine(int startLine) {
            this.startLine = startLine;
            return this;
        }

        public Builder startColumn(int startColumn) {
            this.startColumn = startColumn;
            return this;
        }

        public Builder endLine(int endLine) {
            this.endLine = endLine;
            return this;
        }

        public Builder endColumn(int endColumn) {
            this.endColumn = endColumn;
            return this;
        }

        public Builder line(int line) {
            this.startLine = line;
            this.endLine = line;
            return this;
        }

        public Builder columns(int start, int end) {
            this.startColumn = start;
            this.endColumn = end;
            return this;
        }

        public Builder sourceLine(String sourceLine) {
            this.sourceLine = sourceLine;
            return this;
        }

        public Builder fromPosition(SourcePosition pos) {
            this.startLine = pos.lineNumber;
            this.endLine = pos.lineNumber;
            this.startColumn = pos.startColumn;
            this.endColumn = pos.endColumn;
            return this;
        }

        public SourceLocation build() {
            return new SourceLocation(this);
        }
    }
}
