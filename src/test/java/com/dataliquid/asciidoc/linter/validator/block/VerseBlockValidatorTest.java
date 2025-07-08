package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Unit tests for {@link VerseBlockValidator}.
 * 
 * <p>This test class validates the behavior of the verse block validator,
 * which processes verse/quote blocks in AsciiDoc documents. The tests cover
 * validation rules for author information, source attribution, and verse
 * content.</p>
 * 
 * <p>Test structure follows a nested class pattern for better organization:</p>
 * <ul>
 *   <li>Validate - Basic validator functionality and type checking</li>
 *   <li>AuthorValidation - Author requirements and pattern matching</li>
 *   <li>AttributionValidation - Source attribution validation</li>
 *   <li>ContentValidation - Verse content length constraints</li>
 *   <li>SeverityHierarchy - Block-level severity usage (no nested severity support)</li>
 *   <li>ComplexScenarios - Combined validation scenarios and edge cases</li>
 * </ul>
 * 
 * <p>The validator supports various AsciiDoc attributes for verse blocks:</p>
 * <ul>
 *   <li>author - The author of the verse/quote</li>
 *   <li>attribution/citetitle - The source or title of the work</li>
 *   <li>content - The actual verse or quote text</li>
 * </ul>
 * 
 * <p>Note: Like ImageBlock, VerseBlock configurations do not support
 * individual severity levels for nested rules. All validations use
 * the block-level severity.</p>
 * 
 * @see VerseBlockValidator
 * @see VerseBlock
 */
@DisplayName("VerseBlockValidator")
class VerseBlockValidatorTest {
    
    private VerseBlockValidator validator;
    private BlockValidationContext context;
    private Block mockBlock;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new VerseBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(Block.class);
    }
    
    @Test
    @DisplayName("should return VERSE as supported type")
    void shouldReturnVerseAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.VERSE, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when block is not Block instance")
        void shouldReturnEmptyListWhenNotBlockInstance() {
            // Given
            StructuralNode notABlock = mock(StructuralNode.class);
            VerseBlock config = VerseBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(notABlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            VerseBlock config = VerseBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("author validation")
    class AuthorValidation {
        
        @Test
        @DisplayName("should validate required author")
        void shouldValidateRequiredAuthor() {
            // Given
            VerseBlock.AuthorConfig authorConfig = VerseBlock.AuthorConfig.builder()
                .required(true)
                .build();
            VerseBlock config = VerseBlock.builder()
                .author(authorConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("attribution")).thenReturn(false);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("verse.author.required", msg.getRuleId());
            assertEquals("Verse block must have an author", msg.getMessage());
            assertEquals("No author", msg.getActualValue().orElse(null));
            assertEquals("Author required", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate author pattern")
        void shouldValidateAuthorPattern() {
            // Given
            VerseBlock.AuthorConfig authorConfig = VerseBlock.AuthorConfig.builder()
                .pattern(Pattern.compile("^[A-Z][a-z]+ [A-Z][a-z]+$"))
                .build();
            VerseBlock config = VerseBlock.builder()
                .author(authorConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("author")).thenReturn(true);
            when(mockBlock.getAttribute("author")).thenReturn("john doe");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("verse.author.pattern", msg.getRuleId());
            assertEquals("Verse author does not match required pattern", msg.getMessage());
            assertEquals("john doe", msg.getActualValue().orElse(null));
            assertEquals("Pattern: ^[A-Z][a-z]+ [A-Z][a-z]+$", msg.getExpectedValue().orElse(null));
        }
        
        
        @Test
        @DisplayName("should pass when author matches pattern")
        void shouldPassWhenAuthorMatchesPattern() {
            // Given
            VerseBlock.AuthorConfig authorConfig = VerseBlock.AuthorConfig.builder()
                .pattern(Pattern.compile("^[A-Z][a-z]+ [A-Z][a-z]+$"))
                .build();
            VerseBlock config = VerseBlock.builder()
                .author(authorConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("attribution")).thenReturn(true);
            when(mockBlock.getAttribute("attribution")).thenReturn("John Doe");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("attribution validation")
    class AttributionValidation {
        
        @Test
        @DisplayName("should validate required attribution")
        void shouldValidateRequiredAttribution() {
            // Given
            VerseBlock.AttributionConfig attributionConfig = VerseBlock.AttributionConfig.builder()
                .required(true)
                .build();
            VerseBlock config = VerseBlock.builder()
                .attribution(attributionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("citetitle")).thenReturn(false);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("verse.attribution.required", msg.getRuleId());
            assertEquals("Verse block must have an attribution", msg.getMessage());
            assertEquals("No attribution", msg.getActualValue().orElse(null));
            assertEquals("Attribution required", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate attribution pattern")
        void shouldValidateAttributionPattern() {
            // Given
            VerseBlock.AttributionConfig attributionConfig = VerseBlock.AttributionConfig.builder()
                .pattern(Pattern.compile(".*\\(\\d{4}\\)$"))
                .build();
            VerseBlock config = VerseBlock.builder()
                .attribution(attributionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("citetitle")).thenReturn(true);
            when(mockBlock.getAttribute("citetitle")).thenReturn("Book Title");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("verse.attribution.pattern", msg.getRuleId());
            assertEquals("Verse attribution does not match required pattern", msg.getMessage());
            assertEquals("Book Title", msg.getActualValue().orElse(null));
        }
        
    }
    
    @Nested
    @DisplayName("content validation")
    class ContentValidation {
        
        @Test
        @DisplayName("should validate minimum content length")
        void shouldValidateMinimumContentLength() {
            // Given
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .minLength(20)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("Short verse");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("verse.content.minLength", msg.getRuleId());
            assertEquals("Verse content is too short", msg.getMessage());
            assertEquals("11 characters", msg.getActualValue().orElse(null));
            assertEquals("At least 20 characters", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum content length")
        void shouldValidateMaximumContentLength() {
            // Given
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .maxLength(50)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR)
                .build();
            
            // Create long content
            String longContent = "This is a very long verse that exceeds the maximum length allowed";
            when(mockBlock.getContent()).thenReturn(longContent);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("verse.content.maxLength", msg.getRuleId());
            assertEquals("Verse content is too long", msg.getMessage());
            assertEquals("65 characters", msg.getActualValue().orElse(null));
            assertEquals("At most 50 characters", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should pass when content length is within bounds")
        void shouldPassWhenContentLengthIsWithinBounds() {
            // Given
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .minLength(10)
                .maxLength(100)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("This is a perfect verse");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("severity hierarchy")
    class SeverityHierarchy {
        
        @Test
        @DisplayName("should always use block severity for author validation")
        void shouldAlwaysUseBlockSeverityForAuthor() {
            // Given - VerseBlock.AuthorConfig has no severity field
            VerseBlock.AuthorConfig authorConfig = VerseBlock.AuthorConfig.builder()
                .required(true)
                .build();
            VerseBlock config = VerseBlock.builder()
                .author(authorConfig)
                .severity(Severity.WARN) // Block severity
                .build();
            
            when(mockBlock.hasAttribute("attribution")).thenReturn(false);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(), 
                "Should use block severity (WARN) since AuthorConfig has no severity field");
            assertEquals("verse.author.required", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should always use block severity for attribution validation")
        void shouldAlwaysUseBlockSeverityForAttribution() {
            // Given - VerseBlock.AttributionConfig has no severity field
            VerseBlock.AttributionConfig attributionConfig = VerseBlock.AttributionConfig.builder()
                .required(true)
                .build();
            VerseBlock config = VerseBlock.builder()
                .attribution(attributionConfig)
                .severity(Severity.INFO) // Block severity
                .build();
            
            when(mockBlock.hasAttribute("citetitle")).thenReturn(false);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(), 
                "Should use block severity (INFO) since AttributionConfig has no severity field");
            assertEquals("verse.attribution.required", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should always use block severity for content validation")
        void shouldAlwaysUseBlockSeverityForContent() {
            // Given - VerseBlock.ContentConfig has no severity field
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .minLength(10)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR) // Block severity
                .build();
            
            when(mockBlock.getContent()).thenReturn("Short");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity(), 
                "Should use block severity (ERROR) since ContentConfig has no severity field");
            assertEquals("verse.content.minLength", msg.getRuleId());
        }
    }
    
    @Nested
    @DisplayName("complex validation scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRules() {
            // Given
            VerseBlock config = VerseBlock.builder()
                .author(VerseBlock.AuthorConfig.builder()
                    .required(true)
                    .pattern(Pattern.compile("^[A-Z].*"))
                    .build())
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("author")).thenReturn(true);
            when(mockBlock.getAttribute("author")).thenReturn("anonymous"); // Invalid pattern
            when(mockBlock.hasAttribute("citetitle")).thenReturn(false); // Missing
            when(mockBlock.getContent()).thenReturn("Short verse");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.stream().anyMatch(m -> "verse.author.pattern".equals(m.getRuleId())));
        }
        
        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            // Given
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .minLength(1)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("0 characters", messages.get(0).getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should handle null content")
        void shouldHandleNullContent() {
            // Given
            VerseBlock.ContentConfig contentConfig = VerseBlock.ContentConfig.builder()
                .minLength(1)
                .build();
            VerseBlock config = VerseBlock.builder()
                .content(contentConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("0 characters", messages.get(0).getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate all attributes when present")
        void shouldValidateAllAttributesWhenPresent() {
            // Given
            VerseBlock config = VerseBlock.builder()
                .author(VerseBlock.AuthorConfig.builder()
                    .required(true)
                    .pattern(Pattern.compile("^[A-Z][a-z]+ [A-Z][a-z]+$"))
                    .build())
                .attribution(VerseBlock.AttributionConfig.builder()
                    .required(true)
                    .pattern(Pattern.compile(".*\\(\\d{4}\\)$"))
                    .build())
                .content(VerseBlock.ContentConfig.builder()
                    .minLength(10)
                    .maxLength(200)
                    .build())
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("author")).thenReturn(true);
            when(mockBlock.getAttribute("author")).thenReturn("William Shakespeare");
            when(mockBlock.hasAttribute("attribution")).thenReturn(true);
            when(mockBlock.getAttribute("attribution")).thenReturn("Hamlet (1603)");
            when(mockBlock.getContent()).thenReturn("To be, or not to be,\nthat is the question");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty()); // All validations pass
        }
    }
}