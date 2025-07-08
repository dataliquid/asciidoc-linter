package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ImageBlock.Builder.class)
public final class ImageBlock extends AbstractBlock {
    @JsonProperty("url")
    private final UrlConfig url;
    @JsonProperty("height")
    private final DimensionConfig height;
    @JsonProperty("width")
    private final DimensionConfig width;
    @JsonProperty("alt")
    private final AltTextConfig alt;
    
    private ImageBlock(Builder builder) {
        super(builder);
        this.url = builder.url;
        this.height = builder.height;
        this.width = builder.width;
        this.alt = builder.alt;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.IMAGE;
    }
    
    public UrlConfig getUrl() {
        return url;
    }
    
    public DimensionConfig getHeight() {
        return height;
    }
    
    public DimensionConfig getWidth() {
        return width;
    }
    
    public AltTextConfig getAlt() {
        return alt;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = UrlConfig.UrlConfigBuilder.class)
    public static class UrlConfig {
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("required")
        private final boolean required;
        
        private UrlConfig(UrlConfigBuilder builder) {
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static UrlConfigBuilder builder() {
            return new UrlConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class UrlConfigBuilder {
            private Pattern pattern;
            private boolean required;
            
            public UrlConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public UrlConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public UrlConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public UrlConfig build() {
                return new UrlConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UrlConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        @JsonProperty("minValue")
        private final Integer minValue;
        @JsonProperty("maxValue")
        private final Integer maxValue;
        @JsonProperty("required")
        private final boolean required;
        
        private DimensionConfig(DimensionConfigBuilder builder) {
            this.minValue = builder.minValue;
            this.maxValue = builder.maxValue;
            this.required = builder.required;
        }
        
        public Integer getMinValue() {
            return minValue;
        }
        
        public Integer getMaxValue() {
            return maxValue;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DimensionConfigBuilder {
            private Integer minValue;
            private Integer maxValue;
            private boolean required;
            
            public DimensionConfigBuilder minValue(Integer minValue) {
                this.minValue = minValue;
                return this;
            }
            
            public DimensionConfigBuilder maxValue(Integer maxValue) {
                this.maxValue = maxValue;
                return this;
            }
            
            public DimensionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public DimensionConfig build() {
                return new DimensionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minValue, that.minValue) &&
                   Objects.equals(maxValue, that.maxValue);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minValue, maxValue, required);
        }
    }
    
    @JsonDeserialize(builder = AltTextConfig.AltTextConfigBuilder.class)
    public static class AltTextConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        
        private AltTextConfig(AltTextConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
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
        
        public static AltTextConfigBuilder builder() {
            return new AltTextConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AltTextConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            
            public AltTextConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AltTextConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AltTextConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AltTextConfig build() {
                return new AltTextConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AltTextConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlConfig url;
        private DimensionConfig height;
        private DimensionConfig width;
        private AltTextConfig alt;
        
        public Builder url(UrlConfig url) {
            this.url = url;
            return this;
        }
        
        public Builder height(DimensionConfig height) {
            this.height = height;
            return this;
        }
        
        public Builder width(DimensionConfig width) {
            this.width = width;
            return this;
        }
        
        public Builder alt(AltTextConfig alt) {
            this.alt = alt;
            return this;
        }
        
        @Override
        public ImageBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new ImageBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(url, that.url) &&
               Objects.equals(height, that.height) &&
               Objects.equals(width, that.width) &&
               Objects.equals(alt, that.alt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, height, width, alt);
    }
}