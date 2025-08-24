package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.util.StringUtils;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Abstract base class for block validators using Template Method pattern.
 * Provides common validation logic and helper methods to reduce code
 * duplication.
 *
 * @param <T> The specific block configuration type
 */
public abstract class AbstractBlockValidator<T extends Block> implements BlockTypeValidator {

    /**
     * Shared file content cache for accessing source file content. Used by
     * subclasses to find exact positions in source files for error reporting.
     */
    protected final FileContentCache fileCache = new FileContentCache();

    /**
     * Returns the specific block configuration class type. Used for safe casting of
     * the generic Block to the specific type.
     *
     * @return the class of the specific block configuration
     */
    protected abstract Class<T> getBlockConfigClass();

    /**
     * Performs block-specific validations. This method should contain all
     * validation logic specific to the block type.
     *
     * @param  node    the AsciiDoc node to validate
     * @param  config  the specific block configuration
     * @param  context the validation context
     *
     * @return         list of validation messages
     */
    protected abstract List<ValidationMessage> performSpecificValidations(StructuralNode node, T config,
            BlockValidationContext context);

    @Override
    public final List<ValidationMessage> validate(StructuralNode node, Block config, BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();

        // Safe cast to specific type
        if (!getBlockConfigClass().isInstance(config)) {
            return messages;
        }

        T typedConfig = getBlockConfigClass().cast(config);

        // Delegate to specific validation
        messages.addAll(performSpecificValidations(node, typedConfig, context));

        return messages;
    }

    // Common helper methods

    /**
     * Extracts text content from a structural node. Handles both direct content and
     * nested blocks.
     *
     * @param  node the node to extract content from
     *
     * @return      the text content or empty string if none
     */
    protected String getBlockContent(StructuralNode node) {
        if (node.getContent() instanceof String) {
            return (String) node.getContent();
        }

        // Handle nested blocks
        if (node.getBlocks() != null && !node.getBlocks().isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (Object block : node.getBlocks()) {
                if (block instanceof StructuralNode) {
                    StructuralNode childNode = (StructuralNode) block;
                    if (childNode.getContent() instanceof String) {
                        content.append(childNode.getContent()).append('\n');
                    }
                }
            }
            return content.toString().trim();
        }

        return "";
    }

    /**
     * Counts lines in text content.
     *
     * @param  content the text content
     *
     * @return         number of lines
     */
    protected int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.split("\n").length;
    }

    /**
     * Resolves severity with fallback. If specific severity is null, uses the
     * fallback severity.
     *
     * @param  specific the specific severity (may be null)
     * @param  fallback the fallback severity
     *
     * @return          resolved severity
     */
    protected Severity resolveSeverity(Severity specific, Severity fallback) {
        return specific != null ? specific : fallback;
    }

    /**
     * Creates a validation message for required field violations.
     *
     * @param  fieldName the name of the required field
     * @param  severity  the severity level
     * @param  context   the validation context
     * @param  node      the node being validated
     *
     * @return           validation message
     */
    protected ValidationMessage createRequiredFieldMessage(String fieldName, Severity severity,
            BlockValidationContext context, StructuralNode node) {
        return ValidationMessage
                .builder()
                .severity(severity)
                .ruleId(getSupportedType().toValue() + "." + fieldName + ".required")
                .location(context.createLocation(node))
                .message("Literal block must have a " + fieldName)
                .actualValue("No " + fieldName)
                .expectedValue(
                        fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1) + " required")
                .build();
    }

    /**
     * Creates a validation message for pattern violations.
     *
     * @param  fieldName the name of the field
     * @param  value     the actual value
     * @param  pattern   the expected pattern
     * @param  severity  the severity level
     * @param  context   the validation context
     * @param  node      the node being validated
     *
     * @return           validation message
     */
    protected ValidationMessage createPatternViolationMessage(String fieldName, String value, String pattern,
            Severity severity, BlockValidationContext context, StructuralNode node) {
        return ValidationMessage
                .builder()
                .severity(severity)
                .ruleId(getSupportedType().toValue() + "." + fieldName + ".pattern")
                .location(context.createLocation(node))
                .message(fieldName + " does not match pattern: " + pattern + ". Actual: "
                        + (value != null ? value : "null"))
                .build();
    }

    /**
     * Creates a validation message for length violations.
     *
     * @param  fieldName    the name of the field
     * @param  actualLength the actual length
     * @param  minLength    the minimum length (may be null)
     * @param  maxLength    the maximum length (may be null)
     * @param  severity     the severity level
     * @param  context      the validation context
     * @param  node         the node being validated
     *
     * @return              validation message
     */
    protected ValidationMessage createLengthViolationMessage(String fieldName, int actualLength, Integer minLength,
            Integer maxLength, Severity severity, BlockValidationContext context, StructuralNode node) {
        StringBuilder message = new StringBuilder(fieldName);
        String ruleIdSuffix;
        String expectedValue;
        String actualValue = actualLength + " characters";

        if (minLength != null && actualLength < minLength) {
            message.append(" is too short");
            ruleIdSuffix = "minLength";
            expectedValue = "At least " + minLength + " characters";
        } else if (maxLength != null && actualLength > maxLength) {
            message.append(" is too long");
            ruleIdSuffix = "maxLength";
            expectedValue = "At most " + maxLength + " characters";
        } else {
            // Should not happen, but handle gracefully
            return null;
        }

        return ValidationMessage
                .builder()
                .severity(severity)
                .ruleId(getSupportedType().toValue() + "." + fieldName + "." + ruleIdSuffix)
                .location(context.createLocation(node))
                .message(message.toString())
                .actualValue(actualValue)
                .expectedValue(expectedValue)
                .build();
    }

    /**
     * Validates a required field.
     *
     * @param  value     the value to check
     * @param  fieldName the name of the field
     * @param  required  whether the field is required
     * @param  severity  the severity level
     * @param  context   the validation context
     * @param  node      the node being validated
     *
     * @return           validation message if validation fails, null otherwise
     */
    protected ValidationMessage validateRequired(String value, String fieldName, boolean required, Severity severity,
            BlockValidationContext context, StructuralNode node) {
        if (required && StringUtils.isBlank(value)) {
            return createRequiredFieldMessage(fieldName, severity, context, node);
        }
        return null;
    }

    /**
     * Validates a value against a pattern.
     *
     * @param  value      the value to validate
     * @param  patternStr the pattern string
     * @param  fieldName  the name of the field
     * @param  severity   the severity level
     * @param  context    the validation context
     * @param  node       the node being validated
     *
     * @return            validation message if validation fails, null otherwise
     */
    protected ValidationMessage validatePattern(String value, String patternStr, String fieldName, Severity severity,
            BlockValidationContext context, StructuralNode node) {
        if (value != null && patternStr != null) {
            try {
                Pattern pattern = Pattern.compile(patternStr);
                if (!pattern.matcher(value).matches()) {
                    return createPatternViolationMessage(fieldName, value, patternStr, severity, context, node);
                }
            } catch (PatternSyntaxException e) {
                return ValidationMessage
                        .builder()
                        .severity(Severity.ERROR)
                        .ruleId(getSupportedType().toValue() + "." + fieldName + ".pattern.invalid")
                        .location(context.createLocation(node))
                        .message("Invalid pattern for " + fieldName + ": " + e.getMessage())
                        .build();
            }
        }
        return null;
    }

    /**
     * Validates string length constraints.
     *
     * @param  value     the value to validate
     * @param  minLength minimum length constraint
     * @param  maxLength maximum length constraint
     * @param  fieldName the name of the field
     * @param  severity  the severity level
     * @param  context   the validation context
     * @param  node      the node being validated
     *
     * @return           validation message if validation fails, null otherwise
     */
    protected ValidationMessage validateLength(String value, Integer minLength, Integer maxLength, String fieldName,
            Severity severity, BlockValidationContext context, StructuralNode node) {
        if (value != null && (minLength != null || maxLength != null)) {
            int length = value.length();
            if ((minLength != null && length < minLength) || (maxLength != null && length > maxLength)) {
                return createLengthViolationMessage(fieldName, length, minLength, maxLength, severity, context, node);
            }
        }
        return null;
    }

    /**
     * Validates numeric constraints.
     *
     * @param  value     the value to validate
     * @param  min       minimum constraint
     * @param  max       maximum constraint
     * @param  fieldName the name of the field
     * @param  severity  the severity level
     * @param  context   the validation context
     * @param  node      the node being validated
     *
     * @return           validation message if validation fails, null otherwise
     */
    protected ValidationMessage validateMinMax(int value, Integer min, Integer max, String fieldName, Severity severity,
            BlockValidationContext context, StructuralNode node) {
        if ((min != null && value < min) || (max != null && value > max)) {
            StringBuilder message = new StringBuilder(fieldName);
            String ruleIdSuffix;
            String actualValue;
            String expectedValue;

            // Special handling for line count
            boolean isLineCount = fieldName.toLowerCase(Locale.ROOT).contains("line");
            String unit = isLineCount ? " lines" : "";

            if (min != null && value < min) {
                message.append(" is too small");
                ruleIdSuffix = "min";
                actualValue = value + unit;
                expectedValue = "At least " + min + unit;
            } else if (max != null && value > max) {
                message.append(" is too large");
                ruleIdSuffix = "max";
                actualValue = value + unit;
                expectedValue = "At most " + max + unit;
            } else {
                return null;
            }

            // Construct rule ID - for lines, use "lines.min" or "lines.max"
            String ruleIdBase;
            if (isLineCount) {
                ruleIdBase = getSupportedType().toValue() + ".lines";
            } else {
                ruleIdBase = getSupportedType().toValue() + "." + fieldName.toLowerCase(Locale.ROOT).replace(" ", "");
            }

            return ValidationMessage
                    .builder()
                    .severity(severity)
                    .ruleId(ruleIdBase + "." + ruleIdSuffix)
                    .location(context.createLocation(node))
                    .message(message.toString())
                    .actualValue(actualValue)
                    .expectedValue(expectedValue)
                    .build();
        }
        return null;
    }

    /**
     * Helper method to add a validation message to the list if not null.
     *
     * @param messages the list to add to
     * @param message  the message to add (may be null)
     */
    protected void addIfNotNull(List<ValidationMessage> messages, ValidationMessage message) {
        if (message != null) {
            messages.add(message);
        }
    }
}
