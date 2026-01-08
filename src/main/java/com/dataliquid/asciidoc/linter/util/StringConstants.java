package com.dataliquid.asciidoc.linter.util;

/**
 * Central repository for commonly used string constants across the application.
 * This class eliminates string literal duplication and ensures consistency in
 * messaging and formatting.
 */
public final class StringConstants {

    private StringConstants() {
        // Private constructor to prevent instantiation
    }

    // Common suffixes for measurements and units
    public static final String CHARACTERS_SUFFIX = " characters";
    public static final String PX_UNIT = "px";

    // Common comparison phrases for validation messages
    public static final String AT_LEAST = "At least ";
    public static final String AT_MOST = "At most ";
}
