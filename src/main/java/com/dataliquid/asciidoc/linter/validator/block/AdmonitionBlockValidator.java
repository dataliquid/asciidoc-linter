package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.AdmonitionBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Admonition.*;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

/**
 * Validator for admonition blocks in AsciiDoc documents.
 * 
 * <p>This validator validates admonition blocks (NOTE, TIP, IMPORTANT, WARNING, CAUTION)
 * based on the YAML schema structure defined in 
 * {@code src/main/resources/schemas/blocks/admonition-block.yaml}.
 * The YAML configuration is parsed into {@link AdmonitionBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>type</b>: Validates admonition type (required, allowed types)</li>
 *   <li><b>title</b>: Validates block title (required, pattern, min/max length)</li>
 *   <li><b>content</b>: Validates content (required, min/max length, lines)</li>
 *   <li><b>icon</b>: Validates icon settings (required, pattern)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see AdmonitionBlock
 * @see BlockTypeValidator
 */
public final class AdmonitionBlockValidator extends AbstractBlockValidator<AdmonitionBlock> {
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.ADMONITION;
    }
    
    @Override
    protected Class<AdmonitionBlock> getBlockConfigClass() {
        return AdmonitionBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               AdmonitionBlock admonitionConfig,
                                                               BlockValidationContext context) {
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get admonition attributes
        String admonitionType = getAdmonitionType(block);
        String title = block.getTitle();
        String content = getBlockContent(block);
        boolean hasIcon = hasIcon(block);
        
        // Validate type
        if (admonitionConfig.getTypeConfig() != null) {
            validateType(admonitionType, admonitionConfig.getTypeConfig(), admonitionConfig, context, block, messages);
        }
        
        // Validate title
        if (admonitionConfig.getTitle() != null) {
            validateTitle(title, admonitionConfig.getTitle(), admonitionConfig, context, block, messages);
        }
        
        // Validate content
        if (admonitionConfig.getContent() != null) {
            validateContent(content, admonitionConfig.getContent(), admonitionConfig, context, block, messages);
        }
        
        // Validate icon
        if (admonitionConfig.getIcon() != null) {
            validateIcon(hasIcon, admonitionConfig.getIcon(), admonitionConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getAdmonitionType(StructuralNode block) {
        // Admonition type is typically in the style attribute
        String style = block.getStyle();
        if (style != null) {
            // Convert to uppercase for consistency
            return style.toUpperCase();
        }
        
        // Try role attribute as fallback
        Object role = block.getAttribute(ROLE);
        if (role != null) {
            return role.toString().toUpperCase();
        }
        
        return null;
    }
    
    
    private boolean hasIcon(StructuralNode block) {
        // Check if icons are enabled at document level
        Object docIcons = block.getDocument().getAttribute(ICONS);
        if (docIcons != null && "font".equals(docIcons.toString())) {
            return true;
        }
        
        // Check block-level icon attribute
        Object blockIcon = block.getAttribute(ICON);
        return blockIcon != null && !"none".equals(blockIcon.toString());
    }
    
    private void validateType(String admonitionType, AdmonitionBlock.TypeConfig config,
                            AdmonitionBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if type is required
        if (config.isRequired() && (admonitionType == null || admonitionType.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TYPE_REQUIRED)
                .location(context.createLocation(block))
                .message("Admonition block must have a type")
                .actualValue("No type")
                .expectedValue("Type required")
                .build());
            return;
        }
        
        // Validate allowed types if specified
        if (admonitionType != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(admonitionType)) {
                TypePosition pos = findTypePosition(block, context, admonitionType);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TYPE_ALLOWED)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Admonition type '" + admonitionType + "' is not allowed")
                    .actualValue(admonitionType)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
                    .build());
            }
        }
    }
    
    private void validateTitle(String title, AdmonitionBlock.TitleConfig config,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TITLE_REQUIRED)
                .location(context.createLocation(block))
                .message("Admonition block requires a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(".Title")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (title != null) {
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(title).matches()) {
                TitlePosition pos = findTitlePosition(block, context, title);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_PATTERN)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Admonition title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
            
            // Validate min length
            if (config.getMinLength() != null && title.length() < config.getMinLength()) {
                TitlePosition pos = findTitlePosition(block, context, title);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_MIN_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Admonition title is too short")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && title.length() > config.getMaxLength()) {
                TitlePosition pos = findTitlePosition(block, context, title);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_MAX_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Admonition title is too long")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateContent(String content, AdmonitionBlock.ContentConfig config,
                               AdmonitionBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if content is required
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_REQUIRED)
                .location(context.createLocation(block))
                .message("Admonition block requires content")
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
        
        int contentLength = content.trim().length();
        
        // Validate min length
        if (config.getMinLength() != null && contentLength < config.getMinLength()) {
            ContentPosition pos = findContentPosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_MIN_LENGTH)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Admonition content is too short")
                .actualValue(contentLength + " characters")
                .expectedValue("At least " + config.getMinLength() + " characters")
                .build());
        }
        
        // Validate max length
        if (config.getMaxLength() != null && contentLength > config.getMaxLength()) {
            ContentPosition pos = findContentPosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_MAX_LENGTH)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Admonition content is too long")
                .actualValue(contentLength + " characters")
                .expectedValue("At most " + config.getMaxLength() + " characters")
                .build());
        }
        
        // Validate lines if configured
        if (config.getLines() != null) {
            validateLines(content, config.getLines(), config, blockConfig, context, block, messages);
        }
    }
    
    private void validateLines(String content, com.dataliquid.asciidoc.linter.config.rule.LineConfig config,
                             AdmonitionBlock.ContentConfig contentConfig,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to content config severity, then block severity
        Severity severity = config.severity() != null ? config.severity() : 
                           (contentConfig.getSeverity() != null ? contentConfig.getSeverity() : 
                            blockConfig.getSeverity());
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (config.min() != null && lineCount < config.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_LINES_MIN)
                .location(context.createLocation(block))
                .message("Admonition block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + config.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.max() != null && lineCount > config.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CONTENT_LINES_MAX)
                .location(context.createLocation(block))
                .message("Admonition block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + config.max() + " lines")
                .build());
        }
    }
    
    private void validateIcon(boolean hasIcon, AdmonitionBlock.IconConfig config,
                            AdmonitionBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && !hasIcon) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(ICON_REQUIRED)
                .location(context.createLocation(block))
                .message("Admonition block requires an icon")
                .actualValue("No icon")
                .expectedValue("Icon required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(":icons: font")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
        }
        
        // Validate icon pattern if present and pattern is configured
        if (hasIcon && config.getPattern() != null) {
            String iconValue = getIconValue(block);
            if (iconValue != null && !config.getPattern().matcher(iconValue).matches()) {
                IconPosition pos = findIconPosition(block, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(ICON_PATTERN)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Admonition icon does not match required pattern")
                    .actualValue(iconValue)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
        }
    }
    
    private String getIconValue(StructuralNode block) {
        Object icon = block.getAttribute(ICON);
        return icon != null ? icon.toString() : null;
    }
    
    /**
     * Finds the position of admonition title.
     */
    private TitlePosition findTitlePosition(StructuralNode block, BlockValidationContext context, String title) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new TitlePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // Admonition titles are typically on the line before the admonition keyword (NOTE:, TIP:, etc.)
        // Example:
        // .My Title
        // NOTE: Content here
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                if (line.trim().startsWith(".") && line.trim().substring(1).equals(title)) {
                    // Found the title line
                    int titleStart = line.indexOf(".");
                    int titleEnd = titleStart + 1 + title.length();
                    return new TitlePosition(titleStart + 1, titleEnd, checkLine);
                }
            }
        }
        
        return new TitlePosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of admonition content.
     */
    private ContentPosition findContentPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new ContentPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // Content is on the admonition line itself after the admonition keyword
        if (blockLineNum > 0 && blockLineNum <= fileLines.size()) {
            String line = fileLines.get(blockLineNum - 1);
            // Look for admonition keywords: NOTE:, TIP:, IMPORTANT:, WARNING:, CAUTION:
            for (String keyword : new String[]{"NOTE:", "TIP:", "IMPORTANT:", "WARNING:", "CAUTION:"}) {
                int keywordPos = line.indexOf(keyword);
                if (keywordPos >= 0) {
                    // Content starts after the keyword and space
                    int contentStart = keywordPos + keyword.length();
                    while (contentStart < line.length() && line.charAt(contentStart) == ' ') {
                        contentStart++;
                    }
                    // For content validation, highlight only the content part (after "NOTE: ")
                    if (contentStart < line.length()) {
                        return new ContentPosition(contentStart + 1, line.length(), blockLineNum);
                    } else {
                        // No content after the keyword
                        return new ContentPosition(contentStart + 1, contentStart + 1, blockLineNum);
                    }
                }
            }
        }
        
        return new ContentPosition(1, 1, blockLineNum);
    }
    
    /**
     * Finds the position of the admonition type line.
     */
    private TypePosition findTypePosition(StructuralNode block, BlockValidationContext context, String admonitionType) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new TypePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        
        // The admonition type line is the line with the keyword (NOTE:, TIP:, etc.)
        if (blockLineNum > 0 && blockLineNum <= fileLines.size()) {
            String line = fileLines.get(blockLineNum - 1);
            // Look for the admonition keyword at the start of the line
            int typeStart = line.indexOf(admonitionType);
            if (typeStart >= 0) {
                // Check if it's followed by a colon
                int colonPos = typeStart + admonitionType.length();
                if (colonPos < line.length() && line.charAt(colonPos) == ':') {
                    // Highlight only the admonition type keyword (not the colon or content)
                    return new TypePosition(typeStart + 1, typeStart + admonitionType.length(), blockLineNum);
                }
            }
        }
        
        return new TypePosition(1, 1, blockLineNum);
    }
    
    private static class TitlePosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        TitlePosition(int startColumn, int endColumn, int lineNumber) {
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
    
    private static class TypePosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        TypePosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    /**
     * Finds the position of icon attribute in admonition block.
     */
    private IconPosition findIconPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new IconPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int blockLineNum = block.getSourceLocation().getLineNumber();
        String iconValue = getIconValue(block);
        
        // Look for the [NOTE,icon=...] or [NOTE,icon:...] line before the block delimiter
        for (int offset = -2; offset <= 0; offset++) {
            int checkLine = blockLineNum + offset;
            if (checkLine > 0 && checkLine <= fileLines.size()) {
                String line = fileLines.get(checkLine - 1);
                // Check if this line contains the admonition declaration with icon
                if (line.matches("^\\s*\\[(NOTE|TIP|WARNING|IMPORTANT|CAUTION).*icon.*\\]\\s*$")) {
                    // Found the line, now find the icon value position
                    int iconPos = line.indexOf("icon=");
                    if (iconPos < 0) {
                        iconPos = line.indexOf("icon:");
                    }
                    
                    if (iconPos >= 0 && iconValue != null) {
                        // Find where the icon value starts (after "icon=" or "icon:")
                        int valueStart = iconPos + (line.charAt(iconPos + 4) == '=' ? 5 : 5); // "icon=" or "icon:"
                        // Find the end of the value (comma or closing bracket)
                        int valueEnd = valueStart;
                        for (int i = valueStart; i < line.length(); i++) {
                            char ch = line.charAt(i);
                            if (ch == ',' || ch == ']') {
                                break;
                            }
                            valueEnd = i;
                        }
                        return new IconPosition(valueStart + 1, valueEnd + 1, checkLine);
                    }
                    
                    // If no icon= found, default to entire bracket content
                    int startBracket = line.indexOf('[');
                    int endBracket = line.indexOf(']');
                    if (startBracket >= 0 && endBracket > startBracket) {
                        return new IconPosition(startBracket + 1, endBracket + 1, checkLine);
                    }
                }
            }
        }
        
        return new IconPosition(1, 1, blockLineNum);
    }
    
    private static class IconPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        IconPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}