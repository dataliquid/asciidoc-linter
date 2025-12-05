package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy interface for highlighting errors in source lines.
 */
public interface HighlightStrategy {

    /**
     * Checks if this strategy handles the given rule ID.
     *
     * @param  ruleId the rule ID
     *
     * @return        true if this strategy handles the rule
     */
    boolean supports(String ruleId);

    /**
     * Highlights an error in the given line.
     *
     * @param  line        the source line content
     * @param  message     the validation message
     * @param  colorScheme the color scheme to use
     *
     * @return             the highlighted line, or null if default handling should
     *                     be used
     */
    String highlight(String line, ValidationMessage message, ColorScheme colorScheme);
}
