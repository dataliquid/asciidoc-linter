package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("QuoteBlockValidator Tests")
class QuoteBlockValidatorTest {

    private QuoteBlockValidator validator;
    private StructuralNode mockNode;
    private BlockValidationContext mockContext;

    @BeforeEach
    void setUp() {
        validator = new QuoteBlockValidator();
        mockNode = mock(StructuralNode.class);
        mockContext = mock(BlockValidationContext.class);

        // Mock the location creation
        SourceLocation mockLocation = mock(SourceLocation.class);
        when(mockContext.createLocation(any(StructuralNode.class))).thenReturn(mockLocation);
        when(mockContext.getFilename()).thenReturn("test.adoc");
        when(mockNode.getSourceLocation()).thenReturn(null);
    }

    @Test
    @DisplayName("should return QUOTE block type")
    void shouldReturnCorrectBlockType() {
        assertEquals(BlockType.QUOTE, validator.getSupportedType());
    }

    @Test
    @DisplayName("should return empty list for non-quote block config")
    void shouldReturnEmptyListForNonQuoteBlock() {
        Block nonQuoteBlock = mock(Block.class);
        List<ValidationMessage> results = validator.validate(mockNode, nonQuoteBlock, mockContext);
        assertTrue(results.isEmpty());
    }

    @Nested
    @DisplayName("Author Validation Tests")
    class AttributionValidationTests {

        @Test
        @DisplayName("should validate required author when missing")
        void shouldValidateRequiredAuthorWhenMissing() {
            when(mockNode.getAttribute("author")).thenReturn(null);
            when(mockNode.getAttribute("attribution")).thenReturn(null);
            when(mockNode.getAttribute("1")).thenReturn(null);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .attribution(QuoteBlock.AttributionConfig.builder().required(true).severity(Severity.ERROR).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("Quote attribution is required but not provided"));
        }

        @Test
        @DisplayName("should validate author min length")
        void shouldValidateAuthorMinLength() {
            when(mockNode.getAttribute("author")).thenReturn("AB");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .attribution(QuoteBlock.AttributionConfig.builder().minLength(3).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("too short"));
            assertTrue(results.get(0).getMessage().contains("minimum 3"));
            assertTrue(results.get(0).getMessage().contains("found 2"));
        }

        @Test
        @DisplayName("should validate author max length")
        void shouldValidateAuthorMaxLength() {
            String longAuthor = "A".repeat(101);
            when(mockNode.getAttribute("author")).thenReturn(longAuthor);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.INFO)
                    .attribution(QuoteBlock.AttributionConfig.builder().maxLength(100).severity(Severity.ERROR).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("too long"));
            assertTrue(results.get(0).getMessage().contains("maximum 100"));
            assertTrue(results.get(0).getMessage().contains("found 101"));
        }

        @Test
        @DisplayName("should validate author pattern")
        void shouldValidateAuthorPattern() {
            when(mockNode.getAttribute("author")).thenReturn("123 Invalid");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .attribution(QuoteBlock.AttributionConfig.builder().pattern("^[A-Z][a-zA-Z\\s\\.\\-,]+$").build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("Quote attribution does not match required pattern"));
            assertEquals("123 Invalid", results.get(0).getActualValue().orElse(null));
        }

        @Test
        @DisplayName("should accept valid author")
        void shouldAcceptValidAuthor() {
            when(mockNode.getAttribute("author")).thenReturn("John Doe");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN).attribution(QuoteBlock.AttributionConfig
                    .builder().required(true).minLength(3).maxLength(100).pattern("^[A-Z][a-zA-Z\\s\\.\\-,]+$").build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should check multiple author sources")
        void shouldCheckMultipleAuthorSources() {
            when(mockNode.getAttribute("author")).thenReturn(null);
            when(mockNode.getAttribute("attribution")).thenReturn("Jane Smith");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .attribution(QuoteBlock.AttributionConfig.builder().required(true).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Source Validation Tests")
    class CitationValidationTests {

        @Test
        @DisplayName("should validate required source when missing")
        void shouldValidateRequiredSourceWhenMissing() {
            when(mockNode.getAttribute("citetitle")).thenReturn(null);
            when(mockNode.getAttribute("source")).thenReturn(null);
            when(mockNode.getAttribute("2")).thenReturn(null);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .citation(QuoteBlock.CitationConfig.builder().required(true).severity(Severity.ERROR).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("Quote citation is required but not provided"));
        }

        @Test
        @DisplayName("should validate source pattern")
        void shouldValidateSourcePattern() {
            when(mockNode.getAttribute("citetitle")).thenReturn("Book @ Title #123");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .citation(QuoteBlock.CitationConfig.builder().pattern("^[A-Za-z0-9\\s,\\.\\-\\(\\)]+$").build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("Quote citation does not match required pattern"));
        }

        @Test
        @DisplayName("should check multiple source attributes")
        void shouldCheckMultipleSourceAttributes() {
            when(mockNode.getAttribute("citetitle")).thenReturn(null);
            when(mockNode.getAttribute("source")).thenReturn(null);
            when(mockNode.getAttribute("2")).thenReturn("The Great Book");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .citation(QuoteBlock.CitationConfig.builder().required(true).minLength(5).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @DisplayName("should validate required content when missing")
        void shouldValidateRequiredContentWhenMissing() {
            when(mockNode.getContent()).thenReturn(null);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .content(QuoteBlock.ContentConfig.builder().required(true).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("requires content"));
        }

        @Test
        @DisplayName("should validate content min length")
        void shouldValidateContentMinLength() {
            when(mockNode.getContent()).thenReturn("Short");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .content(QuoteBlock.ContentConfig.builder().minLength(20).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too short"));
            assertTrue(results.get(0).getMessage().contains("minimum 20"));
            assertTrue(results.get(0).getMessage().contains("found 5"));
        }

        @Test
        @DisplayName("should validate content max length")
        void shouldValidateContentMaxLength() {
            String longContent = "A".repeat(1001);
            when(mockNode.getContent()).thenReturn(longContent);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .content(QuoteBlock.ContentConfig.builder().maxLength(1000).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too long"));
            assertTrue(results.get(0).getMessage().contains("maximum 1000"));
            assertTrue(results.get(0).getMessage().contains("found 1001"));
        }

        @Test
        @DisplayName("should validate content line count")
        void shouldValidateContentLineCount() {
            String multiLineContent = "Line 1\nLine 2\nLine 3";
            when(mockNode.getContent()).thenReturn(multiLineContent);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .content(QuoteBlock.ContentConfig.builder()
                            .lines(QuoteBlock.LinesConfig.builder().min(5).severity(Severity.ERROR).build()).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("too few lines"));
            assertTrue(results.get(0).getMessage().contains("minimum 5"));
            assertTrue(results.get(0).getMessage().contains("found 3"));
        }

        @Test
        @DisplayName("should validate max line count")
        void shouldValidateMaxLineCount() {
            String manyLines = String.join("\n", "Line".repeat(21).split("(?<=.{4})"));
            when(mockNode.getContent()).thenReturn(manyLines);

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN).content(
                    QuoteBlock.ContentConfig.builder().lines(QuoteBlock.LinesConfig.builder().max(20).build()).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too many lines"));
        }
    }

    @Nested
    @DisplayName("Severity Hierarchy Tests")
    class SeverityHierarchyTests {

        @Test
        @DisplayName("should use nested severity over block severity")
        void shouldUseNestedSeverityOverBlockSeverity() {
            when(mockNode.getAttribute("author")).thenReturn("AB");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.INFO) // Block level
                    .attribution(QuoteBlock.AttributionConfig.builder().minLength(3).severity(Severity.ERROR) // Nested
                                                                                                              // level
                            .build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
        }

        @Test
        @DisplayName("should fall back to block severity when nested not specified")
        void shouldFallBackToBlockSeverity() {
            when(mockNode.getAttribute("source")).thenReturn("AB");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN) // Block level
                    .citation(QuoteBlock.CitationConfig.builder().minLength(3)
                            // No severity specified
                            .build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should validate complete quote block")
        void shouldValidateCompleteQuoteBlock() {
            when(mockNode.getAttribute("author")).thenReturn("Albert Einstein");
            when(mockNode.getAttribute("citetitle")).thenReturn("Theory of Relativity");
            when(mockNode.getContent()).thenReturn("Imagination is more important than knowledge. "
                    + "Knowledge is limited. Imagination embraces the entire world, stimulating progress, giving birth to evolution.");

            QuoteBlock block = QuoteBlock.builder().severity(Severity.INFO)
                    .attribution(QuoteBlock.AttributionConfig.builder().required(true).minLength(3).maxLength(100)
                            .pattern("^[A-Z][a-zA-Z\\s\\.\\-,]+$").severity(Severity.ERROR).build())
                    .citation(QuoteBlock.CitationConfig.builder().required(false).minLength(5).maxLength(200).build())
                    .content(QuoteBlock.ContentConfig.builder().required(true).minLength(20).maxLength(1000)
                            .lines(QuoteBlock.LinesConfig.builder().min(1).max(20).build()).build())
                    .build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should collect multiple validation errors")
        void shouldCollectMultipleValidationErrors() {
            when(mockNode.getAttribute("author")).thenReturn("a"); // Too short
            when(mockNode.getAttribute("citetitle")).thenReturn("b"); // Too short
            when(mockNode.getContent()).thenReturn("Short"); // Too short

            QuoteBlock block = QuoteBlock.builder().severity(Severity.WARN)
                    .attribution(QuoteBlock.AttributionConfig.builder().minLength(3).build())
                    .citation(QuoteBlock.CitationConfig.builder().minLength(5).build())
                    .content(QuoteBlock.ContentConfig.builder().minLength(20).build()).build();

            List<ValidationMessage> results = validator.validate(mockNode, block, mockContext);

            assertEquals(3, results.size());
            assertTrue(results.stream().allMatch(r -> r.getSeverity() == Severity.WARN));
        }
    }
}
