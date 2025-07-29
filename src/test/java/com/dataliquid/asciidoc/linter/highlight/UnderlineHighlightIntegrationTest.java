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
    
    @Nested
    @DisplayName("Video Block Validation Tests")
    class VideoValidationTests {
        
        @Test
        @DisplayName("should show underline for video URL not matching pattern")
        void shouldShowUnderlineForVideoUrlPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with URL pattern for video blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - video:
                            severity: error
                            url:
                              required: true
                              pattern: "^https?://.*\\.(mp4|webm|ogg|avi)$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with video URLs not matching the pattern
            String adocContent = """
                = Test Document
                
                video::file:///local/video.mp4[Local video file]
                
                video::https://example.com/video.mov[Unsupported format]
                
                video::https://example.com/demo.mp4[Valid video]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Video URL does not match required pattern [video.url.pattern]
                  File: %s:3:1-48
                
                   1 | = Test Document
                   2 |\s
                   3 | video::file:///local/video.mp4[Local video file]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | video::https://example.com/video.mov[Unsupported format]
                   6 |\s
                
                [ERROR]: Video URL does not match required pattern [video.url.pattern]
                  File: %s:5:1-56
                
                   2 |\s
                   3 | video::file:///local/video.mp4[Local video file]
                   4 |\s
                   5 | video::https://example.com/video.mov[Unsupported format]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 |\s
                   7 | video::https://example.com/demo.mp4[Valid video]
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for video poster not matching pattern")
        void shouldShowUnderlineForVideoPosterPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with poster pattern for video blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - video:
                            severity: error
                            poster:
                              required: true
                              pattern: "^https?://.*\\.(jpg|jpeg|png)$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with poster URLs not matching the pattern
            String adocContent = """
                = Test Document
                
                video::https://example.com/video.mp4[poster=file:///local/poster.jpg]
                
                video::https://example.com/video.mp4[poster=https://example.com/poster.bmp]
                
                video::https://example.com/video.mp4[poster=https://example.com/poster.png]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Video poster does not match required pattern [video.poster.pattern]
                  File: %s:3:1-69
                
                   1 | = Test Document
                   2 |\s
                   3 | video::https://example.com/video.mp4[poster=file:///local/poster.jpg]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | video::https://example.com/video.mp4[poster=https://example.com/poster.bmp]
                   6 |\s
                
                [ERROR]: Video poster does not match required pattern [video.poster.pattern]
                  File: %s:5:1-75
                
                   2 |\s
                   3 | video::https://example.com/video.mp4[poster=file:///local/poster.jpg]
                   4 |\s
                   5 | video::https://example.com/video.mp4[poster=https://example.com/poster.bmp]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 |\s
                   7 | video::https://example.com/video.mp4[poster=https://example.com/poster.png]
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for video caption exceeding max length")
        void shouldShowUnderlineForVideoCaptionExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for video caption
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - video:
                            severity: error
                            caption:
                              required: true
                              maxLength: 25
                              severity: error
                """;
            
            // Given - AsciiDoc content with captions exceeding max length
            String adocContent = """
                = Test Document
                
                .This is an extremely long video caption that definitely exceeds the maximum allowed length
                video::https://example.com/intro.mp4[]
                
                .Short caption
                video::https://example.com/demo.mp4[]
                
                .Another very long caption that should be much shorter to comply with rules
                video::https://example.com/tutorial.mp4[]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Video caption is too long [video.caption.maxLength]
                  File: %s:3:1-91
                
                   1 | = Test Document
                   2 |\s
                   3 | .This is an extremely long video caption that definitely exceeds the maximum allowed length
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | video::https://example.com/intro.mp4[]
                   5 |\s
                   6 | .Short caption
                
                [ERROR]: Video caption is too long [video.caption.maxLength]
                  File: %s:9:1-75
                
                   6 | .Short caption
                   7 | video::https://example.com/demo.mp4[]
                   8 |\s
                   9 | .Another very long caption that should be much shorter to comply with rules
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  10 | video::https://example.com/tutorial.mp4[]
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for video caption below min length")
        void shouldShowUnderlineForVideoCaptionBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for video caption
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - video:
                            severity: warn
                            caption:
                              required: true
                              minLength: 15
                              severity: warn
                """;
            
            // Given - AsciiDoc content with captions below min length
            String adocContent = """
                = Test Document
                
                .Demo
                video::https://example.com/demo.mp4[]
                
                .Product demonstration video showing main features
                video::https://example.com/product.mp4[]
                
                .Tutorial
                video::https://example.com/tutorial.mp4[]
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Video caption is too short [video.caption.minLength]
                  File: %s:3:1-5
                
                   1 | = Test Document
                   2 |\s
                   3 | .Demo
                     | ~~~~~
                   4 | video::https://example.com/demo.mp4[]
                   5 |\s
                   6 | .Product demonstration video showing main features
                
                [WARN]: Video caption is too short [video.caption.minLength]
                  File: %s:9:1-9
                
                   6 | .Product demonstration video showing main features
                   7 | video::https://example.com/product.mp4[]
                   8 |\s
                   9 | .Tutorial
                     | ~~~~~~~~~
                  10 | video::https://example.com/tutorial.mp4[]
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Table Block Validation Tests")
    class TableValidationTests {
        
        @Test
        @DisplayName("should show underline for table header not matching pattern")
        void shouldShowUnderlineForTableHeaderPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with header pattern for table blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - table:
                            severity: error
                            header:
                              required: true
                              pattern: "^[A-Z][a-zA-Z0-9 ]+$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with table headers not matching the pattern
            String adocContent = """
                = Test Document
                
                |===
                | lowercase header | Another Column
                
                | Data 1 | Data 2
                |===
                
                |===
                | Valid Header | Second Column
                
                | Data 1 | Data 2
                |===
                
                |===
                | Special-Characters! | Column#2
                
                | Data 1 | Data 2
                |===
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Table header does not match required pattern [table.header.pattern]
                  File: %s:4:3-17
                
                   1 | = Test Document
                   2 |\s
                   3 | |===
                   4 | | lowercase header | Another Column
                     |   ~~~~~~~~~~~~~~~~
                   5 |\s
                   6 | | Data 1 | Data 2
                   7 | |===
                
                [ERROR]: Table header does not match required pattern [table.header.pattern]
                  File: %s:16:3-20
                
                  13 | |===
                  14 |\s
                  15 | |===
                  16 | | Special-Characters! | Column#2
                     |   ~~~~~~~~~~~~~~~~~~
                  17 |\s
                  18 | | Data 1 | Data 2
                  19 | |===
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for table caption below min length")
        void shouldShowUnderlineForTableCaptionBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for table caption
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - table:
                            severity: warn
                            caption:
                              required: true
                              minLength: 20
                              severity: warn
                """;
            
            // Given - AsciiDoc content with table captions below min length
            String adocContent = """
                = Test Document
                
                .Results
                |===
                | Name | Score
                
                | Alice | 95
                | Bob | 87
                |===
                
                .Comprehensive analysis of test results
                |===
                | Name | Score
                
                | Charlie | 92
                | David | 88
                |===
                
                .Data
                |===
                | ID | Value
                
                | 1 | 100
                | 2 | 200
                |===
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Table caption is too short [table.caption.minLength]
                  File: %s:3:1-8
                
                   1 | = Test Document
                   2 |\s
                   3 | .Results
                     | ~~~~~~~~
                   4 | |===
                   5 | | Name | Score
                   6 |\s
                
                [WARN]: Table caption is too short [table.caption.minLength]
                  File: %s:19:1-5
                
                  16 | | David | 88
                  17 | |===
                  18 |\s
                  19 | .Data
                     | ~~~~~
                  20 | |===
                  21 | | ID | Value
                  22 |\s
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for table caption exceeding max length")
        void shouldShowUnderlineForTableCaptionExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for table caption
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - table:
                            severity: error
                            caption:
                              required: true
                              maxLength: 30
                              severity: error
                """;
            
            // Given - AsciiDoc content with table captions exceeding max length
            String adocContent = """
                = Test Document
                
                .This is an extremely long table caption that definitely exceeds the maximum allowed length
                |===
                | Column 1 | Column 2
                
                | Data 1 | Data 2
                |===
                
                .Short caption
                |===
                | A | B
                
                | 1 | 2
                |===
                
                .Another excessively long caption that should be shortened to comply with the rules
                |===
                | X | Y
                
                | 3 | 4
                |===
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Table caption is too long [table.caption.maxLength]
                  File: %s:3:1-91
                
                   1 | = Test Document
                   2 |\s
                   3 | .This is an extremely long table caption that definitely exceeds the maximum allowed length
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | |===
                   5 | | Column 1 | Column 2
                   6 |\s
                
                [ERROR]: Table caption is too long [table.caption.maxLength]
                  File: %s:17:1-83
                
                  14 | | 1 | 2
                  15 | |===
                  16 |\s
                  17 | .Another excessively long caption that should be shortened to comply with the rules
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  18 | |===
                  19 | | X | Y
                  20 |\s
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for table caption not matching pattern")
        void shouldShowUnderlineForTableCaptionPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with caption pattern for table blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - table:
                            severity: error
                            caption:
                              required: true
                              pattern: "^Table \\d+\\..+"
                              severity: error
                """;
            
            // Given - AsciiDoc content with table captions not matching the pattern
            String adocContent = """
                = Test Document
                
                .Results Summary
                |===
                | Name | Score
                
                | Alice | 95
                |===
                
                .Table 1. Valid caption format
                |===
                | ID | Value
                
                | 1 | 100
                |===
                
                .Invalid format
                |===
                | X | Y
                
                | A | B
                |===
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Table caption does not match required pattern [table.caption.pattern]
                  File: %s:3:1-16
                
                   1 | = Test Document
                   2 |\s
                   3 | .Results Summary
                     | ~~~~~~~~~~~~~~~~
                   4 | |===
                   5 | | Name | Score
                   6 |\s
                
                [ERROR]: Table caption does not match required pattern [table.caption.pattern]
                  File: %s:17:1-15
                
                  14 | | 1 | 100
                  15 | |===
                  16 |\s
                  17 | .Invalid format
                     | ~~~~~~~~~~~~~~~
                  18 | |===
                  19 | | X | Y
                  20 |\s
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Verse Block Validation Tests")
    class VerseValidationTests {
        
        @Test
        @DisplayName("should show underline for verse author exceeding max length")
        void shouldShowUnderlineForVerseAuthorExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for verse author
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: error
                            author:
                              required: true
                              maxLength: 25
                              severity: error
                """;
            
            // Given - AsciiDoc content with verse authors exceeding max length
            String adocContent = """
                = Test Document
                
                [verse, "William Shakespeare and all his collaborators", "Hamlet"]
                ____
                To be, or not to be, that is the question
                ____
                
                [verse, "Emily Dickinson", "Complete Poems"]
                ____
                Because I could not stop for Death
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Verse author is too long [verse.author.maxLength]
                  File: %s:3:1-67
                
                   1 | = Test Document
                   2 |\s
                   3 | [verse, "William Shakespeare and all his collaborators", "Hamlet"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | To be, or not to be, that is the question
                   6 | ____
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for verse author below min length")
        void shouldShowUnderlineForVerseAuthorBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for verse author
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: warn
                            author:
                              required: true
                              minLength: 10
                              severity: warn
                """;
            
            // Given - AsciiDoc content with verse authors below min length
            String adocContent = """
                = Test Document
                
                [verse, "Anon", "Unknown"]
                ____
                Roses are red, violets are blue
                ____
                
                [verse, "Shakespeare", "Sonnets"]
                ____
                Shall I compare thee to a summer's day?
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Verse author is too short [verse.author.minLength]
                  File: %s:3:1-25
                
                   1 | = Test Document
                   2 |\s
                   3 | [verse, "Anon", "Unknown"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | Roses are red, violets are blue
                   6 | ____
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for verse author not matching pattern")
        void shouldShowUnderlineForVerseAuthorPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for verse author
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: error
                            author:
                              required: true
                              pattern: "^[A-Z][a-z]+ [A-Z][a-z]+$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with verse authors not matching pattern
            String adocContent = """
                = Test Document
                
                [verse, "shakespeare", "Hamlet"]
                ____
                To be or not to be
                ____
                
                [verse, "Emily Dickinson", "Poems"]
                ____
                I'm nobody! Who are you?
                ____
                
                [verse, "E.E. Cummings", "Complete Poems"]
                ____
                i carry your heart with me
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Verse author does not match required pattern [verse.author.pattern]
                  File: %s:3:1-32
                
                   1 | = Test Document
                   2 |\s
                   3 | [verse, "shakespeare", "Hamlet"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | To be or not to be
                   6 | ____
                
                [ERROR]: Verse author does not match required pattern [verse.author.pattern]
                  File: %s:13:1-41
                
                  10 | I'm nobody! Who are you?
                  11 | ____
                  12 |\s
                  13 | [verse, "E.E. Cummings", "Complete Poems"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  14 | ____
                  15 | i carry your heart with me
                  16 | ____
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for verse attribution exceeding max length")
        void shouldShowUnderlineForVerseAttributionExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for verse attribution
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: error
                            attribution:
                              required: true
                              maxLength: 20
                              severity: error
                """;
            
            // Given - AsciiDoc content with verse attributions exceeding max length
            String adocContent = """
                = Test Document
                
                [verse, "William Shakespeare", "Hamlet Act 3 Scene 1 Line 56"]
                ____
                To be, or not to be
                ____
                
                [verse, "Emily Dickinson", "Poems"]
                ____
                Hope is the thing with feathers
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Verse attribution is too long [verse.attribution.maxLength]
                  File: %s:3:1-62
                
                   1 | = Test Document
                   2 |\s
                   3 | [verse, "William Shakespeare", "Hamlet Act 3 Scene 1 Line 56"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | To be, or not to be
                   6 | ____
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for verse attribution not matching pattern")
        void shouldShowUnderlineForVerseAttributionPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for verse attribution
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: error
                            attribution:
                              required: true
                              pattern: "^[A-Z][a-zA-Z\\s]+, \\d{4}$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with verse attributions not matching pattern
            String adocContent = """
                = Test Document
                
                [verse, "Robert Frost", "Unknown date"]
                ____
                Two roads diverged in a wood
                ____
                
                [verse, "Maya Angelou", "Still I Rise, 1978"]
                ____
                You may write me down in history
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Verse attribution does not match required pattern [verse.attribution.pattern]
                  File: %s:3:1-38
                
                   1 | = Test Document
                   2 |\s
                   3 | [verse, "Robert Frost", "Unknown date"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | Two roads diverged in a wood
                   6 | ____
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for verse content exceeding max length")
        void shouldShowUnderlineForVerseContentExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for verse content
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - verse:
                            severity: error
                            content:
                              required: true
                              maxLength: 50
                              severity: error
                """;
            
            // Given - AsciiDoc content with verse content exceeding max length
            String adocContent = """
                = Test Document
                
                [verse]
                ____
                This is an extremely long verse content that definitely exceeds the maximum allowed length of fifty characters
                ____
                
                [verse]
                ____
                Short verse content
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Verse content is too long [verse.content.maxLength]
                  File: %s:5:1-110
                
                   2 |\s
                   3 | [verse]
                   4 | ____
                   5 | This is an extremely long verse content that definitely exceeds the maximum allowed length of fifty characters
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 | ____
                   7 |\s
                   8 | [verse]
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Pass Block Validation Tests")
    class PassValidationTests {
        
        @Test
        @DisplayName("should show underline for pass block with disallowed type")
        void shouldShowUnderlineForPassDisallowedType(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with allowed types for pass blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - pass:
                            severity: error
                            type:
                              required: true
                              allowed: ["html", "xml", "svg"]
                              severity: error
                """;
            
            // Given - AsciiDoc content with pass blocks having disallowed types
            String adocContent = """
                = Test Document
                
                [pass,type=javascript]
                ++++
                console.log("Hello");
                ++++
                
                [pass,type=html]
                ++++
                <p>Valid HTML</p>
                ++++
                
                [pass,type=css]
                ++++
                body { color: red; }
                ++++
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Pass block type 'javascript' is not allowed [pass.type.allowed]
                  File: %s:3:1-22
                
                   1 | = Test Document
                   2 |\s
                   3 | [pass,type=javascript]
                     | ~~~~~~~~~~~~~~~~~~~~~~
                   4 | ++++
                   5 | console.log("Hello");
                   6 | ++++
                
                [ERROR]: Pass block type 'css' is not allowed [pass.type.allowed]
                  File: %s:13:1-15
                
                  10 | <p>Valid HTML</p>
                  11 | ++++
                  12 |\s
                  13 | [pass,type=css]
                     | ~~~~~~~~~~~~~~~
                  14 | ++++
                  15 | body { color: red; }
                  16 | ++++
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for pass content exceeding max length")
        void shouldShowUnderlineForPassContentExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for pass content
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - pass:
                            severity: error
                            content:
                              required: true
                              maxLength: 50
                              severity: error
                """;
            
            // Given - AsciiDoc content with pass content exceeding max length
            String adocContent = """
                = Test Document
                
                [pass]
                ++++
                This is an extremely long pass block content that definitely exceeds the maximum allowed length
                ++++
                
                [pass]
                ++++
                Short content
                ++++
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Pass block content is too long [pass.content.maxLength]
                  File: %s:5:1-95
                
                   2 |\s
                   3 | [pass]
                   4 | ++++
                   5 | This is an extremely long pass block content that definitely exceeds the maximum allowed length
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 | ++++
                   7 |\s
                   8 | [pass]
                
                
                """, testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for pass content not matching pattern")
        void shouldShowUnderlineForPassContentPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for pass content
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - pass:
                            severity: error
                            content:
                              required: true
                              pattern: "^<[^>]+>.*</[^>]+>$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with pass content not matching pattern
            String adocContent = """
                = Test Document
                
                [pass]
                ++++
                Not a valid XML/HTML tag
                ++++
                
                [pass]
                ++++
                <div>Valid content</div>
                ++++
                
                [pass]
                ++++
                <p>Unclosed tag
                ++++
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Pass block content does not match required pattern [pass.content.pattern]
                  File: %s:5:1-24
                
                   2 |\s
                   3 | [pass]
                   4 | ++++
                   5 | Not a valid XML/HTML tag
                     | ~~~~~~~~~~~~~~~~~~~~~~~~
                   6 | ++++
                   7 |\s
                   8 | [pass]
                
                [ERROR]: Pass block content does not match required pattern [pass.content.pattern]
                  File: %s:15:1-15
                
                  12 | ++++
                  13 |\s
                  14 | [pass]
                  15 | <p>Unclosed tag
                     | ~~~~~~~~~~~~~~~
                  16 | ++++
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for pass reason below min length")
        void shouldShowUnderlineForPassReasonBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for pass reason
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - pass:
                            severity: warn
                            reason:
                              required: true
                              minLength: 20
                              severity: warn
                """;
            
            // Given - AsciiDoc content with pass reasons below min length
            String adocContent = """
                = Test Document
                
                [pass,reason=Legacy]
                ++++
                <custom>Content</custom>
                ++++
                
                [pass,reason=For backwards compatibility with old systems]
                ++++
                <legacy>Data</legacy>
                ++++
                
                [pass,reason=Custom]
                ++++
                <special>Tag</special>
                ++++
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Pass block reason is too short [pass.reason.minLength]
                  File: %s:3:1-19
                
                   1 | = Test Document
                   2 |\s
                   3 | [pass,reason=Legacy]
                     | ~~~~~~~~~~~~~~~~~~~
                   4 | ++++
                   5 | <custom>Content</custom>
                   6 | ++++
                
                [WARN]: Pass block reason is too short [pass.reason.minLength]
                  File: %s:13:1-19
                
                  10 | <legacy>Data</legacy>
                  11 | ++++
                  12 |\s
                  13 | [pass,reason=Custom]
                     | ~~~~~~~~~~~~~~~~~~~
                  14 | ++++
                  15 | <special>Tag</special>
                  16 | ++++
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for pass reason exceeding max length")
        void shouldShowUnderlineForPassReasonExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for pass reason
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - pass:
                            severity: error
                            reason:
                              required: true
                              maxLength: 30
                              severity: error
                """;
            
            // Given - AsciiDoc content with pass reasons exceeding max length
            String adocContent = """
                = Test Document
                
                [pass,reason=This is an extremely long reason that exceeds the maximum allowed length]
                ++++
                <content>Data</content>
                ++++
                
                [pass,reason=Short reason]
                ++++
                <valid>Content</valid>
                ++++
                
                [pass,reason=Another excessively long explanation for using passthrough that should be shorter]
                ++++
                <data>Value</data>
                ++++
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Pass block reason is too long [pass.reason.maxLength]
                  File: %s:3:1-85
                
                   1 | = Test Document
                   2 |\s
                   3 | [pass,reason=This is an extremely long reason that exceeds the maximum allowed length]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ++++
                   5 | <content>Data</content>
                   6 | ++++
                
                [ERROR]: Pass block reason is too long [pass.reason.maxLength]
                  File: %s:13:1-93
                
                  10 | <valid>Content</valid>
                  11 | ++++
                  12 |\s
                  13 | [pass,reason=Another excessively long explanation for using passthrough that should be shorter]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  14 | ++++
                  15 | <data>Value</data>
                  16 | ++++
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Quote Block Validation Tests")
    class QuoteValidationTests {
        
        @Test
        @DisplayName("should show underline for quote attribution not matching pattern")
        void shouldShowUnderlineForQuoteAttributionPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with attribution pattern for quote blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - quote:
                            severity: error
                            attribution:
                              required: true
                              pattern: "^[A-Z][a-zA-Z\\s\\.]+$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with quote attributions not matching the pattern
            String adocContent = """
                = Test Document
                
                [quote, "shakespeare123", "Hamlet"]
                ____
                To be, or not to be, that is the question.
                ____
                
                [quote, "William Shakespeare", "Hamlet"]
                ____
                All the world's a stage.
                ____
                
                [quote, "@john_doe", "Twitter"]
                ____
                Hello world!
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Quote attribution does not match required pattern [quote.attribution.pattern]
                  File: %s:3:1-35
                
                   1 | = Test Document
                   2 |\s
                   3 | [quote, "shakespeare123", "Hamlet"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | To be, or not to be, that is the question.
                   6 | ____
                
                [ERROR]: Quote attribution does not match required pattern [quote.attribution.pattern]
                  File: %s:13:1-31
                
                  10 | All the world's a stage.
                  11 | ____
                  12 |\s
                  13 | [quote, "@john_doe", "Twitter"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  14 | ____
                  15 | Hello world!
                  16 | ____
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for quote citation not matching pattern")
        void shouldShowUnderlineForQuoteCitationPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with citation pattern for quote blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - quote:
                            severity: error
                            citation:
                              required: true
                              pattern: "^[A-Z][a-zA-Z0-9\\s]+, \\d{4}$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with quote citations not matching the pattern
            String adocContent = """
                = Test Document
                
                [quote, "Albert Einstein", "unknown date"]
                ____
                Imagination is more important than knowledge.
                ____
                
                [quote, "Maya Angelou", "I Know Why the Caged Bird Sings, 1969"]
                ____
                There is no greater agony than bearing an untold story inside you.
                ____
                
                [quote, "Oscar Wilde", "The Picture of Dorian Gray"]
                ____
                We are all in the gutter, but some of us are looking at the stars.
                ____
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Quote citation does not match required pattern [quote.citation.pattern]
                  File: %s:3:1-42
                
                   1 | = Test Document
                   2 |\s
                   3 | [quote, "Albert Einstein", "unknown date"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | ____
                   5 | Imagination is more important than knowledge.
                   6 | ____
                
                [ERROR]: Quote citation does not match required pattern [quote.citation.pattern]
                  File: %s:13:1-52
                
                  10 | There is no greater agony than bearing an untold story inside you.
                  11 | ____
                  12 |\s
                  13 | [quote, "Oscar Wilde", "The Picture of Dorian Gray"]
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  14 | ____
                  15 | We are all in the gutter, but some of us are looking at the stars.
                  16 | ____
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Ulist Block Validation Tests")
    class UlistValidationTests {
        
        @Test
        @DisplayName("should show underline for ulist with disallowed marker style")
        void shouldShowUnderlineForUlistDisallowedMarkerStyle(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with allowed marker styles for ulist blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - ulist:
                            severity: error
                            markerStyle: "*"
                """;
            
            // Given - AsciiDoc content with ulists using different marker styles
            String adocContent = """
                = Test Document
                
                * Valid item with asterisk
                * Another valid item
                
                - Invalid item with dash
                - Another invalid item
                
                . Invalid item with dot
                . Another invalid item with dot
                
                * Back to valid asterisk
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Unordered list marker style '-' does not match expected style '*' [ulist.markerStyle]
                  File: %s:6:1-24
                
                   3 | * Valid item with asterisk
                   4 | * Another valid item
                   5 |\s
                   6 | - Invalid item with dash
                     | ~~~~~~~~~~~~~~~~~~~~~~~~
                   7 | - Another invalid item
                   8 |\s
                   9 | . Invalid item with dot
                
                [ERROR]: Unordered list marker style '-' does not match expected style '*' [ulist.markerStyle]
                  File: %s:7:1-22
                
                   4 | * Another valid item
                   5 |\s
                   6 | - Invalid item with dash
                   7 | - Another invalid item
                     | ~~~~~~~~~~~~~~~~~~~~~~
                   8 |\s
                   9 | . Invalid item with dot
                  10 | . Another invalid item with dot
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for nested ulist with disallowed marker style")
        void shouldShowUnderlineForNestedUlistDisallowedMarkerStyle(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with allowed marker style for ulist blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - ulist:
                            severity: warn
                            markerStyle: "-"
                """;
            
            // Given - AsciiDoc content with nested ulists using different marker styles
            String adocContent = """
                = Test Document
                
                - Valid top-level item
                  * Invalid nested item with asterisk
                  * Another invalid nested item
                - Another valid top-level item
                  - Valid nested item with dash
                  - Another valid nested item
                - Third item
                  . Invalid nested item with dot
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Unordered list marker style '*' does not match expected style '-' [ulist.markerStyle]
                  File: %s:4:3-35
                
                   1 | = Test Document
                   2 |\s
                   3 | - Valid top-level item
                   4 |   * Invalid nested item with asterisk
                     |   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   5 |   * Another invalid nested item
                   6 | - Another valid top-level item
                   7 |   - Valid nested item with dash
                
                [WARN]: Unordered list marker style '*' does not match expected style '-' [ulist.markerStyle]
                  File: %s:5:3-31
                
                   2 |\s
                   3 | - Valid top-level item
                   4 |   * Invalid nested item with asterisk
                   5 |   * Another invalid nested item
                     |   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   6 | - Another valid top-level item
                   7 |   - Valid nested item with dash
                   8 |   - Another valid nested item
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Admonition Block Validation Tests")
    class AdmonitionValidationTests {
        
        @Test
        @DisplayName("should show underline for admonition with disallowed type")
        void shouldShowUnderlineForAdmonitionDisallowedType(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with allowed types for admonition blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: error
                            type:
                              required: true
                              allowed: ["NOTE", "TIP", "WARNING"]
                              severity: error
                """;
            
            // Given - AsciiDoc content with admonitions using different types
            String adocContent = """
                = Test Document
                
                NOTE: This is a valid note.
                
                TIP: This is a valid tip.
                
                IMPORTANT: This type is not allowed.
                
                WARNING: This is allowed.
                
                CAUTION: This type is also not allowed.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Admonition type 'IMPORTANT' is not allowed [admonition.type.allowed]
                  File: %s:7:1-36
                
                   4 | TIP: This is a valid tip.
                   5 |\s
                   6 |\s
                   7 | IMPORTANT: This type is not allowed.
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   8 |\s
                   9 | WARNING: This is allowed.
                  10 |\s
                
                [ERROR]: Admonition type 'CAUTION' is not allowed [admonition.type.allowed]
                  File: %s:11:1-39
                
                   8 |\s
                   9 | WARNING: This is allowed.
                  10 |\s
                  11 | CAUTION: This type is also not allowed.
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition title below min length")
        void shouldShowUnderlineForAdmonitionTitleBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for admonition title
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: warn
                            title:
                              required: true
                              minLength: 10
                              severity: warn
                """;
            
            // Given - AsciiDoc content with admonition titles below min length
            String adocContent = """
                = Test Document
                
                .Note
                NOTE: This title is too short.
                
                .Important Information
                NOTE: This title has proper length.
                
                .Tip
                TIP: Another short title.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Admonition title is too short [admonition.title.minLength]
                  File: %s:3:1-5
                
                   1 | = Test Document
                   2 |\s
                   3 | .Note
                     | ~~~~~
                   4 | NOTE: This title is too short.
                   5 |\s
                   6 | .Important Information
                
                [WARN]: Admonition title is too short [admonition.title.minLength]
                  File: %s:9:1-4
                
                   6 | .Important Information
                   7 | NOTE: This title has proper length.
                   8 |\s
                   9 | .Tip
                     | ~~~~
                  10 | TIP: Another short title.
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition title exceeding max length")
        void shouldShowUnderlineForAdmonitionTitleExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for admonition title
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: error
                            title:
                              required: true
                              maxLength: 25
                              severity: error
                """;
            
            // Given - AsciiDoc content with admonition titles exceeding max length
            String adocContent = """
                = Test Document
                
                .This is an extremely long admonition title that exceeds the maximum allowed length
                NOTE: Content here.
                
                .Short Title
                WARNING: Content here.
                
                .Another very long title that should be much shorter
                TIP: Content here.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Admonition title is too long [admonition.title.maxLength]
                  File: %s:3:1-83
                
                   1 | = Test Document
                   2 |\s
                   3 | .This is an extremely long admonition title that exceeds the maximum allowed length
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 | NOTE: Content here.
                   5 |\s
                   6 | .Short Title
                
                [ERROR]: Admonition title is too long [admonition.title.maxLength]
                  File: %s:9:1-52
                
                   6 | .Short Title
                   7 | WARNING: Content here.
                   8 |\s
                   9 | .Another very long title that should be much shorter
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                  10 | TIP: Content here.
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition title not matching pattern")
        void shouldShowUnderlineForAdmonitionTitlePatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for admonition title
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: error
                            title:
                              required: true
                              pattern: "^[A-Z][a-zA-Z0-9 ]+$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with admonition titles not matching pattern
            String adocContent = """
                = Test Document
                
                .lowercase title
                NOTE: Invalid title starting with lowercase.
                
                .Valid Title Format
                WARNING: This title matches the pattern.
                
                .Title-with-hyphens!
                TIP: Invalid title with special characters.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Admonition title does not match required pattern [admonition.title.pattern]
                  File: %s:3:1-16
                
                   1 | = Test Document
                   2 |\s
                   3 | .lowercase title
                     | ~~~~~~~~~~~~~~~~
                   4 | NOTE: Invalid title starting with lowercase.
                   5 |\s
                   6 | .Valid Title Format
                
                [ERROR]: Admonition title does not match required pattern [admonition.title.pattern]
                  File: %s:9:1-20
                
                   6 | .Valid Title Format
                   7 | WARNING: This title matches the pattern.
                   8 |\s
                   9 | .Title-with-hyphens!
                     | ~~~~~~~~~~~~~~~~~~~~
                  10 | TIP: Invalid title with special characters.
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition content below min length")
        void shouldShowUnderlineForAdmonitionContentBelowMinLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with min length for admonition content
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: warn
                            content:
                              required: true
                              minLength: 20
                              severity: warn
                """;
            
            // Given - AsciiDoc content with admonition content below min length
            String adocContent = """
                = Test Document
                
                NOTE: Too short.
                
                WARNING: This content has sufficient length to pass validation.
                
                TIP: Brief tip.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [WARN]: Admonition content is too short [admonition.content.minLength]
                  File: %s:3:1-16
                
                   1 | = Test Document
                   2 |\s
                   3 | NOTE: Too short.
                     | ~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | WARNING: This content has sufficient length to pass validation.
                   6 |\s
                
                [WARN]: Admonition content is too short [admonition.content.minLength]
                  File: %s:7:1-15
                
                   4 |\s
                   5 | WARNING: This content has sufficient length to pass validation.
                   6 |\s
                   7 | TIP: Brief tip.
                     | ~~~~~~~~~~~~~~~
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition content exceeding max length")
        void shouldShowUnderlineForAdmonitionContentExceedingMaxLength(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with max length for admonition content
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: error
                            content:
                              required: true
                              maxLength: 50
                              severity: error
                """;
            
            // Given - AsciiDoc content with admonition content exceeding max length
            String adocContent = """
                = Test Document
                
                NOTE: This is an extremely long admonition content that definitely exceeds the maximum allowed length.
                
                WARNING: Short and concise warning message.
                
                TIP: Another excessively long tip content that should be shortened to comply with the maximum length rules.
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Admonition content is too long [admonition.content.maxLength]
                  File: %s:3:1-101
                
                   1 | = Test Document
                   2 |\s
                   3 | NOTE: This is an extremely long admonition content that definitely exceeds the maximum allowed length.
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   4 |\s
                   5 | WARNING: Short and concise warning message.
                   6 |\s
                
                [ERROR]: Admonition content is too long [admonition.content.maxLength]
                  File: %s:7:1-106
                
                   4 |\s
                   5 | WARNING: Short and concise warning message.
                   6 |\s
                   7 | TIP: Another excessively long tip content that should be shortened to comply with the maximum length rules.
                     | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for admonition icon not matching pattern")
        void shouldShowUnderlineForAdmonitionIconPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for admonition icon
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - admonition:
                            severity: error
                            icon:
                              required: true
                              pattern: "^icon:[a-z-]+\\[\\]$"
                              severity: error
                """;
            
            // Given - AsciiDoc content with admonition icons not matching pattern
            String adocContent = """
                = Test Document
                
                [NOTE,icon=warning]
                ====
                Invalid icon format.
                ====
                
                [TIP,icon:lightbulb[]]
                ====
                Valid icon format.
                ====
                
                [WARNING,icon:ALERT[]]
                ====
                Invalid uppercase icon.
                ====
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Admonition icon does not match required pattern [admonition.icon.pattern]
                  File: %s:3:1-19
                
                   1 | = Test Document
                   2 |\s
                   3 | [NOTE,icon=warning]
                     | ~~~~~~~~~~~~~~~~~~~
                   4 | ====
                   5 | Invalid icon format.
                   6 | ====
                
                [ERROR]: Admonition icon does not match required pattern [admonition.icon.pattern]
                  File: %s:13:1-22
                
                  10 | Valid icon format.
                  11 | ====
                  12 |\s
                  13 | [WARNING,icon:ALERT[]]
                     | ~~~~~~~~~~~~~~~~~~~~~~
                  14 | ====
                  15 | Invalid uppercase icon.
                  16 | ====
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
    
    @Nested
    @DisplayName("Listing Block Validation Tests")
    class ListingValidationTests {
        
        @Test
        @DisplayName("should show underline for listing block with disallowed language")
        void shouldShowUnderlineForListingDisallowedLanguage(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with allowed languages for listing blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - listing:
                            severity: error
                            language:
                              required: true
                              allowed: ["java", "python", "javascript", "xml"]
                              severity: error
                """;
            
            // Given - AsciiDoc content with listing blocks using different languages
            String adocContent = """
                = Test Document
                
                [source,java]
                ----
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello World");
                    }
                }
                ----
                
                [source,ruby]
                ----
                puts "Hello World"
                ----
                
                [source,python]
                ----
                print("Hello World")
                ----
                
                [source,golang]
                ----
                fmt.Println("Hello World")
                ----
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Listing language 'ruby' is not allowed [listing.language.allowed]
                  File: %s:12:1-13
                
                   9 | }
                  10 | ----
                  11 |\s
                  12 | [source,ruby]
                     | ~~~~~~~~~~~~~
                  13 | ----
                  14 | puts "Hello World"
                  15 | ----
                
                [ERROR]: Listing language 'golang' is not allowed [listing.language.allowed]
                  File: %s:22:1-15
                
                  19 | print("Hello World")
                  20 | ----
                  21 |\s
                  22 | [source,golang]
                     | ~~~~~~~~~~~~~~~
                  23 | ----
                  24 | fmt.Println("Hello World")
                  25 | ----
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for listing title not matching pattern")
        void shouldShowUnderlineForListingTitlePatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with title pattern for listing blocks
            String rules = """
                document:
                  sections:
                    - level: 0
                      allowedBlocks:
                        - listing:
                            severity: error
                            title:
                              required: true
                              pattern: "^(Listing|Example|Code) \\d+\\..+"
                              severity: error
                """;
            
            // Given - AsciiDoc content with listing blocks having titles
            String adocContent = """
                = Test Document
                
                .Invalid title format
                [source,java]
                ----
                public class Test {}
                ----
                
                .Listing 1. Valid Java Example
                [source,java]
                ----
                public class Valid {}
                ----
                
                .My Code Sample
                [source,python]
                ----
                def hello():
                    pass
                ----
                
                .Example 2. Another valid title
                [source,xml]
                ----
                <root/>
                ----
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Listing title does not match required pattern [listing.title.pattern]
                  File: %s:3:1-21
                
                   1 | = Test Document
                   2 |\s
                   3 | .Invalid title format
                     | ~~~~~~~~~~~~~~~~~~~~~
                   4 | [source,java]
                   5 | ----
                   6 | public class Test {}
                
                [ERROR]: Listing title does not match required pattern [listing.title.pattern]
                  File: %s:15:1-15
                
                  12 | public class Valid {}
                  13 | ----
                  14 |\s
                  15 | .My Code Sample
                     | ~~~~~~~~~~~~~~~
                  16 | [source,python]
                  17 | ----
                  18 | def hello():
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
        
        @Test
        @DisplayName("should show underline for listing block without language when required")
        void shouldShowUnderlineForListingMissingLanguage(@TempDir Path tempDir) throws IOException {
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
            
            // Given - AsciiDoc content with listing blocks without language
            String adocContent = """
                = Test Document
                
                [source,java]
                ----
                // Valid listing with language
                ----
                
                ----
                Plain listing without language
                ----
                
                [listing]
                ----
                Another listing without source language
                ----
                """;
            
            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);
            
            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String.format("""
                Validation Report
                =================
                
                %s:
                
                [ERROR]: Listing language is required [listing.language.required]
                  File: %s:8:1-4
                
                   5 | // Valid listing with language
                   6 | ----
                   7 |\s
                   8 | ----
                     | ~~~~
                   9 | Plain listing without language
                  10 | ----
                  11 |\s
                
                [ERROR]: Listing language is required [listing.language.required]
                  File: %s:12:1-9
                
                   9 | Plain listing without language
                  10 | ----
                  11 |\s
                  12 | [listing]
                     | ~~~~~~~~~
                  13 | ----
                  14 | Another listing without source language
                  15 | ----
                
                
                """, testFile.toString(), testFile.toString(), testFile.toString());
            
            assertEquals(expectedOutput, actualOutput);
        }
    }
}