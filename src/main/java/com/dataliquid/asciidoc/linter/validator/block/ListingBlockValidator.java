package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
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
        Object lang = block.getAttribute("language");
        if (lang != null) {
            return lang.toString();
        }
        
        // Try source attribute
        lang = block.getAttribute("source");
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if language is required
        if (config.isRequired() && (language == null || language.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("listing.language.required")
                .location(context.createLocation(block))
                .message("Listing block must specify a language")
                .actualValue("No language")
                .expectedValue("Language required")
                .build());
        }
        
        // Validate allowed languages if specified
        if (language != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(language)) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("listing.language.allowed")
                    .location(context.createLocation(block))
                    .message("Listing block has unsupported language")
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("listing.title.required")
                .location(context.createLocation(block))
                .message("Listing block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null && config.getPattern() != null) {
            if (!config.getPattern().matcher(title).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("listing.title.pattern")
                    .location(context.createLocation(block))
                    .message("Code listing title does not match required pattern")
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
                .ruleId("listing.lines.min")
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
                .ruleId("listing.lines.max")
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
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Count callouts in content
        int calloutCount = countCallouts(content);
        
        // Check if callouts are allowed
        if (!config.isAllowed() && calloutCount > 0) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("listing.callouts.notAllowed")
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
                .ruleId("listing.callouts.max")
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
}