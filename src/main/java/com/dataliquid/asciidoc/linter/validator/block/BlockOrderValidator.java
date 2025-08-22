package com.dataliquid.asciidoc.linter.validator.block;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Block.*;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.OrderConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validates block order constraints within sections.
 */
public final class BlockOrderValidator {

    /**
     * Validates order constraints for blocks in a section.
     *
     * @param context
     *            the validation context containing tracked blocks
     * @param orderConfig
     *            the order configuration to validate
     * @return list of validation messages
     */
    public List<ValidationMessage> validate(BlockValidationContext context, OrderConfig orderConfig) {
        if (context == null || orderConfig == null) {
            return List.of();
        }

        List<ValidationMessage> messages = new ArrayList<>();
        List<BlockValidationContext.BlockPosition> actualOrder = context.getBlockOrder();

        // Validate fixed order
        if (orderConfig.fixedOrder() != null && !orderConfig.fixedOrder().isEmpty()) {
            validateFixedOrder(actualOrder, orderConfig, context, messages);
        }

        // Validate before constraints
        if (orderConfig.before() != null && !orderConfig.before().isEmpty()) {
            validateBeforeConstraints(actualOrder, orderConfig, context, messages);
        }

        // Validate after constraints
        if (orderConfig.after() != null && !orderConfig.after().isEmpty()) {
            validateAfterConstraints(actualOrder, orderConfig, context, messages);
        }

        return messages;
    }

    /**
     * Validates that blocks appear in a fixed order.
     */
    private void validateFixedOrder(List<BlockValidationContext.BlockPosition> actualOrder, OrderConfig orderConfig,
            BlockValidationContext context, List<ValidationMessage> messages) {

        List<String> expectedOrder = orderConfig.fixedOrder();
        int expectedIndex = 0;

        for (BlockValidationContext.BlockPosition position : actualOrder) {
            String blockIdentifier = getBlockIdentifier(position.getConfig());

            // Check if this block is in the expected order
            int foundIndex = expectedOrder.indexOf(blockIdentifier);
            if (foundIndex >= 0) {
                if (foundIndex < expectedIndex) {
                    // Block appears out of order
                    messages.add(ValidationMessage.builder().severity(orderConfig.severity()).ruleId(ORDER_FIXED)
                            .location(context.createLocation(position.getBlock()))
                            .message("Block '" + blockIdentifier + "' appears out of order")
                            .actualValue("Position " + (position.getIndex() + 1))
                            .expectedValue("Should appear after '" + expectedOrder.get(expectedIndex - 1) + "'")
                            .build());
                } else {
                    expectedIndex = foundIndex + 1;
                }
            }
        }
    }

    /**
     * Validates before constraints (blockA must come before blockB).
     */
    private void validateBeforeConstraints(List<BlockValidationContext.BlockPosition> actualOrder,
            OrderConfig orderConfig, BlockValidationContext context, List<ValidationMessage> messages) {

        for (OrderConfig.OrderConstraint constraint : orderConfig.before()) {
            validateBeforeConstraint(actualOrder, constraint, context, messages);
        }
    }

    private void validateBeforeConstraint(List<BlockValidationContext.BlockPosition> actualOrder,
            OrderConfig.OrderConstraint constraint, BlockValidationContext context, List<ValidationMessage> messages) {

        Integer firstPos = null;
        Integer secondPos = null;

        // Find positions of both blocks
        for (BlockValidationContext.BlockPosition position : actualOrder) {
            String identifier = getBlockIdentifier(position.getConfig());

            if (identifier.equals(constraint.first())) {
                firstPos = position.getIndex();
            }
            if (identifier.equals(constraint.second())) {
                secondPos = position.getIndex();
            }
        }

        // Validate constraint if both blocks exist
        if (firstPos != null && secondPos != null && firstPos > secondPos) {
            messages.add(ValidationMessage.builder().severity(constraint.severity()).ruleId(ORDER_BEFORE)
                    .location(createSectionLocation(context))
                    .message("Block '" + constraint.first() + "' must appear before '" + constraint.second() + "'")
                    .actualValue("'" + constraint.first() + "' at position " + (firstPos + 1) + ", '"
                            + constraint.second() + "' at position " + (secondPos + 1))
                    .expectedValue("'" + constraint.first() + "' before '" + constraint.second() + "'").build());
        }
    }

    /**
     * Validates after constraints (blockA must come after blockB).
     */
    private void validateAfterConstraints(List<BlockValidationContext.BlockPosition> actualOrder,
            OrderConfig orderConfig, BlockValidationContext context, List<ValidationMessage> messages) {

        for (OrderConfig.OrderConstraint constraint : orderConfig.after()) {
            validateAfterConstraint(actualOrder, constraint, context, messages);
        }
    }

    private void validateAfterConstraint(List<BlockValidationContext.BlockPosition> actualOrder,
            OrderConfig.OrderConstraint constraint, BlockValidationContext context, List<ValidationMessage> messages) {

        Integer firstPos = null;
        Integer secondPos = null;

        // Find positions of both blocks
        for (BlockValidationContext.BlockPosition position : actualOrder) {
            String identifier = getBlockIdentifier(position.getConfig());

            if (identifier.equals(constraint.first())) {
                firstPos = position.getIndex();
            }
            if (identifier.equals(constraint.second())) {
                secondPos = position.getIndex();
            }
        }

        // Validate constraint if both blocks exist
        if (firstPos != null && secondPos != null && firstPos < secondPos) {
            messages.add(ValidationMessage.builder().severity(constraint.severity()).ruleId(ORDER_AFTER)
                    .location(createSectionLocation(context))
                    .message("Block '" + constraint.first() + "' must appear after '" + constraint.second() + "'")
                    .actualValue("'" + constraint.first() + "' at position " + (firstPos + 1) + ", '"
                            + constraint.second() + "' at position " + (secondPos + 1))
                    .expectedValue("'" + constraint.first() + "' after '" + constraint.second() + "'").build());
        }
    }

    /**
     * Gets the identifier for a block (name or type).
     */
    private String getBlockIdentifier(Block block) {
        if (block.getName() != null) {
            return block.getName();
        }
        return block.getType().toString().toLowerCase();
    }

    /**
     * Creates a location for the section.
     */
    private com.dataliquid.asciidoc.linter.validator.SourceLocation createSectionLocation(
            BlockValidationContext context) {

        int lineNumber = -1; // Default if no source location (should not happen)

        // Get the actual line number from the container (Section or Document)
        org.asciidoctor.ast.StructuralNode container = context.getContainer();
        if (container != null && container.getSourceLocation() != null) {
            lineNumber = container.getSourceLocation().getLineNumber();
        }

        return com.dataliquid.asciidoc.linter.validator.SourceLocation.builder().filename(context.getFilename())
                .startLine(lineNumber).build();
    }
}
