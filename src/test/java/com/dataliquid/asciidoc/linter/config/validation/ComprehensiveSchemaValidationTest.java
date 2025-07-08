package com.dataliquid.asciidoc.linter.config.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationException;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;

/**
 * Comprehensive schema validation tests for all block types.
 * These tests ensure that schema validation is working correctly and catching errors.
 * 
 * IMPORTANT: This test class uses ConfigurationLoader() with schema validation ENABLED.
 * This is different from most other test classes which skip validation for performance.
 */
@DisplayName("Comprehensive Schema Validation Tests")
class ComprehensiveSchemaValidationTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        // IMPORTANT: Schema validation is ENABLED (not using ConfigurationLoader(true))
        loader = new ConfigurationLoader();
    }
    
    @Nested
    @DisplayName("All Block Types Validation")
    class AllBlockTypesValidation {
        
        @Test
        @DisplayName("should validate configuration with all block types")
        void shouldValidateConfigurationWithAllBlockTypes() throws IOException {
            // Given - configuration with all supported block types
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        required: true
                        severity: error
                  sections:
                    - name: "comprehensive"
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - paragraph:
                            severity: error
                            occurrence:
                              min: 0
                              max: 10
                        - listing:
                            severity: warn
                            occurrence:
                              min: 0
                              max: 5
                            language:
                              required: true
                              allowed: ["java", "python"]
                        - table:
                            severity: info
                            occurrence:
                              min: 0
                              max: 3
                            columns:
                              min: 2
                              max: 10
                        - image:
                            severity: error
                            occurrence:
                              min: 0
                              max: 5
                            url:
                              required: true
                              pattern: ".*\\\\.(jpg|png|gif)$"
                        - verse:
                            severity: warn
                            occurrence:
                              min: 0
                              max: 2
                            author:
                              required: false
                        - admonition:
                            severity: info
                            occurrence:
                              min: 0
                              max: 10
                            type:
                              required: true
                              allowed: ["NOTE", "TIP", "WARNING"]
                        - pass:
                            severity: error
                            occurrence:
                              min: 0
                              max: 1
                            type:
                              required: true
                              allowed: ["html", "xml"]
                        - literal:
                            severity: warn
                            occurrence:
                              min: 0
                              max: 5
                            lines:
                              min: 1
                              max: 100
                        - quote:
                            severity: info
                            occurrence:
                              min: 0
                              max: 3
                            author:
                              required: false
                        - sidebar:
                            severity: error
                            occurrence:
                              min: 0
                              max: 2
                            title:
                              required: false
                        - example:
                            severity: warn
                            occurrence:
                              min: 0
                              max: 5
                            caption:
                              required: false
                        - audio:
                            severity: info
                            occurrence:
                              min: 0
                              max: 3
                            url:
                              required: true
                        - video:
                            severity: error
                            occurrence:
                              min: 0
                              max: 3
                            url:
                              required: true
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then - should load without schema validation errors
            LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(input));
            
            // Verify configuration loaded correctly
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().sections());
            assertTrue(config.document().sections().size() > 0);
        }
        
        @Test
        @DisplayName("should detect invalid block type in schema")
        void shouldDetectInvalidBlockType() {
            // Given - configuration with invalid block type
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - invalid_block_type:
                            severity: error
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then - should throw exception for invalid block type
            assertThrows(ConfigurationException.class, () -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should detect missing required severity in block")
        void shouldDetectMissingRequiredSeverity() {
            // Given - paragraph block without required severity
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - paragraph:
                            occurrence:
                              min: 1
                              max: 5
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then - should throw exception for missing severity
            assertThrows(ConfigurationException.class, () -> loader.loadConfiguration(input));
        }
    }
    
    @Nested
    @DisplayName("Individual Block Type Schema Validation")
    class IndividualBlockTypeValidation {
        
        @Test
        @DisplayName("should validate paragraph block schema")
        void shouldValidateParagraphBlockSchema() throws IOException {
            // Given
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - paragraph:
                            severity: error
                            lines:
                              min: 1
                              max: 50
                            sentence:
                              occurrence:
                                min: 1
                                max: 10
                              words:
                                min: 5
                                max: 30
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertDoesNotThrow(() -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should validate listing block schema")
        void shouldValidateListingBlockSchema() throws IOException {
            // Given
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - listing:
                            severity: warn
                            language:
                              required: true
                              allowed: ["java", "python", "javascript"]
                            title:
                              required: false
                              pattern: "^Listing \\\\d+:.*"
                            callouts:
                              allowed: true
                              max: 20
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertDoesNotThrow(() -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should validate admonition block schema")
        void shouldValidateAdmonitionBlockSchema() throws IOException {
            // Given
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - admonition:
                            severity: info
                            type:
                              required: true
                              allowed: ["NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION"]
                            title:
                              required: true
                              minLength: 3
                              maxLength: 50
                            icon:
                              required: true
                              pattern: "^(info|warning|caution|tip|note)$"
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertDoesNotThrow(() -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should validate video block schema")
        void shouldValidateVideoBlockSchema() throws IOException {
            // Given
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - video:
                            severity: error
                            url:
                              required: true
                              pattern: ".*\\\\.(mp4|webm|ogg)$"
                            width:
                              required: false
                              minValue: 100
                              maxValue: 1920
                            height:
                              required: false
                              minValue: 100
                              maxValue: 1080
                            poster:
                              required: false
                              pattern: ".*\\\\.(jpg|jpeg|png)$"
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertDoesNotThrow(() -> loader.loadConfiguration(input));
        }
    }
    
    @Nested
    @DisplayName("Schema Error Detection")
    class SchemaErrorDetection {
        
        @Test
        @DisplayName("should detect invalid enum value for severity")
        void shouldDetectInvalidEnumValueForSeverity() {
            // Given - invalid severity value
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - paragraph:
                            severity: critical  # Invalid - should be error, warn, or info
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should detect invalid property in block configuration")
        void shouldDetectInvalidPropertyInBlockConfiguration() {
            // Given - invalid property for paragraph block
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - paragraph:
                            severity: error
                            invalidProperty: true  # This property doesn't exist in schema
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> loader.loadConfiguration(input));
        }
        
        @Test
        @DisplayName("should detect type mismatch in configuration")
        void shouldDetectTypeMismatchInConfiguration() {
            // Given - string instead of integer
            String yaml = """
                document:
                  sections:
                    - name: "test"
                      allowedBlocks:
                        - table:
                            severity: error
                            columns:
                              min: "two"  # Should be integer, not string
                              max: 10
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> loader.loadConfiguration(input));
        }
    }
    
    @Nested
    @DisplayName("Complex Configuration Validation")
    class ComplexConfigurationValidation {
        
        @Test
        @DisplayName("should validate nested sections with multiple block types")
        void shouldValidateNestedSectionsWithMultipleBlockTypes() throws IOException {
            // Given - complex nested configuration
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
                  sections:
                    - name: "introduction"
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - paragraph:
                            severity: error
                            occurrence:
                              min: 1
                              max: 3
                      subsections:
                        - name: "overview"
                          level: 2
                          min: 0
                          max: 1
                          allowedBlocks:
                            - paragraph:
                                severity: warn
                            - image:
                                severity: info
                                url:
                                  required: true
                    - name: "main"
                      level: 1
                      min: 1
                      max: 5
                      allowedBlocks:
                        - paragraph:
                            severity: error
                        - listing:
                            severity: warn
                            language:
                              required: true
                        - table:
                            severity: info
                      subsections:
                        - level: 2
                          min: 0
                          max: 10
                          allowedBlocks:
                            - paragraph:
                                severity: warn
                            - admonition:
                                severity: info
                                type:
                                  required: true
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When & Then
            LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(input));
            
            // Verify structure
            assertNotNull(config);
            assertTrue(config.document().sections().size() >= 2);
        }
    }
}