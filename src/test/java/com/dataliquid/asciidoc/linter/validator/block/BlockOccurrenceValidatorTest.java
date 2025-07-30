package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("BlockOccurrenceValidator")
class BlockOccurrenceValidatorTest {
    
    private BlockOccurrenceValidator validator;
    private BlockValidationContext context;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new BlockOccurrenceValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when no blocks configured")
        void shouldReturnEmptyListWhenNoBlocksConfigured() {
            // Given
            List<Block> blocks = new ArrayList<>();
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no occurrence rules configured")
        void shouldReturnEmptyListWhenNoOccurrenceRulesConfigured() {
            // Given
            ParagraphBlock block = ParagraphBlock.builder()
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should throw when context is null")
        void shouldThrowWhenContextIsNull() {
            // Given
            List<Block> blocks = new ArrayList<>();
            
            // When/Then
            assertThrows(NullPointerException.class, () -> 
                validator.validate(null, blocks));
        }
        
        @Test
        @DisplayName("should throw when blocks is null")
        void shouldThrowWhenBlocksIsNull() {
            // Given/When/Then
            assertThrows(NullPointerException.class, () -> 
                validator.validate(context, null));
        }
    }
    
    @Nested
    @DisplayName("minimum occurrence validation")
    class MinimumOccurrenceValidation {
        
        @Test
        @DisplayName("should validate minimum occurrences not met")
        void shouldValidateMinimumOccurrencesNotMet() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(2)
                .max(5)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .name("introduction")
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add only one occurrence to context
            StructuralNode node = mock(StructuralNode.class);
            context.trackBlock(block, node);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("block.occurrence.min", msg.getRuleId());
            assertEquals("Too few occurrences of block: paragraph", msg.getMessage());
            assertEquals("1", msg.getActualValue().orElse(null));
            assertEquals("At least 2 occurrences", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate minimum occurrences met")
        void shouldValidateMinimumOccurrencesMet() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(2)
                .max(5)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .name("introduction")
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add two occurrences to context
            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            context.trackBlock(block, node1);
            context.trackBlock(block, node2);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should validate zero occurrences when minimum required")
        void shouldValidateZeroOccurrencesWhenMinimumRequired() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(1)
                .max(3)
                .severity(Severity.WARN)
                .build();
            TableBlock block = TableBlock.builder()
                .name("summary")
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // No blocks tracked in context
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("0", msg.getActualValue().orElse(null));
            assertEquals("At least 1 occurrences", msg.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("maximum occurrence validation")
    class MaximumOccurrenceValidation {
        
        @Test
        @DisplayName("should validate maximum occurrences exceeded")
        void shouldValidateMaximumOccurrencesExceeded() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(1)
                .max(2)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .name("note")
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add three occurrences to context
            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);
            context.trackBlock(block, node1);
            context.trackBlock(block, node2);
            context.trackBlock(block, node3);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("block.occurrence.max", msg.getRuleId());
            assertEquals("Too many occurrences of block: paragraph", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At most 2 occurrences", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum occurrences not exceeded")
        void shouldValidateMaximumOccurrencesNotExceeded() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(0)
                .max(3)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add three occurrences to context (exactly at max)
            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);
            context.trackBlock(block, node1);
            context.trackBlock(block, node2);
            context.trackBlock(block, node3);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("multiple blocks validation")
    class MultipleBlocksValidation {
        
        @Test
        @DisplayName("should validate multiple blocks with different occurrence rules")
        void shouldValidateMultipleBlocksWithDifferentOccurrenceRules() {
            // Given
            OccurrenceConfig paragraphOccurrence = OccurrenceConfig.builder()
                .min(1)
                .max(3)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock paragraphBlock = ParagraphBlock.builder()
                .name("intro")
                .occurrence(paragraphOccurrence)
                .severity(Severity.ERROR)
                .build();
            
            OccurrenceConfig tableOccurrence = OccurrenceConfig.builder()
                .min(2)
                .max(2)
                .severity(Severity.WARN)
                .build();
            TableBlock tableBlock = TableBlock.builder()
                .name("data")
                .occurrence(tableOccurrence)
                .severity(Severity.ERROR)
                .build();
            
            List<Block> blocks = Arrays.asList(paragraphBlock, tableBlock);
            
            // Add no paragraph blocks (violates min)
            // Add one table block (violates min)
            StructuralNode tableNode = mock(StructuralNode.class);
            context.trackBlock(tableBlock, tableNode);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(2, messages.size());
            
            // Check paragraph violation
            ValidationMessage paragraphMsg = messages.stream()
                .filter(m -> m.getMessage().contains("paragraph"))
                .findFirst()
                .orElse(null);
            assertNotNull(paragraphMsg);
            assertEquals(Severity.ERROR, paragraphMsg.getSeverity());
            assertEquals("0", paragraphMsg.getActualValue().orElse(null));
            
            // Check table violation
            ValidationMessage tableMsg = messages.stream()
                .filter(m -> m.getMessage().contains("table"))
                .findFirst()
                .orElse(null);
            assertNotNull(tableMsg);
            assertEquals(Severity.WARN, tableMsg.getSeverity());
            assertEquals("1", tableMsg.getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should handle blocks without names using type")
        void shouldHandleBlocksWithoutNamesUsingType() {
            // Given
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(1)
                .max(1)
                .severity(Severity.INFO)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add two occurrences (violates max)
            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            context.trackBlock(block, node1);
            context.trackBlock(block, node2);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertTrue(msg.getMessage().contains("paragraph")); // Uses type name
        }
    }
    
    @Nested
    @DisplayName("edge cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle exact occurrence count")
        void shouldHandleExactOccurrenceCount() {
            // Given - exactly 3 occurrences required
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(3)
                .max(3)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .name("section")
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Test with exactly 3 occurrences
            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);
            context.trackBlock(block, node1);
            context.trackBlock(block, node2);
            context.trackBlock(block, node3);
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should validate when both min and max violations occur")
        void shouldValidateWhenBothMinAndMaxViolationsOccur() {
            // Given - impossible constraint (min > max)
            OccurrenceConfig occurrenceConfig = OccurrenceConfig.builder()
                .min(5)
                .max(3)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock block = ParagraphBlock.builder()
                .occurrence(occurrenceConfig)
                .severity(Severity.ERROR)
                .build();
            List<Block> blocks = Arrays.asList(block);
            
            // Add 4 occurrences
            for (int i = 0; i < 4; i++) {
                StructuralNode node = mock(StructuralNode.class);
                context.trackBlock(block, node);
            }
            
            // When
            List<ValidationMessage> messages = validator.validate(context, blocks);
            
            // Then
            assertEquals(2, messages.size()); // Both min and max violations
            assertTrue(messages.stream().anyMatch(m -> m.getRuleId().equals("block.occurrence.min")));
            assertTrue(messages.stream().anyMatch(m -> m.getRuleId().equals("block.occurrence.max")));
        }
    }
}