package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
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
        SourceLocation location = SourceLocation.builder().filename("test.adoc").startLine(10).build();
        when(context.createLocation(any(StructuralNode.class))).thenReturn(location);
    }

    @Test
    void shouldReturnVideoBlockType() {
        assertEquals(BlockType.VIDEO, validator.getSupportedType());
    }

    @Test
    void shouldReturnEmptyListForNonVideoBlockConfig() {
        // given
        ParagraphBlock config = new ParagraphBlock("test", Severity.WARN, null, null, null, null, null, null);

        // when
        var messages = validator.validate(node, config, context);

        // then
        assertTrue(messages.isEmpty());
    }

    @Nested
    class UrlValidation {

        @Test
        void shouldReportErrorWhenRequiredUrlMissing() {
            // given
            VideoBlock.UrlConfig url = new VideoBlock.UrlConfig(true, null, Severity.ERROR);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, url, null, null, null, null, null);

            // when
            when(node.getAttribute("target")).thenReturn(null);
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("Video URL is required but not provided", msg.getMessage());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("target", msg.getMissingValueHint());
            assertTrue(msg.hasSuggestions());
        }

        @Test
        void shouldReportErrorWhenUrlDoesntMatchPattern() {
            // given
            VideoBlock.UrlConfig url = new VideoBlock.UrlConfig(null, "^https?://.*\\.(mp4|webm)$", null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, url, null, null, null, null, null);

            // when
            when(node.getAttribute("target")).thenReturn("https://dataliquid.com/video.avi");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
        void shouldUseNestedSeverityWhenAvailable() {
            // given
            VideoBlock.UrlConfig url = new VideoBlock.UrlConfig(true, null, Severity.INFO);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, url, null, null, null, null, null);

            // when
            when(node.getAttribute("target")).thenReturn(null);
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertEquals(1, messages.size());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }
    }

    @Nested
    class DimensionValidation {

        @Test
        void shouldReportErrorWhenWidthBelowMinimum() {
            // given
            VideoBlock.DimensionConfig width = new VideoBlock.DimensionConfig(null, 320, 1920, null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, width, null, null, null, null);

            // when
            when(node.getAttribute("width")).thenReturn("200");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video width is below minimum value", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("200", msg.getActualValue().orElse(null));
            assertEquals(">= 320", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }

        @Test
        void shouldReportErrorWhenHeightExceedsMaximum() {
            // given
            VideoBlock.DimensionConfig height = new VideoBlock.DimensionConfig(null, null, 1080, Severity.INFO);
            VideoBlock config = new VideoBlock("test", Severity.ERROR, null, null, null, null, height, null, null,
                    null);

            // when
            when(node.getAttribute("height")).thenReturn("2000");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
        void shouldReportErrorForInvalidDimensionValue() {
            // given
            VideoBlock.DimensionConfig width = new VideoBlock.DimensionConfig(null, 320, null, null);
            VideoBlock config = new VideoBlock("test", Severity.ERROR, null, null, null, width, null, null, null, null);

            // when
            when(node.getAttribute("width")).thenReturn("invalid");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
    class PosterValidation {

        @Test
        void shouldReportErrorWhenPosterDoesntMatchPattern() {
            // given
            VideoBlock.PosterConfig poster = new VideoBlock.PosterConfig(null, ".*\\.(jpg|jpeg|png)$", null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, poster, null, null);

            // when
            when(node.getAttribute("poster")).thenReturn("poster.gif");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
    class ControlsValidation {

        @Test
        void shouldReportErrorWhenControlsRequiredButNotEnabled() {
            // given
            VideoBlock.ControlsConfig controls = new VideoBlock.ControlsConfig(true, Severity.ERROR);
            VideoBlock.OptionsConfig options = new VideoBlock.OptionsConfig(controls);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, null, options,
                    null);

            // when
            when(node.getAttribute("options")).thenReturn("autoplay");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
        void shouldPassWhenControlsAreEnabled() {
            // given
            VideoBlock.ControlsConfig controls = new VideoBlock.ControlsConfig(true, null);
            VideoBlock.OptionsConfig options = new VideoBlock.OptionsConfig(controls);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, null, options,
                    null);

            // when
            when(node.getAttribute("options")).thenReturn("controls,loop");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    class CaptionValidation {

        @Test
        void shouldReportErrorWhenCaptionTooShort() {
            // given
            VideoBlock.CaptionConfig caption = new VideoBlock.CaptionConfig(null, 15, null, Severity.WARN);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, null, null,
                    caption);

            // when
            when(node.getAttribute("caption")).thenReturn("Short");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
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
        void shouldReportErrorWhenCaptionTooLong() {
            // given
            VideoBlock.CaptionConfig caption = new VideoBlock.CaptionConfig(null, null, 50, null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, null, null,
                    caption);

            // when
            String longCaption = "This is a very long caption that definitely exceeds the maximum allowed length";
            when(node.getAttribute("caption")).thenReturn(longCaption);
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("Video caption is too long", msg.getMessage());
            assertEquals(ErrorType.OUT_OF_RANGE, msg.getErrorType());
            assertEquals("78 characters", msg.getActualValue().orElse(null));
            assertEquals("<= 50 characters", msg.getExpectedValue().orElse(null));
            assertTrue(msg.hasSuggestions());
        }

        @Test
        void shouldUseTitleWhenCaptionAttributeIsNull() {
            // given
            VideoBlock.CaptionConfig caption = new VideoBlock.CaptionConfig(true, null, null, null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, null, null, null, null, null,
                    caption);

            // when
            when(node.getAttribute("caption")).thenReturn(null);
            when(node.getTitle()).thenReturn("Video Title");
            List<ValidationMessage> messages = validator.validate(node, config, context);

            // then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    class ComplexScenarios {

        @Test
        void shouldValidateAllConfiguredRules() {
            // given
            VideoBlock.UrlConfig url = new VideoBlock.UrlConfig(true, "^https://.*\\.(mp4|webm)$", null);
            VideoBlock.DimensionConfig width = new VideoBlock.DimensionConfig(null, 320, 1920, null);
            VideoBlock.CaptionConfig caption = new VideoBlock.CaptionConfig(true, 10, null, null);
            VideoBlock config = new VideoBlock("test", Severity.WARN, null, null, url, width, null, null, null,
                    caption);

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
