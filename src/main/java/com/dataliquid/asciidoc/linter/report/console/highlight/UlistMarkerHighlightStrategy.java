package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for ulist.markerStyle rule that replaces the marker character.
 */
public class UlistMarkerHighlightStrategy implements HighlightStrategy {

    private static final String RULE_ID = "ulist.markerStyle";

    @Override
    public boolean supports(String ruleId) {
        return RULE_ID.equals(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (message.getMissingValueHint() == null) {
            return null; // Use default handling
        }

        int col = message.getLocation().getStartColumn();
        if (col > 0 && col <= line.length()) {
            String before = line.substring(0, col - 1);
            String after = col < line.length() ? line.substring(col) : "";
            String placeholder = HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);
            return before + placeholder + after;
        }

        return null; // Use default handling
    }
}
