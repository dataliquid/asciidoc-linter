package com.dataliquid.asciidoc.linter.config.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;

@DisplayName("ConfigurationLoader with Schema Validation")
class ConfigurationLoaderSchemaTest {

    @Nested
    @DisplayName("With Schema Validation Enabled")
    class WithSchemaValidation {

        private ConfigurationLoader loader;

        @BeforeEach
        void setUp() {
            loader = new ConfigurationLoader(); // Schema validation enabled by default
        }

        @Test
        @DisplayName("should load valid configuration")
        void shouldLoadValidConfiguration(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("valid-config.yaml");
            String yaml = """
                    document:
                      metadata:
                        attributes:
                          - name: title
                            required: true
                            severity: error
                          - name: author
                            required: true
                            severity: error
                    """;

            Files.writeString(configFile, yaml);

            LinterConfiguration config = loader.loadConfiguration(configFile);

            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().metadata());
            assertEquals(2, config.document().metadata().attributes().size());
        }

        @Test
        @DisplayName("should reject configuration with invalid severity")
        void shouldRejectInvalidSeverity(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("invalid-config.yaml");
            String yaml = """
                    document:
                      metadata:
                        attributes:
                          - name: title
                            required: true
                            severity: CRITICAL  # Invalid!
                    """;

            Files.writeString(configFile, yaml);

            ConfigurationException ex = assertThrows(ConfigurationException.class,
                    () -> loader.loadConfiguration(configFile));

            assertTrue(ex.getMessage().contains("does not match schema"));
            assertTrue(ex.getMessage().contains("severity"));
        }

        @Test
        @DisplayName("should reject configuration with missing required field")
        void shouldRejectMissingRequiredField(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("invalid-config.yaml");
            String yaml = """
                    document:
                      metadata:
                        attributes:
                          - name: title
                            # severity is required but missing
                    """;

            Files.writeString(configFile, yaml);

            ConfigurationException ex = assertThrows(ConfigurationException.class,
                    () -> loader.loadConfiguration(configFile));

            assertTrue(ex.getMessage().contains("does not match schema"));
            assertTrue(ex.getMessage().contains("severity"));
            assertTrue(ex.getMessage().contains("required"));
        }

        @Test
        @DisplayName("should reject configuration with invalid section level")
        void shouldRejectInvalidSectionLevel(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("invalid-config.yaml");
            String yaml = """
                    document:
                      sections:
                        - name: intro
                          level: -1  # Must be >= 0
                    """;

            Files.writeString(configFile, yaml);

            ConfigurationException ex = assertThrows(ConfigurationException.class,
                    () -> loader.loadConfiguration(configFile));

            assertTrue(ex.getMessage().contains("does not match schema"));
            assertTrue(ex.getMessage().contains("level"));
        }
    }

    @Nested
    @DisplayName("With Schema Validation Disabled")
    class WithoutSchemaValidation {

        private ConfigurationLoader loader;

        @BeforeEach
        void setUp() {
            loader = new ConfigurationLoader(true); // Skip schema validation
        }

        @Test
        @DisplayName("should still fail on invalid severity even when schema validation is disabled")
        void shouldFailOnInvalidSeverity(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("invalid-config.yaml");
            String yaml = """
                    document:
                      metadata:
                        attributes:
                          - name: title
                            required: true
                            severity: CRITICAL  # Invalid severity
                    """;

            Files.writeString(configFile, yaml);

            // ConfigurationLoader still validates severity values internally
            assertThrows(ConfigurationException.class, () -> {
                loader.loadConfiguration(configFile);
            });
        }
    }
}
