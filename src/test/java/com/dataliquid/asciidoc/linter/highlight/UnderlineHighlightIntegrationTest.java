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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.Linter;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.ErrorGroupingConfig;
import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;
import com.dataliquid.asciidoc.linter.config.output.SuggestionsConfig;
import com.dataliquid.asciidoc.linter.config.output.SummaryConfig;
import com.dataliquid.asciidoc.linter.report.ConsoleFormatter;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Integration test for underline highlighting in console output.
 * Tests that validation errors properly underline problematic text areas,
 * especially for maxLength and minLength validation rules.
 */
@DisplayName("Underline Highlight Integration Test")
class UnderlineHighlightIntegrationTest {
    
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
    
    /**
     * Creates the default output configuration for underline display.
     */
    private OutputConfiguration createDefaultOutputConfig() {
        return OutputConfiguration.builder()
            .format(OutputFormat.ENHANCED)
            .display(DisplayConfig.builder()
                .contextLines(3)
                .useColors(false)  // No colors for easier testing
                .showLineNumbers(true)
                .showHeader(true)  // Enable header to match expected output
                .highlightStyle(HighlightStyle.UNDERLINE) // Enable underline highlighting
                .maxLineWidth(120)
                .build())
            .suggestions(SuggestionsConfig.builder()
                .enabled(false)
                .build())
            .errorGrouping(ErrorGroupingConfig.builder()
                .enabled(false)  // Disable error grouping for predictable output
                .build())
            .summary(SummaryConfig.builder()
                .enabled(false)
                .build())
            .build();
    }
    
    /**
     * Validates content and returns the formatted console output.
     */
    private String validateAndFormat(String rules, String adocContent, Path tempDir) throws IOException {
        // Clear any previous output
        stringWriter.getBuffer().setLength(0);
        
        // Create temporary file with content
        Path testFile = tempDir.resolve("test.adoc");
        Files.writeString(testFile, adocContent);
        
        // Load configuration and validate
        LinterConfiguration config = configLoader.loadConfiguration(rules);
        ValidationResult result = linter.validateFile(testFile, config);
        
        // Format output
        ConsoleFormatter formatter = new ConsoleFormatter(createDefaultOutputConfig());
        formatter.format(result, printWriter);
        printWriter.flush();
        
        return stringWriter.toString();
    }
    
    @Nested
    @DisplayName("DList Validation Tests")
    class DlistValidationTests {
        
        @Test
        @DisplayName("should show underline for dlist term exceeding max length")
        void shouldShowUnderlineForDlistTermExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for dlist terms
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - dlist:
                        severity: error
                        terms:
                          maxLength: 20
                          severity: error
            """;
        
        // Given - AsciiDoc content with a term exceeding max length
        String adocContent = """
            = Test Document
            
            This is a very long definition list term that exceeds twenty characters::
              The description for the long term.
            
            Short term::
              Another description.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with underline
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Definition list term is too long [dlist.terms.maxLength]
              File: %s:3:1-71
            
               1 | = Test Document
               2 |\s
               3 | This is a very long definition list term that exceeds twenty characters::
                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
               4 |   The description for the long term.
               5 |\s
               6 | Short term::
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
        @Test
        @DisplayName("should show underline for dlist term below min length")
        void shouldShowUnderlineForDlistTermBelowMinLength(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules with min length for dlist terms
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - dlist:
                        severity: error
                        terms:
                          minLength: 10
                          severity: warn
            """;
        
        // Given - AsciiDoc content with a term below min length
        String adocContent = """
            = Test Document
            
            API::
              Application Programming Interface
            
            Term::
              This term is too short
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with underline
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [WARN]: Definition list term is too short [dlist.terms.minLength]
              File: %s:3:1-3
            
               1 | = Test Document
               2 |\s
               3 | API::
                 | ~~~
               4 |   Application Programming Interface
               5 |\s
               6 | Term::
            
            [WARN]: Definition list term is too short [dlist.terms.minLength]
              File: %s:6:1-4
            
               3 | API::
               4 |   Application Programming Interface
               5 |\s
               6 | Term::
                 | ~~~~
               7 |   This term is too short
            
            
            """, testFile.toString(), testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    
        @Test
        @DisplayName("should show underline for dlist term not matching pattern")
        void shouldShowUnderlineForDlistTermNotMatchingPattern(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules with pattern for dlist terms
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - dlist:
                        severity: error
                        terms:
                          pattern: "^[A-Z][A-Z0-9_]+$"
                          severity: error
            """;
        
        // Given - AsciiDoc content with terms not matching the pattern
        String adocContent = """
            = Test Document
            
            lowercase_term::
              This term starts with lowercase.
            
            VALID_TERM::
              This term matches the pattern.
            
            Mixed-Case::
              This term has invalid characters.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with underline
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Definition list term does not match required pattern [dlist.terms.pattern]
              File: %s:3:1-14
            
               1 | = Test Document
               2 |\s
               3 | lowercase_term::
                 | ~~~~~~~~~~~~~~
               4 |   This term starts with lowercase.
               5 |\s
               6 | VALID_TERM::
            
            [ERROR]: Definition list term does not match required pattern [dlist.terms.pattern]
              File: %s:9:1-10
            
               6 | VALID_TERM::
               7 |   This term matches the pattern.
               8 |\s
               9 | Mixed-Case::
                 | ~~~~~~~~~~
              10 |   This term has invalid characters.
            
            
            """, testFile.toString(), testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Image Block Validation Tests")
    class ImageValidationTests {
        
        @Test
        @DisplayName("should show underline for image URL not matching pattern")
        void shouldShowUnderlineForImageUrlPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with URL pattern for image blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - image:
                            severity: error
                            url:
                              required: true
                              pattern: "^https?://.*\\.(jpg|jpeg|png|gif|svg)$"
                """;
            
            // Given - AsciiDoc content with image URLs not matching the pattern
            String adocContent = """
                = Test Document
                
                image::file:///local/path/image.png[Local file]
                
                image::https://example.com/image.bmp[Wrong format]
                
                image::https://example.com/valid.png[Valid image]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Image URL does not match required pattern [image.url.pattern]
                  File: %s:3:1-48
                
                   1 | = Test Document
                   2 |\s
                   3 | image::file:///local/path/image.png[Local file]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | image::https://example.com/image.bmp[Wrong format]
                   6 |\s
                
                [ERROR]: Image URL does not match required pattern [image.url.pattern]
                  File: %s:5:1-51
                
                   2 |\s
                   3 | image::file:///local/path/image.png[Local file]
                   4 |\s
                   5 | image::https://example.com/image.bmp[Wrong format]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 |\s
                   7 | image::https://example.com/valid.png[Valid image]
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for image alt text exceeding max length")
        void shouldShowUnderlineForImageAltExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for image alt text
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - image:
                            severity: error
                            alt:
                              required: true
                              maxLength: 30
                """;
            
            // Given - AsciiDoc content with alt text exceeding max length
            String adocContent = """
                = Test Document
                
                image::diagram.png[This is a very long alternative text that exceeds the maximum allowed length]
                
                image::logo.png[Short alt text]
                
                image::chart.png[Another extremely long alternative text description that should be shorter]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Image alt text is too long [image.alt.maxLength]
                  File: %s:3:1-97
                
                   1 | = Test Document
                   2 |\s
                   3 | image::diagram.png[This is a very long alternative text that exceeds the maximum allowed length]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | image::logo.png[Short alt text]
                   6 |\s
                
                [ERROR]: Image alt text is too long [image.alt.maxLength]
                  File: %s:7:1-90
                
                   4 |\s
                   5 | image::logo.png[Short alt text]
                   6 |\s
                   7 | image::chart.png[Another extremely long alternative text description that should be shorter]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for image alt text below min length")
        void shouldShowUnderlineForImageAltBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for image alt text
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - image:
                            severity: warn
                            alt:
                              required: true
                              minLength: 10
                """;
            
            // Given - AsciiDoc content with alt text below min length
            String adocContent = """
                = Test Document
                
                image::icon.png[Logo]
                
                image::screenshot.png[Application screenshot showing main window]
                
                image::btn.png[OK]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Image alt text is too short [image.alt.minLength]
                  File: %s:3:1-21
                
                   1 | = Test Document
                   2 |\s
                   3 | image::icon.png[Logo]
                     | ~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | image::screenshot.png[Application screenshot showing main window]
                   6 |\s
                
                [WARN]: Image alt text is too short [image.alt.minLength]
                  File: %s:7:1-18
                
                   4 |\s
                   5 | image::screenshot.png[Application screenshot showing main window]
                   6 |\s
                   7 | image::btn.png[OK]
                     | ~~~~~~~~~~~~~~~~~~
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
}