package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for verse/quote blocks in AsciiDoc documents.
 * 
 * <p>This validator validates verse blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/verse-block.yaml}.
 * The YAML configuration is parsed into {@link VerseBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>author</b>: Validates verse author (required, pattern, length constraints)</li>
 *   <li><b>attribution</b>: Validates source attribution (required, pattern, length constraints)</li>
 *   <li><b>content</b>: Validates verse content (required, length constraints, pattern)</li>
 * </ul>
 * 
 * <p>Note: Verse block nested configurations do not support individual severity levels.
 * All validations use the block-level severity.</p>
 * 
 * @see VerseBlock
 * @see BlockTypeValidator
 */
public final class VerseBlockValidator extends AbstractBlockValidator<VerseBlock> {
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.VERSE;
    }
    
    @Override
    protected Class<VerseBlock> getBlockConfigClass() {
        return VerseBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               VerseBlock verseConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get verse attributes
        String author = getAuthor(block);
        String attribution = getAttribution(block);
        String content = getBlockContent(block);
        
        // Validate author
        if (verseConfig.getAuthor() != null) {
            validateAuthor(author, verseConfig.getAuthor(), context, block, messages, verseConfig);
        }
        
        // Validate attribution
        if (verseConfig.getAttribution() != null) {
            validateAttribution(attribution, verseConfig.getAttribution(), context, block, messages, verseConfig);
        }
        
        // Validate content
        if (verseConfig.getContent() != null) {
            validateContent(content, verseConfig.getContent(), context, block, messages, verseConfig);
        }
        
        return messages;
    }
    
    private String getAuthor(StructuralNode block) {
        // Author can be in attribution attribute (standard way)
        Object attr = block.getAttribute("attribution");
        if (attr != null) {
            return attr.toString();
        }
        
        // Check for author attribute
        attr = block.getAttribute("author");
        if (attr != null) {
            return attr.toString();
        }
        
        // Check for positional attribute [verse, Author, Source]
        // AsciidoctorJ puts "verse" in attribute "1", author in "2"
        Object attr2 = block.getAttribute("2");
        if (attr2 != null) {
            return attr2.toString();
        }
        
        return null;
    }
    
    private String getAttribution(StructuralNode block) {
        // Or in citetitle attribute (this is where AsciidoctorJ puts the citation)
        Object attr = block.getAttribute("citetitle");
        if (attr != null) {
            return attr.toString();
        }
        
        // Attribution can be in attribution attribute
        attr = block.getAttribute("attribution");
        if (attr != null) {
            return attr.toString();
        }
        
        // Check for positional attribute [verse, Author, Source]
        // AsciidoctorJ puts "verse" in attribute "1", author in "2", source in "3"
        Object attr3 = block.getAttribute("3");
        if (attr3 != null) {
            return attr3.toString();
        }
        
        return null;
    }
    
    // getBlockContent is now inherited from AbstractBlockValidator
    
    private void validateAuthor(String author, VerseBlock.AuthorConfig config,
                              BlockValidationContext context,
                              StructuralNode block,
                              List<ValidationMessage> messages,
                              VerseBlock verseConfig) {
        
        // Check if author is required
        if (config.isRequired() && (author == null || author.trim().isEmpty())) {
            // Create location pointing to where author should be in [verse] line
            SourceLocation verseLocation = context.createLocation(block);
            // The [verse] line is typically one line before the block delimiter
            SourceLocation authorLocation = SourceLocation.builder()
                .filename(verseLocation.getFilename())
                .startLine(verseLocation.getStartLine() - 1)  // [verse] line is before ____
                .endLine(verseLocation.getStartLine() - 1)
                .startColumn(7)  // After "[verse,"
                .endColumn(7)
                .build();
                
            messages.add(ValidationMessage.builder()
                .severity(verseConfig.getSeverity())
                .ruleId("verse.author.required")
                .location(authorLocation)
                .message("Verse author is required but not provided")
                .actualValue("No author")
                .expectedValue("Author required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("author")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (author != null && !author.trim().isEmpty()) {
            // Validate author length
            if (config.getMinLength() != null && author.length() < config.getMinLength()) {
                AuthorPosition pos = findAuthorPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.author.minLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Verse author is too short")
                    .actualValue(author.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && author.length() > config.getMaxLength()) {
                AuthorPosition pos = findAuthorPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.author.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Verse author is too long")
                    .actualValue(author.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(author).matches()) {
                    AuthorPosition pos = findAuthorPositionWithValue(block, context, author);
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.author.pattern")
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Verse author does not match required pattern")
                        .actualValue(author)
                        .expectedValue("Pattern: " + config.getPattern().pattern())
                        .build());
                }
            }
        }
    }
    
    private void validateAttribution(String attribution, VerseBlock.AttributionConfig config,
                                   BlockValidationContext context,
                                   StructuralNode block,
                                   List<ValidationMessage> messages,
                                   VerseBlock verseConfig) {
        
        // Check if attribution is required
        if (config.isRequired() && (attribution == null || attribution.trim().isEmpty())) {
            // Create location pointing to where attribution should be in [verse] line
            SourceLocation verseLocation = context.createLocation(block);
            // The [verse] line is typically one line before the block delimiter
            SourceLocation attributionLocation = SourceLocation.builder()
                .filename(verseLocation.getFilename())
                .startLine(verseLocation.getStartLine() - 1)  // [verse] line is before ____
                .endLine(verseLocation.getStartLine() - 1)
                .startColumn(7)  // After "[verse,"
                .endColumn(7)
                .build();
                
            messages.add(ValidationMessage.builder()
                .severity(verseConfig.getSeverity())
                .ruleId("verse.attribution.required")
                .location(attributionLocation)
                .message("Verse attribution is required but not provided")
                .actualValue("No attribution")
                .expectedValue("Attribution required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("attribution")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (attribution != null && !attribution.trim().isEmpty()) {
            // Validate attribution length
            if (config.getMinLength() != null && attribution.length() < config.getMinLength()) {
                AttributionPosition pos = findAttributionPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.attribution.minLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Verse attribution is too short")
                    .actualValue(attribution.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && attribution.length() > config.getMaxLength()) {
                AttributionPosition pos = findAttributionPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.attribution.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Verse attribution is too long")
                    .actualValue(attribution.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(attribution).matches()) {
                    AttributionPosition pos = findAttributionPositionWithValue(block, context, attribution);
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.attribution.pattern")
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Verse attribution does not match required pattern")
                        .actualValue(attribution)
                        .expectedValue("Pattern: " + config.getPattern().pattern())
                        .build());
                }
            }
        }
    }
    
    private void validateContent(String content, VerseBlock.ContentConfig config,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages,
                               VerseBlock verseConfig) {
        
        // Check if content is required
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            // Report error on the [verse] line, not the block delimiter
            SourceLocation verseLocation = context.createLocation(block, 1, 1);
            SourceLocation contentLocation = SourceLocation.builder()
                .filename(verseLocation.getFilename())
                .startLine(verseLocation.getStartLine() - 1)  // [verse] line is before ____
                .endLine(verseLocation.getStartLine() - 1)
                .startColumn(1)
                .endColumn(1)
                .build();
                
            messages.add(ValidationMessage.builder()
                .severity(verseConfig.getSeverity())
                .ruleId("verse.content.required")
                .location(contentLocation)
                .message("Verse block requires content")
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
        
        // Validate content length
        int contentLength = content != null ? content.length() : 0;
        if (config.getMinLength() != null && contentLength < config.getMinLength()) {
            ContentPosition pos = findContentPosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(verseConfig.getSeverity())
                .ruleId("verse.content.minLength")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Verse content is too short")
                .actualValue(contentLength + " characters")
                .expectedValue("At least " + config.getMinLength() + " characters")
                .build());
        }
        
        if (content != null) {
            if (config.getMaxLength() != null && content.length() > config.getMaxLength()) {
                ContentPosition pos = findContentPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.content.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Verse content is too long")
                    .actualValue(content.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(content).matches()) {
                    ContentPosition pos = findContentPosition(block, context);
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.content.pattern")
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Verse content does not match required pattern")
                        .actualValue(content.substring(0, Math.min(content.length(), 50)) + "...")
                        .expectedValue("Pattern: " + config.getPattern().pattern())
                        .build());
                }
            }
        }
    }
    
    /**
     * Finds the position of author in [verse] attribute line.
     */
    /**
     * Finds the position of the entire [verse] line when author has a value.
     */
    private AuthorPosition findAuthorPositionWithValue(StructuralNode block, BlockValidationContext context, String author) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new AuthorPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [verse] line is typically one or two lines before the block delimiter ____
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[verse")) {
                    // Found the [verse] line - highlight the entire line
                    int startCol = line.indexOf("[");
                    int endCol = line.indexOf("]");
                    if (endCol > startCol) {
                        return new AuthorPosition(startCol + 1, endCol + 1, checkLine);
                    }
                }
            }
        }
        
        return new AuthorPosition(1, 1, blockLineNum);
    }

    private AuthorPosition findAuthorPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new AuthorPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [verse] line is typically one or two lines before the block delimiter ____
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[verse")) {
                    // Found the [verse] line
                    // Author is the first quoted parameter after [verse,
                    int authorStart = line.indexOf("\"");
                    if (authorStart >= 0) {
                        int authorEnd = line.indexOf("\"", authorStart + 1);
                        if (authorEnd > authorStart) {
                            String author = line.substring(authorStart + 1, authorEnd);
                            return new AuthorPosition(authorStart + 2, authorEnd + 1, checkLine);
                        }
                    }
                    // No quotes found, position after comma
                    int commaPos = line.indexOf(",");
                    if (commaPos >= 0) {
                        return new AuthorPosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    return new AuthorPosition(1, line.length(), checkLine);
                }
            }
        }
        
        return new AuthorPosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of the entire [verse] line when attribution has a value.
     */
    private AttributionPosition findAttributionPositionWithValue(StructuralNode block, BlockValidationContext context, String attribution) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new AttributionPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [verse] line is typically one or two lines before the block delimiter ____
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[verse")) {
                    // Found the [verse] line - highlight the entire line
                    int startCol = line.indexOf("[");
                    int endCol = line.indexOf("]");
                    if (endCol > startCol) {
                        return new AttributionPosition(startCol + 1, endCol + 1, checkLine);
                    }
                }
            }
        }
        
        return new AttributionPosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of attribution in [verse] attribute line.
     */
    private AttributionPosition findAttributionPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new AttributionPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // [verse] line is typically one or two lines before the block delimiter ____
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[verse")) {
                    // Found the [verse] line
                    // Attribution is the second quoted parameter
                    int firstQuoteStart = line.indexOf("\"");
                    if (firstQuoteStart >= 0) {
                        int firstQuoteEnd = line.indexOf("\"", firstQuoteStart + 1);
                        if (firstQuoteEnd > firstQuoteStart) {
                            // Look for second quoted parameter
                            int secondQuoteStart = line.indexOf("\"", firstQuoteEnd + 1);
                            if (secondQuoteStart >= 0) {
                                int secondQuoteEnd = line.indexOf("\"", secondQuoteStart + 1);
                                if (secondQuoteEnd > secondQuoteStart) {
                                    return new AttributionPosition(secondQuoteStart + 2, secondQuoteEnd + 1, checkLine);
                                }
                            }
                        }
                    }
                    return new AttributionPosition(1, line.length(), checkLine);
                }
            }
        }
        
        return new AttributionPosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of verse content.
     */
    private ContentPosition findContentPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new ContentPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        String content = getBlockContent(block);
        
        if (content != null && !content.isEmpty()) {
            // Content is between the ____ delimiters
            // Find the first line of actual content
            for (int i = blockLineNum; i < fileLines.size() && i < blockLineNum + 10; i++) {
                String line = fileLines.get(i - 1);
                if (!line.trim().equals("____") && !line.trim().isEmpty()) {
                    // Found content line
                    return new ContentPosition(1, line.length(), i);
                }
            }
        }
        
        // Default to block line
        return new ContentPosition(1, 1, blockLineNum);
    }
    
    private static class AuthorPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        AuthorPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
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
}