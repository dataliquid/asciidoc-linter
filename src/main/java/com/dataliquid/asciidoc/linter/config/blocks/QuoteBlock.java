package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.util.PatternUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration for quote blocks. Based on the YAML schema structure for
 * validating AsciiDoc quote blocks. Syntax: [quote, Autor, Quelle] ____ Quote
 * content here ____
 */
@JsonDeserialize
public class QuoteBlock extends AbstractBlock {

    private final AttributionConfig attribution;
    private final CitationConfig citation;
    private final ContentConfig content;

    @JsonCreator
    public QuoteBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty(ATTRIBUTION) AttributionConfig attribution, @JsonProperty(CITATION) CitationConfig citation,
            @JsonProperty(CONTENT) ContentConfig content) {
        super(name, severity, occurrence, order);
        this.attribution = attribution;
        this.citation = citation;
        this.content = content;
    }

    @Override
    public BlockType getType() {
        return BlockType.QUOTE;
    }

    @JsonProperty(ATTRIBUTION)
    public AttributionConfig getAttribution() {
        return attribution;
    }

    @JsonProperty(CITATION)
    public CitationConfig getCitation() {
        return citation;
    }

    @JsonProperty(CONTENT)
    public ContentConfig getContent() {
        return content;
    }

    /**
     * Configuration for quote attribution validation.
     */
    @JsonDeserialize
    public static class AttributionConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public AttributionConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return maxLength;
        }

        @JsonProperty(PATTERN)
        public Pattern getPattern() {
            return pattern;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            AttributionConfig that = (AttributionConfig) o;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && PatternUtils.patternEquals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, PatternUtils.patternHashCode(pattern), severity);
        }

    }

    /**
     * Configuration for quote citation validation.
     */
    @JsonDeserialize
    public static class CitationConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public CitationConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return maxLength;
        }

        @JsonProperty(PATTERN)
        public Pattern getPattern() {
            return pattern;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CitationConfig that = (CitationConfig) o;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && PatternUtils.patternEquals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, PatternUtils.patternHashCode(pattern), severity);
        }

    }

    /**
     * Configuration for quote content validation.
     */
    @JsonDeserialize
    public static class ContentConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final LinesConfig lines;

        @JsonCreator
        public ContentConfig(@JsonProperty("required") boolean required, @JsonProperty("minLength") Integer minLength,
                @JsonProperty("maxLength") Integer maxLength, @JsonProperty("lines") LinesConfig lines) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.lines = lines;
        }

        @JsonProperty(REQUIRED)
        public boolean isRequired() {
            return required;
        }

        @JsonProperty(MIN_LENGTH)
        public Integer getMinLength() {
            return minLength;
        }

        @JsonProperty(MAX_LENGTH)
        public Integer getMaxLength() {
            return maxLength;
        }

        @JsonProperty(LINES)
        public LinesConfig getLines() {
            return lines;
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

    }

    /**
     * Configuration for content line count validation.
     */
    @JsonDeserialize
    public static class LinesConfig {
        private final Integer min;
        private final Integer max;
        private final Severity severity;

        @JsonCreator
        public LinesConfig(@JsonProperty("min") Integer min, @JsonProperty("max") Integer max,
                @JsonProperty("severity") Severity severity) {
            this.min = min;
            this.max = max;
            this.severity = severity;
        }

        @JsonProperty(MIN)
        public Integer getMin() {
            return min;
        }

        @JsonProperty(MAX)
        public Integer getMax() {
            return max;
        }

        @JsonProperty(SEVERITY)
        public Severity getSeverity() {
            return severity;
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

}
