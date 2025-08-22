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
 * Integration test for underline highlighting in console output. Tests that
 * validation errors properly underline problematic text areas, especially for
 * maxLength and minLength validation rules.
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
        return OutputConfiguration
                .builder()
                .format(OutputFormat.ENHANCED)
                .display(DisplayConfig
                        .builder()
                        .contextLines(3)
                        .useColors(false) // No colors for easier testing
                        .showLineNumbers(true)
                        .showHeader(true) // Enable header to match expected output
                        .highlightStyle(HighlightStyle.UNDERLINE) // Enable underline highlighting
                        .maxLineWidth(120)
                        .build())
                .suggestions(SuggestionsConfig.builder().enabled(false).build())
                .errorGrouping(ErrorGroupingConfig
                        .builder()
                        .enabled(false) // Disable error grouping for predictable
                                        // output
                        .build())
                .summary(SummaryConfig.builder().enabled(false).build())
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Definition list term is too long [dlist.terms.maxLength]
                              File: %s:3:1-71
                              Actual: This is a very long definition list term that exceeds twenty characters (length: 71)
                              Expected: Maximum length: 20

                               1 | = Test Document
                               2 |\s
                               3 | This is a very long definition list term that exceeds twenty characters::
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 |   The description for the long term.
                               5 |\s
                               6 | Short term::


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Definition list term is too short [dlist.terms.minLength]
                              File: %s:3:1-3
                              Actual: API (length: 3)
                              Expected: Minimum length: 10

                               1 | = Test Document
                               2 |\s
                               3 | API::
                                 | ~~~
                               4 |   Application Programming Interface
                               5 |\s
                               6 | Term::

                            [WARN]: Definition list term is too short [dlist.terms.minLength]
                              File: %s:6:1-4
                              Actual: Term (length: 4)
                              Expected: Minimum length: 10

                               3 | API::
                               4 |   Application Programming Interface
                               5 |\s
                               6 | Term::
                                 | ~~~~
                               7 |   This term is too short


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Definition list term does not match required pattern [dlist.terms.pattern]
                              File: %s:3:1-14
                              Actual: lowercase_term
                              Expected: Pattern: ^[A-Z][A-Z0-9_]+$

                               1 | = Test Document
                               2 |\s
                               3 | lowercase_term::
                                 | ~~~~~~~~~~~~~~
                               4 |   This term starts with lowercase.
                               5 |\s
                               6 | VALID_TERM::

                            [ERROR]: Definition list term does not match required pattern [dlist.terms.pattern]
                              File: %s:9:1-10
                              Actual: Mixed-Case
                              Expected: Pattern: ^[A-Z][A-Z0-9_]+$

                               6 | VALID_TERM::
                               7 |   This term matches the pattern.
                               8 |\s
                               9 | Mixed-Case::
                                 | ~~~~~~~~~~
                              10 |   This term has invalid characters.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^https?://.*\\\\.(jpg|jpeg|png|gif|svg)$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Image URL does not match required pattern [image.url.pattern]
                              File: %s:3:8-35
                              Actual: file:///local/path/image.png
                              Expected: Pattern: ^https?://.*\\.(jpg|jpeg|png|gif|svg)$

                               1 | = Test Document
                               2 |\s
                               3 | image::file:///local/path/image.png[Local file]
                                 |        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 |\s
                               5 | image::https://example.com/image.bmp[Wrong format]
                               6 |\s

                            [ERROR]: Image URL does not match required pattern [image.url.pattern]
                              File: %s:5:8-36
                              Actual: https://example.com/image.bmp
                              Expected: Pattern: ^https?://.*\\.(jpg|jpeg|png|gif|svg)$

                               2 |\s
                               3 | image::file:///local/path/image.png[Local file]
                               4 |\s
                               5 | image::https://example.com/image.bmp[Wrong format]
                                 |        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 |\s
                               7 | image::https://example.com/valid.png[Valid image]


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Image alt text is too long [image.alt.maxLength]
                              File: %s:3:20-95
                              Actual: 76 characters
                              Expected: At most 30 characters

                               1 | = Test Document
                               2 |\s
                               3 | image::diagram.png[This is a very long alternative text that exceeds the maximum allowed length]
                                 |                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 |\s
                               5 | image::logo.png[Short alt text]
                               6 |\s

                            [ERROR]: Image alt text is too long [image.alt.maxLength]
                              File: %s:7:18-91
                              Actual: 74 characters
                              Expected: At most 30 characters

                               4 |\s
                               5 | image::logo.png[Short alt text]
                               6 |\s
                               7 | image::chart.png[Another extremely long alternative text description that should be shorter]
                                 |                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Image alt text is too short [image.alt.minLength]
                              File: %s:3:17-20
                              Actual: 4 characters
                              Expected: At least 10 characters

                               1 | = Test Document
                               2 |\s
                               3 | image::icon.png[Logo]
                                 |                 ~~~~
                               4 |\s
                               5 | image::screenshot.png[Application screenshot showing main window]
                               6 |\s

                            [WARN]: Image alt text is too short [image.alt.minLength]
                              File: %s:7:16-17
                              Actual: 2 characters
                              Expected: At least 10 characters

                               4 |\s
                               5 | image::screenshot.png[Application screenshot showing main window]
                               6 |\s
                               7 | image::btn.png[OK]
                                 |                ~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^https?://.*\\\\.(mp4|webm|ogg|avi)$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Video URL does not match required pattern [video.url.pattern]
                              File: %s:3:8-30
                              Actual: file:///local/video.mp4
                              Expected: ^https?://.*\\.(mp4|webm|ogg|avi)$

                               1 | = Test Document
                               2 |\s
                               3 | video::file:///local/video.mp4[Local video file]
                                 |        ~~~~~~~~~~~~~~~~~~~~~~~
                               4 |\s
                               5 | video::https://example.com/video.mov[Unsupported format]
                               6 |\s

                            [ERROR]: Video URL does not match required pattern [video.url.pattern]
                              File: %s:5:8-36
                              Actual: https://example.com/video.mov
                              Expected: ^https?://.*\\.(mp4|webm|ogg|avi)$

                               2 |\s
                               3 | video::file:///local/video.mp4[Local video file]
                               4 |\s
                               5 | video::https://example.com/video.mov[Unsupported format]
                                 |        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 |\s
                               7 | video::https://example.com/demo.mp4[Valid video]


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^https?://.*\\\\.(jpg|jpeg|png)$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Video poster does not match required pattern [video.poster.pattern]
                              File: %s:3:45-68
                              Actual: file:///local/poster.jpg
                              Expected: ^https?://.*\\.(jpg|jpeg|png)$

                               1 | = Test Document
                               2 |\s
                               3 | video::https://example.com/video.mp4[poster=file:///local/poster.jpg]
                                 |                                             ~~~~~~~~~~~~~~~~~~~~~~~~
                               4 |\s
                               5 | video::https://example.com/video.mp4[poster=https://example.com/poster.bmp]
                               6 |\s

                            [ERROR]: Video poster does not match required pattern [video.poster.pattern]
                              File: %s:5:45-74
                              Actual: https://example.com/poster.bmp
                              Expected: ^https?://.*\\.(jpg|jpeg|png)$

                               2 |\s
                               3 | video::https://example.com/video.mp4[poster=file:///local/poster.jpg]
                               4 |\s
                               5 | video::https://example.com/video.mp4[poster=https://example.com/poster.bmp]
                                 |                                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 |\s
                               7 | video::https://example.com/video.mp4[poster=https://example.com/poster.png]


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Video caption is too long [video.caption.maxLength]
                              File: %s:3:1-91
                              Actual: 90 characters
                              Expected: <= 25 characters

                               1 | = Test Document
                               2 |\s
                               3 | .This is an extremely long video caption that definitely exceeds the maximum allowed length
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | video::https://example.com/intro.mp4[]
                               5 |\s
                               6 | .Short caption

                            [ERROR]: Video caption is too long [video.caption.maxLength]
                              File: %s:9:1-75
                              Actual: 74 characters
                              Expected: <= 25 characters

                               6 | .Short caption
                               7 | video::https://example.com/demo.mp4[]
                               8 |\s
                               9 | .Another very long caption that should be much shorter to comply with rules
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                              10 | video::https://example.com/tutorial.mp4[]


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Video caption is too short [video.caption.minLength]
                              File: %s:3:1-5
                              Actual: 4 characters
                              Expected: >= 15 characters

                               1 | = Test Document
                               2 |\s
                               3 | .Demo
                                 | ~~~~~
                               4 | video::https://example.com/demo.mp4[]
                               5 |\s
                               6 | .Product demonstration video showing main features

                            [WARN]: Video caption is too short [video.caption.minLength]
                              File: %s:9:1-9
                              Actual: 8 characters
                              Expected: >= 15 characters

                               6 | .Product demonstration video showing main features
                               7 | video::https://example.com/product.mp4[]
                               8 |\s
                               9 | .Tutorial
                                 | ~~~~~~~~~
                              10 | video::https://example.com/tutorial.mp4[]


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: error
                                  required: true
                                  pattern: "^[A-Z][a-zA-Z0-9 ]+$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Table header does not match required pattern [table.header.pattern]
                              File: %s:4:3-18
                              Actual: lowercase header
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9 ]+$

                               1 | = Test Document
                               2 |\s
                               3 | |===
                               4 | | lowercase header | Another Column
                                 |   ~~~~~~~~~~~~~~~~
                               5 |\s
                               6 | | Data 1 | Data 2
                               7 | |===

                            [ERROR]: Table header does not match required pattern [table.header.pattern]
                              File: %s:16:3-21
                              Actual: Special-Characters!
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9 ]+$

                              13 | |===
                              14 |\s
                              15 | |===
                              16 | | Special-Characters! | Column#2
                                 |   ~~~~~~~~~~~~~~~~~~~
                              17 |\s
                              18 | | Data 1 | Data 2
                              19 | |===

                            [ERROR]: Table header does not match required pattern [table.header.pattern]
                              File: %s:16:25-32
                              Actual: Column#2
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9 ]+$

                              13 | |===
                              14 |\s
                              15 | |===
                              16 | | Special-Characters! | Column#2
                                 |                         ~~~~~~~~
                              17 |\s
                              18 | | Data 1 | Data 2
                              19 | |===


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: warn
                                  required: true
                                  minLength: 20
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Table caption is too short [table.caption.minLength]
                              File: %s:3:1-8
                              Actual: 7 characters
                              Expected: At least 20 characters

                               1 | = Test Document
                               2 |\s
                               3 | .Results
                                 | ~~~~~~~~
                               4 | |===
                               5 | | Name | Score
                               6 |\s

                            [WARN]: Table caption is too short [table.caption.minLength]
                              File: %s:19:1-5
                              Actual: 4 characters
                              Expected: At least 20 characters

                              16 | | David | 88
                              17 | |===
                              18 |\s
                              19 | .Data
                                 | ~~~~~
                              20 | |===
                              21 | | ID | Value
                              22 |\s


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: error
                                  required: true
                                  maxLength: 30
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Table caption is too long [table.caption.maxLength]
                              File: %s:3:1-91
                              Actual: 90 characters
                              Expected: At most 30 characters

                               1 | = Test Document
                               2 |\s
                               3 | .This is an extremely long table caption that definitely exceeds the maximum allowed length
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | |===
                               5 | | Column 1 | Column 2
                               6 |\s

                            [ERROR]: Table caption is too long [table.caption.maxLength]
                              File: %s:17:1-83
                              Actual: 82 characters
                              Expected: At most 30 characters

                              14 | | 1 | 2
                              15 | |===
                              16 |\s
                              17 | .Another excessively long caption that should be shortened to comply with the rules
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                              18 | |===
                              19 | | X | Y
                              20 |\s


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: error
                                  required: true
                                  pattern: "^Table \\\\d+\\\\.+"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Table caption does not match required pattern [table.caption.pattern]
                              File: %s:3:1-16
                              Actual: Results Summary
                              Expected: Pattern: ^Table \\d+\\.+

                               1 | = Test Document
                               2 |\s
                               3 | .Results Summary
                                 | ~~~~~~~~~~~~~~~~
                               4 | |===
                               5 | | Name | Score
                               6 |\s

                            [ERROR]: Table caption does not match required pattern [table.caption.pattern]
                              File: %s:10:1-30
                              Actual: Table 1. Valid caption format
                              Expected: Pattern: ^Table \\d+\\.+

                               7 | | Alice | 95
                               8 | |===
                               9 |\s
                              10 | .Table 1. Valid caption format
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                              11 | |===
                              12 | | ID | Value
                              13 |\s

                            [ERROR]: Table caption does not match required pattern [table.caption.pattern]
                              File: %s:17:1-15
                              Actual: Invalid format
                              Expected: Pattern: ^Table \\d+\\.+

                              14 | | 1 | 100
                              15 | |===
                              16 |\s
                              17 | .Invalid format
                                 | ~~~~~~~~~~~~~~~
                              18 | |===
                              19 | | X | Y
                              20 |\s


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Verse author is too long [verse.author.maxLength]
                              File: %s:3:10-54
                              Actual: 45 characters
                              Expected: At most 25 characters

                               1 | = Test Document
                               2 |\s
                               3 | [verse, "William Shakespeare and all his collaborators", "Hamlet"]
                                 |          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | ____
                               5 | To be, or not to be, that is the question
                               6 | ____


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Verse author is too short [verse.author.minLength]
                              File: %s:3:10-13
                              Actual: 4 characters
                              Expected: At least 10 characters

                               1 | = Test Document
                               2 |\s
                               3 | [verse, "Anon", "Unknown"]
                                 |          ~~~~
                               4 | ____
                               5 | Roses are red, violets are blue
                               6 | ____


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Verse author does not match required pattern [verse.author.pattern]
                              File: %s:3:10-20
                              Actual: shakespeare
                              Expected: Pattern: ^[A-Z][a-z]+ [A-Z][a-z]+$

                               1 | = Test Document
                               2 |\s
                               3 | [verse, "shakespeare", "Hamlet"]
                                 |          ~~~~~~~~~~~
                               4 | ____
                               5 | To be or not to be
                               6 | ____

                            [ERROR]: Verse author does not match required pattern [verse.author.pattern]
                              File: %s:13:10-22
                              Actual: E.E. Cummings
                              Expected: Pattern: ^[A-Z][a-z]+ [A-Z][a-z]+$

                              10 | I'm nobody! Who are you?
                              11 | ____
                              12 |\s
                              13 | [verse, "E.E. Cummings", "Complete Poems"]
                                 |          ~~~~~~~~~~~~~
                              14 | ____
                              15 | i carry your heart with me
                              16 | ____


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Verse attribution is too long [verse.attribution.maxLength]
                              File: %s:3:33-60
                              Actual: 28 characters
                              Expected: At most 20 characters

                               1 | = Test Document
                               2 |\s
                               3 | [verse, "William Shakespeare", "Hamlet Act 3 Scene 1 Line 56"]
                                 |                                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | ____
                               5 | To be, or not to be
                               6 | ____


                            """,
                            testFile.toString(), testFile.toString());

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
                                  pattern: "^[A-Z][a-zA-Z\\\\s]+, \\\\d{4}$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Verse attribution does not match required pattern [verse.attribution.pattern]
                              File: %s:3:26-37
                              Actual: Unknown date
                              Expected: Pattern: ^[A-Z][a-zA-Z\\s]+, \\d{4}$

                               1 | = Test Document
                               2 |\s
                               3 | [verse, "Robert Frost", "Unknown date"]
                                 |                          ~~~~~~~~~~~~
                               4 | ____
                               5 | Two roads diverged in a wood
                               6 | ____


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Verse content is too long [verse.content.maxLength]
                              File: %s:5:1-110
                              Actual: 110 characters
                              Expected: At most 50 characters

                               2 |\s
                               3 | [verse]
                               4 | ____
                               5 | This is an extremely long verse content that definitely exceeds the maximum allowed length of fifty characters
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 | ____
                               7 |\s
                               8 | [verse]


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Pass block type 'javascript' is not allowed [pass.type.allowed]
                              File: %s:3:12-21
                              Actual: javascript
                              Expected: One of: html, xml, svg

                               1 | = Test Document
                               2 |\s
                               3 | [pass,type=javascript]
                                 |            ~~~~~~~~~~
                               4 | ++++
                               5 | console.log("Hello");
                               6 | ++++

                            [ERROR]: Pass block type 'css' is not allowed [pass.type.allowed]
                              File: %s:13:12-14
                              Actual: css
                              Expected: One of: html, xml, svg

                              10 | <p>Valid HTML</p>
                              11 | ++++
                              12 |\s
                              13 | [pass,type=css]
                                 |            ~~~
                              14 | ++++
                              15 | body { color: red; }
                              16 | ++++


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Pass block content is too long [pass.content.maxLength]
                              File: %s:5:1-95
                              Actual: 95 characters
                              Expected: Maximum 50 characters

                               2 |\s
                               3 | [pass]
                               4 | ++++
                               5 | This is an extremely long pass block content that definitely exceeds the maximum allowed length
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 | ++++
                               7 |\s
                               8 | [pass]


                            """,
                            testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Pass block content does not match required pattern [pass.content.pattern]
                              File: %s:5:1-24
                              Actual: Content does not match pattern
                              Expected: Pattern: ^<[^>]+>.*</[^>]+>$

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
                              Actual: Content does not match pattern
                              Expected: Pattern: ^<[^>]+>.*</[^>]+>$

                              12 |\s
                              13 | [pass]
                              14 | ++++
                              15 | <p>Unclosed tag
                                 | ~~~~~~~~~~~~~~~
                              16 | ++++


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Pass block reason is too short [pass.reason.minLength]
                              File: %s:3:14-19
                              Actual: 6 characters
                              Expected: At least 20 characters

                               1 | = Test Document
                               2 |\s
                               3 | [pass,reason=Legacy]
                                 |              ~~~~~~
                               4 | ++++
                               5 | <custom>Content</custom>
                               6 | ++++

                            [WARN]: Pass block reason is too short [pass.reason.minLength]
                              File: %s:13:14-19
                              Actual: 6 characters
                              Expected: At least 20 characters

                              10 | <legacy>Data</legacy>
                              11 | ++++
                              12 |\s
                              13 | [pass,reason=Custom]
                                 |              ~~~~~~
                              14 | ++++
                              15 | <special>Tag</special>
                              16 | ++++


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Pass block reason is too long [pass.reason.maxLength]
                              File: %s:3:14-85
                              Actual: 72 characters
                              Expected: At most 30 characters

                               1 | = Test Document
                               2 |\s
                               3 | [pass,reason=This is an extremely long reason that exceeds the maximum allowed length]
                                 |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | ++++
                               5 | <content>Data</content>
                               6 | ++++

                            [ERROR]: Pass block reason is too long [pass.reason.maxLength]
                              File: %s:13:14-94
                              Actual: 81 characters
                              Expected: At most 30 characters

                              10 | <valid>Content</valid>
                              11 | ++++
                              12 |\s
                              13 | [pass,reason=Another excessively long explanation for using passthrough that should be shorter]
                                 |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                              14 | ++++
                              15 | <data>Value</data>
                              16 | ++++


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }
    }

    @Nested
    @DisplayName("Paragraph Validation Tests")
    class ParagraphValidationTests {

        @Test
        @DisplayName("should show underline for paragraph with too many sentences")
        void shouldShowUnderlineForParagraphWithTooManySentences(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with maximum sentence count for paragraphs
            String rules = """
                    document:
                      sections:
                        - level: 0
                          allowedBlocks:
                            - paragraph:
                                severity: warn
                                sentence:
                                  occurrence:
                                    max: 2
                                    severity: warn
                    """;

            // Given - AsciiDoc content with paragraphs having too many sentences
            String adocContent = """
                    = Test Document

                    This paragraph has only one sentence.

                    This has two sentences. Here is the second.

                    This paragraph contains three sentences. Here is the second one. And this is the third sentence.

                    Another paragraph with four sentences here. This is the second sentence. Here is the third one. And finally the fourth.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Paragraph has too many sentences [paragraph.sentence.occurrence.max]
                              File: %s:7:1-96
                              Actual: 3
                              Expected: At most 2 sentences

                               4 |\s
                               5 | This has two sentences. Here is the second.
                               6 |\s
                               7 | This paragraph contains three sentences. Here is the second one. And this is the third sentence.
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               8 |\s
                               9 | Another paragraph with four sentences here. This is the second sentence. Here is the third one. And finally the fourth.

                            [WARN]: Paragraph has too many sentences [paragraph.sentence.occurrence.max]
                              File: %s:9:1-119
                              Actual: 4
                              Expected: At most 2 sentences

                               6 |\s
                               7 | This paragraph contains three sentences. Here is the second one. And this is the third sentence.
                               8 |\s
                               9 | Another paragraph with four sentences here. This is the second sentence. Here is the third one. And finally the fourth.
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for sentence with too many words")
        void shouldShowUnderlineForSentenceWithTooManyWords(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with maximum words per sentence
            String rules = """
                    document:
                      sections:
                        - level: 0
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                sentence:
                                  words:
                                    max: 10
                                    severity: warn
                    """;

            // Given - AsciiDoc content with sentences having too many words
            String adocContent = """
                    = Test Document

                    This sentence is fine with exactly ten words in it.

                    This sentence has way too many words and definitely exceeds the maximum limit that we have configured for validation purposes.

                    Short sentence here. But this one also has too many words for the configured maximum limit.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Sentence 1 has too many words [paragraph.sentence.words.max]
                              File: %s:5:1-126
                              Actual: 20 words
                              Expected: At most 10 words

                               2 |\s
                               3 | This sentence is fine with exactly ten words in it.
                               4 |\s
                               5 | This sentence has way too many words and definitely exceeds the maximum limit that we have configured for validation purposes.
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               6 |\s
                               7 | Short sentence here. But this one also has too many words for the configured maximum limit.

                            [WARN]: Sentence 2 has too many words [paragraph.sentence.words.max]
                              File: %s:7:22-91
                              Actual: 13 words
                              Expected: At most 10 words

                               4 |\s
                               5 | This sentence has way too many words and definitely exceeds the maximum limit that we have configured for validation purposes.
                               6 |\s
                               7 | Short sentence here. But this one also has too many words for the configured maximum limit.
                                 |                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^[A-Z][a-zA-Z\\\\s\\\\.]+$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Quote attribution does not match required pattern [quote.attribution.pattern]
                              File: %s:3:10-23
                              Actual: shakespeare123
                              Expected: Pattern: ^[A-Z][a-zA-Z\\s\\.]+$

                               1 | = Test Document
                               2 |\s
                               3 | [quote, "shakespeare123", "Hamlet"]
                                 |          ~~~~~~~~~~~~~~
                               4 | ____
                               5 | To be, or not to be, that is the question.
                               6 | ____

                            [ERROR]: Quote attribution does not match required pattern [quote.attribution.pattern]
                              File: %s:13:10-18
                              Actual: @john_doe
                              Expected: Pattern: ^[A-Z][a-zA-Z\\s\\.]+$

                              10 | All the world's a stage.
                              11 | ____
                              12 |\s
                              13 | [quote, "@john_doe", "Twitter"]
                                 |          ~~~~~~~~~
                              14 | ____
                              15 | Hello world!
                              16 | ____


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^[A-Z][a-zA-Z0-9\\\\s]+, \\\\d{4}$"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Quote citation does not match required pattern [quote.citation.pattern]
                              File: %s:3:29-40
                              Actual: unknown date
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9\\s]+, \\d{4}$

                               1 | = Test Document
                               2 |\s
                               3 | [quote, "Albert Einstein", "unknown date"]
                                 |                             ~~~~~~~~~~~~
                               4 | ____
                               5 | Imagination is more important than knowledge.
                               6 | ____

                            [ERROR]: Quote citation does not match required pattern [quote.citation.pattern]
                              File: %s:13:25-50
                              Actual: The Picture of Dorian Gray
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9\\s]+, \\d{4}$

                              10 | There is no greater agony than bearing an untold story inside you.
                              11 | ____
                              12 |\s
                              13 | [quote, "Oscar Wilde", "The Picture of Dorian Gray"]
                                 |                         ~~~~~~~~~~~~~~~~~~~~~~~~~~
                              14 | ____
                              15 | We are all in the gutter, but some of us are looking at the stars.
                              16 | ____


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }
    }

    @Nested
    @DisplayName("Ulist Block Validation Tests")
    class UlistValidationTests {

    }

    @Nested
    @DisplayName("Section Validation Tests")
    class SectionValidationTests {

        @Test
        @DisplayName("should show underline for section title not matching pattern")
        void shouldShowUnderlineForSectionTitleNotMatchingPattern(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with title pattern for sections
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: "^(Introduction|Overview|Summary)$"
                            severity: error
                          subsections:
                            - level: 2
                              title:
                                pattern: "^[A-Z][a-z]+ [A-Z][a-z]+$"
                                severity: error
                    """;

            // Given - AsciiDoc content with section titles not matching patterns
            String adocContent = """
                    = Test Document

                    == Getting Started

                    This section title doesn't match the required pattern.

                    === technical details

                    This subsection title doesn't start with uppercase.

                    == Introduction

                    This title matches the pattern.

                    === Another Topic

                    This subsection also matches the pattern.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Section title does not match required pattern [section.title.pattern]
                              File: %s:3:4-18
                              Actual: Getting Started
                              Expected: Pattern: ^(Introduction|Overview|Summary)$

                               1 | = Test Document
                               2 |\s
                               3 | == Getting Started
                                 |    ~~~~~~~~~~~~~~~
                               4 |\s
                               5 | This section title doesn't match the required pattern.
                               6 |\s

                            [ERROR]: Section title does not match required pattern [section.title.pattern]
                              File: %s:7:5-21
                              Actual: technical details
                              Expected: Pattern: ^[A-Z][a-z]+ [A-Z][a-z]+$

                               4 |\s
                               5 | This section title doesn't match the required pattern.
                               6 |\s
                               7 | === technical details
                                 |     ~~~~~~~~~~~~~~~~~
                               8 |\s
                               9 | This subsection title doesn't start with uppercase.
                              10 |\s


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for document title not matching pattern")
        void shouldShowUnderlineForDocumentTitleNotMatchingPattern(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for document title (level 0)
            String rules = """
                    document:
                      sections:
                        - level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z][A-Z0-9\\\\s]+$"
                            severity: error
                    """;

            // Given - AsciiDoc content with document title not matching pattern
            String adocContent = """
                    = My Test Document!

                    == Section One

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Document title does not match required pattern [section.title.pattern]
                              File: %s:1:3-19
                              Actual: My Test Document!
                              Expected: Pattern: ^[A-Z][A-Z0-9\\s]+$

                               1 | = My Test Document!
                                 |   ~~~~~~~~~~~~~~~~~
                               2 |\s
                               3 | == Section One
                               4 |\s


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for section with subsection pattern mismatch")
        void shouldShowUnderlineForSectionWithSubsectionPatternMismatch(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern for subsection title
            String rules = """
                    document:
                      sections:
                        - level: 0
                          subsections:
                            - name: headerTypeRule
                              order: 1
                              level: 1
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^Typ$"
                                severity: error
                    """;

            // Given - AsciiDoc content with subsection title not matching pattern
            String adocContent = """
                    = Test Document

                    == Wrong Title

                    This section title doesn't match the required pattern "Typ".
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Section title does not match required pattern [section.title.pattern]
                              File: %s:3:4-14
                              Actual: Wrong Title
                              Expected: Pattern: ^Typ$

                               1 | = Test Document
                               2 |\s
                               3 | == Wrong Title
                                 |    ~~~~~~~~~~~
                               4 |\s
                               5 | This section title doesn't match the required pattern "Typ".


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for multiple section pattern mismatches")
        void shouldShowUnderlineForMultipleSectionPatternMismatches(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with different patterns for different section levels
            String rules = """
                    document:
                      sections:
                        - level: 0
                          title:
                            pattern: "^[A-Z][a-zA-Z\\\\s]+Guide$"
                            severity: error
                          subsections:
                            - level: 1
                              title:
                                pattern: "^Chapter \\\\d+: .+$"
                                severity: error
                              subsections:
                                - level: 2
                                  title:
                                    pattern: "^\\\\d+\\\\.\\\\d+ .+$"
                                    severity: warn
                    """;

            // Given - AsciiDoc content with multiple pattern violations
            String adocContent = """
                    = Technical Documentation

                    == Introduction

                    This doesn't match the Chapter pattern.

                    === Overview

                    This doesn't match the numbered pattern.

                    == Chapter 1: Getting Started

                    This section matches the pattern.

                    === 1.1 Installation

                    This subsection matches the pattern.

                    === Setup Instructions

                    This doesn't match the numbered pattern.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Document title does not match required pattern [section.title.pattern]
                              File: %s:1:3-25
                              Actual: Technical Documentation
                              Expected: Pattern: ^[A-Z][a-zA-Z\\s]+Guide$

                               1 | = Technical Documentation
                                 |   ~~~~~~~~~~~~~~~~~~~~~~~
                               2 |\s
                               3 | == Introduction
                               4 |\s

                            [ERROR]: Section title does not match required pattern [section.title.pattern]
                              File: %s:3:4-15
                              Actual: Introduction
                              Expected: Pattern: ^Chapter \\d+: .+$

                               1 | = Technical Documentation
                               2 |\s
                               3 | == Introduction
                                 |    ~~~~~~~~~~~~
                               4 |\s
                               5 | This doesn't match the Chapter pattern.
                               6 |\s

                            [WARN]: Section title does not match required pattern [section.title.pattern]
                              File: %s:7:5-12
                              Actual: Overview
                              Expected: Pattern: ^\\d+\\.\\d+ .+$

                               4 |\s
                               5 | This doesn't match the Chapter pattern.
                               6 |\s
                               7 | === Overview
                                 |     ~~~~~~~~
                               8 |\s
                               9 | This doesn't match the numbered pattern.
                              10 |\s

                            [WARN]: Section title does not match required pattern [section.title.pattern]
                              File: %s:19:5-22
                              Actual: Setup Instructions
                              Expected: Pattern: ^\\d+\\.\\d+ .+$

                              16 |\s
                              17 | This subsection matches the pattern.
                              18 |\s
                              19 | === Setup Instructions
                                 |     ~~~~~~~~~~~~~~~~~~
                              20 |\s
                              21 | This doesn't match the numbered pattern.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString(), testFile.toString(),
                            testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Admonition type 'IMPORTANT' is not allowed [admonition.type.allowed]
                              File: %s:7:1-9
                              Actual: IMPORTANT
                              Expected: One of: NOTE, TIP, WARNING

                               4 |\s
                               5 | TIP: This is a valid tip.
                               6 |\s
                               7 | IMPORTANT: This type is not allowed.
                                 | ~~~~~~~~~
                               8 |\s
                               9 | WARNING: This is allowed.
                              10 |\s

                            [ERROR]: Admonition type 'CAUTION' is not allowed [admonition.type.allowed]
                              File: %s:11:1-7
                              Actual: CAUTION
                              Expected: One of: NOTE, TIP, WARNING

                               8 |\s
                               9 | WARNING: This is allowed.
                              10 |\s
                              11 | CAUTION: This type is also not allowed.
                                 | ~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Admonition title is too short [admonition.title.minLength]
                              File: %s:3:1-5
                              Actual: 4 characters
                              Expected: At least 10 characters

                               1 | = Test Document
                               2 |\s
                               3 | .Note
                                 | ~~~~~
                               4 | NOTE: This title is too short.
                               5 |\s
                               6 | .Important Information

                            [WARN]: Admonition title is too short [admonition.title.minLength]
                              File: %s:9:1-4
                              Actual: 3 characters
                              Expected: At least 10 characters

                               6 | .Important Information
                               7 | NOTE: This title has proper length.
                               8 |\s
                               9 | .Tip
                                 | ~~~~
                              10 | TIP: Another short title.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Admonition title is too long [admonition.title.maxLength]
                              File: %s:3:1-83
                              Actual: 82 characters
                              Expected: At most 25 characters

                               1 | = Test Document
                               2 |\s
                               3 | .This is an extremely long admonition title that exceeds the maximum allowed length
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 | NOTE: Content here.
                               5 |\s
                               6 | .Short Title

                            [ERROR]: Admonition title is too long [admonition.title.maxLength]
                              File: %s:9:1-52
                              Actual: 51 characters
                              Expected: At most 25 characters

                               6 | .Short Title
                               7 | WARNING: Content here.
                               8 |\s
                               9 | .Another very long title that should be much shorter
                                 | ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                              10 | TIP: Content here.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Admonition title does not match required pattern [admonition.title.pattern]
                              File: %s:3:1-16
                              Actual: lowercase title
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9 ]+$

                               1 | = Test Document
                               2 |\s
                               3 | .lowercase title
                                 | ~~~~~~~~~~~~~~~~
                               4 | NOTE: Invalid title starting with lowercase.
                               5 |\s
                               6 | .Valid Title Format

                            [ERROR]: Admonition title does not match required pattern [admonition.title.pattern]
                              File: %s:9:1-20
                              Actual: Title-with-hyphens!
                              Expected: Pattern: ^[A-Z][a-zA-Z0-9 ]+$

                               6 | .Valid Title Format
                               7 | WARNING: This title matches the pattern.
                               8 |\s
                               9 | .Title-with-hyphens!
                                 | ~~~~~~~~~~~~~~~~~~~~
                              10 | TIP: Invalid title with special characters.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [WARN]: Admonition content is too short [admonition.content.minLength]
                              File: %s:3:7-16
                              Actual: 10 characters
                              Expected: At least 20 characters

                               1 | = Test Document
                               2 |\s
                               3 | NOTE: Too short.
                                 |       ~~~~~~~~~~
                               4 |\s
                               5 | WARNING: This content has sufficient length to pass validation.
                               6 |\s

                            [WARN]: Admonition content is too short [admonition.content.minLength]
                              File: %s:7:6-15
                              Actual: 10 characters
                              Expected: At least 20 characters

                               4 |\s
                               5 | WARNING: This content has sufficient length to pass validation.
                               6 |\s
                               7 | TIP: Brief tip.
                                 |      ~~~~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Admonition content is too long [admonition.content.maxLength]
                              File: %s:3:7-102
                              Actual: 96 characters
                              Expected: At most 50 characters

                               1 | = Test Document
                               2 |\s
                               3 | NOTE: This is an extremely long admonition content that definitely exceeds the maximum allowed length.
                                 |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               4 |\s
                               5 | WARNING: Short and concise warning message.
                               6 |\s

                            [ERROR]: Admonition content is too long [admonition.content.maxLength]
                              File: %s:7:6-107
                              Actual: 102 characters
                              Expected: At most 50 characters

                               4 |\s
                               5 | WARNING: Short and concise warning message.
                               6 |\s
                               7 | TIP: Another excessively long tip content that should be shortened to comply with the maximum length rules.
                                 |      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  pattern: "^icon:[a-z-]+\\\\[\\\\]$"
                    """;

            // Given - AsciiDoc content with admonition icons not matching pattern
            String adocContent = """
                    = Test Document

                    [NOTE,icon=warning]
                    ====
                    Invalid icon format.
                    ====

                    [TIP,icon="icon:lightbulb[]"]
                    ====
                    Valid icon format.
                    ====

                    [WARNING,icon="icon:ALERT[]"]
                    ====
                    Invalid uppercase icon.
                    ====
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Admonition icon does not match required pattern [admonition.icon.pattern]
                              File: %s:3:12-18
                              Actual: warning
                              Expected: Pattern: ^icon:[a-z-]+\\[\\]$

                               1 | = Test Document
                               2 |\s
                               3 | [NOTE,icon=warning]
                                 |            ~~~~~~~
                               4 | ====
                               5 | Invalid icon format.
                               6 | ====

                            [ERROR]: Admonition icon does not match required pattern [admonition.icon.pattern]
                              File: %s:13:16-27
                              Actual: icon:ALERT[]
                              Expected: Pattern: ^icon:[a-z-]+\\[\\]$

                              10 | Valid icon format.
                              11 | ====
                              12 |\s
                              13 | [WARNING,icon="icon:ALERT[]"]
                                 |                ~~~~~~~~~~~~
                              14 | ====
                              15 | Invalid uppercase icon.
                              16 | ====


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: error
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Listing language 'ruby' is not allowed [listing.language.allowed]
                              File: %s:12:9-12
                              Actual: ruby
                              Expected: One of: java, python, javascript, xml

                               9 | }
                              10 | ----
                              11 |\s
                              12 | [source,ruby]
                                 |         ~~~~
                              13 | ----
                              14 | puts "Hello World"
                              15 | ----

                            [ERROR]: Listing language 'golang' is not allowed [listing.language.allowed]
                              File: %s:22:9-14
                              Actual: golang
                              Expected: One of: java, python, javascript, xml

                              19 | print("Hello World")
                              20 | ----
                              21 |\s
                              22 | [source,golang]
                                 |         ~~~~~~
                              23 | ----
                              24 | fmt.Println("Hello World")
                              25 | ----


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

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
                                  severity: error
                                  required: true
                                  pattern: "^(Listing|Example|Code) \\\\d+\\\\..+"
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
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Listing title does not match required pattern [listing.title.pattern]
                              File: %s:3:1-21
                              Actual: Invalid title format
                              Expected: Pattern: ^(Listing|Example|Code) \\d+\\..+

                               1 | = Test Document
                               2 |\s
                               3 | .Invalid title format
                                 | ~~~~~~~~~~~~~~~~~~~~~
                               4 | [source,java]
                               5 | ----
                               6 | public class Test {}

                            [ERROR]: Listing title does not match required pattern [listing.title.pattern]
                              File: %s:15:1-15
                              Actual: My Code Sample
                              Expected: Pattern: ^(Listing|Example|Code) \\d+\\..+

                              12 | public class Valid {}
                              13 | ----
                              14 |\s
                              15 | .My Code Sample
                                 | ~~~~~~~~~~~~~~~
                              16 | [source,python]
                              17 | ----
                              18 | def hello():


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }
    }

    @Nested
    @DisplayName("Metadata Attribute Validation Tests")
    class MetadataValidationTests {

        @Test
        @DisplayName("should show underline for attribute value violating pattern rule")
        void shouldShowUnderlineForAttributePatternViolation(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern rule for metadata attributes
            String rules = """
                    document:
                      metadata:
                        attributes:
                          - name: author
                            pattern: "^[A-Z][a-z]+ [A-Z][a-z]+$"
                            severity: error
                    """;

            // Given - AsciiDoc content with invalid author format
            String adocContent = """
                    = Test Document
                    :author: john doe
                    :email: john@example.com

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Attribute 'author' does not match required pattern: actual 'john doe', expected pattern '^[A-Z][a-z]+ [A-Z][a-z]+$' [metadata.pattern]
                              File: %s:2:10-17
                              Actual: john doe
                              Expected: Pattern '^[A-Z][a-z]+ [A-Z][a-z]+$'

                               1 | = Test Document
                               2 | :author: john doe
                                 |          ~~~~~~~~
                               3 | :email: john@example.com
                               4 |\s
                               5 | Content here.


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for attribute value violating minLength rule")
        void shouldShowUnderlineForAttributeMinLengthViolation(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with minLength rule for metadata attributes
            String rules = """
                    document:
                      metadata:
                        attributes:
                          - name: description
                            minLength: 20
                            severity: error
                    """;

            // Given - AsciiDoc content with too short description
            String adocContent = """
                    = Test Document
                    :author: John Doe
                    :description: Short

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Attribute 'description' is too short: actual 'Short' (5 characters), expected minimum 20 characters [metadata.length.min]
                              File: %s:3:15-19
                              Actual: Short (5 characters)
                              Expected: Minimum 20 characters

                               1 | = Test Document
                               2 | :author: John Doe
                               3 | :description: Short
                                 |               ~~~~~
                               4 |\s
                               5 | Content here.


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for attribute value violating maxLength rule")
        void shouldShowUnderlineForAttributeMaxLengthViolation(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with maxLength rule for metadata attributes
            String rules = """
                    document:
                      metadata:
                        attributes:
                          - name: title
                            maxLength: 10
                            severity: error
                    """;

            // Given - AsciiDoc content with too long title
            String adocContent = """
                    = Test Document
                    :title: This is a very long title that exceeds the limit
                    :author: John Doe

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Attribute 'title' is too long: actual 'This is a very long title that exceeds the limit' (48 characters), expected maximum 10 characters [metadata.length.max]
                              File: %s:2:9-56
                              Actual: This is a very long title that exceeds the limit (48 characters)
                              Expected: Maximum 10 characters

                               1 | = Test Document
                               2 | :title: This is a very long title that exceeds the limit
                                 |         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                               3 | :author: John Doe
                               4 |\s
                               5 | Content here.


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should correctly position underline for attribute value with spaces")
        void shouldCorrectlyPositionUnderlineWithSpaces(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with pattern rule for metadata attributes
            String rules = """
                    document:
                      metadata:
                        attributes:
                          - name: version
                            pattern: "^\\\\d+\\\\.\\\\d+\\\\.\\\\d+$"
                            severity: error
                    """;

            // Given - AsciiDoc content with invalid version (has spaces before value)
            String adocContent = """
                    = Test Document
                    :version:   1.0
                    :author: John Doe

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underline at correct position
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Attribute 'version' does not match required pattern: actual '1.0', expected pattern '^\\d+\\.\\d+\\.\\d+$' [metadata.pattern]
                              File: %s:2:13-15
                              Actual: 1.0
                              Expected: Pattern '^\\d+\\.\\d+\\.\\d+$'

                               1 | = Test Document
                               2 | :version:   1.0
                                 |             ~~~
                               3 | :author: John Doe
                               4 |\s
                               5 | Content here.


                            """,
                            testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }

        @Test
        @DisplayName("should show underline for multiple attribute violations")
        void shouldShowUnderlineForMultipleAttributeViolations(@TempDir Path tempDir) throws IOException {
            // Given - YAML rules with multiple constraints for metadata attributes
            String rules = """
                    document:
                      metadata:
                        attributes:
                          - name: author
                            pattern: "^[A-Z][a-z]+ [A-Z][a-z]+$"
                            minLength: 10
                            severity: error
                          - name: version
                            pattern: "^\\\\d+\\\\.\\\\d+\\\\.\\\\d+$"
                            severity: warn
                    """;

            // Given - AsciiDoc content with multiple violations
            String adocContent = """
                    = Test Document
                    :author: joe
                    :version: 1.0-SNAPSHOT
                    :description: Valid description

                    Content here.
                    """;

            // When - Validate and format output
            String actualOutput = validateAndFormat(rules, adocContent, tempDir);

            // Then - Verify exact console output with underlines for both violations
            Path testFile = tempDir.resolve("test.adoc");
            String expectedOutput = String
                    .format("""
                            +----------------------------------------------------------------------------------------------------------------------+
                            |                                                  Validation Report                                                   |
                            +----------------------------------------------------------------------------------------------------------------------+

                            %s:

                            [ERROR]: Attribute 'author' does not match required pattern: actual 'joe', expected pattern '^[A-Z][a-z]+ [A-Z][a-z]+$' [metadata.pattern]
                              File: %s:2:10-12
                              Actual: joe
                              Expected: Pattern '^[A-Z][a-z]+ [A-Z][a-z]+$'

                               1 | = Test Document
                               2 | :author: joe
                                 |          ~~~
                               3 | :version: 1.0-SNAPSHOT
                               4 | :description: Valid description
                               5 |\s

                            [ERROR]: Attribute 'author' is too short: actual 'joe' (3 characters), expected minimum 10 characters [metadata.length.min]
                              File: %s:2:10-12
                              Actual: joe (3 characters)
                              Expected: Minimum 10 characters

                               1 | = Test Document
                               2 | :author: joe
                                 |          ~~~
                               3 | :version: 1.0-SNAPSHOT
                               4 | :description: Valid description
                               5 |\s

                            [WARN]: Attribute 'version' does not match required pattern: actual '1.0-SNAPSHOT', expected pattern '^\\d+\\.\\d+\\.\\d+$' [metadata.pattern]
                              File: %s:3:11-22
                              Actual: 1.0-SNAPSHOT
                              Expected: Pattern '^\\d+\\.\\d+\\.\\d+$'

                               1 | = Test Document
                               2 | :author: joe
                               3 | :version: 1.0-SNAPSHOT
                                 |           ~~~~~~~~~~~~
                               4 | :description: Valid description
                               5 |\s
                               6 | Content here.


                            """,
                            testFile.toString(), testFile.toString(), testFile.toString(), testFile.toString());

            assertEquals(expectedOutput, actualOutput);
        }
    }

}
