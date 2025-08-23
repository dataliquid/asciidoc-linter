package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Paragraph.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;
import com.dataliquid.asciidoc.linter.util.StringUtils;

/**
 * Validator for paragraph blocks in AsciiDoc documents.
 * <p>
 * This validator validates paragraph blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/paragraph-block.yaml}.
 * The YAML configuration is parsed into {@link ParagraphBlock} objects which
 * define the validation rules.
 * </p>
 * <p>
 * Supported validation rules from YAML schema:
 * </p>
 * <ul>
 * <li><b>lines</b>: Validates line count constraints (min/max number of
 * lines)</li>
 * <li><b>sentence</b>: Validates sentence-level constraints:
 * <ul>
 * <li><b>occurrence</b>: Min/max number of sentences per paragraph</li>
 * <li><b>words</b>: Min/max number of words per sentence</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * The lines and sentence configurations can optionally define their own
 * severity levels. If not specified, the block-level severity is used as
 * fallback.
 * </p>
 *
 * @see ParagraphBlock
 * @see BlockTypeValidator
 */
public final class ParagraphBlockValidator extends AbstractBlockValidator<ParagraphBlock> {
    private static final String AT_LEAST = "At least ";
    private static final String LINES_UNIT = " lines";
    private static final String SENTENCES_UNIT = " sentences";
    private static final String WORDS_UNIT = " words";

    @Override
    public BlockType getSupportedType() {
        return BlockType.PARAGRAPH;
    }

    @Override
    protected Class<ParagraphBlock> getBlockConfigClass() {
        return ParagraphBlock.class;
    }

    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, ParagraphBlock paragraphConfig,
            BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();

        // Get paragraph content
        String content = getBlockContent(block);

        // Validate line count if configured
        if (paragraphConfig.getLines() != null) {
            int lineCount = countLinesNonEmpty(content);
            validateLineCount(lineCount, paragraphConfig.getLines(), paragraphConfig, context, block, messages);
        }

        // Validate sentence count and structure if configured
        if (paragraphConfig.getSentence() != null) {
            validateSentences(content, paragraphConfig.getSentence(), paragraphConfig, context, block, messages);
        }

        return messages;
    }

    // getBlockContent is now inherited from AbstractBlockValidator

    private int countLinesNonEmpty(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        // Split by newlines and count non-empty lines
        String[] lines = content.split("\n");
        int count = 0;
        for (String line : lines) {
            if (!StringUtils.isBlank(line)) {
                count++;
            }
        }

        return count;
    }

    private void validateLineCount(int actualLines, com.dataliquid.asciidoc.linter.config.rule.LineConfig lineConfig,
            ParagraphBlock blockConfig, BlockValidationContext context, StructuralNode block,
            List<ValidationMessage> messages) {

        // Get severity with fallback to block severity
        Severity severity = resolveSeverity(lineConfig.severity(), blockConfig.getSeverity());

        if (lineConfig.min() != null && actualLines < lineConfig.min()) {
            SourcePosition pos = findSourcePosition(block, context, actualLines);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(LINES_MIN)
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .message("Paragraph has too few lines")
                            .actualValue(String.valueOf(actualLines))
                            .expectedValue(AT_LEAST + lineConfig.min() + LINES_UNIT)
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("Add more content here...")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add more lines to paragraph")
                                    .addExample("This is the first line of content.")
                                    .addExample("This is additional content on a new line.")
                                    .addExample("Add more descriptive text here.")
                                    .explanation("Paragraph needs at least " + lineConfig.min() + LINES_UNIT)
                                    .build())
                            .build());
        }

        if (lineConfig.max() != null && actualLines > lineConfig.max()) {
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(LINES_MAX)
                            .location(context.createLocation(block))
                            .message("Paragraph has too many lines")
                            .actualValue(String.valueOf(actualLines))
                            .expectedValue("At most " + lineConfig.max() + LINES_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Split paragraph into smaller parts")
                                    .addExample("Break into multiple paragraphs")
                                    .addExample("Use bullet points for lists")
                                    .addExample("Remove unnecessary details")
                                    .explanation(
                                            "Consider splitting content to stay under " + lineConfig.max() + LINES_UNIT)
                                    .build())
                            .build());
        }
    }

    private void validateSentences(String content, ParagraphBlock.SentenceConfig sentenceConfig,
            ParagraphBlock blockConfig, BlockValidationContext context, StructuralNode block,
            List<ValidationMessage> messages) {

        if (content == null || content.isEmpty()) {
            // If content is empty and sentences are required, check occurrence min
            if (sentenceConfig.getOccurrence() != null && sentenceConfig.getOccurrence().min() > 0) {
                Severity severity = resolveSeverity(sentenceConfig.getOccurrence().severity(),
                        blockConfig.getSeverity());

                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(SENTENCE_OCCURRENCE_MIN)
                                .location(context.createLocation(block))
                                .message("Paragraph has too few sentences")
                                .actualValue("0")
                                .expectedValue(AT_LEAST + sentenceConfig.getOccurrence().min() + SENTENCES_UNIT)
                                .errorType(ErrorType.MISSING_VALUE)
                                .missingValueHint("Add sentence content.")
                                .placeholderContext(PlaceholderContext
                                        .builder()
                                        .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                        .build())
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Add sentences to paragraph")
                                        .addExample("This is the first sentence.")
                                        .addExample("This is the second sentence with more detail.")
                                        .addExample("This concludes the paragraph.")
                                        .explanation("Paragraph needs at least " + sentenceConfig.getOccurrence().min()
                                                + SENTENCES_UNIT)
                                        .build())
                                .build());
            }
            return;
        }

        // Split content into sentences
        List<String> sentences = splitIntoSentences(content);

        // Validate sentence occurrence
        if (sentenceConfig.getOccurrence() != null) {
            validateSentenceOccurrence(sentences.size(), sentenceConfig.getOccurrence(), blockConfig, context, block,
                    messages);
        }

        // Validate words per sentence
        if (sentenceConfig.getWords() != null) {
            validateWordsPerSentence(sentences, sentenceConfig.getWords(), blockConfig, context, block, messages);
        }
    }

    private List<String> splitIntoSentences(String content) {
        List<String> sentences = new ArrayList<>();

        // First, replace newlines with spaces to handle multi-line sentences
        String normalizedContent = content.replaceAll("\\n+", " ").trim();

        // Simple approach: split by sentence-ending punctuation
        // This is a simplified version that works for most cases
        String[] parts = normalizedContent.split("(?<=[.!?])\\s+");

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }

        // If no sentences found but content exists, treat the whole content as one
        // sentence
        if (sentences.isEmpty() && !StringUtils.isBlank(normalizedContent)) {
            sentences.add(normalizedContent.trim());
        }

        return sentences;
    }

    private void validateSentenceOccurrence(int sentenceCount, OccurrenceConfig occurrenceConfig,
            ParagraphBlock blockConfig, BlockValidationContext context, StructuralNode block,
            List<ValidationMessage> messages) {

        Severity severity = resolveSeverity(occurrenceConfig.severity(), blockConfig.getSeverity());

        if (sentenceCount < occurrenceConfig.min()) {
            SourcePosition pos = findSourcePositionAtEndOfContent(block, context);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(SENTENCE_OCCURRENCE_MIN)
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .message("Paragraph has too few sentences")
                            .actualValue(String.valueOf(sentenceCount))
                            .expectedValue(AT_LEAST + occurrenceConfig.min() + SENTENCES_UNIT)
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("Add more sentences.")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Add more sentences")
                                    .addExample("Expand with additional details.")
                                    .addExample("Provide supporting information.")
                                    .addExample("Include relevant examples.")
                                    .explanation("Need " + (occurrenceConfig.min() - sentenceCount) + " more sentences")
                                    .build())
                            .build());
        }

        if (sentenceCount > occurrenceConfig.max()) {
            SourceLocation location = createParagraphLocation(block, context);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(SENTENCE_OCCURRENCE_MAX)
                            .location(location)
                            .message("Paragraph has too many sentences")
                            .actualValue(String.valueOf(sentenceCount))
                            .expectedValue("At most " + occurrenceConfig.max() + SENTENCES_UNIT)
                            .addSuggestion(Suggestion
                                    .builder()
                                    .description("Reduce sentence count")
                                    .addExample("Split into multiple paragraphs")
                                    .addExample("Combine related sentences")
                                    .addExample("Remove redundant information")
                                    .explanation("Remove " + (sentenceCount - occurrenceConfig.max()) + SENTENCES_UNIT)
                                    .build())
                            .build());
        }
    }

    private void validateWordsPerSentence(List<String> sentences, ParagraphBlock.WordsConfig wordsConfig,
            ParagraphBlock blockConfig, BlockValidationContext context, StructuralNode block,
            List<ValidationMessage> messages) {

        Severity severity = resolveSeverity(wordsConfig.getSeverity(), blockConfig.getSeverity());
        String fullContent = getBlockContent(block);

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            int wordCount = countWords(sentence);

            if (wordsConfig.getMin() != null && wordCount < wordsConfig.getMin()) {
                SourcePosition pos = findSentenceEndPosition(block, context, sentence, i);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(SENTENCE_WORDS_MIN)
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .message("Sentence " + (i + 1) + " has too few words")
                                .actualValue(wordCount + WORDS_UNIT)
                                .expectedValue(AT_LEAST + wordsConfig.getMin() + WORDS_UNIT)
                                .errorType(ErrorType.MISSING_VALUE)
                                .missingValueHint("add more words")
                                .placeholderContext(PlaceholderContext
                                        .builder()
                                        .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                        .build())
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Expand sentence with more detail")
                                        .addExample("Add descriptive adjectives")
                                        .addExample("Include supporting details")
                                        .addExample("Provide specific examples")
                                        .explanation(
                                                "Sentence needs " + (wordsConfig.getMin() - wordCount) + " more words")
                                        .build())
                                .build());
            }

            if (wordsConfig.getMax() != null && wordCount > wordsConfig.getMax()) {
                SourceLocation location = createSentenceLocation(block, context, fullContent, sentence, i);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(SENTENCE_WORDS_MAX)
                                .location(location)
                                .message("Sentence " + (i + 1) + " has too many words")
                                .actualValue(wordCount + WORDS_UNIT)
                                .expectedValue("At most " + wordsConfig.getMax() + WORDS_UNIT)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Simplify sentence")
                                        .addExample("Remove unnecessary words")
                                        .addExample("Split into two sentences")
                                        .addExample("Use more concise language")
                                        .explanation("Remove " + (wordCount - wordsConfig.getMax()) + WORDS_UNIT)
                                        .build())
                                .build());
            }
        }
    }

    private int countWords(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }

        // Split by whitespace and count non-empty parts
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Creates a source location for the entire paragraph block with proper column
     * positions.
     */
    private SourceLocation createParagraphLocation(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return context.createLocation(block);
        }

        int startLine = block.getSourceLocation().getLineNumber();
        if (startLine <= 0 || startLine > fileLines.size()) {
            return context.createLocation(block);
        }

        // Get the paragraph content
        String content = getBlockContent(block);
        if (content == null || content.isEmpty()) {
            return context.createLocation(block);
        }

        // For paragraphs, find the actual text position
        String firstLine = fileLines.get(startLine - 1);

        // Create location with full line span
        return SourceLocation
                .builder()
                .filename(context.getFilename())
                .startLine(startLine)
                .endLine(startLine)
                .startColumn(1)
                .endColumn(firstLine.length())
                .build();
    }

    /**
     * Creates a source location for a specific sentence within a paragraph.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private SourceLocation createSentenceLocation(StructuralNode block, BlockValidationContext context,
            String fullContent, String sentence, int sentenceIndex) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return context.createLocation(block);
        }

        int startLine = block.getSourceLocation().getLineNumber();
        if (startLine <= 0 || startLine > fileLines.size()) {
            return context.createLocation(block);
        }

        // For simplicity, if it's a single-line paragraph, highlight the whole line
        // In real scenarios, we could find the exact position of the sentence
        String paragraphLine = fileLines.get(startLine - 1);

        // Try to find the sentence in the paragraph
        int sentenceStart = paragraphLine.indexOf(sentence.trim());
        if (sentenceStart == -1) {
            // If not found, return the whole paragraph location
            return createParagraphLocation(block, context);
        }

        // Create location for the specific sentence
        return SourceLocation
                .builder()
                .filename(context.getFilename())
                .startLine(startLine)
                .endLine(startLine)
                .startColumn(sentenceStart + 1) // 1-based indexing
                .endColumn(sentenceStart + sentence.trim().length()) // Exclusive end position
                .build();
    }

    /**
     * Finds the position before the punctuation mark at the end of a sentence.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private SourcePosition findSentenceEndPosition(StructuralNode block, BlockValidationContext context,
            String sentence, int sentenceIndex) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int startLine = block.getSourceLocation().getLineNumber();
        if (startLine <= 0 || startLine > fileLines.size()) {
            return new SourcePosition(1, 1, startLine);
        }

        // Get the paragraph line
        String paragraphLine = fileLines.get(startLine - 1);

        // Find the sentence in the paragraph
        String trimmedSentence = sentence.trim();
        int sentenceStart = paragraphLine.indexOf(trimmedSentence);

        if (sentenceStart == -1) {
            // If not found, try to find without the punctuation
            String sentenceWithoutPunctuation = trimmedSentence.replaceAll("[.!?]+$", "");
            sentenceStart = paragraphLine.indexOf(sentenceWithoutPunctuation);
        }

        if (sentenceStart != -1) {
            // Find position before punctuation
            int endPos; // Will be set based on punctuation presence

            // Check if there's punctuation at the end
            if (trimmedSentence.matches(".*[.!?]+$")) {
                // Find where the punctuation starts
                int punctuationStart = trimmedSentence.length() - 1;
                while (punctuationStart > 0 && ".!?".indexOf(trimmedSentence.charAt(punctuationStart)) != -1) {
                    punctuationStart--;
                }
                punctuationStart++; // Move to first punctuation character

                // Position should be before the punctuation
                endPos = sentenceStart + punctuationStart + 1; // +1 for 1-based column
                return new SourcePosition(endPos, endPos, startLine);
            } else {
                // No punctuation, position at end of sentence
                endPos = sentenceStart + trimmedSentence.length() + 1; // +1 for 1-based column
                return new SourcePosition(endPos, endPos, startLine);
            }
        }

        // Fallback: position at end of line
        return new SourcePosition(paragraphLine.length() + 1, paragraphLine.length() + 1, startLine);
    }

    /**
     * Finds the position at the end of the paragraph content for appending.
     */
    private SourcePosition findSourcePositionAtEndOfContent(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int startLine = block.getSourceLocation().getLineNumber();
        if (startLine <= 0 || startLine > fileLines.size()) {
            return new SourcePosition(1, 1, startLine);
        }

        // For paragraphs, we want to position at the end of the content
        String content = getBlockContent(block);
        if (content != null && !content.isEmpty()) {
            // For single-line paragraphs, position at the end of the line
            String paragraphLine = fileLines.get(startLine - 1);
            int endColumn = paragraphLine.length() + 1;
            return new SourcePosition(endColumn, endColumn, startLine);
        }

        return new SourcePosition(1, 1, startLine);
    }

    /**
     * Finds the position where additional lines should be added.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context, int currentLines) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int startLine = block.getSourceLocation().getLineNumber();
        if (startLine <= 0 || startLine > fileLines.size()) {
            return new SourcePosition(1, 1, startLine);
        }

        // For paragraphs, we want to position at the end of the current content
        String content = getBlockContent(block);
        if (content != null && !content.isEmpty()) {
            String[] lines = content.split("\n");
            int lastNonEmptyLine = startLine;

            // Find the last line of the paragraph
            for (int i = 0; i < lines.length && startLine + i <= fileLines.size(); i++) {
                if (!lines[i].isBlank()) { // More efficient than trim().isEmpty()
                    lastNonEmptyLine = startLine + i;
                }
            }

            // Position at the end of the last line
            if (lastNonEmptyLine > 0 && lastNonEmptyLine <= fileLines.size()) {
                String lastLine = fileLines.get(lastNonEmptyLine - 1);
                return new SourcePosition(lastLine.length() + 1, lastLine.length() + 1, lastNonEmptyLine);
            }
        }

        return new SourcePosition(1, 1, startLine);
    }

}
