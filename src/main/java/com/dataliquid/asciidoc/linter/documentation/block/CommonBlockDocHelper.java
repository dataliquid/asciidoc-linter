package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Helper class with common documentation generation operations.
 */
public final class CommonBlockDocHelper {

    private static final String CHARACTERS_SUFFIX = " characters";
    private static final String REQUIRED_YES = "  - Required: Yes";
    private static final String PATTERN_PREFIX = "  - Pattern: ";
    private static final String MINIMUM_PREFIX = "  - Minimum: ";
    private static final String MAXIMUM_PREFIX = "  - Maximum: ";
    private static final String MINIMUM_LENGTH_PREFIX = "  - Minimum length: ";
    private static final String MAXIMUM_LENGTH_PREFIX = "  - Maximum length: ";

    private CommonBlockDocHelper() {
        // Utility class
    }

    public static void writeRequired(PrintWriter writer) {
        writer.println(REQUIRED_YES);
    }

    public static void writePattern(PrintWriter writer, String pattern, PatternHumanizer humanizer) {
        if (pattern != null) {
            writer.println(PATTERN_PREFIX + humanizer.humanize(pattern));
        }
    }

    public static void writeMinimum(PrintWriter writer, Object value, String unit) {
        if (value != null) {
            writer.println(MINIMUM_PREFIX + value + (unit != null ? unit : ""));
        }
    }

    public static void writeMaximum(PrintWriter writer, Object value, String unit) {
        if (value != null) {
            writer.println(MAXIMUM_PREFIX + value + (unit != null ? unit : ""));
        }
    }

    public static void writeMinLength(PrintWriter writer, Object value) {
        if (value != null) {
            writer.println(MINIMUM_LENGTH_PREFIX + value + CHARACTERS_SUFFIX);
        }
    }

    public static void writeMaxLength(PrintWriter writer, Object value) {
        if (value != null) {
            writer.println(MAXIMUM_LENGTH_PREFIX + value + CHARACTERS_SUFFIX);
        }
    }

    public static void writeTitleSection(PrintWriter writer, boolean required, String pattern,
            PatternHumanizer humanizer) {
        writer.println("* **Title:**");
        if (required) {
            writeRequired(writer);
        }
        writePattern(writer, pattern, humanizer);
    }

    public static void writeLinesSection(PrintWriter writer, Integer min, Integer max) {
        writer.println("* **Lines:**");
        writeMinimum(writer, min, null);
        writeMaximum(writer, max, null);
    }

    public static void writeContentSection(PrintWriter writer, boolean required) {
        writer.println("* **Content:**");
        if (required) {
            writeRequired(writer);
        }
    }
}
