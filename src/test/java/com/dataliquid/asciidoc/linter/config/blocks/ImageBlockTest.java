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

@DisplayName("ImageBlock")
class ImageBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build ImageBlock with all attributes")
        void shouldBuildImageBlockWithAllAttributes() {
            // Given
            ImageBlock.UrlConfig urlRule = new ImageBlock.UrlConfig("^https?://.*\\.(jpg|jpeg|png|gif|svg)$", true);

            ImageBlock.DimensionConfig heightRule = new ImageBlock.DimensionConfig(100, 2000, false);

            ImageBlock.DimensionConfig widthRule = new ImageBlock.DimensionConfig(100, 3000, false);

            ImageBlock.AltTextConfig altRule = new ImageBlock.AltTextConfig(true, 10, 200);

            // When
            ImageBlock image = new ImageBlock(null, Severity.ERROR, null, null, urlRule, heightRule, widthRule,
                    altRule);

            // Then
            assertEquals(Severity.ERROR, image.getSeverity());

            assertNotNull(image.getUrl());
            assertTrue(image.getUrl().isRequired());
            assertNotNull(image.getUrl().getPattern());

            assertNotNull(image.getHeight());
            assertFalse(image.getHeight().isRequired());
            assertEquals(100, image.getHeight().getMinValue());
            assertEquals(2000, image.getHeight().getMaxValue());

            assertNotNull(image.getWidth());
            assertFalse(image.getWidth().isRequired());
            assertEquals(100, image.getWidth().getMinValue());
            assertEquals(3000, image.getWidth().getMaxValue());

            assertNotNull(image.getAlt());
            assertTrue(image.getAlt().isRequired());
            assertEquals(10, image.getAlt().getMinLength());
            assertEquals(200, image.getAlt().getMaxLength());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                new ImageBlock(null, null, null, null, null, null, null, null);
            });
        }
    }

    @Nested
    @DisplayName("UrlConfig Tests")
    class UrlConfigTests {

        @Test
        @DisplayName("should create UrlConfig with string pattern")
        void shouldCreateUrlConfigWithStringPattern() {
            // Given & When
            ImageBlock.UrlConfig urlRule = new ImageBlock.UrlConfig("^https://.*", true);

            // Then
            assertNotNull(urlRule.getPattern());
            assertEquals("^https://.*", urlRule.getPattern().pattern());
            assertTrue(urlRule.isRequired());
        }

        @Test
        @DisplayName("should create UrlConfig with Pattern object")
        void shouldCreateUrlConfigWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile(".*\\.png$");

            // When
            ImageBlock.UrlConfig urlRule = new ImageBlock.UrlConfig(pattern.pattern(), false);

            // Then
            assertEquals(pattern.pattern(), urlRule.getPattern().pattern());
            assertFalse(urlRule.isRequired());
        }

        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // Given & When
            ImageBlock.UrlConfig urlRule = new ImageBlock.UrlConfig(null, false);

            // Then
            assertNull(urlRule.getPattern());
        }
    }

    @Nested
    @DisplayName("DimensionConfig Tests")
    class DimensionConfigTests {

        @Test
        @DisplayName("should create DimensionConfig with min and max values")
        void shouldCreateDimensionConfigWithMinAndMaxValues() {
            // Given & When
            ImageBlock.DimensionConfig dimension = new ImageBlock.DimensionConfig(50, 1000, true);

            // Then
            assertEquals(50, dimension.getMinValue());
            assertEquals(1000, dimension.getMaxValue());
            assertTrue(dimension.isRequired());
        }

        @Test
        @DisplayName("should allow optional dimensions")
        void shouldAllowOptionalDimensions() {
            // Given & When
            ImageBlock.DimensionConfig dimension = new ImageBlock.DimensionConfig(null, null, false);

            // Then
            assertNull(dimension.getMinValue());
            assertNull(dimension.getMaxValue());
            assertFalse(dimension.isRequired());
        }
    }

    @Nested
    @DisplayName("AltTextConfig Tests")
    class AltTextConfigTests {

        @Test
        @DisplayName("should create AltTextConfig with length constraints")
        void shouldCreateAltTextConfigWithLengthConstraints() {
            // Given & When
            ImageBlock.AltTextConfig altText = new ImageBlock.AltTextConfig(true, 5, 150);

            // Then
            assertTrue(altText.isRequired());
            assertEquals(5, altText.getMinLength());
            assertEquals(150, altText.getMaxLength());
        }

        @Test
        @DisplayName("should allow optional alt text")
        void shouldAllowOptionalAltText() {
            // Given & When
            ImageBlock.AltTextConfig altText = new ImageBlock.AltTextConfig(false, null, null);

            // Then
            assertFalse(altText.isRequired());
            assertNull(altText.getMinLength());
            assertNull(altText.getMaxLength());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            ImageBlock.UrlConfig url1 = new ImageBlock.UrlConfig(".*\\.jpg$", true);

            ImageBlock.UrlConfig url2 = new ImageBlock.UrlConfig(".*\\.jpg$", true);

            ImageBlock.AltTextConfig alt1 = new ImageBlock.AltTextConfig(true, 10, null);

            ImageBlock.AltTextConfig alt2 = new ImageBlock.AltTextConfig(true, 10, null);

            // When
            ImageBlock image1 = new ImageBlock(null, Severity.WARN, null, null, url1, null, null, alt1);

            ImageBlock image2 = new ImageBlock(null, Severity.WARN, null, null, url2, null, null, alt2);

            ImageBlock image3 = new ImageBlock(null, Severity.ERROR, null, null, url1, null, null, alt1);

            // Then
            assertEquals(image1, image2);
            assertNotEquals(image1, image3);
            assertEquals(image1.hashCode(), image2.hashCode());
            assertNotEquals(image1.hashCode(), image3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            ImageBlock.UrlConfig url1 = new ImageBlock.UrlConfig("test", true);

            ImageBlock.UrlConfig url2 = new ImageBlock.UrlConfig("test", true);

            ImageBlock.DimensionConfig dim1 = new ImageBlock.DimensionConfig(100, 200, false);

            ImageBlock.DimensionConfig dim2 = new ImageBlock.DimensionConfig(100, 200, false);

            ImageBlock.AltTextConfig alt1 = new ImageBlock.AltTextConfig(true, 5, 50);

            ImageBlock.AltTextConfig alt2 = new ImageBlock.AltTextConfig(true, 5, 50);

            // Then
            assertEquals(url1, url2);
            assertEquals(url1.hashCode(), url2.hashCode());

            assertEquals(dim1, dim2);
            assertEquals(dim1.hashCode(), dim2.hashCode());

            assertEquals(alt1, alt2);
            assertEquals(alt1.hashCode(), alt2.hashCode());
        }
    }
}
