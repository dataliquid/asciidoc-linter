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
        String blockType = block.getType().toString().toLowerCase();
        
        // Validate occurrence count
        
        // Determine severity: use occurrence-specific severity if present, otherwise fall back to block severity
        com.dataliquid.asciidoc.linter.config.Severity severity = occurrences.severity() != null 
            ? occurrences.severity() 
            : block.getSeverity();
        
        // Validate minimum occurrences
        if (actualCount < occurrences.min()) {
            // Generate placeholder for missing block
            String blockPlaceholder = generateBlockPlaceholder(blockType);
            
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("block.occurrence.min")
                .location(createSectionLocation(context))
                .message("Too few occurrences of block: " + blockType)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At least " + occurrences.min() + " occurrences")
                .errorType(com.dataliquid.asciidoc.linter.validator.ErrorType.MISSING_VALUE)
                .missingValueHint(blockPlaceholder)
                .placeholderContext(com.dataliquid.asciidoc.linter.validator.PlaceholderContext.builder()
                    .type(com.dataliquid.asciidoc.linter.validator.PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
        }
        
        // Validate maximum occurrences
        if (actualCount > occurrences.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("block.occurrence.max")
                .location(createSectionLocation(context))
                .message("Too many occurrences of block: " + blockType)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At most " + occurrences.max() + " occurrences")
                .build());
        }
        
    }
    
    /**
     * Generates a placeholder hint for a missing block.
     */
    private String generateBlockPlaceholder(String blockName) {
        // Basic placeholders for common block types
        switch (blockName.toLowerCase()) {
            case "paragraph":
                return "Paragraph content";
            case "listing":
                return "[source]\n----\nCode here\n----";
            case "image":
                return "image::filename.png[]";
            case "table":
                return "|===\n| Header 1 | Header 2\n| Data 1 | Data 2\n|===";
            case "quote":
                return "[quote]\n____\nQuote content\n____";
            case "example":
                return "====\nExample content\n====";
            case "sidebar":
                return "****\nSidebar content\n****";
            case "verse":
                return "[verse]\n____\nVerse content\n____";
            case "literal":
                return "....\nLiteral content\n....";
            case "admonition":
                return "[NOTE]\n====\nNote content\n====";
            case "ulist":
                return "* Item";
            case "olist":
                return ". Item";
            case "dlist":
                return "Term:: Description";
            default:
                return blockName + " content";
        }
    }
    
    /**
     * Creates a location for the section.
     */
    private com.dataliquid.asciidoc.linter.validator.SourceLocation createSectionLocation(
            BlockValidationContext context) {
        
        // Get the container (section or document)
        org.asciidoctor.ast.StructuralNode container = context.getContainer();
        
        // Try to find the last line of the section/document content
        int insertLine = 1;
        
        if (container != null && container.getSourceLocation() != null) {
            // Start with section/document start line
            insertLine = container.getSourceLocation().getLineNumber();
        }
        
        // Get all blocks in the container
        java.util.List<org.asciidoctor.ast.StructuralNode> blocks = container.getBlocks();
        
        // Check if this is a document container with no content blocks
        if (container instanceof org.asciidoctor.ast.Document) {
            org.asciidoctor.ast.Document doc = (org.asciidoctor.ast.Document) container;
            // If document has a title but no content blocks, position after title
            if (doc.getTitle() != null && (blocks == null || blocks.isEmpty() || 
                blocks.stream().allMatch(b -> b instanceof org.asciidoctor.ast.Section))) {
                // Position after the document title (typically line 2)
                insertLine = 2;
                return com.dataliquid.asciidoc.linter.validator.SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(insertLine)
                    .endLine(insertLine)
                    .startColumn(0)
                    .endColumn(0)
                    .build();
            }
        }
        
        if (blocks != null && !blocks.isEmpty()) {
            // Find the last non-section block
            org.asciidoctor.ast.StructuralNode lastBlock = null;
            for (int i = blocks.size() - 1; i >= 0; i--) {
                org.asciidoctor.ast.StructuralNode block = blocks.get(i);
                if (!(block instanceof org.asciidoctor.ast.Section)) {
                    lastBlock = block;
                    break;
                }
            }
            
            if (lastBlock != null && lastBlock.getSourceLocation() != null) {
                // Position after the last block
                insertLine = lastBlock.getSourceLocation().getLineNumber();
                
                // Try to account for multi-line blocks
                if (lastBlock.getContext() != null) {
                    // For delimited blocks, we need to account for closing delimiter
                    switch (lastBlock.getContext()) {
                        case "listing":
                        case "literal":
                        case "example":
                        case "sidebar":
                        case "quote":
                        case "verse":
                        case "pass":
                            // These blocks have closing delimiters, add some lines
                            insertLine += 3; // Rough estimate
                            break;
                        case "table":
                            // Tables end with |===
                            insertLine += 2;
                            break;
                        default:
                            // For simple blocks like paragraphs, just add 1
                            insertLine += 1;
                            break;
                    }
                }
            }
        }
        
        return com.dataliquid.asciidoc.linter.validator.SourceLocation.builder()
            .filename(context.getFilename())
            .startLine(insertLine)
            .endLine(insertLine)
            .startColumn(0)  // No column for section errors
            .endColumn(0)
            .build();
    }
}