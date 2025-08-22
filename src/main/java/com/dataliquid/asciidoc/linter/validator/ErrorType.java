package com.dataliquid.asciidoc.linter.validator;

/**
 * Types of validation errors for enhanced console output.
 */
public enum ErrorType {
    /**
     * A required value is missing.
     */
    MISSING_VALUE,

    /**
     * A value doesn't match the expected pattern.
     */
    INVALID_PATTERN,

    /**
     * A value is outside the allowed range.
     */
    OUT_OF_RANGE,

    /**
     * A value is not in the allowed set.
     */
    INVALID_ENUM,

    /**
     * Multiple validation failures.
     */
    MULTIPLE_ERRORS,

    /**
     * Generic validation error.
     */
    GENERIC
}
