package com.dataliquid.linter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.Linter;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

@DisplayName("Linter Validation Integration Test")
class LinterValidationIntegrationTest {

    private Linter linter;
    private ConfigurationLoader configLoader;

    @BeforeEach
    void setUp() {
        linter = new Linter();
        configLoader = new ConfigurationLoader(); // Schema validation enabled
    }

    @AfterEach
    void tearDown() {
        linter.close();
    }

    @Nested
    @DisplayName("Metadata Attribute Validation")
    class MetadataAttributeValidationTest {

        @Nested
        @DisplayName("Required Attribute Tests")
        class RequiredAttributeTests {

            @Test
            @DisplayName("should detect missing required author")
            void shouldDetectMissingRequiredAuthor() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                severity: error
                        """;

                String adocContent = """
                        = Document Without Author

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(1, result.getErrorCount());
                assertEquals("Missing required attribute 'author'", result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should detect multiple missing required attributes")
            void shouldDetectMultipleMissingRequiredAttributes() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                severity: error
                              - name: revdate
                                required: true
                                severity: error
                        """;

                String adocContent = """
                        = Only Title Present

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(2, result.getErrorCount());
                List<ValidationMessage> messages = result.getMessages();
                assertTrue(messages
                        .stream()
                        .anyMatch(msg -> msg.getMessage().equals("Missing required attribute 'author'")));
                assertTrue(messages
                        .stream()
                        .anyMatch(msg -> msg.getMessage().equals("Missing required attribute 'revdate'")));
            }

            @Test
            @DisplayName("should accept all required attributes present")
            void shouldAcceptAllRequiredAttributesPresent() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                severity: error
                        """;

                String adocContent = """
                        = Complete Document
                        John Doe <john@example.com>

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }
        }

        @Nested
        @DisplayName("Pattern Validation Tests")
        class PatternValidationTests {

            @Test
            @DisplayName("should validate email pattern")
            void shouldValidateEmailPattern() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: email
                                required: true
                                pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :email: invalid-email

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(
                        "Attribute 'email' does not match required pattern: actual 'invalid-email', expected pattern '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should validate date pattern")
            void shouldValidateDatePattern() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: revdate
                                required: true
                                pattern: "^\\\\d{4}-\\\\d{2}-\\\\d{2}$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :revdate: 15.01.2024

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(
                        "Attribute 'revdate' does not match required pattern: actual '15.01.2024', expected pattern '^\\d{4}-\\d{2}-\\d{2}$'",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should accept valid patterns")
            void shouldAcceptValidPatterns() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: version
                                required: true
                                pattern: "^\\\\d+\\\\.\\\\d+(\\\\.\\\\d+)?$"
                                severity: error
                              - name: author
                                required: true
                                pattern: "^[A-Z][a-zA-Z\\\\s]+$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        John Doe
                        :version: 1.0.0

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }
        }

        @Nested
        @DisplayName("Length Validation Tests")
        class LengthValidationTests {

            @Test
            @DisplayName("should validate minimum length")
            void shouldValidateMinimumLength() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                minLength: 5
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :author: Bob

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(
                        "Attribute 'author' is too short: actual 'Bob' (3 characters), expected minimum 5 characters",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should validate maximum length")
            void shouldValidateMaximumLength() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: keywords
                                required: true
                                maxLength: 20
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :keywords: This is a very long keywords list that exceeds the maximum allowed

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(
                        "Attribute 'keywords' is too long: actual 'This is a very long keywords list that exceeds the maximum allowed' (66 characters), expected maximum 20 characters",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should accept length within bounds")
            void shouldAcceptLengthWithinBounds() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: description
                                required: true
                                minLength: 5
                                maxLength: 50
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :description: Valid description length

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }
        }

        @Nested
        @DisplayName("Severity Level Tests")
        class SeverityLevelTests {

            @Test
            @DisplayName("should report error severity for missing required")
            void shouldReportErrorSeverity() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                severity: error
                        """;

                String adocContent = """
                        = Document

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(1, result.getErrorCount());
                assertEquals(0, result.getWarningCount());
            }

            @Test
            @DisplayName("should report warning severity")
            void shouldReportWarningSeverity() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: email
                                pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"
                                required: true
                                severity: warn
                        """;

                String adocContent = """
                        = Document
                        :email: invalid-email

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
                assertTrue(result.hasWarnings());
                assertEquals(1, result.getWarningCount());
            }

            @Test
            @DisplayName("should report info severity")
            void shouldReportInfoSeverity() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: keywords
                                minLength: 10
                                required: true
                                severity: info
                        """;

                String adocContent = """
                        = Document
                        :keywords: short

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
                assertFalse(result.hasWarnings());
                assertEquals(1, result.getInfoCount());
            }
        }

        @Nested
        @DisplayName("Edge Cases and Special Tests")
        class EdgeCasesTests {

            @Test
            @DisplayName("should validate empty attribute with minLength")
            void shouldValidateEmptyAttributeWithMinLength() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                minLength: 1
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :author:

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals("Attribute 'author' is too short: actual '' (0 characters), expected minimum 1 characters",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should validate multiple constraints on single attribute")
            void shouldValidateMultipleConstraints() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: description
                                required: true
                                pattern: "^[A-Z].*"
                                minLength: 10
                                maxLength: 50
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :description: short

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(2, result.getErrorCount()); // Pattern and minLength violations
                List<ValidationMessage> messages = result.getMessages();
                assertTrue(messages
                        .stream()
                        .anyMatch(msg -> msg
                                .getMessage()
                                .equals("Attribute 'description' does not match required pattern: actual 'short', expected pattern '^[A-Z].*'")));
                assertTrue(messages
                        .stream()
                        .anyMatch(msg -> msg
                                .getMessage()
                                .equals("Attribute 'description' is too short: actual 'short' (5 characters), expected minimum 10 characters")));
            }

            @Test
            @DisplayName("should allow empty toc attribute")
            void shouldAllowEmptyTocAttribute() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: toc
                                required: true
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :toc:

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should validate toc attribute with allowed values")
            void shouldValidateTocAttributeWithAllowedValues() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: toc
                                required: true
                                pattern: "^(left|right|preamble|macro)?$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :toc: left

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should allow empty toc attribute with pattern")
            void shouldAllowEmptyTocAttributeWithPattern() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: toc
                                required: true
                                pattern: "^(left|right|preamble|macro)?$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :toc:

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should distinguish between missing and empty attributes")
            void shouldDistinguishBetweenMissingAndEmptyAttributes() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: toc
                                required: true
                                severity: error
                              - name: author
                                required: true
                                minLength: 1
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :toc:

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals(1, result.getErrorCount());
                // toc is present (empty is OK), but author is missing
                assertEquals("Missing required attribute 'author'", result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should ignore undefined attributes")
            void shouldIgnoreUndefinedAttributes() {
                // Given - configuration defines only title and author
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                severity: error
                        """;

                // Document has additional attributes that are not defined in rules
                String adocContent = """
                        = Document Title
                        John Doe
                        :keywords: java, testing, asciidoc
                        :custom-field: some value
                        :version: 1.0.0
                        :toc:

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then - no errors because undefined attributes are ignored
                assertFalse(result.hasErrors());
                assertFalse(result.hasWarnings());
            }

            @Test
            @DisplayName("should handle special characters in attribute values")
            void shouldHandleSpecialCharactersInAttributeValues() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                pattern: "^[A-Za-zÄÖÜäöüß\\\\s]+$"
                                severity: error
                        """;

                String adocContent = """
                        = Document
                        :author: Björn Müller

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }
        }

        @Nested
        @DisplayName("Complex Integration Scenarios")
        class ComplexIntegrationTests {

            @Test
            @DisplayName("should validate complex metadata configuration")
            void shouldValidateComplexMetadataConfiguration() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: author
                                required: true
                                pattern: "^[A-Z][a-zA-Z\\\\s\\\\.]+$"
                                minLength: 5
                                maxLength: 50
                                severity: error
                              - name: revdate
                                required: true
                                pattern: "^\\\\d{4}-\\\\d{2}-\\\\d{2}$"
                                severity: error
                              - name: version
                                required: true
                                pattern: "^\\\\d+\\\\.\\\\d+(\\\\.\\\\d+)?$"
                                severity: error
                              - name: email
                                required: false
                                pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"
                                severity: warn
                        """;

                String adocContent = """
                        = Complete Technical Documentation
                        John Doe Sr.
                        :revdate: 2024-01-15
                        :version: 2.1.0
                        :email: john.doe@example.com

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
                assertFalse(result.hasWarnings());
            }

            @Test
            @DisplayName("should validate document with non-required attributes")
            void shouldValidateDocumentWithNonRequiredAttributes() {
                // Given
                String rules = """
                        document:
                          metadata:
                            attributes:
                              - name: description
                                required: false
                                maxLength: 5
                                severity: warn
                              - name: keywords
                                required: false
                                pattern: "^[a-zA-Z,\\\\s]+$"
                                severity: info
                        """;

                String adocContent = """
                        = Document Title
                        :description: This is a very long description
                        :keywords: java, testing, 123numbers

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
                assertTrue(result.hasWarnings());
                assertEquals(1, result.getWarningCount()); // Description too long
                assertEquals(1, result.getInfoCount()); // Keywords pattern violation
            }
        }
    }

    @Nested
    @DisplayName("Content Validation")
    class ContentValidation {

        @Nested
        @DisplayName("Document Title Validation")
        class DocumentTitleValidation {

            @Test
            @DisplayName("should not validate title pattern - must start with uppercase")
            void shouldNotValidateTitlePattern() {
                // Given
                String rules = """
                        document:
                          sections:
                            - level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                        """;

                String adocContent = """
                        = lowercase title

                        Content here.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertEquals("Document title does not match required pattern",
                        result.getMessages().get(0).getMessage());
            }

            @Test
            @DisplayName("should validate simple section with paragraph blocks")
            void shouldValidateSimpleSectionWithParagraphBlocks() {
                // Given - Simple test with just one section
                String rules = """
                        document:
                          sections:
                            - name: intro
                              level: 1
                              occurrence:
                                min: 1
                                max: 1
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                        """;

                String adocContent = """
                        = My Document

                        == Introduction

                        This is the first paragraph.

                        This is the second paragraph.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should validate complex document structure as requested")
            void shouldValidateComplexDocumentStructureAsRequested() {
                // Given - Structure exactly as requested:
                // level 0 with paragraphs min 1 max 2
                // subsection level 1 "Introduction" with paragraphs min 1 max 2
                // subsection level 1 "Tutorial" with paragraphs min 1 (no max)
                // subsection level 2 with paragraphs min 1 max 2
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                            - name: einleitung
                              level: 1
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^Einleitung$"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                            - name: tutorial
                              level: 1
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^Tutorial$"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      severity: error
                              subsections:
                                - level: 2
                                  occurrence:
                                    min: 0
                                    max: 10
                                  title:
                                    pattern: "^Step \\\\d+$"
                                    severity: error
                                  allowedBlocks:
                                    - paragraph:
                                        severity: error
                                        occurrence:
                                          min: 1
                                          max: 2
                                          severity: error
                        """;

                String adocContent = """
                        = My Document

                        This is a paragraph at document level.

                        This is another paragraph at document level.

                        == Einleitung

                        First paragraph in Einleitung section.

                        Second paragraph in Einleitung section.

                        == Tutorial

                        First paragraph in Tutorial.

                        Second paragraph in Tutorial.

                        Third paragraph in Tutorial (no max limit).

                        === Step 1

                        Step 1 paragraph.

                        === Step 2

                        Step 2 first paragraph.

                        Step 2 second paragraph.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should detect too many paragraphs in Einleitung section")
            void shouldDetectTooManyParagraphsInEinleitung() {
                // Given
                String rules = """
                        document:
                          sections:
                            - name: einleitung
                              level: 1
                              occurrence:
                                min: 1
                                max: 1
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                        """;

                String adocContent = """
                        = My Document

                        == Einleitung

                        First paragraph.

                        Second paragraph.

                        Third paragraph - this exceeds the max of 2.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertTrue(result
                        .getMessages()
                        .stream()
                        .anyMatch(msg -> msg.getMessage().contains("Too many occurrences of block: paragraph")));
            }

            @Test
            @DisplayName("should validate document level blocks correctly")
            void shouldValidateDocumentLevelBlocksCorrectly() {
                // Given - Simple test with just document level paragraphs
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                        """;

                String adocContent = """
                        = My Document

                        First paragraph at document level.

                        Second paragraph at document level.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should validate exactly two paragraphs at document level")
            void shouldValidateExactlyTwoParagraphsAtDocumentLevel() {
                // Given - Only document level with 2 paragraphs
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 2
                                      max: 2
                                      severity: error
                        """;

                String adocContent = """
                        = My Document

                        First paragraph at document level.

                        Second paragraph at document level.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should validate document level blocks with sections present")
            void shouldValidateDocumentLevelBlocksWithSectionsPresent() {
                // Given - Document with 2 paragraphs at root + section with paragraph
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 2
                                      max: 2
                                      severity: error
                            - name: section1
                              level: 1
                              occurrence:
                                min: 0
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                        """;

                String adocContent = """
                        = My Document

                        First paragraph at document level.

                        Second paragraph at document level.

                        == Section One

                        Paragraph in section one.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors());
            }

            @Test
            @DisplayName("should detect too many paragraphs at document level (level 0)")
            void shouldDetectTooManyParagraphsAtDocumentLevel() {
                // Given
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                      severity: error
                        """;

                String adocContent = """
                        = My Document

                        First paragraph at document level.

                        Second paragraph at document level.

                        Third paragraph - this exceeds the max of 2.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertTrue(result.hasErrors());
                assertTrue(result
                        .getMessages()
                        .stream()
                        .anyMatch(msg -> msg.getMessage().contains("Too many occurrences of block: paragraph")));
            }

            @Test
            @DisplayName("should validate Level 0 subsections configuration - debugging")
            void shouldValidateLevel0SubsectionsConfigurationDebug() {
                // Given - Level 0 WITHOUT subsections first to verify it works
                String rulesWithoutSubsections = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: info
                                    occurrence:
                                      min: 1
                                      max: 1
                            - name: mainSections
                              level: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: info
                              allowedBlocks:
                                - paragraph:
                                    severity: info
                        """;

                String adocContent = """
                        = Document Title

                        This is the document preamble paragraph.

                        == Main Section

                        This is a paragraph in the main section.

                        Another paragraph in the main section.
                        """;

                // First verify without subsections works
                LinterConfiguration configWithout = configLoader.loadConfiguration(rulesWithoutSubsections);

                ValidationResult resultWithout = linter.validateContent(adocContent, configWithout);
                assertFalse(resultWithout.hasErrors(),
                        "Without subsections should work, but got: " + resultWithout
                                .getMessages()
                                .stream()
                                .map(msg -> msg.getMessage())
                                .collect(Collectors.joining(", ")));
            }

            @Test
            @DisplayName("should validate named blocks matching unnamed document blocks")
            void shouldValidateNamedBlocksMatchingUnnamedDocumentBlocks() {
                // Given - Config with named blocks
                String rules = """
                        document:
                          sections:
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              allowedBlocks:
                                - paragraph:
                                    name: Introduction paragraph
                                    severity: error
                                    occurrence:
                                      min: 1
                                      max: 2
                                - listing:
                                    name: Example code
                                    severity: info
                        """;

                String adocContent = """
                        = My Document

                        This is an introduction paragraph without a name attribute.

                        Second paragraph.

                        [source,java]
                        ----
                        public class Example {
                            // Code without name attribute
                        }
                        ----
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors(), "Named blocks in config should match unnamed blocks in document");
                assertFalse(result.hasWarnings());
            }

            @Test
            @DisplayName("should validate Level 0 subsections configuration")
            void shouldValidateLevel0SubsectionsConfiguration() {
                // Given - Level 0 with subsections structure exactly as in basic-rules.yaml
                String rules = """
                        document:
                          sections:
                            # Document title (level 0) with subsections structure
                            - name: documentTitle
                              level: 0
                              occurrence:
                                min: 1
                                max: 1
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    name: Document preamble
                                    severity: info
                                    occurrence:
                                      min: 1
                                      max: 1
                              subsections:
                                # Level 1 sections defined as subsections of level 0
                                - name: mainSections
                                  level: 1
                                  title:
                                    pattern: "^[A-Z].*"
                                    severity: info
                                  allowedBlocks:
                                    - paragraph:
                                        name: Section paragraphs
                                        severity: info
                        """;

                String adocContent = """
                        = Document Title

                        This is the document preamble paragraph.

                        == Main Section

                        This is a paragraph in the main section.

                        Another paragraph in the main section.
                        """;

                LinterConfiguration config = configLoader.loadConfiguration(rules);

                // When
                ValidationResult result = linter.validateContent(adocContent, config);

                // Then
                assertFalse(result.hasErrors(), "Expected no errors, but got: "
                        + result.getMessages().stream().map(msg -> msg.getMessage()).collect(Collectors.joining(", ")));
                assertFalse(result.hasWarnings());
            }

        }
    }

    @Nested
    @DisplayName("Block Occurrence Validation")
    class BlockOccurrenceValidation {

        @Test
        @DisplayName("should validate minimum occurrence of blocks")
        void shouldValidateMinimumOccurrenceOfBlocks() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 2
                                  severity: error
                            - listing:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Implementation

                    This is only one paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(2, result.getErrorCount()); // Missing 1 paragraph and 1 listing
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Too few occurrences of block: paragraph")));
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Too few occurrences of block: listing")));
        }

        @Test
        @DisplayName("should validate maximum occurrence of blocks")
        void shouldValidateMaximumOccurrenceOfBlocks() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  max: 2
                                  severity: error
                            - image:
                                severity: error
                                occurrence:
                                  max: 1
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Overview

                    First paragraph.

                    Second paragraph.

                    Third paragraph - exceeds max.

                    image::diagram1.png[]

                    image::diagram2.png[]
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(2, result.getErrorCount()); // Too many paragraphs and images
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Too many occurrences of block: paragraph")));
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Too many occurrences of block: image")));
        }

        @Test
        @DisplayName("should validate exact occurrence of blocks")
        void shouldValidateExactOccurrenceOfBlocks() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 3
                                  max: 3
                                  severity: error
                            - table:
                                severity: error
                                occurrence:
                                  min: 1
                                  max: 1
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Configuration

                    First paragraph about configuration.

                    Second paragraph with details.

                    Third paragraph with examples.

                    |===
                    | Option | Description
                    | debug | Enable debug mode
                    | verbose | Enable verbose output
                    |===
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should validate occurrence with mixed severity levels")
        void shouldValidateOccurrenceWithMixedSeverityLevels() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          title:
                            pattern: ".*"
                            severity: info
                          allowedBlocks:
                            - paragraph:
                                severity: warn
                                occurrence:
                                  min: 1
                                  severity: error
                            - listing:
                                severity: info
                                occurrence:
                                  max: 2
                                  severity: warn
                    """;

            String adocContent = """
                    = Document

                    == Section

                    [source,java]
                    ----
                    // First listing
                    ----

                    [source,java]
                    ----
                    // Second listing
                    ----

                    [source,java]
                    ----
                    // Third listing - exceeds max
                    ----
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors()); // min paragraph violation is error
            assertTrue(result.hasWarnings()); // max listing violation is warning
            assertEquals(1, result.getErrorCount());
            assertEquals(1, result.getWarningCount());
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getSeverity().toString().equals("ERROR")
                            && msg.getMessage().contains("Too few occurrences of block: paragraph")));
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getSeverity().toString().equals("WARN")
                            && msg.getMessage().contains("Too many occurrences of block: listing")));
        }

        @Test
        @DisplayName("should validate occurrence across multiple sections")
        void shouldValidateOccurrenceAcrossMultipleSections() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: introSection
                          level: 1
                          title:
                            pattern: "^Introduction$"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 2
                                  max: 3
                                  severity: error
                        - name: detailsSection
                          level: 1
                          title:
                            pattern: "^Details$"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                            - listing:
                                severity: error
                                occurrence:
                                  min: 1
                                  max: 2
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Introduction

                    First intro paragraph.

                    Second intro paragraph.

                    == Details

                    Details paragraph.

                    [source,java]
                    ----
                    public class Example {
                        // Code example
                    }
                    ----
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should show placeholder at correct position in nested sections")
        void shouldShowPlaceholderAtCorrectPositionInNestedSections() {
            // Given - Document with level 0, 1, and 2 sections where listing is missing in
            // level 2
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                        - name: mainSection
                          level: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                          subsections:
                            - name: subSection
                              level: 2
                              title:
                                pattern: "^[A-Z].*"
                                severity: error
                              allowedBlocks:
                                - paragraph:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      severity: error
                                - listing:
                                    severity: error
                                    occurrence:
                                      min: 1
                                      severity: error
                    """;

            String adocContent = """
                    = My Document

                    This is a paragraph at document level.

                    == Main Section

                    This is a paragraph in the main section.

                    === Sub Section

                    This is a paragraph in the sub section.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(1, result.getErrorCount());

            // Check that the error is for the missing listing in level 2
            ValidationMessage msg = result.getMessages().get(0);
            assertEquals("Too few occurrences of block: listing", msg.getMessage());

            // The location should point to where the missing block should be inserted
            // The placeholder should appear after the paragraph in the subsection
            // After our fix, the line is correctly calculated as 13 (after the last line of
            // content)
            assertEquals(13, msg.getLocation().getStartLine()); // Points to where block should be added

            // Let's also run a console report to see where the placeholder appears
            // This would help debug the actual positioning
        }

        @Test
        @DisplayName("should handle blocks without occurrence configuration")
        void shouldHandleBlocksWithoutOccurrenceConfiguration() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          title:
                            pattern: ".*"
                            severity: info
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                # No occurrence configuration
                            - listing:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Section

                    Multiple paragraphs are allowed.

                    Another paragraph.

                    And another one.

                    [source,java]
                    ----
                    // Required listing
                    ----
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }
    }

    @Nested
    @DisplayName("Block Order Validation")
    class BlockOrderValidation {

        @Test
        @DisplayName("should validate block order using order attribute")
        void shouldValidateBlockOrderUsingOrderAttribute() {
            // Given - Configuration with order attributes for blocks
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          title:
                            pattern: ".*"
                            severity: info
                          allowedBlocks:
                            - paragraph:
                                name: intro
                                severity: error
                                order: 1
                            - listing:
                                name: code
                                severity: error
                                order: 2
                            - paragraph:
                                name: explanation
                                severity: error
                                order: 3
                    """;

            // Wrong order: code (order=2) before intro (order=1)
            String adocContent = """
                    = Document

                    == Section One

                    [source,java]
                    ----
                    public class Test {
                    }
                    ----

                    This is the introduction paragraph.

                    This is the explanation paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertTrue(
                    result
                            .getMessages()
                            .stream()
                            .anyMatch(msg -> msg.getMessage().contains("order") || msg.getMessage().contains("Order")),
                    "Expected order validation error, but got: " + result
                            .getMessages()
                            .stream()
                            .map(ValidationMessage::getMessage)
                            .collect(Collectors.joining(", ")));
        }

        @Test
        @DisplayName("should accept correct block order")
        void shouldAcceptCorrectBlockOrder() {
            // Given - Configuration with order attributes
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          title:
                            pattern: ".*"
                            severity: info
                          allowedBlocks:
                            - paragraph:
                                name: intro
                                severity: error
                                order: 1
                            - listing:
                                name: code
                                severity: error
                                order: 2
                            - paragraph:
                                name: explanation
                                severity: error
                                order: 3
                    """;

            // Richtige Reihenfolge
            String adocContent = """
                    = Document

                    == Section One

                    This is the introduction paragraph.

                    [source,java]
                    ----
                    public class Test {
                    }
                    ----

                    This is the explanation paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors(), "Expected no errors for correct order, but got: "
                    + result.getMessages().stream().map(msg -> msg.getMessage()).collect(Collectors.joining(", ")));
        }

        @Test
        @DisplayName("should handle blocks without order attribute")
        void shouldHandleBlocksWithoutOrderAttribute() {
            // Given - Mix of blocks with and without order
            String rules = """
                    document:
                      sections:
                        - name: mainSection
                          level: 1
                          title:
                            pattern: ".*"
                            severity: info
                          allowedBlocks:
                            - paragraph:
                                name: intro
                                severity: error
                                order: 1
                            - listing:
                                name: code
                                severity: error
                                # no order specified
                            - paragraph:
                                name: conclusion
                                severity: error
                                order: 10
                    """;

            String adocContent = """
                    = Document

                    == Section One

                    This is the introduction paragraph.

                    [source,java]
                    ----
                    // Code can appear anywhere when no order specified
                    ----

                    This is the conclusion paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors(), "Blocks without order should be allowed anywhere, but got: "
                    + result.getMessages().stream().map(msg -> msg.getMessage()).collect(Collectors.joining(", ")));
        }
    }

    @Nested
    @DisplayName("Front Matter Support")
    class FrontMatterSupport {

        @Test
        @DisplayName("should skip front matter when validating document")
        void shouldSkipFrontMatterWhenValidatingDocument() {
            // Given - Document with YAML front matter
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  max: 2
                                  severity: error
                    """;

            String adocContent = """
                    ---
                    title: My Document
                    author: John Doe
                    tags: [asciidoc, testing]
                    date: 2024-01-15
                    ---
                    = My Document

                    This is the first paragraph after front matter.

                    This is the second paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then - Should validate successfully, ignoring the front matter
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should validate document with front matter and sections")
        void shouldValidateDocumentWithFrontMatterAndSections() {
            // Given - Complex document with front matter
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  severity: error
                        - name: intro
                          level: 1
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^Introduction$"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  max: 3
                                  severity: error
                    """;

            String adocContent = """
                    ---
                    layout: post
                    title: Technical Documentation
                    author: Jane Smith
                    categories:
                      - documentation
                      - technical
                    metadata:
                      version: 1.0.0
                      status: draft
                    ---
                    = Technical Documentation

                    This is the document introduction paragraph.

                    == Introduction

                    First paragraph of the introduction section.

                    Second paragraph with more details.

                    Third paragraph concluding the introduction.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should handle empty front matter")
        void shouldHandleEmptyFrontMatter() {
            // Given - Document with empty front matter
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          allowedBlocks:
                            - paragraph:
                                severity: error
                    """;

            String adocContent = """
                    ---
                    ---
                    = Document with Empty Front Matter

                    This paragraph should be validated normally.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should validate document without front matter normally")
        void shouldValidateDocumentWithoutFrontMatterNormally() {
            // Given - Document without front matter (ensure backward compatibility)
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 2
                                  max: 2
                                  severity: error
                    """;

            String adocContent = """
                    = Regular Document

                    First paragraph.

                    Second paragraph.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should detect errors after front matter")
        void shouldDetectErrorsAfterFrontMatter() {
            // Given - Document with front matter but violating rules
            String rules = """
                    document:
                      sections:
                        - name: documentTitle
                          level: 0
                          occurrence:
                            min: 1
                            max: 1
                          title:
                            pattern: "^[A-Z].*"
                            severity: error
                          allowedBlocks:
                            - paragraph:
                                severity: error
                                occurrence:
                                  min: 1
                                  max: 2
                                  severity: error
                    """;

            String adocContent = """
                    ---
                    title: Document
                    author: Test Author
                    ---
                    = lowercase title

                    First paragraph.

                    Second paragraph.

                    Third paragraph - this exceeds the max.
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(2, result.getErrorCount()); // Title pattern error + too many paragraphs

            // Check for title pattern error
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Document title does not match required pattern")));

            // Check for paragraph occurrence error
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("Too many occurrences of block: paragraph")));
        }
    }

    @Nested
    @DisplayName("Definition List Validation")
    class DefinitionListValidation {

        @Test
        @DisplayName("should validate definition list term count")
        void shouldValidateDefinitionListTermCount() {
            // Given
            String rules = """
                    document:
                      sections:
                        - name: glossary
                          level: 1
                          title:
                            pattern: "^Glossary$"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                terms:
                                  min: 3
                                  severity: warn
                    """;

            String adocContent = """
                    = Document

                    == Glossary

                    API:: Application Programming Interface
                    HTTP:: Hypertext Transfer Protocol
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasWarnings());
            assertEquals(1, result.getMessages().size());
            ValidationMessage msg = result.getMessages().get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertTrue(msg.getMessage().contains("too few terms"));
        }

        @Test
        @DisplayName("should validate definition list term pattern")
        void shouldValidateDefinitionListTermPattern() {
            // Given
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: ".*"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                terms:
                                  pattern: "^[A-Z].*"
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Section

                    API:: Good - starts with uppercase
                    http:: Bad - starts with lowercase
                    REST:: Good - all uppercase
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(1, result.getMessages().size());
            ValidationMessage msg = result.getMessages().get(0);
            assertEquals("http", msg.getActualValue().orElse(""));
            assertTrue(msg.getMessage().contains("does not match required pattern"));
        }

        @Test
        @DisplayName("should validate definition list description requirements")
        void shouldValidateDefinitionListDescriptionRequirements() {
            // Given
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: ".*"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                descriptions:
                                  required: true
                                  pattern: ".*\\\\.$"
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Section

                    Term1:: Description with period.
                    Term2:: Description without period
                    Term3::
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertTrue(result.hasErrors());
            assertEquals(2, result.getMessages().size());

            // Check for pattern violation
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("does not match required pattern")
                            && msg.getActualValue().orElse("").equals("Description without period")));

            // Check for missing description
            assertTrue(result
                    .getMessages()
                    .stream()
                    .anyMatch(msg -> msg.getMessage().contains("missing required description")));
        }

        @Test
        @DisplayName("should skip nesting level validation - not supported by AsciiDoctor AST")
        void shouldSkipNestingLevelValidation() {
            // Given
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: ".*"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                nestingLevel:
                                  max: 1
                                  severity: warn
                    """;

            String adocContent = """
                    = Document

                    == Section

                    Level1::
                      Nested::
                        DoubleNested:::
                          This exceeds max nesting level
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then - nesting validation is not implemented
            assertFalse(result.hasWarnings());
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should skip delimiter style validation - not supported by AsciiDoctor AST")
        void shouldSkipDelimiterStyleValidation() {
            // Given
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: ".*"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                delimiterStyle:
                                  allowedDelimiters: ["::", ":::"]
                                  severity: error
                    """;

            String adocContent = """
                    = Document

                    == Section

                    Term1:: Valid delimiter
                    Term2::: Also valid
                    Term3:::: Invalid delimiter
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then - delimiter validation is not implemented
            assertFalse(result.hasErrors());
            assertFalse(result.hasWarnings());
        }

        @Test
        @DisplayName("should pass valid definition list")
        void shouldPassValidDefinitionList() {
            // Given
            String rules = """
                    document:
                      sections:
                        - level: 1
                          title:
                            pattern: ".*"
                            severity: error
                          allowedBlocks:
                            - dlist:
                                severity: error
                                terms:
                                  min: 2
                                  max: 5
                                  pattern: "^[A-Z].*"
                                descriptions:
                                  required: true
                    """;

            String adocContent = """
                    = Document

                    == Section

                    API:: Application Programming Interface
                    HTTP:: Hypertext Transfer Protocol
                    REST:: Representational State Transfer
                    """;

            LinterConfiguration config = configLoader.loadConfiguration(rules);

            // When
            ValidationResult result = linter.validateContent(adocContent, config);

            // Then
            assertFalse(result.hasErrors());
            assertFalse(result.hasWarnings());
        }
    }
}
