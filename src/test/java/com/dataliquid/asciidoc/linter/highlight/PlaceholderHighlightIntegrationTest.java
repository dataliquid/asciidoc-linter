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
    
    /**
     * Creates the default output configuration for enhanced placeholder display.
     */
    private OutputConfiguration createDefaultOutputConfig() {
        return OutputConfiguration.builder()
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
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
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
    
    // TODO: Check later - Asciidoctor doesn't recognize image::[] as an image block when URL is empty
    // @Test
    @DisplayName("should show placeholder for missing image URL")
    void shouldShowPlaceholderForMissingImageUrl(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring URL for image blocks at document level
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - image:
                        severity: error
                        url:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing image URL
        String adocContent = """
            = Test Document
            
            image::[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Image must have a URL [image.url.required]
              File: %s:3:8
            
               1 | = Test Document
               2 | 
               3 | image::«filename.png»[]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing alt text")
    void shouldShowPlaceholderForMissingAltText(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring alt text for image blocks at document level
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - image:
                        severity: error
                        alt:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing alt text
        String adocContent = """
            = Test Document
            
            image::example.png[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Image must have alt text [image.alt.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | image::example.png[«Alt text»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing width attribute")
    void shouldShowPlaceholderForMissingWidth(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring width for image blocks at document level
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - image:
                        severity: error
                        width:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing width attribute
        String adocContent = """
            = Test Document
            
            image::example.png[alt="Example image"]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Image must have width specified [image.width.required]
              File: %s:3:39
            
               1 | = Test Document
               2 |\s
               3 | image::example.png[alt="Example image",width=«100»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing height attribute")
    void shouldShowPlaceholderForMissingHeight(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring height for image blocks at document level
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - image:
                        severity: error
                        height:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing height attribute
        String adocContent = """
            = Test Document
            
            image::example.png[alt="Example image",width=200]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Image must have height specified [image.height.required]
              File: %s:3:49
            
               1 | = Test Document
               2 |\s
               3 | image::example.png[alt="Example image",width=200,height=«100»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for paragraph with too few lines")
    void shouldShowPlaceholderForParagraphWithTooFewLines(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum 2 lines for paragraphs
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - paragraph:
                        severity: error
                        lines:
                          min: 2
                          severity: error
            """;
        
        // Given - AsciiDoc content with single-line paragraph
        String adocContent = """
            = Test Document
            
            This is a single line paragraph.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Paragraph has too few lines [paragraph.lines.min]
              File: %s:3:33
            
               1 | = Test Document
               2 |\s
               3 | This is a single line paragraph.
               4 | «Add more content here...»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for paragraph missing second line")
    void shouldShowPlaceholderForParagraphMissingSecondLine(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum 3 lines for paragraphs
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - paragraph:
                        severity: error
                        lines:
                          min: 3
                          severity: warn
            """;
        
        // Given - AsciiDoc content with two-line paragraph
        String adocContent = """
            = Test Document
            
            This is the first line of content.
            This is the second line of content.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [WARN]: Paragraph has too few lines [paragraph.lines.min]
              File: %s:4:36
            
               1 | = Test Document
               2 |\s
               3 | This is the first line of content.
               4 | This is the second line of content.
               5 | «Add more content here...»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for paragraph with one line when three required")
    void shouldShowPlaceholderForParagraphWithOneLineWhenThreeRequired(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum 3 lines for paragraphs
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - paragraph:
                        severity: error
                        lines:
                          min: 3
                          severity: error
            """;
        
        // Given - AsciiDoc content with single-line paragraph
        String adocContent = """
            = Test Document
            
            This is only one line when three are required.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Paragraph has too few lines [paragraph.lines.min]
              File: %s:3:47
            
               1 | = Test Document
               2 |\s
               3 | This is only one line when three are required.
               4 | «Add more content here...»
               5 | «Add more content here...»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video URL")
    void shouldShowPlaceholderForMissingVideoUrl(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring URL for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        url:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing video URL
        String adocContent = """
            = Test Document
            
            video::[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video URL is required but not provided [video.url.required]
              File: %s:3:8
            
               1 | = Test Document
               2 |\s
               3 | video::«target»[]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video width")
    void shouldShowPlaceholderForMissingVideoWidth(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring width for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        width:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing width
        String adocContent = """
            = Test Document
            
            video::example.mp4[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video width is required but not provided [video.width.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | video::example.mp4[width=«640»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video height")
    void shouldShowPlaceholderForMissingVideoHeight(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring height for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        height:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing height
        String adocContent = """
            = Test Document
            
            video::example.mp4[width=640]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video height is required but not provided [video.height.required]
              File: %s:3:29
            
               1 | = Test Document
               2 |\s
               3 | video::example.mp4[width=640,height=«360»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video poster")
    void shouldShowPlaceholderForMissingVideoPoster(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring poster for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        poster:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing poster
        String adocContent = """
            = Test Document
            
            video::example.mp4[width=640,height=360]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video poster image is required but not provided [video.poster.required]
              File: %s:3:40
            
               1 | = Test Document
               2 |\s
               3 | video::example.mp4[width=640,height=360,poster=«thumbnail.jpg»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video controls")
    void shouldShowPlaceholderForMissingVideoControls(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring controls for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        options:
                          controls:
                            required: true
            """;
        
        // Given - AsciiDoc content with missing controls
        String adocContent = """
            = Test Document
            
            video::example.mp4[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video controls are required but not enabled [video.controls.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | video::example.mp4[options=«controls»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing video caption")
    void shouldShowPlaceholderForMissingVideoCaption(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring caption for video blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - video:
                        severity: error
                        caption:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing caption
        String adocContent = """
            = Test Document
            
            video::example.mp4[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Video caption is required but not provided [video.caption.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «.Video Title»
               4 | video::example.mp4[]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
}