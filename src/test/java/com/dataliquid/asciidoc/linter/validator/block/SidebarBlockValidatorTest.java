package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("SidebarBlockValidator")
class SidebarBlockValidatorTest {

    private SidebarBlockValidator validator;
    private BlockValidationContext context;

    @Mock
    private StructuralNode node;

    @Mock
    private Section section;

    @Mock
    private Cursor sourceLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new SidebarBlockValidator();
        context = new BlockValidationContext(section, "test.adoc");
    }

    @Test
    @DisplayName("should support SIDEBAR block type")
    void shouldSupportSidebarBlockType() {
        assertEquals(BlockType.SIDEBAR, validator.getSupportedType());
    }

    @Nested
    @DisplayName("Title validation")
    class TitleValidation {

        @Test
        @DisplayName("should pass when title is not configured")
        void shouldPassWhenTitleNotConfigured() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should fail when required title is missing")
        void shouldFailWhenRequiredTitleMissing() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .title(SidebarBlock.TitleConfig.builder().required(true).build()).build();

            when(node.getTitle()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertEquals("Sidebar block requires a title", results.get(0).getMessage());
            assertEquals(10, results.get(0).getLocation().getStartLine());
        }

        @Test
        @DisplayName("should use title severity when specified")
        void shouldUseTitleSeverityWhenSpecified() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .title(SidebarBlock.TitleConfig.builder().required(true).severity(Severity.WARN).build()).build();

            when(node.getTitle()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
        }

        @Test
        @DisplayName("should validate title length")
        void shouldValidateTitleLength() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .title(SidebarBlock.TitleConfig.builder().minLength(5).maxLength(10).build()).build();

            when(node.getTitle()).thenReturn("abc");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too short"));
            assertEquals("3 characters", results.get(0).getActualValue().get());
        }

        @Test
        @DisplayName("should validate title pattern")
        void shouldValidateTitlePattern() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .title(SidebarBlock.TitleConfig.builder().pattern("^[A-Z].*$").build()).build();

            when(node.getTitle()).thenReturn("lowercase");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("does not match required pattern"));
        }

        @Test
        @DisplayName("should pass valid title")
        void shouldPassValidTitle() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .title(SidebarBlock.TitleConfig.builder().required(true).minLength(5).maxLength(50)
                            .pattern("^[A-Z].*$").build())
                    .build();

            when(node.getTitle()).thenReturn("Valid Title");

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Content validation")
    class ContentValidation {

        @Test
        @DisplayName("should pass when content is not configured")
        void shouldPassWhenContentNotConfigured() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should fail when required content is missing")
        void shouldFailWhenRequiredContentMissing() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .content(SidebarBlock.ContentConfig.builder().required(true).build()).build();

            when(node.getContent()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals("Sidebar block requires content", results.get(0).getMessage());
        }

        @Test
        @DisplayName("should validate content length")
        void shouldValidateContentLength() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .content(SidebarBlock.ContentConfig.builder().minLength(50).maxLength(100).build()).build();

            when(node.getContent()).thenReturn("Short content");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too short"));
        }

        @Test
        @DisplayName("should validate line count")
        void shouldValidateLineCount() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .content(SidebarBlock.ContentConfig.builder()
                            .lines(SidebarBlock.LinesConfig.builder().min(3).max(5).severity(Severity.WARN).build())
                            .build())
                    .build();

            when(node.getContent()).thenReturn("Line 1");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("too few lines"));
        }

        @Test
        @DisplayName("should pass valid content")
        void shouldPassValidContent() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .content(SidebarBlock.ContentConfig.builder().required(true).minLength(10).maxLength(100)
                            .lines(SidebarBlock.LinesConfig.builder().min(1).max(5).build()).build())
                    .build();

            when(node.getContent()).thenReturn("This is valid sidebar content\nWith multiple lines");

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Position validation")
    class PositionValidation {

        @Test
        @DisplayName("should pass when position is not configured")
        void shouldPassWhenPositionNotConfigured() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR).build();

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should fail when required position is missing")
        void shouldFailWhenRequiredPositionMissing() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .position(SidebarBlock.PositionConfig.builder().required(true).build()).build();

            when(node.getAttribute("position")).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals("Sidebar block requires a position attribute", results.get(0).getMessage());
        }

        @Test
        @DisplayName("should validate allowed positions")
        void shouldValidateAllowedPositions() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .position(SidebarBlock.PositionConfig.builder().allowed(List.of("left", "right", "float"))
                            .severity(Severity.INFO).build())
                    .build();

            when(node.getAttribute("position")).thenReturn("center");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertEquals(1, results.size());
            assertEquals(Severity.INFO, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("Invalid sidebar position"));
            assertEquals("center", results.get(0).getActualValue().get());
        }

        @Test
        @DisplayName("should pass valid position")
        void shouldPassValidPosition() {
            // Given
            SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                    .position(SidebarBlock.PositionConfig.builder().required(true)
                            .allowed(List.of("left", "right", "float")).build())
                    .build();

            when(node.getAttribute("position")).thenReturn("left");

            // When
            List<ValidationMessage> results = validator.validate(node, config, context);

            // Then
            assertTrue(results.isEmpty());
        }
    }

    @Test
    @DisplayName("should handle null node gracefully")
    void shouldHandleNullNodeGracefully() {
        // Given
        SidebarBlock config = SidebarBlock.builder().name("test").severity(Severity.ERROR)
                .title(SidebarBlock.TitleConfig.builder().required(true).build())
                .content(SidebarBlock.ContentConfig.builder().required(true).build())
                .position(SidebarBlock.PositionConfig.builder().required(true).build()).build();

        when(node.getTitle()).thenReturn(null);
        when(node.getContent()).thenReturn(null);
        when(node.getAttribute("position")).thenReturn(null);
        when(node.getSourceLocation()).thenReturn(null);

        // When
        List<ValidationMessage> results = validator.validate(node, config, context);

        // Then
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> r.getLocation().getStartLine() == 1));
    }
}
