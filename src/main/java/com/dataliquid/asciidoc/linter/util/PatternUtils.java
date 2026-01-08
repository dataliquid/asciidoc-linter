package com.dataliquid.asciidoc.linter.util;

import java.util.regex.Pattern;

/**
 * Utility class for Pattern comparison operations. Centralizes pattern
 * comparison logic to eliminate code duplication.
 */
public final class PatternUtils {

    private PatternUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Compares two Pattern objects by their pattern strings. Handles null cases and
     * uses reference equality optimization.
     *
     * @param  p1 the first Pattern object
     * @param  p2 the second Pattern object
     *
     * @return    true if both patterns have the same pattern string
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals") // Reference equality optimization
    public static boolean patternEquals(Pattern p1, Pattern p2) {
        if (p1 == p2) {
            return true;
        }
        if (p1 == null || p2 == null) {
            return false;
        }
        return p1.pattern().equals(p2.pattern());
    }

    /**
     * Converts a Pattern to its pattern string. Returns null if the pattern is
     * null.
     *
     * @param  pattern the Pattern object
     *
     * @return         the pattern string, or null if pattern is null
     */
    public static String patternToString(Pattern pattern) {
        return pattern != null ? pattern.pattern() : null;
    }

    /**
     * Computes hash code for a Pattern based on its pattern string. Returns 0 if
     * the pattern is null.
     *
     * @param  pattern the Pattern object
     *
     * @return         hash code of the pattern string, or 0 if pattern is null
     */
    public static int patternHashCode(Pattern pattern) {
        return pattern != null ? pattern.pattern().hashCode() : 0;
    }
}
