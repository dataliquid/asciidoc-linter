package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

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
    private final FileContentCache fileCache = new FileContentCache();
    
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
        String passType = getAttributeAsString(block, "type");
        String passReason = getAttributeAsString(block, "reason");
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if type is required
        if (config.isRequired() && (passType == null || passType.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("pass.type.required")
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires a type")
                .actualValue("No type specified")
                .expectedValue("Type required (type attribute)")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("[pass,type=html]")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
        }
        
        // Validate allowed types if specified
        if (passType != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(passType)) {
                TypePosition pos = findTypePosition(block, context, passType);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("pass.type.allowed")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Pass block type '" + passType + "' is not allowed")
                    .actualValue(passType)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if content is required
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("pass.content.required")
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires content")
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
        
        // Validate max length
        if (content != null && config.getMaxLength() != null) {
            int contentLength = content.length();
            if (contentLength > config.getMaxLength()) {
                ContentPosition pos = findContentPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("pass.content.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Pass block content is too long")
                    .actualValue(contentLength + " characters")
                    .expectedValue("Maximum " + config.getMaxLength() + " characters")
                    .build());
            }
        }
        
        // Validate pattern
        if (content != null && config.getPattern() != null) {
            if (!config.getPattern().matcher(content).matches()) {
                ContentPosition pos = findContentPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("pass.content.pattern")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Pass block content does not match required pattern")
                    .actualValue("Content does not match pattern")
                    .expectedValue("Pattern: " + config.getPattern().pattern())
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if reason is required
        if (config.isRequired() && (passReason == null || passReason.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("pass.reason.required")
                .location(context.createLocation(block, 1, 1))
                .message("Pass block requires a reason")
                .actualValue("No reason provided")
                .expectedValue("Reason required (reason attribute)")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("[pass,reason=\"explanation\"]")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (passReason != null) {
            int reasonLength = passReason.length();
            
            // Validate min length
            if (config.getMinLength() != null && reasonLength < config.getMinLength()) {
                ReasonPosition pos = findReasonPosition(block, context, passReason);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("pass.reason.minLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Pass block reason is too short")
                    .actualValue(reasonLength + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && reasonLength > config.getMaxLength()) {
                ReasonPosition pos = findReasonPosition(block, context, passReason);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("pass.reason.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Pass block reason is too long")
                    .actualValue(reasonLength + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    /**
     * Finds the position of pass block content.
     */
    private ContentPosition findContentPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new ContentPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
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
                    return new ContentPosition(1, line.length(), i);
                }
            }
        }
        
        // Default to block line
        return new ContentPosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of type attribute or entire [pass] line.
     */
    private TypePosition findTypePosition(StructuralNode block, BlockValidationContext context, String passType) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new TypePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
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
                        return new TypePosition(valueStart + 1, valueEnd + 1, checkLine);
                    }
                    // If no type= found, position after the comma if there is one
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new TypePosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    // Default to after [pass
                    return new TypePosition(6, 6, checkLine);
                }
            }
        }
        
        return new TypePosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of reason attribute in [pass] line.
     */
    private ReasonPosition findReasonPosition(StructuralNode block, BlockValidationContext context, String reason) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new ReasonPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
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
                            return new ReasonPosition(valueStart + 1, valueEnd + 1, checkLine);
                        }
                    }
                    // If no reason= found, position after the comma if there is one
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new ReasonPosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    // Default to after [pass
                    return new ReasonPosition(6, 6, checkLine);
                }
            }
        }
        
        return new ReasonPosition(1, 1, blockLineNum);
    }
    
    private static class ContentPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        ContentPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    private static class ReasonPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        ReasonPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    private static class TypePosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        TypePosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}