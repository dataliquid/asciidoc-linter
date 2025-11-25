package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.ContentConfig;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.ReasonConfig;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock.TypeConfig;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

class PassBlockValidatorTest {

    private PassBlockValidator validator;

    @Mock
    private StructuralNode mockBlock;

    @Mock
    private BlockValidationContext mockContext;

    private SourceLocation mockLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new PassBlockValidator();

        // Mock context filename
        when(mockContext.getFilename()).thenReturn("test.adoc");

        // Create a proper location object instead of mocking it
        mockLocation = SourceLocation.builder().filename("test.adoc").startLine(10).build();

        // Default setup
        when(mockContext.createLocation(any())).thenReturn(mockLocation);
        when(mockContext.createLocation(any(), anyInt(), anyInt())).thenReturn(mockLocation);
        when(mockBlock.getContent()).thenReturn("<div>Test content</div>");
        when(mockBlock.getSourceLocation()).thenReturn(null);
    }

    @Test
    @DisplayName("should return PASS as supported type")
    void shouldReturnPassAsSupportedType() {
        assertEquals(BlockType.PASS, validator.getSupportedType());
    }

    @Nested
    @DisplayName("Type Validation")
    class TypeValidationTests {

        @Test
        @DisplayName("should validate required type when missing")
        void shouldValidateRequiredTypeWhenMissing() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn(null);

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .type(TypeConfig.builder().required(true).severity(Severity.ERROR).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.type.required", message.getRuleId());
            assertEquals("Pass block requires a type", message.getMessage());
        }

        @Test
        @DisplayName("should validate allowed types")
        void shouldValidateAllowedTypes() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn("javascript");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("html", "xml", "svg"))
                            .severity(Severity.WARN)
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.type.allowed", message.getRuleId());
            assertEquals("javascript", message.getActualValue().orElse(null));
            assertTrue(message.getExpectedValue().orElse("").contains("html, xml, svg"));
        }

        @Test
        @DisplayName("should pass when type is valid")
        void shouldPassWhenTypeIsValid() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn("html");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("html", "xml", "svg"))
                            .severity(Severity.ERROR)
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should use block severity when type severity is null")
        void shouldUseBlockSeverityWhenTypeSeverityIsNull() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn(null);

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.WARN)
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .severity(null) // No severity specified
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
    }

    @Nested
    @DisplayName("Content Validation")
    class ContentValidationTests {

        @Test
        @DisplayName("should validate required content when missing")
        void shouldValidateRequiredContentWhenMissing() {
            // Given
            when(mockBlock.getContent()).thenReturn("");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .content(ContentConfig.builder().required(true).severity(Severity.ERROR).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.content.required", message.getRuleId());
        }

        @Test
        @DisplayName("should validate max length")
        void shouldValidateMaxLength() {
            // Given
            String longContent = "x".repeat(100);
            when(mockBlock.getContent()).thenReturn(longContent);

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .content(ContentConfig.builder().maxLength(50).severity(Severity.WARN).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.content.maxLength", message.getRuleId());
            assertEquals("100 characters", message.getActualValue().orElse(null));
            assertEquals("Maximum 50 characters", message.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate content pattern")
        void shouldValidateContentPattern() {
            // Given
            when(mockBlock.getContent()).thenReturn("<script>alert('bad')</script>");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .content(ContentConfig.builder().pattern("^<[^>]+>.*</[^>]+>$").severity(Severity.ERROR).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertTrue(messages.isEmpty()); // Pattern matches
        }

        @Test
        @DisplayName("should fail when content does not match pattern")
        void shouldFailWhenContentDoesNotMatchPattern() {
            // Given
            when(mockBlock.getContent()).thenReturn("plain text");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .content(ContentConfig.builder().pattern("^<[^>]+>.*</[^>]+>$").severity(Severity.ERROR).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            assertEquals("pass.content.pattern", messages.get(0).getRuleId());
        }
    }

    @Nested
    @DisplayName("Reason Validation")
    class ReasonValidationTests {

        @Test
        @DisplayName("should validate required reason when missing")
        void shouldValidateRequiredReasonWhenMissing() {
            // Given
            when(mockBlock.getAttribute("reason")).thenReturn(null);

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .reason(ReasonConfig.builder().required(true).severity(Severity.ERROR).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.reason.required", message.getRuleId());
            assertEquals("Pass block requires a reason", message.getMessage());
        }

        @Test
        @DisplayName("should validate min length of reason")
        void shouldValidateMinLengthOfReason() {
            // Given
            when(mockBlock.getAttribute("reason")).thenReturn("Too short");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .reason(ReasonConfig.builder().required(true).minLength(20).severity(Severity.WARN).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.reason.minLength", message.getRuleId());
            assertEquals("9 characters", message.getActualValue().orElse(null));
            assertEquals("At least 20 characters", message.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate max length of reason")
        void shouldValidateMaxLengthOfReason() {
            // Given
            String longReason = "This is a very long reason that exceeds the maximum allowed length";
            when(mockBlock.getAttribute("reason")).thenReturn(longReason);

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .reason(ReasonConfig.builder().required(true).maxLength(50).severity(Severity.INFO).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.INFO, message.getSeverity());
            assertEquals("pass.reason.maxLength", message.getRuleId());
        }

        @Test
        @DisplayName("should pass when reason is valid")
        void shouldPassWhenReasonIsValid() {
            // Given
            when(mockBlock.getAttribute("reason")).thenReturn("Custom widget for product gallery display");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .reason(ReasonConfig
                            .builder()
                            .required(true)
                            .minLength(20)
                            .maxLength(200)
                            .severity(Severity.ERROR)
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should validate all rules together")
        void shouldValidateAllRulesTogether() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn("html");
            when(mockBlock.getAttribute("reason")).thenReturn("Custom widget for product gallery display");
            when(mockBlock.getContent()).thenReturn("<div class=\"product-slider\">Content</div>");

            PassBlock config = PassBlock
                    .builder()
                    .name("Passthrough Block")
                    .severity(Severity.ERROR)
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("html", "xml", "svg"))
                            .severity(Severity.ERROR)
                            .build())
                    .content(ContentConfig
                            .builder()
                            .required(true)
                            .maxLength(1000)
                            .pattern("^<[^>]+>.*</[^>]+>$")
                            .severity(Severity.ERROR)
                            .build())
                    .reason(ReasonConfig
                            .builder()
                            .required(true)
                            .minLength(20)
                            .maxLength(200)
                            .severity(Severity.ERROR)
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should collect multiple validation errors")
        void shouldCollectMultipleValidationErrors() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn(null);
            when(mockBlock.getAttribute("reason")).thenReturn("Short");
            when(mockBlock.getContent()).thenReturn("");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR)
                    .type(TypeConfig.builder().required(true).severity(Severity.ERROR).build())
                    .content(ContentConfig.builder().required(true).severity(Severity.WARN).build())
                    .reason(ReasonConfig.builder().required(true).minLength(20).severity(Severity.INFO).build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(3, messages.size());

            // Verify different severities
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR));
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.WARN));
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.INFO));
        }
    }

    @Nested
    @DisplayName("Severity Hierarchy Tests")
    class SeverityHierarchyTests {

        @Test
        @DisplayName("should use nested severity when specified for all configs")
        void shouldUseNestedSeverityWhenSpecified() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn("javascript");
            when(mockBlock.getAttribute("reason")).thenReturn("Short");
            when(mockBlock.getContent()).thenReturn("x".repeat(100));

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.ERROR) // Block severity
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("html", "xml", "svg"))
                            .severity(Severity.INFO) // Override with INFO
                            .build())
                    .content(ContentConfig
                            .builder()
                            .maxLength(50)
                            .severity(Severity.WARN) // Override with WARN
                            .build())
                    .reason(ReasonConfig
                            .builder()
                            .minLength(20)
                            .severity(Severity.ERROR) // Keep ERROR
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(3, messages.size());

            // Type validation should use INFO
            ValidationMessage typeMessage = messages
                    .stream()
                    .filter(m -> m.getRuleId().equals("pass.type.allowed"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Severity.INFO, typeMessage.getSeverity());

            // Content validation should use WARN
            ValidationMessage contentMessage = messages
                    .stream()
                    .filter(m -> m.getRuleId().equals("pass.content.maxLength"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Severity.WARN, contentMessage.getSeverity());

            // Reason validation should use ERROR
            ValidationMessage reasonMessage = messages
                    .stream()
                    .filter(m -> m.getRuleId().equals("pass.reason.minLength"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Severity.ERROR, reasonMessage.getSeverity());
        }

        @Test
        @DisplayName("should fallback to block severity when nested severity is null")
        void shouldFallbackToBlockSeverityWhenNull() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn(null);
            when(mockBlock.getAttribute("reason")).thenReturn(null);
            when(mockBlock.getContent()).thenReturn("");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.WARN) // Block severity
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .severity(null) // No severity specified
                            .build())
                    .content(ContentConfig
                            .builder()
                            .required(true)
                            .severity(null) // No severity specified
                            .build())
                    .reason(ReasonConfig
                            .builder()
                            .required(true)
                            .severity(null) // No severity specified
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(3, messages.size());

            // All messages should use block severity (WARN)
            assertTrue(messages.stream().allMatch(m -> m.getSeverity() == Severity.WARN));
        }

        @Test
        @DisplayName("should handle mixed severity configurations")
        void shouldHandleMixedSeverityConfigurations() {
            // Given
            when(mockBlock.getAttribute("type")).thenReturn("invalid");
            when(mockBlock.getAttribute("reason")).thenReturn("Valid reason for using pass block");
            when(mockBlock.getContent()).thenReturn("<div>Valid content</div>");

            PassBlock config = PassBlock
                    .builder()
                    .severity(Severity.INFO) // Block severity
                    .type(TypeConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("html", "xml"))
                            .severity(Severity.ERROR) // Override type severity
                            .build())
                    .content(ContentConfig
                            .builder()
                            .required(true)
                            .severity(null) // Use block severity
                            .build())
                    .reason(ReasonConfig
                            .builder()
                            .required(true)
                            .severity(null) // Use block severity
                            .build())
                    .build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);

            // Then
            assertEquals(1, messages.size()); // Only type validation fails

            ValidationMessage message = messages.get(0);
            assertEquals("pass.type.allowed", message.getRuleId());
            assertEquals(Severity.ERROR, message.getSeverity()); // Uses nested severity
        }
    }
}
