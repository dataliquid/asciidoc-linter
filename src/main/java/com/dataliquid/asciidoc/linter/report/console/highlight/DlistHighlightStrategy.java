package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for dlist.descriptions.required rule that appends placeholder to
 * term line.
 */
public class DlistHighlightStrategy implements HighlightStrategy {

    private static final String RULE_ID = "dlist.descriptions.required";

    @Override
    public boolean supports(String ruleId) {
        return RULE_ID.equals(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (message.getMissingValueHint() == null) {
            return line;
        }

        // Append placeholder to the term line
        String placeholder = HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);
        return line + " " + placeholder;
    }
}
