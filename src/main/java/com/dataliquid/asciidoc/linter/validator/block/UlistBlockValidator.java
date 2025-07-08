package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for unordered list (ulist) blocks in AsciiDoc documents.
 * 
 * <p>This validator validates unordered list blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/ulist-block.yaml}.
 * The YAML configuration is parsed into {@link UlistBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>items</b>: Validates item count (min/max)</li>
 *   <li><b>nestingLevel</b>: Validates maximum nesting depth</li>
 *   <li><b>markerStyle</b>: Validates the list marker style (*, -, etc.)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see UlistBlock
 * @see BlockTypeValidator
 */
public final class UlistBlockValidator extends AbstractBlockValidator<UlistBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.ULIST;
    }
    
    @Override
    protected Class<UlistBlock> getBlockConfigClass() {
        return UlistBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               UlistBlock ulistConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get list items
        List<StructuralNode> items = getListItems(block);
        
        // Validate items count
        if (ulistConfig.getItems() != null) {
            validateItemsCount(items, ulistConfig.getItems(), ulistConfig, context, block, messages);
        }
        
        // Validate nesting level
        if (ulistConfig.getNestingLevel() != null) {
            validateNestingLevel(block, ulistConfig.getNestingLevel(), ulistConfig, context, messages);
        }
        
        // Validate marker style
        if (ulistConfig.getMarkerStyle() != null) {
            validateMarkerStyle(block, ulistConfig.getMarkerStyle(), ulistConfig, context, messages);
        }
        
        return messages;
    }
    
    private List<StructuralNode> getListItems(StructuralNode block) {
        // For unordered lists, items are in the blocks
        List<StructuralNode> items = block.getBlocks();
        return items != null ? items : new ArrayList<>();
    }
    
    private void validateItemsCount(List<StructuralNode> items, 
                                  UlistBlock.ItemsConfig config,
                                  UlistBlock blockConfig,
                                  BlockValidationContext context,
                                  StructuralNode block,
                                  List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int itemCount = items.size();
        
        // Validate min items
        if (config.getMin() != null && itemCount < config.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("ulist.items.min")
                .location(context.createLocation(block))
                .message("Unordered list has too few items")
                .actualValue(String.valueOf(itemCount))
                .expectedValue("At least " + config.getMin() + " items")
                .build());
        }
        
        // Validate max items
        if (config.getMax() != null && itemCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("ulist.items.max")
                .location(context.createLocation(block))
                .message("Unordered list has too many items")
                .actualValue(String.valueOf(itemCount))
                .expectedValue("At most " + config.getMax() + " items")
                .build());
        }
    }
    
    private void validateNestingLevel(StructuralNode block,
                                    UlistBlock.NestingLevelConfig config,
                                    UlistBlock blockConfig,
                                    BlockValidationContext context,
                                    List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int nestingLevel = calculateNestingLevel(block);
        
        // Validate max nesting
        if (config.getMax() != null && nestingLevel > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("ulist.nestingLevel.max")
                .location(context.createLocation(block))
                .message("Unordered list exceeds maximum nesting level")
                .actualValue(String.valueOf(nestingLevel))
                .expectedValue("Maximum nesting level: " + config.getMax())
                .build());
        }
    }
    
    private void validateMarkerStyle(StructuralNode block,
                                   String expectedMarkerStyle,
                                   UlistBlock blockConfig,
                                   BlockValidationContext context,
                                   List<ValidationMessage> messages) {
        
        // Get the marker style from block attributes
        String actualMarkerStyle = getMarkerStyle(block);
        
        if (actualMarkerStyle != null && !actualMarkerStyle.equals(expectedMarkerStyle)) {
            messages.add(ValidationMessage.builder()
                .severity(blockConfig.getSeverity())
                .ruleId("ulist.markerStyle")
                .location(context.createLocation(block))
                .message("Unordered list uses incorrect marker style")
                .actualValue(actualMarkerStyle)
                .expectedValue(expectedMarkerStyle)
                .build());
        }
    }
    
    private int calculateNestingLevel(StructuralNode block) {
        int level = 0;
        StructuralNode parent = null;
        
        // Get parent safely
        if (block.getParent() instanceof StructuralNode) {
            parent = (StructuralNode) block.getParent();
        }
        
        // Count parent lists
        while (parent != null) {
            if ("ulist".equals(parent.getContext()) || "olist".equals(parent.getContext())) {
                level++;
            }
            // Get next parent safely
            if (parent.getParent() instanceof StructuralNode) {
                parent = (StructuralNode) parent.getParent();
            } else {
                parent = null;
            }
        }
        
        return level;
    }
    
    private String getMarkerStyle(StructuralNode block) {
        // Try to get marker from attributes
        Object marker = block.getAttribute("marker");
        if (marker != null) {
            return marker.toString();
        }
        
        // Try to get style
        String style = block.getStyle();
        if (style != null) {
            return style;
        }
        
        // Default for unordered lists is usually "*"
        return "*";
    }
}