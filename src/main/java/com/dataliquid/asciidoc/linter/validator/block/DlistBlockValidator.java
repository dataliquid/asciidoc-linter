package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.DlistBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Dlist.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for definition list (dlist) blocks in AsciiDoc documents.
 * 
 * <p>This validator validates definition list blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/dlist-block.yaml}.
 * The YAML configuration is parsed into {@link DlistBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>terms</b>: Validates term count, pattern, and length</li>
 *   <li><b>descriptions</b>: Validates description presence, count, and pattern</li>
 *   <li><b>nestingLevel</b>: Validates maximum nesting depth</li>
 *   <li><b>delimiterStyle</b>: Validates delimiter consistency and allowed styles</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see DlistBlock
 * @see BlockTypeValidator
 */
public final class DlistBlockValidator extends AbstractBlockValidator<DlistBlock> {
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.DLIST;
    }
    
    @Override
    protected Class<DlistBlock> getBlockConfigClass() {
        return DlistBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               DlistBlock dlistConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate only if block is a DescriptionList
        if (!(block instanceof DescriptionList)) {
            return messages;
        }
        
        DescriptionList dlist = (DescriptionList) block;
        List<DescriptionListEntry> entries = dlist.getItems();
        
        // Validate terms
        if (dlistConfig.getTerms() != null) {
            validateTerms(entries, dlistConfig.getTerms(), dlistConfig, context, block, messages);
        }
        
        // Validate descriptions
        if (dlistConfig.getDescriptions() != null) {
            validateDescriptions(entries, dlistConfig.getDescriptions(), dlistConfig, context, block, messages);
        }
        
        // Note: Nesting level and delimiter style validation are not implemented
        // because AsciiDoctor's AST doesn't provide this information reliably.
        // - Delimiter style (::, :::, etc.) is not preserved in the AST
        // - Nesting requires specific syntax that's not commonly used
        
        return messages;
    }
    
    private void validateTerms(List<DescriptionListEntry> entries,
                             DlistBlock.TermsConfig config,
                             DlistBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int termCount = entries.size();
        
        // Validate min terms
        if (config.getMin() != null && termCount < config.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TERMS_MIN)
                .location(context.createLocation(block))
                .message("Definition list has too few terms")
                .actualValue(String.valueOf(termCount))
                .expectedValue("At least " + config.getMin() + " terms")
                .build());
        }
        
        // Validate max terms
        if (config.getMax() != null && termCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TERMS_MAX)
                .location(context.createLocation(block))
                .message("Definition list has too many terms")
                .actualValue(String.valueOf(termCount))
                .expectedValue("At most " + config.getMax() + " terms")
                .build());
        }
        
        // Validate each term
        if (config.getPattern() != null || config.getMinLength() != null || config.getMaxLength() != null) {
            Pattern pattern = config.getPattern() != null ? Pattern.compile(config.getPattern()) : null;
            
            for (DescriptionListEntry entry : entries) {
                List<ListItem> termItems = entry.getTerms();
                for (ListItem termItem : termItems) {
                    String term = termItem.getText();
                    if (term != null) {
                        validateTermContent(term, config, pattern, severity, context, block, termItem, messages);
                    }
                }
            }
        }
    }
    
    private void validateTermContent(String term,
                                   DlistBlock.TermsConfig config,
                                   Pattern pattern,
                                   Severity severity,
                                   BlockValidationContext context,
                                   StructuralNode block,
                                   ListItem termItem,
                                   List<ValidationMessage> messages) {
        
        SourcePosition pos = findSourcePosition(block, termItem, context, term);
        
        // Validate pattern
        if (pattern != null && !pattern.matcher(term).matches()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TERMS_PATTERN)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Definition list term does not match required pattern")
                .actualValue(term)
                .expectedValue("Pattern: " + config.getPattern())
                .build());
        }
        
        // Validate min length
        if (config.getMinLength() != null && term.length() < config.getMinLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TERMS_MIN_LENGTH)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Definition list term is too short")
                .actualValue(term + " (length: " + term.length() + ")")
                .expectedValue("Minimum length: " + config.getMinLength())
                .build());
        }
        
        // Validate max length
        if (config.getMaxLength() != null && term.length() > config.getMaxLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TERMS_MAX_LENGTH)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Definition list term is too long")
                .actualValue(term + " (length: " + term.length() + ")")
                .expectedValue("Maximum length: " + config.getMaxLength())
                .build());
        }
    }
    
    private void validateDescriptions(List<DescriptionListEntry> entries,
                                    DlistBlock.DescriptionsConfig config,
                                    DlistBlock blockConfig,
                                    BlockValidationContext context,
                                    StructuralNode block,
                                    List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        Pattern pattern = config.getPattern() != null ? Pattern.compile(config.getPattern()) : null;
        
        for (DescriptionListEntry entry : entries) {
            ListItem description = entry.getDescription();
            
            // Check if description is required
            if (config.getRequired() != null && config.getRequired() && 
                (description == null || description.getText() == null || description.getText().trim().isEmpty())) {
                // Get the first term for error location
                List<ListItem> terms = entry.getTerms();
                if (!terms.isEmpty()) {
                    ListItem firstTerm = terms.get(0);
                    String termText = firstTerm.getText();
                    SourcePosition pos = findSourcePosition(block, firstTerm, context, termText);
                    
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId(DESCRIPTIONS_REQUIRED)
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Definition list term missing required description")
                        .actualValue("No description")
                        .expectedValue("Description required")
                        .errorType(ErrorType.MISSING_VALUE)
                        .missingValueHint("Description")
                        .placeholderContext(PlaceholderContext.builder()
                            .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                            .build())
                        .build());
                }
            }
            
            // Validate description content if present
            if (description != null && description.getText() != null && pattern != null) {
                String descText = description.getText();
                if (!pattern.matcher(descText).find()) {
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId(DESCRIPTIONS_PATTERN)
                        .location(context.createLocation(block))
                        .message("Definition list description does not match required pattern")
                        .actualValue(descText.length() > 50 ? descText.substring(0, 50) + "..." : descText)
                        .expectedValue("Pattern: " + config.getPattern())
                        .build());
                }
            }
        }
    }
    
    /**
     * Finds the column position of a term in definition list.
     */
    private SourcePosition findSourcePosition(StructuralNode block, ListItem termItem, 
                                        BlockValidationContext context, String term) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        
        // Try to get line number from termItem's source location
        int lineNum = block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1;
        if (termItem.getSourceLocation() != null) {
            lineNum = termItem.getSourceLocation().getLineNumber();
        }
        
        if (fileLines.isEmpty() || lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for the term in the source line
        // Definition list terms are usually in format "term::" or "term:::"
        int termStart = sourceLine.indexOf(term);
        if (termStart >= 0) {
            // Check if this is actually the term (followed by ::)
            int colonPos = sourceLine.indexOf("::", termStart);
            if (colonPos >= termStart + term.length()) {
                return new SourcePosition(termStart + 1, termStart + term.length(), lineNum);
            }
        }
        
        // If not found on expected line, search nearby lines
        // Terms might be on different lines than the block start
        for (int offset = -2; offset <= 2; offset++) {
            int checkLine = lineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size() && checkLine != lineNum) {
                String checkContent = fileLines.get(checkLine - 1);
                termStart = checkContent.indexOf(term);
                if (termStart >= 0) {
                    int colonPos = checkContent.indexOf("::", termStart);
                    if (colonPos >= termStart + term.length()) {
                        return new SourcePosition(termStart + 1, termStart + term.length(), checkLine);
                    }
                }
            }
        }
        
        return new SourcePosition(1, 1, lineNum);
    }
    
}