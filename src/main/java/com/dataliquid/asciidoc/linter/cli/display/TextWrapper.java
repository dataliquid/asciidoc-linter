package com.dataliquid.asciidoc.linter.cli.display;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for wrapping text to fit within a specified width. Provides
 * different strategies for wrapping based on content type.
 */
public class TextWrapper {

    /**
     * Wraps text to fit within the specified maximum width. Automatically detects
     * the best wrapping strategy based on content.
     *
     * @param  text     the text to wrap
     * @param  maxWidth the maximum width for each line
     *
     * @return          a list of wrapped lines
     */
    public List<String> wrap(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }

        if (text.length() <= maxWidth) {
            return List.of(text);
        }

        // Choose wrapping strategy based on content
        if (text.contains(",")) {
            return wrapByComma(text, maxWidth);
        } else if (text.contains("/") || text.contains("\\")) {
            return wrapByPath(text, maxWidth);
        } else {
            return wrapByWords(text, maxWidth);
        }
    }

    /**
     * Wraps comma-separated text, breaking at commas when possible. Useful for
     * lists of items.
     *
     * @param  text     the comma-separated text to wrap
     * @param  maxWidth the maximum width for each line
     *
     * @return          a list of wrapped lines
     */
    public List<String> wrapByComma(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] parts = text.split(",");
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            String separator = i < parts.length - 1 ? ", " : "";

            if (currentLine.isEmpty()) {
                currentLine.append(part).append(separator);
            } else if (currentLine.length() + part.length() + separator.length() <= maxWidth) {
                currentLine.append(part).append(separator);
            } else {
                lines.add(currentLine.toString());
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Necessary to create new line
                StringBuilder newLine = new StringBuilder(part).append(separator);
                currentLine = newLine;
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? List.of(text) : lines;
    }

    /**
     * Wraps file paths, breaking at path separators when possible. Preserves path
     * structure for readability.
     *
     * @param  text     the path text to wrap
     * @param  maxWidth the maximum width for each line
     *
     * @return          a list of wrapped lines
     */
    public List<String> wrapByPath(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String remaining = text;

        while (remaining.length() > maxWidth) {
            // Try to break at path separator
            int lastSeparator = findLastPathSeparator(remaining, maxWidth);

            if (lastSeparator > 0 && lastSeparator < maxWidth) {
                lines.add(remaining.substring(0, lastSeparator + 1));
                remaining = remaining.substring(lastSeparator + 1);
            } else {
                // Force break at maxWidth if no good break point
                lines.add(remaining.substring(0, maxWidth));
                remaining = remaining.substring(maxWidth);
            }
        }

        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }

        return lines.isEmpty() ? List.of(text) : lines;
    }

    /**
     * Wraps text by words, breaking at spaces when possible. Standard word wrapping
     * algorithm.
     *
     * @param  text     the text to wrap
     * @param  maxWidth the maximum width for each line
     *
     * @return          a list of wrapped lines
     */
    public List<String> wrapByWords(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.isEmpty()) {
                currentLine.append(word);
            } else if (currentLine.length() + 1 + word.length() <= maxWidth) {
                currentLine.append(' ').append(word);
            } else {
                lines.add(currentLine.toString());
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Necessary to create new line
                StringBuilder newLine = new StringBuilder(word);
                currentLine = newLine;
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? List.of(text) : lines;
    }

    /**
     * Finds the last path separator (/ or \) within the specified limit.
     *
     * @param  text  the text to search
     * @param  limit the maximum index to search up to
     *
     * @return       the index of the last path separator, or -1 if not found
     */
    private int findLastPathSeparator(String text, int limit) {
        int lastSlash = text.lastIndexOf('/', limit);
        int lastBackslash = text.lastIndexOf('\\', limit);
        return Math.max(lastSlash, lastBackslash);
    }
}
