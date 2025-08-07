package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;

@DisplayName("MetadataValidator Unit Test")
class MetadataValidatorTest {

    private MetadataConfiguration testConfig;

    @BeforeEach
    void setUp() {
        testConfig = MetadataConfiguration.builder()
            .attributes(Arrays.asList(
                AttributeConfig.builder()
                    .name("author")
                    .required(true)
                    .severity(Severity.ERROR)
                    .build()
            ))
            .build();
    }

    @Test
    @DisplayName("should build validator from configuration")
    void shouldBuildValidatorWhenGivenConfiguration() {
        // Given
        // testConfig is already set up in @BeforeEach
        
        // When
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        
        // Then
        assertNotNull(validator);
    }

    @Test
    @DisplayName("should validate document with valid metadata")
    void shouldPassValidationWhenDocumentHasValidMetadata() {
        // Given
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        String content = """
            = Test Document
            :author: Test Author
            
            Content here.
            """;
        Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertNotNull(result);
        assertFalse(result.hasErrors());
    }

    @Test
    @DisplayName("should detect missing author")
    void shouldReportErrorWhenAuthorIsMissing() throws IOException {
        // Given
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        String content = """
            = Document Title
            
            Document without author.
            """;
        Path tempFile = Files.createTempFile("test", ".adoc");
        Files.writeString(tempFile, content);
        Document document = asciidoctor.loadFile(tempFile.toFile(), Options.builder().sourcemap(true).toFile(false).build());
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().equals("Missing required attribute 'author'")));
        
        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("should properly extract line numbers")
    void shouldIncludeLineNumbersWhenReportingValidationMessages() {
        // Given
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        String content = """
            = Test Title
            :author: John Doe
            :version: 1.0
            
            Content.
            """;
        Document document = asciidoctor.load(content, Options.builder().sourcemap(true).toFile(false).build());
        MetadataConfiguration config = MetadataConfiguration.builder()
            .attributes(Arrays.asList(
                AttributeConfig.builder()
                    .name("author")
                    .order(1)
                    .severity(Severity.ERROR)
                    .build(),
                AttributeConfig.builder()
                    .name("version")
                    .order(2)
                    .severity(Severity.ERROR)
                    .build()
            ))
            .build();
        MetadataValidator validator = MetadataValidator.fromConfiguration(config).build();
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertNotNull(result);
        result.getMessages().forEach(msg -> {
            assertNotNull(msg.getLocation());
            assertTrue(msg.getLocation().getStartLine() > 0);
        });
    }
}