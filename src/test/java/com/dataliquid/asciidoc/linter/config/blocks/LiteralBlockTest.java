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
            TitleConfig titleConfig = TitleConfig.builder().required(false).minLength(5).maxLength(50)
                    .severity(Severity.INFO).build();

            LinesConfig linesConfig = LinesConfig.builder().min(1).max(50).severity(Severity.WARN).build();

            IndentationConfig indentationConfig = IndentationConfig.builder().required(false).consistent(true)
                    .minSpaces(0).maxSpaces(8).severity(Severity.INFO).build();

            // When
            LiteralBlock block = LiteralBlock.builder().name("Config Example").severity(Severity.INFO)
                    .title(titleConfig).lines(linesConfig).indentation(indentationConfig).build();

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
            assertThrows(NullPointerException.class, () -> LiteralBlock.builder().name("Invalid Block").build());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TitleConfig titleConfig = TitleConfig.builder().required(true).severity(Severity.INFO).build();

            LiteralBlock block1 = LiteralBlock.builder().severity(Severity.WARN).title(titleConfig).build();

            LiteralBlock block2 = LiteralBlock.builder().severity(Severity.WARN).title(titleConfig).build();

            LiteralBlock block3 = LiteralBlock.builder().severity(Severity.ERROR).title(titleConfig).build();

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
            TitleConfig config = TitleConfig.builder().required(false).minLength(5).maxLength(50)
                    .severity(Severity.INFO).build();

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
            TitleConfig config = TitleConfig.builder().required(true).severity(Severity.ERROR).build();

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
            TitleConfig config1 = TitleConfig.builder().required(false).minLength(10).maxLength(100)
                    .severity(Severity.WARN).build();

            TitleConfig config2 = TitleConfig.builder().required(false).minLength(10).maxLength(100)
                    .severity(Severity.WARN).build();

            TitleConfig config3 = TitleConfig.builder().required(true).minLength(10).maxLength(100)
                    .severity(Severity.WARN).build();

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
            LinesConfig config = LinesConfig.builder().min(1).max(50).severity(Severity.WARN).build();

            // Then
            assertEquals(1, config.getMin());
            assertEquals(50, config.getMax());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        @DisplayName("should handle optional fields")
        void shouldHandleOptionalFields() {
            // When
            LinesConfig config = LinesConfig.builder().severity(Severity.INFO).build();

            // Then
            assertNull(config.getMin());
            assertNull(config.getMax());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            LinesConfig config1 = LinesConfig.builder().min(5).max(100).severity(Severity.ERROR).build();

            LinesConfig config2 = LinesConfig.builder().min(5).max(100).severity(Severity.ERROR).build();

            LinesConfig config3 = LinesConfig.builder().min(10).max(100).severity(Severity.ERROR).build();

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
            IndentationConfig config = IndentationConfig.builder().required(false).consistent(true).minSpaces(0)
                    .maxSpaces(8).severity(Severity.INFO).build();

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
            IndentationConfig config = IndentationConfig.builder().severity(Severity.WARN).build();

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
            IndentationConfig config1 = IndentationConfig.builder().required(true).consistent(true).minSpaces(2)
                    .maxSpaces(4).severity(Severity.ERROR).build();

            IndentationConfig config2 = IndentationConfig.builder().required(true).consistent(true).minSpaces(2)
                    .maxSpaces(4).severity(Severity.ERROR).build();

            IndentationConfig config3 = IndentationConfig.builder().required(true).consistent(false).minSpaces(2)
                    .maxSpaces(4).severity(Severity.ERROR).build();

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
            OccurrenceConfig occurrence = OccurrenceConfig.builder().min(0).max(3).severity(Severity.INFO).build();

            // When
            LiteralBlock block = LiteralBlock.builder().severity(Severity.INFO).occurrence(occurrence).build();

            // Then
            assertEquals(occurrence, block.getOccurrence());
        }

        @Test
        @DisplayName("should support full configuration")
        void shouldSupportFullConfiguration() {
            // Given
            TitleConfig titleConfig = TitleConfig.builder().required(false).minLength(5).maxLength(50)
                    .severity(Severity.INFO).build();

            LinesConfig linesConfig = LinesConfig.builder().min(1).max(50).severity(Severity.WARN).build();

            IndentationConfig indentationConfig = IndentationConfig.builder().required(false).consistent(true)
                    .minSpaces(0).maxSpaces(8).severity(Severity.INFO).build();

            OccurrenceConfig occurrence = OccurrenceConfig.builder().min(0).max(3).severity(Severity.INFO).build();

            // When
            LiteralBlock block = LiteralBlock.builder().name("Literal Block").severity(Severity.INFO)
                    .occurrence(occurrence).title(titleConfig).lines(linesConfig).indentation(indentationConfig)
                    .build();

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
