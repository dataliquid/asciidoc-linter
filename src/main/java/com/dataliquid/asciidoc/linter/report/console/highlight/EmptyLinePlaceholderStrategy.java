package com.dataliquid.asciidoc.linter.report.console.highlight;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for rules that insert placeholder only on empty lines. Handles:
 * section.min-occurrences, paragraph.lines.min, video.caption.required,
 * audio.title.required, table.caption.required, table.header.required,
 * example.caption.required, example.collapsible.required,
 * admonition.title.required, admonition.content.required,
 * admonition.icon.required, sidebar.title.required, sidebar.content.required,
 * sidebar.position.required, verse.content.required, pass.content.required,
 * pass.reason.required, pass.type.required, literal.title.required,
 * ulist.items.min, metadata.required
 */
public class EmptyLinePlaceholderStrategy implements HighlightStrategy {

    private static final Set<String> SUPPORTED_RULES = new HashSet<>(Arrays
            .asList("section.min-occurrences", "paragraph.lines.min", "video.caption.required", "audio.title.required",
                    "table.caption.required", "table.header.required", "example.caption.required",
                    "example.collapsible.required", "admonition.title.required", "admonition.content.required",
                    "admonition.icon.required", "sidebar.title.required", "sidebar.content.required",
                    "sidebar.position.required", "verse.content.required", "pass.content.required",
                    "pass.reason.required", "pass.type.required", "literal.title.required", "ulist.items.min",
                    "metadata.required"));

    @Override
    public boolean supports(String ruleId) {
        return SUPPORTED_RULES.contains(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (line.isEmpty() && message.getMissingValueHint() != null) {
            return HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);
        }
        // Non-empty lines: return original (don't add placeholder to existing content)
        return line;
    }
}
