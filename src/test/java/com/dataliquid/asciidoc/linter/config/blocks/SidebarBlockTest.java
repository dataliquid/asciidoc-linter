package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;

class SidebarBlockTest {

    @Nested
    class BuilderTest {

        @Test
        void shouldBuildWithRequiredFields() {
            // given
            String name = "sidebar-test";
            Severity severity = Severity.WARN;

            // when
            SidebarBlock block = new SidebarBlock(name, // name
                    severity, // severity
                    null, // occurrence
                    null, // order
                    null, // title
                    null, // content
                    null); // position

            // then
            assertEquals(name, block.getName());
            assertEquals(severity, block.getSeverity());
            assertEquals(BlockType.SIDEBAR, block.getType());
            assertNull(block.getTitle());
            assertNull(block.getContent());
            assertNull(block.getPosition());
        }

        @Test
        void shouldBuildWithAllFields() {
            // given
            String name = "sidebar-full";
            Severity severity = Severity.ERROR;
            SidebarBlock.TitleConfig title = new SidebarBlock.TitleConfig(true, // required
                    5, // minLength
                    50, // maxLength
                    "^[A-Z].*$", // pattern
                    Severity.INFO); // severity

            SidebarBlock.LinesConfig lines = new SidebarBlock.LinesConfig(3, // min
                    30, // max
                    Severity.INFO); // severity

            SidebarBlock.ContentConfig content = new SidebarBlock.ContentConfig(true, // required
                    50, // minLength
                    800, // maxLength
                    lines); // lines

            SidebarBlock.PositionConfig position = new SidebarBlock.PositionConfig(false, // required
                    List.of("left", "right", "float"), // allowed
                    Severity.INFO); // severity

            // when
            SidebarBlock block = new SidebarBlock(name, // name
                    severity, // severity
                    null, // occurrence
                    null, // order
                    title, // title
                    content, // content
                    position); // position

            // then
            assertEquals(name, block.getName());
            assertEquals(severity, block.getSeverity());
            assertEquals(title, block.getTitle());
            assertEquals(content, block.getContent());
            assertEquals(position, block.getPosition());
        }

        @Test
        void shouldRequireSeverity() {
            // when & then
            assertThrows(NullPointerException.class, () -> new SidebarBlock("test", null, null, null, null, null, null),
                    "severity is required");
        }
    }

    @Nested
    class TitleConfigTest {

        @Test
        void shouldBuildWithAllFields() {
            // given
            boolean required = true;
            Integer minLength = 10;
            Integer maxLength = 100;
            String patternStr = "^[A-Z].*$";
            Severity severity = Severity.WARN;

            // when
            SidebarBlock.TitleConfig config = new SidebarBlock.TitleConfig(required, // required
                    minLength, // minLength
                    maxLength, // maxLength
                    patternStr, // pattern
                    severity); // severity

            // then
            assertEquals(required, config.isRequired());
            assertEquals(minLength, config.getMinLength());
            assertEquals(maxLength, config.getMaxLength());
            assertEquals(patternStr, config.getPattern().pattern());
            assertEquals(severity, config.getSeverity());
        }

        @Test
        void shouldBuildWithPatternString() {
            // given
            String patternStr = "^[A-Z].*$";

            // when
            SidebarBlock.TitleConfig config = new SidebarBlock.TitleConfig(false, // required
                    null, // minLength
                    null, // maxLength
                    patternStr, // pattern
                    null); // severity

            // then
            assertNotNull(config.getPattern());
            assertEquals(patternStr, config.getPattern().pattern());
        }

        @Test
        void shouldHandleNullPatternString() {
            // when
            SidebarBlock.TitleConfig config = new SidebarBlock.TitleConfig(false, // required
                    null, // minLength
                    null, // maxLength
                    null, // pattern
                    null); // severity

            // then
            assertNull(config.getPattern());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            SidebarBlock.TitleConfig config1 = new SidebarBlock.TitleConfig(true, // required
                    5, // minLength
                    50, // maxLength
                    "^[A-Z].*$", // pattern
                    Severity.INFO); // severity

            SidebarBlock.TitleConfig config2 = new SidebarBlock.TitleConfig(true, // required
                    5, // minLength
                    50, // maxLength
                    "^[A-Z].*$", // pattern
                    Severity.INFO); // severity

            SidebarBlock.TitleConfig config3 = new SidebarBlock.TitleConfig(false, // required
                    5, // minLength
                    50, // maxLength
                    "^[A-Z].*$", // pattern
                    Severity.INFO); // severity

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class ContentConfigTest {

        @Test
        void shouldBuildWithAllFields() {
            // given
            boolean required = true;
            Integer minLength = 50;
            Integer maxLength = 800;
            SidebarBlock.LinesConfig lines = new SidebarBlock.LinesConfig(3, // min
                    30, // max
                    Severity.INFO); // severity

            // when
            SidebarBlock.ContentConfig config = new SidebarBlock.ContentConfig(required, // required
                    minLength, // minLength
                    maxLength, // maxLength
                    lines); // lines

            // then
            assertEquals(required, config.isRequired());
            assertEquals(minLength, config.getMinLength());
            assertEquals(maxLength, config.getMaxLength());
            assertEquals(lines, config.getLines());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            SidebarBlock.LinesConfig lines = new SidebarBlock.LinesConfig(3, 30, null);

            SidebarBlock.ContentConfig config1 = new SidebarBlock.ContentConfig(true, // required
                    50, // minLength
                    800, // maxLength
                    lines); // lines

            SidebarBlock.ContentConfig config2 = new SidebarBlock.ContentConfig(true, // required
                    50, // minLength
                    800, // maxLength
                    lines); // lines

            SidebarBlock.ContentConfig config3 = new SidebarBlock.ContentConfig(false, // required
                    50, // minLength
                    800, // maxLength
                    lines); // lines

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class LinesConfigTest {

        @Test
        void shouldBuildWithAllFields() {
            // given
            Integer min = 3;
            Integer max = 30;
            Severity severity = Severity.INFO;

            // when
            SidebarBlock.LinesConfig config = new SidebarBlock.LinesConfig(min, // min
                    max, // max
                    severity); // severity

            // then
            assertEquals(min, config.getMin());
            assertEquals(max, config.getMax());
            assertEquals(severity, config.getSeverity());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            SidebarBlock.LinesConfig config1 = new SidebarBlock.LinesConfig(3, // min
                    30, // max
                    Severity.INFO); // severity

            SidebarBlock.LinesConfig config2 = new SidebarBlock.LinesConfig(3, // min
                    30, // max
                    Severity.INFO); // severity

            SidebarBlock.LinesConfig config3 = new SidebarBlock.LinesConfig(5, // min
                    30, // max
                    Severity.INFO); // severity

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class PositionConfigTest {

        @Test
        void shouldBuildWithAllFields() {
            // given
            boolean required = false;
            List<String> allowed = List.of("left", "right", "float");
            Severity severity = Severity.INFO;

            // when
            SidebarBlock.PositionConfig config = new SidebarBlock.PositionConfig(required, // required
                    allowed, // allowed
                    severity); // severity

            // then
            assertEquals(required, config.isRequired());
            assertEquals(allowed, config.getAllowed());
            assertEquals(severity, config.getSeverity());
        }

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            List<String> allowed = List.of("left", "right");

            SidebarBlock.PositionConfig config1 = new SidebarBlock.PositionConfig(false, // required
                    allowed, // allowed
                    Severity.INFO); // severity

            SidebarBlock.PositionConfig config2 = new SidebarBlock.PositionConfig(false, // required
                    allowed, // allowed
                    Severity.INFO); // severity

            SidebarBlock.PositionConfig config3 = new SidebarBlock.PositionConfig(true, // required
                    allowed, // allowed
                    Severity.INFO); // severity

            // then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }

    @Nested
    class EqualsAndHashCodeTest {

        @Test
        void shouldImplementEqualsAndHashCode() {
            // given
            SidebarBlock.TitleConfig title = new SidebarBlock.TitleConfig(true, null, null, null, null);

            // when
            SidebarBlock block1 = new SidebarBlock("sidebar1", Severity.ERROR, null, null, title, null, null);

            SidebarBlock block2 = new SidebarBlock("sidebar1", Severity.ERROR, null, null, title, null, null);

            SidebarBlock block3 = new SidebarBlock("sidebar2", Severity.ERROR, null, null, title, null, null);

            // then
            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1, block3);
            assertNotEquals(block1, null);
            assertNotEquals(block1, new Object());
            assertEquals(block1, block1);
        }
    }
}
