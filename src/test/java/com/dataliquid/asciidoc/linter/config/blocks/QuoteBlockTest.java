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

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;

@DisplayName("QuoteBlock Configuration Tests")
class QuoteBlockTest {
    
    @Test
    @DisplayName("should return QUOTE as block type")
    void shouldReturnCorrectBlockType() {
        QuoteBlock block = QuoteBlock.builder()
                .severity(Severity.INFO)
                .build();
        
        assertEquals(BlockType.QUOTE, block.getType());
    }
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build with minimal configuration")
        void shouldBuildWithMinimalConfig() {
            QuoteBlock block = QuoteBlock.builder()
                    .severity(Severity.WARN)
                    .build();
            
            assertNotNull(block);
            assertEquals(Severity.WARN, block.getSeverity());
            assertNull(block.getAuthor());
            assertNull(block.getSource());
            assertNull(block.getContent());
        }
        
        @Test
        @DisplayName("should build with complete configuration")
        void shouldBuildWithCompleteConfig() {
            QuoteBlock.AuthorConfig author = QuoteBlock.AuthorConfig.builder()
                    .required(true)
                    .minLength(3)
                    .maxLength(100)
                    .pattern("^[A-Z][a-zA-Z\\s\\.\\-,]+$")
                    .severity(Severity.ERROR)
                    .build();
            
            QuoteBlock.SourceConfig source = QuoteBlock.SourceConfig.builder()
                    .required(false)
                    .minLength(5)
                    .maxLength(200)
                    .pattern("^[A-Za-z0-9\\s,\\.\\-\\(\\)]+$")
                    .severity(Severity.WARN)
                    .build();
            
            QuoteBlock.ContentConfig content = QuoteBlock.ContentConfig.builder()
                    .required(true)
                    .minLength(20)
                    .maxLength(1000)
                    .lines(QuoteBlock.LinesConfig.builder()
                            .min(1)
                            .max(20)
                            .severity(Severity.INFO)
                            .build())
                    .build();
            
            QuoteBlock block = QuoteBlock.builder()
                    .name("important-quote")
                    .severity(Severity.INFO)
                    .occurrence(OccurrenceConfig.builder()
                            .min(0)
                            .max(3)
                            .build())
                    .author(author)
                    .source(source)
                    .content(content)
                    .build();
            
            assertNotNull(block);
            assertEquals("important-quote", block.getName());
            assertEquals(Severity.INFO, block.getSeverity());
            assertNotNull(block.getOccurrence());
            assertEquals(0, block.getOccurrence().min());
            assertEquals(3, block.getOccurrence().max());
            assertNotNull(block.getAuthor());
            assertTrue(block.getAuthor().isRequired());
            assertNotNull(block.getSource());
            assertFalse(block.getSource().isRequired());
            assertNotNull(block.getContent());
            assertTrue(block.getContent().isRequired());
        }
        
        @Test
        @DisplayName("should throw exception when severity is null")
        void shouldThrowWhenSeverityNull() {
            assertThrows(NullPointerException.class, () -> 
                    QuoteBlock.builder().build(),
                    "severity is required"
            );
        }
    }
    
    @Nested
    @DisplayName("AuthorConfig Tests")
    class AuthorConfigTests {
        
        @Test
        @DisplayName("should build with default values")
        void shouldBuildWithDefaults() {
            QuoteBlock.AuthorConfig config = QuoteBlock.AuthorConfig.builder().build();
            
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getPattern());
            assertNull(config.getSeverity());
        }
        
        @Test
        @DisplayName("should build with all values")
        void shouldBuildWithAllValues() {
            QuoteBlock.AuthorConfig config = QuoteBlock.AuthorConfig.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build();
            
            assertTrue(config.isRequired());
            assertEquals(5, config.getMinLength());
            assertEquals(50, config.getMaxLength());
            assertNotNull(config.getPattern());
            assertEquals("^[A-Z].*", config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            QuoteBlock.AuthorConfig config1 = QuoteBlock.AuthorConfig.builder()
                    .required(true)
                    .minLength(5)
                    .pattern("^[A-Z].*")
                    .build();
            
            QuoteBlock.AuthorConfig config2 = QuoteBlock.AuthorConfig.builder()
                    .required(true)
                    .minLength(5)
                    .pattern("^[A-Z].*")
                    .build();
            
            QuoteBlock.AuthorConfig config3 = QuoteBlock.AuthorConfig.builder()
                    .required(false)
                    .minLength(5)
                    .pattern("^[A-Z].*")
                    .build();
            
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("SourceConfig Tests")
    class SourceConfigTests {
        
        @Test
        @DisplayName("should build with default values")
        void shouldBuildWithDefaults() {
            QuoteBlock.SourceConfig config = QuoteBlock.SourceConfig.builder().build();
            
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getPattern());
            assertNull(config.getSeverity());
        }
        
        @Test
        @DisplayName("should build with all values")
        void shouldBuildWithAllValues() {
            QuoteBlock.SourceConfig config = QuoteBlock.SourceConfig.builder()
                    .required(true)
                    .minLength(10)
                    .maxLength(100)
                    .pattern("^[A-Za-z0-9\\\\s]+$")
                    .severity(Severity.WARN)
                    .build();
            
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(100, config.getMaxLength());
            assertNotNull(config.getPattern());
            assertEquals("^[A-Za-z0-9\\\\s]+$", config.getPattern().pattern());
            assertEquals(Severity.WARN, config.getSeverity());
        }
    }
    
    @Nested
    @DisplayName("ContentConfig Tests")
    class ContentConfigTests {
        
        @Test
        @DisplayName("should build with default values")
        void shouldBuildWithDefaults() {
            QuoteBlock.ContentConfig config = QuoteBlock.ContentConfig.builder().build();
            
            assertFalse(config.isRequired());
            assertNull(config.getMinLength());
            assertNull(config.getMaxLength());
            assertNull(config.getLines());
        }
        
        @Test
        @DisplayName("should build with all values")
        void shouldBuildWithAllValues() {
            QuoteBlock.LinesConfig lines = QuoteBlock.LinesConfig.builder()
                    .min(2)
                    .max(10)
                    .severity(Severity.INFO)
                    .build();
            
            QuoteBlock.ContentConfig config = QuoteBlock.ContentConfig.builder()
                    .required(true)
                    .minLength(50)
                    .maxLength(500)
                    .lines(lines)
                    .build();
            
            assertTrue(config.isRequired());
            assertEquals(50, config.getMinLength());
            assertEquals(500, config.getMaxLength());
            assertNotNull(config.getLines());
            assertEquals(2, config.getLines().getMin());
            assertEquals(10, config.getLines().getMax());
        }
    }
    
    @Nested
    @DisplayName("LinesConfig Tests")
    class LinesConfigTests {
        
        @Test
        @DisplayName("should build with default values")
        void shouldBuildWithDefaults() {
            QuoteBlock.LinesConfig config = QuoteBlock.LinesConfig.builder().build();
            
            assertNull(config.getMin());
            assertNull(config.getMax());
            assertNull(config.getSeverity());
        }
        
        @Test
        @DisplayName("should build with all values")
        void shouldBuildWithAllValues() {
            QuoteBlock.LinesConfig config = QuoteBlock.LinesConfig.builder()
                    .min(1)
                    .max(20)
                    .severity(Severity.WARN)
                    .build();
            
            assertEquals(1, config.getMin());
            assertEquals(20, config.getMax());
            assertEquals(Severity.WARN, config.getSeverity());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            QuoteBlock.LinesConfig config1 = QuoteBlock.LinesConfig.builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.INFO)
                    .build();
            
            QuoteBlock.LinesConfig config2 = QuoteBlock.LinesConfig.builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.INFO)
                    .build();
            
            QuoteBlock.LinesConfig config3 = QuoteBlock.LinesConfig.builder()
                    .min(5)
                    .max(15)
                    .severity(Severity.INFO)
                    .build();
            
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("should implement equals correctly")
        void shouldImplementEquals() {
            QuoteBlock block1 = QuoteBlock.builder()
                    .name("quote1")
                    .severity(Severity.WARN)
                    .author(QuoteBlock.AuthorConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            QuoteBlock block2 = QuoteBlock.builder()
                    .name("quote1")
                    .severity(Severity.WARN)
                    .author(QuoteBlock.AuthorConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            QuoteBlock block3 = QuoteBlock.builder()
                    .name("quote2")
                    .severity(Severity.WARN)
                    .author(QuoteBlock.AuthorConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            assertEquals(block1, block2);
            assertNotEquals(block1, block3);
            assertNotEquals(block1, null);
            assertNotEquals(block1, new Object());
        }
        
        @Test
        @DisplayName("should implement hashCode consistently")
        void shouldImplementHashCode() {
            QuoteBlock block1 = QuoteBlock.builder()
                    .severity(Severity.ERROR)
                    .source(QuoteBlock.SourceConfig.builder()
                            .required(false)
                            .maxLength(100)
                            .build())
                    .build();
            
            QuoteBlock block2 = QuoteBlock.builder()
                    .severity(Severity.ERROR)
                    .source(QuoteBlock.SourceConfig.builder()
                            .required(false)
                            .maxLength(100)
                            .build())
                    .build();
            
            assertEquals(block1.hashCode(), block2.hashCode());
        }
    }
}