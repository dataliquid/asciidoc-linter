package com.dataliquid.asciidoc.linter.config.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Severity {
    ERROR, WARN, INFO;

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static Severity fromValue(String value) {
        if (value == null)
            return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
        case "error" -> ERROR;
        case "warn" -> WARN;
        case "info" -> INFO;
        default -> throw new IllegalArgumentException("Unknown severity: " + value);
        };
    }
}
