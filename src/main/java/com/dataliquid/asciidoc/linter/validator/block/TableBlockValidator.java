package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
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
                .ruleId("table.columns.min")
                .location(context.createLocation(table))
                .message("Table has too few columns")
                .actualValue(String.valueOf(columnCount))
                .expectedValue("At least " + config.getMin() + " columns")
                .build());
        }
        
        if (config.getMax() != null && columnCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("table.columns.max")
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
                .ruleId("table.rows.min")
                .location(context.createLocation(table))
                .message("Table has too few rows")
                .actualValue(String.valueOf(rowCount))
                .expectedValue("At least " + config.getMin() + " rows")
                .build());
        }
        
        if (config.getMax() != null && rowCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("table.rows.max")
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
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("table.header.required")
                .location(context.createLocation(table))
                .message("Table must have a header row")
                .actualValue("No header")
                .expectedValue("Header row required")
                .build());
        }
        
        // Validate header pattern if header exists
        if (hasHeader && config.getPattern() != null) {
            Pattern pattern = config.getPattern();
            
            for (Row headerRow : table.getHeader()) {
                for (Cell cell : headerRow.getCells()) {
                    String content = cell.getText();
                    if (!pattern.matcher(content).matches()) {
                        messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId("table.header.pattern")
                            .location(context.createLocation(table))
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
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("table.caption.required")
                .location(context.createLocation(table))
                .message("Table must have a caption")
                .actualValue("No caption")
                .expectedValue("Caption required")
                .build());
            return;
        }
        
        if (caption != null && !caption.trim().isEmpty()) {
            // Validate caption pattern
            if (config.getPattern() != null) {
                Pattern pattern = config.getPattern();
                if (!pattern.matcher(caption).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("table.caption.pattern")
                        .location(context.createLocation(table))
                        .message("Table caption does not match required pattern")
                        .actualValue(caption)
                        .expectedValue("Pattern: " + config.getPattern())
                        .build());
                }
            }
            
            // Validate caption length
            if (config.getMinLength() != null && caption.length() < config.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("table.caption.minLength")
                    .location(context.createLocation(table))
                    .message("Table caption is too short")
                    .actualValue(caption.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && caption.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("table.caption.maxLength")
                    .location(context.createLocation(table))
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
            Object styleObj = table.getAttribute("options");
            String actualStyle = styleObj != null ? styleObj.toString() : null;
            if (actualStyle == null || !actualStyle.contains(config.getStyle())) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("table.format.style")
                    .location(context.createLocation(table))
                    .message("Table does not have required style")
                    .actualValue(actualStyle != null ? actualStyle : "default")
                    .expectedValue("Style: " + config.getStyle())
                    .build());
            }
        }
        
        // Validate borders
        if (config.getBorders() != null && config.getBorders()) {
            Object frameObj = table.getAttribute("frame");
            String frame = frameObj != null ? frameObj.toString() : null;
            if (frame == null || "none".equals(frame)) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("table.format.borders")
                    .location(context.createLocation(table))
                    .message("Table must have borders")
                    .actualValue("No borders")
                    .expectedValue("Borders required")
                    .build());
            }
        }
    }
}