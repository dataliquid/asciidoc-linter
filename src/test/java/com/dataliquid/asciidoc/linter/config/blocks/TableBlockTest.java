package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("TableBlock")
class TableBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build TableBlock with all attributes")
        void shouldBuildTableBlockWithAllAttributes() {
            // Given
            TableBlock.DimensionConfig columnsRule = TableBlock.DimensionConfig
                    .builder()
                    .min(2)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.DimensionConfig rowsRule = TableBlock.DimensionConfig
                    .builder()
                    .min(1)
                    .max(100)
                    .severity(Severity.WARN)
                    .build();

            TableBlock.HeaderConfig headerRule = TableBlock.HeaderConfig
                    .builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.CaptionConfig captionRule = TableBlock.CaptionConfig
                    .builder()
                    .required(true)
                    .pattern("^Table \\d+:")
                    .minLength(10)
                    .maxLength(200)
                    .severity(Severity.WARN)
                    .build();

            TableBlock.FormatConfig formatRule = TableBlock.FormatConfig
                    .builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();

            // When
            TableBlock table = TableBlock
                    .builder()
                    .name("data-tables")
                    .severity(Severity.ERROR)
                    .columns(columnsRule)
                    .rows(rowsRule)
                    .header(headerRule)
                    .caption(captionRule)
                    .format(formatRule)
                    .build();

            // Then
            assertEquals("data-tables", table.getName());
            assertEquals(Severity.ERROR, table.getSeverity());

            assertNotNull(table.getColumns());
            assertEquals(2, table.getColumns().getMin());
            assertEquals(10, table.getColumns().getMax());
            assertEquals(Severity.ERROR, table.getColumns().getSeverity());

            assertNotNull(table.getRows());
            assertEquals(1, table.getRows().getMin());
            assertEquals(100, table.getRows().getMax());
            assertEquals(Severity.WARN, table.getRows().getSeverity());

            assertNotNull(table.getHeader());
            assertTrue(table.getHeader().isRequired());
            assertNotNull(table.getHeader().getPattern());
            assertEquals(Severity.ERROR, table.getHeader().getSeverity());

            assertNotNull(table.getCaption());
            assertTrue(table.getCaption().isRequired());
            assertNotNull(table.getCaption().getPattern());
            assertEquals(10, table.getCaption().getMinLength());
            assertEquals(200, table.getCaption().getMaxLength());
            assertEquals(Severity.WARN, table.getCaption().getSeverity());

            assertNotNull(table.getFormat());
            assertEquals("grid", table.getFormat().getStyle());
            assertTrue(table.getFormat().getBorders());
            assertEquals(Severity.INFO, table.getFormat().getSeverity());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                TableBlock.builder().build();
            });
        }
    }

    @Nested
    @DisplayName("DimensionConfig Tests")
    class DimensionConfigTests {

        @Test
        @DisplayName("should create DimensionConfig with min and max")
        void shouldCreateDimensionConfigWithMinAndMax() {
            // Given & When
            TableBlock.DimensionConfig dimensionRule = TableBlock.DimensionConfig
                    .builder()
                    .min(3)
                    .max(15)
                    .severity(Severity.ERROR)
                    .build();

            // Then
            assertEquals(3, dimensionRule.getMin());
            assertEquals(15, dimensionRule.getMax());
            assertEquals(Severity.ERROR, dimensionRule.getSeverity());
        }

        @Test
        @DisplayName("should create DimensionConfig with only min")
        void shouldCreateDimensionConfigWithOnlyMin() {
            // When
            TableBlock.DimensionConfig dimensionRule = TableBlock.DimensionConfig
                    .builder()
                    .min(5)
                    .severity(Severity.WARN)
                    .build();

            // Then
            assertEquals(5, dimensionRule.getMin());
            assertNull(dimensionRule.getMax());
            assertEquals(Severity.WARN, dimensionRule.getSeverity());
        }

        @Test
        @DisplayName("should create DimensionConfig with only max")
        void shouldCreateDimensionConfigWithOnlyMax() {
            // When
            TableBlock.DimensionConfig dimensionRule = TableBlock.DimensionConfig
                    .builder()
                    .max(20)
                    .severity(Severity.INFO)
                    .build();

            // Then
            assertNull(dimensionRule.getMin());
            assertEquals(20, dimensionRule.getMax());
            assertEquals(Severity.INFO, dimensionRule.getSeverity());
        }
    }

    @Nested
    @DisplayName("HeaderConfig Tests")
    class HeaderConfigTests {

        @Test
        @DisplayName("should create HeaderConfig with string pattern")
        void shouldCreateHeaderConfigWithStringPattern() {
            // Given & When
            TableBlock.HeaderConfig headerRule = TableBlock.HeaderConfig
                    .builder()
                    .required(true)
                    .pattern("^[A-Z][a-zA-Z\\s]+$")
                    .severity(Severity.ERROR)
                    .build();

            // Then
            assertTrue(headerRule.isRequired());
            assertNotNull(headerRule.getPattern());
            assertEquals("^[A-Z][a-zA-Z\\s]+$", headerRule.getPattern().pattern());
            assertEquals(Severity.ERROR, headerRule.getSeverity());
        }

        @Test
        @DisplayName("should create HeaderConfig with Pattern object")
        void shouldCreateHeaderConfigWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^Header.*");

            // When
            TableBlock.HeaderConfig headerRule = TableBlock.HeaderConfig
                    .builder()
                    .required(false)
                    .pattern(pattern)
                    .severity(Severity.WARN)
                    .build();

            // Then
            assertFalse(headerRule.isRequired());
            assertEquals(pattern, headerRule.getPattern());
            assertEquals(Severity.WARN, headerRule.getSeverity());
        }

        @Test
        @DisplayName("should allow optional severity for HeaderConfig")
        void shouldAllowOptionalSeverityForHeaderConfig() {
            // Given & When
            TableBlock.HeaderConfig config = TableBlock.HeaderConfig.builder().required(true).pattern("test").build();

            // Then
            assertNull(config.getSeverity());
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
        }
    }

    @Nested
    @DisplayName("CaptionConfig Tests")
    class CaptionConfigTests {

        @Test
        @DisplayName("should create CaptionConfig with all attributes")
        void shouldCreateCaptionConfigWithAllAttributes() {
            // Given & When
            TableBlock.CaptionConfig captionRule = TableBlock.CaptionConfig
                    .builder()
                    .required(true)
                    .pattern("^Table \\d+: .*")
                    .minLength(15)
                    .maxLength(150)
                    .severity(Severity.ERROR)
                    .build();

            // Then
            assertTrue(captionRule.isRequired());
            assertNotNull(captionRule.getPattern());
            assertEquals(15, captionRule.getMinLength());
            assertEquals(150, captionRule.getMaxLength());
            assertEquals(Severity.ERROR, captionRule.getSeverity());
        }

        @Test
        @DisplayName("should create CaptionConfig without pattern")
        void shouldCreateCaptionConfigWithoutPattern() {
            // Given & When
            TableBlock.CaptionConfig captionRule = TableBlock.CaptionConfig
                    .builder()
                    .required(false)
                    .minLength(5)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();

            // Then
            assertFalse(captionRule.isRequired());
            assertNull(captionRule.getPattern());
            assertEquals(5, captionRule.getMinLength());
            assertEquals(100, captionRule.getMaxLength());
            assertEquals(Severity.WARN, captionRule.getSeverity());
        }

        @Test
        @DisplayName("should allow optional severity for CaptionConfig")
        void shouldAllowOptionalSeverityForCaptionConfig() {
            // Given & When
            TableBlock.CaptionConfig config = TableBlock.CaptionConfig
                    .builder()
                    .required(true)
                    .minLength(10)
                    .maxLength(50)
                    .build();

            // Then
            assertNull(config.getSeverity());
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(50, config.getMaxLength());
        }
    }

    @Nested
    @DisplayName("FormatConfig Tests")
    class FormatConfigTests {

        @Test
        @DisplayName("should create FormatConfig with style and borders")
        void shouldCreateFormatConfigWithStyleAndBorders() {
            // Given & When
            TableBlock.FormatConfig formatRule = TableBlock.FormatConfig
                    .builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();

            // Then
            assertEquals("grid", formatRule.getStyle());
            assertTrue(formatRule.getBorders());
            assertEquals(Severity.INFO, formatRule.getSeverity());
        }

        @Test
        @DisplayName("should create FormatConfig with only style")
        void shouldCreateFormatConfigWithOnlyStyle() {
            // Given & When
            TableBlock.FormatConfig formatRule = TableBlock.FormatConfig
                    .builder()
                    .style("simple")
                    .severity(Severity.WARN)
                    .build();

            // Then
            assertEquals("simple", formatRule.getStyle());
            assertNull(formatRule.getBorders());
            assertEquals(Severity.WARN, formatRule.getSeverity());
        }

        @Test
        @DisplayName("should create FormatConfig with only borders")
        void shouldCreateFormatConfigWithOnlyBorders() {
            // Given & When
            TableBlock.FormatConfig formatRule = TableBlock.FormatConfig
                    .builder()
                    .borders(false)
                    .severity(Severity.ERROR)
                    .build();

            // Then
            assertNull(formatRule.getStyle());
            assertFalse(formatRule.getBorders());
            assertEquals(Severity.ERROR, formatRule.getSeverity());
        }

        @Test
        @DisplayName("should allow optional severity for FormatConfig")
        void shouldAllowOptionalSeverityForFormatConfig() {
            // Given & When
            TableBlock.FormatConfig config = TableBlock.FormatConfig.builder().style("grid").borders(true).build();

            // Then
            assertNull(config.getSeverity());
            assertEquals("grid", config.getStyle());
            assertTrue(config.getBorders());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            TableBlock.DimensionConfig columns1 = TableBlock.DimensionConfig
                    .builder()
                    .min(2)
                    .max(8)
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.DimensionConfig columns2 = TableBlock.DimensionConfig
                    .builder()
                    .min(2)
                    .max(8)
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.HeaderConfig header1 = TableBlock.HeaderConfig
                    .builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build();

            TableBlock.HeaderConfig header2 = TableBlock.HeaderConfig
                    .builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build();

            // When
            TableBlock table1 = TableBlock.builder().severity(Severity.ERROR).columns(columns1).header(header1).build();

            TableBlock table2 = TableBlock.builder().severity(Severity.ERROR).columns(columns2).header(header2).build();

            TableBlock table3 = TableBlock.builder().severity(Severity.WARN).columns(columns1).header(header1).build();

            // Then
            assertEquals(table1, table2);
            assertNotEquals(table1, table3);
            assertEquals(table1.hashCode(), table2.hashCode());
            assertNotEquals(table1.hashCode(), table3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            TableBlock.DimensionConfig dim1 = TableBlock.DimensionConfig
                    .builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.DimensionConfig dim2 = TableBlock.DimensionConfig
                    .builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();

            TableBlock.HeaderConfig header1 = TableBlock.HeaderConfig
                    .builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();

            TableBlock.HeaderConfig header2 = TableBlock.HeaderConfig
                    .builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();

            TableBlock.CaptionConfig caption1 = TableBlock.CaptionConfig
                    .builder()
                    .required(true)
                    .pattern("^Table.*")
                    .minLength(10)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();

            TableBlock.CaptionConfig caption2 = TableBlock.CaptionConfig
                    .builder()
                    .required(true)
                    .pattern("^Table.*")
                    .minLength(10)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();

            TableBlock.FormatConfig format1 = TableBlock.FormatConfig
                    .builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();

            TableBlock.FormatConfig format2 = TableBlock.FormatConfig
                    .builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();

            // Then
            assertEquals(dim1, dim2);
            assertEquals(dim1.hashCode(), dim2.hashCode());

            assertEquals(header1, header2);
            assertEquals(header1.hashCode(), header2.hashCode());

            assertEquals(caption1, caption2);
            assertEquals(caption1.hashCode(), caption2.hashCode());

            assertEquals(format1, format2);
            assertEquals(format1.hashCode(), format2.hashCode());
        }
    }
}
