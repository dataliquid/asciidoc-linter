package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.CONTEXT_LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.HIGHLIGHT_STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.MAX_LINE_WIDTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_HEADER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_LINE_NUMBERS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.USE_COLORS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
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

    private final int _contextLines;
    private final HighlightStyle _highlightStyle;
    private final boolean _useColors;
    private final boolean _showLineNumbers;
    private final int _maxLineWidth;
    private final boolean _showHeader;

    private DisplayConfig(Builder builder) {
        this._contextLines = builder._contextLines;
        this._highlightStyle = builder._highlightStyle;
        this._useColors = builder._useColors;
        this._showLineNumbers = builder._showLineNumbers;
        this._maxLineWidth = builder._maxLineWidth;
        this._showHeader = builder._showHeader;
    }

    public int getContextLines() {
        return this._contextLines;
    }

    public HighlightStyle getHighlightStyle() {
        return this._highlightStyle;
    }

    public boolean isUseColors() {
        return this._useColors;
    }

    public boolean isShowLineNumbers() {
        return this._showLineNumbers;
    }

    public int getMaxLineWidth() {
        return this._maxLineWidth;
    }

    public boolean isShowHeader() {
        return this._showHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DisplayConfig that = (DisplayConfig) o;
        return _contextLines == that._contextLines && _useColors == that._useColors
                && _showLineNumbers == that._showLineNumbers && _maxLineWidth == that._maxLineWidth
                && _showHeader == that._showHeader && _highlightStyle == that._highlightStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_contextLines, _highlightStyle, _useColors, _showLineNumbers, _maxLineWidth, _showHeader);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static final class Builder {
        private int _contextLines = DEFAULT_CONTEXT_LINES;
        private HighlightStyle _highlightStyle = DEFAULT_HIGHLIGHT_STYLE;
        private boolean _useColors = DEFAULT_USE_COLORS;
        private boolean _showLineNumbers = DEFAULT_SHOW_LINE_NUMBERS;
        private int _maxLineWidth = DEFAULT_MAX_LINE_WIDTH;
        private boolean _showHeader = DEFAULT_SHOW_HEADER;

        private Builder() {
        }

        @JsonProperty(CONTEXT_LINES)
        public Builder contextLines(int contextLines) {
            this._contextLines = contextLines;
            return this;
        }

        @JsonProperty(HIGHLIGHT_STYLE)
        public Builder highlightStyle(HighlightStyle highlightStyle) {
            this._highlightStyle = highlightStyle != null ? highlightStyle : DEFAULT_HIGHLIGHT_STYLE;
            return this;
        }

        @JsonProperty(USE_COLORS)
        public Builder useColors(boolean useColors) {
            this._useColors = useColors;
            return this;
        }

        @JsonProperty(SHOW_LINE_NUMBERS)
        public Builder showLineNumbers(boolean showLineNumbers) {
            this._showLineNumbers = showLineNumbers;
            return this;
        }

        @JsonProperty(MAX_LINE_WIDTH)
        public Builder maxLineWidth(int maxLineWidth) {
            this._maxLineWidth = maxLineWidth;
            return this;
        }

        @JsonProperty(SHOW_HEADER)
        public Builder showHeader(boolean showHeader) {
            this._showHeader = showHeader;
            return this;
        }

        public DisplayConfig build() {
            return new DisplayConfig(this);
        }
    }
}
