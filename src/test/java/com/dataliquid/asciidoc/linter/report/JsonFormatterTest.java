package com.dataliquid.asciidoc.linter.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;
import com.jayway.jsonpath.JsonPath;

@DisplayName("JsonFormatter")
class JsonFormatterTest {

    private JsonFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        formatter = JsonFormatter.pretty();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Nested
    @DisplayName("Basic JSON Structure")
    class BasicJsonStructure {

        @Test
        @DisplayName("should format empty result as valid JSON")
        void shouldFormatEmptyResultAsValidJson() {
            // Given
            ValidationResult result = ValidationResult.builder().complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertNotNull(JsonPath.read(output, "$.timestamp"));
            assertNotNull(JsonPath.read(output, "$.duration"));
            assertEquals(0, (int) JsonPath.read(output, "$.summary.totalMessages"));
            assertEquals(0, (int) JsonPath.read(output, "$.summary.errors"));
            assertEquals(0, (int) JsonPath.read(output, "$.summary.warnings"));
            assertEquals(0, (int) JsonPath.read(output, "$.summary.infos"));
            assertTrue(((List<?>) JsonPath.read(output, "$.messages")).isEmpty());
        }

        @Test
        @DisplayName("should format single message as valid JSON")
        void shouldFormatSingleMessageAsValidJson() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("required-attribute")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(10).build())
                    .message("Missing required attribute")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals(1, (int) JsonPath.read(output, "$.summary.totalMessages"));
            assertEquals(1, (int) JsonPath.read(output, "$.summary.errors"));
            assertEquals("test.adoc", JsonPath.read(output, "$.messages[0].file"));
            assertEquals(10, (int) JsonPath.read(output, "$.messages[0].line"));
            assertEquals("ERROR", JsonPath.read(output, "$.messages[0].severity"));
            assertEquals("Missing required attribute", JsonPath.read(output, "$.messages[0].message"));
        }

        @Test
        @DisplayName("should return correct name")
        void shouldReturnCorrectName() {
            assertEquals("json", formatter.getName());
        }
    }

    @Nested
    @DisplayName("JSON Escaping")
    class JsonEscaping {

        @Test
        @DisplayName("should escape special characters in messages")
        void shouldEscapeSpecialCharacters() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(5).build())
                    .message("Message with \"quotes\" and \nnewline")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals("Message with \"quotes\" and \nnewline", JsonPath.read(output, "$.messages[0].message"));
        }

        @Test
        @DisplayName("should escape backslashes")
        void shouldEscapeBackslashes() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.INFO)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("C:\\path\\to\\file.adoc").startLine(1).build())
                    .message("Path with backslashes")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals("C:\\path\\to\\file.adoc", JsonPath.read(output, "$.messages[0].file"));
        }
    }

    @Nested
    @DisplayName("Optional Fields")
    class OptionalFields {

        @Test
        @DisplayName("should include optional fields when present")
        void shouldIncludeOptionalFields() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("value-check")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(15).startColumn(20).build())
                    .message("Invalid value")
                    .actualValue("100")
                    .expectedValue("80")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals(20, (int) JsonPath.read(output, "$.messages[0].column"));
            assertEquals("value-check", JsonPath.read(output, "$.messages[0].ruleId"));
            assertEquals("100", JsonPath.read(output, "$.messages[0].actualValue"));
            assertEquals("80", JsonPath.read(output, "$.messages[0].expectedValue"));
        }
    }

    @Nested
    @DisplayName("Multiple Messages")
    class MultipleMessages {

        @Test
        @DisplayName("should format multiple messages with proper commas")
        void shouldFormatMultipleMessagesWithProperCommas() {
            // Given
            ValidationMessage msg1 = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("file1.adoc").startLine(10).build())
                    .message("First error")
                    .build();

            ValidationMessage msg2 = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("file2.adoc").startLine(20).build())
                    .message("Second warning")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(msg1).addMessage(msg2).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals(2, (int) JsonPath.read(output, "$.summary.totalMessages"));
            assertEquals(1, (int) JsonPath.read(output, "$.summary.errors"));
            assertEquals(1, (int) JsonPath.read(output, "$.summary.warnings"));

            List<String> messages = JsonPath.read(output, "$.messages[*].message");
            assertEquals(2, messages.size());
            assertTrue(messages.contains("First error"));
            assertTrue(messages.contains("Second warning"));
        }
    }

    @Nested
    @DisplayName("Duration Formatting")
    class DurationFormatting {

        @Test
        @DisplayName("should format duration in milliseconds when less than 1 second")
        void shouldFormatDurationInMilliseconds() {
            // Given
            ValidationResult result = ValidationResult.builder().startTime(1000).endTime(1500).build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals("500ms", JsonPath.read(output, "$.duration"));
        }

        @Test
        @DisplayName("should format duration in seconds when 1 second or more")
        void shouldFormatDurationInSeconds() {
            // Given
            ValidationResult result = ValidationResult.builder().startTime(1000).endTime(3500).build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertEquals("2.500s", JsonPath.read(output, "$.duration"));
        }
    }
}
