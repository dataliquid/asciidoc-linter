package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.CONTEXT_LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.HIGHLIGHT_STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.MAX_LINE_WIDTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_HEADER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_LINE_NUMBERS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.USE_COLORS;

/**
 * Configuration for display settings in console output.
 */
public final class DisplayConfig {
    private static final int DEFAULT_CONTEXT_LINES = 2;
    private static final HighlightStyle DEFAULT_HIGHLIGHT_STYLE = HighlightStyle.UNDERLINE;
    private static final boolean DEFAULT_USE_COLORS = true;
    private static final boolean DEFAULT_SHOW_LINE_NUMBERS = true;
    private static final int DEFAULT_MAX_LINE_WIDTH = 120;
    private static final boolean DEFAULT_SHOW_HEADER = true;

    private final int contextLinesValue;
    private final HighlightStyle highlightStyleValue;
    private final boolean useColorsValue;
    private final boolean showLineNumbersValue;
    private final int maxLineWidthValue;
    private final boolean showHeaderValue;

    @JsonCreator
    public DisplayConfig(@JsonProperty(CONTEXT_LINES) Integer contextLines,
            @JsonProperty(HIGHLIGHT_STYLE) HighlightStyle highlightStyle, @JsonProperty(USE_COLORS) Boolean useColors,
            @JsonProperty(SHOW_LINE_NUMBERS) Boolean showLineNumbers,
            @JsonProperty(MAX_LINE_WIDTH) Integer maxLineWidth, @JsonProperty(SHOW_HEADER) Boolean showHeader) {
        this.contextLinesValue = contextLines != null ? contextLines : DEFAULT_CONTEXT_LINES;
        this.highlightStyleValue = highlightStyle != null ? highlightStyle : DEFAULT_HIGHLIGHT_STYLE;
        this.useColorsValue = useColors != null ? useColors : DEFAULT_USE_COLORS;
        this.showLineNumbersValue = showLineNumbers != null ? showLineNumbers : DEFAULT_SHOW_LINE_NUMBERS;
        this.maxLineWidthValue = maxLineWidth != null ? maxLineWidth : DEFAULT_MAX_LINE_WIDTH;
        this.showHeaderValue = showHeader != null ? showHeader : DEFAULT_SHOW_HEADER;
    }

    public int getContextLines() {
        return this.contextLinesValue;
    }

    public HighlightStyle getHighlightStyle() {
        return this.highlightStyleValue;
    }

    public boolean isUseColors() {
        return this.useColorsValue;
    }

    public boolean isShowLineNumbers() {
        return this.showLineNumbersValue;
    }

    public int getMaxLineWidth() {
        return this.maxLineWidthValue;
    }

    public boolean isShowHeader() {
        return this.showHeaderValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DisplayConfig that = (DisplayConfig) o;
        return contextLinesValue == that.contextLinesValue && useColorsValue == that.useColorsValue
                && showLineNumbersValue == that.showLineNumbersValue && maxLineWidthValue == that.maxLineWidthValue
                && showHeaderValue == that.showHeaderValue && highlightStyleValue == that.highlightStyleValue;
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(contextLinesValue, highlightStyleValue, useColorsValue, showLineNumbersValue, maxLineWidthValue,
                        showHeaderValue);
    }
}