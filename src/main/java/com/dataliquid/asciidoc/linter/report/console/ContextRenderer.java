package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Extracts source code context around validation errors.
 */
public class ContextRenderer {
    private final DisplayConfig config;
    private final FileContentCache fileCache;
    
    public ContextRenderer(DisplayConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.fileCache = new FileContentCache();
    }
    
    /**
     * Gets the source context for a validation message.
     */
    public SourceContext getContext(ValidationMessage message) {
        SourceLocation loc = message.getLocation();
        
        // If message already has context lines, use them
        if (!message.getContextLines().isEmpty()) {
            return new SourceContext(
                message.getContextLines(),
                Math.max(1, loc.getStartLine() - config.getContextLines()),
                loc
            );
        }
        
        // Otherwise, read from file
        List<String> fileLines = fileCache.getFileLines(loc.getFilename());
        
        if (fileLines.isEmpty()) {
            // No file content available
            return new SourceContext(List.of(), loc.getStartLine(), loc);
        }
        
        // Calculate context bounds
        int startLine = Math.max(1, loc.getStartLine() - config.getContextLines());
        int endLine = Math.min(fileLines.size(), loc.getEndLine() + config.getContextLines());
        
        // Ensure valid bounds for subList
        int fromIndex = Math.max(0, Math.min(startLine - 1, fileLines.size()));
        int toIndex = Math.max(fromIndex, Math.min(endLine, fileLines.size()));
        
        // Extract context lines
        List<String> contextLines = new ArrayList<>(fileLines.subList(fromIndex, toIndex));
        
        // For paragraph.lines.min errors, add extra empty lines for the placeholders
        if ("paragraph.lines.min".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Calculate how many lines are missing
            int missingLines = calculateMissingLines(message);
            for (int i = 0; i < missingLines; i++) {
                contextLines.add("");
            }
            // Create a special SourceContext that marks the extra lines as error lines
            return createContextWithExtraLines(contextLines, startLine, loc, missingLines);
        }
        
        return new SourceContext(contextLines, startLine, loc);
    }
    
    /**
     * Calculates how many lines are missing based on the validation message.
     */
    private int calculateMissingLines(ValidationMessage message) {
        // Parse actual and expected values
        String actualValue = message.getActualValue().orElse("0");
        String expectedValue = message.getExpectedValue().orElse("");
        
        try {
            int actual = Integer.parseInt(actualValue);
            // Expected value is in format "At least X lines"
            String[] parts = expectedValue.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("\\d+")) {
                    int expected = Integer.parseInt(parts[i]);
                    return Math.max(1, expected - actual);
                }
            }
        } catch (NumberFormatException e) {
            // Fallback to 1 line if parsing fails
        }
        
        return 1;
    }
    
    /**
     * Creates a SourceContext with extra lines marked as error lines.
     * Used for paragraph.lines.min errors where we need to show where the missing lines should be.
     */
    private SourceContext createContextWithExtraLines(List<String> contextLines, int startLine, SourceLocation loc, int extraLineCount) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        
        int lineNum = startLine;
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = false;
            
            // Mark the original error line
            if (lineNum >= loc.getStartLine() && lineNum <= loc.getEndLine()) {
                isErrorLine = true;
            }
            
            // Mark the last extraLineCount lines (the added empty lines) as error lines too
            if (i >= contextLines.size() - extraLineCount && content.isEmpty()) {
                isErrorLine = true;
            }
            
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        
        return new SourceContext(lines, loc);
    }
    
    /**
     * Clears the file cache to free memory.
     */
    public void clearCache() {
        fileCache.clear();
    }
}