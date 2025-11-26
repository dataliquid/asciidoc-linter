package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock.IndentationConfig;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock.LinesConfig;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock.TitleConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

class LiteralBlockTest {

    @Nested
    @DisplayName("LiteralBlock")
    class LiteralBlockTests {

        @Test
        @DisplayName("should create LiteralBlock with builder")
        void shouldCreateLiteralBlockWithBuilder() {
            // Given
            TitleConfig titleConfig = new TitleConfig(false, 5, 50, Severity.INFO);

            LinesConfig linesConfig = new LinesConfig(1, 50, Severity.WARN);

            IndentationConfig indentationConfig = new IndentationConfig(false, true, 0, 8, Severity.INFO);

            // When
            LiteralBlock block = new LiteralBlock("Config Example", Severity.INFO, null, null, titleConfig, linesConfig,
                    indentationConfig);

            // Then
            assertNotNull(block);
            assertEquals("Config Example", block.getName());
            assertEquals(Severity.INFO, block.getSeverity());
            assertEquals(BlockType.LITERAL, block.getType());
            assertEquals(titleConfig, block.getTitle());
            assertEquals(linesConfig, block.getLines());
            assertEquals(indentationConfig, block.getIndentation());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            assertThrows(NullPointerException.class,
                    () -> new LiteralBlock("Invalid Block", null, null, null, null, null, null));
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TitleConfig titleConfig = new TitleConfig(true, null, null, Severity.INFO);

            LiteralBlock block1 = new LiteralBlock(null, Severity.WARN, null, null, titleConfig, null, null);

            LiteralBlock block2 = new LiteralBlock(null, Severity.WARN, null, null, titleConfig, null, null);

            LiteralBlock block3 = new LiteralBlock(null, Severity.ERROR, null, null, titleConfig, null, null);

            // Then
            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1, block3);
        }
    }

    @Nested
    @DisplayName("TitleConfig")
    class TitleConfigTests {

        @Test
        @DisplayName("should create TitleConfig with builder")
        void shouldCreateTitleConfigWithBuilder() {
            // When
            TitleConfig config = new TitleConfig(false, 5, 50, Severity.INFO);

            // Then
            assertFalse(config.isRequired());
            assertEquals(5, config.getMinLength());
            assertEquals(50, config.getMaxLength());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        @DisplayName("should handle optional fields")
        void shouldHandleOptionalFields() {
            // When
            TitleConfig config = new TitleConfig(true, null, null, Severity.ERROR);

            // Then
            assertTrue(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TitleConfig config1 = new TitleConfig(false, 10, 100, Severity.WARN);

            TitleConfig config2 = new TitleConfig(false, 10, 100, Severity.WARN);

            TitleConfig config3 = new TitleConfig(true, 10, 100, Severity.WARN);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    @DisplayName("LinesConfig")
    class LinesConfigTests {

        @Test
        @DisplayName("should create LinesConfig with builder")
        void shouldCreateLinesConfigWithBuilder() {
            // When
            LinesConfig config = new LinesConfig(1, 50, Severity.WARN);

            // Then
            assertEquals(1, config.getMin());
            assertEquals(50, config.getMax());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        @DisplayName("should handle optional fields")
        void shouldHandleOptionalFields() {
            // When
            LinesConfig config = new LinesConfig(null, null, Severity.INFO);

            // Then
            assertNull(config.getMin());
            assertNull(config.getMax());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            LinesConfig config1 = new LinesConfig(5, 100, Severity.ERROR);

            LinesConfig config2 = new LinesConfig(5, 100, Severity.ERROR);

            LinesConfig config3 = new LinesConfig(10, 100, Severity.ERROR);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    @DisplayName("IndentationConfig")
    class IndentationConfigTests {

        @Test
        @DisplayName("should create IndentationConfig with builder")
        void shouldCreateIndentationConfigWithBuilder() {
            // When
            IndentationConfig config = new IndentationConfig(false, true, 0, 8, Severity.INFO);

            // Then
            assertFalse(config.isRequired());
            assertTrue(config.isConsistent());
            assertEquals(0, config.getMinSpaces());
            assertEquals(8, config.getMaxSpaces());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        @DisplayName("should handle default boolean values")
        void shouldHandleDefaultBooleanValues() {
            // When
            IndentationConfig config = new IndentationConfig(false, false, null, null, Severity.WARN);

            // Then
            assertFalse(config.isRequired());
            assertFalse(config.isConsistent());
            assertNull(config.getMinSpaces());
            assertNull(config.getMaxSpaces());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            IndentationConfig config1 = new IndentationConfig(true, true, 2, 4, Severity.ERROR);

            IndentationConfig config2 = new IndentationConfig(true, true, 2, 4, Severity.ERROR);

            IndentationConfig config3 = new IndentationConfig(true, false, 2, 4, Severity.ERROR);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    @DisplayName("Integration with AbstractBlock")
    class IntegrationTests {

        @Test
        @DisplayName("should inherit occurrence from AbstractBlock")
        void shouldInheritOccurrenceFromAbstractBlock() {
            // Given
            OccurrenceConfig occurrence = new OccurrenceConfig(null, 0, 3, Severity.INFO);

            // When
            LiteralBlock block = new LiteralBlock(null, Severity.INFO, occurrence, null, null, null, null);

            // Then
            assertEquals(occurrence, block.getOccurrence());
        }

        @Test
        @DisplayName("should support full configuration")
        void shouldSupportFullConfiguration() {
            // Given
            TitleConfig titleConfig = new TitleConfig(false, 5, 50, Severity.INFO);

            LinesConfig linesConfig = new LinesConfig(1, 50, Severity.WARN);

            IndentationConfig indentationConfig = new IndentationConfig(false, true, 0, 8, Severity.INFO);

            OccurrenceConfig occurrence = new OccurrenceConfig(null, 0, 3, Severity.INFO);

            // When
            LiteralBlock block = new LiteralBlock("Literal Block", Severity.INFO, occurrence, null, titleConfig,
                    linesConfig, indentationConfig);

            // Then
            assertEquals("Literal Block", block.getName());
            assertEquals(Severity.INFO, block.getSeverity());
            assertEquals(BlockType.LITERAL, block.getType());
            assertEquals(occurrence, block.getOccurrence());
            assertEquals(titleConfig, block.getTitle());
            assertEquals(linesConfig, block.getLines());
            assertEquals(indentationConfig, block.getIndentation());
        }
    }
}
