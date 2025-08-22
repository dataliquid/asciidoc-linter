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

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import org.asciidoctor.ast.Cursor;

/**
 * Unit tests for {@link ImageBlockValidator}.
 *
 * <p>
 * This test class validates the behavior of the image block validator, which processes image elements in AsciiDoc
 * documents. The tests cover validation rules for image URLs, dimensions, and alternative text.
 * </p>
 *
 * <p>
 * Test structure follows a nested class pattern for better organization:
 * </p>
 * <ul>
 * <li>Validate - Basic validator functionality and type checking</li>
 * <li>UrlValidation - URL requirements and pattern matching</li>
 * <li>DimensionsValidation - Width and height constraints</li>
 * <li>AltTextValidation - Alternative text requirements and length constraints</li>
 * <li>SeverityHierarchy - Block-level severity usage (no nested severity support)</li>
 * <li>ComplexScenarios - Combined validation scenarios</li>
 * </ul>
 *
 * <p>
 * Note: Unlike other block validators, ImageBlock configurations do not support individual severity levels for nested
 * rules. All validations use the block-level severity.
 * </p>
 *
 * @see ImageBlockValidator
 * @see ImageBlock
 */
@DisplayName("ImageBlockValidator")
class ImageBlockValidatorTest {

    private ImageBlockValidator validator;
    private BlockValidationContext context;
    private Block mockBlock;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        validator = new ImageBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(Block.class);
    }

    @Test
    @DisplayName("should return IMAGE as supported type")
    void shouldReturnImageAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();

        // Then
        assertEquals(BlockType.IMAGE, type);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return empty list when block is not Image instance")
        void shouldReturnEmptyListWhenNotImageInstance() {
            // Given
            StructuralNode notAnImage = mock(StructuralNode.class);
            ImageBlock config = ImageBlock.builder().severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> messages = validator.validate(notAnImage, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            ImageBlock config = ImageBlock.builder().severity(Severity.ERROR).build();
            when(mockBlock.hasAttribute("target")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("URL validation")
    class UrlValidation {

        @Test
        @DisplayName("should validate required URL")
        void shouldValidateRequiredUrl() {
            // Given
            ImageBlock.UrlConfig urlConfig = ImageBlock.UrlConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().url(urlConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("target")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("image.url.required", msg.getRuleId());
            assertEquals("Image must have a URL", msg.getMessage());
            assertEquals("No URL", msg.getActualValue().orElse(null));
            assertEquals("URL required", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate URL pattern")
        void shouldValidateUrlPattern() {
            // Given
            ImageBlock.UrlConfig urlConfig = ImageBlock.UrlConfig.builder()
                    .pattern(Pattern.compile("^images/.*\\.png$")).build();
            ImageBlock config = ImageBlock.builder().url(urlConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("target")).thenReturn(true);
            when(mockBlock.getAttribute("target")).thenReturn("assets/logo.jpg");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("image.url.pattern", msg.getRuleId());
            assertEquals("Image URL does not match required pattern", msg.getMessage());
            assertEquals("assets/logo.jpg", msg.getActualValue().orElse(null));
            assertEquals("Pattern: ^images/.*\\.png$", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should include placeholder for missing URL")
        void shouldIncludePlaceholderForMissingUrl() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.UrlConfig urlConfig = ImageBlock.UrlConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().url(urlConfig).severity(Severity.ERROR).build();

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(36);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("target")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.url.required", msg.getRuleId());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("filename.png", msg.getMissingValueHint());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(36, msg.getLocation().getStartLine());
        }

        @Test
        @DisplayName("should highlight invalid URL pattern")
        void shouldHighlightInvalidUrlPattern() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.UrlConfig urlConfig = ImageBlock.UrlConfig.builder()
                    .pattern(Pattern.compile("^[a-zA-Z0-9/_-]+\\.(png|jpg|jpeg|gif|svg)$")).build();
            ImageBlock config = ImageBlock.builder().url(urlConfig).severity(Severity.ERROR).build();

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(38);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("target")).thenReturn(true);
            when(mockBlock.getAttribute("target")).thenReturn("invalid file name.png");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.url.pattern", msg.getRuleId());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(38, msg.getLocation().getStartLine());
        }

    }

    @Nested
    @DisplayName("dimensions validation")
    class DimensionsValidation {

        @Test
        @DisplayName("should highlight width too small")
        void shouldHighlightWidthTooSmall() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().minValue(100).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.ERROR).build();

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(40);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("width")).thenReturn(true);
            when(mockBlock.getAttribute("width")).thenReturn("50");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.width.min", msg.getRuleId());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(40, msg.getLocation().getStartLine());
            assertEquals("Image width is too small", msg.getMessage());
            assertEquals("50px", msg.getActualValue().orElse(null));
            assertEquals("At least 100px", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should include placeholder for missing width")
        void shouldIncludePlaceholderForMissingWidth() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.ERROR).build();

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(42);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("width")).thenReturn(true);
            when(mockBlock.getAttribute("width")).thenReturn("");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.width.required", msg.getRuleId());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("640", msg.getMissingValueHint());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(42, msg.getLocation().getStartLine());
        }

        @Test
        @DisplayName("should highlight width too large")
        void shouldHighlightWidthTooLarge() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().maxValue(1200).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.ERROR).build();

            // Mock source location
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(44);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("width")).thenReturn(true);
            when(mockBlock.getAttribute("width")).thenReturn("2000");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.width.max", msg.getRuleId());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(44, msg.getLocation().getStartLine());
        }

        @Test
        @DisplayName("should validate maximum width")
        void shouldValidateMaximumWidth() {
            // Given
            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().maxValue(800).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("width")).thenReturn(true);
            when(mockBlock.getAttribute("width")).thenReturn("1000");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("image.width.max", msg.getRuleId());
            assertEquals("Image width is too large", msg.getMessage());
        }

        @Test
        @DisplayName("should validate required width")
        void shouldValidateRequiredWidth() {
            // Given
            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("width")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.width.required", msg.getRuleId());
            assertEquals("Image must have width specified", msg.getMessage());
        }

        @Test
        @DisplayName("should validate height")
        void shouldValidateHeight() {
            // Given
            ImageBlock.DimensionConfig heightConfig = ImageBlock.DimensionConfig.builder().minValue(50).maxValue(600)
                    .build();
            ImageBlock config = ImageBlock.builder().height(heightConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("height")).thenReturn(true);
            when(mockBlock.getAttribute("height")).thenReturn("700");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.height.max", msg.getRuleId());
            assertEquals("Image height is too large", msg.getMessage());
        }
    }

    @Nested
    @DisplayName("alt text validation")
    class AltTextValidation {

        @Test
        @DisplayName("should validate required alt text")
        void shouldValidateRequiredAltText() {
            // Given
            ImageBlock.AltTextConfig altConfig = ImageBlock.AltTextConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().alt(altConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("alt")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.alt.required", msg.getRuleId());
            assertEquals("Image must have alt text", msg.getMessage());
            assertEquals("No alt text", msg.getActualValue().orElse(null));
            assertEquals("Alt text required", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should include placeholder and exact position for missing alt text")
        void shouldIncludePlaceholderAndExactPositionForMissingAltText() {
            // Given
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test.adoc");

            ImageBlock.AltTextConfig altConfig = ImageBlock.AltTextConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().alt(altConfig).severity(Severity.ERROR).build();

            // Mock source location for line 32
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(32);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("alt")).thenReturn(false);
            when(mockBlock.getAttribute("target")).thenReturn("missing-image.png");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.alt.required", msg.getRuleId());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("Description of image", msg.getMissingValueHint());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(32, msg.getLocation().getStartLine());
            assertEquals(32, msg.getLocation().getEndLine());
        }

        @Test
        @DisplayName("should validate alt text minimum length")
        void shouldValidateAltTextMinLength() {
            // Given
            ImageBlock.AltTextConfig altConfig = ImageBlock.AltTextConfig.builder().minLength(10).build();
            ImageBlock config = ImageBlock.builder().alt(altConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("alt")).thenReturn(true);
            when(mockBlock.getAttribute("alt")).thenReturn("Logo");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("image.alt.minLength", msg.getRuleId());
            assertEquals("Image alt text is too short", msg.getMessage());
            assertEquals("4 characters", msg.getActualValue().orElse(null));
            assertEquals("At least 10 characters", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate alt text maximum length")
        void shouldValidateAltTextMaxLength() {
            // Given
            ImageBlock.AltTextConfig altConfig = ImageBlock.AltTextConfig.builder().maxLength(20).build();
            ImageBlock config = ImageBlock.builder().alt(altConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("alt")).thenReturn(true);
            when(mockBlock.getAttribute("alt")).thenReturn("This is a very long alt text description");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("image.alt.maxLength", msg.getRuleId());
            assertEquals("Image alt text is too long", msg.getMessage());
        }

    }

    @Nested
    @DisplayName("severity hierarchy")
    class SeverityHierarchy {

        @Test
        @DisplayName("should always use block severity for URL validation")
        void shouldAlwaysUseBlockSeverityForUrl() {
            // Given - ImageBlock.UrlConfig has no severity field
            ImageBlock.UrlConfig urlConfig = ImageBlock.UrlConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().url(urlConfig).severity(Severity.INFO) // Block severity
                    .build();

            when(mockBlock.hasAttribute("target")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(),
                    "Should use block severity (INFO) since UrlConfig has no severity field");
            assertEquals("image.url.required", msg.getRuleId());
        }

        @Test
        @DisplayName("should always use block severity for dimension validation")
        void shouldAlwaysUseBlockSeverityForDimension() {
            // Given - ImageBlock.DimensionConfig has no severity field
            ImageBlock.DimensionConfig widthConfig = ImageBlock.DimensionConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().width(widthConfig).severity(Severity.WARN) // Block severity
                    .build();

            when(mockBlock.hasAttribute("width")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(),
                    "Should use block severity (WARN) since DimensionConfig has no severity field");
            assertEquals("image.width.required", msg.getRuleId());
        }

        @Test
        @DisplayName("should always use block severity for alt text validation")
        void shouldAlwaysUseBlockSeverityForAltText() {
            // Given - ImageBlock.AltTextConfig has no severity field
            ImageBlock.AltTextConfig altConfig = ImageBlock.AltTextConfig.builder().required(true).build();
            ImageBlock config = ImageBlock.builder().alt(altConfig).severity(Severity.ERROR) // Block severity
                    .build();

            when(mockBlock.hasAttribute("alt")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity(),
                    "Should use block severity (ERROR) since AltTextConfig has no severity field");
            assertEquals("image.alt.required", msg.getRuleId());
        }
    }

    @Nested
    @DisplayName("complex validation scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRules() {
            // Given
            ImageBlock config = ImageBlock.builder()
                    .url(ImageBlock.UrlConfig.builder().required(true).pattern(Pattern.compile("^images/.*")).build())
                    .alt(ImageBlock.AltTextConfig.builder().required(true).minLength(5).build())
                    .severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("target")).thenReturn(true);
            when(mockBlock.getAttribute("target")).thenReturn("assets/logo.png");
            when(mockBlock.hasAttribute("alt")).thenReturn(true);
            when(mockBlock.getAttribute("alt")).thenReturn("Log");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(2, messages.size());
            assertTrue(messages.stream().anyMatch(m -> "image.url.pattern".equals(m.getRuleId())));
            assertTrue(messages.stream().anyMatch(m -> "image.alt.minLength".equals(m.getRuleId())));
        }
    }
}
