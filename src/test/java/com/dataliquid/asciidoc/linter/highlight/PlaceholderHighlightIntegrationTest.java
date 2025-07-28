package com.dataliquid.asciidoc.linter.highlight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.Linter;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;
import com.dataliquid.asciidoc.linter.config.output.SuggestionsConfig;
import com.dataliquid.asciidoc.linter.config.output.SummaryConfig;
import com.dataliquid.asciidoc.linter.report.ConsoleFormatter;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Integration test for placeholder highlighting in console output.
 * Tests the enhanced error display with placeholders for missing values.
 */
@DisplayName("Placeholder Highlight Integration Test")
class PlaceholderHighlightIntegrationTest {
    
    private Linter linter;
    private ConfigurationLoader configLoader;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() {
        linter = new Linter();
        configLoader = new ConfigurationLoader();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }
    
    @AfterEach
    void tearDown() {
        linter.close();
    }
    
    @Test
    @DisplayName("should show placeholder for missing listing language")
    void shouldShowPlaceholderForMissingListingLanguage(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring language for listing blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - listing:
                        severity: error
                        language:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing language  
        String adocContent = """
            = Test Document
            
            [source]
            ----
            public class Example {
                // Some code here
            }
            ----
            """;
        
        // Create temporary file with content
        Path testFile = tempDir.resolve("test.adoc");
        Files.writeString(testFile, adocContent);
        
        // Given - Enhanced output configuration
        OutputConfiguration outputConfig = OutputConfiguration.builder()
            .format(OutputFormat.ENHANCED)
            .display(DisplayConfig.builder()
                .contextLines(3)
                .useColors(false)  // No colors for easier testing
                .showLineNumbers(true)
                .showHeader(true)  // Enable header to match expected output
                .highlightStyle(HighlightStyle.UNDERLINE)
                .maxLineWidth(120)
                .build())
            .suggestions(SuggestionsConfig.builder()
                .enabled(false)
                .build())
            .summary(SummaryConfig.builder()
                .enabled(false)
                .build())
            .build();
        
        // When - Validate and format output
        LinterConfiguration config = configLoader.loadConfiguration(rules);
        ValidationResult result = linter.validateFile(testFile, config);
        
        ConsoleFormatter formatter = new ConsoleFormatter(outputConfig);
        formatter.format(result, printWriter);
        printWriter.flush();
        
        // Then - Verify exact console output with placeholder
        String actualOutput = stringWriter.toString();
        
        // The empty line in the adoc file gets a trailing space when rendered
        // Expected output with placeholder for missing language
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Listing block must specify a language [listing.language.required]
              File: %s:3:8
            
               1 | = Test Document
               2 |\s
               3 | [source,«language»]
               4 | ----
               5 | public class Example {
               6 |     // Some code here
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
}