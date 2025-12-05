package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import org.asciidoctor.ast.Cursor;

/**
 * Unit tests for {@link ParagraphBlockValidator}.
 * <p>
 * This test class validates the behavior of the paragraph block validator,
 * which processes text paragraph blocks in AsciiDoc documents. The tests focus
 * on line count validation rules including minimum and maximum constraints.
 * </p>
 * <p>
 * Test scenarios include:
 * </p>
 * <ul>
 * <li>Basic validator functionality</li>
 * <li>Line count validation (min/max)</li>
 * <li>Empty and null content handling</li>
 * <li>Non-empty line counting logic</li>
 * <li>Content extraction from nested blocks</li>
 * <li>Severity hierarchy with fallback to block severity</li>
 * </ul>
 * <p>
 * The validator counts only non-empty lines, ignoring blank lines and lines
 * containing only whitespace.
 * </p>
 *
 * @see ParagraphBlockValidator
 * @see ParagraphBlock
 * @see LineConfig
 */
@DisplayName("ParagraphBlockValidator")
class ParagraphBlockValidatorTest {

    private ParagraphBlockValidator validator;
    private BlockValidationContext context;
    private StructuralNode mockBlock;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        validator = new ParagraphBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(StructuralNode.class);
    }

    @Test
    @DisplayName("should return PARAGRAPH as supported type")
    void shouldReturnParagraphAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();

        // Then
        assertEquals(BlockType.PARAGRAPH, type);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return empty list when no line config")
        void shouldReturnEmptyListWhenNoLineConfig() {
            // Given
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, null);
            when(mockBlock.getContent()).thenReturn("Some content");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should validate minimum lines")
        void shouldValidateMinimumLines() {
            // Given
            LineConfig lineConfig = new LineConfig(3, null, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("paragraph.lines.min", msg.getRuleId());
            assertEquals("Paragraph has too few lines", msg.getMessage());
            assertEquals("2", msg.getActualValue().orElse(null));
            assertEquals("At least 3 lines", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should include placeholder and exact position for too few lines - based on test-errors.adoc line 11")
        void shouldIncludePlaceholderForTooFewLines() {
            // Given - Based on test-errors.adoc line 11: "This section has a paragraph
            // without any errors."
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test-errors.adoc");

            LineConfig lineConfig = new LineConfig(2, null, Severity.INFO);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(11);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.getContent()).thenReturn("This section has a paragraph without any errors.");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("paragraph.lines.min", msg.getRuleId());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("Add more content here...", msg.getMissingValueHint());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(11, msg.getLocation().getStartLine());
            assertEquals(11, msg.getLocation().getEndLine());
        }

        @Test
        @DisplayName("should validate maximum lines")
        void shouldValidateMaximumLines() {
            // Given
            LineConfig lineConfig = new LineConfig(null, 2, Severity.WARN);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("paragraph.lines.max", msg.getRuleId());
            assertEquals("Paragraph has too many lines", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At most 2 lines", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should use block severity when lines severity is not defined")
        void shouldUseBlockSeverityWhenLinesSeverityNotDefined() {
            // Given - lines has no severity, block has INFO
            LineConfig lineConfig = new LineConfig(null, 2, null); // No severity set
            ParagraphBlock config = new ParagraphBlock(null, Severity.INFO, null, null, lineConfig, null);

            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(),
                    "Should use block severity (INFO) when lines severity is not defined");
            assertEquals("paragraph.lines.max", msg.getRuleId());
        }

        @Test
        @DisplayName("should pass when lines within range")
        void shouldPassWhenLinesWithinRange() {
            // Given
            LineConfig lineConfig = new LineConfig(2, 5, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should count only non-empty lines")
        void shouldCountOnlyNonEmptyLines() {
            // Given
            LineConfig lineConfig = new LineConfig(2, null, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn("Line 1\n\n  \nLine 2");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty()); // 2 non-empty lines, meets minimum
        }

        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            // Given
            LineConfig lineConfig = new LineConfig(1, null, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn("");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            assertEquals("0", messages.get(0).getActualValue().orElse(null));
        }

        @Test
        @DisplayName("should handle null content")
        void shouldHandleNullContent() {
            // Given
            LineConfig lineConfig = new LineConfig(1, null, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);
            when(mockBlock.getContent()).thenReturn(null);
            when(mockBlock.getBlocks()).thenReturn(null);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            assertEquals("0", messages.get(0).getActualValue().orElse(null));
        }

        @Test
        @DisplayName("should get content from blocks when direct content is null")
        void shouldGetContentFromBlocksWhenDirectContentIsNull() {
            // Given
            LineConfig lineConfig = new LineConfig(1, null, Severity.ERROR);
            ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, lineConfig, null);

            StructuralNode childBlock = mock(StructuralNode.class);
            when(childBlock.getContent()).thenReturn("Child content");
            when(mockBlock.getContent()).thenReturn(null);
            when(mockBlock.getBlocks()).thenReturn(Arrays.asList(childBlock));

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty()); // Has content from child
        }
    }

    @Nested
    @DisplayName("sentence validation")
    class SentenceValidation {

        @Nested
        @DisplayName("sentence occurrence")
        class SentenceOccurrence {

            @Test
            @DisplayName("should validate minimum sentence count")
            void shouldValidateMinimumSentenceCount() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 3, null, Severity.ERROR), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.WARN, null, null, null, sentenceConfig);

                // Content with only 2 sentences
                when(mockBlock.getContent()).thenReturn("This is the first sentence. This is the second sentence.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.ERROR, msg.getSeverity());
                assertEquals("paragraph.sentence.occurrence.min", msg.getRuleId());
                assertEquals("Paragraph has too few sentences", msg.getMessage());
                assertEquals("2", msg.getActualValue().orElse(null));
                assertEquals("At least 3 sentences", msg.getExpectedValue().orElse(null));
            }

            @Test
            @DisplayName("should validate maximum sentence count")
            void shouldValidateMaximumSentenceCount() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, null, 2, Severity.WARN), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Content with 3 sentences
                when(mockBlock.getContent()).thenReturn("First sentence. Second sentence. Third sentence.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.WARN, msg.getSeverity());
                assertEquals("paragraph.sentence.occurrence.max", msg.getRuleId());
                assertEquals("Paragraph has too many sentences", msg.getMessage());
                assertEquals("3", msg.getActualValue().orElse(null));
                assertEquals("At most 2 sentences", msg.getExpectedValue().orElse(null));
            }

            @Test
            @DisplayName("should use block severity when occurrence severity is not defined")
            void shouldUseBlockSeverityWhenOccurrenceSeverityNotDefined() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 2, null, null), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.INFO, null, null, null, sentenceConfig);

                when(mockBlock.getContent()).thenReturn("Only one sentence here.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.INFO, msg.getSeverity(),
                        "Should use block severity (INFO) when occurrence severity is not defined");
            }

            @Test
            @DisplayName("should handle empty content with minimum sentences required")
            void shouldHandleEmptyContentWithMinimumSentencesRequired() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 1, null, Severity.ERROR), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                when(mockBlock.getContent()).thenReturn("");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                assertEquals("0", messages.get(0).getActualValue().orElse(null));
            }
        }

        @Nested
        @DisplayName("words per sentence")
        class WordsPerSentence {

            @Test
            @DisplayName("should validate minimum words per sentence")
            void shouldValidateMinimumWordsPerSentence() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(null,
                        new ParagraphBlock.WordsConfig(5, null, Severity.ERROR));
                ParagraphBlock config = new ParagraphBlock(null, Severity.WARN, null, null, null, sentenceConfig);

                // First sentence has only 3 words, second has 5
                when(mockBlock.getContent()).thenReturn("Too short sentence. This sentence has five words.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.ERROR, msg.getSeverity());
                assertEquals("paragraph.sentence.words.min", msg.getRuleId());
                assertEquals("Sentence 1 has too few words", msg.getMessage());
                assertEquals("3 words", msg.getActualValue().orElse(null));
                assertEquals("At least 5 words", msg.getExpectedValue().orElse(null));
            }

            @Test
            @DisplayName("should validate maximum words per sentence")
            void shouldValidateMaximumWordsPerSentence() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(null,
                        new ParagraphBlock.WordsConfig(null, 8, Severity.WARN));
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Sentence with 10 words
                when(mockBlock.getContent())
                        .thenReturn("This sentence has way too many words for the configured maximum limit.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.WARN, msg.getSeverity());
                assertEquals("paragraph.sentence.words.max", msg.getRuleId());
                assertEquals("Sentence 1 has too many words", msg.getMessage());
                assertEquals("12 words", msg.getActualValue().orElse(null));
                assertEquals("At most 8 words", msg.getExpectedValue().orElse(null));
            }

            @Test
            @DisplayName("should use block severity when words severity is not defined")
            void shouldUseBlockSeverityWhenWordsSeverityNotDefined() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(null,
                        new ParagraphBlock.WordsConfig(5, null, null));
                ParagraphBlock config = new ParagraphBlock(null, Severity.INFO, null, null, null, sentenceConfig);

                when(mockBlock.getContent()).thenReturn("Short.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(1, messages.size());
                ValidationMessage msg = messages.get(0);
                assertEquals(Severity.INFO, msg.getSeverity(),
                        "Should use block severity (INFO) when words severity is not defined");
            }

            @Test
            @DisplayName("should validate multiple sentences for word count")
            void shouldValidateMultipleSentencesForWordCount() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(null,
                        new ParagraphBlock.WordsConfig(4, 8, Severity.ERROR));
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Mix of valid and invalid sentences
                when(mockBlock.getContent()).thenReturn("Too short. " + // 2 words - too few
                        "This sentence is just right. " + // 5 words - OK
                        "This sentence has way too many words to be considered valid." // 12 words - too many
                );

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(2, messages.size());
                assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Sentence 1 has too few words")));
                assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Sentence 3 has too many words")));
            }
        }

        @Nested
        @DisplayName("sentence detection")
        class SentenceDetection {

            @Test
            @DisplayName("should detect sentences with different punctuation")
            void shouldDetectSentencesWithDifferentPunctuation() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 3, 3, Severity.ERROR), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Content with period, question mark, and exclamation mark
                when(mockBlock.getContent()).thenReturn("This is a statement. Is this a question? This is exciting!");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertTrue(messages.isEmpty()); // Exactly 3 sentences
            }

            @Test
            @DisplayName("should handle multi-line sentences")
            void shouldHandleMultiLineSentences() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 2, 2, Severity.ERROR), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Content with sentences spanning multiple lines
                when(mockBlock.getContent())
                        .thenReturn("This is a long sentence that\nspans multiple lines\nbut is still one sentence. "
                                + "This is\nanother sentence.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertTrue(messages.isEmpty()); // Exactly 2 sentences
            }

            @Test
            @DisplayName("should treat content without sentence ending as one sentence")
            void shouldTreatContentWithoutSentenceEndingAsOneSentence() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 1, 1, Severity.ERROR), null);
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, sentenceConfig);

                // Content without sentence-ending punctuation
                when(mockBlock.getContent()).thenReturn("This is content without proper punctuation");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertTrue(messages.isEmpty()); // Treated as 1 sentence
            }
        }

        @Nested
        @DisplayName("complex sentence validation")
        class ComplexSentenceValidation {

            @Test
            @DisplayName("should validate both occurrence and words constraints")
            void shouldValidateBothOccurrenceAndWordsConstraints() {
                // Given
                ParagraphBlock.SentenceConfig sentenceConfig = new ParagraphBlock.SentenceConfig(
                        new OccurrenceConfig(null, 2, 4, Severity.WARN),
                        new ParagraphBlock.WordsConfig(5, 10, Severity.ERROR));
                ParagraphBlock config = new ParagraphBlock(null, Severity.INFO, null, null, null, sentenceConfig);

                // One sentence with too few words, one with too many
                when(mockBlock.getContent()).thenReturn("Short. " + // 1 word - too few
                        "This sentence has way too many words and should trigger a validation error for exceeding the limit." // 17
                                                                                                                              // words
                                                                                                                              // -
                                                                                                                              // too
                                                                                                                              // many
                );

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertEquals(2, messages.size());
                // Check word violations (should use ERROR severity)
                assertTrue(messages
                        .stream()
                        .anyMatch(m -> m.getRuleId().equals("paragraph.sentence.words.min")
                                && m.getSeverity() == Severity.ERROR));
                assertTrue(messages
                        .stream()
                        .anyMatch(m -> m.getRuleId().equals("paragraph.sentence.words.max")
                                && m.getSeverity() == Severity.ERROR));
            }

            @Test
            @DisplayName("should handle no sentence config gracefully")
            void shouldHandleNoSentenceConfigGracefully() {
                // Given
                ParagraphBlock config = new ParagraphBlock(null, Severity.ERROR, null, null, null, null);

                when(mockBlock.getContent()).thenReturn("Some content with sentences. Another sentence here.");

                // When
                List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

                // Then
                assertTrue(messages.isEmpty()); // No sentence validation performed
            }
        }
    }
}
