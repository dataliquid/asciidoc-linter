package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Literal.CONSISTENT;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Literal.INDENTATION;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Literal.MAX_SPACES;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Literal.MIN_SPACES;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for literal blocks in AsciiDoc.
 * Literal blocks are delimited by .... and display preformatted text without syntax highlighting.
 * 
 * <p>Example usage:
 * <pre>
 * ....
 * server:
 *   host: localhost
 *   port: 8080
 *   timeout: 30s
 * ....
 * </pre>
 * 
 * <p>Validation is based on the YAML schema configuration for literal blocks.
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
        this.title = builder.title;
        this.lines = builder.lines;
        this.indentation = builder.indentation;
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
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
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
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public TitleConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public TitleConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public TitleConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public TitleConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TitleConfig build() {
                return new TitleConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TitleConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
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
            this.min = builder.min;
            this.max = builder.max;
            this.severity = builder.severity;
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
            private Integer min;
            private Integer max;
            private Severity severity;
            
            public LinesConfigBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            public LinesConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public LinesConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public LinesConfig build() {
                return new LinesConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinesConfig that)) return false;
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
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
            this.required = builder.required;
            this.consistent = builder.consistent;
            this.minSpaces = builder.minSpaces;
            this.maxSpaces = builder.maxSpaces;
            this.severity = builder.severity;
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
            private boolean required;
            private boolean consistent;
            private Integer minSpaces;
            private Integer maxSpaces;
            private Severity severity;
            
            public IndentationConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public IndentationConfigBuilder consistent(boolean consistent) {
                this.consistent = consistent;
                return this;
            }
            
            public IndentationConfigBuilder minSpaces(Integer minSpaces) {
                this.minSpaces = minSpaces;
                return this;
            }
            
            public IndentationConfigBuilder maxSpaces(Integer maxSpaces) {
                this.maxSpaces = maxSpaces;
                return this;
            }
            
            public IndentationConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public IndentationConfig build() {
                return new IndentationConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IndentationConfig that)) return false;
            return required == that.required &&
                   consistent == that.consistent &&
                   Objects.equals(minSpaces, that.minSpaces) &&
                   Objects.equals(maxSpaces, that.maxSpaces) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, consistent, minSpaces, maxSpaces, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TitleConfig title;
        private LinesConfig lines;
        private IndentationConfig indentation;
        
        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }
        
        public Builder lines(LinesConfig lines) {
            this.lines = lines;
            return this;
        }
        
        public Builder indentation(IndentationConfig indentation) {
            this.indentation = indentation;
            return this;
        }
        
        @Override
        public LiteralBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new LiteralBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiteralBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(title, that.title) &&
               Objects.equals(lines, that.lines) &&
               Objects.equals(indentation, that.indentation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, lines, indentation);
    }
}