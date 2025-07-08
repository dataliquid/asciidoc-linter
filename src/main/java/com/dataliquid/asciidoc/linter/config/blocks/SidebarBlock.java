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
 * Configuration for sidebar blocks in AsciiDoc.
 * Sidebar blocks are used for supplementary information displayed alongside the main content.
 * 
 * <p>Example usage:
 * <pre>
 * ****
 * Sidebar content here
 * ****
 * </pre>
 * 
 * <p>Validation is based on the YAML schema configuration for sidebar blocks.
 */
@JsonDeserialize(builder = SidebarBlock.Builder.class)
public final class SidebarBlock extends AbstractBlock {
    @JsonProperty("title")
    private final TitleConfig title;
    @JsonProperty("content")
    private final ContentConfig content;
    @JsonProperty("position")
    private final PositionConfig position;
    
    private SidebarBlock(Builder builder) {
        super(builder);
        this.title = builder.title;
        this.content = builder.content;
        this.position = builder.position;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.SIDEBAR;
    }
    
    public TitleConfig getTitle() {
        return title;
    }
    
    public ContentConfig getContent() {
        return content;
    }
    
    public PositionConfig getPosition() {
        return position;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
    public static class TitleConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
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
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class TitleConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
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
            
            public TitleConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public TitleConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
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
                   Objects.equals(pattern != null ? pattern.pattern() : null, 
                                that.pattern != null ? that.pattern.pattern() : null) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, 
                              pattern != null ? pattern.pattern() : null, severity);
        }
    }
    
    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("lines")
        private final LinesConfig lines;
        
        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.lines = builder.lines;
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
        
        public LinesConfig getLines() {
            return lines;
        }
        
        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class ContentConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private LinesConfig lines;
            
            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public ContentConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentConfigBuilder lines(LinesConfig lines) {
                this.lines = lines;
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
                   Objects.equals(lines, that.lines);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines);
        }
    }
    
    @JsonDeserialize(builder = LinesConfig.LinesConfigBuilder.class)
    public static class LinesConfig {
        @JsonProperty("min")
        private final Integer min;
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("severity")
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
        
        @JsonPOJOBuilder(withPrefix = "")
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
    
    @JsonDeserialize(builder = PositionConfig.PositionConfigBuilder.class)
    public static class PositionConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("allowed")
        private final List<String> allowed;
        @JsonProperty("severity")
        private final Severity severity;
        
        private PositionConfig(PositionConfigBuilder builder) {
            this.required = builder.required;
            this.allowed = builder.allowed;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public List<String> getAllowed() {
            return allowed;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static PositionConfigBuilder builder() {
            return new PositionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class PositionConfigBuilder {
            private boolean required;
            private List<String> allowed;
            private Severity severity;
            
            public PositionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public PositionConfigBuilder allowed(List<String> allowed) {
                this.allowed = allowed;
                return this;
            }
            
            public PositionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public PositionConfig build() {
                return new PositionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PositionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(allowed, that.allowed) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private TitleConfig title;
        private ContentConfig content;
        private PositionConfig position;
        
        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }
        
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        public Builder position(PositionConfig position) {
            this.position = position;
            return this;
        }
        
        @Override
        public SidebarBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new SidebarBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SidebarBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(title, that.title) &&
               Objects.equals(content, that.content) &&
               Objects.equals(position, that.position);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, content, position);
    }
}