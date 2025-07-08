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

import com.dataliquid.asciidoc.linter.config.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

@DisplayName("AsciiDocRuleGenerator")
class AsciiDocRuleGeneratorTest {
    
    private AsciiDocRuleGenerator generator;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() {
        generator = new AsciiDocRuleGenerator();
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
        assertEquals("AsciiDoc Rule Documentation Generator", generator.getName());
    }
    
    @Nested
    @DisplayName("Generate Documentation")
    class GenerateDocumentation {
        
        @Test
        @DisplayName("should require non-null configuration")
        void shouldRequireNonNullConfiguration() {
            assertThrows(NullPointerException.class, () -> 
                generator.generate(null, printWriter)
            );
        }
        
        @Test
        @DisplayName("should require non-null writer")
        void shouldRequireNonNullWriter() {
            LinterConfiguration config = LinterConfiguration.builder()
                .document(DocumentConfiguration.builder().build())
                .build();
            
            assertThrows(NullPointerException.class, () -> 
                generator.generate(config, null)
            );
        }
        
        @Test
        @DisplayName("should generate basic documentation structure")
        void shouldGenerateBasicDocumentationStructure() {
            // Given
            LinterConfiguration config = LinterConfiguration.builder()
                .document(DocumentConfiguration.builder().build())
                .build();
            
            // When
            generator.generate(config, printWriter);
            printWriter.flush();
            String output = stringWriter.toString();
            
            // Then
            assertTrue(output.contains("= AsciiDoc Document Guidelines"));
            assertTrue(output.contains(":toc: left"));
            assertTrue(output.contains("== Introduction"));
            assertTrue(output.contains("== Validation Levels"));
            assertTrue(output.contains("== Tips for Authors"));
        }
        
        @Test
        @DisplayName("should generate metadata documentation")
        void shouldGenerateMetadataDocumentation() {
            // Given
            AttributeConfig titleAttr = AttributeConfig.builder()
                .name("title")
                .required(true)
                .minLength(10)
                .maxLength(100)
                .severity(Severity.ERROR)
                .build();
            
            AttributeConfig authorAttr = AttributeConfig.builder()
                .name("author")
                .required(false)
                .pattern("^[A-Z].*")
                .severity(Severity.WARN)
                .build();
            
            MetadataConfiguration metadata = MetadataConfiguration.builder()
                .attributes(List.of(titleAttr, authorAttr))
                .build();
            
            DocumentConfiguration document = DocumentConfiguration.builder()
                .metadata(metadata)
                .build();
            
            LinterConfiguration config = LinterConfiguration.builder()
                .document(document)
                .build();
            
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
            ParagraphBlock paragraph = ParagraphBlock.builder()
                .severity(Severity.WARN)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(3)
                    .build())
                .build();
            
            SectionConfig section = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .order(1)
                .min(1)
                .max(1)
                .allowedBlocks(List.of(paragraph))
                .build();
            
            DocumentConfiguration document = DocumentConfiguration.builder()
                .sections(List.of(section))
                .build();
            
            LinterConfiguration config = LinterConfiguration.builder()
                .document(document)
                .build();
            
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
            SectionConfig section = SectionConfig.builder()
                .name("test")
                .level(1)
                .build();
            
            DocumentConfiguration document = DocumentConfiguration.builder()
                .sections(List.of(section))
                .build();
            
            LinterConfiguration config = LinterConfiguration.builder()
                .document(document)
                .build();
            
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
            AsciiDocRuleGenerator multiStyleGenerator = new AsciiDocRuleGenerator(
                Set.of(VisualizationStyle.TREE, VisualizationStyle.TABLE)
            );
            
            SectionConfig section = SectionConfig.builder()
                .name("test")
                .level(1)
                .build();
            
            DocumentConfiguration document = DocumentConfiguration.builder()
                .sections(List.of(section))
                .build();
            
            LinterConfiguration config = LinterConfiguration.builder()
                .document(document)
                .build();
            
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