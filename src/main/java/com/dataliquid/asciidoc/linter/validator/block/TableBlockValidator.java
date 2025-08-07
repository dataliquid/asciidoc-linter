package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Table.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for table blocks in AsciiDoc documents.
 * 
 * <p>This validator validates table blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/table-block.yaml}.
 * The YAML configuration is parsed into {@link TableBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>columns</b>: Validates column count (min/max)</li>
 *   <li><b>rows</b>: Validates row count (min/max)</li>
 *   <li><b>header</b>: Validates header row (required, pattern matching)</li>
 *   <li><b>caption</b>: Validates table caption (required, pattern, length constraints)</li>
 *   <li><b>format</b>: Validates table formatting (style, borders)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see TableBlock
 * @see BlockTypeValidator
 */
public final class TableBlockValidator extends AbstractBlockValidator<TableBlock> {
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.TABLE;
    }
    
    @Override
    protected Class<TableBlock> getBlockConfigClass() {
        return TableBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               TableBlock tableConfig,
                                                               BlockValidationContext context) {
        
        if (!(block instanceof Table)) {
            // Should not happen if BlockTypeDetector works correctly
            return List.of();
        }
        
        Table table = (Table) block;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate columns
        if (tableConfig.getColumns() != null) {
            validateColumns(table, tableConfig.getColumns(), tableConfig, context, messages);
        }
        
        // Validate rows
        if (tableConfig.getRows() != null) {
            validateRows(table, tableConfig.getRows(), tableConfig, context, messages);
        }
        
        // Validate header
        if (tableConfig.getHeader() != null) {
            validateHeader(table, tableConfig.getHeader(), tableConfig, context, messages);
        }
        
        // Validate caption
        if (tableConfig.getCaption() != null) {
            validateCaption(table, tableConfig.getCaption(), tableConfig, context, messages);
        }
        
        // Validate format
        if (tableConfig.getFormat() != null) {
            validateFormat(table, tableConfig.getFormat(), tableConfig, context, messages);
        }
        
        return messages;
    }
    
    private void validateColumns(Table table, TableBlock.DimensionConfig config,
                               TableBlock blockConfig,
                               BlockValidationContext context,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int columnCount = table.getColumns().size();
        
        if (config.getMin() != null && columnCount < config.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(COLUMNS_MIN)
                .location(context.createLocation(table))
                .message("Table has too few columns")
                .actualValue(String.valueOf(columnCount))
                .expectedValue("At least " + config.getMin() + " columns")
                .build());
        }
        
        if (config.getMax() != null && columnCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(COLUMNS_MAX)
                .location(context.createLocation(table))
                .message("Table has too many columns")
                .actualValue(String.valueOf(columnCount))
                .expectedValue("At most " + config.getMax() + " columns")
                .build());
        }
    }
    
    private void validateRows(Table table, TableBlock.DimensionConfig config,
                            TableBlock blockConfig,
                            BlockValidationContext context,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int rowCount = table.getBody().size();
        
        if (config.getMin() != null && rowCount < config.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(ROWS_MIN)
                .location(context.createLocation(table))
                .message("Table has too few rows")
                .actualValue(String.valueOf(rowCount))
                .expectedValue("At least " + config.getMin() + " rows")
                .build());
        }
        
        if (config.getMax() != null && rowCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(ROWS_MAX)
                .location(context.createLocation(table))
                .message("Table has too many rows")
                .actualValue(String.valueOf(rowCount))
                .expectedValue("At most " + config.getMax() + " rows")
                .build());
        }
    }
    
    private void validateHeader(Table table, TableBlock.HeaderConfig config,
                              TableBlock blockConfig,
                              BlockValidationContext context,
                              List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        boolean hasHeader = !table.getHeader().isEmpty();
        
        if (config.isRequired() && !hasHeader) {
            HeaderPosition pos = findHeaderPosition(table, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(HEADER_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Table header is required but not provided")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("| Header 1 | Header 2")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
        }
        
        // Validate header pattern if header exists
        if (hasHeader && config.getPattern() != null) {
            Pattern pattern = config.getPattern();
            
            for (Row headerRow : table.getHeader()) {
                for (Cell cell : headerRow.getCells()) {
                    String content = cell.getText();
                    if (!pattern.matcher(content).matches()) {
                        HeaderPosition pos = findHeaderCellPosition(table, context, content);
                        messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId(HEADER_PATTERN)
                            .location(SourceLocation.builder()
                                .filename(context.getFilename())
                                .startLine(pos.lineNumber)
                                .endLine(pos.lineNumber)
                                .startColumn(pos.startColumn)
                                .endColumn(pos.endColumn)
                                .build())
                            .message("Table header does not match required pattern")
                            .actualValue(content)
                            .expectedValue("Pattern: " + config.getPattern())
                            .build());
                    }
                }
            }
        }
    }
    
    private void validateCaption(Table table, TableBlock.CaptionConfig config,
                               TableBlock blockConfig,
                               BlockValidationContext context,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        String caption = table.getTitle();
        
        if (config.isRequired() && (caption == null || caption.trim().isEmpty())) {
            CaptionPosition pos = findCaptionPosition(table, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(CAPTION_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Table caption is required but not provided")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(".Table Title")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build());
            return;
        }
        
        if (caption != null && !caption.trim().isEmpty()) {
            // Validate caption pattern
            if (config.getPattern() != null) {
                Pattern pattern = config.getPattern();
                if (!pattern.matcher(caption).matches()) {
                    CaptionPosition pos = findCaptionPosition(table, context);
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId(CAPTION_PATTERN)
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Table caption does not match required pattern")
                        .actualValue(caption)
                        .expectedValue("Pattern: " + config.getPattern())
                        .build());
                }
            }
            
            // Validate caption length
            if (config.getMinLength() != null && caption.length() < config.getMinLength()) {
                CaptionPosition pos = findCaptionPosition(table, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(CAPTION_MIN_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Table caption is too short")
                    .actualValue(caption.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && caption.length() > config.getMaxLength()) {
                CaptionPosition pos = findCaptionPosition(table, context);
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(CAPTION_MAX_LENGTH)
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Table caption is too long")
                    .actualValue(caption.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateFormat(Table table, TableBlock.FormatConfig config,
                              TableBlock blockConfig,
                              BlockValidationContext context,
                              List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Validate table style
        if (config.getStyle() != null) {
            Object styleObj = table.getAttribute(OPTIONS);
            String actualStyle = styleObj != null ? styleObj.toString() : null;
            if (actualStyle == null || !actualStyle.contains(config.getStyle())) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(FORMAT_STYLE)
                    .location(context.createLocation(table))
                    .message("Table does not have required style")
                    .actualValue(actualStyle != null ? actualStyle : "default")
                    .expectedValue("Style: " + config.getStyle())
                    .build());
            }
        }
        
        // Validate borders
        if (config.getBorders() != null && config.getBorders()) {
            Object frameObj = table.getAttribute(FRAME);
            String frame = frameObj != null ? frameObj.toString() : null;
            if (frame == null || "none".equals(frame)) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(FORMAT_BORDERS)
                    .location(context.createLocation(table))
                    .message("Table must have borders")
                    .actualValue("No borders")
                    .expectedValue("Borders required")
                    .build());
            }
        }
    }
    
    /**
     * Finds the position for table caption.
     */
    private CaptionPosition findCaptionPosition(Table table, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || table.getSourceLocation() == null) {
            return new CaptionPosition(1, 1, table.getSourceLocation() != null ? table.getSourceLocation().getLineNumber() : 1);
        }
        
        int tableLineNum = table.getSourceLocation().getLineNumber();
        String caption = table.getTitle();
        
        // Caption (title) is typically on the line before the table
        if (caption != null && !caption.isEmpty() && tableLineNum > 1) {
            // Check line before table
            int captionLineNum = tableLineNum - 1;
            if (captionLineNum <= fileLines.size()) {
                String captionLine = fileLines.get(captionLineNum - 1);
                
                // Check if line starts with "." followed by caption
                if (captionLine.startsWith(".")) {
                    // Caption starts at column 1 (the dot) and ends at the line length
                    return new CaptionPosition(1, captionLine.length(), captionLineNum);
                }
            }
        }
        
        // Default to table line if caption not found
        return new CaptionPosition(1, 1, tableLineNum);
    }
    
    /**
     * Finds the position for table header.
     */
    private HeaderPosition findHeaderPosition(Table table, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || table.getSourceLocation() == null) {
            return new HeaderPosition(1, 1, table.getSourceLocation() != null ? table.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = table.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new HeaderPosition(1, 1, lineNum);
        }
        
        // Find the line after |===
        for (int i = lineNum - 1; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if (line.trim().equals("|===")) {
                // Header should be on the next line
                return new HeaderPosition(1, 1, i + 2);
            }
        }
        
        return new HeaderPosition(1, 1, lineNum + 1);
    }
    
    /**
     * Finds the position for a specific header cell.
     */
    private HeaderPosition findHeaderCellPosition(Table table, BlockValidationContext context, String cellContent) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || table.getSourceLocation() == null) {
            return new HeaderPosition(1, 1, table.getSourceLocation() != null ? table.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = table.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new HeaderPosition(1, 1, lineNum);
        }
        
        // Find the line after |===
        for (int i = lineNum - 1; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if (line.trim().equals("|===")) {
                // Header should be on the next line
                int headerLineNum = i + 2;
                if (headerLineNum <= fileLines.size()) {
                    String headerLine = fileLines.get(headerLineNum - 1);
                    // Find the specific cell content
                    int cellStart = headerLine.indexOf(cellContent);
                    if (cellStart >= 0) {
                        return new HeaderPosition(cellStart + 1, cellStart + cellContent.length(), headerLineNum);
                    }
                }
                break;
            }
        }
        
        return new HeaderPosition(1, 1, lineNum);
    }
    
    private static class CaptionPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        CaptionPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    private static class HeaderPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        HeaderPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}