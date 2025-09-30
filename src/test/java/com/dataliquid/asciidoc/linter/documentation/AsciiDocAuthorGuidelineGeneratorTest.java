package com.dataliquid.asciidoc.linter.documentation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

@DisplayName("AsciiDocAuthorGuidelineGenerator")
class AsciiDocAuthorGuidelineGeneratorTest {

    private AsciiDocAuthorGuidelineGenerator generator;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        generator = new AsciiDocAuthorGuidelineGenerator();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    @DisplayName("should return correct format")
    void shouldReturnCorrectFormat() {
        assertEquals(DocumentationFormat.ASCIIDOC, generator.getFormat());
    }

    @Test
    @DisplayName("should return correct name")
    void shouldReturnCorrectName() {
        assertEquals("AsciiDoc Author Guideline Generator", generator.getName());
    }

    @Nested
    @DisplayName("Generate Documentation")
    class GenerateDocumentation {

        @Test
        @DisplayName("should require non-null configuration")
        void shouldRequireNonNullConfiguration() {
            assertThrows(NullPointerException.class, () -> generator.generate(null, printWriter));
        }

        @Test
        @DisplayName("should require non-null writer")
        void shouldRequireNonNullWriter() {
            LinterConfiguration config = new LinterConfiguration(new DocumentConfiguration(null, null));

            assertThrows(NullPointerException.class, () -> generator.generate(config, null));
        }

        @Test
        @DisplayName("should generate basic documentation structure")
        void shouldGenerateBasicDocumentationStructure() {
            // Given
            LinterConfiguration config = new LinterConfiguration(new DocumentConfiguration(null, null));

            // When
            generator.generate(config, printWriter);
            printWriter.flush();
            String output = stringWriter.toString();

            // Then
            assertTrue(output.contains("= AsciiDoc Author Guidelines"));
            assertTrue(output.contains(":toc: left"));
            assertTrue(output.contains("== Introduction"));
            assertTrue(output.contains("== Validation Levels"));
        }

        @Test
        @DisplayName("should generate metadata documentation")
        void shouldGenerateMetadataDocumentation() {
            // Given
            AttributeConfig titleAttr = new AttributeConfig("title", null, true, 10, 100, null, Severity.ERROR);

            AttributeConfig authorAttr = new AttributeConfig("author", null, false, null, null, "^[A-Z].*",
                    Severity.WARN);

            MetadataConfiguration metadata = new MetadataConfiguration(List.of(titleAttr, authorAttr));

            DocumentConfiguration document = new DocumentConfiguration(metadata, null);

            LinterConfiguration config = new LinterConfiguration(document);

            // When
            generator.generate(config, printWriter);
            printWriter.flush();
            String output = stringWriter.toString();

            // Then
            assertTrue(output.contains("== Document Metadata"));
            assertTrue(output.contains("=== Required Attributes"));
            assertTrue(output.contains("=== Optional Attributes"));
            assertTrue(output.contains("|title"));
            assertTrue(output.contains("|author"));
            assertTrue(output.contains("Minimum length: 10 characters"));
            assertTrue(output.contains("Maximum length: 100 characters"));
        }

        @Test
        @DisplayName("should generate section documentation")
        void shouldGenerateSectionDocumentation() {
            // Given
            ParagraphBlock paragraph = ParagraphBlock
                    .builder()
                    .severity(Severity.WARN)
                    .occurrence(new OccurrenceConfig(null, 1, 3, null))
                    .build();

            SectionConfig section = SectionConfig
                    .builder()
                    .name("introduction")
                    .level(1)
                    .order(1)
                    .occurrence(new OccurrenceConfig(null, 1, 1, null))
                    .allowedBlocks(List.of(paragraph))
                    .build();

            DocumentConfiguration document = DocumentConfiguration.builder().sections(List.of(section)).build();

            LinterConfiguration config = new LinterConfiguration(document);

            // When
            generator.generate(config, printWriter);
            printWriter.flush();
            String output = stringWriter.toString();

            // Then
            assertTrue(output.contains("== Document Structure"));
            assertTrue(output.contains("=== Section: introduction"));
            assertTrue(output.contains("**Level**: 1"));
            assertTrue(output.contains("paragraph"));
        }
    }

    @Nested
    @DisplayName("Visualization Styles")
    class VisualizationStyles {

        @Test
        @DisplayName("should use default tree visualization")
        void shouldUseDefaultTreeVisualization() {
            // Given
            SectionConfig section = SectionConfig.builder().name("test").level(1).build();

            DocumentConfiguration document = DocumentConfiguration.builder().sections(List.of(section)).build();

            LinterConfiguration config = new LinterConfiguration(document);

            // When
            generator.generate(config, printWriter);
            printWriter.flush();
            String output = stringWriter.toString();

            // Then
            assertTrue(output.contains("[literal]"));
            assertTrue(output.contains("document/"));
        }

        @Test
        @DisplayName("should support multiple visualization styles")
        void shouldSupportMultipleVisualizationStyles() {
            // Given
            AsciiDocAuthorGuidelineGenerator multiStyleGenerator = new AsciiDocAuthorGuidelineGenerator(
                    Set.of(VisualizationStyle.TREE, VisualizationStyle.TABLE));

            SectionConfig section = SectionConfig.builder().name("test").level(1).build();

            DocumentConfiguration document = DocumentConfiguration.builder().sections(List.of(section)).build();

            LinterConfiguration config = new LinterConfiguration(document);

            // When
            assertDoesNotThrow(() -> {
                multiStyleGenerator.generate(config, printWriter);
                printWriter.flush();
            });

            String output = stringWriter.toString();

            // Then
            assertTrue(output.contains("ASCII-art tree structure"));
            assertTrue(output.contains("Hierarchical table with indentation"));
        }
    }
}
