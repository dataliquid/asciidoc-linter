package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for paragraph blocks in AsciiDoc documents.
 * 
 * <p>This validator validates paragraph blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/paragraph-block.yaml}.
 * The YAML configuration is parsed into {@link ParagraphBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>lines</b>: Validates line count constraints (min/max number of lines)</li>
 *   <li><b>sentence</b>: Validates sentence-level constraints:
 *     <ul>
 *       <li><b>occurrence</b>: Min/max number of sentences per paragraph</li>
 *       <li><b>words</b>: Min/max number of words per sentence</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <p>The lines and sentence configurations can optionally define their own severity levels.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see ParagraphBlock
 * @see BlockTypeValidator
 */
public final class ParagraphBlockValidator extends AbstractBlockValidator<ParagraphBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.PARAGRAPH;
    }
    
    @Override
    protected Class<ParagraphBlock> getBlockConfigClass() {
        return ParagraphBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               ParagraphBlock paragraphConfig,
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
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        
        return count;
    }
    
    private void validateLineCount(int actualLines, 
                                 com.dataliquid.asciidoc.linter.config.rule.LineConfig lineConfig,
                                 ParagraphBlock blockConfig,
                                 BlockValidationContext context,
                                 StructuralNode block,
                                 List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = lineConfig.severity() != null ? lineConfig.severity() : blockConfig.getSeverity();
        
        if (lineConfig.min() != null && actualLines < lineConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.lines.min")
                .location(context.createLocation(block))
                .message("Paragraph has too few lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At least " + lineConfig.min() + " lines")
                .build());
        }
        
        if (lineConfig.max() != null && actualLines > lineConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.lines.max")
                .location(context.createLocation(block))
                .message("Paragraph has too many lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At most " + lineConfig.max() + " lines")
                .build());
        }
    }
    
    private void validateSentences(String content,
                                  ParagraphBlock.SentenceConfig sentenceConfig,
                                  ParagraphBlock blockConfig,
                                  BlockValidationContext context,
                                  StructuralNode block,
                                  List<ValidationMessage> messages) {
        
        if (content == null || content.isEmpty()) {
            // If content is empty and sentences are required, check occurrence min
            if (sentenceConfig.getOccurrence() != null && 
                sentenceConfig.getOccurrence().min() > 0) {
                Severity severity = sentenceConfig.getOccurrence().severity() != null 
                    ? sentenceConfig.getOccurrence().severity() 
                    : blockConfig.getSeverity();
                    
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("paragraph.sentence.occurrence.min")
                    .location(context.createLocation(block))
                    .message("Paragraph has too few sentences")
                    .actualValue("0")
                    .expectedValue("At least " + sentenceConfig.getOccurrence().min() + " sentences")
                    .build());
            }
            return;
        }
        
        // Split content into sentences
        List<String> sentences = splitIntoSentences(content);
        
        // Validate sentence occurrence
        if (sentenceConfig.getOccurrence() != null) {
            validateSentenceOccurrence(sentences.size(), sentenceConfig.getOccurrence(), 
                                     blockConfig, context, block, messages);
        }
        
        // Validate words per sentence
        if (sentenceConfig.getWords() != null) {
            validateWordsPerSentence(sentences, sentenceConfig.getWords(), 
                                   blockConfig, context, block, messages);
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
        
        // If no sentences found but content exists, treat the whole content as one sentence
        if (sentences.isEmpty() && !normalizedContent.trim().isEmpty()) {
            sentences.add(normalizedContent.trim());
        }
        
        return sentences;
    }
    
    private void validateSentenceOccurrence(int sentenceCount,
                                          OccurrenceConfig occurrenceConfig,
                                          ParagraphBlock blockConfig,
                                          BlockValidationContext context,
                                          StructuralNode block,
                                          List<ValidationMessage> messages) {
        
        Severity severity = occurrenceConfig.severity() != null 
            ? occurrenceConfig.severity() 
            : blockConfig.getSeverity();
        
        if (sentenceCount < occurrenceConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.sentence.occurrence.min")
                .location(context.createLocation(block))
                .message("Paragraph has too few sentences")
                .actualValue(String.valueOf(sentenceCount))
                .expectedValue("At least " + occurrenceConfig.min() + " sentences")
                .build());
        }
        
        if (sentenceCount > occurrenceConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.sentence.occurrence.max")
                .location(context.createLocation(block))
                .message("Paragraph has too many sentences")
                .actualValue(String.valueOf(sentenceCount))
                .expectedValue("At most " + occurrenceConfig.max() + " sentences")
                .build());
        }
    }
    
    private void validateWordsPerSentence(List<String> sentences,
                                        ParagraphBlock.WordsConfig wordsConfig,
                                        ParagraphBlock blockConfig,
                                        BlockValidationContext context,
                                        StructuralNode block,
                                        List<ValidationMessage> messages) {
        
        Severity severity = wordsConfig.getSeverity() != null 
            ? wordsConfig.getSeverity() 
            : blockConfig.getSeverity();
        
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            int wordCount = countWords(sentence);
            
            if (wordsConfig.getMin() != null && wordCount < wordsConfig.getMin()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("paragraph.sentence.words.min")
                    .location(context.createLocation(block))
                    .message("Sentence " + (i + 1) + " has too few words")
                    .actualValue(wordCount + " words")
                    .expectedValue("At least " + wordsConfig.getMin() + " words")
                    .build());
            }
            
            if (wordsConfig.getMax() != null && wordCount > wordsConfig.getMax()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("paragraph.sentence.words.max")
                    .location(context.createLocation(block))
                    .message("Sentence " + (i + 1) + " has too many words")
                    .actualValue(wordCount + " words")
                    .expectedValue("At most " + wordsConfig.getMax() + " words")
                    .build());
            }
        }
    }
    
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        // Split by whitespace and count non-empty parts
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}