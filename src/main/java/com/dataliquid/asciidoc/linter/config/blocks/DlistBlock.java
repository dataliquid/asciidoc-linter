package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for definition list (dlist) blocks in AsciiDoc.
 * Represents lists with term-description pairs using :: delimiters.
 */
@JsonDeserialize(builder = DlistBlock.Builder.class)
public final class DlistBlock extends AbstractBlock {
    @JsonProperty("terms")
    private final TermsConfig terms;
    @JsonProperty("descriptions")
    private final DescriptionsConfig descriptions;
    @JsonProperty("nestingLevel")
    private final NestingLevelConfig nestingLevel;
    @JsonProperty("delimiterStyle")
    private final DelimiterStyleConfig delimiterStyle;
    
    private DlistBlock(Builder builder) {
        super(builder);
        this.terms = builder.terms;
        this.descriptions = builder.descriptions;
        this.nestingLevel = builder.nestingLevel;
        this.delimiterStyle = builder.delimiterStyle;
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
        @JsonProperty("min")
        private final Integer min;
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("pattern")
        private final String pattern;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("severity")
        private final Severity severity;
        
        private TermsConfig(TermsConfigBuilder builder) {
            this.min = builder.min;
            this.max = builder.max;
            this.pattern = builder.pattern;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class TermsConfigBuilder {
            private Integer min;
            private Integer max;
            private String pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public TermsConfigBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            public TermsConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public TermsConfigBuilder pattern(String pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public TermsConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public TermsConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public TermsConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TermsConfig build() {
                return new TermsConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TermsConfig that)) return false;
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   Objects.equals(pattern, that.pattern) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
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
        @JsonProperty("required")
        private final Boolean required;
        @JsonProperty("min")
        private final Integer min;
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("pattern")
        private final String pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private DescriptionsConfig(DescriptionsConfigBuilder builder) {
            this.required = builder.required;
            this.min = builder.min;
            this.max = builder.max;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DescriptionsConfigBuilder {
            private Boolean required;
            private Integer min;
            private Integer max;
            private String pattern;
            private Severity severity;
            
            public DescriptionsConfigBuilder required(Boolean required) {
                this.required = required;
                return this;
            }
            
            public DescriptionsConfigBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            public DescriptionsConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public DescriptionsConfigBuilder pattern(String pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public DescriptionsConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public DescriptionsConfig build() {
                return new DescriptionsConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DescriptionsConfig that)) return false;
            return Objects.equals(required, that.required) &&
                   Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   Objects.equals(pattern, that.pattern) &&
                   severity == that.severity;
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
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("severity")
        private final Severity severity;
        
        private NestingLevelConfig(NestingLevelConfigBuilder builder) {
            this.max = builder.max;
            this.severity = builder.severity;
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class NestingLevelConfigBuilder {
            private Integer max;
            private Severity severity;
            
            public NestingLevelConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public NestingLevelConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public NestingLevelConfig build() {
                return new NestingLevelConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NestingLevelConfig that)) return false;
            return Objects.equals(max, that.max) &&
                   severity == that.severity;
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
        @JsonProperty("allowedDelimiters")
        private final String[] allowedDelimiters;
        @JsonProperty("consistent")
        private final Boolean consistent;
        @JsonProperty("severity")
        private final Severity severity;
        
        private DelimiterStyleConfig(DelimiterStyleConfigBuilder builder) {
            this.allowedDelimiters = builder.allowedDelimiters;
            this.consistent = builder.consistent;
            this.severity = builder.severity;
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DelimiterStyleConfigBuilder {
            private String[] allowedDelimiters;
            private Boolean consistent;
            private Severity severity;
            
            public DelimiterStyleConfigBuilder allowedDelimiters(String[] allowedDelimiters) {
                this.allowedDelimiters = allowedDelimiters;
                return this;
            }
            
            public DelimiterStyleConfigBuilder consistent(Boolean consistent) {
                this.consistent = consistent;
                return this;
            }
            
            public DelimiterStyleConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public DelimiterStyleConfig build() {
                return new DelimiterStyleConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DelimiterStyleConfig that)) return false;
            return java.util.Arrays.equals(allowedDelimiters, that.allowedDelimiters) &&
                   Objects.equals(consistent, that.consistent) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            int result = Objects.hash(consistent, severity);
            result = 31 * result + java.util.Arrays.hashCode(allowedDelimiters);
            return result;
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private TermsConfig terms;
        private DescriptionsConfig descriptions;
        private NestingLevelConfig nestingLevel;
        private DelimiterStyleConfig delimiterStyle;
        
        public Builder terms(TermsConfig terms) {
            this.terms = terms;
            return this;
        }
        
        public Builder descriptions(DescriptionsConfig descriptions) {
            this.descriptions = descriptions;
            return this;
        }
        
        public Builder nestingLevel(NestingLevelConfig nestingLevel) {
            this.nestingLevel = nestingLevel;
            return this;
        }
        
        public Builder delimiterStyle(DelimiterStyleConfig delimiterStyle) {
            this.delimiterStyle = delimiterStyle;
            return this;
        }
        
        @Override
        public DlistBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new DlistBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DlistBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(terms, that.terms) &&
               Objects.equals(descriptions, that.descriptions) &&
               Objects.equals(nestingLevel, that.nestingLevel) &&
               Objects.equals(delimiterStyle, that.delimiterStyle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), terms, descriptions, nestingLevel, delimiterStyle);
    }
}