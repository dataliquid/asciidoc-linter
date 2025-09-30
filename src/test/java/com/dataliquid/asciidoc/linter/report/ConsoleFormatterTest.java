package com.dataliquid.asciidoc.linter.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

@DisplayName("ConsoleFormatter")
class ConsoleFormatterTest {

    private ConsoleFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        // Create formatter with no colors for testing
        OutputConfiguration config = OutputConfiguration
                .builder()
                .format(OutputFormat.SIMPLE)
                .display(new DisplayConfig(null, null, false, null, null, null))
                .build();
        formatter = new ConsoleFormatter(config);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Nested
    @DisplayName("Basic Formatting")
    class BasicFormatting {

        @Test
        @DisplayName("should format empty result")
        void shouldFormatEmptyResult() {
            // Given
            ValidationResult result = ValidationResult.builder().complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("Validation Report"));
            assertTrue(output.contains("No validation issues found."));
            assertTrue(output.contains("Summary: 0 errors, 0 warnings, 0 info messages"));
        }

        @Test
        @DisplayName("should format single error message")
        void shouldFormatSingleErrorMessage() {
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
            assertTrue(output.contains("test.adoc:"));
            assertTrue(output.contains("Line 10, Column 1: [ERROR] Missing required attribute"));
            assertTrue(output.contains("Summary: 1 error, 0 warnings, 0 info messages"));
        }

        @Test
        @DisplayName("should return correct name")
        void shouldReturnCorrectName() {
            assertEquals("console", formatter.getName());
        }
    }

    @Nested
    @DisplayName("Message Details")
    class MessageDetails {

        @Test
        @DisplayName("should include rule ID when present")
        void shouldIncludeRuleId() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .location(SourceLocation.builder().filename("test.adoc").startLine(5).build())
                    .message("Line too long")
                    .ruleId("line-length")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("Rule: line-length"));
        }

        @Test
        @DisplayName("should include actual and expected values")
        void shouldIncludeActualAndExpectedValues() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("value-check")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(15).build())
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
            assertTrue(output.contains("Actual: 100"));
            assertTrue(output.contains("Expected: 80"));
        }

        @Test
        @DisplayName("should include column when present")
        void shouldIncludeColumn() {
            // Given
            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.INFO)
                    .ruleId("info-rule")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(20).startColumn(15).build())
                    .message("Info message")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(message).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("Line 20, Column 15: [INFO] Info message"));
        }
    }

    @Nested
    @DisplayName("Multiple Files")
    class MultipleFiles {

        @Test
        @DisplayName("should group messages by file")
        void shouldGroupMessagesByFile() {
            // Given
            ValidationMessage msg1 = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("file1.adoc").startLine(10).build())
                    .message("Error in file1")
                    .build();

            ValidationMessage msg2 = ValidationMessage
                    .builder()
                    .severity(Severity.WARN)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("file2.adoc").startLine(5).build())
                    .message("Warning in file2")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(msg1).addMessage(msg2).complete().build();

            // When
            formatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("file1.adoc:"));
            assertTrue(output.contains("file2.adoc:"));
            int file1Index = output.indexOf("file1.adoc:");
            int file2Index = output.indexOf("file2.adoc:");
            assertTrue(file1Index < file2Index); // Files should be sorted
        }
    }

    @Nested
    @DisplayName("Color Support")
    class ColorSupport {

        @Test
        @DisplayName("should add colors when enabled")
        void shouldAddColorsWhenEnabled() {
            // Given
            OutputConfiguration colorConfig = OutputConfiguration
                    .builder()
                    .format(OutputFormat.SIMPLE)
                    .display(new DisplayConfig(null, null, true, null, null, null))
                    .build();
            ConsoleFormatter colorFormatter = new ConsoleFormatter(colorConfig);

            ValidationMessage error = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder().filename("test.adoc").startLine(1).build())
                    .message("Error message")
                    .build();

            ValidationResult result = ValidationResult.builder().addMessage(error).complete().build();

            // When
            colorFormatter.format(result, printWriter);
            printWriter.flush();

            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\u001B[31m[ERROR]\u001B[0m")); // Red color
        }
    }
}
