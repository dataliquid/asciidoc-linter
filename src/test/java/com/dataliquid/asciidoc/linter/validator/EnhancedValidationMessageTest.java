package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

/**
 * Tests for enhanced ValidationMessage features.
 */
@DisplayName("Enhanced ValidationMessage Tests")
class EnhancedValidationMessageTest {

    @Nested
    @DisplayName("Enhanced Fields Tests")
    class EnhancedFieldsTests {

        @Test
        @DisplayName("should create message with all enhanced fields")
        void shouldCreateMessageWithAllEnhancedFields() {
            // Given
            List<Suggestion> suggestions = Arrays
                    .asList(Suggestion.builder().description("Fix suggestion 1").addExample("Example code").build());

            List<String> contextLines = Arrays.asList("Line before", "Error line", "Line after");

            // When
            ValidationMessage message = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error")
                    .location(SourceLocation.builder().filename("test.adoc").line(10).startColumn(5).build())
                    .errorType(ErrorType.MISSING_VALUE).actualValue("actual").expectedValue("expected")
                    .missingValueHint("id").suggestions(suggestions).contextLines(contextLines).build();

            // Then
            assertEquals(ErrorType.MISSING_VALUE, message.getErrorType());
            assertEquals("actual", message.getActualValue().orElse(null));
            assertEquals("expected", message.getExpectedValue().orElse(null));
            assertEquals("id", message.getMissingValueHint());
            assertEquals(suggestions, message.getSuggestions());
            assertEquals(contextLines, message.getContextLines());
            assertTrue(message.hasSuggestions());
        }

        @Test
        @DisplayName("should handle null enhanced fields")
        void shouldHandleNullEnhancedFields() {
            // When
            ValidationMessage message = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .build();

            // Then
            assertEquals(ErrorType.GENERIC, message.getErrorType()); // Defaults to GENERIC
            assertTrue(message.getActualValue().isEmpty());
            assertTrue(message.getExpectedValue().isEmpty());
            assertNull(message.getMissingValueHint());
            assertTrue(message.getSuggestions().isEmpty());
            assertTrue(message.getContextLines().isEmpty());
            assertFalse(message.hasSuggestions());
        }
    }

    @Nested
    @DisplayName("Suggestion Tests")
    class SuggestionTests {

    }

    @Nested
    @DisplayName("Error Type Tests")
    class ErrorTypeTests {

        @Test
        @DisplayName("should support all error types")
        void shouldSupportAllErrorTypes() {
            for (ErrorType type : ErrorType.values()) {
                // When
                ValidationMessage message = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                        .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                        .errorType(type).build();

                // Then
                assertEquals(type, message.getErrorType());
            }
        }
    }

    @Nested
    @DisplayName("Builder Defense Tests")
    class BuilderDefenseTests {

        @Test
        @DisplayName("should make defensive copies of lists")
        void shouldMakeDefensiveCopiesOfLists() {
            // Given
            List<Suggestion> mutableSuggestions = Arrays.asList(Suggestion.builder().description("Suggestion").build());
            List<String> mutableContext = Arrays.asList("Line 1", "Line 2");

            // When
            ValidationMessage message = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .suggestions(mutableSuggestions).contextLines(mutableContext).build();

            // Then - returned lists should be defensive copies (mutable but separate)
            List<Suggestion> suggestions = message.getSuggestions();
            suggestions.add(Suggestion.builder().description("new").build());
            assertEquals(1, message.getSuggestions().size()); // Original unchanged

            List<String> contextLines = message.getContextLines();
            contextLines.add("new line");
            assertEquals(2, message.getContextLines().size()); // Original unchanged
        }

        @Test
        @DisplayName("should handle null lists in builder")
        void shouldHandleNullListsInBuilder() {
            // When
            ValidationMessage message = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .suggestions(null).contextLines(null).build();

            // Then
            assertNotNull(message.getSuggestions());
            assertNotNull(message.getContextLines());
            assertTrue(message.getSuggestions().isEmpty());
            assertTrue(message.getContextLines().isEmpty());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should consider enhanced fields in equals")
        void shouldConsiderEnhancedFieldsInEquals() {
            // Given
            ValidationMessage message1 = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .errorType(ErrorType.MISSING_VALUE).actualValue("actual").build();

            ValidationMessage message2 = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .errorType(ErrorType.MISSING_VALUE).actualValue("actual").build();

            ValidationMessage message3 = ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.rule")
                    .message("Test error").location(SourceLocation.builder().filename("test.adoc").line(10).build())
                    .errorType(ErrorType.INVALID_PATTERN) // Different
                    .actualValue("actual").build();

            // Then
            assertEquals(message1, message2);
            assertNotEquals(message1, message3);
            assertEquals(message1.hashCode(), message2.hashCode());
        }
    }
}
