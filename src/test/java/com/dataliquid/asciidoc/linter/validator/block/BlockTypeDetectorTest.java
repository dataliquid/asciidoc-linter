package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;

@DisplayName("BlockTypeDetector")
class BlockTypeDetectorTest {
    
    private BlockTypeDetector detector;
    private StructuralNode mockNode;
    
    @BeforeEach
    void setUp() {
        detector = new BlockTypeDetector();
        mockNode = mock(StructuralNode.class);
    }
    
    @Nested
    @DisplayName("detectType")
    class DetectType {
        
        @Test
        @DisplayName("should return null when node is null")
        void shouldReturnNullWhenNodeIsNull() {
            // Given
            StructuralNode nullNode = null;
            
            // When
            BlockType result = detector.detectType(nullNode);
            
            // Then
            assertNull(result);
        }
        
        @Test
        @DisplayName("should return null when context is null")
        void shouldReturnNullWhenContextIsNull() {
            // Given
            when(mockNode.getContext()).thenReturn(null);
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertNull(result);
        }
        
        @Test
        @DisplayName("should detect paragraph block")
        void shouldDetectParagraphBlock() {
            // Given
            when(mockNode.getContext()).thenReturn("paragraph");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.PARAGRAPH, result);
        }
        
        @Test
        @DisplayName("should detect listing block from listing context")
        void shouldDetectListingBlockFromListingContext() {
            // Given
            when(mockNode.getContext()).thenReturn("listing");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.LISTING, result);
        }
        
        @Test
        @DisplayName("should detect literal block from literal context")
        void shouldDetectLiteralBlockFromLiteralContext() {
            // Given
            when(mockNode.getContext()).thenReturn("literal");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.LITERAL, result);
        }
        
        @Test
        @DisplayName("should detect table block")
        void shouldDetectTableBlock() {
            // Given
            when(mockNode.getContext()).thenReturn("table");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.TABLE, result);
        }
        
        @Test
        @DisplayName("should detect image block")
        void shouldDetectImageBlock() {
            // Given
            when(mockNode.getContext()).thenReturn("image");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.IMAGE, result);
        }
        
        @Test
        @DisplayName("should detect verse block from verse context")
        void shouldDetectVerseBlockFromVerseContext() {
            // Given
            when(mockNode.getContext()).thenReturn("verse");
            when(mockNode.getStyle()).thenReturn("verse");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.VERSE, result);
        }
        
        @Test
        @DisplayName("should detect quote block from quote with attribution")
        void shouldDetectQuoteBlockFromQuoteWithAttribution() {
            // Given
            when(mockNode.getContext()).thenReturn("quote");
            when(mockNode.getAttribute("attribution")).thenReturn("Some Author");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.QUOTE, result);
        }
        
        @Test
        @DisplayName("should detect listing block from example with source style")
        void shouldDetectListingBlockFromExampleWithSourceStyle() {
            // Given
            when(mockNode.getContext()).thenReturn("example");
            when(mockNode.getStyle()).thenReturn("source");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.LISTING, result);
        }
        
        @Test
        @DisplayName("should detect image block from open with image role")
        void shouldDetectImageBlockFromOpenWithImageRole() {
            // Given
            when(mockNode.getContext()).thenReturn("open");
            when(mockNode.hasRole("image")).thenReturn(true);
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertEquals(BlockType.IMAGE, result);
        }
        
        @Test
        @DisplayName("should return null for unknown context")
        void shouldReturnNullForUnknownContext() {
            // Given
            when(mockNode.getContext()).thenReturn("unknown");
            
            // When
            BlockType result = detector.detectType(mockNode);
            
            // Then
            assertNull(result);
        }
    }
    
    @Nested
    @DisplayName("isBlockType")
    class IsBlockType {
        
        @Test
        @DisplayName("should return true when block matches type")
        void shouldReturnTrueWhenBlockMatchesType() {
            // Given
            when(mockNode.getContext()).thenReturn("paragraph");
            
            // When
            boolean result = detector.isBlockType(mockNode, BlockType.PARAGRAPH);
            
            // Then
            assertTrue(result);
        }
        
        @Test
        @DisplayName("should return false when block does not match type")
        void shouldReturnFalseWhenBlockDoesNotMatchType() {
            // Given
            when(mockNode.getContext()).thenReturn("paragraph");
            
            // When
            boolean result = detector.isBlockType(mockNode, BlockType.TABLE);
            
            // Then
            assertFalse(result);
        }
        
        @Test
        @DisplayName("should return false when node is null")
        void shouldReturnFalseWhenNodeIsNull() {
            // Given
            StructuralNode nullNode = null;
            
            // When
            boolean result = detector.isBlockType(nullNode, BlockType.PARAGRAPH);
            
            // Then
            assertFalse(result);
        }
    }
}