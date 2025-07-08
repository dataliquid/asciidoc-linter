package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for EXAMPLE blocks.
 * 
 * Validates example blocks according to the YAML schema definition,
 * including caption format and collapsible attribute.
 */
public final class ExampleBlockValidator extends AbstractBlockValidator<ExampleBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.EXAMPLE;
    }
    
    @Override
    protected Class<ExampleBlock> getBlockConfigClass() {
        return ExampleBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode node, 
                                                               ExampleBlock exampleBlock,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate caption
        if (exampleBlock.getCaption() != null) {
            messages.addAll(validateCaption(node, exampleBlock, context));
        }
        
        // Validate collapsible
        if (exampleBlock.getCollapsible() != null) {
            messages.addAll(validateCollapsible(node, exampleBlock, context));
        }
        
        return messages;
    }
    
    private List<ValidationMessage> validateCaption(StructuralNode node, ExampleBlock block, BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        ExampleBlock.CaptionConfig config = block.getCaption();
        
        String caption = node.getTitle();
        
        // Check if caption is required
        if (config.isRequired() && (caption == null || caption.trim().isEmpty())) {
            messages.add(createMessage(
                "Example block requires a caption",
                determineSeverity(config.getSeverity(), block.getSeverity()),
                context.createLocation(node)
            ));
            return messages;
        }
        
        // If caption is not present and not required, skip further validation
        if (caption == null || caption.trim().isEmpty()) {
            return messages;
        }
        
        // Validate caption length
        if (config.getMinLength() != null && caption.length() < config.getMinLength()) {
            messages.add(createMessage(
                String.format("Example caption length %d is less than required minimum %d",
                    caption.length(), config.getMinLength()),
                determineSeverity(config.getSeverity(), block.getSeverity()),
                context.createLocation(node)
            ));
        }
        
        if (config.getMaxLength() != null && caption.length() > config.getMaxLength()) {
            messages.add(createMessage(
                String.format("Example caption length %d exceeds maximum %d",
                    caption.length(), config.getMaxLength()),
                determineSeverity(config.getSeverity(), block.getSeverity()),
                context.createLocation(node)
            ));
        }
        
        // Validate caption pattern
        if (config.getPattern() != null && !config.getPattern().matcher(caption).matches()) {
            messages.add(createMessage(
                String.format("Example caption '%s' does not match required pattern '%s'",
                    caption, config.getPattern().pattern()),
                determineSeverity(config.getSeverity(), block.getSeverity()),
                context.createLocation(node)
            ));
        }
        
        return messages;
    }
    
    private List<ValidationMessage> validateCollapsible(StructuralNode node, ExampleBlock block, BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        ExampleBlock.CollapsibleConfig config = block.getCollapsible();
        
        // Check for collapsible attribute
        Object collapsibleAttr = node.getAttribute("collapsible-option");
        if (collapsibleAttr == null) {
            collapsibleAttr = node.getAttribute("collapsible");
        }
        
        // Check if collapsible is required
        if (config.isRequired() && collapsibleAttr == null) {
            messages.add(createMessage(
                "Example block requires a collapsible attribute",
                determineSeverity(config.getSeverity(), block.getSeverity()),
                context.createLocation(node)
            ));
            return messages;
        }
        
        // If collapsible is not present and not required, skip further validation
        if (collapsibleAttr == null) {
            return messages;
        }
        
        // Parse boolean value
        Boolean collapsibleValue = null;
        if (collapsibleAttr instanceof Boolean) {
            collapsibleValue = (Boolean) collapsibleAttr;
        } else if (collapsibleAttr instanceof String) {
            String strValue = ((String) collapsibleAttr).toLowerCase();
            if ("true".equals(strValue) || "yes".equals(strValue) || "1".equals(strValue)) {
                collapsibleValue = true;
            } else if ("false".equals(strValue) || "no".equals(strValue) || "0".equals(strValue)) {
                collapsibleValue = false;
            }
        }
        
        // Validate allowed values
        if (config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (collapsibleValue == null || !config.getAllowed().contains(collapsibleValue)) {
                messages.add(createMessage(
                    String.format("Example collapsible value '%s' is not in allowed values %s",
                        collapsibleAttr, config.getAllowed()),
                    determineSeverity(config.getSeverity(), block.getSeverity()),
                    context.createLocation(node)
                ));
            }
        }
        
        return messages;
    }
    
    private ValidationMessage createMessage(String message, Severity severity, SourceLocation location) {
        return ValidationMessage.builder()
                .message(message)
                .severity(severity)
                .location(location)
                .ruleId("example-block")
                .build();
    }
    
    private Severity determineSeverity(Severity specificSeverity, Severity blockSeverity) {
        return specificSeverity != null ? specificSeverity : blockSeverity;
    }
}