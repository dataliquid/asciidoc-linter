package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for block.occurrence.min rule. Handles multi-line placeholders
 * specially.
 */
public class BlockOccurrenceHighlightStrategy implements HighlightStrategy {

    private static final String RULE_ID = "block.occurrence.min";

    @Override
    public boolean supports(String ruleId) {
        return RULE_ID.equals(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (!line.isEmpty() || message.getMissingValueHint() == null) {
            return line;
        }

        // Only handle single-line placeholders here
        // Multi-line placeholders are handled in HighlightRenderer.renderWithHighlight
        if (!message.getMissingValueHint().contains("\n")) {
            return HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);
        }

        // For multi-line placeholders, return empty line (handled separately)
        return line;
    }
}
