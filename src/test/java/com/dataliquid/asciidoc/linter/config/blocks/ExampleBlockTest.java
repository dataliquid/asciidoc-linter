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
            ExampleBlock block = ExampleBlock.builder().name("example-1").severity(Severity.WARN).build();

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
            ExampleBlock.CaptionConfig caption = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^Example \\d+:.*").minLength(10).maxLength(100).severity(Severity.ERROR).build();

            ExampleBlock.CollapsibleConfig collapsible = ExampleBlock.CollapsibleConfig.builder().required(false)
                    .allowed(Arrays.asList(true, false)).severity(Severity.INFO).build();

            // When
            ExampleBlock block = ExampleBlock.builder().name("example-full").severity(Severity.WARN).caption(caption)
                    .collapsible(collapsible).build();

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
            ExampleBlock.CaptionConfig config = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^(Example|Beispiel)\\s+\\d+\\.\\d*:.*").minLength(15).maxLength(100)
                    .severity(Severity.ERROR).build();

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
            ExampleBlock.CaptionConfig config = ExampleBlock.CaptionConfig.builder().required(false).pattern(null)
                    .build();

            // Then
            assertNull(config.getPattern());
        }

        @Test
        @DisplayName("should have correct equals and hashCode")
        void shouldHaveCorrectEqualsAndHashCode() {
            // Given
            ExampleBlock.CaptionConfig config1 = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^Example.*").minLength(10).maxLength(50).severity(Severity.WARN).build();

            ExampleBlock.CaptionConfig config2 = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^Example.*").minLength(10).maxLength(50).severity(Severity.WARN).build();

            ExampleBlock.CaptionConfig config3 = ExampleBlock.CaptionConfig.builder().required(false)
                    .pattern("^Example.*").minLength(10).maxLength(50).severity(Severity.WARN).build();

            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }

        @Test
        @DisplayName("should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            ExampleBlock.CaptionConfig config = ExampleBlock.CaptionConfig.builder().required(true)
                    .pattern("^Example.*").minLength(10).maxLength(50).severity(Severity.ERROR).build();

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
            ExampleBlock.CollapsibleConfig config = ExampleBlock.CollapsibleConfig.builder().required(false)
                    .allowed(allowed).severity(Severity.INFO).build();

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

            ExampleBlock.CollapsibleConfig config1 = ExampleBlock.CollapsibleConfig.builder().required(false)
                    .allowed(allowed).severity(Severity.INFO).build();

            ExampleBlock.CollapsibleConfig config2 = ExampleBlock.CollapsibleConfig.builder().required(false)
                    .allowed(allowed).severity(Severity.INFO).build();

            ExampleBlock.CollapsibleConfig config3 = ExampleBlock.CollapsibleConfig.builder().required(true)
                    .allowed(allowed).severity(Severity.INFO).build();

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
            ExampleBlock.CollapsibleConfig config = ExampleBlock.CollapsibleConfig.builder().required(false)
                    .allowed(allowed).severity(Severity.INFO).build();

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
        ExampleBlock.CaptionConfig caption = ExampleBlock.CaptionConfig.builder().required(true).pattern("^Example.*")
                .build();

        ExampleBlock block1 = ExampleBlock.builder().name("example-1").severity(Severity.WARN).caption(caption).build();

        ExampleBlock block2 = ExampleBlock.builder().name("example-1").severity(Severity.WARN).caption(caption).build();

        ExampleBlock block3 = ExampleBlock.builder().name("example-2").severity(Severity.WARN).caption(caption).build();

        // Then
        assertEquals(block1, block2);
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, block3);
    }

    @Test
    @DisplayName("should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        // Given
        ExampleBlock.CaptionConfig caption = ExampleBlock.CaptionConfig.builder().required(true).pattern("^Example.*")
                .severity(Severity.ERROR).build();

        ExampleBlock.CollapsibleConfig collapsible = ExampleBlock.CollapsibleConfig.builder().required(false)
                .allowed(Arrays.asList(true, false)).severity(Severity.INFO).build();

        ExampleBlock block = ExampleBlock.builder().name("example-test").severity(Severity.WARN).caption(caption)
                .collapsible(collapsible).build();

        // When
        String result = block.toString();

        // Then
        assertTrue(result.contains("name='example-test'"));
        assertTrue(result.contains("severity=WARN"));
        assertTrue(result.contains("caption="));
        assertTrue(result.contains("collapsible="));
    }
}
