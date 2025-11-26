package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

class QuoteBlockTest {

    @Test
    void shouldReturnCorrectBlockType() {
        // when
        QuoteBlock block = new QuoteBlock(null, Severity.INFO, null, null, null, null, null);

        // then
        assertEquals(BlockType.QUOTE, block.getType());
    }

    @Nested
    class BuilderTests {

        @Test
        void shouldBuildWithMinimalConfig() {
            // when
            QuoteBlock block = new QuoteBlock(null, Severity.WARN, null, null, null, null, null);

            // then
            assertNotNull(block);
            assertEquals(Severity.WARN, block.getSeverity());
            assertNull(block.getAttribution());
            assertNull(block.getCitation());
            assertNull(block.getContent());
        }

        @Test
        void shouldBuildWithCompleteConfig() {
            // given
            QuoteBlock.AttributionConfig attribution = new QuoteBlock.AttributionConfig(true, // required
                    3, // minLength
                    100, // maxLength
                    "^[A-Z][a-zA-Z\\s\\.\\-,]+$", // pattern
                    Severity.ERROR); // severity

            QuoteBlock.CitationConfig citation = new QuoteBlock.CitationConfig(false, // required
                    5, // minLength
                    200, // maxLength
                    "^[A-Za-z0-9\\s,\\.\\-\\(\\)]+$", // pattern
                    Severity.WARN); // severity

            QuoteBlock.LinesConfig lines = new QuoteBlock.LinesConfig(1, // min
                    20, // max
                    Severity.INFO); // severity

            QuoteBlock.ContentConfig content = new QuoteBlock.ContentConfig(true, // required
                    20, // minLength
                    1000, // maxLength
                    lines); // lines

            // when
            QuoteBlock block = new QuoteBlock("important-quote", // name
                    Severity.INFO, // severity
                    new OccurrenceConfig(null, 0, 3, null), // occurrence
                    null, // order
                    attribution, // attribution
                    citation, // citation
                    content); // content

            // then
            assertNotNull(block);
            assertEquals("important-quote", block.getName());
            assertEquals(Severity.INFO, block.getSeverity());
            assertNotNull(block.getOccurrence());
            assertEquals(0, block.getOccurrence().min());
            assertEquals(3, block.getOccurrence().max());
            assertNotNull(block.getAttribution());
            assertTrue(block.getAttribution().isRequired());
            assertNotNull(block.getCitation());
            assertFalse(block.getCitation().isRequired());
            assertNotNull(block.getContent());
            assertTrue(block.getContent().isRequired());
        }

        @Test
        void shouldThrowWhenSeverityNull() {
            // when & then
            assertThrows(NullPointerException.class, () -> new QuoteBlock(null, null, null, null, null, null, null),
                    "severity is required");
        }
    }

    @Nested
    class AttributionConfigTests {

        @Test
        void shouldBuildWithDefaults() {
            // when
            QuoteBlock.AttributionConfig config = new QuoteBlock.AttributionConfig(false, // required (default)
                    null, // minLength
                    null, // maxLength
                    null, // pattern
                    null); // severity

            // then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getPattern());
            assertNull(config.getSeverity());
        }

        @Test
        void shouldBuildWithAllValues() {
            // when
            QuoteBlock.AttributionConfig config = new QuoteBlock.AttributionConfig(true, // required
                    5, // minLength
                    50, // maxLength
                    "^[A-Z].*", // pattern
                    Severity.ERROR); // severity

            // then
            assertTrue(config.isRequired());
            assertEquals(5, config.getMinLength());
            assertEquals(50, config.getMaxLength());
            assertNotNull(config.getPattern());
            assertEquals("^[A-Z].*", config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            QuoteBlock.AttributionConfig config1 = new QuoteBlock.AttributionConfig(true, // required
                    5, // minLength
                    null, // maxLength
                    "^[A-Z].*", // pattern
                    null); // severity

            QuoteBlock.AttributionConfig config2 = new QuoteBlock.AttributionConfig(true, // required
                    5, // minLength
                    null, // maxLength
                    "^[A-Z].*", // pattern
                    null); // severity

            QuoteBlock.AttributionConfig config3 = new QuoteBlock.AttributionConfig(false, // required
                    5, // minLength
                    null, // maxLength
                    "^[A-Z].*", // pattern
                    null); // severity

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class CitationConfigTests {

        @Test
        void shouldBuildWithDefaults() {
            // when
            QuoteBlock.CitationConfig config = new QuoteBlock.CitationConfig(false, // required (default)
                    null, // minLength
                    null, // maxLength
                    null, // pattern
                    null); // severity

            // then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getPattern());
            assertNull(config.getSeverity());
        }

        @Test
        void shouldBuildWithAllValues() {
            // when
            QuoteBlock.CitationConfig config = new QuoteBlock.CitationConfig(true, // required
                    10, // minLength
                    100, // maxLength
                    "^[A-Za-z0-9\\\\s]+$", // pattern
                    Severity.WARN); // severity

            // then
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(100, config.getMaxLength());
            assertNotNull(config.getPattern());
            assertEquals("^[A-Za-z0-9\\\\s]+$", config.getPattern().pattern());
            assertEquals(Severity.WARN, config.getSeverity());
        }
    }

    @Nested
    class ContentConfigTests {

        @Test
        void shouldBuildWithDefaults() {
            // when
            QuoteBlock.ContentConfig config = new QuoteBlock.ContentConfig(false, // required (default)
                    null, // minLength
                    null, // maxLength
                    null); // lines

            // then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getLines());
        }

        @Test
        void shouldBuildWithAllValues() {
            // given
            QuoteBlock.LinesConfig lines = new QuoteBlock.LinesConfig(2, // min
                    10, // max
                    Severity.INFO); // severity

            // when
            QuoteBlock.ContentConfig config = new QuoteBlock.ContentConfig(true, // required
                    50, // minLength
                    500, // maxLength
                    lines); // lines

            // then
            assertTrue(config.isRequired());
            assertEquals(50, config.getMinLength());
            assertEquals(500, config.getMaxLength());
            assertNotNull(config.getLines());
            assertEquals(2, config.getLines().getMin());
            assertEquals(10, config.getLines().getMax());
        }
    }

    @Nested
    class LinesConfigTests {

        @Test
        void shouldBuildWithDefaults() {
            // when
            QuoteBlock.LinesConfig config = new QuoteBlock.LinesConfig(null, // min
                    null, // max
                    null); // severity

            // then
            assertNull(config.getMin());
            assertNull(config.getMax());
            assertNull(config.getSeverity());
        }

        @Test
        void shouldBuildWithAllValues() {
            // when
            QuoteBlock.LinesConfig config = new QuoteBlock.LinesConfig(1, // min
                    20, // max
                    Severity.WARN); // severity

            // then
            assertEquals(1, config.getMin());
            assertEquals(20, config.getMax());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            QuoteBlock.LinesConfig config1 = new QuoteBlock.LinesConfig(5, // min
                    10, // max
                    Severity.INFO); // severity

            QuoteBlock.LinesConfig config2 = new QuoteBlock.LinesConfig(5, // min
                    10, // max
                    Severity.INFO); // severity

            QuoteBlock.LinesConfig config3 = new QuoteBlock.LinesConfig(5, // min
                    15, // max
                    Severity.INFO); // severity

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldImplementEquals() {
            // given
            QuoteBlock.AttributionConfig attribution = new QuoteBlock.AttributionConfig(true, null, null, null, null);

            // when
            QuoteBlock block1 = new QuoteBlock("quote1", // name
                    Severity.WARN, // severity
                    null, // occurrence
                    null, // order
                    attribution, // attribution
                    null, // citation
                    null); // content

            QuoteBlock block2 = new QuoteBlock("quote1", // name
                    Severity.WARN, // severity
                    null, // occurrence
                    null, // order
                    attribution, // attribution
                    null, // citation
                    null); // content

            QuoteBlock block3 = new QuoteBlock("quote2", // name
                    Severity.WARN, // severity
                    null, // occurrence
                    null, // order
                    attribution, // attribution
                    null, // citation
                    null); // content

            // then
            assertEquals(block1, block2);
            assertNotEquals(block1, block3);
            assertNotEquals(block1, null);
            assertNotEquals(block1, new Object());
        }

        @Test
        void shouldImplementHashCode() {
            // given
            QuoteBlock.CitationConfig citation = new QuoteBlock.CitationConfig(false, null, 100, null, null);

            // when
            QuoteBlock block1 = new QuoteBlock(null, // name
                    Severity.ERROR, // severity
                    null, // occurrence
                    null, // order
                    null, // attribution
                    citation, // citation
                    null); // content

            QuoteBlock block2 = new QuoteBlock(null, // name
                    Severity.ERROR, // severity
                    null, // occurrence
                    null, // order
                    null, // attribution
                    citation, // citation
                    null); // content

            // then
            assertEquals(block1.hashCode(), block2.hashCode());
        }
    }
}
