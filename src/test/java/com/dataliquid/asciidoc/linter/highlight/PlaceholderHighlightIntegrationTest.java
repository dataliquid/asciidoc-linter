package com.dataliquid.asciidoc.linter.highlight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    void shouldShowPlaceholderForMissingListingLanguage() {
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
            
            == Code Example
            
            [source]
            ----
            public class Example {
                // Some code here
            }
            ----
            """;
        
        // Given - Enhanced output configuration
        OutputConfiguration outputConfig = OutputConfiguration.builder()
            .format(OutputFormat.ENHANCED)
            .display(DisplayConfig.builder()
                .contextLines(3)
                .useColors(false)  // No colors for easier testing
                .showLineNumbers(true)
                .showHeader(false)
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
        ValidationResult result = linter.validateContent(adocContent, config);
        
        ConsoleFormatter formatter = new ConsoleFormatter(outputConfig);
        formatter.format(result, printWriter);
        printWriter.flush();
        
        // Then - Verify exact console output with placeholder
        String actualOutput = stringWriter.toString();
        String expectedOutput = """
            Validation Report
            =================
            
            test.adoc:
            
            [ERROR]: Listing block must specify a language [listing.language.required]
              File: test.adoc:5:8
            
               3 | == Code Example
               4 | 
               5 | [source,«language»]
               6 | ----
               7 | public class Example {
               8 |     // Some code here
            
            
            """;
        
        assertEquals(expectedOutput, actualOutput);
    }
}