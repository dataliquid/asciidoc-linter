package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for sidebar blocks based on YAML schema configuration.
 * 
 * <p>Sidebar blocks in AsciiDoc are delimited by **** and contain supplementary information.
 * This validator checks:
 * <ul>
 *   <li>Title requirements (optional with pattern, minLength, maxLength)</li>
 *   <li>Content requirements (required with minLength, maxLength, nested lines)</li>
 *   <li>Position requirements (optional with allowed values)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see SidebarBlock
 * @see BlockTypeValidator
 */
public final class SidebarBlockValidator extends AbstractBlockValidator<SidebarBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.SIDEBAR;
    }
    
    @Override
    protected Class<SidebarBlock> getBlockConfigClass() {
        return SidebarBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               SidebarBlock sidebarConfig,
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
    
    private void validateTitle(StructuralNode block, SidebarBlock config,
                             BlockValidationContext context, List<ValidationMessage> messages) {
        SidebarBlock.TitleConfig titleConfig = config.getTitle();
        
        String title = block.getTitle();
        boolean hasTitle = title != null && !title.trim().isEmpty();
        
        // Get severity with fallback to block severity
        Severity severity = titleConfig.getSeverity() != null ? titleConfig.getSeverity() : config.getSeverity();
        
        // Check if title is required
        if (titleConfig.isRequired() && !hasTitle) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.title.required")
                .location(context.createLocation(block))
                .message("Sidebar block requires a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(".Sidebar Title")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (!hasTitle) {
            return;
        }
        
        // Validate title length
        if (titleConfig.getMinLength() != null && title.length() < titleConfig.getMinLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.title.minLength")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar title too short")
                .actualValue(title.length() + " characters")
                .expectedValue("At least " + titleConfig.getMinLength() + " characters")
                .build());
        }
        
        if (titleConfig.getMaxLength() != null && title.length() > titleConfig.getMaxLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.title.maxLength")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar title too long")
                .actualValue(title.length() + " characters")
                .expectedValue("At most " + titleConfig.getMaxLength() + " characters")
                .build());
        }
        
        // Validate title pattern
        if (titleConfig.getPattern() != null) {
            Pattern pattern = titleConfig.getPattern();
            if (!pattern.matcher(title).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("sidebar.title.pattern")
                    .location(context.createLocation(block, 1, 1))
                    .message("Sidebar title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + pattern.pattern())
                    .build());
            }
        }
    }
    
    private void validateContent(StructuralNode block, SidebarBlock config,
                               BlockValidationContext context, List<ValidationMessage> messages) {
        SidebarBlock.ContentConfig contentConfig = config.getContent();
        
        String content = getBlockContent(block);
        
        // Check if content is required
        if (contentConfig.isRequired() && content.isEmpty()) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("sidebar.content.required")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar block requires content")
                .actualValue("No content")
                .expectedValue("Content required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("Content")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (content.isEmpty()) {
            return;
        }
        
        // Validate content length
        if (contentConfig.getMinLength() != null && content.length() < contentConfig.getMinLength()) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("sidebar.content.minLength")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar content too short")
                .actualValue(content.length() + " characters")
                .expectedValue("At least " + contentConfig.getMinLength() + " characters")
                .build());
        }
        
        if (contentConfig.getMaxLength() != null && content.length() > contentConfig.getMaxLength()) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("sidebar.content.maxLength")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar content too long")
                .actualValue(content.length() + " characters")
                .expectedValue("At most " + contentConfig.getMaxLength() + " characters")
                .build());
        }
        
        // Validate lines if configured
        if (contentConfig.getLines() != null) {
            validateLines(block, config, contentConfig.getLines(), context, messages);
        }
    }
    
    private void validateLines(StructuralNode block, SidebarBlock config, 
                             SidebarBlock.LinesConfig linesConfig,
                             BlockValidationContext context, List<ValidationMessage> messages) {
        
        String content = getBlockContent(block);
        String[] lines = content.split("\n");
        int lineCount = lines.length;
        
        // Get severity with fallback to block severity
        Severity severity = linesConfig.getSeverity() != null ? linesConfig.getSeverity() : config.getSeverity();
        
        // Validate minimum lines
        if (linesConfig.getMin() != null && lineCount < linesConfig.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.lines.min")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar has too few lines")
                .actualValue(lineCount + " lines")
                .expectedValue("At least " + linesConfig.getMin() + " lines")
                .build());
        }
        
        // Validate maximum lines
        if (linesConfig.getMax() != null && lineCount > linesConfig.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.lines.max")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar has too many lines")
                .actualValue(lineCount + " lines")
                .expectedValue("At most " + linesConfig.getMax() + " lines")
                .build());
        }
    }
    
    private void validatePosition(StructuralNode block, SidebarBlock config,
                                BlockValidationContext context, List<ValidationMessage> messages) {
        SidebarBlock.PositionConfig positionConfig = config.getPosition();
        
        // Get position attribute
        Object positionAttr = block.getAttribute(POSITION);
        String position = positionAttr != null ? positionAttr.toString() : null;
        
        // Get severity with fallback to block severity
        Severity severity = positionConfig.getSeverity() != null ? positionConfig.getSeverity() : config.getSeverity();
        
        // Check if position is required
        if (positionConfig.isRequired() && (position == null || position.isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("sidebar.position.required")
                .location(context.createLocation(block, 1, 1))
                .message("Sidebar block requires a position attribute")
                .actualValue("No position attribute")
                .expectedValue("Position attribute required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("[position=left]")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        // Validate allowed positions
        if (position != null && !position.isEmpty() && 
            positionConfig.getAllowed() != null && !positionConfig.getAllowed().isEmpty()) {
            if (!positionConfig.getAllowed().contains(position)) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("sidebar.position.allowed")
                    .location(context.createLocation(block, 1, 1))
                    .message("Invalid sidebar position")
                    .actualValue(position)
                    .expectedValue("One of: " + String.join(", ", positionConfig.getAllowed()))
                    .build());
            }
        }
    }
}