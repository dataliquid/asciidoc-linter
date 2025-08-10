package com.dataliquid.asciidoc.linter.cli.display;

/**
 * Constants for display formatting in the CLI.
 * Centralizes display-related configuration values.
 */
public final class DisplayConstants {
    
    /**
     * Default width for ASCII boxes used in console output.
     * Set to 120 characters for better readability on modern terminals.
     */
    public static final int DEFAULT_BOX_WIDTH = 120;
    
    /**
     * Default width for labels in configuration display.
     * Proportionally adjusted for the wider box.
     */
    public static final int DEFAULT_LABEL_WIDTH = 30;
    
    private DisplayConstants() {
        // Private constructor to prevent instantiation
    }
}