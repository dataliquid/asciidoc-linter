package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;

/**
 * Test for metadata validator with column position tracking.
 */
@DisplayName("MetadataValidator Underline Test")
class MetadataValidatorUnderlineTest {

    private Asciidoctor asciidoctor;

    @BeforeEach
    void setUp() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    @Test
    @DisplayName("should report correct column position for attribute value")
    void shouldReportCorrectColumnPositionForAttributeValue(@TempDir Path tempDir) throws IOException {
        // Given
        String content = """
                = Test Document
                :author: john doe
                :version: 1.0
                """;

        Path testFile = tempDir.resolve("test.adoc");
        Files.writeString(testFile, content);

        Document document = asciidoctor
                .loadFile(testFile.toFile(), Options.builder().sourcemap(true).toFile(false).build());

        MetadataConfiguration config = MetadataConfiguration
                .builder()
                .attributes(java.util.Arrays
                        .asList(AttributeConfig
                                .builder()
                                .name("author")
                                .pattern("^[A-Z][a-z]+ [A-Z][a-z]+$")
                                .severity(Severity.ERROR)
                                .build()))
                .build();

        MetadataValidator validator = MetadataValidator.fromConfiguration(config).build();

        // When
        ValidationResult result = validator.validate(document, testFile.toString());

        // Then
        assertFalse(result.getMessages().isEmpty(), "Should have validation errors");

        ValidationMessage message = result.getMessages().get(0);
        assertEquals("author", message.getAttributeName().orElse(""));

        // Check location
        SourceLocation location = message.getLocation();
        assertEquals(2, location.getStartLine(), "Should be on line 2");
        assertEquals(10, location.getStartColumn(), "Should start at column 10 (after ':author: ')");
        assertEquals(17, location.getEndColumn(), "Should end at column 17 (end of 'john doe')");
    }
}
