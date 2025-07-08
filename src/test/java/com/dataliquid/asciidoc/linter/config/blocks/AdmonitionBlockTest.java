package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;

@DisplayName("AdmonitionBlock Tests")
class AdmonitionBlockTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build block with all properties")
        void shouldBuildCompleteBlock() {
            // Given
            AdmonitionBlock.TypeConfig typeConfig = AdmonitionBlock.TypeConfig.builder()
                .required(true)
                .allowed(List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION"))
                .severity(Severity.ERROR)
                .build();
                
            AdmonitionBlock.TitleConfig titleConfig = AdmonitionBlock.TitleConfig.builder()
                .required(true)
                .pattern("^[A-Z].*")
                .minLength(3)
                .maxLength(50)
                .severity(Severity.ERROR)
                .build();
                
            AdmonitionBlock.ContentConfig contentConfig = AdmonitionBlock.ContentConfig.builder()
                .required(true)
                .minLength(10)
                .maxLength(500)
                .severity(Severity.WARN)
                .build();
                
            AdmonitionBlock.IconConfig iconConfig = AdmonitionBlock.IconConfig.builder()
                .required(false)
                .pattern("^(fa-|icon-|octicon-).*$")
                .severity(Severity.INFO)
                .build();
            
            // When
            AdmonitionBlock block = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .type(typeConfig)
                .title(titleConfig)
                .content(contentConfig)
                .icon(iconConfig)
                .build();
            
            // Then
            assertNotNull(block);
            assertEquals(Severity.ERROR, block.getSeverity());
            assertEquals(typeConfig, block.getTypeConfig());
            assertEquals(titleConfig, block.getTitle());
            assertEquals(contentConfig, block.getContent());
            assertEquals(iconConfig, block.getIcon());
        }
        
        @Test
        @DisplayName("should throw exception when severity is missing")
        void shouldThrowExceptionWhenSeverityMissing() {
            // When & Then
            assertThrows(NullPointerException.class, () -> 
                AdmonitionBlock.builder().build()
            );
        }
    }
    
    @Nested
    @DisplayName("TypeConfig Tests")
    class TypeConfigTests {
        
        @Test
        @DisplayName("should build type config with all properties")
        void shouldBuildCompleteTypeConfig() {
            // Given
            List<String> allowedTypes = List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION");
            
            // When
            AdmonitionBlock.TypeConfig config = AdmonitionBlock.TypeConfig.builder()
                .required(true)
                .allowed(allowedTypes)
                .severity(Severity.ERROR)
                .build();
            
            // Then
            assertTrue(config.isRequired());
            assertEquals(allowedTypes, config.getAllowed());
            assertEquals(Severity.ERROR, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle empty allowed list")
        void shouldHandleEmptyAllowedList() {
            // When
            AdmonitionBlock.TypeConfig config = AdmonitionBlock.TypeConfig.builder()
                .required(false)
                .build();
            
            // Then
            assertFalse(config.isRequired());
            assertNotNull(config.getAllowed());
            assertTrue(config.getAllowed().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("TitleConfig Tests")
    class TitleConfigTests {
        
        @Test
        @DisplayName("should build title config with all properties")
        void shouldBuildCompleteTitleConfig() {
            // Given
            String patternStr = "^[A-Z][A-Za-z\\s]{2,49}$";
            
            // When
            AdmonitionBlock.TitleConfig config = AdmonitionBlock.TitleConfig.builder()
                .required(true)
                .pattern(patternStr)
                .minLength(3)
                .maxLength(50)
                .severity(Severity.ERROR)
                .build();
            
            // Then
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals(patternStr, config.getPattern().pattern());
            assertEquals(3, config.getMinLength());
            assertEquals(50, config.getMaxLength());
            assertEquals(Severity.ERROR, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle pattern as Pattern object")
        void shouldHandlePatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^[A-Z].*");
            
            // When
            AdmonitionBlock.TitleConfig config = AdmonitionBlock.TitleConfig.builder()
                .pattern(pattern)
                .build();
            
            // Then
            assertEquals(pattern, config.getPattern());
        }
    }
    
    @Nested
    @DisplayName("ContentConfig Tests")
    class ContentConfigTests {
        
        @Test
        @DisplayName("should build content config with all properties")
        void shouldBuildCompleteContentConfig() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(1)
                .max(10)
                .severity(Severity.INFO)
                .build();
            
            // When
            AdmonitionBlock.ContentConfig config = AdmonitionBlock.ContentConfig.builder()
                .required(true)
                .minLength(10)
                .maxLength(500)
                .lines(lineConfig)
                .severity(Severity.WARN)
                .build();
            
            // Then
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(500, config.getMaxLength());
            assertNotNull(config.getLines());
            assertEquals(Severity.WARN, config.getSeverity());
        }
        
        @Test
        @DisplayName("should build minimal content config")
        void shouldBuildMinimalContentConfig() {
            // When
            AdmonitionBlock.ContentConfig config = AdmonitionBlock.ContentConfig.builder()
                .build();
            
            // Then
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getLines());
            assertNull(config.getSeverity());
        }
    }
    
    @Nested
    @DisplayName("IconConfig Tests")
    class IconConfigTests {
        
        @Test
        @DisplayName("should build icon config with all properties")
        void shouldBuildCompleteIconConfig() {
            // When
            AdmonitionBlock.IconConfig config = AdmonitionBlock.IconConfig.builder()
                .required(false)
                .pattern("^(fa-|icon-|octicon-).*$")
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertFalse(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals("^(fa-|icon-|octicon-).*$", config.getPattern().pattern());
            assertEquals(Severity.INFO, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle pattern as Pattern object")
        void shouldHandlePatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^icon-.*");
            
            // When
            AdmonitionBlock.IconConfig config = AdmonitionBlock.IconConfig.builder()
                .pattern(pattern)
                .build();
            
            // Then
            assertEquals(pattern, config.getPattern());
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("should implement equals and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            // Given
            AdmonitionBlock.TitleConfig title1 = AdmonitionBlock.TitleConfig.builder()
                .required(true)
                .pattern("^[A-Z].*")
                .minLength(3)
                .maxLength(50)
                .severity(Severity.ERROR)
                .build();
                
            AdmonitionBlock.TitleConfig title2 = AdmonitionBlock.TitleConfig.builder()
                .required(true)
                .pattern("^[A-Z].*")
                .minLength(3)
                .maxLength(50)
                .severity(Severity.ERROR)
                .build();
                
            AdmonitionBlock block1 = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .title(title1)
                .build();
                
            AdmonitionBlock block2 = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .title(title2)
                .build();
                
            AdmonitionBlock block3 = AdmonitionBlock.builder()
                .severity(Severity.WARN)
                .title(title1)
                .build();
            
            // Then
            assertEquals(block1, block2);
            assertNotEquals(block1, block3);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1.hashCode(), block3.hashCode());
        }
        
        @Test
        @DisplayName("should handle pattern equality correctly")
        void shouldHandlePatternEquality() {
            // Given
            AdmonitionBlock.TitleConfig config1 = AdmonitionBlock.TitleConfig.builder()
                .pattern("^[A-Z].*")
                .build();
                
            AdmonitionBlock.TitleConfig config2 = AdmonitionBlock.TitleConfig.builder()
                .pattern("^[A-Z].*")
                .build();
                
            AdmonitionBlock.TitleConfig config3 = AdmonitionBlock.TitleConfig.builder()
                .pattern("^[a-z].*")
                .build();
            
            // Then
            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
            assertEquals(config1.hashCode(), config2.hashCode());
        }
    }
}