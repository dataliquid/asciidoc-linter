package com.dataliquid.asciidoc.linter.config.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("RuleSchemaValidator")
class RuleSchemaValidatorTest {
    
    private RuleSchemaValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new RuleSchemaValidator();
    }
    
    @Nested
    @DisplayName("Valid Configurations")
    class ValidConfigurations {
        
        @Test
        @DisplayName("should accept minimal valid configuration")
        void shouldAcceptMinimalValidConfiguration() {
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        required: true
                        severity: error
                """;
            
            assertDoesNotThrow(() -> 
                validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
        }
        
        @Test
        @DisplayName("should accept configuration with sections")
        void shouldAcceptConfigurationWithSections() {
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        required: true
                        severity: error
                  sections:
                    - name: introduction
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - paragraph:
                            severity: warn
                """;
            
            assertDoesNotThrow(() -> 
                validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
        }
        
        @Test
        @DisplayName("should accept all severity values")
        void shouldAcceptAllSeverityValues() {
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: attr1
                        severity: error
                      - name: attr2
                        severity: warn
                      - name: attr3
                        severity: info
                """;
            
            assertDoesNotThrow(() -> 
                validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
        }
    }
    
    @Nested
    @DisplayName("Invalid Configurations")
    class InvalidConfigurations {
        
        @Test
        @DisplayName("should reject invalid severity value")
        void shouldRejectInvalidSeverity() {
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        severity: CRITICAL
                """;
            
            RuleValidationException ex = assertThrows(
                RuleValidationException.class,
                () -> validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
            
            assertTrue(ex.getMessage().contains("severity"));
            assertTrue(ex.getMessage().contains("error, warn, info"));
        }
        
        @Test
        @DisplayName("should reject missing required fields")
        void shouldRejectMissingRequiredFields() {
            String yaml = """
                document:
                  sections:
                    - name: intro
                      # level is required but missing
                """;
            
            RuleValidationException ex = assertThrows(
                RuleValidationException.class,
                () -> validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
            
            assertTrue(ex.getMessage().contains("level"));
            assertTrue(ex.getMessage().contains("required"));
        }
        
        @Test
        @DisplayName("should reject invalid section level")
        void shouldRejectInvalidSectionLevel() {
            String yaml = """
                document:
                  sections:
                    - name: intro
                      level: -1  # Must be >= 0
                """;
            
            RuleValidationException ex = assertThrows(
                RuleValidationException.class,
                () -> validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
            
            assertTrue(ex.getMessage().contains("level"));
            assertTrue(ex.getMessage().contains("minimum"));
        }
        
        @Test
        @DisplayName("should reject empty document")
        void shouldRejectEmptyDocument() {
            String yaml = """
                # Empty configuration
                """;
            
            assertThrows(
                RuleValidationException.class,
                () -> validator.validateUserConfig(new ByteArrayInputStream(yaml.getBytes()))
            );
        }
    }
    
    @Nested
    @DisplayName("File Validation")
    class FileValidation {
        
        @Test
        @DisplayName("should validate configuration file")
        void shouldValidateConfigurationFile(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve("config.yaml");
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        required: true
                        severity: error
                      - name: author
                        pattern: "^[A-Z].*"
                        severity: warn
                """;
            
            Files.writeString(configFile, yaml);
            
            assertDoesNotThrow(() -> validator.validateUserConfig(configFile));
        }
        
        @Test
        @DisplayName("should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile(@TempDir Path tempDir) {
            Path nonExistent = tempDir.resolve("non-existent.yaml");
            
            RuleValidationException ex = assertThrows(
                RuleValidationException.class,
                () -> validator.validateUserConfig(nonExistent)
            );
            
            assertTrue(ex.getMessage().contains("not found"));
        }
    }
}