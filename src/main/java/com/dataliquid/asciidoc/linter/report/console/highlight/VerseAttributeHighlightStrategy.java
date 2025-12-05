package com.dataliquid.asciidoc.linter.report.console.highlight;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Verse.AUTHOR_REQUIRED;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Verse.ATTRIBUTION_REQUIRED;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for verse author/attribution rules that insert placeholder inline.
 * Handles: verse.author.required, verse.attribution.required
 */
public class VerseAttributeHighlightStrategy implements HighlightStrategy {

    private static final String AUTHOR_RULE = AUTHOR_REQUIRED;
    private static final String ATTRIBUTION_RULE = ATTRIBUTION_REQUIRED;

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
