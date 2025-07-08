package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for quote blocks.
 * Based on the YAML schema structure for validating AsciiDoc quote blocks.
 * 
 * Syntax: [quote, Autor, Quelle]
 * ____
 * Quote content here
 * ____
 */
@JsonDeserialize(builder = QuoteBlock.Builder.class)
public class QuoteBlock extends AbstractBlock {
    
    private final AuthorConfig author;
    private final SourceConfig source;
    private final ContentConfig content;
    
    private QuoteBlock(Builder builder) {
        super(builder);
        this.author = builder.author;
        this.source = builder.source;
        this.content = builder.content;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.QUOTE;
    }
    
    @JsonProperty("author")
    public AuthorConfig getAuthor() {
        return author;
    }
    
    @JsonProperty("source")
    public SourceConfig getSource() {
        return source;
    }
    
    @JsonProperty("content")
    public ContentConfig getContent() {
        return content;
    }
    
    /**
     * Configuration for quote author validation.
     */
    @JsonDeserialize(builder = AuthorConfig.Builder.class)
    public static class AuthorConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;
        
        private AuthorConfig(Builder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthorConfig that = (AuthorConfig) o;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   patternEquals(pattern, that.pattern) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, 
                               pattern != null ? pattern.pattern() : null, severity);
        }
        
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2) return true;
            if (p1 == null || p2 == null) return false;
            return p1.pattern().equals(p2.pattern());
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private boolean required = false;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private Severity severity;
            
            @JsonProperty("required")
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("minLength")
            public Builder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public Builder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            @JsonProperty("pattern")
            public Builder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public AuthorConfig build() {
                return new AuthorConfig(this);
            }
        }
    }
    
    /**
     * Configuration for quote source validation.
     */
    @JsonDeserialize(builder = SourceConfig.Builder.class)
    public static class SourceConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;
        
        private SourceConfig(Builder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SourceConfig that = (SourceConfig) o;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   patternEquals(pattern, that.pattern) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, 
                               pattern != null ? pattern.pattern() : null, severity);
        }
        
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2) return true;
            if (p1 == null || p2 == null) return false;
            return p1.pattern().equals(p2.pattern());
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private boolean required = false;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private Severity severity;
            
            @JsonProperty("required")
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("minLength")
            public Builder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public Builder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            @JsonProperty("pattern")
            public Builder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public SourceConfig build() {
                return new SourceConfig(this);
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
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.lines = builder.lines;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("lines")
        public LinesConfig getLines() {
            return lines;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContentConfig that = (ContentConfig) o;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(lines, that.lines);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines);
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private boolean required = false;
            private Integer minLength;
            private Integer maxLength;
            private LinesConfig lines;
            
            @JsonProperty("required")
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("minLength")
            public Builder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public Builder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            @JsonProperty("lines")
            public Builder lines(LinesConfig lines) {
                this.lines = lines;
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
            this.min = builder.min;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        @JsonProperty("min")
        public Integer getMin() {
            return min;
        }
        
        @JsonProperty("max")
        public Integer getMax() {
            return max;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LinesConfig that = (LinesConfig) o;
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Integer min;
            private Integer max;
            private Severity severity;
            
            @JsonProperty("min")
            public Builder min(Integer min) {
                this.min = min;
                return this;
            }
            
            @JsonProperty("max")
            public Builder max(Integer max) {
                this.max = max;
                return this;
            }
            
            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public LinesConfig build() {
                return new LinesConfig(this);
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        QuoteBlock that = (QuoteBlock) o;
        return Objects.equals(author, that.author) &&
               Objects.equals(source, that.source) &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, source, content);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBlock.AbstractBuilder<Builder> {
        private AuthorConfig author;
        private SourceConfig source;
        private ContentConfig content;
        
        @JsonProperty("author")
        public Builder author(AuthorConfig author) {
            this.author = author;
            return this;
        }
        
        @JsonProperty("source")
        public Builder source(SourceConfig source) {
            this.source = source;
            return this;
        }
        
        @JsonProperty("content")
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        @Override
        public QuoteBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new QuoteBlock(this);
        }
    }
}