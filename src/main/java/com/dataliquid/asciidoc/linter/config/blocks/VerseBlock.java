package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VerseBlock.Builder.class)
public final class VerseBlock extends AbstractBlock {
    @JsonProperty("author")
    private final AuthorConfig author;
    @JsonProperty("attribution")
    private final AttributionConfig attribution;
    @JsonProperty("content")
    private final ContentConfig content;
    
    private VerseBlock(Builder builder) {
        super(builder);
        this.author = builder.author;
        this.attribution = builder.attribution;
        this.content = builder.content;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.VERSE;
    }
    
    public AuthorConfig getAuthor() {
        return author;
    }
    
    public AttributionConfig getAttribution() {
        return attribution;
    }
    
    public ContentConfig getContent() {
        return content;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = AuthorConfig.AuthorConfigBuilder.class)
    public static class AuthorConfig {
        @JsonProperty("defaultValue")
        private final String defaultValue;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("required")
        private final boolean required;
        
        private AuthorConfig(AuthorConfigBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static AuthorConfigBuilder builder() {
            return new AuthorConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AuthorConfigBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public AuthorConfigBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            public AuthorConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AuthorConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AuthorConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public AuthorConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public AuthorConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AuthorConfig build() {
                return new AuthorConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AuthorConfig that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    @JsonDeserialize(builder = AttributionConfig.AttributionConfigBuilder.class)
    public static class AttributionConfig {
        @JsonProperty("defaultValue")
        private final String defaultValue;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("required")
        private final boolean required;
        
        private AttributionConfig(AttributionConfigBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static AttributionConfigBuilder builder() {
            return new AttributionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AttributionConfigBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public AttributionConfigBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            public AttributionConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AttributionConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AttributionConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public AttributionConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public AttributionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AttributionConfig build() {
                return new AttributionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("required")
        private final boolean required;
        
        private ContentConfig(ContentConfigBuilder builder) {
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class ContentConfigBuilder {
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public ContentConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public ContentConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public ContentConfig build() {
                return new ContentConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContentConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private AuthorConfig author;
        private AttributionConfig attribution;
        private ContentConfig content;
        
        public Builder author(AuthorConfig author) {
            this.author = author;
            return this;
        }
        
        public Builder attribution(AttributionConfig attribution) {
            this.attribution = attribution;
            return this;
        }
        
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        @Override
        public VerseBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new VerseBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerseBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(author, that.author) &&
               Objects.equals(attribution, that.attribution) &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, attribution, content);
    }
}