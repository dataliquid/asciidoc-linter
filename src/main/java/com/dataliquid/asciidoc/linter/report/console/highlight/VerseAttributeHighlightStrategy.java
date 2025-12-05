package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for verse author/attribution rules that insert placeholder inline.
 * Handles: verse.author.required, verse.attribution.required
 */
public class VerseAttributeHighlightStrategy implements HighlightStrategy {

    private static final String AUTHOR_RULE = "verse.author.required";
    private static final String ATTRIBUTION_RULE = "verse.attribution.required";

    @Override
    public boolean supports(String ruleId) {
        return AUTHOR_RULE.equals(ruleId) || ATTRIBUTION_RULE.equals(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (message.getMissingValueHint() == null) {
            return line;
        }

        // Insert placeholder inline in the [verse] line
        if (line.startsWith("[verse")) {
            String before = line.substring(0, 6); // "[verse"
            String after = line.substring(6); // "]" or existing content
            String placeholder = HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);
            return before + "," + placeholder + after;
        }

        return line;
    }
}
