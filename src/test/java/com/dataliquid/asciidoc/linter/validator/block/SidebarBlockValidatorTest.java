package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

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
    void shouldSupportSidebarBlockType() {
        // given, when, then
        assertEquals(BlockType.SIDEBAR, validator.getSupportedType());
    }

    @Nested
    class TitleValidation {

        @Test
        void shouldPassWhenTitleNotConfigured() {
            // given
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, null);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }

        @Test
        void shouldFailWhenRequiredTitleMissing() {
            // given
            SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(true, null, null, null, null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, null, null);

            when(node.getTitle()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals(Severity.ERROR, results.get(0).getSeverity());
            assertEquals("Sidebar block requires a title", results.get(0).getMessage());
            assertEquals(10, results.get(0).getLocation().getStartLine());
        }

        @Test
        void shouldUseTitleSeverityWhenSpecified() {
            // given
            SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(true, null, null, null, Severity.WARN);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, null, null);

            when(node.getTitle()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
        }

        @Test
        void shouldValidateTitleLength() {
            // given
            SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(false, 5, 10, null, null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, null, null);

            when(node.getTitle()).thenReturn("abc");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too short"));
            assertEquals("3 characters", results.get(0).getActualValue().get());
        }

        @Test
        void shouldValidateTitlePattern() {
            // given
            SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(false, null, null, "^[A-Z].*$", null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, null, null);

            when(node.getTitle()).thenReturn("lowercase");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("does not match required pattern"));
        }

        @Test
        void shouldPassValidTitle() {
            // given
            SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(true, 5, 50, "^[A-Z].*$", null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, null, null);

            when(node.getTitle()).thenReturn("Valid Title");

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class ContentValidation {

        @Test
        void shouldPassWhenContentNotConfigured() {
            // given
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, null);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }

        @Test
        void shouldFailWhenRequiredContentMissing() {
            // given
            SidebarBlock.ContentConfig contentConfig = new SidebarBlock.ContentConfig(true, null, null, null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, contentConfig, null);

            when(node.getContent()).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals("Sidebar block requires content", results.get(0).getMessage());
        }

        @Test
        void shouldValidateContentLength() {
            // given
            SidebarBlock.ContentConfig contentConfig = new SidebarBlock.ContentConfig(false, 50, 100, null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, contentConfig, null);

            when(node.getContent()).thenReturn("Short content");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertTrue(results.get(0).getMessage().contains("too short"));
        }

        @Test
        void shouldValidateLineCount() {
            // given
            SidebarBlock.LinesConfig linesConfig = new SidebarBlock.LinesConfig(3, 5, Severity.WARN);
            SidebarBlock.ContentConfig contentConfig = new SidebarBlock.ContentConfig(false, null, null, linesConfig);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, contentConfig, null);

            when(node.getContent()).thenReturn("Line 1");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals(Severity.WARN, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("too few lines"));
        }

        @Test
        void shouldPassValidContent() {
            // given
            SidebarBlock.LinesConfig linesConfig = new SidebarBlock.LinesConfig(1, 5, null);
            SidebarBlock.ContentConfig contentConfig = new SidebarBlock.ContentConfig(true, 10, 100, linesConfig);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, contentConfig, null);

            when(node.getContent()).thenReturn("This is valid sidebar content\nWith multiple lines");

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class PositionValidation {

        @Test
        void shouldPassWhenPositionNotConfigured() {
            // given
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, null);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }

        @Test
        void shouldFailWhenRequiredPositionMissing() {
            // given
            SidebarBlock.PositionConfig positionConfig = new SidebarBlock.PositionConfig(true, null, null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, positionConfig);

            when(node.getAttribute("position")).thenReturn(null);
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals("Sidebar block requires a position attribute", results.get(0).getMessage());
        }

        @Test
        void shouldValidateAllowedPositions() {
            // given
            SidebarBlock.PositionConfig positionConfig = new SidebarBlock.PositionConfig(false,
                    List.of("left", "right", "float"), Severity.INFO);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, positionConfig);

            when(node.getAttribute("position")).thenReturn("center");
            when(node.getSourceLocation()).thenReturn(sourceLocation);
            when(sourceLocation.getLineNumber()).thenReturn(10);

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertEquals(1, results.size());
            assertEquals(Severity.INFO, results.get(0).getSeverity());
            assertTrue(results.get(0).getMessage().contains("Invalid sidebar position"));
            assertEquals("center", results.get(0).getActualValue().get());
        }

        @Test
        void shouldPassValidPosition() {
            // given
            SidebarBlock.PositionConfig positionConfig = new SidebarBlock.PositionConfig(true,
                    List.of("left", "right", "float"), null);
            SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, null, null, positionConfig);

            when(node.getAttribute("position")).thenReturn("left");

            // when
            List<ValidationMessage> results = validator.validate(node, config, context);

            // then
            assertTrue(results.isEmpty());
        }
    }

    @Test
    void shouldHandleNullNodeGracefully() {
        // given
        SidebarBlock.TitleConfig titleConfig = new SidebarBlock.TitleConfig(true, null, null, null, null);
        SidebarBlock.ContentConfig contentConfig = new SidebarBlock.ContentConfig(true, null, null, null);
        SidebarBlock.PositionConfig positionConfig = new SidebarBlock.PositionConfig(true, null, null);
        SidebarBlock config = new SidebarBlock("test", Severity.ERROR, null, null, titleConfig, contentConfig,
                positionConfig);

        when(node.getTitle()).thenReturn(null);
        when(node.getContent()).thenReturn(null);
        when(node.getAttribute("position")).thenReturn(null);
        when(node.getSourceLocation()).thenReturn(null);

        // when
        List<ValidationMessage> results = validator.validate(node, config, context);

        // then
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> r.getLocation().getStartLine() == 1));
    }
}
