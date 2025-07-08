package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for display settings in console output.
 */
@JsonDeserialize(builder = DisplayConfig.Builder.class)
public final class DisplayConfig {
    private static final int DEFAULT_CONTEXT_LINES = 2;
    private static final HighlightStyle DEFAULT_HIGHLIGHT_STYLE = HighlightStyle.UNDERLINE;
    private static final boolean DEFAULT_USE_COLORS = true;
    private static final boolean DEFAULT_SHOW_LINE_NUMBERS = true;
    private static final int DEFAULT_MAX_LINE_WIDTH = 120;
    private static final boolean DEFAULT_SHOW_HEADER = true;
    
    private final int contextLines;
    private final HighlightStyle highlightStyle;
    private final boolean useColors;
    private final boolean showLineNumbers;
    private final int maxLineWidth;
    private final boolean showHeader;
    
    private DisplayConfig(Builder builder) {
        this.contextLines = builder.contextLines;
        this.highlightStyle = builder.highlightStyle;
        this.useColors = builder.useColors;
        this.showLineNumbers = builder.showLineNumbers;
        this.maxLineWidth = builder.maxLineWidth;
        this.showHeader = builder.showHeader;
    }
    
    public int getContextLines() {
        return contextLines;
    }
    
    public HighlightStyle getHighlightStyle() {
        return highlightStyle;
    }
    
    public boolean isUseColors() {
        return useColors;
    }
    
    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }
    
    public int getMaxLineWidth() {
        return maxLineWidth;
    }
    
    public boolean isShowHeader() {
        return showHeader;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisplayConfig that = (DisplayConfig) o;
        return contextLines == that.contextLines &&
                useColors == that.useColors &&
                showLineNumbers == that.showLineNumbers &&
                maxLineWidth == that.maxLineWidth &&
                showHeader == that.showHeader &&
                highlightStyle == that.highlightStyle;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contextLines, highlightStyle, useColors, 
                          showLineNumbers, maxLineWidth, showHeader);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private int contextLines = DEFAULT_CONTEXT_LINES;
        private HighlightStyle highlightStyle = DEFAULT_HIGHLIGHT_STYLE;
        private boolean useColors = DEFAULT_USE_COLORS;
        private boolean showLineNumbers = DEFAULT_SHOW_LINE_NUMBERS;
        private int maxLineWidth = DEFAULT_MAX_LINE_WIDTH;
        private boolean showHeader = DEFAULT_SHOW_HEADER;
        
        private Builder() {
        }
        
        @JsonProperty("contextLines")
        public Builder contextLines(int contextLines) {
            this.contextLines = contextLines;
            return this;
        }
        
        @JsonProperty("highlightStyle")
        public Builder highlightStyle(HighlightStyle highlightStyle) {
            this.highlightStyle = highlightStyle != null ? highlightStyle : DEFAULT_HIGHLIGHT_STYLE;
            return this;
        }
        
        @JsonProperty("useColors")
        public Builder useColors(boolean useColors) {
            this.useColors = useColors;
            return this;
        }
        
        @JsonProperty("showLineNumbers")
        public Builder showLineNumbers(boolean showLineNumbers) {
            this.showLineNumbers = showLineNumbers;
            return this;
        }
        
        @JsonProperty("maxLineWidth")
        public Builder maxLineWidth(int maxLineWidth) {
            this.maxLineWidth = maxLineWidth;
            return this;
        }
        
        @JsonProperty("showHeader")
        public Builder showHeader(boolean showHeader) {
            this.showHeader = showHeader;
            return this;
        }
        
        public DisplayConfig build() {
            return new DisplayConfig(this);
        }
    }
}