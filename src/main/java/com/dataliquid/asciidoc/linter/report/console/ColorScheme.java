package com.dataliquid.asciidoc.linter.report.console;

import com.dataliquid.asciidoc.linter.config.common.Severity;

/**
 * Manages ANSI color codes for console output.
 */
public class ColorScheme {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GRAY = "\u001B[90m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_DIM = "\u001B[2m";
    
    private final boolean useColors;
    
    public ColorScheme(boolean useColors) {
        this.useColors = useColors;
    }
    
    public String colorize(String text, Severity severity) {
        if (!useColors) {
            return text;
        }
        
        return switch (severity) {
            case ERROR -> ANSI_RED + text + ANSI_RESET;
            case WARN -> ANSI_YELLOW + text + ANSI_RESET;
            case INFO -> ANSI_BLUE + text + ANSI_RESET;
        };
    }
    
    public String error(String text) {
        return useColors ? ANSI_RED + text + ANSI_RESET : text;
    }
    
    public String warning(String text) {
        return useColors ? ANSI_YELLOW + text + ANSI_RESET : text;
    }
    
    public String info(String text) {
        return useColors ? ANSI_BLUE + text + ANSI_RESET : text;
    }
    
    public String success(String text) {
        return useColors ? ANSI_GREEN + text + ANSI_RESET : text;
    }
    
    public String code(String text) {
        return useColors ? ANSI_CYAN + text + ANSI_RESET : text;
    }
    
    public String contextLine(String text) {
        return useColors ? ANSI_DIM + text + ANSI_RESET : text;
    }
    
    public String contextLineNumber(String lineNum) {
        return useColors ? ANSI_GRAY + lineNum + ANSI_RESET : lineNum;
    }
    
    public String errorLineNumber(String lineNum) {
        return useColors ? ANSI_RED + ANSI_BOLD + lineNum + ANSI_RESET : lineNum;
    }
    
    public String errorMarker(String marker) {
        return useColors ? ANSI_RED + marker + ANSI_RESET : marker;
    }
    
    public String suggestion(String text) {
        return useColors ? ANSI_GREEN + text + ANSI_RESET : text;
    }
    
    public String suggestionIcon(String icon) {
        return useColors ? ANSI_YELLOW + icon + ANSI_RESET : icon;
    }
    
    public String separator(String separator) {
        return useColors ? ANSI_GRAY + separator + ANSI_RESET : separator;
    }
    
    public String header(String text) {
        return useColors ? ANSI_BOLD + text + ANSI_RESET : text;
    }
    
    public String errorBar(String bar) {
        return useColors ? ANSI_RED + bar + ANSI_RESET : bar;
    }
    
    public String warningBar(String bar) {
        return useColors ? ANSI_YELLOW + bar + ANSI_RESET : bar;
    }
    
    public String infoBar(String bar) {
        return useColors ? ANSI_BLUE + bar + ANSI_RESET : bar;
    }
}