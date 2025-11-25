package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("ValidationMessage")
class ValidationMessageTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder().filename("test.adoc").line(10).build();
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("should create message with required fields")
        void shouldCreateMessageWhenAllRequiredFieldsProvided() {
            // Given
            Severity severity = Severity.ERROR;
            String ruleId = "metadata.required";
            String message = "Missing required attribute";

            // When
            ValidationMessage validationMessage = ValidationMessage
                    .builder()
                    .severity(severity)
                    .ruleId(ruleId)
                    .message(message)
                    .location(testLocation)
                    .build();

            // Then
            assertEquals(severity, validationMessage.getSeverity());
            assertEquals(ruleId, validationMessage.getRuleId());
            assertEquals(message, validationMessage.getMessage());
            assertEquals(testLocation, validationMessage.getLocation());
        }

        @Test
        @DisplayName("should create message with all fields")
        void shouldCreateMessageWhenAllFieldsProvided() {
            // Given
            Severity severity = Severity.WARN;
            String ruleId = "metadata.pattern";
            String messageText = "Invalid format";
            String attributeName = "author";
            String actualValue = "john";
            String expectedValue = "Pattern '^[A-Z].*'";

            // When
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(severity)
                    .ruleId(ruleId)
                    .message(messageText)
                    .location(testLocation)
                    .attributeName(attributeName)
                    .actualValue(actualValue)
                    .expectedValue(expectedValue)
                    .build();

            // Then
            assertTrue(message.getAttributeName().isPresent());
            assertEquals(attributeName, message.getAttributeName().get());
            assertTrue(message.getActualValue().isPresent());
            assertEquals(actualValue, message.getActualValue().get());
            assertTrue(message.getExpectedValue().isPresent());
            assertEquals(expectedValue, message.getExpectedValue().get());
        }

        @Test
        @DisplayName("should require severity")
        void shouldThrowExceptionWhenSeverityNotProvided() {
            // Given
            String ruleId = "test";
            String message = "test";

            // When & Then
            assertThrows(NullPointerException.class,
                    () -> ValidationMessage.builder().ruleId(ruleId).message(message).location(testLocation).build());
        }

        @Test
        @DisplayName("should require location")
        void shouldThrowExceptionWhenLocationNotProvided() {
            // Given
            Severity severity = Severity.ERROR;
            String ruleId = "test";
            String message = "test";

            // When & Then
            assertThrows(NullPointerException.class,
                    () -> ValidationMessage.builder().severity(severity).ruleId(ruleId).message(message).build());
        }
    }

    @Nested
    @DisplayName("Formatting")
    class FormattingTest {

        @Test
        @DisplayName("should format simple message")
        void shouldFormatMessageWhenOnlyRequiredFieldsProvided() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("metadata.required")
                    .message("Missing required attribute 'author': actual not present, expected non-empty value")
                    .location(testLocation)
                    .build();

            // When
            String formatted = message.format();

            // Then
            assertTrue(formatted.contains("test.adoc:10"));
            assertTrue(formatted.contains("[ERROR]"));
            assertTrue(formatted.contains("Missing required attribute 'author'"));
        }

        @Test
        @DisplayName("should format message with actual and expected values")
        void shouldFormatMessageWhenActualAndExpectedValuesProvided() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .ruleId("metadata.pattern")
                    .message("Invalid format")
                    .location(testLocation)
                    .actualValue("john")
                    .expectedValue("Pattern '^[A-Z].*'")
                    .build();

            // When
            String formatted = message.format();

            // Then
            assertTrue(formatted.contains("Found: \"john\""));
            assertTrue(formatted.contains("Expected: Pattern '^[A-Z].*'"));
        }

        @Test
        @DisplayName("should format message with only actual value")
        void shouldFormatMessageWhenOnlyActualValueProvided() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.INFO)
                    .ruleId("metadata.length")
                    .message("Value detected")
                    .location(testLocation)
                    .actualValue("Some long text...")
                    .build();

            // When
            String formatted = message.format();

            // Then
            assertTrue(formatted.contains("Found: \"Some long text...\""));
            assertFalse(formatted.contains("Expected:"));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            Severity severity = Severity.ERROR;
            String ruleId = "test.rule";
            String messageText = "Test message";

            // When
            ValidationMessage message1 = ValidationMessage
                    .builder()
                    .severity(severity)
                    .ruleId(ruleId)
                    .message(messageText)
                    .location(testLocation)
                    .build();

            ValidationMessage message2 = ValidationMessage
                    .builder()
                    .severity(severity)
                    .ruleId(ruleId)
                    .message(messageText)
                    .location(testLocation)
                    .build();

            // Then
            assertEquals(message1, message2);
            assertEquals(message1.hashCode(), message2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different severities")
        void shouldNotBeEqualWhenSeveritiesDiffer() {
            // Given
            String ruleId = "test.rule";
            String messageText = "Test message";

            // When
            ValidationMessage message1 = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId(ruleId)
                    .message(messageText)
                    .location(testLocation)
                    .build();

            ValidationMessage message2 = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .ruleId(ruleId)
                    .message(messageText)
                    .location(testLocation)
                    .build();

            // Then
            assertNotEquals(message1, message2);
        }
    }
}
