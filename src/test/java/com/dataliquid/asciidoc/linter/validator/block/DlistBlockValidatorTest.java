package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.DlistBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import org.asciidoctor.ast.Cursor;

/**
 * Unit tests for {@link DlistBlockValidator}.
 * 
 * <p>This test class validates the behavior of the definition list block validator,
 * which processes dlist blocks in AsciiDoc documents. The tests cover all
 * validation rules including term count, description validation, nesting level, 
 * and delimiter style.</p>
 * 
 * <p>Test structure follows a nested class pattern for better organization:</p>
 * <ul>
 *   <li>Validate - Basic validator functionality</li>
 *   <li>TermsValidation - Term count and pattern constraints</li>
 *   <li>DescriptionsValidation - Description presence and content</li>
 *   <li>NestingLevelValidation - Maximum nesting depth</li>
 *   <li>DelimiterStyleValidation - Delimiter consistency</li>
 *   <li>ComplexScenarios - Combined validation scenarios</li>
 * </ul>
 * 
 * @see DlistBlockValidator
 * @see DlistBlock
 */
@DisplayName("DlistBlockValidator")
class DlistBlockValidatorTest {
    
    private DlistBlockValidator validator;
    private BlockValidationContext context;
    private DescriptionList mockDlist;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new DlistBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockDlist = mock(DescriptionList.class);
        when(mockDlist.getContext()).thenReturn("dlist");
        when(mockDlist.getSourceLocation()).thenReturn(null);
    }
    
    @Test
    @DisplayName("should return DLIST as supported type")
    void shouldReturnDlistAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.DLIST, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when block is not DescriptionList instance")
        void shouldReturnEmptyListWhenNotDescriptionListInstance() {
            // Given
            Block notADlist = mock(Block.class);
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(notADlist, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            when(mockDlist.getItems()).thenReturn(Arrays.asList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("TermsValidation")
    class TermsValidation {
        
        @Test
        @DisplayName("should validate minimum term count")
        void shouldValidateMinimumTermCount() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("Term1", "Description1")
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .min(2)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("dlist.terms.min", message.getRuleId());
            assertEquals("Definition list has too few terms", message.getMessage());
            assertEquals("1", message.getActualValue().orElse(null));
            assertEquals("At least 2 terms", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum term count")
        void shouldValidateMaximumTermCount() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("Term1", "Desc1"),
                createMockEntry("Term2", "Desc2"),
                createMockEntry("Term3", "Desc3"),
                createMockEntry("Term4", "Desc4")
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .max(3)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("dlist.terms.max", message.getRuleId());
            assertEquals("Definition list has too many terms", message.getMessage());
            assertEquals("4", message.getActualValue().orElse(null));
            assertEquals("At most 3 terms", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate term pattern")
        void shouldValidateTermPattern() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("term", "Description"), // lowercase, should fail
                createMockEntry("Term", "Description")  // uppercase, should pass
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("dlist.terms.pattern", message.getRuleId());
            assertEquals("Definition list term does not match required pattern", message.getMessage());
            assertEquals("term", message.getActualValue().orElse(null));
            assertEquals("Pattern: ^[A-Z].*", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should highlight exact position for term pattern violation")
        void shouldHighlightExactPositionForTermPatternViolation() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");
            
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntryWithLocation("term without capital", "description", 53)
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(53);
            when(mockDlist.getSourceLocation()).thenReturn(mockCursor);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, testContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals("dlist.terms.pattern", message.getRuleId());
            assertEquals(Severity.WARN, message.getSeverity());
            
            // Since we don't have actual file content in tests, the validator falls back to columns 1,1
            assertEquals(1, message.getLocation().getStartColumn(), "Should point to start of term");
            assertEquals(1, message.getLocation().getEndColumn(), "Falls back to column 1 without file content");
            assertEquals(53, message.getLocation().getStartLine());
            assertEquals(53, message.getLocation().getEndLine());
        }
        
        @Test
        @DisplayName("should validate term length constraints")
        void shouldValidateTermLengthConstraints() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("AB", "Description"),     // too short
                createMockEntry("ABCDEF", "Description"), // ok
                createMockEntry("ABCDEFGHIJKLMNOP", "Description") // too long
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .minLength(3)
                    .maxLength(10)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(2, messages.size());
            
            // Check too short message
            ValidationMessage shortMessage = messages.stream()
                .filter(m -> m.getRuleId().equals("dlist.terms.minLength"))
                .findFirst().orElse(null);
            assertEquals("Definition list term is too short", shortMessage.getMessage());
            assertEquals("AB (length: 2)", shortMessage.getActualValue().orElse(null));
            
            // Check too long message
            ValidationMessage longMessage = messages.stream()
                .filter(m -> m.getRuleId().equals("dlist.terms.maxLength"))
                .findFirst().orElse(null);
            assertEquals("Definition list term is too long", longMessage.getMessage());
            assertTrue(longMessage.getActualValue().orElse("").contains("length: 16"));
        }
    }
    
    @Nested
    @DisplayName("DescriptionsValidation")
    class DescriptionsValidation {
        
        @Test
        @DisplayName("should validate required descriptions")
        void shouldValidateRequiredDescriptions() {
            // Given
            DescriptionListEntry entryWithoutDesc = createMockEntry("Term", null);
            List<DescriptionListEntry> entries = Arrays.asList(entryWithoutDesc);
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .descriptions(DlistBlock.DescriptionsConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("dlist.descriptions.required", message.getRuleId());
            assertEquals("Definition list term missing required description", message.getMessage());
            assertEquals("No description", message.getActualValue().orElse(null));
            assertEquals("Description required", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate description pattern")
        void shouldValidateDescriptionPattern() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("Term1", "Description without period"),
                createMockEntry("Term2", "Description with period.")
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .descriptions(DlistBlock.DescriptionsConfig.builder()
                    .pattern(".*\\.$")  // Must end with period
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("dlist.descriptions.pattern", message.getRuleId());
            assertEquals("Definition list description does not match required pattern", message.getMessage());
            assertEquals("Description without period", message.getActualValue().orElse(null));
            assertEquals("Pattern: .*\\.$", message.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("NestingLevelValidation")
    class NestingLevelValidation {
        
        @Test
        @DisplayName("should skip nesting level validation - not implemented")
        void shouldSkipNestingLevelValidation() {
            // Given
            // Create nested structure: grandParent -> parent -> mockDlist
            DescriptionList grandParent = mock(DescriptionList.class);
            when(grandParent.getContext()).thenReturn("dlist");
            when(grandParent.getParent()).thenReturn(null);
            
            DescriptionList parent = mock(DescriptionList.class);
            when(parent.getContext()).thenReturn("dlist");
            when(parent.getParent()).thenReturn(grandParent);
            
            when(mockDlist.getParent()).thenReturn(parent);
            when(mockDlist.getItems()).thenReturn(Arrays.asList());
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .nestingLevel(DlistBlock.NestingLevelConfig.builder()
                    .max(1)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then - nesting validation is not implemented
            assertEquals(0, messages.size());
        }
    }
    
    @Nested
    @DisplayName("DelimiterStyleValidation")
    class DelimiterStyleValidation {
        
        @Test
        @DisplayName("should skip delimiter validation - not implemented")
        void shouldSkipDelimiterValidation() {
            // Given
            when(mockDlist.getAttribute("delimiter")).thenReturn("::::");
            when(mockDlist.getItems()).thenReturn(Arrays.asList());
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .delimiterStyle(DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", ":::"})
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then - delimiter validation is not implemented
            assertEquals(0, messages.size());
        }
        
        @Test
        @DisplayName("should skip delimiter validation even for allowed delimiters")
        void shouldSkipDelimiterValidationForAllowed() {
            // Given
            when(mockDlist.getAttribute("delimiter")).thenReturn("::");
            when(mockDlist.getItems()).thenReturn(Arrays.asList());
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .delimiterStyle(DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", ":::"})
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("ComplexScenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRulesTogether() {
            // Given
            List<DescriptionListEntry> entries = Arrays.asList(
                createMockEntry("term", "Description"),  // lowercase term
                createMockEntry("Term", null)            // missing description
            );
            when(mockDlist.getItems()).thenReturn(entries);
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build())
                .descriptions(DlistBlock.DescriptionsConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(2, messages.size());
            
            // Verify we have one pattern violation and one required description violation
            assertTrue(messages.stream().anyMatch(m -> 
                m.getRuleId().equals("dlist.terms.pattern") && 
                m.getSeverity() == Severity.WARN));
            assertTrue(messages.stream().anyMatch(m -> 
                m.getRuleId().equals("dlist.descriptions.required") && 
                m.getSeverity() == Severity.ERROR));
        }
        
        @Test
        @DisplayName("should handle empty definition list")
        void shouldHandleEmptyDefinitionList() {
            // Given
            when(mockDlist.getItems()).thenReturn(Arrays.asList());
            
            DlistBlock config = DlistBlock.builder()
                .severity(Severity.ERROR)
                .terms(DlistBlock.TermsConfig.builder()
                    .min(1)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockDlist, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals("dlist.terms.min", message.getRuleId());
            assertEquals("0", message.getActualValue().orElse(null));
        }
    }
    
    // Helper method to create mock DescriptionListEntry
    private DescriptionListEntry createMockEntry(String term, String description) {
        DescriptionListEntry entry = mock(DescriptionListEntry.class);
        
        ListItem termItem = mock(ListItem.class);
        when(termItem.getText()).thenReturn(term);
        when(entry.getTerms()).thenReturn(Arrays.asList(termItem));
        
        if (description != null) {
            ListItem descItem = mock(ListItem.class);
            when(descItem.getText()).thenReturn(description);
            when(entry.getDescription()).thenReturn(descItem);
        } else {
            when(entry.getDescription()).thenReturn(null);
        }
        
        return entry;
    }
    
    // Helper method to create mock DescriptionListEntry with source location
    private DescriptionListEntry createMockEntryWithLocation(String term, String description, int lineNumber) {
        DescriptionListEntry entry = mock(DescriptionListEntry.class);
        
        ListItem termItem = mock(ListItem.class);
        when(termItem.getText()).thenReturn(term);
        
        // Add source location to termItem
        Cursor termCursor = mock(Cursor.class);
        when(termCursor.getLineNumber()).thenReturn(lineNumber);
        when(termItem.getSourceLocation()).thenReturn(termCursor);
        
        when(entry.getTerms()).thenReturn(Arrays.asList(termItem));
        
        if (description != null) {
            ListItem descItem = mock(ListItem.class);
            when(descItem.getText()).thenReturn(description);
            when(entry.getDescription()).thenReturn(descItem);
        } else {
            when(entry.getDescription()).thenReturn(null);
        }
        
        return entry;
    }
}