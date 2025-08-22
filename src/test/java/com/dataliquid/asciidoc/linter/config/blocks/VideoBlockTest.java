package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

@DisplayName("VideoBlock")
class VideoBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build with minimal required fields")
        void shouldBuildWithMinimalRequiredFields() {
            VideoBlock block = VideoBlock.builder().name("test-video").severity(Severity.WARN).build();

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
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            VideoBlock block = VideoBlock.builder().name("full-video").severity(Severity.ERROR)
                    .occurrence(OccurrenceConfig.builder().min(1).max(3).build())
                    .url(VideoBlock.UrlConfig.builder().required(true).pattern("^https?://.*\\.(mp4|webm)$")
                            .severity(Severity.ERROR).build())
                    .width(VideoBlock.DimensionConfig.builder().minValue(320).maxValue(1920).severity(Severity.INFO)
                            .build())
                    .height(VideoBlock.DimensionConfig
                            .builder().minValue(180).maxValue(1080).severity(Severity.INFO).build())
                    .poster(VideoBlock.PosterConfig.builder().pattern(".*\\.(jpg|jpeg|png)$").build())
                    .options(VideoBlock.OptionsConfig.builder()
                            .controls(
                                    VideoBlock.ControlsConfig.builder().required(true).severity(Severity.ERROR).build())
                            .build())
                    .caption(VideoBlock.CaptionConfig.builder().required(true).minLength(15).maxLength(200)
                            .severity(Severity.WARN).build())
                    .build();

            assertNotNull(block);
            assertNotNull(block.getUrl());
            assertNotNull(block.getWidth());
            assertNotNull(block.getHeight());
            assertNotNull(block.getPoster());
            assertNotNull(block.getOptions());
            assertNotNull(block.getCaption());
        }

        @Test
        @DisplayName("should build successfully without name")
        void shouldBuildSuccessfullyWithoutName() {
            VideoBlock block = VideoBlock.builder().severity(Severity.WARN).build();

            assertNull(block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
        }

        @Test
        @DisplayName("should throw when severity is null")
        void shouldThrowWhenSeverityIsNull() {
            assertThrows(NullPointerException.class, () -> VideoBlock.builder().name("test").build());
        }
    }

    @Nested
    @DisplayName("UrlConfig Tests")
    class UrlConfigTests {

        @Test
        @DisplayName("should build url config with all fields")
        void shouldBuildUrlConfigWithAllFields() {
            VideoBlock.UrlConfig config = VideoBlock.UrlConfig.builder().required(true)
                    .pattern("^https?://.*\\.(mp4|webm)$").severity(Severity.ERROR).build();

            assertTrue(config.getRequired());
            assertNotNull(config.getPattern());
            assertEquals("^https?://.*\\.(mp4|webm)$", config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should build with Pattern object")
        void shouldBuildWithPatternObject() {
            Pattern pattern = Pattern.compile("test.*");
            VideoBlock.UrlConfig config = VideoBlock.UrlConfig.builder().pattern(pattern).build();

            assertEquals(pattern, config.getPattern());
        }

        @Test
        @DisplayName("should handle null pattern string")
        void shouldHandleNullPatternString() {
            VideoBlock.UrlConfig config = VideoBlock.UrlConfig.builder().pattern((String) null).build();

            assertNull(config.getPattern());
        }
    }

    @Nested
    @DisplayName("DimensionConfig Tests")
    class DimensionConfigTests {

        @Test
        @DisplayName("should build dimension config with all fields")
        void shouldBuildDimensionConfigWithAllFields() {
            VideoBlock.DimensionConfig config = VideoBlock.DimensionConfig.builder().required(false).minValue(320)
                    .maxValue(1920).severity(Severity.INFO).build();

            assertFalse(config.getRequired());
            assertEquals(320, config.getMinValue());
            assertEquals(1920, config.getMaxValue());
            assertEquals(Severity.INFO, config.getSeverity());
        }
    }

    @Nested
    @DisplayName("CaptionConfig Tests")
    class CaptionConfigTests {

        @Test
        @DisplayName("should build caption config with all fields")
        void shouldBuildCaptionConfigWithAllFields() {
            VideoBlock.CaptionConfig config = VideoBlock.CaptionConfig.builder().required(true).minLength(15)
                    .maxLength(200).severity(Severity.WARN).build();

            assertTrue(config.getRequired());
            assertEquals(15, config.getMinLength());
            assertEquals(200, config.getMaxLength());
            assertEquals(Severity.WARN, config.getSeverity());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualForSameValues() {
            VideoBlock block1 = VideoBlock.builder().name("test").severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder().required(true).pattern("test.*").build()).build();

            VideoBlock block2 = VideoBlock.builder().name("test").severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder().required(true).pattern("test.*").build()).build();

            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            VideoBlock block1 = VideoBlock.builder().name("test1").severity(Severity.WARN).build();

            VideoBlock block2 = VideoBlock.builder().name("test2").severity(Severity.WARN).build();

            assertNotEquals(block1, block2);
        }

        @Test
        @DisplayName("should handle pattern equality correctly")
        void shouldHandlePatternEqualityCorrectly() {
            VideoBlock.UrlConfig config1 = VideoBlock.UrlConfig.builder().pattern("test.*").build();

            VideoBlock.UrlConfig config2 = VideoBlock.UrlConfig.builder().pattern("test.*").build();

            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
        }

        @Test
        @DisplayName("should handle null patterns in equals")
        void shouldHandleNullPatternsInEquals() {
            VideoBlock.UrlConfig config1 = VideoBlock.UrlConfig.builder().pattern((Pattern) null).build();

            VideoBlock.UrlConfig config2 = VideoBlock.UrlConfig.builder().pattern((Pattern) null).build();

            assertEquals(config1, config2);
        }
    }
}
