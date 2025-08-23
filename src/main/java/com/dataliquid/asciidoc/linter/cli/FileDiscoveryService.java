package com.dataliquid.asciidoc.linter.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for discovering AsciiDoc files based on Ant patterns.
 */
public class FileDiscoveryService {

    private static final Logger logger = LogManager.getLogger(FileDiscoveryService.class);

    /**
     * Discovers files based on the CLI configuration.
     *
     * @param  config      The CLI configuration
     *
     * @return             List of paths to validate
     *
     * @throws IOException if an I/O error occurs
     */
    public List<Path> discoverFiles(CLIConfig config) throws IOException {
        return discoverFiles(config.getInputPatterns(), config.getBaseDirectory());
    }

    /**
     * Discovers files matching the given Ant patterns.
     *
     * @param  patterns    List of Ant patterns
     * @param  baseDir     Base directory for relative patterns
     *
     * @return             List of matching file paths (duplicates removed)
     *
     * @throws IOException if an I/O error occurs
     */
    public List<Path> discoverFiles(List<String> patterns, Path baseDir) throws IOException {
        Set<Path> matchedFiles = new LinkedHashSet<>(); // Use LinkedHashSet to maintain order and remove duplicates

        for (String pattern : patterns) {
            logger.debug("Processing pattern: {}", pattern);

            // Handle absolute paths and simple file names
            Path patternPath = Paths.get(pattern);
            if (patternPath.isAbsolute() && patternPath.toFile().exists()) {
                if (patternPath.toFile().isFile()) {
                    matchedFiles.add(patternPath.normalize());
                    continue;
                }
            }

            // Check if it's a simple filename in the base directory
            Path simpleFile = baseDir.resolve(pattern);
            if (simpleFile.toFile().isFile()) {
                matchedFiles.add(simpleFile.normalize());
                continue;
            }

            // Process as Ant pattern
            matchedFiles.addAll(findFilesMatchingAntPattern(pattern, baseDir));
        }

        return new ArrayList<>(matchedFiles);
    }

    private List<Path> findFilesMatchingAntPattern(String pattern, Path baseDir) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();

        // Convert Ant pattern to file filter
        AntPatternFileFilter antFilter = new AntPatternFileFilter(pattern, baseDir);

        // Use all directories for traversal
        IOFileFilter dirFilter = TrueFileFilter.INSTANCE;

        // Find matching files
        File baseDirFile = baseDir.toFile();
        if (baseDirFile.exists() && baseDirFile.isDirectory()) {
            for (File file : FileUtils.listFiles(baseDirFile, antFilter, dirFilter)) {
                matchingFiles.add(file.toPath().normalize());
            }
        }

        return matchingFiles;
    }

    /**
     * Custom file filter for Ant patterns.
     */
    private static class AntPatternFileFilter implements IOFileFilter {
        private final String pattern;
        private final Path baseDir;

        public AntPatternFileFilter(String pattern, Path baseDir) {
            this.pattern = pattern;
            this.baseDir = baseDir;
        }

        @Override
        public boolean accept(File file) {
            if (!file.isFile()) {
                return false;
            }

            Path filePath = file.toPath().normalize();
            Path relativePath = baseDir.relativize(filePath);
            String pathStr = relativePath.toString().replace(File.separatorChar, '/');

            return matchesAntPattern(pathStr, pattern);
        }

        @Override
        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }

        private boolean matchesAntPattern(String path, String pattern) {
            boolean result = AntPatternMatcher.match(pattern, path);
            logger.debug("Matching '{}' against pattern '{}': {}", path, pattern, result);
            return result;
        }
    }

    /**
     * Simple Ant pattern matcher implementation with pattern caching.
     */
    private static class AntPatternMatcher {
        // Cache for compiled regex patterns to improve performance
        private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

        public static boolean match(String pattern, String path) {
            // Normalize paths - create local copies to avoid reassigning parameters
            String normalizedPattern = pattern.replace(File.separatorChar, '/');
            String normalizedPath = path.replace(File.separatorChar, '/');

            // Handle ** (matches any number of directories)
            if (normalizedPattern.contains("**")) {
                return matchWithDoubleWildcard(normalizedPattern, normalizedPath);
            }

            // Simple pattern matching
            return matchSimplePattern(normalizedPattern, normalizedPath);
        }

        private static boolean matchWithDoubleWildcard(String pattern, String path) {
            String[] patternParts = pattern.split("/");
            String[] pathParts = path.split("/");

            int patternIndex = 0;
            int pathIndex = 0;

            while (patternIndex < patternParts.length) {
                if (pathIndex >= pathParts.length) {
                    // Check if remaining pattern parts are all **
                    while (patternIndex < patternParts.length) {
                        if (!patternParts[patternIndex].equals("**")) {
                            return false;
                        }
                        patternIndex++;
                    }
                    return true;
                }

                String patternPart = patternParts[patternIndex];

                if (patternPart.equals("**")) {
                    // Handle **
                    if (patternIndex == patternParts.length - 1) {
                        // ** at end matches everything
                        return true;
                    }

                    // Find next pattern part after **
                    patternIndex++;
                    String nextPattern = patternParts[patternIndex];

                    // Try to match nextPattern with any remaining path part
                    boolean found = false;
                    while (pathIndex < pathParts.length) {
                        if (matchPart(nextPattern, pathParts[pathIndex])) {
                            found = true;
                            pathIndex++;
                            patternIndex++;
                            break;
                        }
                        pathIndex++;
                    }

                    if (!found) {
                        return false;
                    }
                } else {
                    // Normal part matching
                    if (pathIndex >= pathParts.length || !matchPart(patternPart, pathParts[pathIndex])) {
                        return false;
                    }
                    patternIndex++;
                    pathIndex++;
                }
            }

            // All pattern parts consumed, check if all path parts consumed
            return pathIndex == pathParts.length;
        }

        private static boolean matchSimplePattern(String pattern, String path) {
            String[] patternParts = pattern.split("/");
            String[] pathParts = path.split("/");

            if (patternParts.length != pathParts.length) {
                return false;
            }

            for (int i = 0; i < patternParts.length; i++) {
                if (!matchPart(patternParts[i], pathParts[i])) {
                    return false;
                }
            }

            return true;
        }

        private static boolean matchPart(String pattern, String text) {
            // Use cached pattern if available, otherwise compile and cache it
            Pattern compiledPattern = PATTERN_CACHE.computeIfAbsent(pattern, p -> {
                // Convert pattern to regex
                StringBuilder regex = new StringBuilder("^");
                for (int i = 0; i < p.length(); i++) {
                    char c = p.charAt(i);
                    switch (c) {
                    case '*':
                        regex.append(".*");
                        break;
                    case '?':
                        regex.append(".");
                        break;
                    case '.':
                    case '\\':
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                    case '^':
                    case '$':
                    case '{':
                    case '}':
                    case '+':
                    case '|':
                        regex.append("\\").append(c);
                        break;
                    default:
                        regex.append(c);
                    }
                }
                regex.append("$");

                return Pattern.compile(regex.toString());
            });

            return compiledPattern.matcher(text).matches();
        }
    }
}
