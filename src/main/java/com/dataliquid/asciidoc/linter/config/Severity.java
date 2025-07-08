package com.dataliquid.asciidoc.linter.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Severity {
    ERROR,
    WARN,
    INFO;
    
    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
    
    @JsonCreator
    public static Severity fromValue(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "error" -> ERROR;
            case "warn" -> WARN;
            case "info" -> INFO;
            default -> throw new IllegalArgumentException("Unknown severity: " + value);
        };
    }
}