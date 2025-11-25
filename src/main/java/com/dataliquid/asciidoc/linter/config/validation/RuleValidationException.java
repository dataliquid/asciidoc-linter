package com.dataliquid.asciidoc.linter.config.validation;

/**
 * Exception thrown when user configuration does not match the schema.
 */
public class RuleValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RuleValidationException(String message) {
        super(message);
    }

    public RuleValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
