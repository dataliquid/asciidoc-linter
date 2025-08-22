package com.dataliquid.asciidoc.linter.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for JSON compact formatting.
 *
 * <p>
 * This test class validates the behavior of the JSON compact formatter, which produces single-line JSON output suitable
 * for log processing and pipeline integration.
 * </p>
 *
 * @see JsonFormatter
 */
@DisplayName("JsonCompactFormatter")
class JsonCompactFormatterTest {

    private JsonFormatter formatter;
    private ObjectMapper objectMapper;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        formatter = JsonFormatter.compact();
        objectMapper = new ObjectMapper();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    @DisplayName("should have correct name")
    void shouldHaveCorrectName() {
        // Given/When
        String name = formatter.getName();

        // Then
        assertEquals("json-compact", name);
    }

    @Test
    @DisplayName("should format empty result as single-line JSON")
    void shouldFormatEmptyResultAsSingleLineJson() throws IOException {
        // Given
        ValidationResult result = ValidationResult.builder().startTime(System.currentTimeMillis() - 100).complete()
                .build();

        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();

        // Then
        assertNotNull(output);
        assertFalse(output.contains("\n"), "Output should not contain newlines");
        assertFalse(output.contains("  "), "Output should not contain indentation");

        // Parse and verify structure
        JsonNode json = objectMapper.readTree(output);
        assertTrue(json.has("timestamp"));
        assertTrue(json.has("duration"));
        assertTrue(json.has("summary"));
        assertTrue(json.has("messages"));

        JsonNode summary = json.get("summary");
        assertEquals(0, summary.get("totalMessages").asInt());
        assertEquals(0, summary.get("errors").asInt());
        assertEquals(0, summary.get("warnings").asInt());
        assertEquals(0, summary.get("infos").asInt());

        JsonNode messages = json.get("messages");
        assertTrue(messages.isArray());
        assertEquals(0, messages.size());
    }

    @Test
    @DisplayName("should format result with messages as single-line JSON")
    void shouldFormatResultWithMessagesAsSingleLineJson() throws IOException {
        // Given
        ValidationResult result = ValidationResult.builder().startTime(System.currentTimeMillis() - 250)
                .addMessages(Arrays.asList(
                        ValidationMessage.builder().severity(Severity.ERROR).ruleId("metadata.required")
                                .location(SourceLocation.builder().filename("test.adoc").startLine(1).startColumn(5)
                                        .build())
                                .message("Missing required attribute: title").actualValue("null")
                                .expectedValue("non-empty string").build(),
                        ValidationMessage.builder().severity(Severity.WARN).ruleId("section.order")
                                .location(SourceLocation.builder().filename("test.adoc").startLine(10).build())
                                .message("Section order violation").build(),
                        ValidationMessage.builder().severity(Severity.INFO).ruleId("general.info")
                                .location(SourceLocation.builder().filename("test.adoc").startLine(20).build())
                                .message("Consider adding description").build()))
                .complete().build();

        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();

        // Then
        assertNotNull(output);
        assertFalse(output.contains("\n"), "Output should not contain newlines");
        assertFalse(output.contains("  "), "Output should not contain indentation");

        // Parse and verify structure
        JsonNode json = objectMapper.readTree(output);

        // Verify summary
        JsonNode summary = json.get("summary");
        assertEquals(3, summary.get("totalMessages").asInt());
        assertEquals(1, summary.get("errors").asInt());
        assertEquals(1, summary.get("warnings").asInt());
        assertEquals(1, summary.get("infos").asInt());

        // Verify messages
        JsonNode messages = json.get("messages");
        assertEquals(3, messages.size());

        // Verify first message (error)
        JsonNode error = messages.get(0);
        assertEquals("test.adoc", error.get("file").asText());
        assertEquals(1, error.get("line").asInt());
        assertEquals(5, error.get("column").asInt());
        assertEquals("ERROR", error.get("severity").asText());
        assertEquals("Missing required attribute: title", error.get("message").asText());
        assertEquals("metadata.required", error.get("ruleId").asText());
        assertEquals("null", error.get("actualValue").asText());
        assertEquals("non-empty string", error.get("expectedValue").asText());

        // Verify second message (warning) - no column
        JsonNode warning = messages.get(1);
        assertEquals("test.adoc", warning.get("file").asText());
        assertEquals(10, warning.get("line").asInt());
        assertFalse(warning.has("column"));
        assertEquals("WARN", warning.get("severity").asText());
        assertEquals("section.order", warning.get("ruleId").asText());

        // Verify third message (info)
        JsonNode info = messages.get(2);
        assertEquals(20, info.get("line").asInt());
        assertEquals("INFO", info.get("severity").asText());
        assertEquals("general.info", info.get("ruleId").asText());
    }

    @Test
    @DisplayName("should format duration correctly")
    void shouldFormatDurationCorrectly() throws IOException {
        // Given - test with milliseconds
        ValidationResult result1 = ValidationResult.builder().startTime(System.currentTimeMillis() - 999).complete()
                .build();

        // When
        formatter.format(result1, printWriter);
        String output1 = stringWriter.toString();

        // Then
        JsonNode json1 = objectMapper.readTree(output1);
        assertEquals("999ms", json1.get("duration").asText());

        // Given - test with seconds
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        ValidationResult result2 = ValidationResult.builder().startTime(System.currentTimeMillis() - 1500).complete()
                .build();

        // When
        formatter.format(result2, printWriter);
        String output2 = stringWriter.toString();

        // Then
        JsonNode json2 = objectMapper.readTree(output2);
        assertEquals("1.500s", json2.get("duration").asText());
    }

    @Test
    @DisplayName("should handle messages without optional fields")
    void shouldHandleMessagesWithoutOptionalFields() throws IOException {
        // Given
        ValidationResult result = ValidationResult.builder().startTime(System.currentTimeMillis() - 50)
                .addMessages(Arrays.asList(ValidationMessage.builder().severity(Severity.ERROR).ruleId("basic.error")
                        .location(SourceLocation.builder().filename("test.adoc").startLine(1).build())
                        .message("Basic error").build()))
                .complete().build();

        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();

        // Then
        JsonNode json = objectMapper.readTree(output);
        JsonNode messages = json.get("messages");
        JsonNode message = messages.get(0);

        // Required fields
        assertTrue(message.has("file"));
        assertTrue(message.has("line"));
        assertTrue(message.has("severity"));
        assertTrue(message.has("message"));

        // Optional fields should not be present
        assertFalse(message.has("column"));
        assertTrue(message.has("ruleId")); // ruleId is always required in our implementation
        assertFalse(message.has("actualValue"));
        assertFalse(message.has("expectedValue"));
    }

    @Test
    @DisplayName("should escape special characters properly")
    void shouldEscapeSpecialCharactersProperly() throws IOException {
        // Given
        ValidationResult result = ValidationResult.builder().startTime(System.currentTimeMillis() - 100)
                .addMessages(Arrays.asList(ValidationMessage.builder().severity(Severity.ERROR).ruleId("test.escape")
                        .location(SourceLocation.builder().filename("test/with\"quotes\".adoc").startLine(1).build())
                        .message("Message with \"quotes\" and backslash\\").build()))
                .complete().build();

        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();

        // Then
        // Verify it's valid JSON
        JsonNode json = objectMapper.readTree(output);
        JsonNode messages = json.get("messages");
        JsonNode message = messages.get(0);

        // Jackson should handle escaping properly
        assertEquals("test/with\"quotes\".adoc", message.get("file").asText());
        assertEquals("Message with \"quotes\" and backslash\\", message.get("message").asText());

        // Raw output should contain escaped characters
        assertTrue(output.contains("\\\"quotes\\\""));
        assertTrue(output.contains("backslash\\\\"));
    }
}
