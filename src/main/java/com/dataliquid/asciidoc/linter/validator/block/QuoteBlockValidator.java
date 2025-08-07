package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for quote blocks.
 * Based on the YAML schema structure for validating AsciiDoc quote blocks.
 */
public final class QuoteBlockValidator extends AbstractBlockValidator<QuoteBlock> {
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.QUOTE;
    }
    
    @Override
    protected Class<QuoteBlock> getBlockConfigClass() {
        return QuoteBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode node, 
                                                               QuoteBlock quoteBlock,
                                                               BlockValidationContext context) {
        List<ValidationMessage> results = new ArrayList<>();
        
        // Validate attribution
        if (quoteBlock.getAttribution() != null) {
            validateAttribution(node, quoteBlock.getAttribution(), quoteBlock.getSeverity(), results, context);
        }
        
        // Validate citation
        if (quoteBlock.getCitation() != null) {
            validateCitation(node, quoteBlock.getCitation(), quoteBlock.getSeverity(), results, context);
        }
        
        // Validate content
        if (quoteBlock.getContent() != null) {
            validateContent(node, quoteBlock.getContent(), quoteBlock.getSeverity(), results, context);
        }
        
        return results;
    }
    
    private void validateAttribution(StructuralNode node, QuoteBlock.AttributionConfig config, 
                               Severity blockSeverity, List<ValidationMessage> results,
                               BlockValidationContext context) {
        String attribution = extractAttribution(node);
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.isRequired() && (attribution == null || attribution.trim().isEmpty())) {
            AttributionPosition pos = findAttributionPosition(node, context);
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.attribution.required")
                .message("Quote attribution is required but not provided")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("attribution")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.LIST_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (attribution != null && !attribution.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && attribution.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.attribution.minLength")
                    .message(String.format("Quote attribution is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), attribution.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && attribution.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.attribution.maxLength")
                    .message(String.format("Quote attribution is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), attribution.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(attribution).matches()) {
                AttributionPosition pos = findAttributionPosition(node, context);
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.attribution.pattern")
                    .message("Quote attribution does not match required pattern")
                    .actualValue(attribution)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .build());
            }
        }
    }
    
    private void validateCitation(StructuralNode node, QuoteBlock.CitationConfig config,
                               Severity blockSeverity, List<ValidationMessage> results,
                               BlockValidationContext context) {
        String citation = extractCitation(node);
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.isRequired() && (citation == null || citation.trim().isEmpty())) {
            CitationPosition pos = findCitationPosition(node, context);
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.citation.required")
                .message("Quote citation is required but not provided")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("citation")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.LIST_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (citation != null && !citation.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && citation.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.citation.minLength")
                    .message(String.format("Quote citation is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), citation.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && citation.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.citation.maxLength")
                    .message(String.format("Quote citation is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), citation.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(citation).matches()) {
                CitationPosition pos = findCitationPosition(node, context);
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.citation.pattern")
                    .message("Quote citation does not match required pattern")
                    .actualValue(citation)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .build());
            }
        }
    }
    
    private void validateContent(StructuralNode node, QuoteBlock.ContentConfig config,
                                Severity blockSeverity, List<ValidationMessage> results,
                                BlockValidationContext context) {
        String content = extractContent(node);
        
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            results.add(ValidationMessage.builder()
                .severity(blockSeverity)
                .ruleId("quote.content.required")
                .message("Quote block requires content")
                .location(context.createLocation(node))
                .build());
            return;
        }
        
        if (content != null && !content.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && content.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(blockSeverity)
                    .ruleId("quote.content.minLength")
                    .message(String.format("Quote content is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), content.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && content.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(blockSeverity)
                    .ruleId("quote.content.maxLength")
                    .message(String.format("Quote content is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), content.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate lines
            if (config.getLines() != null) {
                validateLines(node, content, config.getLines(), blockSeverity, results, context);
            }
        }
    }
    
    private void validateLines(StructuralNode node, String content, QuoteBlock.LinesConfig config,
                              Severity blockSeverity, List<ValidationMessage> results,
                              BlockValidationContext context) {
        String[] lines = content.split("\n");
        int lineCount = lines.length;
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.getMin() != null && lineCount < config.getMin()) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.content.lines.min")
                .message(String.format("Quote content has too few lines (minimum %d, found %d)",
                              config.getMin(), lineCount))
                .location(context.createLocation(node))
                .build());
        }
        
        if (config.getMax() != null && lineCount > config.getMax()) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.content.lines.max")
                .message(String.format("Quote content has too many lines (maximum %d, found %d)",
                              config.getMax(), lineCount))
                .location(context.createLocation(node))
                .build());
        }
    }
    
    private String extractAttribution(StructuralNode node) {
        // Check for attribution attribute (standard way)
        Object attribution = node.getAttribute(ATTRIBUTION);
        if (attribution != null) {
            return attribution.toString();
        }
        
        // Check for author attribute (alternative way)
        Object author = node.getAttribute(AUTHOR);
        if (author != null) {
            return author.toString();
        }
        
        // Check for positional attribute [quote, Author, Source]
        Object attr1 = node.getAttribute(ATTR_1);
        if (attr1 != null) {
            return attr1.toString();
        }
        
        return null;
    }
    
    private String extractCitation(StructuralNode node) {
        // Check for citetitle attribute (standard way for citation)
        Object citetitle = node.getAttribute(CITETITLE);
        if (citetitle != null) {
            return citetitle.toString();
        }
        
        // Check for source attribute (alternative way)
        Object source = node.getAttribute(SOURCE);
        if (source != null) {
            return source.toString();
        }
        
        // Check for positional attribute [quote, Author, Source]
        Object attr2 = node.getAttribute(ATTR_2);
        if (attr2 != null) {
            return attr2.toString();
        }
        
        return null;
    }
    
    private String extractContent(StructuralNode node) {
        // Use inherited getBlockContent method from AbstractBlockValidator
        return getBlockContent(node);
    }
    
    /**
     * Finds the position for quote attribution.
     */
    private AttributionPosition findAttributionPosition(StructuralNode node, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || node.getSourceLocation() == null) {
            return new AttributionPosition(7, 7, node.getSourceLocation() != null ? node.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = node.getSourceLocation().getLineNumber();
        
        // Quote blocks typically start one line before the reported line
        // Search backward for [quote]
        for (int i = lineNum - 1; i >= 0 && i >= lineNum - 5; i--) {
            if (i < fileLines.size()) {
                String line = fileLines.get(i);
                if (line.trim().startsWith("[quote")) {
                    // Find the first quoted parameter after [quote,
                    int firstQuoteStart = line.indexOf("\"");
                    if (firstQuoteStart >= 0) {
                        int firstQuoteEnd = line.indexOf("\"", firstQuoteStart + 1);
                        if (firstQuoteEnd > firstQuoteStart) {
                            // Found the attribution in quotes
                            return new AttributionPosition(firstQuoteStart + 2, firstQuoteEnd, i + 1);
                        }
                    }
                    // If no quotes found but has comma, position after comma
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new AttributionPosition(commaPos + 2, commaPos + 2, i + 1);
                    }
                    // Default position after [quote
                    return new AttributionPosition(7, 7, i + 1);
                }
            }
        }
        
        return new AttributionPosition(7, 7, lineNum);
    }
    
    /**
     * Finds the position for quote citation.
     */
    private CitationPosition findCitationPosition(StructuralNode node, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || node.getSourceLocation() == null) {
            return new CitationPosition(16, 16, node.getSourceLocation() != null ? node.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = node.getSourceLocation().getLineNumber();
        
        // Quote blocks typically start one line before the reported line
        // Search backward for [quote]
        for (int i = lineNum - 1; i >= 0 && i >= lineNum - 5; i--) {
            if (i < fileLines.size()) {
                String line = fileLines.get(i);
                if (line.trim().startsWith("[quote")) {
                    // Find the second quoted parameter (citation is after attribution)
                    int firstQuoteStart = line.indexOf("\"");
                    if (firstQuoteStart >= 0) {
                        int firstQuoteEnd = line.indexOf("\"", firstQuoteStart + 1);
                        if (firstQuoteEnd > firstQuoteStart) {
                            // Look for second quoted parameter
                            int secondQuoteStart = line.indexOf("\"", firstQuoteEnd + 1);
                            if (secondQuoteStart >= 0) {
                                int secondQuoteEnd = line.indexOf("\"", secondQuoteStart + 1);
                                if (secondQuoteEnd > secondQuoteStart) {
                                    // Found the citation in quotes
                                    return new CitationPosition(secondQuoteStart + 2, secondQuoteEnd, i + 1);
                                }
                            }
                        }
                    }
                    // Default position
                    return new CitationPosition(16, 16, i + 1);
                }
            }
        }
        
        return new CitationPosition(16, 16, lineNum);
    }
    
    private static class AttributionPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        AttributionPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    private static class CitationPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        CitationPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}