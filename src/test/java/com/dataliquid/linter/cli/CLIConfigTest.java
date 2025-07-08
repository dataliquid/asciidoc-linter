package com.dataliquid.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.cli.CLIConfig;
import com.dataliquid.asciidoc.linter.config.Severity;

@DisplayName("CLIConfig")
class CLIConfigTest {
    
    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        
        @Test
        @DisplayName("should create config with required fields")
        void shouldCreateConfigWithRequiredFields() {
            // Given
            List<String> patterns = Arrays.asList("**/*.adoc", "docs/**/*.asciidoc");
            
            // When
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(patterns)
                .build();
            
            // Then
            assertEquals(patterns, config.getInputPatterns());
            assertNotNull(config.getBaseDirectory());
            assertEquals(System.getProperty("user.dir"), config.getBaseDirectory().toString());
            assertEquals("console", config.getReportFormat());
            assertEquals(Severity.ERROR, config.getFailLevel());
            assertNull(config.getConfigFile());
            assertNull(config.getReportOutput());
            assertFalse(config.isOutputToFile());
        }
        
        @Test
        @DisplayName("should create config with all fields")
        void shouldCreateConfigWithAllFields() {
            // Given
            List<String> patterns = Arrays.asList("src/**/*.adoc", "test/**/*.adoc");
            Path baseDir = Paths.get("/custom/base");
            Path configFile = Paths.get("config.yaml");
            Path reportOutput = Paths.get("report.json");
            
            // When
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(patterns)
                .baseDirectory(baseDir)
                .configFile(configFile)
                .reportFormat("json")
                .reportOutput(reportOutput)
                .failLevel(Severity.WARN)
                .build();
            
            // Then
            assertEquals(patterns, config.getInputPatterns());
            assertEquals(baseDir, config.getBaseDirectory());
            assertEquals(configFile, config.getConfigFile());
            assertEquals("json", config.getReportFormat());
            assertEquals(reportOutput, config.getReportOutput());
            assertEquals(Severity.WARN, config.getFailLevel());
            assertTrue(config.isOutputToFile());
        }
        
        @Test
        @DisplayName("should throw exception for null input patterns")
        void shouldThrowExceptionForNullInputPatterns() {
            assertThrows(NullPointerException.class, () -> 
                CLIConfig.builder().build()
            );
        }
        
        @Test
        @DisplayName("should throw exception for empty input patterns")
        void shouldThrowExceptionForEmptyInputPatterns() {
            assertThrows(IllegalArgumentException.class, () -> 
                CLIConfig.builder()
                    .inputPatterns(Arrays.asList())
                    .build()
            );
        }
        
        @Test
        @DisplayName("should have sensible defaults")
        void shouldHaveSensibleDefaults() {
            // Given
            CLIConfig.Builder builder = CLIConfig.builder();
            
            // When
            CLIConfig config = builder
                .inputPatterns(Arrays.asList("*.adoc"))
                .build();
            
            // Then
            assertEquals("console", config.getReportFormat());
            assertEquals(Severity.ERROR, config.getFailLevel());
            assertNotNull(config.getBaseDirectory());
            assertNull(config.getConfigFile());
            assertNull(config.getReportOutput());
        }
    }
    
    @Nested
    @DisplayName("Getters")
    class GettersTest {
        
        @Test
        @DisplayName("should return correct values")
        void shouldReturnCorrectValues() {
            // Given
            List<String> patterns = Arrays.asList("**/*.adoc");
            Path configFile = Paths.get("config.yaml");
            Path reportOutput = Paths.get("output.json");
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(patterns)
                .configFile(configFile)
                .reportFormat("json-compact")
                .reportOutput(reportOutput)
                .failLevel(Severity.INFO)
                .build();
            
            // Then
            assertEquals(patterns, config.getInputPatterns());
            assertEquals(configFile, config.getConfigFile());
            assertEquals("json-compact", config.getReportFormat());
            assertEquals(reportOutput, config.getReportOutput());
            assertEquals(Severity.INFO, config.getFailLevel());
            assertTrue(config.isOutputToFile());
        }
        
        @Test
        @DisplayName("should return false for isOutputToFile when reportOutput is null")
        void shouldReturnFalseForIsOutputToFileWhenReportOutputIsNull() {
            // Given
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("*.adoc"))
                .build();
            
            // Then
            assertFalse(config.isOutputToFile());
        }
    }
}