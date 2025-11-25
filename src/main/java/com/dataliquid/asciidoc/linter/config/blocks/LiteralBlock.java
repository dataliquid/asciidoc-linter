package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.CONSISTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.INDENTATION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.MAX_SPACES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.MIN_SPACES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

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
@JsonDeserialize(builder = LiteralBlock.Builder.class)
public final class LiteralBlock extends AbstractBlock {
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(LINES)
    private final LinesConfig lines;
    @JsonProperty(INDENTATION)
    private final IndentationConfig indentation;

    private LiteralBlock(Builder builder) {
        super(builder);
        this.title = builder._title;
        this.lines = builder._lines;
        this.indentation = builder._indentation;
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

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
    public static class TitleConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
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

        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TitleConfigBuilder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            public TitleConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public TitleConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public TitleConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public TitleConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public TitleConfig build() {
                return new TitleConfig(this);
            }
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

    @JsonDeserialize(builder = LinesConfig.LinesConfigBuilder.class)
    public static class LinesConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private LinesConfig(LinesConfigBuilder builder) {
            this.min = builder._min;
            this.max = builder._max;
            this.severity = builder._severity;
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

        public static LinesConfigBuilder builder() {
            return new LinesConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class LinesConfigBuilder {
            private Integer _min;
            private Integer _max;
            private Severity _severity;

            public LinesConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public LinesConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public LinesConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public LinesConfig build() {
                return new LinesConfig(this);
            }
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

    @JsonDeserialize(builder = IndentationConfig.IndentationConfigBuilder.class)
    public static class IndentationConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(CONSISTENT)
        private final boolean consistent;
        @JsonProperty(MIN_SPACES)
        private final Integer minSpaces;
        @JsonProperty(MAX_SPACES)
        private final Integer maxSpaces;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private IndentationConfig(IndentationConfigBuilder builder) {
            this.required = builder._required;
            this.consistent = builder._consistent;
            this.minSpaces = builder._minSpaces;
            this.maxSpaces = builder._maxSpaces;
            this.severity = builder._severity;
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

        public static IndentationConfigBuilder builder() {
            return new IndentationConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class IndentationConfigBuilder {
            private boolean _required;
            private boolean _consistent;
            private Integer _minSpaces;
            private Integer _maxSpaces;
            private Severity _severity;

            public IndentationConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public IndentationConfigBuilder consistent(boolean consistent) {
                this._consistent = consistent;
                return this;
            }

            public IndentationConfigBuilder minSpaces(Integer minSpaces) {
                this._minSpaces = minSpaces;
                return this;
            }

            public IndentationConfigBuilder maxSpaces(Integer maxSpaces) {
                this._maxSpaces = maxSpaces;
                return this;
            }

            public IndentationConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public IndentationConfig build() {
                return new IndentationConfig(this);
            }
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TitleConfig _title;
        private LinesConfig _lines;
        private IndentationConfig _indentation;

        public Builder title(TitleConfig title) {
            this._title = title;
            return this;
        }

        public Builder lines(LinesConfig lines) {
            this._lines = lines;
            return this;
        }

        public Builder indentation(IndentationConfig indentation) {
            this._indentation = indentation;
            return this;
        }

        @Override
        public LiteralBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new LiteralBlock(this);
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
