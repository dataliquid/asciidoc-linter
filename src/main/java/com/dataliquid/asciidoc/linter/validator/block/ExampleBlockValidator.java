package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Example.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

/**
 * Validator for EXAMPLE blocks. Validates example blocks according to the YAML
 * schema definition, including caption format and collapsible attribute.
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
    protected List<ValidationMessage> performSpecificValidations(StructuralNode node, ExampleBlock exampleBlock,
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

    private List<ValidationMessage> validateCaption(StructuralNode node, ExampleBlock block,
            BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        ExampleBlock.CaptionConfig config = block.getCaption();

        String caption = node.getTitle();

        // Check if caption is required
        if (config.isRequired() && (caption == null || caption.trim().isEmpty())) {
            // Find position for caption placeholder
            int lineNumber = node.getSourceLocation() != null ? node.getSourceLocation().getLineNumber() : 1;
            messages
                    .add(ValidationMessage
                            .builder()
                            .message("Example block requires a caption")
                            .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                            .location(SourceLocation
                                    .builder()
                                    .filename(context.getFilename())
                                    .startLine(lineNumber)
                                    .endLine(lineNumber)
                                    .startColumn(1)
                                    .endColumn(1)
                                    .build())
                            .ruleId(CAPTION_REQUIRED)
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint(".Example Title")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add caption to example block")
                                    .fixedValue(".Example Title")
                                    .addExample(".Sample Configuration")
                                    .addExample(".Code Example")
                                    .addExample(".Usage Demonstration")
                                    .explanation(
                                            "Example blocks should have descriptive captions to explain their purpose")
                                    .build())
                            .build());
            return messages;
        }

        // If caption is not present and not required, skip further validation
        if (caption == null || caption.trim().isEmpty()) {
            return messages;
        }

        // Validate caption length
        if (config.getMinLength() != null && caption.length() < config.getMinLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .message(String
                                    .format("Example caption length %d is less than required minimum %d",
                                            caption.length(), config.getMinLength()))
                            .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                            .location(context.createLocation(node))
                            .ruleId(CAPTION_MIN_LENGTH)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Use a longer, more descriptive caption")
                                    .addExample("Sample Configuration File")
                                    .addExample("Complete Code Example")
                                    .addExample("Step-by-Step Demonstration")
                                    .explanation(
                                            "Example captions should be descriptive enough to meet minimum length requirements")
                                    .build())
                            .build());
        }

        if (config.getMaxLength() != null && caption.length() > config.getMaxLength()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .message(String
                                    .format("Example caption length %d exceeds maximum %d", caption.length(),
                                            config.getMaxLength()))
                            .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                            .location(context.createLocation(node))
                            .ruleId(CAPTION_MAX_LENGTH)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Shorten the caption")
                                    .addExample("Config Example")
                                    .addExample("Code Sample")
                                    .addExample("Usage Demo")
                                    .explanation(
                                            "Example captions should be concise and not exceed maximum length limits")
                                    .build())
                            .build());
        }

        // Validate caption pattern
        if (config.getPattern() != null && !config.getPattern().matcher(caption).matches()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .message(String
                                    .format("Example caption '%s' does not match required pattern '%s'", caption,
                                            config.getPattern().pattern()))
                            .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                            .location(context.createLocation(node))
                            .ruleId(CAPTION_PATTERN)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Format caption to match required pattern")
                                    .addExample("Example 1: Basic Setup")
                                    .addExample("Example 2: Advanced Configuration")
                                    .addExample("Example 3: Common Use Case")
                                    .explanation("Example captions must follow the specified format pattern")
                                    .build())
                            .build());
        }

        return messages;
    }

    private List<ValidationMessage> validateCollapsible(StructuralNode node, ExampleBlock block,
            BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        ExampleBlock.CollapsibleConfig config = block.getCollapsible();

        // Check for collapsible attribute
        Object collapsibleAttr = node.getAttribute(COLLAPSIBLE_OPTION);
        if (collapsibleAttr == null) {
            collapsibleAttr = node.getAttribute(COLLAPSIBLE);
        }

        // Check if collapsible is required
        if (config.isRequired() && collapsibleAttr == null) {
            // Find position for collapsible placeholder
            int lineNumber = node.getSourceLocation() != null ? node.getSourceLocation().getLineNumber() : 1;
            messages
                    .add(ValidationMessage
                            .builder()
                            .message("Example block requires a collapsible attribute")
                            .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                            .location(SourceLocation
                                    .builder()
                                    .filename(context.getFilename())
                                    .startLine(lineNumber)
                                    .endLine(lineNumber)
                                    .startColumn(1)
                                    .endColumn(1)
                                    .build())
                            .ruleId(COLLAPSIBLE_REQUIRED)
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("[%collapsible]")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add collapsible attribute to example block")
                                    .fixedValue("[%collapsible]")
                                    .addExample("[%collapsible]")
                                    .addExample("[example,%collapsible]")
                                    .addExample("[%collapsible%open]")
                                    .explanation(
                                            "Collapsible examples can be collapsed/expanded by readers for better content organization")
                                    .build())
                            .build());
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
                messages
                        .add(ValidationMessage
                                .builder()
                                .message(String
                                        .format("Example collapsible value '%s' is not in allowed values %s",
                                                collapsibleAttr, config.getAllowed()))
                                .severity(resolveSeverity(config.getSeverity(), block.getSeverity()))
                                .location(context.createLocation(node))
                                .ruleId(COLLAPSIBLE_ALLOWED)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Use a valid collapsible value")
                                        .fixedValue(config.getAllowed().contains(true) ? "[%collapsible]"
                                                : "[%collapsible=false]")
                                        .addExample("[%collapsible] for collapsible")
                                        .addExample("[%collapsible%open] for initially open")
                                        .addExample("Remove attribute for non-collapsible")
                                        .explanation("Collapsible attribute must be one of the allowed values")
                                        .build())
                                .build());
            }
        }

        return messages;
    }
}
