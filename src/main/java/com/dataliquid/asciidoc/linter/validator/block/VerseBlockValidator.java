package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
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
        // Author can be in author attribute
        Object attr = block.getAttribute("author");
        if (attr != null) {
            return attr.toString();
        }
        
        return null;
    }
    
    private String getAttribution(StructuralNode block) {
        // Attribution can be in attribution attribute
        Object attr = block.getAttribute("attribution");
        if (attr != null) {
            return attr.toString();
        }
        
        // Or in citetitle attribute
        attr = block.getAttribute("citetitle");
        if (attr != null) {
            return attr.toString();
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
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.author.minLength")
                    .location(context.createLocation(block, 1, 1))
                    .message("Verse author is too short")
                    .actualValue(author.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && author.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.author.maxLength")
                    .location(context.createLocation(block, 1, 1))
                    .message("Verse author is too long")
                    .actualValue(author.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(author).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.author.pattern")
                        .location(context.createLocation(block, 1, 1))
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
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.attribution.minLength")
                    .location(context.createLocation(block, 1, 1))
                    .message("Verse attribution is too short")
                    .actualValue(attribution.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && attribution.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.attribution.maxLength")
                    .location(context.createLocation(block, 1, 1))
                    .message("Verse attribution is too long")
                    .actualValue(attribution.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(attribution).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.attribution.pattern")
                        .location(context.createLocation(block, 1, 1))
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
            messages.add(ValidationMessage.builder()
                .severity(verseConfig.getSeverity())
                .ruleId("verse.content.minLength")
                .location(context.createLocation(block, 1, 1))
                .message("Verse content is too short")
                .actualValue(contentLength + " characters")
                .expectedValue("At least " + config.getMinLength() + " characters")
                .build());
        }
        
        if (content != null) {
            if (config.getMaxLength() != null && content.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(verseConfig.getSeverity())
                    .ruleId("verse.content.maxLength")
                    .location(context.createLocation(block, 1, 1))
                    .message("Verse content is too long")
                    .actualValue(content.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
            
            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(content).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(verseConfig.getSeverity())
                        .ruleId("verse.content.pattern")
                        .location(context.createLocation(block, 1, 1))
                        .message("Verse content does not match required pattern")
                        .actualValue(content.substring(0, Math.min(content.length(), 50)) + "...")
                        .expectedValue("Pattern: " + config.getPattern().pattern())
                        .build());
                }
            }
        }
    }
}