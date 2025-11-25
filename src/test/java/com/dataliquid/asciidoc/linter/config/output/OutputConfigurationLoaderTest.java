package com.dataliquid.asciidoc.linter.config.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for OutputConfigurationLoader.
 */
@DisplayName("OutputConfigurationLoader Tests")
class OutputConfigurationLoaderTest {

    private OutputConfigurationLoader loader;
    private OutputConfigurationLoader loaderWithoutValidation;

    @BeforeEach
    void setUp() {
        loader = new OutputConfigurationLoader();
        loaderWithoutValidation = new OutputConfigurationLoader(true);
    }

    @Nested
    @DisplayName("File Loading Tests")
    class FileLoadingTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("should load valid configuration from file")
        void shouldLoadValidConfigurationFromFile() throws IOException {
            // Given
            String yaml = """
                    output:
                      format: enhanced
                      display:
                        useColors: true
                        contextLines: 3
                        showLineNumbers: true
                        highlightStyle: underline
                    """;

            Path configFile = tempDir.resolve("output.yaml");
            Files.writeString(configFile, yaml);

            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(configFile.toString());

            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertEquals(3, config.getDisplay().getContextLines());
            assertTrue(config.getDisplay().isShowLineNumbers());
            assertEquals(HighlightStyle.UNDERLINE, config.getDisplay().getHighlightStyle());
        }

        @Test
        @DisplayName("should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile() {
            // When/Then
            assertThrows(IOException.class, () -> loader.loadConfiguration("/non/existent/file.yaml"));
        }
    }

    @Nested
    @DisplayName("Stream Loading Tests")
    class StreamLoadingTests {
        @Test
        @DisplayName("should load minimal configuration")
        void shouldLoadMinimalConfiguration() throws IOException {
            // Given
            String yaml = """
                    output:
                      format: simple
                    """;

            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(input);

            // Then
            assertEquals(OutputFormat.SIMPLE, config.getFormat());
            assertNotNull(config.getDisplay());
            assertNotNull(config.getErrorGrouping());
            assertNotNull(config.getSummary());
        }

        @Test
        @DisplayName("should load compact configuration")
        void shouldLoadCompactConfiguration() throws IOException {
            // Given
            String yaml = """
                    output:
                      format: compact
                      display:
                        useColors: false
                        contextLines: 0
                        highlightStyle: none
                      errorGrouping:
                        enabled: false
                      summary:
                        enabled: false
                    """;

            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(input);

            // Then
            assertEquals(OutputFormat.COMPACT, config.getFormat());
            assertFalse(config.getDisplay().isUseColors());
            assertEquals(0, config.getDisplay().getContextLines());
            assertEquals(HighlightStyle.NONE, config.getDisplay().getHighlightStyle());
            assertFalse(config.getErrorGrouping().isEnabled());
            assertFalse(config.getSummary().isEnabled());
        }
    }

    @Nested
    @DisplayName("Predefined Configuration Tests")
    class PredefinedConfigurationTests {

        @Test
        @DisplayName("should load predefined enhanced configuration")
        void shouldLoadPredefinedEnhancedConfiguration() throws IOException {
            // When
            OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.ENHANCED);

            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertTrue(config.getDisplay().isShowLineNumbers());
            assertTrue(config.getSummary().isEnabled());
        }

        @Test
        @DisplayName("should load predefined simple configuration")
        void shouldLoadPredefinedSimpleConfiguration() throws IOException {
            // When
            OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.SIMPLE);

            // Then
            assertEquals(OutputFormat.SIMPLE, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertTrue(config.getDisplay().isShowLineNumbers());
            assertTrue(config.getSummary().isEnabled());
        }

        @Test
        @DisplayName("should load predefined compact configuration")
        void shouldLoadPredefinedCompactConfiguration() throws IOException {
            // When
            OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.COMPACT);

            // Then
            assertEquals(OutputFormat.COMPACT, config.getFormat());
            assertFalse(config.getDisplay().isUseColors());
            assertFalse(config.getDisplay().isShowLineNumbers());
            assertFalse(config.getSummary().isEnabled());
        }

        @Test
        @DisplayName("should throw exception for non-existent predefined configuration")
        void shouldThrowExceptionForNonExistentPredefinedConfiguration() {
            // When/Then
            // Test with an invalid format string using reflection or a non-enum value
            assertThrows(IllegalArgumentException.class, () -> OutputFormat.fromValue("non-existent"));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should fail validation for invalid context lines")
        void shouldFailValidationForInvalidContextLines() {
            // Given
            String yaml = """
                    output:
                      format: enhanced
                      display:
                        contextLines: 20
                    """;

            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            // When/Then
            assertThrows(OutputConfigurationException.class, () -> loader.loadConfiguration(input));
        }

    }
}
