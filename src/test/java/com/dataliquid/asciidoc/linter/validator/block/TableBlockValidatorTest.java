package com.dataliquid.asciidoc.linter.validator.block;
import com.dataliquid.asciidoc.linter.config.blocks.BlockType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Unit tests for {@link TableBlockValidator}.
 * 
 * <p>This test class validates the behavior of the table block validator,
 * which processes table structures in AsciiDoc documents. The tests cover
 * comprehensive validation rules for table dimensions, headers, captions,
 * and formatting options.</p>
 * 
 * <p>Test structure follows a nested class pattern for better organization:</p>
 * <ul>
 *   <li>Validate - Basic validator functionality and type checking</li>
 *   <li>ColumnsValidation - Column count constraints and severity hierarchy</li>
 *   <li>RowsValidation - Row count constraints and severity hierarchy</li>
 *   <li>HeaderValidation - Header requirements and pattern matching</li>
 *   <li>CaptionValidation - Caption requirements, patterns, and length constraints</li>
 *   <li>FormatValidation - Table formatting options (style, borders)</li>
 * </ul>
 * 
 * <p>Each nested test class includes severity hierarchy tests to verify that
 * nested configuration severity overrides block-level severity when specified,
 * and falls back to block severity when not specified.</p>
 * 
 * @see TableBlockValidator
 * @see TableBlock
 */
@DisplayName("TableBlockValidator")
class TableBlockValidatorTest {
    
    private TableBlockValidator validator;
    private BlockValidationContext context;
    private Table mockTable;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new TableBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockTable = mock(Table.class);
    }
    
    @Test
    @DisplayName("should return TABLE as supported type")
    void shouldReturnTableAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.TABLE, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when table is not Table instance")
        void shouldReturnEmptyListWhenNotTableInstance() {
            // Given
            StructuralNode notATable = mock(StructuralNode.class);
            TableBlock config = TableBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(notATable, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            TableBlock config = TableBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("columns validation")
    class ColumnsValidation {
        
        @Test
        @DisplayName("should validate minimum columns")
        void shouldValidateMinimumColumns() {
            // Given
            TableBlock.DimensionConfig columnsConfig = TableBlock.DimensionConfig.builder()
                .min(3)
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .columns(columnsConfig)
                .severity(Severity.ERROR)
                .build();
            
            Column col1 = mock(Column.class);
            Column col2 = mock(Column.class);
            when(mockTable.getColumns()).thenReturn(Arrays.asList(col1, col2));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("table.columns.min", msg.getRuleId());
            assertEquals("Table has too few columns", msg.getMessage());
            assertEquals("2", msg.getActualValue().orElse(null));
            assertEquals("At least 3 columns", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum columns")
        void shouldValidateMaximumColumns() {
            // Given
            TableBlock.DimensionConfig columnsConfig = TableBlock.DimensionConfig.builder()
                .max(2)
                .severity(Severity.WARN)
                .build();
            TableBlock config = TableBlock.builder()
                .columns(columnsConfig)
                .severity(Severity.ERROR)
                .build();
            
            Column col1 = mock(Column.class);
            Column col2 = mock(Column.class);
            Column col3 = mock(Column.class);
            when(mockTable.getColumns()).thenReturn(Arrays.asList(col1, col2, col3));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("table.columns.max", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should use columns severity over block severity")
        void shouldUseColumnsSeverityOverBlockSeverity() {
            // Given - columns has WARN, block has ERROR
            TableBlock.DimensionConfig columnsConfig = TableBlock.DimensionConfig.builder()
                .min(3)
                .severity(Severity.WARN)
                .build();
            TableBlock config = TableBlock.builder()
                .columns(columnsConfig)
                .severity(Severity.ERROR)
                .build();
            
            Column col1 = mock(Column.class);
            when(mockTable.getColumns()).thenReturn(Collections.singletonList(col1));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(), 
                "Should use columns severity (WARN) instead of block severity (ERROR)");
            assertEquals("table.columns.min", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should use block severity when columns severity is not defined")
        void shouldUseBlockSeverityWhenColumnsSeverityNotDefined() {
            // Given - columns has no severity, block has INFO
            TableBlock.DimensionConfig columnsConfig = TableBlock.DimensionConfig.builder()
                .min(3)
                // No severity set
                .build();
            TableBlock config = TableBlock.builder()
                .columns(columnsConfig)
                .severity(Severity.INFO)
                .build();
            
            Column col1 = mock(Column.class);
            when(mockTable.getColumns()).thenReturn(Collections.singletonList(col1));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(), 
                "Should use block severity (INFO) when columns severity is not defined");
            assertEquals("table.columns.min", msg.getRuleId());
        }
    }
    
    @Nested
    @DisplayName("rows validation")
    class RowsValidation {
        
        @Test
        @DisplayName("should validate minimum rows")
        void shouldValidateMinimumRows() {
            // Given
            TableBlock.DimensionConfig rowsConfig = TableBlock.DimensionConfig.builder()
                .min(5)
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .rows(rowsConfig)
                .severity(Severity.ERROR)
                .build();
            
            Row row1 = mock(Row.class);
            Row row2 = mock(Row.class);
            Row row3 = mock(Row.class);
            when(mockTable.getBody()).thenReturn(Arrays.asList(row1, row2, row3));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.rows.min", msg.getRuleId());
            assertEquals("Table has too few rows", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At least 5 rows", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should use rows severity over block severity")
        void shouldUseRowsSeverityOverBlockSeverity() {
            // Given - rows has INFO, block has ERROR
            TableBlock.DimensionConfig rowsConfig = TableBlock.DimensionConfig.builder()
                .min(5)
                .severity(Severity.INFO)
                .build();
            TableBlock config = TableBlock.builder()
                .rows(rowsConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getBody()).thenReturn(Collections.emptyList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(), 
                "Should use rows severity (INFO) instead of block severity (ERROR)");
            assertEquals("table.rows.min", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should use block severity when rows severity is not defined")
        void shouldUseBlockSeverityWhenRowsSeverityNotDefined() {
            // Given - rows has no severity, block has WARN
            TableBlock.DimensionConfig rowsConfig = TableBlock.DimensionConfig.builder()
                .min(5)
                // No severity set
                .build();
            TableBlock config = TableBlock.builder()
                .rows(rowsConfig)
                .severity(Severity.WARN)
                .build();
            
            when(mockTable.getBody()).thenReturn(Collections.emptyList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(), 
                "Should use block severity (WARN) when rows severity is not defined");
            assertEquals("table.rows.min", msg.getRuleId());
        }
    }
    
    @Nested
    @DisplayName("header validation")
    class HeaderValidation {
        
        @Test
        @DisplayName("should validate required header")
        void shouldValidateRequiredHeader() {
            // Given
            TableBlock.HeaderConfig headerConfig = TableBlock.HeaderConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .header(headerConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getHeader()).thenReturn(Collections.emptyList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.header.required", msg.getRuleId());
            assertEquals("Table header is required but not provided", msg.getMessage());
            // The validator may or may not set actualValue/expectedValue - don't assert on them
        }
        
        @Test
        @DisplayName("should validate header pattern")
        void shouldValidateHeaderPattern() {
            // Given
            TableBlock.HeaderConfig headerConfig = TableBlock.HeaderConfig.builder()
                .required(true)
                .pattern(Pattern.compile("^[A-Z].*"))
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .header(headerConfig)
                .severity(Severity.ERROR)
                .build();
            
            Row headerRow = mock(Row.class);
            Cell cell1 = mock(Cell.class);
            Cell cell2 = mock(Cell.class);
            when(cell1.getText()).thenReturn("Name");
            when(cell2.getText()).thenReturn("age"); // lowercase - should fail
            when(headerRow.getCells()).thenReturn(Arrays.asList(cell1, cell2));
            when(mockTable.getHeader()).thenReturn(Arrays.asList(headerRow));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.header.pattern", msg.getRuleId());
            assertEquals("Table header does not match required pattern", msg.getMessage());
            assertEquals("age", msg.getActualValue().orElse(null));
            assertEquals("Pattern: ^[A-Z].*", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should use header severity over block severity")
        void shouldUseHeaderSeverityOverBlockSeverity() {
            // Given - header has WARN, block has ERROR
            TableBlock.HeaderConfig headerConfig = TableBlock.HeaderConfig.builder()
                .required(true)
                .severity(Severity.WARN)
                .build();
            TableBlock config = TableBlock.builder()
                .header(headerConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getHeader()).thenReturn(Collections.emptyList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(), 
                "Should use header severity (WARN) instead of block severity (ERROR)");
            assertEquals("table.header.required", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should use block severity when header severity is not defined")
        void shouldUseBlockSeverityWhenHeaderSeverityNotDefined() {
            // Given - header has no severity, block has ERROR
            TableBlock.HeaderConfig headerConfig = TableBlock.HeaderConfig.builder()
                .required(true)
                // No severity set
                .build();
            TableBlock config = TableBlock.builder()
                .header(headerConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getHeader()).thenReturn(Collections.emptyList());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity(), 
                "Should use block severity (ERROR) when header severity is not defined");
            assertEquals("table.header.required", msg.getRuleId());
        }
    }
    
    @Nested
    @DisplayName("caption validation")
    class CaptionValidation {
        
        @Test
        @DisplayName("should validate required caption")
        void shouldValidateRequiredCaption() {
            // Given
            TableBlock.CaptionConfig captionConfig = TableBlock.CaptionConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .caption(captionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getTitle()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.caption.required", msg.getRuleId());
            assertEquals("Table caption is required but not provided", msg.getMessage());
        }
        
        @Test
        @DisplayName("should validate caption min length")
        void shouldValidateCaptionMinLength() {
            // Given
            TableBlock.CaptionConfig captionConfig = TableBlock.CaptionConfig.builder()
                .minLength(10)
                .severity(Severity.WARN)
                .build();
            TableBlock config = TableBlock.builder()
                .caption(captionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getTitle()).thenReturn("Short");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.caption.minLength", msg.getRuleId());
            assertEquals("Table caption is too short", msg.getMessage());
            assertEquals("5 characters", msg.getActualValue().orElse(null));
            assertEquals("At least 10 characters", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate caption pattern")
        void shouldValidateCaptionPattern() {
            // Given
            TableBlock.CaptionConfig captionConfig = TableBlock.CaptionConfig.builder()
                .pattern(Pattern.compile("^Table \\d+:.*"))
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .caption(captionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getTitle()).thenReturn("Invalid caption");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.caption.pattern", msg.getRuleId());
            assertEquals("Table caption does not match required pattern", msg.getMessage());
            assertEquals("Invalid caption", msg.getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should use caption severity over block severity")
        void shouldUseCaptionSeverityOverBlockSeverity() {
            // Given - caption has INFO, block has ERROR
            TableBlock.CaptionConfig captionConfig = TableBlock.CaptionConfig.builder()
                .required(true)
                .severity(Severity.INFO)
                .build();
            TableBlock config = TableBlock.builder()
                .caption(captionConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getTitle()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity(), 
                "Should use caption severity (INFO) instead of block severity (ERROR)");
            assertEquals("table.caption.required", msg.getRuleId());
        }
        
        @Test
        @DisplayName("should use block severity when caption severity is not defined")
        void shouldUseBlockSeverityWhenCaptionSeverityNotDefined() {
            // Given - caption has no severity, block has WARN
            TableBlock.CaptionConfig captionConfig = TableBlock.CaptionConfig.builder()
                .required(true)
                // No severity set
                .build();
            TableBlock config = TableBlock.builder()
                .caption(captionConfig)
                .severity(Severity.WARN)
                .build();
            
            when(mockTable.getTitle()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity(), 
                "Should use block severity (WARN) when caption severity is not defined");
            assertEquals("table.caption.required", msg.getRuleId());
        }
    }
    
    @Nested
    @DisplayName("format validation")
    class FormatValidation {
        
        @Test
        @DisplayName("should validate table style")
        void shouldValidateTableStyle() {
            // Given
            TableBlock.FormatConfig formatConfig = TableBlock.FormatConfig.builder()
                .style("grid")
                .severity(Severity.INFO)
                .build();
            TableBlock config = TableBlock.builder()
                .format(formatConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getAttribute("options")).thenReturn("header,footer");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.format.style", msg.getRuleId());
            assertEquals("Table does not have required style", msg.getMessage());
            assertEquals("header,footer", msg.getActualValue().orElse(null));
            assertEquals("Style: grid", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate table borders")
        void shouldValidateTableBorders() {
            // Given
            TableBlock.FormatConfig formatConfig = TableBlock.FormatConfig.builder()
                .borders(true)
                .severity(Severity.ERROR)
                .build();
            TableBlock config = TableBlock.builder()
                .format(formatConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockTable.getAttribute("frame")).thenReturn("none");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockTable, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("table.format.borders", msg.getRuleId());
            assertEquals("Table must have borders", msg.getMessage());
            assertEquals("No borders", msg.getActualValue().orElse(null));
            assertEquals("Borders required", msg.getExpectedValue().orElse(null));
        }
    }
}