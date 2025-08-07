package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Literal.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.SourcePosition;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for literal blocks in AsciiDoc documents.
 * 
 * <p>This validator validates literal blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/literal-block.yaml}.
 * The YAML configuration is parsed into {@link LiteralBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Literal blocks use .... delimiters and display preformatted text without syntax highlighting.
 * They are commonly used for configuration files, console output, or other plain text content.</p>
 * 
 * <p>Example usage in AsciiDoc:</p>
 * <pre>
 * .Example Configuration
 * ....
 * server:
 *   host: localhost
 *   port: 8080
 *   timeout: 30s
 * ....
 * </pre>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>title</b>: Validates optional title (required, minLength, maxLength)</li>
 *   <li><b>lines</b>: Validates line count (min, max)</li>
 *   <li><b>indentation</b>: Validates indentation consistency and constraints (required, consistent, minSpaces, maxSpaces)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see LiteralBlock
 * @see BlockTypeValidator
 */
public final class LiteralBlockValidator extends AbstractBlockValidator<LiteralBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.LITERAL;
    }
    
    @Override
    protected Class<LiteralBlock> getBlockConfigClass() {
        return LiteralBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               LiteralBlock config,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get literal block attributes and content
        String title = getTitle(block);
        List<String> lines = getContentLines(block);
        
        // Validate title
        if (config.getTitle() != null) {
            validateTitle(title, config.getTitle(), config, context, block, messages);
        }
        
        // Validate lines
        if (config.getLines() != null) {
            validateLines(lines, config.getLines(), config, context, block, messages);
        }
        
        // Validate indentation
        if (config.getIndentation() != null) {
            validateIndentation(lines, config.getIndentation(), config, context, block, messages);
        }
        
        return messages;
    }
    
    private String getTitle(StructuralNode block) {
        Object titleObj = block.getTitle();
        return titleObj != null ? titleObj.toString() : null;
    }
    
    private List<String> getContentLines(StructuralNode block) {
        List<String> lines = new ArrayList<>();
        String content = getBlockContent(block);
        
        if (!content.isEmpty()) {
            String[] contentLines = content.split("\n");
            for (String line : contentLines) {
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    private void validateTitle(String title, LiteralBlock.TitleConfig config,
                             LiteralBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Check if title is required
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            SourcePosition pos = findTitlePosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TITLE_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .fromPosition(pos)
                    .build())
                .message("Literal block requires a title")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(".Title")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (title != null && !title.trim().isEmpty()) {
            String trimmedTitle = title.trim();
            
            // Validate length constraints
            ValidationMessage lengthMessage = validateLength(trimmedTitle, 
                                                           config.getMinLength(), 
                                                           config.getMaxLength(),
                                                           "title", severity, context, block);
            addIfNotNull(messages, lengthMessage);
        }
    }
    
    private void validateLines(List<String> lines, LiteralBlock.LinesConfig config,
                             LiteralBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        int lineCount = lines.size();
        
        // Validate line count constraints
        ValidationMessage countMessage = validateMinMax(lineCount, 
                                                      config.getMin(), 
                                                      config.getMax(),
                                                      "Line count", severity, context, block);
        addIfNotNull(messages, countMessage);
    }
    
    private void validateIndentation(List<String> lines, LiteralBlock.IndentationConfig config,
                                   LiteralBlock blockConfig,
                                   BlockValidationContext context,
                                   StructuralNode block,
                                   List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Skip if indentation checking is not required
        if (!config.isRequired()) {
            return;
        }
        
        Integer firstIndentation = null;
        int lineNumber = 0;
        
        for (String line : lines) {
            lineNumber++;
            
            // Skip empty lines for indentation checking
            if (line.isEmpty() || line.trim().isEmpty()) {
                continue;
            }
            
            int indentSpaces = countLeadingSpaces(line);
            
            // Check minimum spaces
            if (config.getMinSpaces() != null && indentSpaces < config.getMinSpaces()) {
                String indentPlaceholder = " ".repeat(config.getMinSpaces());
                int currentLineNum = block.getSourceLocation() != null ? 
                    block.getSourceLocation().getLineNumber() + lineNumber : lineNumber;
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(INDENTATION_MIN_SPACES)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(currentLineNum)
                        .endLine(currentLineNum)
                        .startColumn(1)
                        .endColumn(1)
                        .build())
                    .message("Literal block requires minimum indentation of " + config.getMinSpaces() + " spaces")
                    .errorType(ErrorType.MISSING_VALUE)
                    .actualValue(indentSpaces + " spaces")
                    .expectedValue("At least " + config.getMinSpaces() + " spaces")
                    .missingValueHint(indentPlaceholder)
                    .placeholderContext(PlaceholderContext.builder()
                        .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                        .build())
                    .build());
                break; // Only report the first line with insufficient indentation
            }
            
            // Check maximum spaces
            if (config.getMaxSpaces() != null && indentSpaces > config.getMaxSpaces()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(INDENTATION_MAX_SPACES)
                    .location(context.createLocation(block))
                    .message("Line " + lineNumber + " has excessive indentation")
                    .actualValue(indentSpaces + " spaces")
                    .expectedValue("At most " + config.getMaxSpaces() + " spaces")
                    .build());
            }
            
            // Check consistency
            if (config.isConsistent()) {
                if (firstIndentation == null) {
                    firstIndentation = indentSpaces;
                } else if (indentSpaces != firstIndentation) {
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId(INDENTATION_CONSISTENT)
                        .location(context.createLocation(block))
                        .message("Line " + lineNumber + " has inconsistent indentation")
                        .actualValue(indentSpaces + " spaces")
                        .expectedValue(firstIndentation + " spaces (consistent with first non-empty line)")
                        .build());
                }
            }
        }
    }
    
    private int countLeadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                // Count tab as 4 spaces (configurable in future)
                count += 4;
            } else {
                break;
            }
        }
        return count;
    }
    
    /**
     * Finds the position where title should be inserted.
     */
    private SourcePosition findTitlePosition(StructuralNode block, BlockValidationContext context) {
        if (block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        return new SourcePosition(1, 1, lineNum);
    }
}