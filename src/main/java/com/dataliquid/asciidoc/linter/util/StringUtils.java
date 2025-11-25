package com.dataliquid.asciidoc.linter.util;

/**
 * Utility class for string operations.
 */
public final class StringUtils {

    private StringUtils() {
        // Utility class
    }

    /**
     * Checks if a string is blank (null, empty, or contains only whitespace). This
     * method is more efficient than String.trim().isEmpty().
     *
     * @param  str the string to check
     *
     * @return     true if the string is blank, false otherwise
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a string is not blank (not null, not empty, and contains
     * non-whitespace). This method is more efficient than !String.trim().isEmpty().
     *
     * @param  str the string to check
     *
     * @return     true if the string is not blank, false otherwise
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}