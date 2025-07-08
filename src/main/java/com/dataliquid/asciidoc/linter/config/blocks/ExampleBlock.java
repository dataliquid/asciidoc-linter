package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for EXAMPLE blocks.
 * 
 * Validates example blocks with optional numbering and caption format.
 * Based on the YAML schema definition for the linter.
 */
@JsonDeserialize(builder = ExampleBlock.Builder.class)
public class ExampleBlock extends AbstractBlock {
    
    private final CaptionConfig caption;
    private final CollapsibleConfig collapsible;
    
    private ExampleBlock(Builder builder) {
        super(builder);
        this.caption = builder.caption;
        this.collapsible = builder.collapsible;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.EXAMPLE;
    }
    
    public CaptionConfig getCaption() {
        return caption;
    }
    
    public CollapsibleConfig getCollapsible() {
        return collapsible;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExampleBlock that = (ExampleBlock) o;
        return Objects.equals(caption, that.caption) &&
               Objects.equals(collapsible, that.collapsible);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), caption, collapsible);
    }
    
    @Override
    public String toString() {
        return "ExampleBlock{" +
                "name='" + getName() + '\'' +
                ", severity=" + getSeverity() +
                ", occurrence=" + getOccurrence() +
                ", caption=" + caption +
                ", collapsible=" + collapsible +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBlock.AbstractBuilder<Builder> {
        private CaptionConfig caption;
        private CollapsibleConfig collapsible;
        
        @JsonProperty("caption")
        public Builder caption(CaptionConfig caption) {
            this.caption = caption;
            return this;
        }
        
        @JsonProperty("collapsible")
        public Builder collapsible(CollapsibleConfig collapsible) {
            this.collapsible = collapsible;
            return this;
        }
        
        @Override
        public ExampleBlock build() {
            return new ExampleBlock(this);
        }
    }
    
    /**
     * Configuration for the caption of an example block.
     * Based on the YAML schema requiring specific format.
     */
    @JsonDeserialize(builder = CaptionConfig.Builder.class)
    public static class CaptionConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;
        
        private CaptionConfig(Builder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Pattern getPattern() {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CaptionConfig that = (CaptionConfig) o;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(), 
                                that.pattern == null ? null : that.pattern.pattern()) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, 
                              pattern == null ? null : pattern.pattern(), 
                              minLength, maxLength, severity);
        }
        
        @Override
        public String toString() {
            return "CaptionConfig{" +
                    "required=" + required +
                    ", pattern=" + (pattern == null ? null : pattern.pattern()) +
                    ", minLength=" + minLength +
                    ", maxLength=" + maxLength +
                    ", severity=" + severity +
                    '}';
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private boolean required = false;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            @JsonProperty("required")
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("pattern")
            public Builder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
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
            
            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }
    }
    
    /**
     * Configuration for the collapsible attribute of an example block.
     * Based on the YAML schema allowing true/false values.
     */
    @JsonDeserialize(builder = CollapsibleConfig.Builder.class)
    public static class CollapsibleConfig {
        private final boolean required;
        private final List<Boolean> allowed;
        private final Severity severity;
        
        private CollapsibleConfig(Builder builder) {
            this.required = builder.required;
            this.allowed = builder.allowed;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public List<Boolean> getAllowed() {
            return allowed;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CollapsibleConfig that = (CollapsibleConfig) o;
            return required == that.required &&
                   Objects.equals(allowed, that.allowed) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
        
        @Override
        public String toString() {
            return "CollapsibleConfig{" +
                    "required=" + required +
                    ", allowed=" + allowed +
                    ", severity=" + severity +
                    '}';
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private boolean required = false;
            private List<Boolean> allowed;
            private Severity severity;
            
            @JsonProperty("required")
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("allowed")
            public Builder allowed(List<Boolean> allowed) {
                this.allowed = allowed;
                return this;
            }
            
            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CollapsibleConfig build() {
                return new CollapsibleConfig(this);
            }
        }
    }
}