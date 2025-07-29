package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

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
    private final FileContentCache fileCache = new FileContentCache();
    
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
            ItemPosition pos = findItemInsertPosition(block, context, items);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("ulist.items.min")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Unordered list has too few items")
                .errorType(ErrorType.MISSING_VALUE)
                .actualValue(String.valueOf(itemCount))
                .expectedValue("At least " + config.getMin() + " items")
                .missingValueHint("* Item")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
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
            MarkerPosition pos = findMarkerPosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(blockConfig.getSeverity())
                .ruleId("ulist.markerStyle")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Unordered list uses incorrect marker style")
                .errorType(ErrorType.MISSING_VALUE)
                .actualValue(actualMarkerStyle)
                .expectedValue(expectedMarkerStyle)
                .missingValueHint(expectedMarkerStyle)
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
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
    
    /**
     * Finds the position where new item should be inserted.
     */
    private ItemPosition findItemInsertPosition(StructuralNode block, BlockValidationContext context, 
                                               List<StructuralNode> items) {
        if (block.getSourceLocation() == null) {
            return new ItemPosition(1, 1, 1);
        }
        
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        
        // If there are existing items, position after the last one
        if (!items.isEmpty()) {
            StructuralNode lastItem = items.get(items.size() - 1);
            if (lastItem.getSourceLocation() != null) {
                int lineNum = lastItem.getSourceLocation().getLineNumber();
                if (lineNum > 0 && lineNum <= fileLines.size()) {
                    String line = fileLines.get(lineNum - 1);
                    // Return position at end of line to insert on next line
                    return new ItemPosition(line.length() + 1, line.length() + 1, lineNum);
                }
            }
        }
        
        // Otherwise position at the block start
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum > 0 && lineNum <= fileLines.size()) {
            String line = fileLines.get(lineNum - 1);
            return new ItemPosition(line.length() + 1, line.length() + 1, lineNum);
        }
        return new ItemPosition(1, 1, lineNum);
    }
    
    /**
     * Finds the position of the first marker in the list.
     */
    private MarkerPosition findMarkerPosition(StructuralNode block, BlockValidationContext context) {
        if (block.getSourceLocation() == null) {
            return new MarkerPosition(1, 1, 1);
        }
        
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        int lineNum = block.getSourceLocation().getLineNumber();
        
        if (lineNum > 0 && lineNum <= fileLines.size()) {
            String line = fileLines.get(lineNum - 1);
            // Find the position of the marker (* or -)
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '*' || c == '-') {
                    // Found the marker - endColumn should be after the marker to replace it
                    return new MarkerPosition(i + 1, i + 1, lineNum);
                }
            }
        }
        
        return new MarkerPosition(1, 1, lineNum);
    }
    
    private static class ItemPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        ItemPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    private static class MarkerPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        MarkerPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}