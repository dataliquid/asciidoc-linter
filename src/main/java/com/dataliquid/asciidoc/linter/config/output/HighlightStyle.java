package com.dataliquid.asciidoc.linter.config.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Styles for highlighting errors in console output.
 */
public enum HighlightStyle {
    /**
     * Underline style using tilde characters.
     */
    UNDERLINE("underline"),
    
    /**
     * No highlighting.
     */
    NONE("none");
    
    private final String value;
    
    HighlightStyle(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static HighlightStyle fromValue(String value) {
        for (HighlightStyle style : HighlightStyle.values()) {
            if (style.value.equalsIgnoreCase(value)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Unknown highlight style: " + value);
    }
}