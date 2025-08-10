package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Pass.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

/**
 * Validator for pass (passthrough) blocks in AsciiDoc documents.
 * 
 * <p>This validator validates pass blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/pass-block.yaml}.
 * The YAML configuration is parsed into {@link PassBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Pass blocks use ++++ delimiters and pass content through without processing.
 * This validator uses custom attributes that are not native to AsciiDoc:</p>
 * <ul>
 *   <li><b>pass-type</b>: Custom attribute specifying content type (html, xml, svg)</li>
 *   <li><b>pass-reason</b>: Custom attribute providing reason for using raw passthrough</li>
 * </ul>
 * 
 * <p>Example usage in AsciiDoc:</p>
 * <pre>
 * [pass,pass-type=html,pass-reason="Custom widget for product gallery"]
 * ++++
 * &lt;div class="product-slider"&gt;
 *   &lt;img src="product1.jpg" alt="Product 1"&gt;
 * &lt;/div&gt;
 * ++++
 * </pre>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>type</b>: Validates content type specification (required, allowed values)</li>
 *   <li><b>content</b>: Validates content (required, maxLength, pattern)</li>
 *   <li><b>reason</b>: Validates reason (required, minLength, maxLength)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see PassBlock
 * @see BlockTypeValidator
 */
public final class PassBlockValidator extends AbstractBlockValidator<PassBlock> {    
    @Override
    public BlockType getSupportedType() {
        return BlockType.PASS;
    }
    
    @Override
    protected Class<PassBlock> getBlockConfigClass() {
        return PassBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               PassBlock passConfig,
                                                               BlockValidationContext context) {
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get pass block attributes
        String passType = getAttributeAsString(block, TYPE);
        String passReason = getAttributeAsString(block, REASON);
        String content = getBlockContent(block);
        
        // Validate type
        if (passConfig.getTypeConfig() != null) {
            validateType(passType, passConfig.getTypeConfig(), passConfig, context, block, messages);
        }
        
        // Validate content
        if (passConfig.getContent() != null) {
            validateContent(content, passConfig.getContent(), passConfig, context, block, messages);
        }
        
        // Validate reason
        if (passConfig.getReason() != null) {
            validateReason(passReason, passConfig.getReason(), passConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getAttributeAsString(StructuralNode block, String attributeName) {
        Object value = block.getAttribute(attributeName);
        return value != null ? value.toString() : null;
    }
    
    
    private void validateType(String passType, PassBlock.TypeConfig config,
                            PassBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Check if type is required
        if (config.isRequired() && (passType == null || passType.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TYPE_REQUIRED)
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires a type")
                .actualValue("No type specified")
                .expectedValue("Type required (type attribute)")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("[pass,type=html]")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Add type attribute to pass block")
                    .fixedValue("[pass,type=html]")
                    .addExample("[pass,type=html]")
                    .addExample("[pass,type=xml]")
                    .addExample("[pass,type=svg]")
                    .explanation("Pass blocks should specify the content type for proper processing")
                    .build())
                .build());
        }
        
        // Validate allowed types if specified
        if (passType != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(passType)) {
                SourcePosition pos = findPassTypePosition(block, context, passType);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TYPE_ALLOWED)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Pass block type '" + passType + "' is not allowed")
                    .actualValue(passType)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
                    .addSuggestion(Suggestion.builder()
                        .description("Use a valid pass block type")
                        .fixedValue("[pass,type=" + config.getAllowed().get(0) + "]")
                        .addExample("[pass,type=html] for HTML content")
                        .addExample("[pass,type=xml] for XML content")
                        .addExample("[pass,type=svg] for SVG graphics")
                        .explanation("Pass block type must be one of the allowed content types")
                        .build())
                    .build());
            }
        }
    }
    
    private void validateContent(String content, PassBlock.ContentConfig config,
                               PassBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Check if content is required
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_REQUIRED)
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires content")
                .actualValue("No content")
                .expectedValue("Content required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("Content")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Add content to pass block")
                    .addExample("<div>HTML content</div>")
                    .addExample("<?xml version=\"1.0\"?>")
                    .addExample("<svg>SVG content</svg>")
                    .explanation("Pass blocks must contain the raw content to be passed through")
                    .build())
                .build());
            return;
        }
        
        // Validate max length
        if (content != null && config.getMaxLength() != null) {
            int contentLength = content.length();
            if (contentLength > config.getMaxLength()) {
                SourcePosition pos = findSourcePosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(CONTENT_MAX_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Pass block content is too long")
                    .actualValue(contentLength + " characters")
                    .expectedValue("Maximum " + config.getMaxLength() + " characters")
                    .addSuggestion(Suggestion.builder()
                        .description("Shorten pass block content")
                        .addExample("Remove non-essential markup")
                        .addExample("Split into multiple pass blocks")
                        .addExample("Use external files for large content")
                        .explanation("Pass block content should not exceed the maximum length limit")
                        .build())
                    .build());
            }
        }
        
        // Validate pattern
        if (content != null && config.getPattern() != null) {
            if (!config.getPattern().matcher(content).matches()) {
                SourcePosition pos = findSourcePosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(CONTENT_PATTERN)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Pass block content does not match required pattern")
                    .actualValue("Content does not match pattern")
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .addSuggestion(Suggestion.builder()
                        .description("Format pass block content to match pattern")
                        .addExample("Ensure proper markup syntax")
                        .addExample("Check for valid HTML/XML structure")
                        .addExample("Validate content format")
                        .explanation("Pass block content must follow the specified format pattern")
                        .build())
                    .build());
            }
        }
    }
    
    private void validateReason(String passReason, PassBlock.ReasonConfig config,
                                     PassBlock blockConfig,
                                     BlockValidationContext context,
                                     StructuralNode block,
                                     List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Check if reason is required
        if (config.isRequired() && (passReason == null || passReason.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(REASON_REQUIRED)
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires a reason")
                .actualValue("No reason provided")
                .expectedValue("Reason required (reason attribute)")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("[pass,reason=\"explanation\"]")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Add reason for using pass block")
                    .fixedValue("[pass,reason=\"Custom HTML widget\"]")
                    .addExample("[pass,reason=\"Custom HTML widget\"]")
                    .addExample("[pass,reason=\"Third-party integration\"]")
                    .addExample("[pass,reason=\"Legacy markup support\"]")
                    .explanation("Pass blocks should explain why raw passthrough is necessary")
                    .build())
                .build());
            return;
        }
        
        if (passReason != null) {
            int reasonLength = passReason.length();
            
            // Validate min length
            if (config.getMinLength() != null && reasonLength < config.getMinLength()) {
                SourcePosition pos = findReasonPosition(block, context, passReason);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(REASON_MIN_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Pass block reason is too short")
                    .actualValue(reasonLength + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .addSuggestion(Suggestion.builder()
                        .description("Provide a more detailed reason")
                        .addExample("Custom HTML widget for interactive content")
                        .addExample("Third-party JavaScript integration required")
                        .addExample("Legacy markup that cannot be converted")
                        .explanation("Pass block reasons should be detailed enough to meet minimum length requirements")
                        .build())
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && reasonLength > config.getMaxLength()) {
                SourcePosition pos = findReasonPosition(block, context, passReason);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(REASON_MAX_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Pass block reason is too long")
                    .actualValue(reasonLength + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .addSuggestion(Suggestion.builder()
                        .description("Shorten the reason description")
                        .addExample("Custom widget")
                        .addExample("Third-party integration")
                        .addExample("Legacy markup")
                        .explanation("Pass block reasons should be concise and not exceed maximum length limits")
                        .build())
                    .build());
            }
        }
    }
    
    /**
     * Finds the position of pass block content.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        String content = getBlockContent(block);
        
        if (content != null && !content.isEmpty()) {
            // Content is between the ++++ delimiters
            // Find the first line of actual content
            for (int i = blockLineNum; i < fileLines.size() && i < blockLineNum + 10; i++) {
                String line = fileLines.get(i - 1);
                if (!line.trim().equals("++++") && !line.trim().isEmpty() && !line.trim().startsWith("[")) {
                    // Found content line
                    return new SourcePosition(1, line.length(), i);
                }
            }
        }
        
        // Default to block line
        return new SourcePosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of type attribute or entire [pass] line.
     */
    private SourcePosition findPassTypePosition(StructuralNode block, BlockValidationContext context, String passType) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [pass] line is typically one or two lines before the block delimiter ++++
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[pass")) {
                    // Found the [pass] line
                    int typePos = line.indexOf("type=");
                    if (typePos >= 0 && passType != null) {
                        // Find the position of the type value
                        int valueStart = typePos + 5; // after "type="
                        // Find the end of the value (comma or closing bracket)
                        int valueEnd = valueStart;
                        for (int i = valueStart; i < line.length(); i++) {
                            char ch = line.charAt(i);
                            if (ch == ',' || ch == ']') {
                                break;
                            }
                            valueEnd = i;
                        }
                        return new SourcePosition(valueStart + 1, valueEnd + 1, checkLine);
                    }
                    // If no type= found, position after the comma if there is one
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new SourcePosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    // Default to after [pass
                    return new SourcePosition(6, 6, checkLine);
                }
            }
        }
        
        return new SourcePosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of reason attribute in [pass] line.
     */
    private SourcePosition findReasonPosition(StructuralNode block, BlockValidationContext context, String reason) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [pass] line is typically one or two lines before the block delimiter ++++
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[pass")) {
                    // Found the [pass] line
                    if (reason != null) {
                        int reasonStart = line.indexOf("reason=");
                        if (reasonStart >= 0) {
                            // Find the position of the reason value
                            int valueStart = reasonStart + 7; // after "reason="
                            // Find the end of the value (comma or closing bracket)
                            int valueEnd = valueStart;
                            for (int i = valueStart; i < line.length(); i++) {
                                char ch = line.charAt(i);
                                if (ch == ',' || ch == ']') {
                                    break;
                                }
                                valueEnd = i;
                            }
                            return new SourcePosition(valueStart + 1, valueEnd + 1, checkLine);
                        }
                    }
                    // If no reason= found, position after the comma if there is one
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new SourcePosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    // Default to after [pass
                    return new SourcePosition(6, 6, checkLine);
                }
            }
        }
        
        return new SourcePosition(1, 1, blockLineNum);
    }
    
    
    
}