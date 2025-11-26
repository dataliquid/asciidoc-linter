package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration for literal blocks in AsciiDoc. Literal blocks are delimited by
 * .... and display preformatted text without syntax highlighting.
 * <p>
 * Example usage:
 *
 * <pre>
 * ....
 * server:
 *   host: localhost
 *   port: 8080
 *   timeout: 30s
 * ....
 * </pre>
 * <p>
 * Validation is based on the YAML schema configuration for literal blocks.
 */
@JsonDeserialize
public final class LiteralBlock extends AbstractBlock {
    private static final String SEVERITY = "severity";
    private final TitleConfig title;
    private final LinesConfig lines;
    private final IndentationConfig indentation;

    @JsonCreator
    public LiteralBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("title") TitleConfig title, @JsonProperty("lines") LinesConfig lines,
            @JsonProperty("indentation") IndentationConfig indentation) {
        super(name, severity, occurrence, order);
        this.title = title;
        this.lines = lines;
        this.indentation = indentation;
    }

    @Override
    public BlockType getType() {
        return BlockType.LITERAL;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public LinesConfig getLines() {
        return lines;
    }

    public IndentationConfig getIndentation() {
        return indentation;
    }

    @JsonDeserialize
    public static class TitleConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        @JsonCreator
        public TitleConfig(@JsonProperty("required") boolean required, @JsonProperty("minLength") Integer minLength,
                @JsonProperty("maxLength") Integer maxLength, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TitleConfig that))
                return false;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }
    }

    @JsonDeserialize
    public static class LinesConfig {
        private final Integer min;
        private final Integer max;
        private final Severity severity;

        @JsonCreator
        public LinesConfig(@JsonProperty("min") Integer min, @JsonProperty("max") Integer max,
                @JsonProperty(SEVERITY) Severity severity) {
            this.min = min;
            this.max = max;
            this.severity = severity;
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof LinesConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }

    @JsonDeserialize
    public static class IndentationConfig {
        private final boolean required;
        private final boolean consistent;
        private final Integer minSpaces;
        private final Integer maxSpaces;
        private final Severity severity;

        @JsonCreator
        public IndentationConfig(@JsonProperty("required") boolean required,
                @JsonProperty("consistent") boolean consistent, @JsonProperty("minSpaces") Integer minSpaces,
                @JsonProperty("maxSpaces") Integer maxSpaces, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.consistent = consistent;
            this.minSpaces = minSpaces;
            this.maxSpaces = maxSpaces;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isConsistent() {
            return consistent;
        }

        public Integer getMinSpaces() {
            return minSpaces;
        }

        public Integer getMaxSpaces() {
            return maxSpaces;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof IndentationConfig that))
                return false;
            return required == that.required && consistent == that.consistent
                    && Objects.equals(minSpaces, that.minSpaces) && Objects.equals(maxSpaces, that.maxSpaces)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, consistent, minSpaces, maxSpaces, severity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LiteralBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(title, that.title) && Objects.equals(lines, that.lines)
                && Objects.equals(indentation, that.indentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, lines, indentation);
    }
}
