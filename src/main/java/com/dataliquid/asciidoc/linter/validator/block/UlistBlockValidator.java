package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Ulist.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

/**
 * Validator for unordered list (ulist) blocks in AsciiDoc documents.
 * <p>
 * This validator validates unordered list blocks based on the YAML schema
 * structure defined in
 * {@code src/main/resources/schemas/blocks/ulist-block.yaml}. The YAML
 * configuration is parsed into {@link UlistBlock} objects which define the
 * validation rules.
 * </p>
 * <p>
 * Supported validation rules from YAML schema:
 * </p>
 * <ul>
 * <li><b>items</b>: Validates item count (min/max)</li>
 * <li><b>nestingLevel</b>: Validates maximum nesting depth</li>
 * <li><b>markerStyle</b>: Validates the list marker style (*, -, etc.)</li>
 * </ul>
 * <p>
 * Each nested configuration can optionally define its own severity level. If
 * not specified, the block-level severity is used as fallback.
 * </p>
 *
 * @see UlistBlock
 * @see BlockTypeValidator
 */
public final class UlistBlockValidator extends AbstractBlockValidator<UlistBlock> {
    private static final String ITEMS_UNIT = " items";
    // Constants for block contexts
    private static final String ULIST_CONTEXT = "ulist";

    @Override
    public BlockType getSupportedType() {
        return BlockType.ULIST;
    }

    @Override
    protected Class<UlistBlock> getBlockConfigClass() {
        return UlistBlock.class;
    }

    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, UlistBlock ulistConfig,
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

            // Also validate nested lists within list items
            validateNestedListMarkers(items, ulistConfig.getMarkerStyle(), ulistConfig, context, messages);
        }

        return messages;
    }

    private List<StructuralNode> getListItems(StructuralNode block) {
        // For unordered lists, items are in the blocks
        List<StructuralNode> items = block.getBlocks();
        return items != null ? items : new ArrayList<>();
    }

    private void validateItemsCount(List<StructuralNode> items, UlistBlock.ItemsConfig config, UlistBlock blockConfig,
            BlockValidationContext context, StructuralNode block, List<ValidationMessage> messages) {

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());

        int itemCount = items.size();

        // Validate min items
        if (config.getMin() != null && itemCount < config.getMin()) {
            SourcePosition pos = findItemInsertPosition(block, context, items);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(ITEMS_MIN)
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .message("Unordered list has too few items")
                            .errorType(ErrorType.MISSING_VALUE)
                            .actualValue(String.valueOf(itemCount))
                            .expectedValue("At least " + config.getMin() + ITEMS_UNIT)
                            .missingValueHint("* Item")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add list item with asterisk")
                                    .fixedValue("* ")
                                    .addExample("* First item")
                                    .addExample("* Second item")
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add list item with dash")
                                    .fixedValue("- ")
                                    .addExample("- First item")
                                    .addExample("- Second item")
                                    .build())
                            .build());
        }

        // Validate max items
        if (config.getMax() != null && itemCount > config.getMax()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(ITEMS_MAX)
                            .location(context.createLocation(block))
                            .message("Unordered list has too many items")
                            .actualValue(String.valueOf(itemCount))
                            .expectedValue("At most " + config.getMax() + ITEMS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Remove excess items")
                                    .addExample("Keep only the most important " + config.getMax() + ITEMS_UNIT)
                                    .addExample("Group related items into sub-lists")
                                    .explanation("Consider consolidating or removing " + (itemCount - config.getMax())
                                            + ITEMS_UNIT)
                                    .build())
                            .build());
        }
    }

    private void validateNestingLevel(StructuralNode block, UlistBlock.NestingLevelConfig config,
            UlistBlock blockConfig, BlockValidationContext context, List<ValidationMessage> messages) {

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());

        int nestingLevel = calculateNestingLevel(block);

        // Validate max nesting
        if (config.getMax() != null && nestingLevel > config.getMax()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(NESTING_LEVEL_MAX)
                            .location(context.createLocation(block))
                            .message("Unordered list exceeds maximum nesting level")
                            .actualValue(String.valueOf(nestingLevel))
                            .expectedValue("Maximum nesting level: " + config.getMax())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Flatten nested lists")
                                    .addExample("Move nested items to main level")
                                    .addExample("Use numbered sub-sections instead")
                                    .explanation("Reduce nesting to improve readability")
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Break into separate lists")
                                    .addExample("Split complex nested structure into multiple lists")
                                    .explanation("Consider using separate lists with headings")
                                    .build())
                            .build());
        }
    }

    private void validateMarkerStyle(StructuralNode block, String expectedMarkerStyle, UlistBlock blockConfig,
            BlockValidationContext context, List<ValidationMessage> messages) {

        // Get the marker style from block attributes
        String actualMarkerStyle = getMarkerStyle(block);

        if (actualMarkerStyle != null && !actualMarkerStyle.equals(expectedMarkerStyle)) {
            SourcePosition pos = findSourcePosition(block, context);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(blockConfig.getSeverity())
                            .ruleId(MARKER_STYLE)
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .message("Unordered list uses incorrect marker style")
                            .actualValue(actualMarkerStyle)
                            .expectedValue(expectedMarkerStyle)
                            .errorType(ErrorType.INVALID_ENUM)
                            .missingValueHint(expectedMarkerStyle)
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Change to " + expectedMarkerStyle + " marker")
                                    .fixedValue(expectedMarkerStyle)
                                    .addExample(expectedMarkerStyle + " First item")
                                    .addExample(expectedMarkerStyle + " Second item")
                                    .explanation("Use consistent marker style: " + expectedMarkerStyle)
                                    .build())
                            .build());
        }
    }

    private int calculateNestingLevel(StructuralNode block) {
        int level = 0;
        StructuralNode current = block;

        // Traverse parent chain using for-loop pattern
        for (StructuralNode parent = getStructuralParent(current); parent != null; parent = getStructuralParent(
                parent)) {
            if ("ulist".equals(parent.getContext()) || "olist".equals(parent.getContext())) {
                level++;
            }
        }

        return level;
    }

    private StructuralNode getStructuralParent(StructuralNode node) {
        if (node.getParent() instanceof StructuralNode) {
            return (StructuralNode) node.getParent();
        }
        return null;
    }

    private String getMarkerStyle(StructuralNode block) {
        // AsciidoctorJ doesn't provide marker style in attributes
        // We need to detect it from the source
        if (block.getSourceLocation() == null) {
            return null;
        }

        // Get the file lines from cache
        String filePath = block.getSourceLocation().getPath();
        List<String> fileLines = fileCache.getFileLines(filePath);
        int lineNum = block.getSourceLocation().getLineNumber();

        if (lineNum > 0 && lineNum <= fileLines.size()) {
            String line = fileLines.get(lineNum - 1);
            // Find the first non-whitespace character
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (!Character.isWhitespace(c)) {
                    // Check if it's a list marker
                    if (c == '*' || c == '-' || c == '.') {
                        return String.valueOf(c);
                    }
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Finds the position where new item should be inserted.
     */
    private SourcePosition findItemInsertPosition(StructuralNode block, BlockValidationContext context,
            List<StructuralNode> items) {
        if (block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, 1);
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
                    return new SourcePosition(line.length() + 1, line.length() + 1, lineNum);
                }
            }
        }

        // Otherwise position at the block start
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum > 0 && lineNum <= fileLines.size()) {
            String line = fileLines.get(lineNum - 1);
            return new SourcePosition(line.length() + 1, line.length() + 1, lineNum);
        }
        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Finds the position of the first marker in the list.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        if (block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, 1);
        }

        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        int lineNum = block.getSourceLocation().getLineNumber();

        if (lineNum > 0 && lineNum <= fileLines.size()) {
            String line = fileLines.get(lineNum - 1);
            // Find the marker character position
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (!Character.isWhitespace(c)) {
                    // Found the marker
                    if (c == '*' || c == '-' || c == '.') {
                        return new SourcePosition(i + 1, i + 1, lineNum);
                    }
                    break;
                }
            }
            // Default to start of line if no marker found
            return new SourcePosition(1, 1, lineNum);
        }

        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Validates marker styles in nested lists within list items.
     */
    private void validateNestedListMarkers(List<StructuralNode> items, String expectedMarkerStyle,
            UlistBlock blockConfig, BlockValidationContext context, List<ValidationMessage> messages) {
        for (StructuralNode item : items) {
            if (item.getBlocks() != null) {
                for (StructuralNode nestedBlock : item.getBlocks()) {
                    // Check if it's a nested ulist
                    if (ULIST_CONTEXT.equals(nestedBlock.getContext())) {
                        String nestedMarkerStyle = getMarkerStyle(nestedBlock);
                        if (nestedMarkerStyle != null && !nestedMarkerStyle.equals(expectedMarkerStyle)) {
                            SourcePosition pos = findSourcePosition(nestedBlock, context);
                            messages
                                    .add(ValidationMessage
                                            .builder()
                                            .severity(blockConfig.getSeverity())
                                            .ruleId(MARKER_STYLE)
                                            .location(SourceLocation
                                                    .builder()
                                                    .filename(context.getFilename())
                                                    .fromPosition(pos)
                                                    .build())
                                            .message("Unordered list marker style '" + nestedMarkerStyle
                                                    + "' does not match expected style '" + expectedMarkerStyle + "'")
                                            .actualValue(nestedMarkerStyle)
                                            .expectedValue(expectedMarkerStyle)
                                            .addSuggestion(Suggestion
                                                    .builder()
                                                    .description(
                                                            "Change nested list to " + expectedMarkerStyle + " marker")
                                                    .fixedValue(expectedMarkerStyle)
                                                    .addExample("  " + expectedMarkerStyle + " Nested item")
                                                    .explanation("Maintain consistent marker style in nested lists")
                                                    .build())
                                            .build());

                            // Recursively check nested items
                            List<StructuralNode> nestedItems = nestedBlock.getBlocks();
                            if (nestedItems != null) {
                                validateNestedListMarkers(nestedItems, expectedMarkerStyle, blockConfig, context,
                                        messages);
                            }
                        }
                    }
                }
            }
        }
    }

}
