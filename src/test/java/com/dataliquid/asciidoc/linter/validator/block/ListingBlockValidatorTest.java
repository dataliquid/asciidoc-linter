package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import org.asciidoctor.ast.Cursor;

/**
 * Unit tests for {@link ListingBlockValidator}.
 * <p>
 * This test class validates the behavior of the listing block validator, which
 * processes code blocks in AsciiDoc documents. The tests cover all validation
 * rules including language requirements, title patterns, line count
 * constraints, and callout restrictions.
 * </p>
 * <p>
 * Test structure follows a nested class pattern for better organization:
 * </p>
 * <ul>
 * <li>Validate - Basic validator functionality</li>
 * <li>LanguageValidation - Language specification and allowed values</li>
 * <li>TitleValidation - Title requirements and pattern matching</li>
 * <li>LinesValidation - Line count constraints</li>
 * <li>CalloutsValidation - Callout annotation rules</li>
 * <li>ComplexScenarios - Combined validation scenarios</li>
 * </ul>
 *
 * @see ListingBlockValidator
 * @see ListingBlock
 */
@DisplayName("ListingBlockValidator")
class ListingBlockValidatorTest {

    private ListingBlockValidator validator;
    private BlockValidationContext context;
    private Block mockBlock;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        validator = new ListingBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(Block.class);
    }

    @Test
    @DisplayName("should return LISTING as supported type")
    void shouldReturnListingAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();

        // Then
        assertEquals(BlockType.LISTING, type);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return empty list when block is not Block instance")
        void shouldReturnEmptyListWhenNotBlockInstance() {
            // Given
            StructuralNode notABlock = mock(StructuralNode.class);
            ListingBlock config = ListingBlock.builder().severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> messages = validator.validate(notABlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            ListingBlock config = ListingBlock.builder().severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("language validation")
    class LanguageValidation {

        @Test
        @DisplayName("should validate required language")
        void shouldValidateRequiredLanguage() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("language")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("listing.language.required", msg.getRuleId());
            assertEquals("Listing language is required", msg.getMessage());
            assertEquals("No language", msg.getActualValue().orElse(null));
            assertEquals("Language required", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate allowed languages")
        void shouldValidateAllowedLanguages() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .allowed(Arrays.asList("java", "python", "javascript"))
                    .severity(Severity.WARN)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("ruby");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.language.allowed", msg.getRuleId());
            assertEquals("Listing language 'ruby' is not allowed", msg.getMessage());
            assertEquals("ruby", msg.getActualValue().orElse(null));
            assertEquals("One of: java, python, javascript", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should use language severity over block severity")
        void shouldUseLanguageSeverityOverBlockSeverity() {
            // Given - language has WARN, block has ERROR
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .required(true)
                    .severity(Severity.WARN)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("language")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(),
                    "Should use language severity (WARN) instead of block severity (ERROR)");
        }

        @Test
        @DisplayName("should use block severity when language severity is not defined")
        void shouldUseBlockSeverityWhenLanguageSeverityNotDefined() {
            // Given - language has no severity, block has INFO
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .required(true)
                    // No severity set
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.INFO).build();

            when(mockBlock.hasAttribute("language")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(),
                    "Should use block severity (INFO) when language severity is not defined");
            assertEquals("listing.language.required", msg.getRuleId());
        }

        @Test
        @DisplayName("should include placeholder and exact position for missing language - based on test-errors.adoc line 17")
        void shouldIncludePlaceholderAndColumnForMissingLanguage() {
            // Given - Based on test-errors.adoc line 17: [source]
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test-errors.adoc");

            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            // Mock source location for line 17 from test-errors.adoc
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(17);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("language")).thenReturn(false);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.language.required", msg.getRuleId());
            assertEquals(ErrorType.MISSING_VALUE, msg.getErrorType());
            assertEquals("language", msg.getMissingValueHint());
            assertNotNull(msg.getPlaceholderContext());
            assertEquals(PlaceholderContext.PlaceholderType.LIST_VALUE, msg.getPlaceholderContext().getType());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(17, msg.getLocation().getStartLine());
            assertEquals(17, msg.getLocation().getEndLine());
        }

        @Test
        @DisplayName("should include exact position for invalid language - based on test-errors.adoc line 25")
        void shouldIncludeExactPositionForInvalidLanguage() {
            // Given - Based on test-errors.adoc line 25: [source,invalidlang]
            BlockValidationContext testContext = new BlockValidationContext(mockSection, "test-errors.adoc");

            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .allowed(Arrays.asList("java", "python", "yaml", "bash"))
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            // Mock source location for line 25
            Cursor mockCursor = mock(Cursor.class);
            when(mockCursor.getLineNumber()).thenReturn(25);
            when(mockBlock.getSourceLocation()).thenReturn(mockCursor);
            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("invalidlang");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, testContext);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.language.allowed", msg.getRuleId());

            // Without file content, validator falls back to column 1
            assertEquals(1, msg.getLocation().getStartColumn());
            assertEquals(1, msg.getLocation().getEndColumn());
            assertEquals(25, msg.getLocation().getStartLine());
            assertEquals(25, msg.getLocation().getEndLine());
        }

        @Test
        @DisplayName("should pass when language is allowed")
        void shouldPassWhenLanguageIsAllowed() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig
                    .builder()
                    .allowed(Arrays.asList("java", "python"))
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().language(languageConfig).severity(Severity.ERROR).build();

            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("java");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("title validation")
    class TitleValidation {

        @Test
        @DisplayName("should validate required title")
        void shouldValidateRequiredTitle() {
            // Given
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig
                    .builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().title(titleConfig).severity(Severity.ERROR).build();

            when(mockBlock.getTitle()).thenReturn(null);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.title.required", msg.getRuleId());
            assertEquals("Listing block must have a title", msg.getMessage());
        }

        @Test
        @DisplayName("should validate title pattern")
        void shouldValidateTitlePattern() {
            // Given
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig
                    .builder()
                    .pattern(Pattern.compile("^Listing \\d+:.*"))
                    .severity(Severity.INFO)
                    .build();
            ListingBlock config = ListingBlock.builder().title(titleConfig).severity(Severity.ERROR).build();

            when(mockBlock.getTitle()).thenReturn("Code Example");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("listing.title.pattern", msg.getRuleId());
            assertEquals("Listing title does not match required pattern", msg.getMessage());
            assertEquals("Code Example", msg.getActualValue().orElse(null));
            assertEquals("Pattern: ^Listing \\d+:.*", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should use title severity over block severity")
        void shouldUseTitleSeverityOverBlockSeverity() {
            // Given - title has INFO, block has ERROR
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig
                    .builder()
                    .required(true)
                    .severity(Severity.INFO)
                    .build();
            ListingBlock config = ListingBlock.builder().title(titleConfig).severity(Severity.ERROR).build();

            when(mockBlock.getTitle()).thenReturn(null);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(),
                    "Should use title severity (INFO) instead of block severity (ERROR)");
        }

        @Test
        @DisplayName("should use block severity when title severity is not defined")
        void shouldUseBlockSeverityWhenTitleSeverityNotDefined() {
            // Given - title has no severity, block has WARN
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig
                    .builder()
                    .required(true)
                    // No severity set
                    .build();
            ListingBlock config = ListingBlock.builder().title(titleConfig).severity(Severity.WARN).build();

            when(mockBlock.getTitle()).thenReturn(null);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(),
                    "Should use block severity (WARN) when title severity is not defined");
            assertEquals("listing.title.required", msg.getRuleId());
        }
    }

    @Nested
    @DisplayName("callouts validation")
    class CalloutsValidation {

        @Test
        @DisplayName("should validate callouts not allowed")
        void shouldValidateCalloutsNotAllowed() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(false)
                    .severity(Severity.WARN)
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.callouts.notAllowed", msg.getRuleId());
            assertEquals("Listing block must not contain callouts", msg.getMessage());
            assertEquals("1 callouts", msg.getActualValue().orElse(null));
            assertEquals("No callouts allowed", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should use callouts severity over block severity")
        void shouldUseCalloutsSeverityOverBlockSeverity() {
            // Given - callouts has WARN, block has ERROR
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(false)
                    .severity(Severity.WARN)
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(),
                    "Should use callouts severity (WARN) instead of block severity (ERROR)");
        }

        @Test
        @DisplayName("should use block severity when callouts severity is not defined")
        void shouldUseBlockSeverityWhenCalloutsSeverityNotDefined() {
            // Given - callouts has no severity, block has ERROR
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(false)
                    // No severity set
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity(),
                    "Should use block severity (ERROR) when callouts severity is not defined");
            assertEquals("listing.callouts.notAllowed", msg.getRuleId());
        }

        @Test
        @DisplayName("should allow callouts when allowed is true")
        void shouldAllowCalloutsWhenAllowedIsTrue() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(true)
                    .severity(Severity.WARN)
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty()); // Callouts allowed
        }

        @Test
        @DisplayName("should validate maximum callout count")
        void shouldValidateMaximumCalloutCount() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(true)
                    .max(2)
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("code // <1>\nmore // <2>\nagain // <3>");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.callouts.max", msg.getRuleId());
            assertEquals("Listing block has too many callouts", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At most 2 callouts", msg.getExpectedValue().orElse(null));
        }
    }

    @Nested
    @DisplayName("lines validation")
    class LinesValidation {

        @Test
        @DisplayName("should validate minimum lines")
        void shouldValidateMinimumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder().min(5).severity(Severity.INFO).build();
            ListingBlock config = ListingBlock.builder().lines(lineConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("line1\nline2\nline3");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("listing.lines.min", msg.getRuleId());
            assertEquals("Listing block has too few lines", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At least 5 lines", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should validate maximum lines")
        void shouldValidateMaximumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder().max(50).severity(Severity.WARN).build();
            ListingBlock config = ListingBlock.builder().lines(lineConfig).severity(Severity.ERROR).build();

            // Create content with 51 lines
            StringBuilder content = new StringBuilder();
            for (int i = 1; i <= 51; i++) {
                content.append("line ").append(i).append("\n");
            }
            when(mockBlock.getContent()).thenReturn(content.toString());

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.lines.max", msg.getRuleId());
            assertEquals("Listing block has too many lines", msg.getMessage());
            assertEquals("51", msg.getActualValue().orElse(null));
            assertEquals("At most 50 lines", msg.getExpectedValue().orElse(null));
        }
    }

    @Nested
    @DisplayName("complex validation scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRules() {
            // Given
            ListingBlock config = ListingBlock
                    .builder()
                    .language(ListingBlock.LanguageConfig
                            .builder()
                            .required(true)
                            .allowed(Arrays.asList("java", "python"))
                            .severity(Severity.ERROR)
                            .build())
                    .title(ListingBlock.TitleConfig.builder().required(true).severity(Severity.WARN).build())
                    .lines(LineConfig.builder().max(10).severity(Severity.INFO).build())
                    .severity(Severity.ERROR)
                    .build();

            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("javascript"); // Not allowed
            when(mockBlock.getTitle()).thenReturn(null); // Missing
            when(mockBlock.getContent())
                    .thenReturn("line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10\nline11"); // Too
                                                                                                                  // long

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(3, messages.size());
            assertTrue(messages.stream().anyMatch(m -> "listing.language.allowed".equals(m.getRuleId())));
            assertTrue(messages.stream().anyMatch(m -> "listing.title.required".equals(m.getRuleId())));
            assertTrue(messages.stream().anyMatch(m -> "listing.lines.max".equals(m.getRuleId())));
        }

        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            // Given
            LineConfig lineConfig = LineConfig.builder().min(1).severity(Severity.ERROR).build();
            ListingBlock config = ListingBlock.builder().lines(lineConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn("");

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertEquals(1, messages.size());
            assertEquals("0", messages.get(0).getActualValue().orElse(null));
        }

        @Test
        @DisplayName("should handle null content")
        void shouldHandleNullContent() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig
                    .builder()
                    .allowed(false)
                    .severity(Severity.ERROR)
                    .build();
            ListingBlock config = ListingBlock.builder().callouts(calloutsConfig).severity(Severity.ERROR).build();

            when(mockBlock.getContent()).thenReturn(null);

            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

            // Then
            assertTrue(messages.isEmpty()); // No content means no callouts
        }
    }
}
