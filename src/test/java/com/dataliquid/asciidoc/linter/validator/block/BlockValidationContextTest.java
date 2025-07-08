package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;

@DisplayName("BlockValidationContext")
class BlockValidationContextTest {
    
    private BlockValidationContext context;
    private Section mockSection;
    private String filename = "test.adoc";
    
    @BeforeEach
    void setUp() {
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, filename);
    }
    
    @Nested
    @DisplayName("constructor")
    class Constructor {
        
        @Test
        @DisplayName("should require non-null section")
        void shouldRequireNonNullSection() {
            // Given
            Section nullSection = null;
            
            // When/Then
            assertThrows(NullPointerException.class, 
                () -> new BlockValidationContext(nullSection, filename));
        }
        
        @Test
        @DisplayName("should require non-null filename")
        void shouldRequireNonNullFilename() {
            // Given
            String nullFilename = null;
            
            // When/Then
            assertThrows(NullPointerException.class, 
                () -> new BlockValidationContext(mockSection, nullFilename));
        }
        
        @Test
        @DisplayName("should store section and filename")
        void shouldStoreSectionAndFilename() {
            // Given/When
            BlockValidationContext ctx = new BlockValidationContext(mockSection, filename);
            
            // Then
            assertEquals(mockSection, ctx.getSection());
            assertEquals(filename, ctx.getFilename());
        }
    }
    
    @Nested
    @DisplayName("createLocation")
    class CreateLocation {
        
        @Test
        @DisplayName("should create location with line number from source location")
        void shouldCreateLocationWithLineNumberFromSourceLocation() {
            // Given
            StructuralNode mockBlock = mock(StructuralNode.class);
            Cursor mockCursor = mock(Cursor.class);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockCursor.getLineNumber()).thenReturn(42);
            
            // When
            SourceLocation location = context.createLocation(mockBlock);
            
            // Then
            assertEquals(filename, location.getFilename());
            assertEquals(42, location.getStartLine());
        }
        
        @Test
        @DisplayName("should create location with line 1 when source location is null")
        void shouldCreateLocationWithLine1WhenSourceLocationIsNull() {
            // Given
            StructuralNode mockBlock = mock(StructuralNode.class);
            when(mockBlock.getSourceLocation()).thenReturn(null);
            
            // When
            SourceLocation location = context.createLocation(mockBlock);
            
            // Then
            assertEquals(filename, location.getFilename());
            assertEquals(1, location.getStartLine());
        }
    }
    
    @Nested
    @DisplayName("trackBlock")
    class TrackBlock {
        
        @Test
        @DisplayName("should track block occurrence")
        void shouldTrackBlockOccurrence() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            StructuralNode mockBlock = mock(StructuralNode.class);
            when(mockConfig.getName()).thenReturn("intro");
            when(mockConfig.getType()).thenReturn(BlockType.PARAGRAPH);
            
            // When
            context.trackBlock(mockConfig, mockBlock);
            
            // Then
            assertEquals(1, context.getOccurrenceCount(mockConfig));
        }
        
        @Test
        @DisplayName("should track multiple occurrences of same block")
        void shouldTrackMultipleOccurrencesOfSameBlock() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            StructuralNode mockBlock1 = mock(StructuralNode.class);
            StructuralNode mockBlock2 = mock(StructuralNode.class);
            when(mockConfig.getName()).thenReturn("intro");
            when(mockConfig.getType()).thenReturn(BlockType.PARAGRAPH);
            
            // When
            context.trackBlock(mockConfig, mockBlock1);
            context.trackBlock(mockConfig, mockBlock2);
            
            // Then
            assertEquals(2, context.getOccurrenceCount(mockConfig));
        }
        
        @Test
        @DisplayName("should track block order")
        void shouldTrackBlockOrder() {
            // Given
            Block config1 = mock(ParagraphBlock.class);
            Block config2 = mock(ParagraphBlock.class);
            StructuralNode block1 = mock(StructuralNode.class);
            StructuralNode block2 = mock(StructuralNode.class);
            when(config1.getName()).thenReturn("first");
            when(config2.getName()).thenReturn("second");
            when(config1.getType()).thenReturn(BlockType.PARAGRAPH);
            when(config2.getType()).thenReturn(BlockType.PARAGRAPH);
            
            // When
            context.trackBlock(config1, block1);
            context.trackBlock(config2, block2);
            
            // Then
            List<BlockValidationContext.BlockPosition> order = context.getBlockOrder();
            assertEquals(2, order.size());
            assertEquals(0, order.get(0).getIndex());
            assertEquals(1, order.get(1).getIndex());
        }
    }
    
    @Nested
    @DisplayName("getOccurrences")
    class GetOccurrences {
        
        @Test
        @DisplayName("should return empty list when no occurrences tracked")
        void shouldReturnEmptyListWhenNoOccurrencesTracked() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            when(mockConfig.getName()).thenReturn("intro");
            when(mockConfig.getType()).thenReturn(BlockType.PARAGRAPH);
            
            // When
            List<BlockValidationContext.BlockOccurrence> occurrences = 
                context.getOccurrences(mockConfig);
            
            // Then
            assertTrue(occurrences.isEmpty());
        }
        
        @Test
        @DisplayName("should return tracked occurrences")
        void shouldReturnTrackedOccurrences() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            StructuralNode mockBlock = mock(StructuralNode.class);
            when(mockConfig.getName()).thenReturn("intro");
            when(mockConfig.getType()).thenReturn(BlockType.PARAGRAPH);
            context.trackBlock(mockConfig, mockBlock);
            
            // When
            List<BlockValidationContext.BlockOccurrence> occurrences = 
                context.getOccurrences(mockConfig);
            
            // Then
            assertEquals(1, occurrences.size());
            assertEquals(mockConfig, occurrences.get(0).getConfig());
            assertEquals(mockBlock, occurrences.get(0).getBlock());
            assertEquals(0, occurrences.get(0).getPosition());
        }
    }
    
    @Nested
    @DisplayName("getBlockName")
    class GetBlockName {
        
        @Test
        @DisplayName("should return named block identifier")
        void shouldReturnNamedBlockIdentifier() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            when(mockConfig.getName()).thenReturn("intro");
            
            // When
            String name = context.getBlockName(mockConfig);
            
            // Then
            assertEquals("block 'intro'", name);
        }
        
        @Test
        @DisplayName("should return type-based identifier when name is null")
        void shouldReturnTypeBasedIdentifierWhenNameIsNull() {
            // Given
            Block mockConfig = mock(ParagraphBlock.class);
            when(mockConfig.getName()).thenReturn(null);
            when(mockConfig.getType()).thenReturn(BlockType.PARAGRAPH);
            
            // When
            String name = context.getBlockName(mockConfig);
            
            // Then
            assertEquals("paragraph block", name);
        }
    }
}