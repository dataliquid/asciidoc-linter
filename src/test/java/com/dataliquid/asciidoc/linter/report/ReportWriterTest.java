package com.dataliquid.asciidoc.linter.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

@DisplayName("ReportWriter")
class ReportWriterTest {
    
    private ReportWriter writer;
    private ValidationResult sampleResult;
    
    @BeforeEach
    void setUp() {
        writer = new ReportWriter();
        
        sampleResult = ValidationResult.builder()
            .addMessage(ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test-rule")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .startLine(10)
                    .build())
                .message("Test error")
                .build())
            .complete()
            .build();
    }
    
    @Nested
    @DisplayName("Formatter Registration")
    class FormatterRegistration {
        
        @Test
        @DisplayName("should have default formatters registered")
        void shouldHaveDefaultFormattersRegistered() {
            Set<String> formats = writer.getAvailableFormats();
            // Console formatter is created dynamically now
            assertTrue(formats.contains("json"));
            assertTrue(formats.contains("json-compact"));
        }
        
        @Test
        @DisplayName("should register custom formatter")
        void shouldRegisterCustomFormatter() {
            // Given
            ReportFormatter customFormatter = new ReportFormatter() {
                @Override
                public void format(ValidationResult result, java.io.PrintWriter writer) {
                    writer.println("CUSTOM");
                }
                
                @Override
                public String getName() {
                    return "custom";
                }
            };
            
            // When
            writer.registerFormatter(customFormatter);
            
            // Then
            assertTrue(writer.getAvailableFormats().contains("custom"));
        }
        
        @Test
        @DisplayName("should throw exception for null formatter")
        void shouldThrowExceptionForNullFormatter() {
            assertThrows(NullPointerException.class, () -> writer.registerFormatter(null));
        }
    }
    
    @Nested
    @DisplayName("Console Output")
    class ConsoleOutput {
        
        @Test
        @DisplayName("should write to console with default format")
        void shouldWriteToConsoleWithDefaultFormat() throws IOException {
            // When & Then - no exception thrown
            assertDoesNotThrow(() -> writer.write(sampleResult, null, (String) null));
        }
        
        @Test
        @DisplayName("should write to console with explicit format")
        void shouldWriteToConsoleWithExplicitFormat() throws IOException {
            // When & Then - no exception thrown
            assertDoesNotThrow(() -> writer.write(sampleResult, "json", (String) null));
        }
    }
    
    @Nested
    @DisplayName("File Output")
    class FileOutput {
        
        @TempDir
        Path tempDir;
        
        @Test
        @DisplayName("should write console format to file")
        void shouldWriteConsoleFormatToFile() throws IOException {
            // Given
            Path outputFile = tempDir.resolve("report.txt");
            
            // When
            writer.write(sampleResult, "console", outputFile.toString());
            
            // Then
            assertTrue(Files.exists(outputFile));
            String content = Files.readString(outputFile);
            assertTrue(content.contains("Validation Report"));
            assertTrue(content.contains("Test error"));
        }
        
        @Test
        @DisplayName("should write JSON format to file")
        void shouldWriteJsonFormatToFile() throws IOException {
            // Given
            Path outputFile = tempDir.resolve("report.json");
            
            // When
            writer.write(sampleResult, "json", outputFile.toString());
            
            // Then
            assertTrue(Files.exists(outputFile));
            String content = Files.readString(outputFile);
            assertTrue(content.contains("{"));
            assertTrue(content.contains("\"severity\" : \"ERROR\""));
            assertTrue(content.contains("\"message\" : \"Test error\""));
        }
        
        @Test
        @DisplayName("should accept Path object")
        void shouldAcceptPathObject() throws IOException {
            // Given
            Path outputFile = tempDir.resolve("report-path.txt");
            
            // When
            writer.write(sampleResult, "console", outputFile);
            
            // Then
            assertTrue(Files.exists(outputFile));
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("should throw exception for unsupported format")
        void shouldThrowExceptionForUnsupportedFormat() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> writer.write(sampleResult, "unknown", (String) null)
            );
            
            assertTrue(exception.getMessage().contains("Unsupported format: unknown"));
            assertTrue(exception.getMessage().contains("Available formats:"));
        }
        
        @Test
        @DisplayName("should throw exception for null result")
        void shouldThrowExceptionForNullResult() {
            assertThrows(NullPointerException.class,
                () -> writer.write(null, "console", (String) null));
        }
        
        @Test
        @DisplayName("should handle invalid file path")
        void shouldHandleInvalidFilePath() {
            assertThrows(IOException.class,
                () -> writer.write(sampleResult, "json", "/invalid/path/report.json"));
        }
    }
    
    @Nested
    @DisplayName("Exit Code Calculation")
    class ExitCodeCalculation {
        
        @Test
        @DisplayName("should return 0 for no errors")
        void shouldReturnZeroForNoErrors() {
            ValidationResult result = ValidationResult.builder()
                .addMessage(ValidationMessage.builder()
                    .severity(Severity.WARN)
                    .ruleId("test-rule")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .startLine(1)
                        .build())
                    .message("Warning")
                    .build())
                .complete()
                .build();
            
            assertEquals(0, ReportWriter.calculateExitCode(result));
        }
        
        @Test
        @DisplayName("should return 1 for errors")
        void shouldReturnOneForErrors() {
            assertEquals(1, ReportWriter.calculateExitCode(sampleResult));
        }
        
        @Test
        @DisplayName("should return 0 for empty result")
        void shouldReturnZeroForEmptyResult() {
            ValidationResult emptyResult = ValidationResult.builder()
                .complete()
                .build();
            
            assertEquals(0, ReportWriter.calculateExitCode(emptyResult));
        }
    }
}