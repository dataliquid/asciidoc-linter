package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.ContentConfig;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.ReasonConfig;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.TypeConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

class PassBlockTest {

    @Nested
    @DisplayName("PassBlock")
    class PassBlockTests {

        @Test
        @DisplayName("should create PassBlock with builder")
        void shouldCreatePassBlockWithBuilder() {
            // Given
            TypeConfig typeConfig = new TypeConfig(true, Arrays.asList("html", "xml", "svg"), Severity.ERROR);

            ContentConfig contentConfig = new ContentConfig(true, 1000, "^<[^>]+>.*</[^>]+>$", Severity.ERROR);

            ReasonConfig reasonConfig = new ReasonConfig(true, 20, 200, Severity.ERROR);

            // When
            PassBlock block = new PassBlock("Custom HTML Pass", Severity.ERROR, null, null, typeConfig, contentConfig,
                    reasonConfig);

            // Then
            assertNotNull(block);
            assertEquals("Custom HTML Pass", block.getName());
            assertEquals(Severity.ERROR, block.getSeverity());
            assertEquals(BlockType.PASS, block.getType());
            assertEquals(typeConfig, block.getTypeConfig());
            assertEquals(contentConfig, block.getContent());
            assertEquals(reasonConfig, block.getReason());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            assertThrows(NullPointerException.class,
                    () -> new PassBlock("Invalid Block", null, null, null, null, null, null));
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TypeConfig typeConfig = new TypeConfig(true, null, Severity.ERROR);

            PassBlock block1 = new PassBlock(null, Severity.WARN, null, null, typeConfig, null, null);

            PassBlock block2 = new PassBlock(null, Severity.WARN, null, null, typeConfig, null, null);

            PassBlock block3 = new PassBlock(null, Severity.ERROR, null, null, typeConfig, null, null);

            // Then
            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1, block3);
        }
    }

    @Nested
    @DisplayName("TypeConfig")
    class TypeConfigTests {

        @Test
        @DisplayName("should create TypeConfig with builder")
        void shouldCreateTypeConfigWithBuilder() {
            // Given
            List<String> allowed = Arrays.asList("html", "xml", "svg");

            // When
            TypeConfig config = new TypeConfig(true, allowed, Severity.ERROR);

            // Then
            assertTrue(config.isRequired());
            assertEquals(allowed, config.getAllowed());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should handle empty allowed list")
        void shouldHandleEmptyAllowedList() {
            // When
            TypeConfig config = new TypeConfig(false, null, Severity.WARN);

            // Then
            assertFalse(config.isRequired());
            assertTrue(config.getAllowed().isEmpty());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        @DisplayName("should make allowed list immutable")
        void shouldMakeAllowedListImmutable() {
            // Given
            List<String> allowed = Arrays.asList("html", "xml");
            TypeConfig config = new TypeConfig(false, allowed, Severity.INFO);

            // Then
            assertThrows(UnsupportedOperationException.class, () -> config.getAllowed().add("svg"));
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TypeConfig config1 = new TypeConfig(true, Arrays.asList("html", "xml"), Severity.ERROR);

            TypeConfig config2 = new TypeConfig(true, Arrays.asList("html", "xml"), Severity.ERROR);

            TypeConfig config3 = new TypeConfig(false, Arrays.asList("html", "xml"), Severity.ERROR);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    @DisplayName("ContentConfig")
    class ContentConfigTests {

        @Test
        @DisplayName("should create ContentConfig with builder")
        void shouldCreateContentConfigWithBuilder() {
            // Given
            Pattern pattern = Pattern.compile("^<[^>]+>.*</[^>]+>$");

            // When
            ContentConfig config = new ContentConfig(true, 1000, pattern.pattern(), Severity.ERROR);

            // Then
            assertTrue(config.isRequired());
            assertEquals(1000, config.getMaxLength());
            assertEquals(pattern.pattern(), config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should accept pattern as string")
        void shouldAcceptPatternAsString() {
            // When
            ContentConfig config = new ContentConfig(false, null, "^<div.*>.*</div>$", Severity.WARN);

            // Then
            assertNotNull(config.getPattern());
            assertEquals("^<div.*>.*</div>$", config.getPattern().pattern());
        }

        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // When
            ContentConfig config = new ContentConfig(false, 500, null, Severity.INFO);

            // Then
            assertNull(config.getPattern());
        }

        @Test
        @DisplayName("should support equals and hashCode with pattern")
        void shouldSupportEqualsAndHashCodeWithPattern() {
            // Given
            ContentConfig config1 = new ContentConfig(true, 1000, "^<[^>]+>.*$", Severity.ERROR);

            ContentConfig config2 = new ContentConfig(true, 1000, "^<[^>]+>.*$", Severity.ERROR);

            ContentConfig config3 = new ContentConfig(true, 1000, "^<div>.*$", Severity.ERROR);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    @DisplayName("ReasonConfig")
    class ReasonConfigTests {

        @Test
        @DisplayName("should create ReasonConfig with builder")
        void shouldCreateReasonConfigWithBuilder() {
            // When
            ReasonConfig config = new ReasonConfig(true, 20, 200, Severity.ERROR);

            // Then
            assertTrue(config.isRequired());
            assertEquals(20, config.getMinLength());
            assertEquals(200, config.getMaxLength());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should handle optional lengths")
        void shouldHandleOptionalLengths() {
            // When
            ReasonConfig config = new ReasonConfig(false, null, null, Severity.WARN);

            // Then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            ReasonConfig config1 = new ReasonConfig(true, 10, 100, Severity.ERROR);

            ReasonConfig config2 = new ReasonConfig(true, 10, 100, Severity.ERROR);

            ReasonConfig config3 = new ReasonConfig(true, 20, 100, Severity.ERROR);

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
            OccurrenceConfig occurrence = new OccurrenceConfig(null, 0, 1, Severity.ERROR);

            // When
            PassBlock block = new PassBlock(null, Severity.ERROR, occurrence, null, null, null, null);

            // Then
            assertEquals(occurrence, block.getOccurrence());
        }

        @Test
        @DisplayName("should support full configuration")
        void shouldSupportFullConfiguration() {
            // Given
            TypeConfig typeConfig = new TypeConfig(true, Arrays.asList("html", "xml", "svg"), Severity.ERROR);

            ContentConfig contentConfig = new ContentConfig(true, 1000, "^<[^>]+>.*</[^>]+>$", Severity.ERROR);

            ReasonConfig reasonConfig = new ReasonConfig(true, 20, 200, Severity.ERROR);

            OccurrenceConfig occurrence = new OccurrenceConfig(null, 0, 1, Severity.ERROR);

            // When
            PassBlock block = new PassBlock("Passthrough Block", Severity.ERROR, occurrence, null, typeConfig,
                    contentConfig, reasonConfig);

            // Then
            assertEquals("Passthrough Block", block.getName());
            assertEquals(Severity.ERROR, block.getSeverity());
            assertEquals(BlockType.PASS, block.getType());
            assertEquals(occurrence, block.getOccurrence());
            assertEquals(typeConfig, block.getTypeConfig());
            assertEquals(contentConfig, block.getContent());
            assertEquals(reasonConfig, block.getReason());
        }
    }
}
