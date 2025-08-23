package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.ATTRIBUTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.CITATION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for quote blocks. Based on the YAML schema structure for
 * validating AsciiDoc quote blocks. Syntax: [quote, Autor, Quelle] ____ Quote
 * content here ____
 */
@JsonDeserialize(builder = QuoteBlock.Builder.class)
public class QuoteBlock extends AbstractBlock {

    private final AttributionConfig attribution;
    private final CitationConfig citation;
    private final ContentConfig content;

    private QuoteBlock(Builder builder) {
        super(builder);
        this.attribution = builder._attribution;
        this.citation = builder._citation;
        this.content = builder._content;
    }

    @Override
    public BlockType getType() {
        return BlockType.QUOTE;
    }

    @JsonProperty(ATTRIBUTION)
    public AttributionConfig getAttribution() {
        return _attribution;
    }

    @JsonProperty(CITATION)
    public CitationConfig getCitation() {
        return _citation;
    }

    @JsonProperty(CONTENT)
    public ContentConfig getContent() {
        return _content;
    }

    /**
     * Configuration for quote attribution validation.
     */
    @JsonDeserialize(builder = AttributionConfig.Builder.class)
    public static class AttributionConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;

        private AttributionConfig(Builder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return _required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return _minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return _maxLength;
        }

        @JsonProperty(PATTERN)
        public Pattern getPattern() {
            return _pattern;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return _severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            AttributionConfig that = (AttributionConfig) o;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && patternEquals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, pattern != null ? pattern.pattern() : null, severity);
        }

        @SuppressWarnings("PMD.CompareObjectsWithEquals") // Pattern comparison optimization
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2)
                return true;
            if (p1 == null || p2 == null)
                return false;
            return p1.pattern().equals(p2.pattern());
        }

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(MIN_LENGTH)
            public Builder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            @JsonProperty(MAX_LENGTH)
            public Builder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            @JsonProperty(PATTERN)
            @SuppressWarnings("PMD.NullAssignment")
            public Builder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public AttributionConfig build() {
                return new AttributionConfig(this);
            }
        }
    }

    /**
     * Configuration for quote citation validation.
     */
    @JsonDeserialize(builder = CitationConfig.Builder.class)
    public static class CitationConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;

        private CitationConfig(Builder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return _required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return _minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return _maxLength;
        }

        @JsonProperty(PATTERN)
        public Pattern getPattern() {
            return _pattern;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return _severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CitationConfig that = (CitationConfig) o;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && patternEquals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, pattern != null ? pattern.pattern() : null, severity);
        }

        @SuppressWarnings("PMD.CompareObjectsWithEquals") // Pattern comparison optimization
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2)
                return true;
            if (p1 == null || p2 == null)
                return false;
            return p1.pattern().equals(p2.pattern());
        }

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(MIN_LENGTH)
            public Builder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            @JsonProperty(MAX_LENGTH)
            public Builder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            @JsonProperty(PATTERN)
            @SuppressWarnings("PMD.NullAssignment")
            public Builder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public CitationConfig build() {
                return new CitationConfig(this);
            }
        }
    }

    /**
     * Configuration for quote content validation.
     */
    @JsonDeserialize(builder = ContentConfig.Builder.class)
    public static class ContentConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final LinesConfig lines;

        private ContentConfig(Builder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.lines = builder._lines;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return _required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return _minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return _maxLength;
        }

        @JsonProperty(LINES)
        public LinesConfig getLines() {
            return _lines;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ContentConfig that = (ContentConfig) o;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && Objects.equals(lines, that.lines);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines);
        }

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private LinesConfig _lines;

            @JsonProperty(REQUIRED)
            public Builder required(boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(MIN_LENGTH)
            public Builder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            @JsonProperty(MAX_LENGTH)
            public Builder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            @JsonProperty(LINES)
            public Builder lines(LinesConfig lines) {
                this._lines = lines;
                return this;
            }

            public ContentConfig build() {
                return new ContentConfig(this);
            }
        }
    }

    /**
     * Configuration for content line count validation.
     */
    @JsonDeserialize(builder = LinesConfig.Builder.class)
    public static class LinesConfig {
        private final Integer min;
        private final Integer max;
        private final Severity severity;

        private LinesConfig(Builder builder) {
            this.min = builder._min;
            this.max = builder._max;
            this.severity = builder._severity;
        }

        @JsonProperty(MIN)
        public Integer getMin() {
            return _min;
        }

        @JsonProperty(MAX)
        public Integer getMax() {
            return _max;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return _severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LinesConfig that = (LinesConfig) o;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Integer _min;
            private Integer _max;
            private Severity _severity;

            @JsonProperty(MIN)
            public Builder min(Integer min) {
                this._min = min;
                return this;
            }

            @JsonProperty(MAX)
            public Builder max(Integer max) {
                this._max = max;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public LinesConfig build() {
                return new LinesConfig(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!super.equals(o))
            return false;
        QuoteBlock that = (QuoteBlock) o;
        return Objects.equals(attribution, that.attribution) && Objects.equals(citation, that.citation)
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attribution, citation, content);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBlock.AbstractBuilder<Builder> {
        private AttributionConfig _attribution;
        private CitationConfig _citation;
        private ContentConfig _content;

        @JsonProperty(ATTRIBUTION)
        public Builder attribution(AttributionConfig attribution) {
            this._attribution = attribution;
            return this;
        }

        @JsonProperty(CITATION)
        public Builder citation(CitationConfig citation) {
            this._citation = citation;
            return this;
        }

        @JsonProperty(CONTENT)
        public Builder content(ContentConfig content) {
            this._content = content;
            return this;
        }

        @Override
        public QuoteBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new QuoteBlock(this);
        }
    }
}
