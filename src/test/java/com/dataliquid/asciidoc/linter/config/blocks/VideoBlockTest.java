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

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

class VideoBlockTest {

    @Nested
    class BuilderTests {

        @Test
        void shouldBuildWithMinimalRequiredFields() {
            // given
            String name = "test-video";
            Severity severity = Severity.WARN;

            // when
            VideoBlock block = new VideoBlock(name, severity, null, null, null, null, null, null, null, null);

            // then
            assertNotNull(block);
            assertEquals("test-video", block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
            assertNull(block.getUrl());
            assertNull(block.getWidth());
            assertNull(block.getHeight());
            assertNull(block.getPoster());
            assertNull(block.getOptions());
            assertNull(block.getCaption());
        }

        @Test
        void shouldBuildWithAllFields() {
            // given
            String name = "full-video";
            Severity severity = Severity.ERROR;
            OccurrenceConfig occurrence = new OccurrenceConfig(null, 1, 3, null);
            VideoBlock.UrlConfig url = new VideoBlock.UrlConfig(true, "^https?://.*\\.(mp4|webm)$", Severity.ERROR);
            VideoBlock.DimensionConfig width = new VideoBlock.DimensionConfig(null, 320, 1920, Severity.INFO);
            VideoBlock.DimensionConfig height = new VideoBlock.DimensionConfig(null, 180, 1080, Severity.INFO);
            VideoBlock.PosterConfig poster = new VideoBlock.PosterConfig(null, ".*\\.(jpg|jpeg|png)$", null);
            VideoBlock.ControlsConfig controls = new VideoBlock.ControlsConfig(true, Severity.ERROR);
            VideoBlock.OptionsConfig options = new VideoBlock.OptionsConfig(controls);
            VideoBlock.CaptionConfig caption = new VideoBlock.CaptionConfig(true, 15, 200, Severity.WARN);

            // when
            VideoBlock block = new VideoBlock(name, severity, occurrence, null, url, width, height, poster, options,
                    caption);

            // then
            assertNotNull(block);
            assertNotNull(block.getUrl());
            assertNotNull(block.getWidth());
            assertNotNull(block.getHeight());
            assertNotNull(block.getPoster());
            assertNotNull(block.getOptions());
            assertNotNull(block.getCaption());
        }

        @Test
        void shouldBuildSuccessfullyWithoutName() {
            // when
            VideoBlock block = new VideoBlock(null, Severity.WARN, null, null, null, null, null, null, null, null);

            // then
            assertNull(block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
        }

        @Test
        void shouldHandleNullSeverity() {
            // when & then - no longer throws, just accepts null
            VideoBlock block = new VideoBlock("test", null, null, null, null, null, null, null, null, null);
            assertEquals("test", block.getName());
            assertNull(block.getSeverity());
        }
    }

    @Nested
    class UrlConfigTests {

        @Test
        void shouldBuildUrlConfigWithAllFields() {
            // when
            VideoBlock.UrlConfig config = new VideoBlock.UrlConfig(true, "^https?://.*\\.(mp4|webm)$", Severity.ERROR);

            // then
            assertTrue(config.getRequired());
            assertNotNull(config.getPattern());
            assertEquals("^https?://.*\\.(mp4|webm)$", config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        void shouldBuildWithPatternString() {
            // given
            String patternString = "test.*";

            // when
            VideoBlock.UrlConfig config = new VideoBlock.UrlConfig(null, patternString, null);

            // then
            assertNotNull(config.getPattern());
            assertEquals("test.*", config.getPattern().pattern());
        }

        @Test
        void shouldHandleNullPatternString() {
            // when
            VideoBlock.UrlConfig config = new VideoBlock.UrlConfig(null, null, null);

            // then
            assertNull(config.getPattern());
        }
    }

    @Nested
    class DimensionConfigTests {

        @Test
        void shouldBuildDimensionConfigWithAllFields() {
            // when
            VideoBlock.DimensionConfig config = new VideoBlock.DimensionConfig(false, 320, 1920, Severity.INFO);

            // then
            assertFalse(config.getRequired());
            assertEquals(320, config.getMinValue());
            assertEquals(1920, config.getMaxValue());
            assertEquals(Severity.INFO, config.getSeverity());
        }
    }

    @Nested
    class CaptionConfigTests {

        @Test
        void shouldBuildCaptionConfigWithAllFields() {
            // when
            VideoBlock.CaptionConfig config = new VideoBlock.CaptionConfig(true, 15, 200, Severity.WARN);

            // then
            assertTrue(config.getRequired());
            assertEquals(15, config.getMinLength());
            assertEquals(200, config.getMaxLength());
            assertEquals(Severity.WARN, config.getSeverity());
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldBeEqualForSameValues() {
            // given
            VideoBlock.UrlConfig url1 = new VideoBlock.UrlConfig(true, "test.*", null);
            VideoBlock.UrlConfig url2 = new VideoBlock.UrlConfig(true, "test.*", null);

            // when
            VideoBlock block1 = new VideoBlock("test", Severity.WARN, null, null, url1, null, null, null, null, null);
            VideoBlock block2 = new VideoBlock("test", Severity.WARN, null, null, url2, null, null, null, null, null);

            // then
            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
        }

        @Test
        void shouldNotBeEqualForDifferentValues() {
            // when
            VideoBlock block1 = new VideoBlock("test1", Severity.WARN, null, null, null, null, null, null, null, null);
            VideoBlock block2 = new VideoBlock("test2", Severity.WARN, null, null, null, null, null, null, null, null);

            // then
            assertNotEquals(block1, block2);
        }

        @Test
        void shouldHandlePatternEqualityCorrectly() {
            // when
            VideoBlock.UrlConfig config1 = new VideoBlock.UrlConfig(null, "test.*", null);
            VideoBlock.UrlConfig config2 = new VideoBlock.UrlConfig(null, "test.*", null);

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
        }

        @Test
        void shouldHandleNullPatternsInEquals() {
            // when
            VideoBlock.UrlConfig config1 = new VideoBlock.UrlConfig(null, null, null);
            VideoBlock.UrlConfig config2 = new VideoBlock.UrlConfig(null, null, null);

            // then
            assertEquals(config1, config2);
        }
    }
}
