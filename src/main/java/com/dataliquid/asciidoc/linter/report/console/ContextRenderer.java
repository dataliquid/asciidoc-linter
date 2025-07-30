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
        
        // For verse blocks, ensure we include the closing delimiter
        if (("verse.author.required".equals(message.getRuleId()) || 
             "verse.attribution.required".equals(message.getRuleId())) &&
            endLine < fileLines.size()) {
            // Extend by one more line to include closing ____
            endLine = Math.min(fileLines.size(), endLine + 1);
        }
        
        // Ensure valid bounds for subList
        int fromIndex = Math.max(0, Math.min(startLine - 1, fileLines.size()));
        int toIndex = Math.max(fromIndex, Math.min(endLine, fileLines.size()));
        
        // Extract context lines
        List<String> contextLines = new ArrayList<>(fileLines.subList(fromIndex, toIndex));
        
        // For section.min-occurrences errors, add an empty line for the missing section
        if ("section.min-occurrences".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Extract the section level from the placeholder hint (e.g., "== section" -> level 1)
            String hint = message.getMissingValueHint();
            int sectionLevel = 0;
            if (hint != null) {
                // Count the = signs to determine level
                for (int i = 0; i < hint.length() && hint.charAt(i) == '='; i++) {
                    sectionLevel++;
                }
                sectionLevel--; // Convert to 0-based level
            }
            
            // Find the appropriate position to insert the section placeholder
            int insertIndex = -1;
            
            if (sectionLevel == 0) {
                // For level 0 sections (document title), insert at the beginning
                insertIndex = 0;
            } else if (sectionLevel == 1) {
                // For level 1 sections, insert after the document title
                for (int i = 0; i < contextLines.size(); i++) {
                    String line = contextLines.get(i).trim();
                    if (line.startsWith("= ") && !line.startsWith("== ")) {
                        insertIndex = i + 1;
                        // If there's an empty line after the title, insert after it
                        if (insertIndex < contextLines.size() && contextLines.get(insertIndex).trim().isEmpty()) {
                            insertIndex++;
                        }
                        break;
                    }
                }
            } else {
                // For other levels, find the last section that would be a parent/sibling
                // and insert after any content following it
                for (int i = contextLines.size() - 1; i >= 0; i--) {
                    String line = contextLines.get(i).trim();
                    
                    // Check if this is a section header at the parent level or same level
                    boolean isSection = false;
                    int lineLevel = -1;
                    
                    // Count = signs at the beginning of the line
                    if (line.startsWith("=")) {
                        int count = 0;
                        for (int j = 0; j < line.length() && line.charAt(j) == '='; j++) {
                            count++;
                        }
                        // Make sure it's followed by a space (valid section header)
                        if (count > 0 && count < line.length() && line.charAt(count) == ' ') {
                            isSection = true;
                            lineLevel = count - 1; // Convert to 0-based
                        }
                    }
                    
                    // If we found a section at parent level, insert after its content
                    if (isSection && lineLevel == sectionLevel - 1) {
                        // Find the end of this section's content
                        insertIndex = i + 1;
                        // Skip any empty lines after the section header
                        while (insertIndex < contextLines.size() && contextLines.get(insertIndex).trim().isEmpty()) {
                            insertIndex++;
                        }
                        // Skip content lines until we find the next section or end
                        while (insertIndex < contextLines.size()) {
                            String nextLine = contextLines.get(insertIndex).trim();
                            // Stop if we hit another section
                            if (nextLine.startsWith("=") && nextLine.contains(" ")) {
                                break;
                            }
                            insertIndex++;
                        }
                        break;
                    }
                }
            }
            
            // If we found where to insert, add an empty line
            if (insertIndex >= 0 && insertIndex <= contextLines.size()) {
                contextLines.add(insertIndex, "");
                // Create a special SourceContext that marks the inserted line as error line
                List<SourceContext.ContextLine> lines = new ArrayList<>();
                int lineNum = startLine;
                for (int i = 0; i < contextLines.size(); i++) {
                    String content = contextLines.get(i);
                    boolean isErrorLine = (i == insertIndex); // Mark the inserted empty line as error
                    lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                    lineNum++;
                }
                return new SourceContext(lines, loc);
            }
        }
        
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
        
        // For video.caption.required errors, add an extra line before the video block
        if ("video.caption.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where caption should be
            int videoLineIndex = loc.getStartLine() - startLine;
            if (videoLineIndex >= 0 && videoLineIndex <= contextLines.size()) {
                contextLines.add(videoLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For audio.title.required errors, add an extra line before the audio block
        if ("audio.title.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where title should be
            int audioLineIndex = loc.getStartLine() - startLine;
            if (audioLineIndex >= 0 && audioLineIndex <= contextLines.size()) {
                contextLines.add(audioLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For table.caption.required errors, add an extra line before the table block
        if ("table.caption.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where caption should be
            int tableLineIndex = loc.getStartLine() - startLine;
            if (tableLineIndex >= 0 && tableLineIndex <= contextLines.size()) {
                contextLines.add(tableLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For example.caption.required errors, add an extra line before the example block
        if ("example.caption.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where caption should be
            int exampleLineIndex = loc.getStartLine() - startLine;
            if (exampleLineIndex >= 0 && exampleLineIndex <= contextLines.size()) {
                contextLines.add(exampleLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For example.collapsible.required errors, add an extra line before the example block
        if ("example.collapsible.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where collapsible attribute should be
            int exampleLineIndex = loc.getStartLine() - startLine;
            if (exampleLineIndex >= 0 && exampleLineIndex <= contextLines.size()) {
                contextLines.add(exampleLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For admonition.title.required errors, add an extra line before the admonition block
        if ("admonition.title.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where title should be (before [NOTE])
            int admonitionLineIndex = loc.getStartLine() - startLine - 1;
            if (admonitionLineIndex >= 0 && admonitionLineIndex <= contextLines.size()) {
                contextLines.add(admonitionLineIndex, "");
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == admonitionLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For admonition.content.required errors, add an extra line inside the admonition block
        if ("admonition.content.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Find the line after the opening delimiter (====)
            int admonitionLineIndex = loc.getStartLine() - startLine;
            int insertedLineIndex = -1;
            // Look for the opening delimiter and insert after it
            for (int i = admonitionLineIndex; i < contextLines.size(); i++) {
                if (contextLines.get(i).trim().equals("====")) {
                    contextLines.add(i + 1, "");
                    insertedLineIndex = i + 1;
                    break;
                }
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == insertedLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For admonition.icon.required errors, add an extra line before the document content
        if ("admonition.icon.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Icon directive should go in the document header, after the title
            // Find the line after the document title (line starting with =)
            for (int i = 0; i < contextLines.size(); i++) {
                if (contextLines.get(i).trim().startsWith("=") && !contextLines.get(i).trim().startsWith("==")) {
                    // Insert after the title line
                    contextLines.add(i + 1, "");
                    break;
                }
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For sidebar.title.required errors, add an extra line before the sidebar block
        if ("sidebar.title.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where title should be (before ****)
            int sidebarLineIndex = loc.getStartLine() - startLine - 1;
            if (sidebarLineIndex >= 0 && sidebarLineIndex <= contextLines.size()) {
                contextLines.add(sidebarLineIndex, "");
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == sidebarLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For sidebar.content.required errors, add an extra line inside the sidebar block
        if ("sidebar.content.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Find the line after the opening delimiter (****)
            int sidebarLineIndex = loc.getStartLine() - startLine;
            int insertedLineIndex = -1;
            // Look for the opening delimiter and insert after it
            for (int i = sidebarLineIndex; i < contextLines.size(); i++) {
                if (contextLines.get(i).trim().equals("****")) {
                    contextLines.add(i + 1, "");
                    insertedLineIndex = i + 1;
                    break;
                }
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == insertedLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For sidebar.position.required errors, add an extra line before the sidebar block
        if ("sidebar.position.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where position attribute should be (before ****)
            int sidebarLineIndex = loc.getStartLine() - startLine;
            if (sidebarLineIndex >= 0 && sidebarLineIndex <= contextLines.size()) {
                contextLines.add(sidebarLineIndex, "");
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == sidebarLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For table.header.required errors, add an extra line after |===
        if ("table.header.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line after |===
            int headerLineIndex = loc.getStartLine() - startLine;
            if (headerLineIndex >= 0 && headerLineIndex <= contextLines.size()) {
                contextLines.add(headerLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For verse.author.required and verse.attribution.required errors, 
        // don't add extra lines - the placeholders will be inserted inline in the [verse] line
        
        // For verse.content.required errors, add an extra line inside the verse block
        if ("verse.content.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Find the line after the opening delimiter (____)
            int verseLineIndex = loc.getStartLine() - startLine;
            int insertedLineIndex = -1;
            // Look for the opening delimiter and insert after it
            for (int i = verseLineIndex; i < contextLines.size(); i++) {
                if (contextLines.get(i).trim().equals("____")) {
                    contextLines.add(i + 1, "");
                    insertedLineIndex = i + 1;
                    break;
                }
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == insertedLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For dlist.descriptions.required errors, don't add extra lines
        // The placeholder will be inserted inline in the term line
        
        // For pass.content.required errors, add an extra line inside the pass block
        if ("pass.content.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Find the line after the opening delimiter (++++)
            int passLineIndex = loc.getStartLine() - startLine;
            int insertedLineIndex = -1;
            // Look for the opening delimiter and insert after it
            for (int i = passLineIndex; i < contextLines.size(); i++) {
                if (contextLines.get(i).trim().equals("++++")) {
                    contextLines.add(i + 1, "");
                    insertedLineIndex = i + 1;
                    break;
                }
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == insertedLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For pass.reason.required errors, add an extra line before the pass block
        if ("pass.reason.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where reason should be
            int passLineIndex = loc.getStartLine() - startLine;
            if (passLineIndex >= 0 && passLineIndex <= contextLines.size()) {
                contextLines.add(passLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For pass.type.required errors, add an extra line before the pass block
        if ("pass.type.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where type should be
            int passLineIndex = loc.getStartLine() - startLine;
            if (passLineIndex >= 0 && passLineIndex <= contextLines.size()) {
                contextLines.add(passLineIndex, "");
            }
            // Create a special SourceContext that marks the inserted line as error line
            return createContextWithCaptionLine(contextLines, startLine, loc);
        }
        
        // For literal.title.required errors, add an extra line before the literal block
        if ("literal.title.required".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Insert empty line at the position where title should be (before ....)
            int literalLineIndex = loc.getStartLine() - startLine;
            if (literalLineIndex >= 0 && literalLineIndex <= contextLines.size()) {
                contextLines.add(literalLineIndex, "");
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == literalLineIndex); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For ulist.items.min errors, add an extra line after the last item
        if ("ulist.items.min".equals(message.getRuleId()) && 
            message.getErrorType() == ErrorType.MISSING_VALUE) {
            // Find the position after the last item in the list
            int ulistLineIndex = loc.getStartLine() - startLine;
            if (ulistLineIndex >= 0 && ulistLineIndex < contextLines.size()) {
                // Insert after the current line (which should be the last item)
                contextLines.add(ulistLineIndex + 1, "");
            }
            
            // Create context with the inserted line marked as error
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            int lineNum = startLine;
            for (int i = 0; i < contextLines.size(); i++) {
                String content = contextLines.get(i);
                boolean isErrorLine = (i == ulistLineIndex + 1); // Mark the inserted empty line as error
                lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
                lineNum++;
            }
            return new SourceContext(lines, loc);
        }
        
        // For ulist.markerStyle errors, show corrected markers in context lines
        if ("ulist.markerStyle".equals(message.getRuleId()) && 
            message.getExpectedValue().isPresent()) {
            String expectedMarker = message.getExpectedValue().get();
            // Replace all list item markers with the expected marker
            for (int i = 0; i < contextLines.size(); i++) {
                String line = contextLines.get(i);
                if (line.trim().startsWith("*") || line.trim().startsWith("-")) {
                    // Replace the first occurrence of * or - with the expected marker
                    contextLines.set(i, line.replaceFirst("[*-]", expectedMarker));
                }
            }
        }
        
        // For quote.attribution.required or quote.citation.required, don't add extra lines
        // The placeholders will be inserted inline in the existing [quote] line
        
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
     * Creates a SourceContext with a caption line inserted before the video block.
     * Used for video.caption.required errors where we need to show where the caption should be.
     */
    private SourceContext createContextWithCaptionLine(List<String> contextLines, int startLine, SourceLocation loc) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        
        int lineNum = startLine;
        int videoLineIndex = loc.getStartLine() - startLine;
        
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = false;
            
            // Mark the inserted empty line (where caption should be) as error line
            if (i == videoLineIndex && content.isEmpty()) {
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