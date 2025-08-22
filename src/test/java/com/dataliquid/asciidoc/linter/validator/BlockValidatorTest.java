package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

@DisplayName("BlockValidator")
class BlockValidatorTest {

    private BlockValidator validator;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        validator = new BlockValidator();
        mockSection = mock(Section.class);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return success when section has no blocks")
        void shouldReturnSuccessWhenSectionHasNoBlocks() {
            // Given
            SectionConfig config = SectionConfig.builder().name("Introduction").build();
            when(mockSection.getBlocks()).thenReturn(null);

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertFalse(result.hasErrors());
            assertTrue(result.getMessages().isEmpty());
        }

        @Test
        @DisplayName("should validate all blocks in section")
        void shouldValidateAllBlocksInSection() {
            // Given
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Introduction")
                    .allowedBlocks(Arrays.asList(paragraphConfig)).build();

            Block block1 = mock(Block.class);
            Block block2 = mock(Block.class);
            when(block1.getContext()).thenReturn("paragraph");
            when(block2.getContext()).thenReturn("paragraph");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block1, block2));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertFalse(result.hasErrors()); // No validation rules configured
        }

        @Test
        @DisplayName("should validate unknown block types")
        void shouldValidateUnknownBlockTypes() {
            // Given
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Section").allowedBlocks(Arrays.asList(paragraphConfig))
                    .build();

            Block unknownBlock = mock(Block.class);
            when(unknownBlock.getContext()).thenReturn("unknown-type");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(unknownBlock));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertTrue(result.hasErrors());
            assertEquals(1, result.getMessages().size());
            ValidationMessage msg = result.getMessages().get(0);
            assertEquals("block.type.unknown", msg.getRuleId());
            assertEquals("Unknown block type: unknown-type", msg.getMessage());
        }
    }

    @Nested
    @DisplayName("occurrence validation")
    class OccurrenceValidation {

        @Test
        @DisplayName("should validate block occurrences")
        void shouldValidateBlockOccurrences() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder().min(2).max(3).severity(Severity.ERROR)
                    .build();
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().name("content").occurrence(occurrenceConfig)
                    .severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Section").allowedBlocks(Arrays.asList(paragraphConfig))
                    .build();

            // Only one paragraph block (violates min)
            Block block = mock(Block.class);
            when(block.getContext()).thenReturn("paragraph");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertTrue(result.hasErrors());
            assertTrue(result.getMessages().stream().anyMatch(m -> "block.occurrence.min".equals(m.getRuleId())));
        }

        @Test
        @DisplayName("should track occurrences across multiple blocks")
        void shouldTrackOccurrencesAcrossMultipleBlocks() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder().min(1).max(2).severity(Severity.WARN)
                    .build();
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().occurrence(occurrenceConfig)
                    .severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Section").allowedBlocks(Arrays.asList(paragraphConfig))
                    .build();

            // Three paragraph blocks (violates max)
            Block block1 = mock(Block.class);
            Block block2 = mock(Block.class);
            Block block3 = mock(Block.class);
            when(block1.getContext()).thenReturn("paragraph");
            when(block2.getContext()).thenReturn("paragraph");
            when(block3.getContext()).thenReturn("paragraph");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block1, block2, block3));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertTrue(result.hasWarnings());
            assertTrue(result.getMessages().stream().anyMatch(m -> "block.occurrence.max".equals(m.getRuleId())));
        }
    }

    @Nested
    @DisplayName("order validation")
    class OrderValidation {

        @Test
        @DisplayName("should validate block order using order attribute")
        void shouldValidateBlockOrderUsingOrderAttribute() {
            // Given
            ParagraphBlock headerBlock = ParagraphBlock.builder().name("header").severity(Severity.ERROR).order(1)
                    .build();
            TableBlock dataBlock = TableBlock.builder().name("data").severity(Severity.ERROR).order(2).build();

            SectionConfig config = SectionConfig.builder().name("Section")
                    .allowedBlocks(Arrays.asList(headerBlock, dataBlock)).build();

            // Wrong order: data before header
            Block block1 = mock(Block.class);
            Block block2 = mock(Block.class);
            when(block1.getContext()).thenReturn("table");
            when(block2.getContext()).thenReturn("paragraph");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block1, block2));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertTrue(result.hasErrors());
            assertTrue(result.getMessages().stream().anyMatch(m -> "block.order".equals(m.getRuleId())));
        }
    }

    @Nested
    @DisplayName("complex validation scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRulesTogether() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder().min(1).max(2).severity(Severity.ERROR)
                    .build();

            ParagraphBlock paragraphConfig = ParagraphBlock.builder().occurrence(occurrenceConfig)
                    .severity(Severity.ERROR).build();

            TableBlock tableConfig = TableBlock.builder().severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Section")
                    .allowedBlocks(Arrays.asList(paragraphConfig, tableConfig)).build();

            // Setup blocks: table, then paragraph
            Block tableBlock = mock(Block.class);
            when(tableBlock.getContext()).thenReturn("table");

            Block paragraphBlock = mock(Block.class);
            when(paragraphBlock.getContext()).thenReturn("paragraph");

            when(mockSection.getBlocks()).thenReturn(Arrays.asList(tableBlock, paragraphBlock));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            // Should succeed - no specific validation rules for these blocks
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("should handle sections with mixed block types")
        void shouldHandleSectionsWithMixedBlockTypes() {
            // Given
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().severity(Severity.ERROR).build();
            TableBlock tableConfig = TableBlock.builder().severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Mixed Content")
                    .allowedBlocks(Arrays.asList(paragraphConfig, tableConfig)).build();

            // Mix of configured and unconfigured block types
            Block para1 = mock(Block.class);
            Block table1 = mock(Block.class);
            Block listing1 = mock(Block.class); // Not configured
            Block para2 = mock(Block.class);

            when(para1.getContext()).thenReturn("paragraph");
            when(table1.getContext()).thenReturn("table");
            when(listing1.getContext()).thenReturn("listing");
            when(para2.getContext()).thenReturn("paragraph");

            when(mockSection.getBlocks()).thenReturn(Arrays.asList(para1, table1, listing1, para2));

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            // The listing block is not allowed because it's not in the allowed blocks list
            assertTrue(result.hasErrors());
            assertEquals(1, result.getErrorCount()); // Only the listing block should error
            assertTrue(
                    result.getMessages().stream().anyMatch(msg -> msg.getMessage().contains("Block type not allowed")));
        }

        @Test
        @DisplayName("should handle null section config gracefully")
        void shouldHandleNullSectionConfigGracefully() {
            // Given
            Block block = mock(Block.class);
            when(block.getContext()).thenReturn("paragraph");
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block));

            // When/Then
            assertThrows(NullPointerException.class, () -> validator.validate(mockSection, null, "test.adoc"));
        }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle validation exceptions gracefully")
        void shouldHandleValidationExceptionsGracefully() {
            // Given
            ParagraphBlock paragraphConfig = ParagraphBlock.builder().severity(Severity.ERROR).build();

            SectionConfig config = SectionConfig.builder().name("Section").allowedBlocks(Arrays.asList(paragraphConfig))
                    .build();

            Block block = mock(Block.class);
            when(block.getContext()).thenThrow(new RuntimeException("Test exception"));
            when(mockSection.getBlocks()).thenReturn(Arrays.asList(block));

            // When/Then - should not throw
            assertDoesNotThrow(() -> {
                ValidationResult result = validator.validate(mockSection, config, "test.adoc");
                assertTrue(result.hasErrors());
            });
        }

        @Test
        @DisplayName("should handle null blocks list")
        void shouldHandleNullBlocksList() {
            // Given
            SectionConfig config = SectionConfig.builder().name("Section")
                    .allowedBlocks(Arrays.asList(ParagraphBlock.builder().severity(Severity.ERROR).build())).build();

            when(mockSection.getBlocks()).thenReturn(null);

            // When
            ValidationResult result = validator.validate(mockSection, config, "test.adoc");

            // Then
            assertFalse(result.hasErrors());
        }
    }
}
