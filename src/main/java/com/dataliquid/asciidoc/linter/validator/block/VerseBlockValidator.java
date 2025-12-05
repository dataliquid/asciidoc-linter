package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Verse.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;
import com.dataliquid.asciidoc.linter.util.StringUtils;

/**
 * Validator for verse/quote blocks in AsciiDoc documents.
 * <p>
 * This validator validates verse blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/verse-block.yaml}. The
 * YAML configuration is parsed into {@link VerseBlock} objects which define the
 * validation rules.
 * </p>
 * <p>
 * Supported validation rules from YAML schema:
 * </p>
 * <ul>
 * <li><b>author</b>: Validates verse author (required, pattern, length
 * constraints)</li>
 * <li><b>attribution</b>: Validates source attribution (required, pattern,
 * length constraints)</li>
 * <li><b>content</b>: Validates verse content (required, length constraints,
 * pattern)</li>
 * </ul>
 * <p>
 * Note: Verse block nested configurations do not support individual severity
 * levels. All validations use the block-level severity.
 * </p>
 *
 * @see VerseBlock
 * @see BlockTypeValidator
 */
public final class VerseBlockValidator extends AbstractBlockValidator<VerseBlock> {
    // Constants for attribution parsing
    private static final int ATTRIBUTION_QUOTE_COUNT = 2;

    @Override
    public BlockType getSupportedType() {
        return BlockType.VERSE;
    }

    @Override
    protected Class<VerseBlock> getBlockConfigClass() {
        return VerseBlock.class;
    }

    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, VerseBlock verseConfig,
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
        Object attr = block.getAttribute(ATTRIBUTION);
        if (attr != null) {
            return attr.toString();
        }

        // Check for author attribute
        attr = block.getAttribute(AUTHOR);
        if (attr != null) {
            return attr.toString();
        }

        // Check for positional attribute [verse, Author, Source]
        // AsciidoctorJ puts "verse" in attribute "1", author in "2"
        Object attr2 = block.getAttribute(ATTR_2);
        if (attr2 != null) {
            return attr2.toString();
        }

        return null;
    }

    private String getAttribution(StructuralNode block) {
        // Or in citetitle attribute (this is where AsciidoctorJ puts the citation)
        Object attr = block.getAttribute(CITETITLE);
        if (attr != null) {
            return attr.toString();
        }

        // Attribution can be in attribution attribute
        attr = block.getAttribute(ATTRIBUTION);
        if (attr != null) {
            return attr.toString();
        }

        // Check for positional attribute [verse, Author, Source]
        // AsciidoctorJ puts "verse" in attribute "1", author in "2", source in "3"
        Object attr3 = block.getAttribute(ATTR_3);
        if (attr3 != null) {
            return attr3.toString();
        }

        return null;
    }

    // getBlockContent is now inherited from AbstractBlockValidator

    private void validateAuthor(String author, VerseBlock.AuthorConfig config, BlockValidationContext context,
            StructuralNode block, List<ValidationMessage> messages, VerseBlock verseConfig) {

        // Check if author is required
        if (config.isRequired() && StringUtils.isBlank(author)) {
            // Create location pointing to where author should be in [verse] line
            SourceLocation verseLocation = context.createLocation(block);
            // The [verse] line is typically one line before the block delimiter
            SourceLocation authorLocation = SourceLocation
                    .builder()
                    .filename(verseLocation.getFilename())
                    .startLine(verseLocation.getStartLine() - 1) // [verse] line is before ____
                    .endLine(verseLocation.getStartLine() - 1)
                    .startColumn(7) // After "[verse,"
                    .endColumn(7)
                    .build();

            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(verseConfig.getSeverity())
                            .ruleId(AUTHOR_REQUIRED)
                            .location(authorLocation)
                            .message("Verse author is required but not provided")
                            .actualValue("No author")
                            .expectedValue("Author required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("author")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add author to verse block")
                                    .addExample("[verse, \"William Shakespeare\", \"Hamlet\"]")
                                    .addExample("[verse, \"Maya Angelou\", \"I Know Why the Caged Bird Sings\"]")
                                    .addExample("[verse, \"Robert Frost\", \"The Road Not Taken\"]")
                                    .explanation("Verse blocks should include proper attribution with author name")
                                    .build())
                            .build());
            return;
        }

        if (author != null && !StringUtils.isBlank(author)) {
            // Validate author length
            if (config.getMinLength() != null && author.length() < config.getMinLength()) {
                SourcePosition pos = findSourcePosition(block, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(verseConfig.getSeverity())
                                .ruleId(AUTHOR_MIN_LENGTH)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Verse author is too short")
                                .actualValue(author.length() + CHARACTERS_UNIT)
                                .expectedValue("At least " + config.getMinLength() + CHARACTERS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Use longer author name")
                                        .addExample("William Shakespeare")
                                        .addExample("Edgar Allan Poe")
                                        .addExample("Emily Dickinson")
                                        .explanation("Author names should meet the minimum length requirement")
                                        .build())
                                .build());
            }

            if (config.getMaxLength() != null && author.length() > config.getMaxLength()) {
                SourcePosition pos = findSourcePosition(block, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(verseConfig.getSeverity())
                                .ruleId(AUTHOR_MAX_LENGTH)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Verse author is too long")
                                .actualValue(author.length() + CHARACTERS_UNIT)
                                .expectedValue("At most " + config.getMaxLength() + CHARACTERS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Shorten author name")
                                        .addExample("Use initials: E.A. Poe")
                                        .addExample("Use last name only: Shakespeare")
                                        .addExample("Use common name: Anonymous")
                                        .explanation("Author names should not exceed the maximum length limit")
                                        .build())
                                .build());
            }

            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(author).matches()) {
                    SourcePosition pos = findSourcePosition(block, context);
                    messages
                            .add(ValidationMessage
                                    .builder()
                                    .severity(verseConfig.getSeverity())
                                    .ruleId(AUTHOR_PATTERN)
                                    .location(SourceLocation
                                            .builder()
                                            .filename(context.getFilename())
                                            .fromPosition(pos)
                                            .build())
                                    .message("Verse author does not match required pattern")
                                    .actualValue(author)
                                    .expectedValue("Pattern: " + config.getPattern().pattern())
                                    .addSuggestion(Suggestion
                                            .builder()
                                            .description("Format author name to match pattern")
                                            .addExample("John Smith")
                                            .addExample("Mary Jane Watson")
                                            .addExample("Dr. John Doe")
                                            .explanation("Author names must follow the specified format pattern")
                                            .build())
                                    .build());
                }
            }
        }
    }

    private void validateAttribution(String attribution, VerseBlock.AttributionConfig config,
            BlockValidationContext context, StructuralNode block, List<ValidationMessage> messages,
            VerseBlock verseConfig) {

        // Check if attribution is required
        if (config.isRequired() && StringUtils.isBlank(attribution)) {
            // Create location pointing to where attribution should be in [verse] line
            SourceLocation verseLocation = context.createLocation(block);
            // The [verse] line is typically one line before the block delimiter
            SourceLocation attributionLocation = SourceLocation
                    .builder()
                    .filename(verseLocation.getFilename())
                    .startLine(verseLocation.getStartLine() - 1) // [verse] line is before ____
                    .endLine(verseLocation.getStartLine() - 1)
                    .startColumn(7) // After "[verse,"
                    .endColumn(7)
                    .build();

            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(verseConfig.getSeverity())
                            .ruleId(ATTRIBUTION_REQUIRED)
                            .location(attributionLocation)
                            .message("Verse attribution is required but not provided")
                            .actualValue("No attribution")
                            .expectedValue("Attribution required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("attribution")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add attribution source to verse block")
                                    .addExample("[verse, \"William Shakespeare\", \"Hamlet Act 3, Scene 1\"]")
                                    .addExample("[verse, \"Maya Angelou\", \"Still I Rise\"]")
                                    .addExample("[verse, \"Robert Frost\", \"The Road Not Taken\"]")
                                    .explanation("Verse blocks should include proper source attribution")
                                    .build())
                            .build());
            return;
        }

        if (attribution != null && !StringUtils.isBlank(attribution)) {
            // Validate attribution length
            if (config.getMinLength() != null && attribution.length() < config.getMinLength()) {
                SourcePosition pos = findAttributionPosition(block, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(verseConfig.getSeverity())
                                .ruleId(ATTRIBUTION_MIN_LENGTH)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Verse attribution is too short")
                                .actualValue(attribution.length() + CHARACTERS_UNIT)
                                .expectedValue("At least " + config.getMinLength() + CHARACTERS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Use longer attribution source")
                                        .addExample("Romeo and Juliet Act 2, Scene 2")
                                        .addExample("The Great Gatsby Chapter 9")
                                        .addExample("Paradise Lost Book 1")
                                        .explanation("Attribution sources should meet the minimum length requirement")
                                        .build())
                                .build());
            }

            if (config.getMaxLength() != null && attribution.length() > config.getMaxLength()) {
                SourcePosition pos = findAttributionPosition(block, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(verseConfig.getSeverity())
                                .ruleId(ATTRIBUTION_MAX_LENGTH)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Verse attribution is too long")
                                .actualValue(attribution.length() + CHARACTERS_UNIT)
                                .expectedValue("At most " + config.getMaxLength() + CHARACTERS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Shorten attribution source")
                                        .addExample("Use abbreviations: Hamlet Act 3")
                                        .addExample("Use short title: The Raven")
                                        .addExample("Use year only: Poetry 1920")
                                        .explanation("Attribution sources should not exceed the maximum length limit")
                                        .build())
                                .build());
            }

            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(attribution).matches()) {
                    SourcePosition pos = findAttributionPosition(block, context);
                    messages
                            .add(ValidationMessage
                                    .builder()
                                    .severity(verseConfig.getSeverity())
                                    .ruleId(ATTRIBUTION_PATTERN)
                                    .location(SourceLocation
                                            .builder()
                                            .filename(context.getFilename())
                                            .fromPosition(pos)
                                            .build())
                                    .message("Verse attribution does not match required pattern")
                                    .actualValue(attribution)
                                    .expectedValue("Pattern: " + config.getPattern().pattern())
                                    .addSuggestion(Suggestion
                                            .builder()
                                            .description("Format attribution source to match pattern")
                                            .addExample("Hamlet, Act III, Scene I")
                                            .addExample("Romeo and Juliet (1595)")
                                            .addExample("Sonnets, No. 18")
                                            .explanation("Attribution sources must follow the specified format pattern")
                                            .build())
                                    .build());
                }
            }
        }
    }

    private void validateContent(String content, VerseBlock.ContentConfig config, BlockValidationContext context,
            StructuralNode block, List<ValidationMessage> messages, VerseBlock verseConfig) {

        // Check if content is required
        if (config.isRequired() && StringUtils.isBlank(content)) {
            // Report error on the [verse] line, not the block delimiter
            SourceLocation verseLocation = context.createLocation(block, 1, 1);
            SourceLocation contentLocation = SourceLocation
                    .builder()
                    .filename(verseLocation.getFilename())
                    .startLine(verseLocation.getStartLine() - 1) // [verse] line is before ____
                    .endLine(verseLocation.getStartLine() - 1)
                    .startColumn(1)
                    .endColumn(1)
                    .build();

            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(verseConfig.getSeverity())
                            .ruleId(CONTENT_REQUIRED)
                            .location(contentLocation)
                            .message("Verse block requires content")
                            .actualValue("No content")
                            .expectedValue("Content required")
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("Content")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add content to verse block")
                                    .addExample("To be or not to be, that is the question")
                                    .addExample("Roses are red,\nViolets are blue")
                                    .addExample("Two roads diverged in a yellow wood")
                                    .explanation("Verse blocks must contain the actual verse or poem content")
                                    .build())
                            .build());
            return;
        }

        // Validate content length
        int contentLength = content != null ? content.length() : 0;
        if (config.getMinLength() != null && contentLength < config.getMinLength()) {
            SourcePosition pos = findContentPosition(block, context);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(verseConfig.getSeverity())
                            .ruleId(CONTENT_MIN_LENGTH)
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .message("Verse content is too short")
                            .actualValue(contentLength + CHARACTERS_UNIT)
                            .expectedValue("At least " + config.getMinLength() + CHARACTERS_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add more content to verse block")
                                    .addExample("Include complete verses or stanzas")
                                    .addExample("Add additional lines of poetry")
                                    .addExample("Include full quotations")
                                    .explanation("Verse content should meet the minimum length requirement")
                                    .build())
                            .build());
        }

        if (content != null) {
            if (config.getMaxLength() != null && content.length() > config.getMaxLength()) {
                SourcePosition pos = findContentPosition(block, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(verseConfig.getSeverity())
                                .ruleId(CONTENT_MAX_LENGTH)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Verse content is too long")
                                .actualValue(content.length() + CHARACTERS_UNIT)
                                .expectedValue("At most " + config.getMaxLength() + CHARACTERS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Shorten verse content")
                                        .addExample("Use selected stanzas only")
                                        .addExample("Quote key lines instead of full text")
                                        .addExample("Split into multiple verse blocks")
                                        .explanation("Verse content should not exceed the maximum length limit")
                                        .build())
                                .build());
            }

            // Validate pattern if specified
            if (config.getPattern() != null) {
                if (!config.getPattern().matcher(content).matches()) {
                    SourcePosition pos = findContentPosition(block, context);
                    messages
                            .add(ValidationMessage
                                    .builder()
                                    .severity(verseConfig.getSeverity())
                                    .ruleId(CONTENT_PATTERN)
                                    .location(SourceLocation
                                            .builder()
                                            .filename(context.getFilename())
                                            .fromPosition(pos)
                                            .build())
                                    .message("Verse content does not match required pattern")
                                    .actualValue(content.substring(0, Math.min(content.length(), 50)) + "...")
                                    .expectedValue("Pattern: " + config.getPattern().pattern())
                                    .addSuggestion(Suggestion
                                            .builder()
                                            .description("Format verse content to match pattern")
                                            .addExample("Ensure proper line breaks")
                                            .addExample("Use correct punctuation")
                                            .addExample("Follow required verse structure")
                                            .explanation("Verse content must follow the specified format pattern")
                                            .build())
                                    .build());
                }
            }
        }
    }

    /**
     * Finds the position of author in [verse] attribute line.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
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
                    int authorStart = line.indexOf('"');
                    if (authorStart >= 0) {
                        int authorEnd = line.indexOf('"', authorStart + 1);
                        if (authorEnd > authorStart) {
                            return new SourcePosition(authorStart + 2, authorEnd, checkLine);
                        }
                    }
                    // No quotes found, position after comma
                    int commaPos = line.indexOf(',');
                    if (commaPos >= 0) {
                        return new SourcePosition(commaPos + 2, commaPos + 2, checkLine);
                    }
                    return new SourcePosition(1, line.length(), checkLine);
                }
            }
        }

        return new SourcePosition(1, 1, blockLineNum);
    }

    /**
     * Finds the position of attribution in [verse] attribute line.
     */
    private SourcePosition findAttributionPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int blockLineNum = block.getSourceLocation().getLineNumber();

        // [verse] line is typically one or two lines before the block delimiter ____
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith("[verse")) {
                    // Found the [verse] line
                    // Attribution is the third parameter (after verse and author)
                    // Pattern: [verse, "author", "attribution"]
                    int quoteCount = 0;
                    int quoteStart = -1;
                    int quoteEnd = -1;
                    int searchPos = 0;

                    // Find the second quoted string (first is author, second is attribution)
                    while (searchPos < line.length() && quoteCount < 2) {
                        int nextQuoteStart = line.indexOf('"', searchPos);
                        if (nextQuoteStart >= 0) {
                            int nextQuoteEnd = line.indexOf('"', nextQuoteStart + 1);
                            if (nextQuoteEnd > nextQuoteStart) {
                                quoteCount++;
                                if (quoteCount == ATTRIBUTION_QUOTE_COUNT) {
                                    // This is the attribution (second quoted string)
                                    quoteStart = nextQuoteStart;
                                    quoteEnd = nextQuoteEnd;
                                    break;
                                }
                                searchPos = nextQuoteEnd + 1;
                            } else {
                                break; // No closing quote found
                            }
                        } else {
                            break; // No more quotes found
                        }
                    }

                    if (quoteStart >= 0 && quoteEnd > quoteStart) {
                        // Return position of the content between quotes, excluding the quotes
                        // Column positions are 1-based
                        return new SourcePosition(quoteStart + 2, quoteEnd, checkLine);
                    }

                    // If we can't find the third parameter, return a default position
                    return new SourcePosition(1, 1, checkLine);
                }
            }
        }

        return new SourcePosition(1, 1, blockLineNum);
    }

    /**
     * Finds the position of verse content.
     */
    private SourcePosition findContentPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int blockLineNum = block.getSourceLocation().getLineNumber();
        String content = getBlockContent(block);

        if (content != null && !content.isEmpty()) {
            // Content is between the ____ delimiters
            // Find the first line of actual content
            for (int i = blockLineNum; i < fileLines.size() && i < blockLineNum + 10; i++) {
                String line = fileLines.get(i - 1);
                if (!"____".equals(line.trim()) && !StringUtils.isBlank(line)) {
                    // Found content line
                    return new SourcePosition(1, line.length(), i);
                }
            }
        }

        // Default to block line
        return new SourcePosition(1, 1, blockLineNum);
    }

}
