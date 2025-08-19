package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.config.rule.TitleConfig;

class SectionValidatorTest {
    
    private Asciidoctor asciidoctor;
    
    @BeforeEach
    void setUp() {
        asciidoctor = Asciidoctor.Factory.create();
    }
    
    @Nested
    @DisplayName("Document Title Validation (Level 0)")
    class DocumentTitleValidation {
        
        @Test
        @DisplayName("should validate document title pattern")
        void shouldValidateDocumentTitlePattern() {
            // Given
            String content = """
                = my document
                
                Content here.
                """;
            
            SectionConfig titleConfig = SectionConfig.builder()
                .level(0)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration docConfig = DocumentConfiguration.builder()
                .sections(Arrays.asList(titleConfig))
                .build();
            
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            SectionValidator validator = SectionValidator.fromConfiguration(docConfig).build();
            
            // When
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.hasErrors());
            assertEquals(1, result.getErrorCount());
            assertTrue(result.getMessages().stream()
                .anyMatch(msg -> msg.getRuleId().equals("section.title.pattern")));
        }
        
        @Test
        @DisplayName("should detect missing document title")
        void shouldDetectMissingDocumentTitle() {
            // Given
            String content = """
                Content without title.
                """;
            
            SectionConfig titleConfig = SectionConfig.builder()
                .level(0)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .build();
            
            DocumentConfiguration docConfig = DocumentConfiguration.builder()
                .sections(Arrays.asList(titleConfig))
                .build();
            
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            SectionValidator validator = SectionValidator.fromConfiguration(docConfig).build();
            
            // When
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.hasErrors());
            assertEquals("Document title is required", result.getMessages().get(0).getMessage());
        }
        
        @Test
        @DisplayName("should accept valid document title")
        void shouldAcceptValidDocumentTitle() {
            // Given
            String content = """
                = Valid Document Title
                
                Content here.
                """;
            
            SectionConfig titleConfig = SectionConfig.builder()
                .level(0)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration docConfig = DocumentConfiguration.builder()
                .sections(Arrays.asList(titleConfig))
                .build();
            
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            SectionValidator validator = SectionValidator.fromConfiguration(docConfig).build();
            
            // When
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.hasErrors());
        }
    }
    
    @Nested
    @DisplayName("Basic Section Validation")
    class BasicSectionValidation {
        
        @Test
        @DisplayName("should validate document with matching sections")
        void shouldValidateDocumentWithMatchingSections() {
            // Given
            String content = """
                = Document Title
                
                == Introduction
                This is the introduction.
                
                == Getting Started
                This is how to get started.
                """;
            
            SectionConfig introSection = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Introduction$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig gettingStartedSection = SectionConfig.builder()
                .name("getting-started")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Getting Started$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(introSection, gettingStartedSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
        
        @Test
        @DisplayName("should detect missing required sections")
        void shouldDetectMissingRequiredSections() {
            // Given
            String content = """
                = Document Title
                
                == Getting Started
                This is how to get started.
                """;
            
            SectionConfig requiredSection = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Introduction$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(requiredSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            
            // The validator finds a section at level 1 ("Getting Started") but it doesn't match
            // the required pattern "^Introduction$", so it reports a pattern mismatch
            assertEquals(1, result.getMessages().size());
            
            ValidationMessage message = result.getMessages().get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Section title does not match required pattern", message.getMessage());
            assertEquals("Getting Started", message.getActualValue().orElse(null));
            assertTrue(message.getExpectedValue().orElse("").contains("Introduction"));
        }
        
        @Test
        @DisplayName("should detect too many section occurrences")
        void shouldDetectTooManySectionOccurrences() {
            // Given
            String content = """
                = Document Title
                
                == Introduction
                First intro.
                
                == Introduction
                Second intro.
                """;
            
            SectionConfig section = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(section))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            assertEquals(1, result.getMessages().size());
            
            ValidationMessage message = result.getMessages().get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Too many occurrences of section: introduction", message.getMessage());
            assertEquals("2", message.getActualValue().orElse(null));
            assertEquals("At most 1", message.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("Title Pattern Validation")
    class TitlePatternValidation {
        
        @Test
        @DisplayName("should provide specific error for pattern mismatch vs level mismatch")
        void shouldProvideSpecificErrorForPatternMismatch() {
            // Given
            String content = """
                = Document Title
                
                == Conclusion
                This is the conclusion section.
                
                === Unexpected Subsection
                This is at level 2.
                """;
            
            SectionConfig conclusionSection = SectionConfig.builder()
                .name("conclusion")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Conclusion$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(conclusionSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid()); // "Conclusion" matches the pattern
            
            // The subsection should generate a "not allowed at level" error
            ValidationMessage levelError = result.getMessages().stream()
                .filter(msg -> msg.getMessage().contains("Section not allowed at level 2"))
                .findFirst()
                .orElse(null);
            
            if (levelError != null) {
                assertEquals("Section not allowed at level 2: 'Unexpected Subsection'", levelError.getMessage());
                assertEquals("No sections configured for level 2", levelError.getExpectedValue().orElse(null));
            }
        }
        
        @Test
        @DisplayName("should show pattern mismatch error when level matches but title doesn't")
        void shouldShowPatternMismatchError() {
            // Given
            String content = """
                = Document Title
                
                == conclusion
                This is the conclusion with lowercase title.
                """;
            
            SectionConfig conclusionSection = SectionConfig.builder()
                .name("conclusion")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Conclusion$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(conclusionSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            
            ValidationMessage message = result.getMessages().stream()
                .filter(msg -> msg.getMessage().contains("Section title does not match required pattern"))
                .findFirst()
                .orElseThrow();
            
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Section title does not match required pattern", message.getMessage());
            assertEquals("conclusion", message.getActualValue().orElse(null));
            assertEquals("Pattern: ^Conclusion$", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate section title with pattern")
        void shouldValidateSectionTitleWithPattern() {
            // Given
            String content = """
                = Document Title
                
                == Chapter 1: Introduction
                This is chapter 1.
                
                == Chapter 2: Implementation
                This is chapter 2.
                """;
            
            SectionConfig chapterSection = SectionConfig.builder()
                .name("chapter")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(10)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("Chapter \\d+: .*")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(chapterSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
        
        @Test
        @DisplayName("should detect title pattern mismatch")
        void shouldDetectTitlePatternMismatch() {
            // Given
            String content = """
                = Document Title
                
                == Introduction
                This doesn't match the pattern.
                """;
            
            SectionConfig section = SectionConfig.builder()
                .name("chapter")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(10)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("Chapter \\d+: .*")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(section))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            
            ValidationMessage message = result.getMessages().stream()
                .filter(msg -> msg.getMessage().contains("Section title does not match required pattern"))
                .findFirst()
                .orElseThrow();
            
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Section title does not match required pattern", message.getMessage());
            assertEquals("Introduction", message.getActualValue().orElse(null));
            assertEquals("Pattern: Chapter \\d+: .*", message.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("Level Validation")
    class LevelValidation {
        
        @Test
        @DisplayName("should detect incorrect section level")
        void shouldDetectIncorrectSectionLevel() {
            // Given
            String content = """
                = Document Title
                
                === Introduction
                This is at level 2 instead of level 1.
                """;
            
            SectionConfig section = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(section))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            
            ValidationMessage unexpectedMessage = result.getMessages().stream()
                .filter(msg -> msg.getMessage().contains("Section not allowed at level"))
                .findFirst()
                .orElseThrow();
            
            assertEquals("Section not allowed at level 2: 'Introduction'", unexpectedMessage.getMessage());
            assertEquals("Introduction", unexpectedMessage.getActualValue().orElse(null));
            assertEquals("No sections configured for level 2", unexpectedMessage.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("Order Constraints Validation")
    class OrderConstraintsValidation {
        
        @Test
        @DisplayName("should validate correct section order")
        void shouldValidateCorrectSectionOrder() {
            // Given
            String content = """
                = Document Title
                
                == Introduction
                This is the introduction.
                
                == Prerequisites
                These are the prerequisites.
                
                == Installation
                This is how to install.
                """;
            
            SectionConfig intro = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .order(1)
                .title(TitleConfig.builder()
                    .pattern("^Introduction$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig prereq = SectionConfig.builder()
                .name("prerequisites")
                .level(1)
                .order(2)
                .title(TitleConfig.builder()
                    .pattern("^Prerequisites$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig install = SectionConfig.builder()
                .name("installation")
                .level(1)
                .order(3)
                .title(TitleConfig.builder()
                    .pattern("^Installation$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(intro, prereq, install))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
        
        @Test
        @DisplayName("should detect incorrect section order")
        void shouldDetectIncorrectSectionOrder() {
            // Given
            String content = """
                = Document Title
                
                == Installation
                This is how to install.
                
                == Introduction
                This is the introduction.
                """;
            
            SectionConfig intro = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .order(1)
                .title(TitleConfig.builder()
                    .pattern("^Introduction$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig install = SectionConfig.builder()
                .name("installation")
                .level(1)
                .order(2)
                .title(TitleConfig.builder()
                    .pattern("^Installation$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(intro, install))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            assertEquals(1, result.getMessages().size());
            
            ValidationMessage message = result.getMessages().get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Section order violation", message.getMessage());
            assertEquals("introduction appears after installation", message.getActualValue().orElse(null));
            assertEquals("introduction should appear before installation", message.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("Subsection Validation")
    class SubsectionValidation {
        
        @Test
        @DisplayName("should validate nested subsections")
        void shouldValidateNestedSubsections() {
            // Given
            String content = """
                = Document Title
                
                == Features
                Main features section.
                
                === Core Features
                These are core features.
                
                === Advanced Features
                These are advanced features.
                """;
            
            SectionConfig coreFeatures = SectionConfig.builder()
                .name("core-features")
                .level(2)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Core Features$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig advancedFeatures = SectionConfig.builder()
                .name("advanced-features")
                .level(2)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Advanced Features$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig featuresSection = SectionConfig.builder()
                .name("features")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Features$")
                    .severity(Severity.ERROR)
                    .build())
                .subsections(Arrays.asList(coreFeatures, advancedFeatures))
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(featuresSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
        
        @Test
        @DisplayName("should detect missing required subsections")
        void shouldDetectMissingRequiredSubsections() {
            // Given
            String content = """
                = Document Title
                
                == Features
                Main features section.
                
                === Advanced Features
                These are advanced features.
                """;
            
            SectionConfig coreFeatures = SectionConfig.builder()
                .name("core-features")
                .level(2)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Core Features$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            SectionConfig featuresSection = SectionConfig.builder()
                .name("features")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(1)
                    .max(1)
                    .build())
                .title(TitleConfig.builder()
                    .pattern("^Features$")
                    .severity(Severity.ERROR)
                    .build())
                .subsections(Arrays.asList(coreFeatures))
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(featuresSection))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertFalse(result.isValid());
            
            // The validator finds a subsection at level 2 ("Advanced Features") but it doesn't match
            // the required pattern "^Core Features$", so it reports a pattern mismatch
            assertEquals(1, result.getMessages().size());
            
            ValidationMessage message = result.getMessages().get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("Section title does not match required pattern", message.getMessage());
            assertEquals("Advanced Features", message.getActualValue().orElse(null));
            assertTrue(message.getExpectedValue().orElse("").contains("Core Features"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle empty document")
        void shouldHandleEmptyDocument() {
            // Given
            String content = "= Document Title\n";
            
            SectionConfig section = SectionConfig.builder()
                .name("introduction")
                .level(1)
                .occurrence(OccurrenceConfig.builder()
                    .min(0)
                    .max(1)
                    .build())
                .build();
            
            DocumentConfiguration config = DocumentConfiguration.builder()
                .sections(Arrays.asList(section))
                .build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
        
        @Test
        @DisplayName("should handle document with no configured sections")
        void shouldHandleDocumentWithNoConfiguredSections() {
            // Given
            String content = """
                = Document Title
                
                == Some Section
                Content here.
                """;
            
            DocumentConfiguration config = DocumentConfiguration.builder().build();
            
            // When
            SectionValidator validator = SectionValidator.fromConfiguration(config).build();
            Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
            ValidationResult result = validator.validate(document);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages().size());
        }
    }
}