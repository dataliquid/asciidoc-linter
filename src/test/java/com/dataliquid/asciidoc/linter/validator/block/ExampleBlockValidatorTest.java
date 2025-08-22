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
            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).caption(captionConfig)
                    .build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^(Example|Beispiel)\\s+\\d+\\.\\d*:.*").minLength(15).maxLength(100)
                    .severity(Severity.ERROR).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).caption(captionConfig)
                    .build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder()
                    .pattern("^(Example|Beispiel)\\s+\\d+\\.\\d*:.*").severity(Severity.ERROR).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).caption(captionConfig)
                    .build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().minLength(15)
                    .severity(Severity.WARN).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.ERROR).caption(captionConfig)
                    .build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().maxLength(100).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).caption(captionConfig)
                    .build();

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

            ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().required(true).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.INFO).caption(captionConfig)
                    .build();

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
            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN)
                    .collapsible(collapsibleConfig).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder()
                    .allowed(Arrays.asList(true, false)).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN)
                    .collapsible(collapsibleConfig).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder()
                    .allowed(Arrays.asList(true, false)).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN)
                    .collapsible(collapsibleConfig).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder()
                    .allowed(Arrays.asList(true, false)).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN)
                    .collapsible(collapsibleConfig).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder()
                    .allowed(Arrays.asList(true, false)).severity(Severity.INFO).build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN)
                    .collapsible(collapsibleConfig).build();

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

            ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder().required(true)
                    .build();

            ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.ERROR)
                    .collapsible(collapsibleConfig).build();

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

        ExampleBlock.CaptionConfig captionConfig = ExampleBlock.CaptionConfig.builder().required(true).build();

        ExampleBlock.CollapsibleConfig collapsibleConfig = ExampleBlock.CollapsibleConfig.builder().required(true)
                .build();

        ExampleBlock block = ExampleBlock.builder().name("test").severity(Severity.WARN).caption(captionConfig)
                .collapsible(collapsibleConfig).build();

        // When
        List<ValidationMessage> messages = validator.validate(mockNode, block, mockContext);

        // Then
        assertEquals(2, messages.size());
        assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("caption")));
        assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("collapsible")));
    }
}
