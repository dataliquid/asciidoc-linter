package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.CONSISTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.DESCRIPTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.NESTING_LEVEL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.TERMS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.DELIMITER_STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.ALLOWED_DELIMITERS;

/**
 * Configuration for definition list (dlist) blocks in AsciiDoc. Represents
 * lists with term-description pairs using :: delimiters.
 */
@JsonDeserialize
public final class DlistBlock extends AbstractBlock {
    @JsonProperty(TERMS)
    private final TermsConfig terms;
    @JsonProperty(DESCRIPTIONS)
    private final DescriptionsConfig descriptions;
    @JsonProperty(NESTING_LEVEL)
    private final NestingLevelConfig nestingLevel;
    @JsonProperty(DELIMITER_STYLE)
    private final DelimiterStyleConfig delimiterStyle;

    @JsonCreator
    public DlistBlock(@JsonProperty(NAME) String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence, @JsonProperty(ORDER) Integer order,
            @JsonProperty(TERMS) TermsConfig terms, @JsonProperty(DESCRIPTIONS) DescriptionsConfig descriptions,
            @JsonProperty(NESTING_LEVEL) NestingLevelConfig nestingLevel,
            @JsonProperty(DELIMITER_STYLE) DelimiterStyleConfig delimiterStyle) {
        super(name, severity, occurrence, order);
        this.terms = terms;
        this.descriptions = descriptions;
        this.nestingLevel = nestingLevel;
        this.delimiterStyle = delimiterStyle;
    }

    @Override
    public BlockType getType() {
        return BlockType.DLIST;
    }

    public TermsConfig getTerms() {
        return terms;
    }

    public DescriptionsConfig getDescriptions() {
        return descriptions;
    }

    public NestingLevelConfig getNestingLevel() {
        return nestingLevel;
    }

    public DelimiterStyleConfig getDelimiterStyle() {
        return delimiterStyle;
    }

    /**
     * Configuration for term validation.
     */
    @JsonDeserialize
    public static class TermsConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(PATTERN)
        private final String pattern;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public TermsConfig(@JsonProperty(MIN) Integer min, @JsonProperty(MAX) Integer max,
                @JsonProperty(PATTERN) String pattern, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(SEVERITY) Severity severity) {
            this.min = min;
            this.max = max;
            this.pattern = pattern;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public String getPattern() {
            return pattern;
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
            if (!(o instanceof TermsConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max)
                    && Objects.equals(pattern, that.pattern) && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, pattern, minLength, maxLength, severity);
        }
    }

    /**
     * Configuration for description validation.
     */
    @JsonDeserialize
    public static class DescriptionsConfig {
        @JsonProperty(REQUIRED)
        private final Boolean required;
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(PATTERN)
        private final String pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public DescriptionsConfig(@JsonProperty(REQUIRED) Boolean required, @JsonProperty(MIN) Integer min,
                @JsonProperty(MAX) Integer max, @JsonProperty(PATTERN) String pattern,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.min = min;
            this.max = max;
            this.pattern = pattern;
            this.severity = severity;
        }

        public Boolean getRequired() {
            return required;
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public String getPattern() {
            return pattern;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DescriptionsConfig that))
                return false;
            return Objects.equals(required, that.required) && Objects.equals(min, that.min)
                    && Objects.equals(max, that.max) && Objects.equals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, min, max, pattern, severity);
        }
    }

    /**
     * Configuration for nesting level validation.
     */
    @JsonDeserialize
    public static class NestingLevelConfig {
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public NestingLevelConfig(@JsonProperty(MAX) Integer max, @JsonProperty(SEVERITY) Severity severity) {
            this.max = max;
            this.severity = severity;
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
            if (!(o instanceof NestingLevelConfig that))
                return false;
            return Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(max, severity);
        }
    }

    /**
     * Configuration for delimiter style validation.
     */
    @JsonDeserialize
    public static class DelimiterStyleConfig {
        @JsonProperty(ALLOWED_DELIMITERS)
        private final String[] allowedDelimiters;
        @JsonProperty(CONSISTENT)
        private final Boolean consistent;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public DelimiterStyleConfig(@JsonProperty(ALLOWED_DELIMITERS) String[] allowedDelimiters,
                @JsonProperty(CONSISTENT) Boolean consistent, @JsonProperty(SEVERITY) Severity severity) {
            this.allowedDelimiters = allowedDelimiters != null ? allowedDelimiters.clone() : null;
            this.consistent = consistent;
            this.severity = severity;
        }

        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public String[] getAllowedDelimiters() {
            return allowedDelimiters;
        }

        public Boolean getConsistent() {
            return consistent;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DelimiterStyleConfig that))
                return false;
            return java.util.Arrays.equals(allowedDelimiters, that.allowedDelimiters)
                    && Objects.equals(consistent, that.consistent) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(consistent, severity);
            result = 31 * result + java.util.Arrays.hashCode(allowedDelimiters);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DlistBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(terms, that.terms) && Objects.equals(descriptions, that.descriptions)
                && Objects.equals(nestingLevel, that.nestingLevel)
                && Objects.equals(delimiterStyle, that.delimiterStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), terms, descriptions, nestingLevel, delimiterStyle);
    }
}
