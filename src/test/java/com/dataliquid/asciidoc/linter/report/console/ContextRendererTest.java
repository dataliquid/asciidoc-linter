package com.dataliquid.asciidoc.linter.report.console;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("ContextRenderer")
class ContextRendererTest {

    private ContextRenderer renderer;
    private DisplayConfig displayConfig;

    @BeforeEach
    void setUp() {
        displayConfig = DisplayConfig
                .builder()
                .contextLines(2)
                .useColors(true)
                .showLineNumbers(true)
                .maxLineWidth(120)
                .showHeader(true)
                .build();
        renderer = new ContextRenderer(displayConfig);
    }

    @Nested
    @DisplayName("getContext")
    class GetContext {

        @Test
        @DisplayName("should handle short files without throwing exception")
        void shouldHandleShortFiles() {
            // Given
            SourceLocation location = SourceLocation
                    .builder()
                    .filename("test.adoc")
                    .startLine(8)
                    .endLine(8)
                    .startColumn(1)
                    .endColumn(1)
                    .build();

            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test.rule")
                    .message("Test error")
                    .location(location)
                    .contextLines(List.of("Line 1", "Line 2", "Line 3")) // Short
                                                                         // file
                                                                         // with
                                                                         // only
                                                                         // 3
                                                                         // lines
                    .build();

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                SourceContext context = renderer.getContext(message);
                assertNotNull(context);
            });
        }

        @Test
        @DisplayName("should use provided context lines when available")
        void shouldUseProvidedContextLines() {
            // Given
            List<String> providedLines = List.of("Line 1", "Line 2", "Line 3");
            SourceLocation location = SourceLocation
                    .builder()
                    .filename("test.adoc")
                    .startLine(2)
                    .endLine(2)
                    .startColumn(1)
                    .endColumn(10)
                    .build();

            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test.rule")
                    .message("Test error")
                    .location(location)
                    .contextLines(providedLines)
                    .build();

            // When
            SourceContext context = renderer.getContext(message);

            // Then
            assertNotNull(context.getLines());
            assertEquals(3, context.getLines().size());
            assertEquals(location, context.getErrorLocation());
        }

        @Test
        @DisplayName("should handle empty file content")
        void shouldHandleEmptyFileContent() {
            // Given
            SourceLocation location = SourceLocation
                    .builder()
                    .filename("empty.adoc")
                    .startLine(1)
                    .endLine(1)
                    .startColumn(1)
                    .endColumn(1)
                    .build();

            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test.rule")
                    .message("Test error")
                    .location(location)
                    .contextLines(List.of()) // Empty context
                    .build();

            // When
            SourceContext context = renderer.getContext(message);

            // Then
            assertTrue(context.getLines().isEmpty());
            assertEquals(location, context.getErrorLocation());
        }

        @Test
        @DisplayName("should handle location at end of file")
        void shouldHandleLocationAtEndOfFile() {
            // Given
            List<String> lines = List.of("Line 1", "Line 2", "Line 3", "Line 4", "Line 5");
            SourceLocation location = SourceLocation
                    .builder()
                    .filename("test.adoc")
                    .startLine(5)
                    .endLine(5)
                    .startColumn(1)
                    .endColumn(10)
                    .build();

            ValidationMessage message = ValidationMessage
                    .builder()
                    .severity(Severity.ERROR)
                    .ruleId("test.rule")
                    .message("Test error")
                    .location(location)
                    .contextLines(lines)
                    .build();

            // When
            SourceContext context = renderer.getContext(message);

            // Then
            assertNotNull(context.getLines());
            assertEquals(5, context.getLines().size());
            assertEquals(location, context.getErrorLocation());
        }
    }

    @Test
    @DisplayName("should clear cache")
    void shouldClearCache() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> renderer.clearCache());
    }
}
