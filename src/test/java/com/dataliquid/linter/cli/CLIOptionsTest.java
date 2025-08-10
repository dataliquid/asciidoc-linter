package com.dataliquid.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.cli.CLIOptions;

@DisplayName("CLIOptions")
class CLIOptionsTest {
    
    private CLIOptions cliOptions;
    private CommandLineParser parser;
    
    @BeforeEach
    void setUp() {
        cliOptions = new CLIOptions();
        parser = new DefaultParser();
    }
    
    @Test
    @DisplayName("should parse short form arguments")
    void shouldParseShortFormArguments() throws ParseException {
        // Given
        String[] args = {"-i", "**/*.adoc", "-r", "config.yaml", "-f", "json", "-o", "report.json"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("i"));
        assertEquals("**/*.adoc", cmd.getOptionValue("i"));
        assertEquals("config.yaml", cmd.getOptionValue("r"));
        assertEquals("json", cmd.getOptionValue("f"));
        assertEquals("report.json", cmd.getOptionValue("o"));
    }
    
    @Test
    @DisplayName("should parse long form arguments")
    void shouldParseLongFormArguments() throws ParseException {
        // Given
        String[] args = {"--input", "docs/**/*.adoc,examples/**/*.asciidoc", "--rule", "config.yaml", 
                        "--report-format", "json", "--report-output", "report.json"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("input"));
        assertEquals("docs/**/*.adoc,examples/**/*.asciidoc", cmd.getOptionValue("input"));
        assertEquals("config.yaml", cmd.getOptionValue("rule"));
        assertEquals("json", cmd.getOptionValue("report-format"));
        assertEquals("report.json", cmd.getOptionValue("report-output"));
    }
    
    @Test
    @DisplayName("should parse without input when using generate-guidelines")
    void shouldParseWithoutInputWhenUsingGenerateGuidelines() throws ParseException {
        // Given
        String[] args = {"--generate-guidelines", "-r", "config.yaml"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("generate-guidelines"));
        assertTrue(cmd.hasOption("rule"));
        assertFalse(cmd.hasOption("input"));
    }
    
    @Test
    @DisplayName("should parse help and version flags")
    void shouldParseHelpAndVersionFlags() throws ParseException {
        // Given
        String[] argsHelp = {"-i", "test.adoc", "-h"};
        String[] argsVersion = {"-i", "test.adoc", "-v"};
        
        // When
        CommandLine cmdHelp = parser.parse(cliOptions.getOptions(), argsHelp);
        CommandLine cmdVersion = parser.parse(cliOptions.getOptions(), argsVersion);
        
        // Then
        assertTrue(cmdHelp.hasOption("help"));
        assertTrue(cmdVersion.hasOption("version"));
    }
    
    @Test
    @DisplayName("should parse fail level")
    void shouldParseFailLevel() throws ParseException {
        // Given
        String[] args = {"-i", "**/*.adoc", "-l", "warn"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertEquals("warn", cmd.getOptionValue("fail-level"));
    }
    
    @Test
    @DisplayName("should parse no-splash option")
    void shouldParseNoSplashOption() throws ParseException {
        // Given
        String[] args = {"-i", "**/*.adoc", "--no-splash"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("no-splash"));
    }
    
    @Test
    @DisplayName("should not have no-splash option by default")
    void shouldNotHaveNoSplashByDefault() throws ParseException {
        // Given
        String[] args = {"-i", "**/*.adoc"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertFalse(cmd.hasOption("no-splash"));
    }
}