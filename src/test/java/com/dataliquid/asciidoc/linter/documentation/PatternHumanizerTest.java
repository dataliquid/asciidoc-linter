package com.dataliquid.asciidoc.linter.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PatternHumanizer")
class PatternHumanizerTest {
    
    private PatternHumanizer humanizer;
    
    @BeforeEach
    void setUp() {
        humanizer = new PatternHumanizer();
    }
    
    @Nested
    @DisplayName("Known Patterns")
    class KnownPatterns {
        
        @Test
        @DisplayName("should humanize uppercase start pattern")
        void shouldHumanizeUppercaseStartPattern() {
            String result = humanizer.humanize("^[A-Z].*");
            assertEquals("Must start with an uppercase letter", result);
        }
        
        @Test
        @DisplayName("should humanize semantic versioning pattern")
        void shouldHumanizeSemanticVersioningPattern() {
            String result = humanizer.humanize("^\\d+\\.\\d+\\.\\d+$");
            assertEquals("Semantic Versioning Format (e.g. 1.0.0)", result);
        }
        
        @Test
        @DisplayName("should humanize email pattern")
        void shouldHumanizeEmailPattern() {
            String result = humanizer.humanize("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
            assertEquals("Valid email address", result);
        }
        
        @Test
        @DisplayName("should humanize image file pattern")
        void shouldHumanizeImageFilePattern() {
            String result = humanizer.humanize(".*\\.(png|jpg|jpeg|gif|svg)$");
            assertEquals("Image file (PNG, JPG, JPEG, GIF or SVG)", result);
        }
        
        @Test
        @DisplayName("should humanize audio file pattern")
        void shouldHumanizeAudioFilePattern() {
            String result = humanizer.humanize(".*\\.(mp3|ogg|wav|m4a)$");
            assertEquals("Audio file (MP3, OGG, WAV or M4A)", result);
        }
    }
    
    @Nested
    @DisplayName("Generated Descriptions")
    class GeneratedDescriptions {
        
        @Test
        @DisplayName("should generate description for simple starts-with pattern")
        void shouldGenerateStartsWithDescription() {
            String result = humanizer.humanize("^Chapter");
            assertEquals("Must start with 'Chapter'", result);
        }
        
        @Test
        @DisplayName("should generate description for simple ends-with pattern")
        void shouldGenerateEndsWithDescription() {
            String result = humanizer.humanize("END$");
            assertEquals("Must end with 'END'", result);
        }
        
        @Test
        @DisplayName("should generate description for exact match pattern")
        void shouldGenerateExactMatchDescription() {
            String result = humanizer.humanize("^EXACT$");
            assertEquals("Must be exactly 'EXACT'", result);
        }
        
        @Test
        @DisplayName("should generate description for letters only pattern")
        void shouldGenerateLettersOnlyDescription() {
            String result = humanizer.humanize("^[A-Za-z]+$");
            assertEquals("Only letters allowed", result);
        }
        
        @Test
        @DisplayName("should generate description for numbers only pattern")
        void shouldGenerateNumbersOnlyDescription() {
            String result = humanizer.humanize("^[0-9]+$");
            assertEquals("Only numbers allowed", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            String result = humanizer.humanize((Pattern) null);
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("should handle empty pattern string")
        void shouldHandleEmptyPatternString() {
            String result = humanizer.humanize("");
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("should show pattern for complex regex")
        void shouldShowPatternForComplexRegex() {
            String complex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
            String result = humanizer.humanize(complex);
            assertEquals("Must match pattern: " + complex, result);
        }
    }
    
    @Nested
    @DisplayName("Custom Patterns")
    class CustomPatterns {
        
        @Test
        @DisplayName("should use registered custom pattern")
        void shouldUseRegisteredCustomPattern() {
            // Given
            String pattern = "^CUSTOM-\\d{4}$";
            String description = "Must match format CUSTOM-XXXX (4 digits)";
            
            // When
            humanizer.registerPattern(pattern, description);
            String result = humanizer.humanize(pattern);
            
            // Then
            assertEquals(description, result);
        }
    }
}