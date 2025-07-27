package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Renders source code with visual error highlighting.
 */
public class HighlightRenderer {
    private static final String PLACEHOLDER_START = "«";
    private static final String PLACEHOLDER_END = "»";
    
    private final DisplayConfig config;
    private final ColorScheme colorScheme;
    
    public HighlightRenderer(DisplayConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.colorScheme = new ColorScheme(config.isUseColors());
    }
    
    /**
     * Renders source context with error highlighting.
     */
    public void renderWithHighlight(SourceContext context, 
                                  ValidationMessage message, 
                                  PrintWriter writer) {
        
        for (SourceContext.ContextLine line : context.getLines()) {
            renderLine(line, message, writer);
        }
    }
    
    private void renderLine(SourceContext.ContextLine line, 
                          ValidationMessage message,
                          PrintWriter writer) {
        // Line number prefix
        String linePrefix = "";
        if (config.isShowLineNumbers()) {
            String lineNum = String.format("%4d", line.getNumber());
            if (line.isErrorLine()) {
                linePrefix = colorScheme.errorLineNumber(lineNum) + " | ";
            } else {
                linePrefix = colorScheme.contextLineNumber(lineNum) + " | ";
            }
        }
        
        // Line content
        if (line.isErrorLine()) {
            String highlightedContent = highlightErrorInLine(
                line.getContent(), 
                message,
                line.getNumber()
            );
            writer.println(linePrefix + highlightedContent);
            
            // Add underline/marker if configured
            if (config.getHighlightStyle() == HighlightStyle.UNDERLINE && 
                shouldShowUnderline(message)) {
                renderUnderline(line, message, writer);
            }
        } else {
            // Context line
            writer.println(linePrefix + colorScheme.contextLine(line.getContent()));
        }
    }
    
    private String highlightErrorInLine(String line, ValidationMessage message, int lineNum) {
        // For missing values: insert placeholder
        if (message.getErrorType() == ErrorType.MISSING_VALUE && 
            message.getMissingValueHint() != null) {
            return insertPlaceholder(line, message);
        }
        
        // For invalid values: keep original (will be underlined)
        return line;
    }
    
    private String insertPlaceholder(String line, ValidationMessage message) {
        int col = message.getLocation().getStartColumn();
        String prefix = message.getPlaceholderPrefix() != null ? message.getPlaceholderPrefix() : "";
        String placeholder = colorScheme.error(
            prefix + PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END
        );
        
        if (col <= 0 || col > line.length()) {
            // Append at end if column is invalid
            return line + placeholder;
        }
        
        // Insert at specific position
        String before = line.substring(0, col - 1);
        String after = col <= line.length() ? line.substring(col - 1) : "";
        
        return before + placeholder + after;
    }
    
    private boolean shouldShowUnderline(ValidationMessage message) {
        // Don't underline for missing values (already shown with placeholder)
        if (message.getErrorType() == ErrorType.MISSING_VALUE) {
            return false;
        }
        
        // Show underline for ALL other error types
        return true;
    }
    
    private void renderUnderline(SourceContext.ContextLine line, 
                               ValidationMessage message, 
                               PrintWriter writer) {
        int startCol = message.getLocation().getStartColumn();
        int endCol = message.getLocation().getEndColumn();
        
        // Validate columns
        if (startCol < 0) {
            return;
        }
        
        // Default to column 1 if not specified
        if (startCol == 0) {
            startCol = 1;
        }
        
        if (endCol <= 0 || endCol < startCol) {
            endCol = Math.min(line.getContent().length(), startCol + 20);
        }
        
        StringBuilder underline = new StringBuilder();
        
        // Padding for line number
        if (config.isShowLineNumbers()) {
            underline.append("    ");   // 4 spaces for line number
            underline.append(" | ");    // 3 spaces for " | "
        }
        
        // Spaces before error
        underline.append(" ".repeat(Math.max(0, startCol - 1)));
        
        // Underline characters
        int length = Math.min(endCol - startCol + 1, config.getMaxLineWidth());
        underline.append(colorScheme.errorMarker("~".repeat(length)));
        
        writer.println(underline);
    }
}