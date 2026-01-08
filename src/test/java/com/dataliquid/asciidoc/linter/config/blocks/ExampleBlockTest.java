package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("ExampleBlock")
class ExampleBlockTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("should build with minimal configuration")
        void shouldBuildWithMinimalConfiguration() {
            // Given/When
            ExampleBlock block = new ExampleBlock("example-1", Severity.WARN, null, null, null, null);

            // Then
            assertNotNull(block);
            assertEquals("example-1", block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
            assertNull(block.getCaption());
            assertNull(block.getCollapsible());
        }

        @Test
        @DisplayName("should build with full configuration")
        void shouldBuildWithFullConfiguration() {
            // Given
            ExampleBlock.CaptionConfig caption = new ExampleBlock.CaptionConfig(true, "^Example \\d+:.*", 10, 100,
                    Severity.ERROR);

            ExampleBlock.CollapsibleConfig collapsible = new ExampleBlock.CollapsibleConfig(false,
                    Arrays.asList(true, false), Severity.INFO);

            // When
            ExampleBlock block = new ExampleBlock("example-full", Severity.WARN, null, null, caption, collapsible);

            // Then
            assertNotNull(block);
            assertEquals("example-full", block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
            assertNotNull(block.getCaption());
            assertNotNull(block.getCollapsible());
        }
    }

    @Nested
    @DisplayName("CaptionConfig")
    class CaptionConfigTest {

        @Test
        @DisplayName("should build with all properties")
        void shouldBuildWithAllProperties() {
            // Given/When
            ExampleBlock.CaptionConfig config = new ExampleBlock.CaptionConfig(true,
                    "^(Example|Beispiel)\\s+\\d+\\.\\d*:.*", 15, 100, Severity.ERROR);

            // Then
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals("^(Example|Beispiel)\\s+\\d+\\.\\d*:.*", config.getPattern().pattern());
            assertEquals(15, config.getMinLength());
            assertEquals(100, config.getMaxLength());
            assertEquals(Severity.ERROR, config.getSeverity());
        }

        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // Given/When
            ExampleBlock.CaptionConfig config = new ExampleBlock.CaptionConfig(false, null, null, null, null);

            // Then
            assertNull(config.getPattern());
        }

        @Test
        @DisplayName("should have correct equals and hashCode")
        void shouldHaveCorrectEqualsAndHashCode() {
            // Given
            ExampleBlock.CaptionConfig config1 = new ExampleBlock.CaptionConfig(true, "^Example.*", 10, 50,
                    Severity.WARN);

            ExampleBlock.CaptionConfig config2 = new ExampleBlock.CaptionConfig(true, "^Example.*", 10, 50,
                    Severity.WARN);

            ExampleBlock.CaptionConfig config3 = new ExampleBlock.CaptionConfig(false, "^Example.*", 10, 50,
                    Severity.WARN);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }

        @Test
        @DisplayName("should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            ExampleBlock.CaptionConfig config = new ExampleBlock.CaptionConfig(true, "^Example.*", 10, 50,
                    Severity.ERROR);

            // When
            String result = config.toString();

            // Then
            assertTrue(result.contains("required=true"));
            assertTrue(result.contains("pattern=^Example.*"));
            assertTrue(result.contains("minLength=10"));
            assertTrue(result.contains("maxLength=50"));
            assertTrue(result.contains("severity=ERROR"));
        }
    }

    @Nested
    @DisplayName("CollapsibleConfig")
    class CollapsibleConfigTest {

        @Test
        @DisplayName("should build with all properties")
        void shouldBuildWithAllProperties() {
            // Given
            List<Boolean> allowed = Arrays.asList(true, false);

            // When
            ExampleBlock.CollapsibleConfig config = new ExampleBlock.CollapsibleConfig(false, allowed, Severity.INFO);

            // Then
            assertFalse(config.isRequired());
            assertEquals(allowed, config.getAllowed());
            assertEquals(Severity.INFO, config.getSeverity());
        }

        @Test
        @DisplayName("should have correct equals and hashCode")
        void shouldHaveCorrectEqualsAndHashCode() {
            // Given
            List<Boolean> allowed = Arrays.asList(true, false);

            ExampleBlock.CollapsibleConfig config1 = new ExampleBlock.CollapsibleConfig(false, allowed, Severity.INFO);

            ExampleBlock.CollapsibleConfig config2 = new ExampleBlock.CollapsibleConfig(false, allowed, Severity.INFO);

            ExampleBlock.CollapsibleConfig config3 = new ExampleBlock.CollapsibleConfig(true, allowed, Severity.INFO);

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }

        @Test
        @DisplayName("should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            List<Boolean> allowed = Arrays.asList(true, false);
            ExampleBlock.CollapsibleConfig config = new ExampleBlock.CollapsibleConfig(false, allowed, Severity.INFO);

            // When
            String result = config.toString();

            // Then
            assertTrue(result.contains("required=false"));
            assertTrue(result.contains("allowed=[true, false]"));
            assertTrue(result.contains("severity=INFO"));
        }
    }

    @Test
    @DisplayName("should have correct equals and hashCode")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        ExampleBlock.CaptionConfig caption = new ExampleBlock.CaptionConfig(true, "^Example.*", null, null, null);

        ExampleBlock block1 = new ExampleBlock("example-1", Severity.WARN, null, null, caption, null);

        ExampleBlock block2 = new ExampleBlock("example-1", Severity.WARN, null, null, caption, null);

        ExampleBlock block3 = new ExampleBlock("example-2", Severity.WARN, null, null, caption, null);

        // Then
        assertEquals(block1, block2);
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, block3);
    }

    @Test
    @DisplayName("should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        // Given
        ExampleBlock.CaptionConfig caption = new ExampleBlock.CaptionConfig(true, "^Example.*", null, null,
                Severity.ERROR);

        ExampleBlock.CollapsibleConfig collapsible = new ExampleBlock.CollapsibleConfig(false,
                Arrays.asList(true, false), Severity.INFO);

        ExampleBlock block = new ExampleBlock("example-test", Severity.WARN, null, null, caption, collapsible);

        // When
        String result = block.toString();

        // Then
        assertTrue(result.contains("name='example-test'"));
        assertTrue(result.contains("severity=WARN"));
        assertTrue(result.contains("caption="));
        assertTrue(result.contains("collapsible="));
    }
}
