package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;

class AdmonitionBlockTest {

    @Nested
    class ConstructorTests {

        @Test
        void shouldConstructBlockWithAllProperties() {
            // given
            AdmonitionBlock.TypeConfig typeConfig = new AdmonitionBlock.TypeConfig(true,
                    List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION"), Severity.ERROR);

            AdmonitionBlock.TitleConfig titleConfig = new AdmonitionBlock.TitleConfig(true, "^[A-Z].*", 3, 50,
                    Severity.ERROR);

            AdmonitionBlock.ContentConfig contentConfig = new AdmonitionBlock.ContentConfig(true, 10, 500, null,
                    Severity.WARN);

            AdmonitionBlock.IconConfig iconConfig = new AdmonitionBlock.IconConfig(false, "^(fa-|icon-|octicon-).*$",
                    Severity.INFO);

            // when
            AdmonitionBlock block = new AdmonitionBlock(null, Severity.ERROR, null, null, typeConfig, titleConfig,
                    contentConfig, iconConfig);

            // then
            assertNotNull(block);
            assertEquals(Severity.ERROR, block.getSeverity());
            assertEquals(typeConfig, block.getTypeConfig());
            assertEquals(titleConfig, block.getTitle());
            assertEquals(contentConfig, block.getContent());
            assertEquals(iconConfig, block.getIcon());
        }

        @Test
        void shouldConstructBlockWithMinimalProperties() {
            // when
            AdmonitionBlock block = new AdmonitionBlock(null, Severity.ERROR, null, null, null, null, null, null);

            // then
            assertNotNull(block);
            assertEquals(Severity.ERROR, block.getSeverity());
            assertNull(block.getTypeConfig());
            assertNull(block.getTitle());
            assertNull(block.getContent());
            assertNull(block.getIcon());
        }
    }

    @Nested
    class TypeConfigTests {

        @Test
        void shouldConstructTypeConfigWithAllProperties() {
            // given
            List<String> allowedTypes = List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION");

            // when
            AdmonitionBlock.TypeConfig config = new AdmonitionBlock.TypeConfig(true, allowedTypes, Severity.ERROR);

            // then
            assertTrue(config.isRequired());
            assertEquals(allowedTypes, config.getAllowed());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        void shouldHandleEmptyAllowedList() {
            // when
            AdmonitionBlock.TypeConfig config = new AdmonitionBlock.TypeConfig(false, null, null);

            // then
            assertFalse(config.isRequired());
            assertNotNull(config.getAllowed());
            assertTrue(config.getAllowed().isEmpty());
        }
    }

    @Nested
    class TitleConfigTests {

        @Test
        void shouldConstructTitleConfigWithAllProperties() {
            // given
            String patternStr = "^[A-Z][A-Za-z\\s]{2,49}$";

            // when
            AdmonitionBlock.TitleConfig config = new AdmonitionBlock.TitleConfig(true, patternStr, 3, 50,
                    Severity.ERROR);

            // then
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals(patternStr, config.getPattern().pattern());
            assertEquals(3, config.getMinLength());
            assertEquals(50, config.getMaxLength());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        void shouldHandleNullPattern() {
            // when
            AdmonitionBlock.TitleConfig config = new AdmonitionBlock.TitleConfig(false, null, null, null, null);

            // then
            assertFalse(config.isRequired());
            assertNull(config.getPattern());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getSeverity());
        }
    }

    @Nested
    class ContentConfigTests {

        @Test
        void shouldConstructContentConfigWithAllProperties() {
            // given
            LineConfig lineConfig = new LineConfig(1, 10, Severity.INFO);

            // when
            AdmonitionBlock.ContentConfig config = new AdmonitionBlock.ContentConfig(true, 10, 500, lineConfig,
                    Severity.WARN);

            // then
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(500, config.getMaxLength());
            assertNotNull(config.getLines());
            assertEquals(Severity.WARN, config.getSeverity());
        }

        @Test
        void shouldConstructMinimalContentConfig() {
            // when
            AdmonitionBlock.ContentConfig config = new AdmonitionBlock.ContentConfig(false, null, null, null, null);

            // then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getLines());
            assertNull(config.getSeverity());
        }
    }

    @Nested
    class IconConfigTests {

        @Test
        void shouldConstructIconConfigWithAllProperties() {
            // when
            AdmonitionBlock.IconConfig config = new AdmonitionBlock.IconConfig(false, "^(fa-|icon-|octicon-).*$",
                    Severity.INFO);

            // then
            assertFalse(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals("^(fa-|icon-|octicon-).*$", config.getPattern().pattern());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        void shouldHandleNullPattern() {
            // when
            AdmonitionBlock.IconConfig config = new AdmonitionBlock.IconConfig(true, null, null);

            // then
            assertTrue(config.isRequired());
            assertNull(config.getPattern());
            assertNull(config.getSeverity());
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldImplementEqualsAndHashCodeCorrectly() {
            // given
            AdmonitionBlock.TitleConfig title1 = new AdmonitionBlock.TitleConfig(true, "^[A-Z].*", 3, 50,
                    Severity.ERROR);

            AdmonitionBlock.TitleConfig title2 = new AdmonitionBlock.TitleConfig(true, "^[A-Z].*", 3, 50,
                    Severity.ERROR);

            // when
            AdmonitionBlock block1 = new AdmonitionBlock(null, Severity.ERROR, null, null, null, title1, null, null);

            AdmonitionBlock block2 = new AdmonitionBlock(null, Severity.ERROR, null, null, null, title2, null, null);

            AdmonitionBlock block3 = new AdmonitionBlock(null, Severity.WARN, null, null, null, title1, null, null);

            // then
            assertEquals(block1, block2);
            assertNotEquals(block1, block3);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1.hashCode(), block3.hashCode());
        }

        @Test
        void shouldHandlePatternEqualityCorrectly() {
            // given
            AdmonitionBlock.TitleConfig config1 = new AdmonitionBlock.TitleConfig(false, "^[A-Z].*", null, null, null);

            AdmonitionBlock.TitleConfig config2 = new AdmonitionBlock.TitleConfig(false, "^[A-Z].*", null, null, null);

            AdmonitionBlock.TitleConfig config3 = new AdmonitionBlock.TitleConfig(false, "^[a-z].*", null, null, null);

            // then
            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
            assertEquals(config1.hashCode(), config2.hashCode());
        }
    }
}
