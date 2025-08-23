package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.util.List;
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

    // Constants for rule IDs
    private static final String SECTION_MIN_OCCURRENCES_RULE = "section.min-occurrences";
    private static final String BLOCK_OCCURRENCE_MIN_RULE = "block.occurrence.min";
    private static final String PARAGRAPH_LINES_MIN_RULE = "paragraph.lines.min";
    private static final String PARAGRAPH_SENTENCE_OCCURRENCE_MIN_RULE = "paragraph.sentence.occurrence.min";
    private static final String PARAGRAPH_SENTENCE_WORDS_MIN_RULE = "paragraph.sentence.words.min";
    private static final String VIDEO_CAPTION_REQUIRED_RULE = "video.caption.required";
    private static final String AUDIO_TITLE_REQUIRED_RULE = "audio.title.required";
    private static final String TABLE_CAPTION_REQUIRED_RULE = "table.caption.required";
    private static final String TABLE_HEADER_REQUIRED_RULE = "table.header.required";
    private static final String EXAMPLE_CAPTION_REQUIRED_RULE = "example.caption.required";
    private static final String EXAMPLE_COLLAPSIBLE_REQUIRED_RULE = "example.collapsible.required";
    private static final String VERSE_AUTHOR_REQUIRED_RULE = "verse.author.required";
    private static final String VERSE_ATTRIBUTION_REQUIRED_RULE = "verse.attribution.required";
    private static final String VERSE_CONTENT_REQUIRED_RULE = "verse.content.required";
    private static final String DLIST_DESCRIPTIONS_REQUIRED_RULE = "dlist.descriptions.required";
    private static final String PASS_CONTENT_REQUIRED_RULE = "pass.content.required";
    private static final String PASS_REASON_REQUIRED_RULE = "pass.reason.required";
    private static final String PASS_TYPE_REQUIRED_RULE = "pass.type.required";
    private static final String LITERAL_TITLE_REQUIRED_RULE = "literal.title.required";
    private static final String ULIST_ITEMS_MIN_RULE = "ulist.items.min";
    private static final String ULIST_MARKER_STYLE_RULE = "ulist.markerStyle";

    private final DisplayConfig config;
    private final ColorScheme colorScheme;

    public HighlightRenderer(DisplayConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.colorScheme = new ColorScheme(config.isUseColors());
    }

    /**
     * Renders source context with error highlighting.
     */
    public void renderWithHighlight(SourceContext context, ValidationMessage message, PrintWriter writer) {

        List<SourceContext.ContextLine> lines = context.getLines();
        for (SourceContext.ContextLine line : lines) {

            // Check if this is a placeholder line for block.occurrence.min
            if (line.isErrorLine() && line.getContent().isEmpty() && "block.occurrence.min".equals(message.getRuleId())
                    && message.getMissingValueHint() != null && message.getMissingValueHint().contains("\n")) {

                // Split multi-line placeholder into separate lines
                String[] placeholderLines = message.getMissingValueHint().split("\n");

                // Render each line of the multi-line placeholder
                for (int j = 0; j < placeholderLines.length; j++) {
                    String placeholderContent = placeholderLines[j];

                    // Create context line for each placeholder line
                    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Necessary for each placeholder line
                    SourceContext.ContextLine placeholderLine = new SourceContext.ContextLine(line.getNumber() + j,
                            placeholderContent, true // Mark as error line
                    );

                    // Render with line number and placeholder markers
                    renderPlaceholderLine(placeholderLine, writer, j == 0, j == placeholderLines.length - 1);
                }
                continue; // Skip the original empty line
            }

            renderLine(line, message, writer);
        }
    }

    private void renderPlaceholderLine(SourceContext.ContextLine line, PrintWriter writer, boolean isFirstLine,
            boolean isLastLine) {
        // Line number prefix
        String linePrefix = "";
        if (config.isShowLineNumbers()) {
            linePrefix = String.format("%4d | ", line.getNumber());
            if (config.isUseColors()) {
                linePrefix = colorScheme.contextLineNumber(linePrefix);
            }
        }

        // Add placeholder markers only on first and last lines
        StringBuilder contentBuilder = new StringBuilder(line.getContent().length() + 20);
        contentBuilder.append(line.getContent());
        if (isFirstLine) {
            contentBuilder.insert(0, PLACEHOLDER_START);
        }
        if (isLastLine) {
            contentBuilder.append(PLACEHOLDER_END);
        }
        String content = contentBuilder.toString();

        // Color the entire placeholder content
        String highlighted = colorScheme.error(content);

        writer.println(linePrefix + highlighted);
    }

    private void renderLine(SourceContext.ContextLine line, ValidationMessage message, PrintWriter writer) {
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
            String highlightedContent = highlightErrorInLine(line.getContent(), message, line.getNumber());
            writer.println(linePrefix + highlightedContent);

            // Add underline/marker if configured
            if (config.getHighlightStyle() == HighlightStyle.UNDERLINE && shouldShowUnderline(message)) {
                renderUnderline(line, message, writer);
            }
        } else {
            // Context line
            writer.println(linePrefix + colorScheme.contextLine(line.getContent()));
        }
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private String highlightErrorInLine(String line, ValidationMessage message, int lineNum) {
        // For missing values: insert placeholder
        if (message.getErrorType() == ErrorType.MISSING_VALUE && message.getMissingValueHint() != null) {
            // For section.min-occurrences, insert placeholder on empty lines
            if (SECTION_MIN_OCCURRENCES_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For block.occurrence.min, insert placeholder on empty lines
            else if (BLOCK_OCCURRENCE_MIN_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For paragraph.lines.min, only insert placeholder on empty lines
            else if (PARAGRAPH_LINES_MIN_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For paragraph.sentence.occurrence.min, append placeholder at end of line with
            // space
            else if (PARAGRAPH_SENTENCE_OCCURRENCE_MIN_RULE.equals(message.getRuleId())) {
                return insertPlaceholderWithSpace(line, message);
            }
            // For paragraph.sentence.words.min, insert placeholder before punctuation
            else if (PARAGRAPH_SENTENCE_WORDS_MIN_RULE.equals(message.getRuleId())) {
                return insertPlaceholderBeforePunctuation(line, message);
            }
            // For video.caption.required, only insert placeholder on empty lines
            else if (VIDEO_CAPTION_REQUIRED_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For audio.title.required, only insert placeholder on empty lines
            else if (AUDIO_TITLE_REQUIRED_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For table.caption.required, only insert placeholder on empty lines
            else if (TABLE_CAPTION_REQUIRED_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For table.header.required, only insert placeholder on empty lines
            else if (TABLE_HEADER_REQUIRED_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For example.caption.required, only insert placeholder on empty lines
            else if (EXAMPLE_CAPTION_REQUIRED_RULE.equals(message.getRuleId())) {
                if (line.isEmpty()) {
                    return insertPlaceholder(line, message);
                } else {
                    // Don't add placeholder to existing content lines
                    return line;
                }
            }
            // For example.collapsible.required, only insert placeholder on empty lines
            else if (EXAMPLE_COLLAPSIBLE_REQUIRED_RULE.equals(message.getRuleId())) {
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

    private String insertPlaceholderWithSpace(String line, ValidationMessage message) {
        // Special handling for sentence placeholders - always add space before
        int col = message.getLocation().getStartColumn();

        // Generate placeholder with leading space
        String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
        String placeholder = " " + colorScheme.error(placeholderText);

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

    private String insertPlaceholderBeforePunctuation(String line, ValidationMessage message) {
        // Special handling for inserting placeholder before punctuation marks
        int col = message.getLocation().getStartColumn();

        // Generate placeholder with leading space
        String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
        String placeholder = " " + colorScheme.error(placeholderText);

        if (col <= 0 || col > line.length() + 1) {
            // Invalid column, try to find punctuation at end of line
            if (line.matches(".*[.!?]+$")) {
                // Find where punctuation starts
                int punctStart = line.length() - 1;
                while (punctStart > 0 && ".!?".indexOf(line.charAt(punctStart)) != -1) {
                    punctStart--;
                }
                punctStart++; // Move to first punctuation character

                String before = line.substring(0, punctStart);
                String after = line.substring(punctStart);
                return before + placeholder + after;
            }
            // No punctuation found, append at end
            return line + placeholder;
        }

        // Insert at specific position (col is 1-based)
        if (col > line.length()) {
            // Position beyond line end, append
            return line + placeholder;
        }

        // Insert before punctuation at specified position
        String before = line.substring(0, col - 1);
        String after = line.substring(col - 1);

        return before + placeholder + after;
    }

    private String insertPlaceholder(String line, ValidationMessage message) {
        // For section.min-occurrences errors with empty lines, show placeholder at
        // start
        if ("section.min-occurrences".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For block.occurrence.min errors with empty lines, show placeholder at start
        if ("block.occurrence.min".equals(message.getRuleId()) && line.isEmpty()) {
            // Only handle single-line placeholders here
            // Multi-line placeholders are handled in renderWithHighlight
            if (!message.getMissingValueHint().contains("\n")) {
                String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
                return colorScheme.error(placeholderText);
            }
            // For multi-line placeholders, return empty line (will be handled later)
            return line;
        }

        // For paragraph.lines.min errors with empty lines, show placeholder at start
        if ("paragraph.lines.min".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For metadata.required errors with empty lines, show placeholder at start
        if ("metadata.required".equals(message.getRuleId()) && line.isEmpty()) {
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

        // For example.caption.required errors with empty lines, show placeholder at
        // start
        if ("example.caption.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For example.collapsible.required errors with empty lines, show placeholder at
        // start
        if ("example.collapsible.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For admonition.title.required errors with empty lines, show placeholder at
        // start
        if ("admonition.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For admonition.content.required errors with empty lines, show placeholder at
        // start
        if ("admonition.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For admonition.icon.required errors with empty lines, show placeholder at
        // start
        if ("admonition.icon.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For sidebar.title.required errors with empty lines, show placeholder at start
        if ("sidebar.title.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For sidebar.content.required errors with empty lines, show placeholder at
        // start
        if ("sidebar.content.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For sidebar.position.required errors with empty lines, show placeholder at
        // start
        if ("sidebar.position.required".equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For verse.author.required and verse.attribution.required errors,
        // insert placeholder with comma inline in the [verse] line
        if (VERSE_AUTHOR_REQUIRED_RULE.equals(message.getRuleId())
                || VERSE_ATTRIBUTION_REQUIRED_RULE.equals(message.getRuleId())) {
            // Insert at position 7 with comma
            if (line.startsWith("[verse")) {
                String before = line.substring(0, 6); // "[verse"
                String after = line.substring(6); // "]" or existing content
                String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
                return before + "," + colorScheme.error(placeholderText) + after;
            }
        }

        // For verse.content.required errors with empty lines, show placeholder at start
        if (VERSE_CONTENT_REQUIRED_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For dlist.descriptions.required errors, append placeholder to term line
        if (DLIST_DESCRIPTIONS_REQUIRED_RULE.equals(message.getRuleId())) {
            // This is special - append to the term line
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return line + " " + colorScheme.error(placeholderText);
        }

        // For pass.content.required errors with empty lines, show placeholder at start
        if (PASS_CONTENT_REQUIRED_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For pass.reason.required errors with empty lines, show placeholder at start
        if (PASS_REASON_REQUIRED_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For pass.type.required errors with empty lines, show placeholder at start
        if (PASS_TYPE_REQUIRED_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For literal.title.required errors with empty lines, show placeholder at start
        if (LITERAL_TITLE_REQUIRED_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For ulist.items.min errors with empty lines, show placeholder at start
        if (ULIST_ITEMS_MIN_RULE.equals(message.getRuleId()) && line.isEmpty()) {
            String placeholderText = PLACEHOLDER_START + message.getMissingValueHint() + PLACEHOLDER_END;
            return colorScheme.error(placeholderText);
        }

        // For ulist.markerStyle errors, replace the existing marker
        if (ULIST_MARKER_STYLE_RULE.equals(message.getRuleId())) {
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

    private void renderUnderline(SourceContext.ContextLine line, ValidationMessage message, PrintWriter writer) {
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
            underline.append("     | "); // Combined: 4 spaces for line number + " | "
        }

        // Spaces before error
        underline.append(" ".repeat(Math.max(0, startCol - 1)));

        // Underline characters
        int length = Math.min(endCol - startCol + 1, config.getMaxLineWidth());
        underline.append(colorScheme.errorMarker("~".repeat(length)));

        writer.println(underline);
    }
}
