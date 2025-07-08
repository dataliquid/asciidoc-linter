package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = TableBlock.Builder.class)
public final class TableBlock extends AbstractBlock {
    @JsonProperty("columns")
    private final DimensionConfig columns;
    @JsonProperty("rows")
    private final DimensionConfig rows;
    @JsonProperty("header")
    private final HeaderConfig header;
    @JsonProperty("caption")
    private final CaptionConfig caption;
    @JsonProperty("format")
    private final FormatConfig format;
    
    private TableBlock(Builder builder) {
        super(builder);
        this.columns = builder.columns;
        this.rows = builder.rows;
        this.header = builder.header;
        this.caption = builder.caption;
        this.format = builder.format;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.TABLE;
    }
    
    public DimensionConfig getColumns() {
        return columns;
    }
    
    public DimensionConfig getRows() {
        return rows;
    }
    
    public HeaderConfig getHeader() {
        return header;
    }
    
    public CaptionConfig getCaption() {
        return caption;
    }
    
    public FormatConfig getFormat() {
        return format;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        @JsonProperty("min")
        private final Integer min;
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("severity")
        private final Severity severity;
        
        private DimensionConfig(DimensionConfigBuilder builder) {
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
        
        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DimensionConfigBuilder {
            private Integer min;
            private Integer max;
            private Severity severity;
            
            public DimensionConfigBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            public DimensionConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public DimensionConfigBuilder severity(Severity severity) {
                this.severity = severity;
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
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }
    
    @JsonDeserialize(builder = HeaderConfig.HeaderConfigBuilder.class)
    public static class HeaderConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private HeaderConfig(HeaderConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static HeaderConfigBuilder builder() {
            return new HeaderConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class HeaderConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            public HeaderConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public HeaderConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public HeaderConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public HeaderConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public HeaderConfig build() {
                return new HeaderConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }
    
    @JsonDeserialize(builder = CaptionConfig.CaptionConfigBuilder.class)
    public static class CaptionConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("severity")
        private final Severity severity;
        
        private CaptionConfig(CaptionConfigBuilder builder) {
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
        
        public static CaptionConfigBuilder builder() {
            return new CaptionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class CaptionConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public CaptionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public CaptionConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public CaptionConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public CaptionConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public CaptionConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public CaptionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CaptionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), 
                               minLength, maxLength, severity);
        }
    }
    
    @JsonDeserialize(builder = FormatConfig.FormatConfigBuilder.class)
    public static class FormatConfig {
        @JsonProperty("style")
        private final String style;
        @JsonProperty("borders")
        private final Boolean borders;
        @JsonProperty("severity")
        private final Severity severity;
        
        private FormatConfig(FormatConfigBuilder builder) {
            this.style = builder.style;
            this.borders = builder.borders;
            this.severity = builder.severity;
        }
        
        public String getStyle() {
            return style;
        }
        
        public Boolean getBorders() {
            return borders;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static FormatConfigBuilder builder() {
            return new FormatConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class FormatConfigBuilder {
            private String style;
            private Boolean borders;
            private Severity severity;
            
            public FormatConfigBuilder style(String style) {
                this.style = style;
                return this;
            }
            
            public FormatConfigBuilder borders(Boolean borders) {
                this.borders = borders;
                return this;
            }
            
            public FormatConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public FormatConfig build() {
                return new FormatConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FormatConfig that)) return false;
            return Objects.equals(style, that.style) &&
                   Objects.equals(borders, that.borders) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(style, borders, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private DimensionConfig columns;
        private DimensionConfig rows;
        private HeaderConfig header;
        private CaptionConfig caption;
        private FormatConfig format;
        
        public Builder columns(DimensionConfig columns) {
            this.columns = columns;
            return this;
        }
        
        public Builder rows(DimensionConfig rows) {
            this.rows = rows;
            return this;
        }
        
        public Builder header(HeaderConfig header) {
            this.header = header;
            return this;
        }
        
        public Builder caption(CaptionConfig caption) {
            this.caption = caption;
            return this;
        }
        
        public Builder format(FormatConfig format) {
            this.format = format;
            return this;
        }
        
        @Override
        public TableBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new TableBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(columns, that.columns) &&
               Objects.equals(rows, that.rows) &&
               Objects.equals(header, that.header) &&
               Objects.equals(caption, that.caption) &&
               Objects.equals(format, that.format);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), columns, rows, header, caption, format);
    }
}