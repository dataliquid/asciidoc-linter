package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
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
            // For section.min-occurrences, insert placeholder on empty lines
            if ("section.min-occurrences".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For block.occurrence.min, insert placeholder on empty lines
            else if ("block.occurrence.min".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For paragraph.lines.min, only insert placeholder on empty lines
            else if ("paragraph.lines.min".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            } 
            // For video.caption.required, only insert placeholder on empty lines
            else if ("video.caption.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For audio.title.required, only insert placeholder on empty lines
            else if ("audio.title.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For table.caption.required, only insert placeholder on empty lines
            else if ("table.caption.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For table.header.required, only insert placeholder on empty lines
            else if ("table.header.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For example.caption.required, only insert placeholder on empty lines
            else if ("example.caption.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For example.collapsible.required, only insert placeholder on empty lines
            else if ("example.collapsible.required".equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            } else {
                return insertPlaceholder(line, message);
            }
        }
        
        // For invalid values: keep original (will be underlined)
        return line;
    }
    
    private String insertPlaceholder(String line, ValidationMessage message) {
        // For section.min-occurrences errors with empty lines, show placeholder at start
        if ("section.min-occurrences".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For block.occurrence.min errors with empty lines, show placeholder at start
        if ("block.occurrence.min".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For paragraph.lines.min errors with empty lines, show placeholder at start
        if ("paragraph.lines.min".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For video.caption.required errors with empty lines, show placeholder at start
        if ("video.caption.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For audio.title.required errors with empty lines, show placeholder at start
        if ("audio.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For table.caption.required errors with empty lines, show placeholder at start
        if ("table.caption.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For table.header.required errors with empty lines, show placeholder at start
        if ("table.header.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For example.caption.required errors with empty lines, show placeholder at start
        if ("example.caption.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For example.collapsible.required errors with empty lines, show placeholder at start
        if ("example.collapsible.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For admonition.title.required errors with empty lines, show placeholder at start
        if ("admonition.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For admonition.content.required errors with empty lines, show placeholder at start
        if ("admonition.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For admonition.icon.required errors with empty lines, show placeholder at start
        if ("admonition.icon.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For sidebar.title.required errors with empty lines, show placeholder at start
        if ("sidebar.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For sidebar.content.required errors with empty lines, show placeholder at start
        if ("sidebar.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For sidebar.position.required errors with empty lines, show placeholder at start
        if ("sidebar.position.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For verse.author.required and verse.attribution.required errors, 
        // insert placeholder with comma inline in the [verse] line
        if ("verse.author.required".equals(message.getRuleId()) || 
            "verse.attribution.required".equals(message.getRuleId())) {
            // Insert at position 7 with comma
            if (line.startsWith("[verse")) {
                String before = line.substring(0, 6);  // "[verse"
                String after = line.substring(6);      // "]" or existing content
                String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
                return before + "," + colorScheme.error(placeholderText) + after;
            }
        }
        
        // For verse.content.required errors with empty lines, show placeholder at start
        if ("verse.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For dlist.descriptions.required errors, append placeholder to term line
        if ("dlist.descriptions.required".equals(message.getRuleId())) {
            // This is special - append to the term line
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return line + " " + colorScheme.error(placeholderText);
        }
        
        // For pass.content.required errors with empty lines, show placeholder at start
        if ("pass.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For pass.reason.required errors with empty lines, show placeholder at start
        if ("pass.reason.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For pass.type.required errors with empty lines, show placeholder at start
        if ("pass.type.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For literal.title.required errors with empty lines, show placeholder at start
        if ("literal.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For ulist.items.min errors with empty lines, show placeholder at start
        if ("ulist.items.min".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }
        
        // For ulist.markerStyle errors, replace the existing marker
        if ("ulist.markerStyle".equals(message.getRuleId())) {
            int col = message.getLocation().getStartColumn();
            if (col > 0 && col <= line.length()) {
                // Replace the marker character at the specified position
                String before = line.substring(0, col - 1);
                String after = col < line.length() ? line.substring(col) : "";
                String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
                return before + colorScheme.error(placeholderText) + after;
            }
        }
        
        int col = message.getLocation().getStartColumn();
        
        // Generate complete placeholder based on context
        String placeholderText;
        PlaceholderContext context = message.getPlaceholderContext();
        if (context != null) {
            placeholderText = context.generatePlaceholder(message.getMissingValueHint());
        } else {
            // Fallback to simple placeholder
            placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
        }
        
        String placeholder = colorScheme.error(placeholderText);
        
        if (col <= 0 || col > line.length() + 1) {
            // Append at end if column is invalid
            return line + placeholder;
        }
        
        // Insert at specific position (col is 1-based)
        if (col > line.length()) {
            // Insert at end of line
            return line + placeholder;
        }
        
        String before = line.substring(0, col - 1);
        String after = line.substring(col - 1);
        
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