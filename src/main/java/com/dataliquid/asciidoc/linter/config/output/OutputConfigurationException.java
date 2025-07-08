package com.dataliquid.asciidoc.linter.config.output;

/**
 * Exception thrown when output configuration loading or validation fails.
 */
public class OutputConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public OutputConfigurationException(String message) {
        super(message);
    }
    
    public OutputConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}