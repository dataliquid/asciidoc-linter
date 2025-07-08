package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validates block occurrence rules (min/max occurrences).
 */
public final class BlockOccurrenceValidator {
    
    /**
     * Validates occurrence rules for all blocks in a section.
     * 
     * @param context the validation context containing tracked blocks
     * @param blocks the configured blocks with their occurrence rules
     * @return list of validation messages
     */
    public List<ValidationMessage> validate(BlockValidationContext context,
                                          List<Block> blocks) {
        Objects.requireNonNull(context, "[" + getClass().getName() + "] context must not be null");
        Objects.requireNonNull(blocks, "[" + getClass().getName() + "] blocks must not be null");
        
        // Validate occurrences for all blocks
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        for (Block block : blocks) {
            // Check if block has occurrence constraints
            if (block.getOccurrence() != null) {
                validateOccurrences(block, context, messages);
            }
        }
        
        return messages;
    }
    
    /**
     * Validates occurrences for a specific block configuration.
     */
    private void validateOccurrences(Block block,
                                   BlockValidationContext context,
                                   List<ValidationMessage> messages) {
        
        com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig occurrences = block.getOccurrence();
        int actualCount = context.getOccurrenceCount(block);
        String blockName = context.getBlockName(block);
        
        // Validate occurrence count
        
        // Determine severity: use occurrence-specific severity if present, otherwise fall back to block severity
        com.dataliquid.asciidoc.linter.config.Severity severity = occurrences.severity() != null 
            ? occurrences.severity() 
            : block.getSeverity();
        
        // Validate minimum occurrences
        if (actualCount < occurrences.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("block.occurrences.min")
                .location(createSectionLocation(context))
                .message("Too few occurrences of " + blockName)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At least " + occurrences.min() + " occurrences")
                .build());
        }
        
        // Validate maximum occurrences
        if (actualCount > occurrences.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("block.occurrences.max")
                .location(createSectionLocation(context))
                .message("Too many occurrences of " + blockName)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At most " + occurrences.max() + " occurrences")
                .build());
        }
        
    }
    
    /**
     * Creates a location for the section.
     */
    private com.dataliquid.asciidoc.linter.validator.SourceLocation createSectionLocation(
            BlockValidationContext context) {
        
        return com.dataliquid.asciidoc.linter.validator.SourceLocation.builder()
            .filename(context.getFilename())
            .startLine(1) // Section start
            .build();
    }
}