package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Sidebar.*;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;
import com.dataliquid.asciidoc.linter.util.StringUtils;

/**
 * Validator for sidebar blocks based on YAML schema configuration.
 * <p>
 * Sidebar blocks in AsciiDoc are delimited by **** and contain supplementary
 * information. This validator checks:
 * <ul>
 * <li>Title requirements (optional with pattern, minLength, maxLength)</li>
 * <li>Content requirements (required with minLength, maxLength, nested
 * lines)</li>
 * <li>Position requirements (optional with allowed values)</li>
 * </ul>
 * <p>
 * Each nested configuration can optionally define its own severity level. If
 * not specified, the block-level severity is used as fallback.
 * </p>
 *
 * @see SidebarBlock
 * @see BlockTypeValidator
 */
public final class SidebarBlockValidator extends AbstractBlockValidator<SidebarBlock> {
    private static final String CHARACTERS_UNIT = " characters";
    private static final String LINES_UNIT = " lines";

    @Override
    public BlockType getSupportedType() {
        return BlockType.SIDEBAR;
    }

    @Override
    protected Class<SidebarBlock> getBlockConfigClass() {
        return SidebarBlock.class;
    }

    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, SidebarBlock sidebarConfig,
            BlockValidationContext context) {

        List<ValidationMessage> messages = new ArrayList<>();

        // Validate title
        if (sidebarConfig.getTitle() != null) {
            validateTitle(block, sidebarConfig, context, messages);
        }

        // Validate content
        if (sidebarConfig.getContent() != null) {
            validateContent(block, sidebarConfig, context, messages);
        }

        // Validate position
        if (sidebarConfig.getPosition() != null) {
            validatePosition(block, sidebarConfig, context, messages);
        }

        return messages;
    }

    private void validateTitle(StructuralNode block, SidebarBlock config, BlockValidationContext context,
            List<ValidationMessage> messages) {
        SidebarBlock.TitleConfig titleConfig = config.getTitle();

        String title = block.getTitle();
        boolean hasTitle = title != null && !StringUtils.isBlank(title);

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(titleConfig.getSeverity(), config.getSeverity());

        // Check if title is required
        if (titleConfig.isRequired() && !hasTitle) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(TITLE_REQUIRED)
                            .location(context.createLocation(block))
                            .message("Sidebar block requires a title")
                            .actualValue("No title")
                            .expectedValue("Title required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint(".Sidebar Title")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add title to sidebar block")
                                    .addExample(".Important Information")
                                    .addExample(".Additional Notes")
                                    .addExample(".Side Note")
                                    .explanation(
                                            "Sidebar blocks should have descriptive titles to indicate their purpose")
                                    .build())
                            .build());
            return;
        }

        if (!hasTitle) {
            return;
        }

        // Validate title length
        if (titleConfig.getMinLength() != null && title.length() < titleConfig.getMinLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(TITLE_MIN_LENGTH)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar title too short")
                            .actualValue(title.length() + CHARACTERS_UNIT)
                            .expectedValue("At least " + titleConfig.getMinLength() + CHARACTERS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Use a longer, more descriptive title")
                                    .addExample("Additional Information")
                                    .addExample("Important Notes")
                                    .addExample("Related Topics")
                                    .explanation(
                                            "Sidebar titles should be descriptive enough to meet minimum length requirements")
                                    .build())
                            .build());
        }

        if (titleConfig.getMaxLength() != null && title.length() > titleConfig.getMaxLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(TITLE_MAX_LENGTH)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar title too long")
                            .actualValue(title.length() + CHARACTERS_UNIT)
                            .expectedValue("At most " + titleConfig.getMaxLength() + CHARACTERS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Shorten the title")
                                    .addExample("Info")
                                    .addExample("Notes")
                                    .addExample("Tip")
                                    .explanation(
                                            "Sidebar titles should be concise and not exceed maximum length limits")
                                    .build())
                            .build());
        }

        // Validate title pattern
        if (titleConfig.getPattern() != null) {
            Pattern pattern = titleConfig.getPattern();
            if (!pattern.matcher(title).matches()) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(TITLE_PATTERN)
                                .location(context.createLocation(block, 1, 1))
                                .message("Sidebar title does not match required pattern")
                                .actualValue(title)
                                .expectedValue("Pattern: " + pattern.pattern())
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Format title to match required pattern")
                                        .addExample("Important Note")
                                        .addExample("Quick Reference")
                                        .addExample("Related Information")
                                        .explanation("Sidebar titles must follow the specified format pattern")
                                        .build())
                                .build());
            }
        }
    }

    private void validateContent(StructuralNode block, SidebarBlock config, BlockValidationContext context,
            List<ValidationMessage> messages) {
        SidebarBlock.ContentConfig contentConfig = config.getContent();

        String content = getBlockContent(block);

        // Check if content is required
        if (contentConfig.isRequired() && content.isEmpty()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(config.getSeverity())
                            .ruleId(CONTENT_REQUIRED)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar block requires content")
                            .actualValue("No content")
                            .expectedValue("Content required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("Content")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add content to sidebar block")
                                    .addExample("This is additional information related to the main content.")
                                    .addExample("Key points to remember:")
                                    .addExample("For more details, see Chapter 5.")
                                    .explanation("Sidebar blocks must contain relevant supplementary information")
                                    .build())
                            .build());
            return;
        }

        if (content.isEmpty()) {
            return;
        }

        // Validate content length
        if (contentConfig.getMinLength() != null && content.length() < contentConfig.getMinLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(config.getSeverity())
                            .ruleId(CONTENT_MIN_LENGTH)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar content too short")
                            .actualValue(content.length() + CHARACTERS_UNIT)
                            .expectedValue("At least " + contentConfig.getMinLength() + CHARACTERS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add more detailed content")
                                    .addExample("Expand with additional details or examples")
                                    .addExample("Include relevant background information")
                                    .addExample("Add context or explanatory notes")
                                    .explanation(
                                            "Sidebar content should be detailed enough to meet minimum length requirements")
                                    .build())
                            .build());
        }

        if (contentConfig.getMaxLength() != null && content.length() > contentConfig.getMaxLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(config.getSeverity())
                            .ruleId(CONTENT_MAX_LENGTH)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar content too long")
                            .actualValue(content.length() + CHARACTERS_UNIT)
                            .expectedValue("At most " + contentConfig.getMaxLength() + CHARACTERS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Shorten the content")
                                    .addExample("Use bullet points for conciseness")
                                    .addExample("Remove non-essential details")
                                    .addExample("Split into multiple smaller sidebars")
                                    .explanation(
                                            "Sidebar content should be concise and not exceed maximum length limits")
                                    .build())
                            .build());
        }

        // Validate lines if configured
        if (contentConfig.getLines() != null) {
            validateLines(block, config, contentConfig.getLines(), context, messages);
        }
    }

    private void validateLines(StructuralNode block, SidebarBlock config, SidebarBlock.LinesConfig linesConfig,
            BlockValidationContext context, List<ValidationMessage> messages) {

        String content = getBlockContent(block);
        String[] lines = content.split("\n");
        int lineCount = lines.length;

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(linesConfig.getSeverity(), config.getSeverity());

        // Validate minimum lines
        if (linesConfig.getMin() != null && lineCount < linesConfig.getMin()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(LINES_MIN)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar has too few lines")
                            .actualValue(lineCount + LINES_UNIT)
                            .expectedValue("At least " + linesConfig.getMin() + LINES_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add more lines of content")
                                    .addExample("Add additional paragraphs")
                                    .addExample("Include multiple bullet points")
                                    .addExample("Break content across more lines")
                                    .explanation("Sidebar content should meet the minimum line count requirement")
                                    .build())
                            .build());
        }

        // Validate maximum lines
        if (linesConfig.getMax() != null && lineCount > linesConfig.getMax()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(LINES_MAX)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar has too many lines")
                            .actualValue(lineCount + LINES_UNIT)
                            .expectedValue("At most " + linesConfig.getMax() + LINES_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Reduce the number of lines")
                                    .addExample("Combine shorter lines")
                                    .addExample("Remove redundant information")
                                    .addExample("Use more concise language")
                                    .explanation("Sidebar content should not exceed the maximum line count limit")
                                    .build())
                            .build());
        }
    }

    private void validatePosition(StructuralNode block, SidebarBlock config, BlockValidationContext context,
            List<ValidationMessage> messages) {
        SidebarBlock.PositionConfig positionConfig = config.getPosition();

        // Get position attribute
        Object positionAttr = block.getAttribute(POSITION);
        String position = positionAttr != null ? positionAttr.toString() : null;

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(positionConfig.getSeverity(), config.getSeverity());

        // Check if position is required
        if (positionConfig.isRequired() && (position == null || position.isEmpty())) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(POSITION_REQUIRED)
                            .location(context.createLocation(block, 1, 1))
                            .message("Sidebar block requires a position attribute")
                            .actualValue("No position attribute")
                            .expectedValue("Position attribute required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("[position=left]")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add position attribute to sidebar")
                                    .fixedValue("[position=left]")
                                    .addExample("[sidebar,position=left]")
                                    .addExample("[sidebar,position=right]")
                                    .addExample("[sidebar,position=center]")
                                    .explanation("Sidebar blocks should specify their position for proper layout")
                                    .build())
                            .build());
            return;
        }

        // Validate allowed positions
        if (position != null && !position.isEmpty() && positionConfig.getAllowed() != null
                && !positionConfig.getAllowed().isEmpty()) {
            if (!positionConfig.getAllowed().contains(position)) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(POSITION_ALLOWED)
                                .location(context.createLocation(block, 1, 1))
                                .message("Invalid sidebar position")
                                .actualValue(position)
                                .expectedValue("One of: " + String.join(", ", positionConfig.getAllowed()))
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Use a valid position value")
                                        .fixedValue("[position=" + positionConfig.getAllowed().get(0) + "]")
                                        .addExample("[sidebar,position=left]")
                                        .addExample("[sidebar,position=right]")
                                        .addExample("[sidebar,position=center]")
                                        .explanation("Sidebar position must be one of the allowed values")
                                        .build())
                                .build());
            }
        }
    }
}
