package com.dataliquid.asciidoc.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

/**
 * Tests for MainCLI output configuration argument parsing.
 */
class MainCLIOutputConfigTest {

    @Test
    void testOutputConfigParsing() {
        MainCLI cli = new MainCLI();

        // Capture output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with predefined config name
            int exitCode = cli.run(new String[]{"lint", "--input", "test.adoc", "--output-config", "simple"});

            // Should fail because test.adoc doesn't exist, but parsing should succeed
            assertEquals(2, exitCode);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testOutputConfigFileParsing() {
        MainCLI cli = new MainCLI();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with custom config file
            int exitCode = cli
                    .run(new String[]{"lint", "--input", "test.adoc", "--output-config-file", "custom-output.yaml"});

            // Should fail because files don't exist, but parsing should succeed
            assertEquals(2, exitCode);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testBothOutputConfigOptionsError() {
        MainCLI cli = new MainCLI();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with both options - should fail
            int exitCode = cli.run(new String[]{"lint", "--input", "test.adoc", "--output-config", "simple",
                    "--output-config-file", "custom.yaml"});

            assertEquals(2, exitCode);
            String error = errContent.toString();
            assertTrue(error.contains("Cannot use both --output-config and --output-config-file"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testInvalidOutputConfigName() {
        MainCLI cli = new MainCLI();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with invalid config name
            int exitCode = cli.run(new String[]{"lint", "--input", "test.adoc", "--output-config", "invalid-name"});

            assertEquals(2, exitCode);
            String error = errContent.toString();
            assertTrue(error.contains("Invalid output config name: invalid-name"));
            assertTrue(error.contains("Valid options: enhanced, simple, compact"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testHelpShowsBothOptions() {
        MainCLI cli = new MainCLI();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Test lint command help which shows the output options
            int exitCode = cli.run(new String[]{"lint", "--help"});

            assertEquals(0, exitCode);
            String output = outContent.toString();
            assertTrue(output.contains("--output-config"));
            assertTrue(output.contains("--output-config-file"));
            assertTrue(output.contains("enhanced, simple, compact"));
        } finally {
            System.setOut(originalOut);
        }
    }
}
