package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Listing.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for listing (code) blocks in AsciiDoc documents.
 * 
 * <p>This validator validates listing blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/listing-block.yaml}.
 * The YAML configuration is parsed into {@link ListingBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>language</b>: Validates programming language specification (required, allowed values)</li>
 *   <li><b>title</b>: Validates block title (required, pattern, length constraints)</li>
 *   <li><b>lines</b>: Validates line count (min/max)</li>
 *   <li><b>callouts</b>: Validates callout annotations (required, min/max count)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see ListingBlock
 * @see BlockTypeValidator
 */
public final class ListingBlockValidator extends AbstractBlockValidator<ListingBlock> {    
    @Override
    public BlockType getSupportedType() {
        return BlockType.LISTING;
    }
    
    @Override
    protected Class<ListingBlock> getBlockConfigClass() {
        return ListingBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               ListingBlock listingConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get listing attributes
        String language = getLanguage(block);
        String title = block.getTitle();
        String content = getBlockContent(block);
        
        // Validate language
        if (listingConfig.getLanguage() != null) {
            validateLanguage(language, listingConfig.getLanguage(), listingConfig, context, block, messages);
        }
        
        // Validate title
        if (listingConfig.getTitle() != null) {
            validateTitle(title, listingConfig.getTitle(), listingConfig, context, block, messages);
        }
        
        // Validate lines
        if (listingConfig.getLines() != null) {
            validateLines(content, listingConfig.getLines(), listingConfig, context, block, messages);
        }
        
        // Validate callouts
        if (listingConfig.getCallouts() != null) {
            validateCallouts(content, listingConfig.getCallouts(), listingConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getLanguage(StructuralNode block) {
        // Language can be in different attributes
        Object lang = block.getAttribute(LANGUAGE);
        if (lang != null) {
            return lang.toString();
        }
        
        // Try source attribute
        lang = block.getAttribute(SOURCE);
        if (lang != null) {
            return lang.toString();
        }
        
        // Check style for source blocks
        String style = block.getStyle();
        if (style != null && !"source".equals(style)) {
            return style;
        }
        
        return null;
    }
    
    // getBlockContent is now inherited from AbstractBlockValidator
    
    private void validateLanguage(String language, ListingBlock.LanguageConfig config,
                                ListingBlock blockConfig,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Check if language is required
        if (config.isRequired() && (language == null || language.trim().isEmpty())) {
            // Find column position for missing language
            SourcePosition pos = findSourcePosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(LANGUAGE_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .fromPosition(pos)
                    .build())
                .message("Listing language is required")
                .actualValue("No language")
                .expectedValue("Language required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("language")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.LIST_VALUE)
                    .build())
                .build());
        }
        
        // Validate allowed languages if specified
        if (language != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(language)) {
                // Find column position for invalid language
                SourcePosition pos = findLanguagePosition(block, context, language);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(LANGUAGE_ALLOWED)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Listing language '" + language + "' is not allowed")
                    .actualValue(language)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
                    .build());
            }
        }
    }
    
    private void validateTitle(String title, ListingBlock.TitleConfig config,
                             ListingBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TITLE_REQUIRED)
                .location(context.createLocation(block))
                .message("Listing block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null && config.getPattern() != null) {
            if (!config.getPattern().matcher(title).matches()) {
                SourcePosition pos = findTitlePosition(block, context, title);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_PATTERN)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .fromPosition(pos)
                        .build())
                    .message("Listing title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
        }
    }
    
    private void validateLines(String content, com.dataliquid.asciidoc.linter.config.rule.LineConfig config,
                             ListingBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.severity() != null ? config.severity() : blockConfig.getSeverity();
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (config.min() != null && lineCount < config.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(LINES_MIN)
                .location(context.createLocation(block))
                .message("Listing block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + config.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.max() != null && lineCount > config.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(LINES_MAX)
                .location(context.createLocation(block))
                .message("Listing block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + config.max() + " lines")
                .build());
        }
    }
    
    private void validateCallouts(String content, ListingBlock.CalloutsConfig config,
                                ListingBlock blockConfig,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(config.getSeverity(), blockConfig.getSeverity());
        
        // Count callouts in content
        int calloutCount = countCallouts(content);
        
        // Check if callouts are allowed
        if (!config.isAllowed() && calloutCount > 0) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CALLOUTS_NOT_ALLOWED)
                .location(context.createLocation(block))
                .message("Listing block must not contain callouts")
                .actualValue(calloutCount + " callouts")
                .expectedValue("No callouts allowed")
                .build());
        }
        
        // Validate max callouts
        if (config.getMax() != null && calloutCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CALLOUTS_MAX)
                .location(context.createLocation(block))
                .message("Listing block has too many callouts")
                .actualValue(String.valueOf(calloutCount))
                .expectedValue("At most " + config.getMax() + " callouts")
                .build());
        }
    }
    
    // countLines is now inherited from AbstractBlockValidator
    
    private int countCallouts(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // Count callouts in format <1>, <2>, etc.
        int count = 0;
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            // Simple pattern to find <number>
            if (line.matches(".*<\\d+>.*")) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Finds the column position where a language attribute should be or is located.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        return findLanguagePosition(block, context, null);
    }
    
    private SourcePosition findLanguagePosition(StructuralNode block, BlockValidationContext context, String language) {
        // Get the source line
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        if (blockLineNum <= 0 || blockLineNum > fileLines.size()) {
            return new SourcePosition(1, 1, blockLineNum);
        }
        
        // For listing blocks, the source location often points to the delimiter (----)
        // We need to look at the previous line for the [source] attribute
        int attributeLineNum = blockLineNum;
        String sourceLine = fileLines.get(blockLineNum - 1);
        
        // Check if current line is the delimiter
        if (sourceLine.trim().equals("----") && blockLineNum > 1) {
            // Look at previous line for [source] attribute
            attributeLineNum = blockLineNum - 1;
            sourceLine = fileLines.get(attributeLineNum - 1);
        }
        
        // Look for [source] or [source,language] pattern
        int sourceStart = sourceLine.indexOf("[source");
        if (sourceStart >= 0) {
            int sourceEnd = sourceLine.indexOf("]", sourceStart);
            if (sourceEnd > sourceStart) {
                if (language != null) {
                    // Find the specific language position
                    int langStart = sourceLine.indexOf(language, sourceStart);
                    if (langStart > sourceStart && langStart < sourceEnd) {
                        return new SourcePosition(langStart + 1, langStart + language.length(), attributeLineNum);
                    }
                }
                // For missing language, position after "[source"
                int commaPos = sourceLine.indexOf(",", sourceStart);
                if (commaPos > sourceStart && commaPos < sourceEnd) {
                    // Language should be after the comma
                    return new SourcePosition(commaPos + 2, commaPos + 2, attributeLineNum);
                } else {
                    // No comma, language should be added before "]"
                    // sourceEnd is 0-based index of ], add 1 for 1-based column
                    return new SourcePosition(sourceEnd + 1, sourceEnd + 1, attributeLineNum);
                }
            }
        }
        
        // Default to beginning of line
        return new SourcePosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of listing title.
     */
    private SourcePosition findTitlePosition(StructuralNode block, BlockValidationContext context, String title) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // Listing titles are typically on the line before the [source] attribute
        // Example:
        // .My Title
        // [source,java]
        // ----
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith(".") && line.trim().substring(1).equals(title)) {
                    // Found the title line
                    int titleStart = line.indexOf(".");
                    int titleEnd = titleStart + 1 + title.length();
                    return new SourcePosition(titleStart + 1, titleEnd, checkLine);
                }
            }
        }
        
        return new SourcePosition(1, 1, blockLineNum);
    }
    
    
}