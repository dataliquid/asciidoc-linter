package com.dataliquid.linter.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.cli.FileDiscoveryService;

/**
 * Performance tests for FileDiscoveryService pattern caching.
 */
@DisplayName("FileDiscoveryService Performance")
class FileDiscoveryServicePerformanceTest {

    @TempDir
    Path tempDir;

    private FileDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new FileDiscoveryService();
    }

    @Test
    @DisplayName("should demonstrate improved performance with pattern caching")
    void shouldDemonstrateImprovedPerformance() throws IOException {
        // Create test files
        createTestFileStructure();

        // Warm up
        for (int i = 0; i < 10; i++) {
            service.discoverFiles(List.of("**/*.adoc"), tempDir);
        }

        // Measure performance with caching
        long startTime = System.nanoTime();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            List<Path> files = service.discoverFiles(List.of("**/*.adoc"), tempDir);
            assertTrue(files.size() > 0, "Should find files");
        }

        long elapsedTime = System.nanoTime() - startTime;
        double avgTimeMs = (elapsedTime / 1_000_000.0) / iterations;

        // The cached version should be significantly faster
        // We expect sub-millisecond performance per iteration
        System.out.printf("Average time per iteration: %.3f ms%n", avgTimeMs);
        assertTrue(avgTimeMs < 5.0, "Average time should be less than 5ms per iteration");
    }

    @Test
    @DisplayName("should handle multiple different patterns efficiently")
    void shouldHandleMultiplePatternsEfficiently() throws IOException {
        createTestFileStructure();

        List<String> patterns = List
                .of("**/*.adoc", "docs/**/*.adoc", "src/*/docs/*.adoc", "**/*example*.adoc", "test?.adoc");

        // Warm up cache
        for (String pattern : patterns) {
            service.discoverFiles(List.of(pattern), tempDir);
        }

        // Measure performance with different patterns
        long startTime = System.nanoTime();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            for (String pattern : patterns) {
                service.discoverFiles(List.of(pattern), tempDir);
                // Just ensure it runs without error
            }
        }

        long elapsedTime = System.nanoTime() - startTime;
        double avgTimeMs = (elapsedTime / 1_000_000.0) / (iterations * patterns.size());

        System.out.printf("Average time per pattern match: %.3f ms%n", avgTimeMs);
        assertTrue(avgTimeMs < 2.0, "Pattern matching should be fast with caching");
    }

    @Test
    @DisplayName("should not have memory leaks with bounded cache")
    void shouldNotHaveMemoryLeaksWithCache() throws IOException {
        createTestFileStructure();

        // Generate many unique patterns to test cache behavior
        List<String> patterns = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            patterns.add(String.format("test%d*.adoc", i));
            patterns.add(String.format("**/*pattern%d*.adoc", i));
        }

        // Run all patterns
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for (String pattern : patterns) {
            service.discoverFiles(List.of(pattern), tempDir);
        }

        // Force garbage collection
        System.gc();
        Thread.yield();
        System.gc();

        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryIncrease = afterMemory - beforeMemory;

        // Memory increase should be reasonable (less than 10MB for 2000 patterns)
        System.out.printf("Memory increase: %.2f MB%n", memoryIncrease / (1024.0 * 1024.0));
        assertTrue(memoryIncrease < 10 * 1024 * 1024, "Memory usage should be reasonable");
    }

    private void createTestFileStructure() throws IOException {
        // Create a realistic file structure
        Files.createDirectories(tempDir.resolve("docs"));
        Files.createDirectories(tempDir.resolve("src/main/docs"));
        Files.createDirectories(tempDir.resolve("src/test/docs"));
        Files.createDirectories(tempDir.resolve("examples"));

        // Create test files
        Files.writeString(tempDir.resolve("README.adoc"), "= README");
        Files.writeString(tempDir.resolve("test1.adoc"), "= Test 1");
        Files.writeString(tempDir.resolve("test2.adoc"), "= Test 2");
        Files.writeString(tempDir.resolve("docs/guide.adoc"), "= Guide");
        Files.writeString(tempDir.resolve("docs/api.adoc"), "= API");
        Files.writeString(tempDir.resolve("src/main/docs/example.adoc"), "= Example");
        Files.writeString(tempDir.resolve("src/test/docs/test-example.adoc"), "= Test Example");
        Files.writeString(tempDir.resolve("examples/sample.adoc"), "= Sample");

        // Create some non-matching files
        Files.writeString(tempDir.resolve("notes.txt"), "Notes");
        Files.writeString(tempDir.resolve("docs/readme.md"), "# Readme");
    }
}
