package com.dataliquid.asciidoc.linter.documentation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts regular expression patterns into human-readable descriptions.
 */
public class PatternHumanizer {
    
    private final Map<String, String> knownPatterns;
    
    public PatternHumanizer() {
        this.knownPatterns = new HashMap<>();
        initializeKnownPatterns();
    }
    
    private void initializeKnownPatterns() {
        // Common patterns
        knownPatterns.put("^[A-Z].*", "Must start with an uppercase letter");
        knownPatterns.put("^[a-z].*", "Must start with a lowercase letter");
        knownPatterns.put("^\\d+\\.\\d+\\.\\d+$", "Semantic Versioning Format (e.g. 1.0.0)");
        knownPatterns.put("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", "Valid email address");
        
        // URL patterns
        knownPatterns.put("^https?://.*", "Must start with http:// or https://");
        knownPatterns.put(".*\\.(png|jpg|jpeg|gif|svg)$", "Image file (PNG, JPG, JPEG, GIF or SVG)");
        knownPatterns.put(".*\\.(mp3|ogg|wav|m4a)$", "Audio file (MP3, OGG, WAV or M4A)");
        
        // Title patterns
        knownPatterns.put("^(Introduction|Einführung)$", "Must be 'Introduction' or 'Einführung'");
        knownPatterns.put("^Listing \\d+:.*", "Must start with 'Listing' followed by a number and colon");
        knownPatterns.put("^Table \\d+:.*", "Must start with 'Table' followed by a number and colon");
        knownPatterns.put("^Figure \\d+:.*", "Must start with 'Figure' followed by a number and colon");
        
        // Language patterns
        knownPatterns.put("^(java|python|javascript|yaml|json|xml)$", "Allowed languages: java, python, javascript, yaml, json, xml");
    }
    
    /**
     * Converts a regex pattern to a human-readable description.
     * 
     * @param pattern the pattern to humanize
     * @return a human-readable description
     */
    public String humanize(Pattern pattern) {
        if (pattern == null) {
            return "";
        }
        return humanize(pattern.pattern());
    }
    
    /**
     * Converts a regex pattern string to a human-readable description.
     * 
     * @param patternString the pattern string to humanize
     * @return a human-readable description
     */
    public String humanize(String patternString) {
        if (patternString == null || patternString.isEmpty()) {
            return "";
        }
        
        // Check known patterns first
        String known = knownPatterns.get(patternString);
        if (known != null) {
            return known;
        }
        
        // Try to generate description for common patterns
        String description = generateDescription(patternString);
        if (description != null) {
            return description;
        }
        
        // Fallback: show the pattern itself
        return "Must match pattern: " + patternString;
    }
    
    private String generateDescription(String pattern) {
        // Handle file extensions
        if (pattern.matches(".*\\\\\\.(\\w+\\|)*\\w+\\)\\$")) {
            String extensions = pattern.replaceAll(".*\\\\\\.(\\()?", "")
                                     .replaceAll("\\)\\$", "")
                                     .replaceAll("\\|", ", ");
            return "File extension must be: " + extensions.toUpperCase();
        }
        
        // Handle simple starts-with patterns
        if (pattern.startsWith("^") && !pattern.contains("$")) {
            String prefix = pattern.substring(1).replaceAll("\\\\_", "_");
            if (!prefix.contains("[") && !prefix.contains("(")) {
                return "Must start with '" + prefix + "'";
            }
        }
        
        // Handle simple ends-with patterns
        if (pattern.endsWith("$") && !pattern.startsWith("^")) {
            String suffix = pattern.substring(0, pattern.length() - 1);
            if (!suffix.contains("[") && !suffix.contains("(")) {
                return "Must end with '" + suffix + "'";
            }
        }
        
        // Handle exact match patterns
        if (pattern.startsWith("^") && pattern.endsWith("$")) {
            String exact = pattern.substring(1, pattern.length() - 1);
            if (!exact.contains("[") && !exact.contains("(") && !exact.contains("*") && !exact.contains("+")) {
                return "Must be exactly '" + exact + "'";
            }
        }
        
        // Handle character class patterns
        if (pattern.equals("^[A-Za-z]+$")) {
            return "Only letters allowed";
        }
        if (pattern.equals("^[0-9]+$")) {
            return "Only numbers allowed";
        }
        if (pattern.equals("^[A-Za-z0-9]+$")) {
            return "Only letters and numbers allowed";
        }
        
        return null;
    }
    
    /**
     * Registers a custom pattern description.
     * 
     * @param pattern the regex pattern
     * @param description the human-readable description
     */
    public void registerPattern(String pattern, String description) {
        knownPatterns.put(pattern, description);
    }
}