package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.AdmonitionBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for admonition blocks in AsciiDoc documents.
 * 
 * <p>This validator validates admonition blocks (NOTE, TIP, IMPORTANT, WARNING, CAUTION)
 * based on the YAML schema structure defined in 
 * {@code src/main/resources/schemas/blocks/admonition-block.yaml}.
 * The YAML configuration is parsed into {@link AdmonitionBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>type</b>: Validates admonition type (required, allowed types)</li>
 *   <li><b>title</b>: Validates block title (required, pattern, min/max length)</li>
 *   <li><b>content</b>: Validates content (required, min/max length, lines)</li>
 *   <li><b>icon</b>: Validates icon settings (required, pattern)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see AdmonitionBlock
 * @see BlockTypeValidator
 */
public final class AdmonitionBlockValidator extends AbstractBlockValidator<AdmonitionBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.ADMONITION;
    }
    
    @Override
    protected Class<AdmonitionBlock> getBlockConfigClass() {
        return AdmonitionBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               AdmonitionBlock admonitionConfig,
                                                               BlockValidationContext context) {
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get admonition attributes
        String admonitionType = getAdmonitionType(block);
        String title = block.getTitle();
        String content = getBlockContent(block);
        boolean hasIcon = hasIcon(block);
        
        // Validate type
        if (admonitionConfig.getTypeConfig() != null) {
            validateType(admonitionType, admonitionConfig.getTypeConfig(), admonitionConfig, context, block, messages);
        }
        
        // Validate title
        if (admonitionConfig.getTitle() != null) {
            validateTitle(title, admonitionConfig.getTitle(), admonitionConfig, context, block, messages);
        }
        
        // Validate content
        if (admonitionConfig.getContent() != null) {
            validateContent(content, admonitionConfig.getContent(), admonitionConfig, context, block, messages);
        }
        
        // Validate icon
        if (admonitionConfig.getIcon() != null) {
            validateIcon(hasIcon, admonitionConfig.getIcon(), admonitionConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getAdmonitionType(StructuralNode block) {
        // Admonition type is typically in the style attribute
        String style = block.getStyle();
        if (style != null) {
            // Convert to uppercase for consistency
            return style.toUpperCase();
        }
        
        // Try role attribute as fallback
        Object role = block.getAttribute("role");
        if (role != null) {
            return role.toString().toUpperCase();
        }
        
        return null;
    }
    
    
    private boolean hasIcon(StructuralNode block) {
        // Check if icons are enabled at document level
        Object docIcons = block.getDocument().getAttribute("icons");
        if (docIcons != null && "font".equals(docIcons.toString())) {
            return true;
        }
        
        // Check block-level icon attribute
        Object blockIcon = block.getAttribute("icon");
        return blockIcon != null && !"none".equals(blockIcon.toString());
    }
    
    private void validateType(String admonitionType, AdmonitionBlock.TypeConfig config,
                            AdmonitionBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if type is required
        if (config.isRequired() && (admonitionType == null || admonitionType.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.type.required")
                .location(context.createLocation(block))
                .message("Admonition block must have a type")
                .actualValue("No type")
                .expectedValue("Type required")
                .build());
            return;
        }
        
        // Validate allowed types if specified
        if (admonitionType != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(admonitionType)) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.type.allowed")
                    .location(context.createLocation(block))
                    .message("Admonition block has unsupported type")
                    .actualValue(admonitionType)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
                    .build());
            }
        }
    }
    
    private void validateTitle(String title, AdmonitionBlock.TitleConfig config,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.title.required")
                .location(context.createLocation(block))
                .message("Admonition block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null) {
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(title).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.pattern")
                    .location(context.createLocation(block))
                    .message("Admonition title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
            
            // Validate min length
            if (config.getMinLength() != null && title.length() < config.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.minLength")
                    .location(context.createLocation(block))
                    .message("Admonition title is too short")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && title.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.maxLength")
                    .location(context.createLocation(block))
                    .message("Admonition title is too long")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateContent(String content, AdmonitionBlock.ContentConfig config,
                               AdmonitionBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if content is required
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.required")
                .location(context.createLocation(block))
                .message("Admonition block must have content")
                .actualValue("No content")
                .expectedValue("Content required")
                .build());
            return;
        }
        
        int contentLength = content.trim().length();
        
        // Validate min length
        if (config.getMinLength() != null && contentLength < config.getMinLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.minLength")
                .location(context.createLocation(block))
                .message("Admonition content is too short")
                .actualValue(contentLength + " characters")
                .expectedValue("At least " + config.getMinLength() + " characters")
                .build());
        }
        
        // Validate max length
        if (config.getMaxLength() != null && contentLength > config.getMaxLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.maxLength")
                .location(context.createLocation(block))
                .message("Admonition content is too long")
                .actualValue(contentLength + " characters")
                .expectedValue("At most " + config.getMaxLength() + " characters")
                .build());
        }
        
        // Validate lines if configured
        if (config.getLines() != null) {
            validateLines(content, config.getLines(), config, blockConfig, context, block, messages);
        }
    }
    
    private void validateLines(String content, com.dataliquid.asciidoc.linter.config.rule.LineConfig config,
                             AdmonitionBlock.ContentConfig contentConfig,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to content config severity, then block severity
        Severity severity = config.severity() != null ? config.severity() : 
                           (contentConfig.getSeverity() != null ? contentConfig.getSeverity() : 
                            blockConfig.getSeverity());
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (config.min() != null && lineCount < config.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.lines.min")
                .location(context.createLocation(block))
                .message("Admonition block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + config.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.max() != null && lineCount > config.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.lines.max")
                .location(context.createLocation(block))
                .message("Admonition block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + config.max() + " lines")
                .build());
        }
    }
    
    private void validateIcon(boolean hasIcon, AdmonitionBlock.IconConfig config,
                            AdmonitionBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && !hasIcon) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.icon.required")
                .location(context.createLocation(block))
                .message("Admonition block must have an icon")
                .actualValue("No icon")
                .expectedValue("Icon required")
                .build());
        }
        
        // Validate icon pattern if present and pattern is configured
        if (hasIcon && config.getPattern() != null) {
            String iconValue = getIconValue(block);
            if (iconValue != null && !config.getPattern().matcher(iconValue).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.icon.pattern")
                    .location(context.createLocation(block))
                    .message("Admonition icon does not match required pattern")
                    .actualValue(iconValue)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
        }
    }
    
    private String getIconValue(StructuralNode block) {
        Object icon = block.getAttribute("icon");
        return icon != null ? icon.toString() : null;
    }
    
}