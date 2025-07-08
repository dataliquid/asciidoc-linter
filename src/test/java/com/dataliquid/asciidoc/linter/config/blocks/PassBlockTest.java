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

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
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
            TypeConfig typeConfig = TypeConfig.builder()
                .required(true)
                .allowed(Arrays.asList("html", "xml", "svg"))
                .severity(Severity.ERROR)
                .build();
                
            ContentConfig contentConfig = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern("^<[^>]+>.*</[^>]+>$")
                .severity(Severity.ERROR)
                .build();
                
            ReasonConfig reasonConfig = ReasonConfig.builder()
                .required(true)
                .minLength(20)
                .maxLength(200)
                .severity(Severity.ERROR)
                .build();
            
            // When
            PassBlock block = PassBlock.builder()
                .name("Custom HTML Pass")
                .severity(Severity.ERROR)
                .type(typeConfig)
                .content(contentConfig)
                .reason(reasonConfig)
                .build();
            
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
            assertThrows(NullPointerException.class, () ->
                PassBlock.builder()
                    .name("Invalid Block")
                    .build()
            );
        }
        
        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TypeConfig typeConfig = TypeConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
                
            PassBlock block1 = PassBlock.builder()
                .severity(Severity.WARN)
                .type(typeConfig)
                .build();
                
            PassBlock block2 = PassBlock.builder()
                .severity(Severity.WARN)
                .type(typeConfig)
                .build();
                
            PassBlock block3 = PassBlock.builder()
                .severity(Severity.ERROR)
                .type(typeConfig)
                .build();
            
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
            TypeConfig config = TypeConfig.builder()
                .required(true)
                .allowed(allowed)
                .severity(Severity.ERROR)
                .build();
            
            // Then
            assertTrue(config.isRequired());
            assertEquals(allowed, config.getAllowed());
            assertEquals(Severity.ERROR, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle empty allowed list")
        void shouldHandleEmptyAllowedList() {
            // When
            TypeConfig config = TypeConfig.builder()
                .required(false)
                .severity(Severity.WARN)
                .build();
            
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
            TypeConfig config = TypeConfig.builder()
                .allowed(allowed)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertThrows(UnsupportedOperationException.class, () ->
                config.getAllowed().add("svg")
            );
        }
        
        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            TypeConfig config1 = TypeConfig.builder()
                .required(true)
                .allowed(Arrays.asList("html", "xml"))
                .severity(Severity.ERROR)
                .build();
                
            TypeConfig config2 = TypeConfig.builder()
                .required(true)
                .allowed(Arrays.asList("html", "xml"))
                .severity(Severity.ERROR)
                .build();
                
            TypeConfig config3 = TypeConfig.builder()
                .required(false)
                .allowed(Arrays.asList("html", "xml"))
                .severity(Severity.ERROR)
                .build();
            
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
            ContentConfig config = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern(pattern)
                .severity(Severity.ERROR)
                .build();
            
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
            ContentConfig config = ContentConfig.builder()
                .pattern("^<div.*>.*</div>$")
                .severity(Severity.WARN)
                .build();
            
            // Then
            assertNotNull(config.getPattern());
            assertEquals("^<div.*>.*</div>$", config.getPattern().pattern());
        }
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // When
            ContentConfig config = ContentConfig.builder()
                .required(false)
                .maxLength(500)
                .pattern((Pattern) null)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertNull(config.getPattern());
        }
        
        @Test
        @DisplayName("should support equals and hashCode with pattern")
        void shouldSupportEqualsAndHashCodeWithPattern() {
            // Given
            ContentConfig config1 = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern("^<[^>]+>.*$")
                .severity(Severity.ERROR)
                .build();
                
            ContentConfig config2 = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern("^<[^>]+>.*$")
                .severity(Severity.ERROR)
                .build();
                
            ContentConfig config3 = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern("^<div>.*$")
                .severity(Severity.ERROR)
                .build();
            
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
            ReasonConfig config = ReasonConfig.builder()
                .required(true)
                .minLength(20)
                .maxLength(200)
                .severity(Severity.ERROR)
                .build();
            
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
            ReasonConfig config = ReasonConfig.builder()
                .required(false)
                .severity(Severity.WARN)
                .build();
            
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
            ReasonConfig config1 = ReasonConfig.builder()
                .required(true)
                .minLength(10)
                .maxLength(100)
                .severity(Severity.ERROR)
                .build();
                
            ReasonConfig config2 = ReasonConfig.builder()
                .required(true)
                .minLength(10)
                .maxLength(100)
                .severity(Severity.ERROR)
                .build();
                
            ReasonConfig config3 = ReasonConfig.builder()
                .required(true)
                .minLength(20)
                .maxLength(100)
                .severity(Severity.ERROR)
                .build();
            
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
            OccurrenceConfig occurrence = OccurrenceConfig.builder()
                .min(0)
                .max(1)
                .severity(Severity.ERROR)
                .build();
            
            // When
            PassBlock block = PassBlock.builder()
                .severity(Severity.ERROR)
                .occurrence(occurrence)
                .build();
            
            // Then
            assertEquals(occurrence, block.getOccurrence());
        }
        
        @Test
        @DisplayName("should support full configuration")
        void shouldSupportFullConfiguration() {
            // Given
            TypeConfig typeConfig = TypeConfig.builder()
                .required(true)
                .allowed(Arrays.asList("html", "xml", "svg"))
                .severity(Severity.ERROR)
                .build();
                
            ContentConfig contentConfig = ContentConfig.builder()
                .required(true)
                .maxLength(1000)
                .pattern("^<[^>]+>.*</[^>]+>$")
                .severity(Severity.ERROR)
                .build();
                
            ReasonConfig reasonConfig = ReasonConfig.builder()
                .required(true)
                .minLength(20)
                .maxLength(200)
                .severity(Severity.ERROR)
                .build();
                
            OccurrenceConfig occurrence = OccurrenceConfig.builder()
                .min(0)
                .max(1)
                .severity(Severity.ERROR)
                .build();
            
            // When
            PassBlock block = PassBlock.builder()
                .name("Passthrough Block")
                .severity(Severity.ERROR)
                .occurrence(occurrence)
                .type(typeConfig)
                .content(contentConfig)
                .reason(reasonConfig)
                .build();
            
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