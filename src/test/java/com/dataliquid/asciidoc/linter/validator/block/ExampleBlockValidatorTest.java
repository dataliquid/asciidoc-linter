package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("ExampleBlockValidator")
class ExampleBlockValidatorTest {

    private ExampleBlockValidator validator;
    private StructuralNode mockNode;
    private BlockValidationContext mockContext;
    private SourceLocation mockLocation;

    @BeforeEach
    void setUp() {
        validator = new ExampleBlockValidator();
        mockNode = mock(StructuralNode.class);
        mockContext = mock(BlockValidationContext.class);
        mockLocation = mock(SourceLocation.class);
        when(mockContext.createLocation(any())).thenReturn(mockLocation);
        when(mockContext.getFilename()).thenReturn("test.adoc");
    }

    @Test
    @DisplayName("should return EXAMPLE as supported type")
    void shouldReturnExampleAsSupportedType() {
        assertEquals(BlockType.EXAMPLE, validator.getSupportedType());
    }

    @Test
    @DisplayName("should return empty list for invalid block config type")
    void shouldReturnEmptyListForInvalidBlockConfigType() {
        // Given
        var invalidBlock = mock(com.dataliquid.asciidoc.linter.config.blocks.Block.class);

        // When
        var messages = validator.validate(mockNode, invalidBlock, mockContext);

        // Then
        assertTrue(messages.isEmpty());
    }

    @Nested
    @DisplayName("Caption Validation")
    class CaptionValidation {

        @Test
        @DisplayName("should pass when caption is not configured")
        void shouldPassWhenCaptionIsNotConfigured() {
            // Given
            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should fail when caption is required but missing")
        void shouldFailWhenCaptionIsRequiredButMissing() {
            // Given
            when(mockNode.getTitle()).thenReturn(null);

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(true, null, null, null,
                    Severity.ERROR);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals("Example block requires a caption", messages.get(0).getMessage());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should pass when caption matches pattern")
        void shouldPassWhenCaptionMatchesPattern() {
            // Given
            when(mockNode.getTitle()).thenReturn("Example 1.2: Basic usage");

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(true,
                    "^(Example|Beispiel)\\s+\\d+\\.\\d*:.*", 15, 100, Severity.ERROR);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should fail when caption does not match pattern")
        void shouldFailWhenCaptionDoesNotMatchPattern() {
            // Given
            when(mockNode.getTitle()).thenReturn("Invalid caption format");

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(false,
                    "^(Example|Beispiel)\\s+\\d+\\.\\d*:.*", null, null, Severity.ERROR);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("does not match required pattern"));
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should fail when caption is too short")
        void shouldFailWhenCaptionIsTooShort() {
            // Given
            when(mockNode.getTitle()).thenReturn("Short");

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(false, null, 15, null,
                    Severity.WARN);

            ExampleBlock block = new ExampleBlock("test", Severity.ERROR, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("less than required minimum"));
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should fail when caption is too long")
        void shouldFailWhenCaptionIsTooLong() {
            // Given
            String longCaption = "A".repeat(101);
            when(mockNode.getTitle()).thenReturn(longCaption);

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(false, null, null, 100, null);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("exceeds maximum"));
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should use block severity when caption severity is not specified")
        void shouldUseBlockSeverityWhenCaptionSeverityNotSpecified() {
            // Given
            when(mockNode.getTitle()).thenReturn(null);

            ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(true, null, null, null, null);

            ExampleBlock block = new ExampleBlock("test", Severity.INFO, null, null, captionConfig, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }
    }

    @Nested
    @DisplayName("Collapsible Validation")
    class CollapsibleValidation {

        @Test
        @DisplayName("should pass when collapsible is not configured")
        void shouldPassWhenCollapsibleIsNotConfigured() {
            // Given
            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, null);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should fail when collapsible is required but missing")
        void shouldFailWhenCollapsibleIsRequiredButMissing() {
            // Given
            when(mockNode.getAttribute("collapsible-option")).thenReturn(null);
            when(mockNode.getAttribute("collapsible")).thenReturn(null);

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(true, null,
                    Severity.ERROR);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals("Example block requires a collapsible attribute", messages.get(0).getMessage());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should pass when collapsible value is allowed")
        void shouldPassWhenCollapsibleValueIsAllowed() {
            // Given
            when(mockNode.getAttribute("collapsible-option")).thenReturn(true);

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(false,
                    Arrays.asList(true, false), null);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should handle string boolean values")
        void shouldHandleStringBooleanValues() {
            // Given
            when(mockNode.getAttribute("collapsible-option")).thenReturn("true");

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(false,
                    Arrays.asList(true, false), null);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should handle alternative string boolean values")
        void shouldHandleAlternativeStringBooleanValues() {
            // Given
            when(mockNode.getAttribute("collapsible")).thenReturn("yes");

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(false,
                    Arrays.asList(true, false), null);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should fail when collapsible value is not allowed")
        void shouldFailWhenCollapsibleValueIsNotAllowed() {
            // Given
            when(mockNode.getAttribute("collapsible-option")).thenReturn("invalid");

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(false,
                    Arrays.asList(true, false), Severity.INFO);

            ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("is not in allowed values"));
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }

        @Test
        @DisplayName("should use block severity when collapsible severity is not specified")
        void shouldUseBlockSeverityWhenCollapsibleSeverityNotSpecified() {
            // Given
            when(mockNode.getAttribute("collapsible-option")).thenReturn(null);

            ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(true, null, null);

            ExampleBlock block = new ExampleBlock("test", Severity.ERROR, null, null, null, collapsibleConfig);

            // When
            List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }
    }

    @Test
    @DisplayName("should validate both caption and collapsible")
    void shouldValidateBothCaptionAndCollapsible() {
        // Given
        when(mockNode.getTitle()).thenReturn(null);
        when(mockNode.getAttribute("collapsible-option")).thenReturn(null);

        ExampleBlock.CaptionConfig captionConfig = new ExampleBlock.CaptionConfig(true, null, null, null, null);

        ExampleBlock.CollapsibleConfig collapsibleConfig = new ExampleBlock.CollapsibleConfig(true, null, null);

        ExampleBlock block = new ExampleBlock("test", Severity.WARN, null, null, captionConfig, collapsibleConfig);

        // When
        List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

        // Then
        assertEquals(2, messages.size());
        assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("caption")));
        assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("collapsible")));
    }
}
