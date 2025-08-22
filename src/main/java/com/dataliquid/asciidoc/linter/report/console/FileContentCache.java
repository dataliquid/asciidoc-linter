package com.dataliquid.asciidoc.linter.report.console;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches file contents during validation to avoid repeated file reads.
 */
public class FileContentCache {
    private final Map<String, List<String>> cache = new HashMap<>();

    /**
     * Gets the lines of a file, reading from cache if available.
     */
    public List<String> getFileLines(String filename) {
        return cache.computeIfAbsent(filename, this::readFileLines);
    }

    private List<String> readFileLines(String filename) {
        try {
            Path path = Paths.get(filename);
            if (Files.exists(path) && Files.isReadable(path)) {
                return Files.readAllLines(path);
            }
        } catch (IOException e) {
            // Log error but don't fail - return empty list
        }
        return List.of();
    }

    /**
     * Clears the cache to free memory.
     */
    public void clear() {
        cache.clear();
    }
}
