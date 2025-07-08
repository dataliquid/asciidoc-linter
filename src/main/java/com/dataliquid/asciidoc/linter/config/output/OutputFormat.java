package com.dataliquid.asciidoc.linter.config.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Output format styles for console output.
 */
public enum OutputFormat {
    /**
     * Enhanced format with full context, highlighting, and suggestions.
     */
    ENHANCED("enhanced"),
    
    /**
     * Simple format with basic error information.
     */
    SIMPLE("simple"),
    
    /**
     * Compact single-line format for CI/CD environments.
     */
    COMPACT("compact");
    
    private final String value;
    
    OutputFormat(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static OutputFormat fromValue(String value) {
        for (OutputFormat format : OutputFormat.values()) {
            if (format.value.equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown output format: " + value);
    }
}