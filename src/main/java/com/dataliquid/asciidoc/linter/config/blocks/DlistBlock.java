package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Literal.CONSISTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.DESCRIPTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.NESTING_LEVEL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.TERMS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.DELIMITER_STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.ALLOWED_DELIMITERS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for definition list (dlist) blocks in AsciiDoc. Represents
 * lists with term-description pairs using :: delimiters.
 */
@JsonDeserialize(builder = DlistBlock.Builder.class)
public final class DlistBlock extends AbstractBlock {
    @JsonProperty(TERMS)
    private final TermsConfig terms;
    @JsonProperty(DESCRIPTIONS)
    private final DescriptionsConfig descriptions;
    @JsonProperty(NESTING_LEVEL)
    private final NestingLevelConfig nestingLevel;
    @JsonProperty(DELIMITER_STYLE)
    private final DelimiterStyleConfig delimiterStyle;

    private DlistBlock(Builder builder) {
        super(builder);
        this.terms = builder._terms;
        this.descriptions = builder._descriptions;
        this.nestingLevel = builder._nestingLevel;
        this.delimiterStyle = builder._delimiterStyle;
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Configuration for term validation.
     */
    @JsonDeserialize(builder = TermsConfig.TermsConfigBuilder.class)
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

        private TermsConfig(TermsConfigBuilder builder) {
            this.min = builder._min;
            this.max = builder._max;
            this.pattern = builder._pattern;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
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

        public static TermsConfigBuilder builder() {
            return new TermsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TermsConfigBuilder {
            private Integer _min;
            private Integer _max;
            private String _pattern;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            public TermsConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public TermsConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public TermsConfigBuilder pattern(String pattern) {
                this._pattern = pattern;
                return this;
            }

            public TermsConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public TermsConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public TermsConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public TermsConfig build() {
                return new TermsConfig(this);
            }
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
    @JsonDeserialize(builder = DescriptionsConfig.DescriptionsConfigBuilder.class)
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

        private DescriptionsConfig(DescriptionsConfigBuilder builder) {
            this.required = builder._required;
            this.min = builder._min;
            this.max = builder._max;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
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

        public static DescriptionsConfigBuilder builder() {
            return new DescriptionsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class DescriptionsConfigBuilder {
            private Boolean _required;
            private Integer _min;
            private Integer _max;
            private String _pattern;
            private Severity _severity;

            public DescriptionsConfigBuilder required(Boolean required) {
                this._required = required;
                return this;
            }

            public DescriptionsConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public DescriptionsConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public DescriptionsConfigBuilder pattern(String pattern) {
                this._pattern = pattern;
                return this;
            }

            public DescriptionsConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public DescriptionsConfig build() {
                return new DescriptionsConfig(this);
            }
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
    @JsonDeserialize(builder = NestingLevelConfig.NestingLevelConfigBuilder.class)
    public static class NestingLevelConfig {
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private NestingLevelConfig(NestingLevelConfigBuilder builder) {
            this.max = builder._max;
            this.severity = builder._severity;
        }

        public Integer getMax() {
            return max;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static NestingLevelConfigBuilder builder() {
            return new NestingLevelConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class NestingLevelConfigBuilder {
            private Integer _max;
            private Severity _severity;

            public NestingLevelConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public NestingLevelConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public NestingLevelConfig build() {
                return new NestingLevelConfig(this);
            }
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
    @JsonDeserialize(builder = DelimiterStyleConfig.DelimiterStyleConfigBuilder.class)
    public static class DelimiterStyleConfig {
        @JsonProperty(ALLOWED_DELIMITERS)
        private final String[] allowedDelimiters;
        @JsonProperty(CONSISTENT)
        private final Boolean consistent;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private DelimiterStyleConfig(DelimiterStyleConfigBuilder builder) {
            this.allowedDelimiters = builder._allowedDelimiters;
            this.consistent = builder._consistent;
            this.severity = builder._severity;
        }

        public String[] getAllowedDelimiters() {
            return allowedDelimiters;
        }

        public Boolean getConsistent() {
            return consistent;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static DelimiterStyleConfigBuilder builder() {
            return new DelimiterStyleConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class DelimiterStyleConfigBuilder {
            private String[] _allowedDelimiters;
            private Boolean _consistent;
            private Severity _severity;

            public DelimiterStyleConfigBuilder allowedDelimiters(String... allowedDelimiters) {
                this._allowedDelimiters = allowedDelimiters.clone(); // Defensive copy to avoid external modification
                return this;
            }

            public DelimiterStyleConfigBuilder consistent(Boolean consistent) {
                this._consistent = consistent;
                return this;
            }

            public DelimiterStyleConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public DelimiterStyleConfig build() {
                return new DelimiterStyleConfig(this);
            }
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TermsConfig _terms;
        private DescriptionsConfig _descriptions;
        private NestingLevelConfig _nestingLevel;
        private DelimiterStyleConfig _delimiterStyle;

        public Builder terms(TermsConfig terms) {
            this._terms = terms;
            return this;
        }

        public Builder descriptions(DescriptionsConfig descriptions) {
            this._descriptions = descriptions;
            return this;
        }

        public Builder nestingLevel(NestingLevelConfig nestingLevel) {
            this._nestingLevel = nestingLevel;
            return this;
        }

        public Builder delimiterStyle(DelimiterStyleConfig delimiterStyle) {
            this._delimiterStyle = delimiterStyle;
            return this;
        }

        @Override
        public DlistBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new DlistBlock(this);
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
