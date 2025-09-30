package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

class TableBlockTest {

    @Nested
    class BuilderTests {

        @Test
        void shouldBuildTableBlockWithAllAttributes() {
            // given
            TableBlock.DimensionConfig columnsRule = new TableBlock.DimensionConfig(2, // min
                    10, // max
                    Severity.ERROR); // severity

            TableBlock.DimensionConfig rowsRule = new TableBlock.DimensionConfig(1, // min
                    100, // max
                    Severity.WARN); // severity

            TableBlock.HeaderConfig headerRule = new TableBlock.HeaderConfig(true, // required
                    "^[A-Z].*", // pattern
                    Severity.ERROR); // severity

            TableBlock.CaptionConfig captionRule = new TableBlock.CaptionConfig(true, // required
                    "^Table \\d+:", // pattern
                    10, // minLength
                    200, // maxLength
                    Severity.WARN); // severity

            TableBlock.FormatConfig formatRule = new TableBlock.FormatConfig("grid", // style
                    true, // borders
                    Severity.INFO); // severity

            // when
            TableBlock table = new TableBlock("data-tables", // name
                    Severity.ERROR, // severity
                    null, // occurrence
                    null, // order
                    columnsRule, // columns
                    rowsRule, // rows
                    headerRule, // header
                    captionRule, // caption
                    formatRule); // format

            // then
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
        void shouldRequireSeverity() {
            // when & then
            assertThrows(NullPointerException.class, () -> {
                new TableBlock(null, null, null, null, null, null, null, null, null);
            });
        }
    }

    @Nested
    class DimensionConfigTests {

        @Test
        void shouldCreateDimensionConfigWithMinAndMax() {
            // given & when
            TableBlock.DimensionConfig dimensionRule = new TableBlock.DimensionConfig(3, // min
                    15, // max
                    Severity.ERROR); // severity

            // then
            assertEquals(3, dimensionRule.getMin());
            assertEquals(15, dimensionRule.getMax());
            assertEquals(Severity.ERROR, dimensionRule.getSeverity());
        }

        @Test
        void shouldCreateDimensionConfigWithOnlyMin() {
            // when
            TableBlock.DimensionConfig dimensionRule = new TableBlock.DimensionConfig(5, // min
                    null, // max
                    Severity.WARN); // severity

            // then
            assertEquals(5, dimensionRule.getMin());
            assertNull(dimensionRule.getMax());
            assertEquals(Severity.WARN, dimensionRule.getSeverity());
        }

        @Test
        void shouldCreateDimensionConfigWithOnlyMax() {
            // when
            TableBlock.DimensionConfig dimensionRule = new TableBlock.DimensionConfig(null, // min
                    20, // max
                    Severity.INFO); // severity

            // then
            assertNull(dimensionRule.getMin());
            assertEquals(20, dimensionRule.getMax());
            assertEquals(Severity.INFO, dimensionRule.getSeverity());
        }
    }

    @Nested
    class HeaderConfigTests {

        @Test
        void shouldCreateHeaderConfigWithStringPattern() {
            // given & when
            TableBlock.HeaderConfig headerRule = new TableBlock.HeaderConfig(true, // required
                    "^[A-Z][a-zA-Z\\s]+$", // pattern
                    Severity.ERROR); // severity

            // then
            assertTrue(headerRule.isRequired());
            assertNotNull(headerRule.getPattern());
            assertEquals("^[A-Z][a-zA-Z\\s]+$", headerRule.getPattern().pattern());
            assertEquals(Severity.ERROR, headerRule.getSeverity());
        }

        @Test
        void shouldCreateHeaderConfigWithPatternObject() {
            // given & when
            TableBlock.HeaderConfig headerRule = new TableBlock.HeaderConfig(false, // required
                    "^Header.*", // pattern
                    Severity.WARN); // severity

            // then
            assertFalse(headerRule.isRequired());
            assertNotNull(headerRule.getPattern());
            assertEquals("^Header.*", headerRule.getPattern().pattern());
            assertEquals(Severity.WARN, headerRule.getSeverity());
        }

        @Test
        void shouldAllowOptionalSeverityForHeaderConfig() {
            // given & when
            TableBlock.HeaderConfig config = new TableBlock.HeaderConfig(true, // required
                    "test", // pattern
                    null); // severity

            // then
            assertNull(config.getSeverity());
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
        }
    }

    @Nested
    class CaptionConfigTests {

        @Test
        void shouldCreateCaptionConfigWithAllAttributes() {
            // given & when
            TableBlock.CaptionConfig captionRule = new TableBlock.CaptionConfig(true, // required
                    "^Table \\d+: .*", // pattern
                    15, // minLength
                    150, // maxLength
                    Severity.ERROR); // severity

            // then
            assertTrue(captionRule.isRequired());
            assertNotNull(captionRule.getPattern());
            assertEquals(15, captionRule.getMinLength());
            assertEquals(150, captionRule.getMaxLength());
            assertEquals(Severity.ERROR, captionRule.getSeverity());
        }

        @Test
        void shouldCreateCaptionConfigWithoutPattern() {
            // given & when
            TableBlock.CaptionConfig captionRule = new TableBlock.CaptionConfig(false, // required
                    null, // pattern
                    5, // minLength
                    100, // maxLength
                    Severity.WARN); // severity

            // then
            assertFalse(captionRule.isRequired());
            assertNull(captionRule.getPattern());
            assertEquals(5, captionRule.getMinLength());
            assertEquals(100, captionRule.getMaxLength());
            assertEquals(Severity.WARN, captionRule.getSeverity());
        }

        @Test
        void shouldAllowOptionalSeverityForCaptionConfig() {
            // given & when
            TableBlock.CaptionConfig config = new TableBlock.CaptionConfig(true, // required
                    null, // pattern
                    10, // minLength
                    50, // maxLength
                    null); // severity

            // then
            assertNull(config.getSeverity());
            assertTrue(config.isRequired());
            assertEquals(10, config.getMinLength());
            assertEquals(50, config.getMaxLength());
        }
    }

    @Nested
    class FormatConfigTests {

        @Test
        void shouldCreateFormatConfigWithStyleAndBorders() {
            // given & when
            TableBlock.FormatConfig formatRule = new TableBlock.FormatConfig("grid", // style
                    true, // borders
                    Severity.INFO); // severity

            // then
            assertEquals("grid", formatRule.getStyle());
            assertTrue(formatRule.getBorders());
            assertEquals(Severity.INFO, formatRule.getSeverity());
        }

        @Test
        void shouldCreateFormatConfigWithOnlyStyle() {
            // given & when
            TableBlock.FormatConfig formatRule = new TableBlock.FormatConfig("simple", // style
                    null, // borders
                    Severity.WARN); // severity

            // then
            assertEquals("simple", formatRule.getStyle());
            assertNull(formatRule.getBorders());
            assertEquals(Severity.WARN, formatRule.getSeverity());
        }

        @Test
        void shouldCreateFormatConfigWithOnlyBorders() {
            // given & when
            TableBlock.FormatConfig formatRule = new TableBlock.FormatConfig(null, // style
                    false, // borders
                    Severity.ERROR); // severity

            // then
            assertNull(formatRule.getStyle());
            assertFalse(formatRule.getBorders());
            assertEquals(Severity.ERROR, formatRule.getSeverity());
        }

        @Test
        void shouldAllowOptionalSeverityForFormatConfig() {
            // given & when
            TableBlock.FormatConfig config = new TableBlock.FormatConfig("grid", // style
                    true, // borders
                    null); // severity

            // then
            assertNull(config.getSeverity());
            assertEquals("grid", config.getStyle());
            assertTrue(config.getBorders());
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // given
            TableBlock.DimensionConfig columns1 = new TableBlock.DimensionConfig(2, // min
                    8, // max
                    Severity.ERROR); // severity

            TableBlock.DimensionConfig columns2 = new TableBlock.DimensionConfig(2, // min
                    8, // max
                    Severity.ERROR); // severity

            TableBlock.HeaderConfig header1 = new TableBlock.HeaderConfig(true, // required
                    "^[A-Z].*", // pattern
                    Severity.WARN); // severity

            TableBlock.HeaderConfig header2 = new TableBlock.HeaderConfig(true, // required
                    "^[A-Z].*", // pattern
                    Severity.WARN); // severity

            // when
            TableBlock table1 = new TableBlock(null, Severity.ERROR, null, null, columns1, null, header1, null, null);

            TableBlock table2 = new TableBlock(null, Severity.ERROR, null, null, columns2, null, header2, null, null);

            TableBlock table3 = new TableBlock(null, Severity.WARN, null, null, columns1, null, header1, null, null);

            // then
            assertEquals(table1, table2);
            assertNotEquals(table1, table3);
            assertEquals(table1.hashCode(), table2.hashCode());
            assertNotEquals(table1.hashCode(), table3.hashCode());
        }

        @Test
        void shouldTestInnerClassEqualsAndHashCode() {
            // given
            TableBlock.DimensionConfig dim1 = new TableBlock.DimensionConfig(5, // min
                    10, // max
                    Severity.ERROR); // severity

            TableBlock.DimensionConfig dim2 = new TableBlock.DimensionConfig(5, // min
                    10, // max
                    Severity.ERROR); // severity

            TableBlock.HeaderConfig header1 = new TableBlock.HeaderConfig(false, // required
                    "test", // pattern
                    Severity.INFO); // severity

            TableBlock.HeaderConfig header2 = new TableBlock.HeaderConfig(false, // required
                    "test", // pattern
                    Severity.INFO); // severity

            TableBlock.CaptionConfig caption1 = new TableBlock.CaptionConfig(true, // required
                    "^Table.*", // pattern
                    10, // minLength
                    100, // maxLength
                    Severity.WARN); // severity

            TableBlock.CaptionConfig caption2 = new TableBlock.CaptionConfig(true, // required
                    "^Table.*", // pattern
                    10, // minLength
                    100, // maxLength
                    Severity.WARN); // severity

            TableBlock.FormatConfig format1 = new TableBlock.FormatConfig("grid", // style
                    true, // borders
                    Severity.INFO); // severity

            TableBlock.FormatConfig format2 = new TableBlock.FormatConfig("grid", // style
                    true, // borders
                    Severity.INFO); // severity

            // then
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
