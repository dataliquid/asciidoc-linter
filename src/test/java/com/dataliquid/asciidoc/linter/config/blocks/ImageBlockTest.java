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
            ImageBlock.UrlConfig urlRule = ImageBlock.UrlConfig.builder()
                    .pattern("^https?://.*\\.(jpg|jpeg|png|gif|svg)$")
                    .required(true)
                    .build();
                    
            ImageBlock.DimensionConfig heightRule = ImageBlock.DimensionConfig.builder()
                    .minValue(100)
                    .maxValue(2000)
                    .required(false)
                    .build();
                    
            ImageBlock.DimensionConfig widthRule = ImageBlock.DimensionConfig.builder()
                    .minValue(100)
                    .maxValue(3000)
                    .required(false)
                    .build();
                    
            ImageBlock.AltTextConfig altRule = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(10)
                    .maxLength(200)
                    .build();
            
            // When
            ImageBlock image = ImageBlock.builder()
                    .severity(Severity.ERROR)
                    .url(urlRule)
                    .height(heightRule)
                    .width(widthRule)
                    .alt(altRule)
                    .build();
            
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
                ImageBlock.builder().build();
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
            ImageBlock.UrlConfig urlRule = ImageBlock.UrlConfig.builder()
                    .pattern("^https://.*")
                    .required(true)
                    .build();
            
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
            ImageBlock.UrlConfig urlRule = ImageBlock.UrlConfig.builder()
                    .pattern(pattern)
                    .required(false)
                    .build();
            
            // Then
            assertEquals(pattern, urlRule.getPattern());
            assertFalse(urlRule.isRequired());
        }
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // Given & When
            ImageBlock.UrlConfig urlRule = ImageBlock.UrlConfig.builder()
                    .pattern((String) null)
                    .build();
            
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
            ImageBlock.DimensionConfig dimension = ImageBlock.DimensionConfig.builder()
                    .minValue(50)
                    .maxValue(1000)
                    .required(true)
                    .build();
            
            // Then
            assertEquals(50, dimension.getMinValue());
            assertEquals(1000, dimension.getMaxValue());
            assertTrue(dimension.isRequired());
        }
        
        @Test
        @DisplayName("should allow optional dimensions")
        void shouldAllowOptionalDimensions() {
            // Given & When
            ImageBlock.DimensionConfig dimension = ImageBlock.DimensionConfig.builder()
                    .required(false)
                    .build();
            
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
            ImageBlock.AltTextConfig altText = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(150)
                    .build();
            
            // Then
            assertTrue(altText.isRequired());
            assertEquals(5, altText.getMinLength());
            assertEquals(150, altText.getMaxLength());
        }
        
        @Test
        @DisplayName("should allow optional alt text")
        void shouldAllowOptionalAltText() {
            // Given & When
            ImageBlock.AltTextConfig altText = ImageBlock.AltTextConfig.builder()
                    .required(false)
                    .build();
            
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
            ImageBlock.UrlConfig url1 = ImageBlock.UrlConfig.builder()
                    .pattern(".*\\.jpg$")
                    .required(true)
                    .build();
                    
            ImageBlock.UrlConfig url2 = ImageBlock.UrlConfig.builder()
                    .pattern(".*\\.jpg$")
                    .required(true)
                    .build();
                    
            ImageBlock.AltTextConfig alt1 = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(10)
                    .build();
                    
            ImageBlock.AltTextConfig alt2 = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(10)
                    .build();
                    
            // When
            ImageBlock image1 = ImageBlock.builder()
                    .severity(Severity.WARN)
                    .url(url1)
                    .alt(alt1)
                    .build();
                    
            ImageBlock image2 = ImageBlock.builder()
                    .severity(Severity.WARN)
                    .url(url2)
                    .alt(alt2)
                    .build();
                    
            ImageBlock image3 = ImageBlock.builder()
                    .severity(Severity.ERROR)
                    .url(url1)
                    .alt(alt1)
                    .build();
            
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
            ImageBlock.UrlConfig url1 = ImageBlock.UrlConfig.builder()
                    .pattern("test")
                    .required(true)
                    .build();
                    
            ImageBlock.UrlConfig url2 = ImageBlock.UrlConfig.builder()
                    .pattern("test")
                    .required(true)
                    .build();
                    
            ImageBlock.DimensionConfig dim1 = ImageBlock.DimensionConfig.builder()
                    .minValue(100)
                    .maxValue(200)
                    .required(false)
                    .build();
                    
            ImageBlock.DimensionConfig dim2 = ImageBlock.DimensionConfig.builder()
                    .minValue(100)
                    .maxValue(200)
                    .required(false)
                    .build();
                    
            ImageBlock.AltTextConfig alt1 = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .build();
                    
            ImageBlock.AltTextConfig alt2 = ImageBlock.AltTextConfig.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .build();
            
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