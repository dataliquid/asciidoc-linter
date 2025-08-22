package com.dataliquid.asciidoc.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;

/**
 * Tests for CLIRunner output configuration handling.
 */
class CLIRunnerOutputConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadPredefinedOutputConfig() throws IOException {
        CLIRunner runner = new CLIRunner();

        // Test with predefined name
        CLIConfig config = CLIConfig
                .builder()
                .inputPatterns(java.util.List.of("*.adoc"))
                .outputConfigFormat(OutputFormat.SIMPLE)
                .build();

        // Use reflection to access private method
        OutputConfiguration outputConfig = invokeLoadOutputConfiguration(runner, config);

        assertNotNull(outputConfig);
        assertEquals(OutputFormat.SIMPLE, outputConfig.getFormat());
    }

    @Test
    void testLoadCustomOutputConfigFile() throws IOException {
        CLIRunner runner = new CLIRunner();

        // Create a custom config file
        Path customConfig = tempDir.resolve("custom.yaml");
        Files.writeString(customConfig, """
                output:
                  format: compact
                  display:
                    contextLines: 0
                    useColors: false
                """);

        CLIConfig config = CLIConfig
                .builder()
                .inputPatterns(java.util.List.of("*.adoc"))
                .outputConfigFile(customConfig)
                .build();

        OutputConfiguration outputConfig = invokeLoadOutputConfiguration(runner, config);

        assertNotNull(outputConfig);
        assertEquals(OutputFormat.COMPACT, outputConfig.getFormat());
        assertEquals(0, outputConfig.getDisplay().getContextLines());
        assertEquals(false, outputConfig.getDisplay().isUseColors());
    }

    @Test
    void testDefaultOutputConfig() throws IOException {
        CLIRunner runner = new CLIRunner();

        // No output config specified
        CLIConfig config = CLIConfig.builder().inputPatterns(java.util.List.of("*.adoc")).build();

        OutputConfiguration outputConfig = invokeLoadOutputConfiguration(runner, config);

        assertNotNull(outputConfig);
        // Should default to enhanced
        assertEquals(OutputFormat.ENHANCED, outputConfig.getFormat());
    }

    @Test
    void testNonExistentConfigFile() {
        CLIRunner runner = new CLIRunner();

        Path nonExistentFile = Paths.get("non-existent-config.yaml");
        CLIConfig config = CLIConfig
                .builder()
                .inputPatterns(java.util.List.of("*.adoc"))
                .outputConfigFile(nonExistentFile)
                .build();

        IOException exception = assertThrows(IOException.class, () -> invokeLoadOutputConfiguration(runner, config));

        assertTrue(exception.getMessage().contains("Output configuration file not found"));
    }

    // Helper method to invoke private method via reflection
    private OutputConfiguration invokeLoadOutputConfiguration(CLIRunner runner, CLIConfig config) throws IOException {
        try {
            var method = CLIRunner.class.getDeclaredMethod("loadOutputConfiguration", CLIConfig.class);
            method.setAccessible(true);
            return (OutputConfiguration) method.invoke(runner, config);
        } catch (Exception e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
}
