package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;

@DisplayName("BlockValidatorFactory")
class BlockValidatorFactoryTest {
    
    private BlockValidatorFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new BlockValidatorFactory();
    }
    
    @Nested
    @DisplayName("getValidator")
    class GetValidator {
        
        @Test
        @DisplayName("should return ParagraphBlockValidator for PARAGRAPH type")
        void shouldReturnParagraphBlockValidatorForParagraphType() {
            // Given
            BlockType type = BlockType.PARAGRAPH;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNotNull(validator);
            assertInstanceOf(ParagraphBlockValidator.class, validator);
            assertEquals(BlockType.PARAGRAPH, validator.getSupportedType());
        }
        
        @Test
        @DisplayName("should return TableBlockValidator for TABLE type")
        void shouldReturnTableBlockValidatorForTableType() {
            // Given
            BlockType type = BlockType.TABLE;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNotNull(validator);
            assertInstanceOf(TableBlockValidator.class, validator);
            assertEquals(BlockType.TABLE, validator.getSupportedType());
        }
        
        @Test
        @DisplayName("should return ImageBlockValidator for IMAGE type")
        void shouldReturnImageBlockValidatorForImageType() {
            // Given
            BlockType type = BlockType.IMAGE;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNotNull(validator);
            assertInstanceOf(ImageBlockValidator.class, validator);
            assertEquals(BlockType.IMAGE, validator.getSupportedType());
        }
        
        @Test
        @DisplayName("should return ListingBlockValidator for LISTING type")
        void shouldReturnListingBlockValidatorForListingType() {
            // Given
            BlockType type = BlockType.LISTING;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNotNull(validator);
            assertInstanceOf(ListingBlockValidator.class, validator);
            assertEquals(BlockType.LISTING, validator.getSupportedType());
        }
        
        @Test
        @DisplayName("should return VerseBlockValidator for VERSE type")
        void shouldReturnVerseBlockValidatorForVerseType() {
            // Given
            BlockType type = BlockType.VERSE;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNotNull(validator);
            assertInstanceOf(VerseBlockValidator.class, validator);
            assertEquals(BlockType.VERSE, validator.getSupportedType());
        }
        
        @Test
        @DisplayName("should return null for null type")
        void shouldReturnNullForNullType() {
            // Given
            BlockType type = null;
            
            // When
            BlockTypeValidator validator = factory.getValidator(type);
            
            // Then
            assertNull(validator);
        }
        
        @Test
        @DisplayName("should return same instance for same type")
        void shouldReturnSameInstanceForSameType() {
            // Given
            BlockType type = BlockType.PARAGRAPH;
            
            // When
            BlockTypeValidator validator1 = factory.getValidator(type);
            BlockTypeValidator validator2 = factory.getValidator(type);
            
            // Then
            assertSame(validator1, validator2);
        }
    }
    
    @Nested
    @DisplayName("hasValidator")
    class HasValidator {
        
        @Test
        @DisplayName("should return true for supported types")
        void shouldReturnTrueForSupportedTypes() {
            // Given
            BlockType[] supportedTypes = {
                BlockType.PARAGRAPH,
                BlockType.TABLE,
                BlockType.IMAGE,
                BlockType.LISTING,
                BlockType.VERSE
            };
            
            // When/Then
            for (BlockType type : supportedTypes) {
                assertTrue(factory.hasValidator(type), 
                    "Should have validator for " + type);
            }
        }
        
        @Test
        @DisplayName("should return false for null type")
        void shouldReturnFalseForNullType() {
            // Given
            BlockType type = null;
            
            // When
            boolean result = factory.hasValidator(type);
            
            // Then
            assertFalse(result);
        }
    }
}