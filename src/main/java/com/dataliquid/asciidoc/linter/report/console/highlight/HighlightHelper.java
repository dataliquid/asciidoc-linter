package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;

/**
 * Helper class with common operations for highlight strategies.
 */
public final class HighlightHelper {

    public static final String PLACEHOLDER_START = "«";
    public static final String PLACEHOLDER_END = "»";

    private HighlightHelper() {
        // Utility class
    }

    /**
     * Creates a colored placeholder with the given hint text.
     */
    public static String createPlaceholder(String hint, ColorScheme colorScheme) {
        String placeholderText = PLACEHOLDER_START + hint + PLACEHOLDER_END;
        return colorScheme.error(placeholderText);
    }

    /**
     * Creates a placeholder text without color (for use when color will be applied
     * later).
     */
    public static String createPlaceholderText(String hint) {
        return PLACEHOLDER_START + hint + PLACEHOLDER_END;
    }
}
