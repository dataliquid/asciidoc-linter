package com.dataliquid.asciidoc.linter.report.console;

import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
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
        List<String> contextLines = fileLines.subList(fromIndex, toIndex);
        
        return new SourceContext(contextLines, startLine, loc);
    }
    
    /**
     * Clears the file cache to free memory.
     */
    public void clearCache() {
        fileCache.clear();
    }
}