package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("VideoBlockValidator")
class VideoBlockValidatorTest {
    
    private VideoBlockValidator validator;
    
    @Mock
    private StructuralNode node;
    
    @Mock
    private org.asciidoctor.ast.Cursor sourceLocation;
    
    @Mock
    private BlockValidationContext context;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new VideoBlockValidator();
        when(node.getSourceLocation()).thenReturn(sourceLocation);
        when(sourceLocation.getLineNumber()).thenReturn(10);
        when(sourceLocation.getFile()).thenReturn("test.adoc");
        
        // Mock context methods
        when(context.getFilename()).thenReturn("test.adoc");
        
        // Mock context.createLocation()
        SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .startLine(10)
                .build();
        when(context.createLocation(any(StructuralNode.class))).thenReturn(location);
    }
    
    @Test
    @DisplayName("should return VIDEO block type")
    void shouldReturnVideoBlockType() {
        assertEquals(BlockType.VIDEO, validator.getSupportedType());
    }
    
    @Test
    @DisplayName("should return empty list for non-VideoBlock config")
    void shouldReturnEmptyListForNonVideoBlockConfig() {
        // When
        var messages = validator.validate(node, ParagraphBlock.builder().name("test").severity(Severity.WARN).build(), context);
        
        // Then
        assertTrue(messages.isEmpty());
    }
    
    @Nested
    @DisplayName("URL Validation")
    class UrlValidation {
        
        @Test
        @DisplayName("should report error when required URL is missing")
        void shouldReportErrorWhenRequiredUrlMissing() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .severity(Severity.ERROR)
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("Video URL is required but not provided", msg.getMessage());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("target", msg.getMissingValueHint());
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should report error when URL doesn't match pattern")
        void shouldReportErrorWhenUrlDoesntMatchPattern() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .pattern("^https?://.*\\.(mp4|webm)$")
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn("https://dataliquid.com/video.avi");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("Video URL does not match required pattern", msg.getMessage());
            assertEquals(ErrorType.INVALID_PATTERN, msg.getErrorType());
            assertEquals("https://dataliquid.com/video.avi", msg.getActualValue().orElse(null));
            assertEquals("^https?://.*\\.(mp4|webm)$", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should use nested severity when available")
        void shouldUseNestedSeverityWhenAvailable() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .severity(Severity.INFO)
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }
    }
    
    @Nested
    @DisplayName("Dimension Validation")
    class DimensionValidation {
        
        @Test
        @DisplayName("should report error when width is below minimum")
        void shouldReportErrorWhenWidthBelowMinimum() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .maxValue(1920)
                            .build())
                    .build();
            
            when(node.getAttribute("width")).thenReturn("200");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video width is below minimum value", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("200", msg.getActualValue().orElse(null));
            assertEquals(">= 320", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should report error when height exceeds maximum")
        void shouldReportErrorWhenHeightExceedsMaximum() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.ERROR)
                    .height(VideoBlock.DimensionConfig.builder()
                            .maxValue(1080)
                            .severity(Severity.INFO)
                            .build())
                    .build();
            
            when(node.getAttribute("height")).thenReturn("2000");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("Video height exceeds maximum value", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("2000", msg.getActualValue().orElse(null));
            assertEquals("<= 1080", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should report error for invalid dimension value")
        void shouldReportErrorForInvalidDimensionValue() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.ERROR)
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .build())
                    .build();
            
            when(node.getAttribute("width")).thenReturn("invalid");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video width is not a valid number", msg.getMessage());
            assertEquals(ErrorType.INVALID_PATTERN, msg.getErrorType());
            assertEquals("invalid", msg.getActualValue().orElse(null));
            assertEquals("numeric value", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
    }
    
    @Nested
    @DisplayName("Poster Validation")
    class PosterValidation {
        
        @Test
        @DisplayName("should report error when poster doesn't match pattern")
        void shouldReportErrorWhenPosterDoesntMatchPattern() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .poster(VideoBlock.PosterConfig.builder()
                            .pattern(".*\\.(jpg|jpeg|png)$")
                            .build())
                    .build();
            
            when(node.getAttribute("poster")).thenReturn("poster.gif");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video poster does not match required pattern", msg.getMessage());
            assertEquals(ErrorType.INVALID_PATTERN, msg.getErrorType());
            assertEquals("poster.gif", msg.getActualValue().orElse(null));
            assertEquals(".*\\.(jpg|jpeg|png)$", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
    }
    
    @Nested
    @DisplayName("Controls Validation")
    class ControlsValidation {
        
        @Test
        @DisplayName("should report error when controls are required but not enabled")
        void shouldReportErrorWhenControlsRequiredButNotEnabled() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .options(VideoBlock.OptionsConfig.builder()
                            .controls(VideoBlock.ControlsConfig.builder()
                                    .required(true)
                                    .severity(Severity.ERROR)
                                    .build())
                            .build())
                    .build();
            
            when(node.getAttribute("options")).thenReturn("autoplay");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("Video controls are required but not enabled", msg.getMessage());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("controls", msg.getMissingValueHint());
            assertEquals("autoplay", msg.getActualValue().orElse(null));
            assertEquals("controls", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should pass when controls are enabled")
        void shouldPassWhenControlsAreEnabled() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .options(VideoBlock.OptionsConfig.builder()
                            .controls(VideoBlock.ControlsConfig.builder()
                                    .required(true)
                                    .build())
                            .build())
                    .build();
            
            when(node.getAttribute("options")).thenReturn("controls,loop");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Caption Validation")
    class CaptionValidation {
        
        @Test
        @DisplayName("should report error when caption is too short")
        void shouldReportErrorWhenCaptionTooShort() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .minLength(15)
                            .severity(Severity.WARN)
                            .build())
                    .build();
            
            when(node.getAttribute("caption")).thenReturn("Short");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("Video caption is too short", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("5 characters", msg.getActualValue().orElse(null));
            assertEquals(">= 15 characters", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should report error when caption is too long")
        void shouldReportErrorWhenCaptionTooLong() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .maxLength(50)
                            .build())
                    .build();
            
            String longCaption = "This is a very long caption that definitely exceeds the maximum allowed length";
            when(node.getAttribute("caption")).thenReturn(longCaption);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video caption is too long", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("78 characters", msg.getActualValue().orElse(null));
            assertEquals("<= 50 characters", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }
        
        @Test
        @DisplayName("should use title when caption attribute is null")
        void shouldUseTitleWhenCaptionAttributeIsNull() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            when(node.getAttribute("caption")).thenReturn(null);
            when(node.getTitle()).thenReturn("Video Title");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate all configured rules")
        void shouldValidateAllConfiguredRules() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .pattern("^https://.*\\.(mp4|webm)$")
                            .build())
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .maxValue(1920)
                            .build())
                    .caption(VideoBlock.CaptionConfig.builder()
                            .required(true)
                            .minLength(10)
                            .build())
                    .build();
            
            // Invalid URL
            when(node.getAttribute("target")).thenReturn("http://example.com/video.avi");
            // Valid width
            when(node.getAttribute("width")).thenReturn("800");
            // Missing caption
            when(node.getAttribute("caption")).thenReturn(null);
            when(node.getTitle()).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(2, messages.size());
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("URL")));
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("caption")));
        }
    }
}