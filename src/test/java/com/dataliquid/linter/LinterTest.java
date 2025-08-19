package com.dataliquid.linter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.Linter;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

@DisplayName("Linter")
class LinterTest {
    
    private Linter linter;
    
    @BeforeEach
    void setUp() {
        linter = new Linter();
    }
    
    @AfterEach
    void tearDown() {
        linter.close();
    }
    
    @Nested
    @DisplayName("validateFile")
    class ValidateFileTest {
        
        @Test
        @DisplayName("should throw NullPointerException when file is null")
        void shouldThrowNullPointerExceptionWhenFileIsNull() {
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateFile(null, config),
                "file must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw NullPointerException when config is null")
        void shouldThrowNullPointerExceptionWhenConfigIsNull(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.adoc");
            Files.writeString(file, "= Test");
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateFile(file, null),
                "config must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw IOException when file does not exist")
        void shouldThrowIOExceptionWhenFileDoesNotExist(@TempDir Path tempDir) {
            Path nonExistentFile = tempDir.resolve("non-existent.adoc");
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            IOException exception = assertThrows(IOException.class, () -> 
                linter.validateFile(nonExistentFile, config)
            );
            
            assertTrue(exception.getMessage().contains("File does not exist"));
        }
        
        @Test
        @DisplayName("should throw IOException when path is directory")
        void shouldThrowIOExceptionWhenPathIsDirectory(@TempDir Path tempDir) {
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            IOException exception = assertThrows(IOException.class, () -> 
                linter.validateFile(tempDir, config)
            );
            
            assertTrue(exception.getMessage().contains("Not a regular file"));
        }
        
        @Test
        @DisplayName("should validate valid AsciiDoc file")
        void shouldValidateValidAsciiDocFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("valid.adoc");
            Files.writeString(file, """
                = Valid Document
                John Doe <john@example.com>
                v1.0, 2024-01-15
                
                == Introduction
                
                This is a valid document.
                """);
            
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            ValidationResult result = linter.validateFile(file, config);
            
            assertNotNull(result);
            assertFalse(result.hasErrors());
            assertFalse(result.hasWarnings());
        }
        
        @Test
        @DisplayName("should validate file with configuration")
        void shouldValidateFileWithConfiguration(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("document.adoc");
            Files.writeString(file, """
                = Document Title
                Author Name
                
                == Section
                
                Content here.
                """);
            
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            ValidationResult result = linter.validateFile(file, config);
            
            assertNotNull(result);
            // With empty config, no validation errors should occur
            assertEquals(0, result.getErrorCount());
        }
    }
    
    @Nested
    @DisplayName("validateFiles")
    class ValidateFilesTest {
        
        @Test
        @DisplayName("should throw NullPointerException when files is null")
        void shouldThrowNullPointerExceptionWhenFilesIsNull() {
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateFiles(null, config),
                "files must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw NullPointerException when config is null")
        void shouldThrowNullPointerExceptionWhenConfigIsNull() {
            List<Path> files = List.of();
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateFiles(files, null),
                "config must not be null"
            );
        }
        
        @Test
        @DisplayName("should return empty map for empty file list")
        void shouldReturnEmptyMapForEmptyFileList() {
            List<Path> files = List.of();
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateFiles(files, config);
            
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
        
        @Test
        @DisplayName("should validate multiple files")
        void shouldValidateMultipleFiles(@TempDir Path tempDir) throws IOException {
            Path file1 = tempDir.resolve("file1.adoc");
            Files.writeString(file1, "= Document 1\n\nContent 1");
            
            Path file2 = tempDir.resolve("file2.adoc");
            Files.writeString(file2, "= Document 2\n\nContent 2");
            
            List<Path> files = List.of(file1, file2);
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateFiles(files, config);
            
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.containsKey(file1));
            assertTrue(results.containsKey(file2));
        }
        
        @Test
        @DisplayName("should create error result for non-existent file")
        void shouldCreateErrorResultForNonExistentFile(@TempDir Path tempDir) throws IOException {
            Path existingFile = tempDir.resolve("existing.adoc");
            Files.writeString(existingFile, "= Existing\n\nContent");
            
            Path nonExistentFile = tempDir.resolve("non-existent.adoc");
            
            List<Path> files = List.of(existingFile, nonExistentFile);
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateFiles(files, config);
            
            assertNotNull(results);
            assertEquals(2, results.size());
            
            ValidationResult errorResult = results.get(nonExistentFile);
            assertNotNull(errorResult);
            assertTrue(errorResult.hasErrors());
            assertEquals(1, errorResult.getErrorCount());
            
            List<ValidationMessage> messages = errorResult.getMessages();
            assertEquals(1, messages.size());
            assertEquals("io-error", messages.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("validateDirectory")
    class ValidateDirectoryTest {
        
        @Test
        @DisplayName("should throw NullPointerException when directory is null")
        void shouldThrowNullPointerExceptionWhenDirectoryIsNull() {
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateDirectory(null, "*.adoc", false, config),
                "directory must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw NullPointerException when pattern is null")
        void shouldThrowNullPointerExceptionWhenPatternIsNull(@TempDir Path tempDir) {
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            assertThrows(NullPointerException.class, () -> 
                linter.validateDirectory(tempDir, null, false, config),
                "pattern must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw NullPointerException when config is null")
        void shouldThrowNullPointerExceptionWhenConfigIsNull(@TempDir Path tempDir) {
            assertThrows(NullPointerException.class, () -> 
                linter.validateDirectory(tempDir, "*.adoc", false, null),
                "config must not be null"
            );
        }
        
        @Test
        @DisplayName("should throw IOException when path is not directory")
        void shouldThrowIOExceptionWhenPathIsNotDirectory(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("file.txt");
            Files.writeString(file, "content");
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            IOException exception = assertThrows(IOException.class, () -> 
                linter.validateDirectory(file, "*.adoc", false, config)
            );
            
            assertTrue(exception.getMessage().contains("Not a directory"));
        }
        
        @Test
        @DisplayName("should find and validate matching files non-recursively")
        void shouldFindAndValidateMatchingFilesNonRecursively(@TempDir Path tempDir) throws IOException {
            Path file1 = tempDir.resolve("test1.adoc");
            Files.writeString(file1, "= Test 1");
            
            Path file2 = tempDir.resolve("test2.adoc");
            Files.writeString(file2, "= Test 2");
            
            Path file3 = tempDir.resolve("other.txt");
            Files.writeString(file3, "Other content");
            
            Path subDir = tempDir.resolve("subdir");
            Files.createDirectory(subDir);
            Path file4 = subDir.resolve("test3.adoc");
            Files.writeString(file4, "= Test 3");
            
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateDirectory(tempDir, "*.adoc", false, config);
            
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.containsKey(file1));
            assertTrue(results.containsKey(file2));
            assertFalse(results.containsKey(file3));
            assertFalse(results.containsKey(file4));
        }
        
        @Test
        @DisplayName("should find and validate matching files recursively")
        void shouldFindAndValidateMatchingFilesRecursively(@TempDir Path tempDir) throws IOException {
            Path file1 = tempDir.resolve("test1.adoc");
            Files.writeString(file1, "= Test 1");
            
            Path subDir = tempDir.resolve("subdir");
            Files.createDirectory(subDir);
            Path file2 = subDir.resolve("test2.adoc");
            Files.writeString(file2, "= Test 2");
            
            Path deepDir = subDir.resolve("deep");
            Files.createDirectory(deepDir);
            Path file3 = deepDir.resolve("test3.adoc");
            Files.writeString(file3, "= Test 3");
            
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateDirectory(tempDir, "*.adoc", true, config);
            
            assertNotNull(results);
            assertEquals(3, results.size());
            assertTrue(results.containsKey(file1));
            assertTrue(results.containsKey(file2));
            assertTrue(results.containsKey(file3));
        }
        
        @Test
        @DisplayName("should return empty map when no files match pattern")
        void shouldReturnEmptyMapWhenNoFilesMatchPattern(@TempDir Path tempDir) throws IOException {
            Path file1 = tempDir.resolve("test.txt");
            Files.writeString(file1, "Text file");
            
            Path file2 = tempDir.resolve("other.md");
            Files.writeString(file2, "Markdown file");
            
            LinterConfiguration config = LinterConfiguration.builder().build();
            
            Map<Path, ValidationResult> results = linter.validateDirectory(tempDir, "*.adoc", false, config);
            
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("close")
    class CloseTest {
        
        @Test
        @DisplayName("should close without error")
        void shouldCloseWithoutError() {
            assertDoesNotThrow(() -> linter.close());
        }
        
        @Test
        @DisplayName("should handle multiple close calls")
        void shouldHandleMultipleCloseCalls() {
            assertDoesNotThrow(() -> {
                linter.close();
                linter.close();
            });
        }
    }
    
    @Nested
    @DisplayName("Integration")
    class IntegrationTest {
        
        @Test
        @DisplayName("should validate document with metadata and sections")
        void shouldValidateDocumentWithMetadataAndSections(@TempDir Path tempDir) throws IOException {
            Path adocFile = tempDir.resolve("document.adoc");
            Files.writeString(adocFile, """
                = Test Document
                John Doe <john@example.com>
                v1.0, 2024-01-15
                
                == Introduction
                
                This is the introduction section.
                
                == Main Content
                
                This is the main content.
                
                [source,java]
                ----
                public class Example {
                    // Code here
                }
                ----
                """);
            
            String configYaml = """
                document:
                  metadata:
                    attributes:
                      - name: author
                        required: true
                        pattern: "^[A-Z][a-zA-Z\\\\s]+$"
                        severity: error
                  sections:
                    - name: documentTitle
                      level: 0
                      occurrence:
                        min: 1
                        max: 1
                      title:
                        pattern: "^[A-Z].*"
                        severity: error
                    - name: introduction
                      level: 1
                      occurrence:
                        min: 1
                        max: 1
                      title:
                        pattern: "^Introduction$"
                        severity: error
                      allowedBlocks:
                        - paragraph:
                            severity: warn
                    - name: mainContent
                      level: 1
                      occurrence:
                        min: 1
                      title:
                        pattern: "^Main.*"
                        severity: error
                      allowedBlocks:
                        - paragraph:
                            severity: info
                        - listing:
                            severity: warn
                            language:
                              required: true
                              severity: error
                """;
            
            Path configFile = tempDir.resolve("config.yaml");
            Files.writeString(configFile, configYaml);
            
            ConfigurationLoader loader = new ConfigurationLoader();
            LinterConfiguration config = loader.loadConfiguration(configFile);
            
            ValidationResult result = linter.validateFile(adocFile, config);
            
            assertNotNull(result);
            // Just verify the validation completes without throwing exceptions
            // The actual validation logic is tested in individual validator tests
            assertTrue(result.getValidationTimeMillis() > 0);
        }
    }
}