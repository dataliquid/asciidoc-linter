package com.dataliquid.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.cli.CLIConfig;
import com.dataliquid.asciidoc.linter.cli.FileDiscoveryService;

@DisplayName("FileDiscoveryService")
class FileDiscoveryServiceTest {
    
    @TempDir
    Path tempDir;
    
    private FileDiscoveryService service;
    
    @BeforeEach
    void setUp() {
        service = new FileDiscoveryService();
    }
    
    @Nested
    @DisplayName("Simple file patterns")
    class SimpleFilePatterns {
        
        @Test
        @DisplayName("should find single file by exact name")
        void shouldFindSingleFileByExactName() throws IOException {
            // Given
            Path file = tempDir.resolve("README.adoc");
            Files.createFile(file);
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("README.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(1, files.size());
            assertEquals(file.normalize(), files.get(0));
        }
        
        @Test
        @DisplayName("should find files with wildcard pattern")
        void shouldFindFilesWithWildcardPattern() throws IOException {
            // Given
            Files.createFile(tempDir.resolve("doc1.adoc"));
            Files.createFile(tempDir.resolve("doc2.adoc"));
            Files.createFile(tempDir.resolve("readme.txt"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(2, files.size());
            List<String> fileNames = files.stream()
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
            assertTrue(fileNames.contains("doc1.adoc"));
            assertTrue(fileNames.contains("doc2.adoc"));
        }
    }
    
    @Nested
    @DisplayName("Ant patterns with **")
    class AntPatternsWithDoubleWildcard {
        
        @Test
        @DisplayName("should find files recursively with **/*.adoc")
        void shouldFindFilesRecursivelyWithDoubleWildcard() throws IOException {
            // Given
            Path subDir = tempDir.resolve("subdir");
            Path deepDir = subDir.resolve("deep");
            Files.createDirectories(deepDir);
            
            Files.createFile(tempDir.resolve("root.adoc"));
            Files.createFile(subDir.resolve("sub.adoc"));
            Files.createFile(deepDir.resolve("deep.adoc"));
            Files.createFile(tempDir.resolve("readme.txt"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("**/*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(3, files.size());
            List<String> relativePaths = files.stream()
                .map(p -> tempDir.relativize(p).toString())
                .collect(Collectors.toList());
            assertTrue(relativePaths.contains("root.adoc"));
            assertTrue(relativePaths.contains("subdir/sub.adoc".replace('/', java.io.File.separatorChar)));
            assertTrue(relativePaths.contains("subdir/deep/deep.adoc".replace('/', java.io.File.separatorChar)));
        }
        
        @Test
        @DisplayName("should find files in specific subdirectories with docs/**/*.adoc")
        void shouldFindFilesInSpecificSubdirectories() throws IOException {
            // Given
            Path docsDir = tempDir.resolve("docs");
            Path srcDir = tempDir.resolve("src");
            Path docsSubDir = docsDir.resolve("api");
            Files.createDirectories(docsSubDir);
            Files.createDirectories(srcDir);
            
            Files.createFile(tempDir.resolve("root.adoc"));
            Files.createFile(docsDir.resolve("manual.adoc"));
            Files.createFile(docsSubDir.resolve("api.adoc"));
            Files.createFile(srcDir.resolve("code.adoc"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("docs/**/*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(2, files.size());
            List<String> relativePaths = files.stream()
                .map(p -> tempDir.relativize(p).toString())
                .collect(Collectors.toList());
            assertTrue(relativePaths.contains("docs/manual.adoc".replace('/', java.io.File.separatorChar)));
            assertTrue(relativePaths.contains("docs/api/api.adoc".replace('/', java.io.File.separatorChar)));
            assertFalse(relativePaths.contains("root.adoc"));
            assertFalse(relativePaths.contains("src/code.adoc".replace('/', java.io.File.separatorChar)));
        }
    }
    
    @Nested
    @DisplayName("Multiple patterns")
    class MultiplePatterns {
        
        @Test
        @DisplayName("should combine results from multiple patterns")
        void shouldCombineResultsFromMultiplePatterns() throws IOException {
            // Given
            Files.createFile(tempDir.resolve("README.adoc"));
            Files.createFile(tempDir.resolve("guide.asciidoc"));
            Files.createFile(tempDir.resolve("manual.txt"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("*.adoc", "*.asciidoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(2, files.size());
            List<String> fileNames = files.stream()
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
            assertTrue(fileNames.contains("README.adoc"));
            assertTrue(fileNames.contains("guide.asciidoc"));
        }
        
        @Test
        @DisplayName("should remove duplicates when patterns overlap")
        void shouldRemoveDuplicatesWhenPatternsOverlap() throws IOException {
            // Given
            Files.createFile(tempDir.resolve("doc.adoc"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("*.adoc", "doc.*", "**/*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(1, files.size());
            assertEquals("doc.adoc", files.get(0).getFileName().toString());
        }
        
        @Test
        @DisplayName("should handle complex comma-separated patterns")
        void shouldHandleComplexCommaSeparatedPatterns() throws IOException {
            // Given
            Path docsDir = tempDir.resolve("docs");
            Path examplesDir = tempDir.resolve("examples");
            Files.createDirectories(docsDir);
            Files.createDirectories(examplesDir);
            
            Files.createFile(tempDir.resolve("README.adoc"));
            Files.createFile(docsDir.resolve("guide.adoc"));
            Files.createFile(examplesDir.resolve("example.asciidoc"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("docs/**/*.adoc", "examples/**/*.asciidoc", "README.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(3, files.size());
        }
    }
    
    @Nested
    @DisplayName("Pattern matching")
    class PatternMatching {
        
        @Test
        @DisplayName("should match single character wildcard (?)")
        void shouldMatchSingleCharacterWildcard() throws IOException {
            // Given
            Files.createFile(tempDir.resolve("doc1.adoc"));
            Files.createFile(tempDir.resolve("doc2.adoc"));
            Files.createFile(tempDir.resolve("docs.adoc"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("doc?.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(3, files.size());
            List<String> fileNames = files.stream()
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
            assertTrue(fileNames.contains("doc1.adoc"));
            assertTrue(fileNames.contains("doc2.adoc"));
            assertTrue(fileNames.contains("docs.adoc")); // ? matches exactly one character, so 's' is matched
        }
        
        @Test
        @DisplayName("should handle patterns with directory wildcards")
        void shouldHandlePatternsWithDirectoryWildcards() throws IOException {
            // Given
            Path moduleA = tempDir.resolve("module-a").resolve("docs");
            Path moduleB = tempDir.resolve("module-b").resolve("docs");
            Files.createDirectories(moduleA);
            Files.createDirectories(moduleB);
            
            Files.createFile(moduleA.resolve("guide.adoc"));
            Files.createFile(moduleB.resolve("guide.adoc"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("module-*/docs/*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(2, files.size());
        }
    }
    
    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should return empty list when no files match")
        void shouldReturnEmptyListWhenNoFilesMatch() throws IOException {
            // Given
            Files.createFile(tempDir.resolve("readme.txt"));
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertTrue(files.isEmpty());
        }
        
        @Test
        @DisplayName("should handle absolute paths")
        void shouldHandleAbsolutePaths() throws IOException {
            // Given
            Path file = tempDir.resolve("test.adoc");
            Files.createFile(file);
            
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList(file.toAbsolutePath().toString()))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertEquals(1, files.size());
            assertEquals(file.normalize(), files.get(0));
        }
        
        @Test
        @DisplayName("should handle empty directory")
        void shouldHandleEmptyDirectory() throws IOException {
            // Given
            CLIConfig config = CLIConfig.builder()
                .inputPatterns(Arrays.asList("**/*.adoc"))
                .baseDirectory(tempDir)
                .build();
            
            // When
            List<Path> files = service.discoverFiles(config);
            
            // Then
            assertTrue(files.isEmpty());
        }
    }
}