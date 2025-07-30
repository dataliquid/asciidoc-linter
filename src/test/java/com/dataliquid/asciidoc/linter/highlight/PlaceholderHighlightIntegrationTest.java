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
        return createDefaultOutputConfig(3);
    }
    
    /**
     * Creates output configuration with specified context lines.
     */
    private OutputConfiguration createDefaultOutputConfig(int contextLines) {
        return OutputConfiguration.builder()
            .format(OutputFormat.ENHANCED)
            .display(DisplayConfig.builder()
                .contextLines(contextLines)
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
        return validateAndFormat(rules, adocContent, tempDir, createDefaultOutputConfig());
    }
    
    /**
     * Validates content and returns the formatted console output with custom config.
     */
    private String validateAndFormat(String rules, String adocContent, Path tempDir, OutputConfiguration outputConfig) throws IOException {
        // Clear any previous output
        stringWriter.getBuffer().setLength(0);
        
        // Create temporary file with content
        Path testFile = tempDir.resolve("test.adoc");
        Files.writeString(testFile, adocContent);
        
        // Load configuration and validate
        LinterConfiguration config = configLoader.loadConfiguration(rules);
        ValidationResult result = linter.validateFile(testFile, config);
        
        // Format output
        ConsoleFormatter formatter = new ConsoleFormatter(outputConfig);
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
            
            [ERROR]: Listing language is required [listing.language.required]
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
    
    //@Test
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
    
    //@Test
    @DisplayName("should show placeholder for missing audio URL")
    void shouldShowPlaceholderForMissingAudioUrl(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring URL for audio blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - audio:
                        severity: error
                        url:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing URL
        String adocContent = """
            = Test Document
            
            audio::[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Audio URL is required but not provided [audio.url.required]
              File: %s:3:8
            
               1 | = Test Document
               2 |\s
               3 | audio::«target»[]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing audio title")
    void shouldShowPlaceholderForMissingAudioTitle(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring title for audio blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - audio:
                        severity: error
                        title:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing caption
        String adocContent = """
            = Test Document
            
            audio::example.mp3[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Audio title is required but not provided [audio.title.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «.Audio Title»
               4 | audio::example.mp3[]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for missing audio controls")
    void shouldShowPlaceholderForMissingAudioControls(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring controls for audio blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - audio:
                        severity: error
                        options:
                          controls:
                            required: true
            """;
        
        // Given - AsciiDoc content without controls
        String adocContent = """
            = Test Document
            
            audio::example.mp3[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Audio controls are required but not enabled [audio.options.controls.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | audio::example.mp3[options=«controls»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for missing audio autoplay")
    void shouldShowPlaceholderForMissingAudioAutoplay(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring autoplay for audio blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - audio:
                        severity: error
                        options:
                          autoplay:
                            required: true
            """;
        
        // Given - AsciiDoc content without autoplay
        String adocContent = """
            = Test Document
            
            audio::example.mp3[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Audio autoplay is required but not enabled [audio.autoplay.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | audio::example.mp3[options=«autoplay»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for missing audio loop")
    void shouldShowPlaceholderForMissingAudioLoop(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring loop for audio blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - audio:
                        severity: error
                        options:
                          loop:
                            required: true
            """;
        
        // Given - AsciiDoc content without loop
        String adocContent = """
            = Test Document
            
            audio::example.mp3[]
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Audio loop is required but not enabled [audio.loop.required]
              File: %s:3:20
            
               1 | = Test Document
               2 |\s
               3 | audio::example.mp3[options=«loop»]
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing table caption")
    void shouldShowPlaceholderForMissingTableCaption(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring caption for table blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - table:
                        severity: error
                        caption:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing caption
        String adocContent = """
            = Test Document
            
            |===
            | Column 1 | Column 2
            | Data 1 | Data 2
            |===
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Table caption is required but not provided [table.caption.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «.Table Title»
               4 | |===
               5 | | Column 1 | Column 2
               6 | | Data 1 | Data 2
               7 | |===
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing table header")
    void shouldShowPlaceholderForMissingTableHeader(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring header for table blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - table:
                        severity: error
                        header:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing header
        String adocContent = """
            = Test Document
            
            |===
            | Data 1 | Data 2
            | Data 3 | Data 4
            |===
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Table header is required but not provided [table.header.required]
              File: %s:4:1
            
               1 | = Test Document
               2 |\s
               3 | |===
               4 | «| Header 1 | Header 2»
               5 | | Data 1 | Data 2
               6 | | Data 3 | Data 4
               7 | |===
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for table with too few columns")
    void shouldShowPlaceholderForTableWithTooFewColumns(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum columns for table blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - table:
                        severity: error
                        columns:
                          min: 3
            """;
        
        // Given - AsciiDoc content with only 2 columns
        String adocContent = """
            = Test Document
            
            |===
            | Column 1 | Column 2
            | Data 1 | Data 2
            |===
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Table has too few columns [table.columns.min]
              File: %s:4:21
            
               1 | = Test Document
               2 |\s
               3 | |===
               4 | | Column 1 | Column 2 «| Column 3»
               5 | | Data 1 | Data 2
               6 | |===
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for table with too few rows")
    void shouldShowPlaceholderForTableWithTooFewRows(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum rows for table blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - table:
                        severity: error
                        rows:
                          min: 3
            """;
        
        // Given - AsciiDoc content with only 2 rows (including header)
        String adocContent = """
            = Test Document
            
            |===
            | Column 1 | Column 2
            | Data 1 | Data 2
            |===
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Table has too few rows [table.rows.min]
              File: %s:5:17
            
               1 | = Test Document
               2 |\s
               3 | |===
               4 | | Column 1 | Column 2
               5 | | Data 1 | Data 2
               6 | «| Data 3 | Data 4»
               7 | |===
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing section when min-occurrences not met")
    void shouldShowPlaceholderForMissingSectionMinOccurrences(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring a section with min occurrences
        String rules = """
            document:
              sections:
                - level: 0
                  subsections:
                    - name: headerTypeRule
                      order: 1
                      level: 1
                      min: 1
                      max: 1
            """;
        
        // Given - AsciiDoc content missing the required section
        // Note: The string has line 1: title, line 2: empty, line 3: content
        String adocContent = """
            = Test Document
            
            Some content without the required section.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Too few occurrences of section: headerTypeRule [section.min-occurrences]
              File: %s:1
            
               1 | = Test Document
               2 |\s
               3 | «== headerTypeRule»
               4 | Some content without the required section.
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing level 0 section when min-occurrences not met")
    void shouldShowPlaceholderForMissingLevel0SectionMinOccurrences(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring a level 0 section with min occurrences
        String rules = """
            document:
              sections:
                # Document title (Level 0) - HTTP Header Name
                - name: headerTitleRule
                  level: 0
                  min: 1
                  max: 1
                  title:
                    pattern: "^[A-Za-z][A-Za-z0-9-]*$"
                    severity: error
            """;
        
        // Given - AsciiDoc content missing the required level 0 section
        String adocContent = """
            Some content without any document title.
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Too few occurrences of section: headerTitleRule [section.min-occurrences]
              File: %s:1
            
               1 | «= headerTitleRule»
               2 | Some content without any document title.
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing level 4 section when min-occurrences not met")
    void shouldShowPlaceholderForMissingLevel4SectionMinOccurrences(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring a level 4 section with min occurrences
        String rules = """
            document:
              sections:
                - level: 0
                  subsections:
                    - name: introduction
                      level: 1
                      subsections:
                        - name: overview
                          level: 2
                          subsections:
                            - name: details
                              level: 3
                              subsections:
                                - name: implementation
                                  level: 4
                                  min: 1
                                  max: 1
            """;
        
        // Given - AsciiDoc content with nested sections but missing the required level 4 section
        String adocContent = """
            = Test Document
            
            == Introduction
            
            === Overview
            
            ==== Details
            
            Some content here but missing the required implementation section.
            """;
        
        // When - Validate and format output (with more context lines to show the full structure)
        String actualOutput = validateAndFormat(rules, adocContent, tempDir, createDefaultOutputConfig(10));
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Too few occurrences of section: implementation [section.min-occurrences]
              File: %s:1
            
               1 | = Test Document
               2 |\s
               3 | == Introduction
               4 |\s
               5 | === Overview
               6 |\s
               7 | ==== Details
               8 |\s
               9 | Some content here but missing the required implementation section.
              10 | «===== implementation»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing quote attribution")
    void shouldShowPlaceholderForMissingQuoteAttribution(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring attribution for quote blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - quote:
                        severity: error
                        attribution:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing attribution
        String adocContent = """
            = Test Document
            
            [quote]
            ____
            This is a quote without attribution.
            ____
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Quote attribution is required but not provided [quote.attribution.required]
              File: %s:3:7
            
               1 | = Test Document
               2 |\s
               3 | [quote,«attribution»]
               4 | ____
               5 | This is a quote without attribution.
               6 | ____
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing quote citation")
    void shouldShowPlaceholderForMissingQuoteCitation(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring citation for quote blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - quote:
                        severity: error
                        citation:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing citation
        String adocContent = """
            = Test Document
            
            [quote,John Doe]
            ____
            This is a quote without citation.
            ____
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Quote citation is required but not provided [quote.citation.required]
              File: %s:3:16
            
               1 | = Test Document
               2 |\s
               3 | [quote,John Doe,«citation»]
               4 | ____
               5 | This is a quote without citation.
               6 | ____
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing example caption")
    void shouldShowPlaceholderForMissingExampleCaption(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring caption for example blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - example:
                        severity: error
                        caption:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing caption
        String adocContent = """
            = Test Document
            
            ====
            This is an example block without a caption.
            ====
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Example block requires a caption [example.caption.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «.Example Title»
               4 | ====
               5 | This is an example block without a caption.
               6 | ====
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing example collapsible attribute")
    void shouldShowPlaceholderForMissingExampleCollapsible(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring collapsible for example blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - example:
                        severity: error
                        collapsible:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing collapsible attribute
        String adocContent = """
            = Test Document
            
            ====
            This is an example block without collapsible attribute.
            ====
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Example block requires a collapsible attribute [example.collapsible.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «[%%collapsible]»
               4 | ====
               5 | This is an example block without collapsible attribute.
               6 | ====
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    //@Test
    @DisplayName("should show placeholder for missing admonition type")
    void shouldShowPlaceholderForMissingAdmonitionType(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring type for admonition blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - admonition:
                        severity: error
                        type:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing admonition type
        String adocContent = """
            = Test Document
            
            []
            ====
            This is an admonition without a type.
            ====
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Admonition block must have a type [admonition.type.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | [«NOTE»]
               4 | ====
               5 | This is an admonition without a type.
               6 | ====
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing admonition title")
    void shouldShowPlaceholderForMissingAdmonitionTitle(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring title for admonition blocks
        String rules = """
            document:
            
              sections:
                - level: 0
                  allowedBlocks:
                    - admonition:
                        severity: error
                        title:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing admonition title
        String adocContent = """
            = Test Document
            
            [NOTE]
            ====
            This is an admonition without a title.
            ====
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Admonition block requires a title [admonition.title.required]
              File: %s:4-1
            
               1 | = Test Document
               2 |\s
               3 | «.Title»
               4 | [NOTE]
               5 | ====
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing admonition content")
    void shouldShowPlaceholderForMissingAdmonitionContent(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring content for admonition blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - admonition:
                        severity: error
                        content:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with empty admonition
        String adocContent = """
            = Test Document
            
            [NOTE]
            ====
            ====
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Admonition block requires content [admonition.content.required]
              File: %s:4-1
            
               1 | = Test Document
               2 |\s
               3 | [NOTE]
               4 | ====
               5 | «Content»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing sidebar title")
    void shouldShowPlaceholderForMissingSidebarTitle(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring title for sidebar blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - sidebar:
                        severity: error
                        title:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing sidebar title
        String adocContent = """
            = Test Document
            
            ****
            This is a sidebar without a title.
            ****
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Sidebar block requires a title [sidebar.title.required]
              File: %s:3-1
            
               1 | = Test Document
               2 | «.Sidebar Title»
               3 |\s
               4 | ****
               5 | This is a sidebar without a title.
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing sidebar content")
    void shouldShowPlaceholderForMissingSidebarContent(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring content for sidebar blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - sidebar:
                        severity: error
                        content:
                          required: true
            """;
        
        // Given - AsciiDoc content with empty sidebar
        String adocContent = """
            = Test Document
            
            ****
            ****
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Sidebar block requires content [sidebar.content.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | ****
               4 | «Content»
               5 | ****
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing sidebar position")
    void shouldShowPlaceholderForMissingSidebarPosition(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring position for sidebar blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - sidebar:
                        severity: error
                        position:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing position attribute
        String adocContent = """
            = Test Document
            
            ****
            This is a sidebar without a position attribute.
            ****
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Sidebar block requires a position attribute [sidebar.position.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «[position=left]»
               4 | ****
               5 | This is a sidebar without a position attribute.
               6 | ****
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing verse author")
    void shouldShowPlaceholderForMissingVerseAuthor(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring author for verse blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - verse:
                        severity: error
                        author:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing verse author
        String adocContent = """
            = Test Document
            
            [verse]
            ____
            Roses are red,
            Violets are blue.
            ____
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Verse author is required but not provided [verse.author.required]
              File: %s:3:7
            
               1 | = Test Document
               2 |\s
               3 | [verse,«author»]
               4 | ____
               5 | Roses are red,
               6 | Violets are blue.
               7 | ____
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing verse attribution")
    void shouldShowPlaceholderForMissingVerseAttribution(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring attribution for verse blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - verse:
                        severity: error
                        attribution:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing verse attribution
        String adocContent = """
            = Test Document
            
            [verse]
            ____
            Roses are red,
            Violets are blue.
            ____
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Verse attribution is required but not provided [verse.attribution.required]
              File: %s:3:7
            
               1 | = Test Document
               2 |\s
               3 | [verse,«attribution»]
               4 | ____
               5 | Roses are red,
               6 | Violets are blue.
               7 | ____
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing verse content")
    void shouldShowPlaceholderForMissingVerseContent(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring content for verse blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - verse:
                        severity: error
                        content:
                          required: true
            """;
        
        // Given - AsciiDoc content with empty verse
        String adocContent = """
            = Test Document
            
            [verse]
            ____
            ____
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Verse block requires content [verse.content.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | [verse]
               4 | ____
               5 | «Content»
               6 | ____
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing pass type")
    void shouldShowPlaceholderForMissingPassType(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring type for pass blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - pass:
                        severity: error
                        type:
                          required: true
                          severity: error
            """;
        
        // Given - AsciiDoc content with missing pass type
        String adocContent = """
            = Test Document
            
            ++++
            <p>This is a pass block without a type.</p>
            ++++
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Pass block requires a type [pass.type.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «[pass,type=html]»
               4 | ++++
               5 | <p>This is a pass block without a type.</p>
               6 | ++++
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing pass content")
    void shouldShowPlaceholderForMissingPassContent(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring content for pass blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - pass:
                        severity: error
                        content:
                          required: true
            """;
        
        // Given - AsciiDoc content with empty pass block
        String adocContent = """
            = Test Document
            
            ++++
            ++++
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Pass block requires content [pass.content.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | ++++
               4 | «Content»
               5 | ++++
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing pass reason")
    void shouldShowPlaceholderForMissingPassReason(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring reason for pass blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - pass:
                        severity: error
                        reason:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing reason
        String adocContent = """
            = Test Document
            
            ++++
            <p>This is a pass block without a reason.</p>
            ++++
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Pass block requires a reason [pass.reason.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «[pass,reason="explanation"]»
               4 | ++++
               5 | <p>This is a pass block without a reason.</p>
               6 | ++++
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing dlist description")
    void shouldShowPlaceholderForMissingDlistDescription(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring descriptions for dlist blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - dlist:
                        severity: error
                        descriptions:
                          required: true
            """;
        
        // Given - AsciiDoc content with term but missing description
        String adocContent = """
            = Test Document
            
            Term1::
            Term2::
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Definition list term missing required description [dlist.descriptions.required]
              File: %s:3:1-5
            
               1 | = Test Document
               2 |\s
               3 | Term1:: «Description»
               4 | Term2::
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing literal block title")
    void shouldShowPlaceholderForMissingLiteralBlockTitle(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring title for literal blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - literal:
                        severity: error
                        title:
                          required: true
            """;
        
        // Given - AsciiDoc content with missing title
        String adocContent = """
            = Test Document
            
            ....
            This is a literal block without a title.
            ....
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Literal block requires a title [literal.title.required]
              File: %s:3:1
            
               1 | = Test Document
               2 |\s
               3 | «.Title»
               4 | ....
               5 | This is a literal block without a title.
               6 | ....
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for literal block with insufficient indentation")
    void shouldShowPlaceholderForLiteralBlockInsufficientIndentation(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum indentation for literal blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - literal:
                        severity: error
                        indentation:
                          required: true
                          minSpaces: 4
            """;
        
        // Given - AsciiDoc content with insufficient indentation
        String adocContent = """
            = Test Document
            
            ....
            This line has no indentation.
              This line has only 2 spaces.
            ....
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Literal block requires minimum indentation of 4 spaces [literal.indentation.minSpaces]
              File: %s:4:1
            
               1 | = Test Document
               2 |\s
               3 | ....
               4 | «    »This line has no indentation.
               5 |   This line has only 2 spaces.
               6 | ....
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for ulist with too few items")
    void shouldShowPlaceholderForUlistWithTooFewItems(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum items for ulist blocks
        String rules = """
            document:
              sections:
                - level: 0
                  allowedBlocks:
                    - ulist:
                        severity: error
                        items:
                          min: 3
            """;
        
        // Given - AsciiDoc content with only 2 items
        String adocContent = """
            = Test Document
            
            * First item
            * Second item
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Unordered list has too few items [ulist.items.min]
              File: %s:4:14
            
               1 | = Test Document
               2 |\s
               3 | * First item
               4 | * Second item
               5 | «* Item»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    @DisplayName("should show placeholder for missing paragraph block when occurrence.min not met")
    void shouldShowPlaceholderForMissingParagraphBlockWithOccurrenceMin(@TempDir Path tempDir) throws IOException {
        // Given - YAML rules requiring minimum occurrence of paragraph block
        String rules = """
            document:
              sections:
                - name: headerTitleRule
                  level: 0
                  min: 1
                  max: 1
                  title:
                    pattern: "^[A-Za-z][A-Za-z0-9-]*$"
                    severity: error
                  allowedBlocks:
                   - paragraph:
                       severity: error
                       occurrence:
                         min: 1
                         max: 1
            """;
        
        // Given - AsciiDoc content with only title, no paragraph
        String adocContent = """
            = Accept
            """;
        
        // When - Validate and format output
        String actualOutput = validateAndFormat(rules, adocContent, tempDir);
        
        // Then - Verify exact console output with placeholder
        Path testFile = tempDir.resolve("test.adoc");
        String expectedOutput = String.format("""
            Validation Report
            =================
            
            %s:
            
            [ERROR]: Too few occurrences of block: paragraph [block.occurrence.min]
              File: %s:1
            
               1 | = Accept
               2 | «Paragraph content»
            
            
            """, testFile.toString(), testFile.toString());
        
        assertEquals(expectedOutput, actualOutput);
    }
    
}