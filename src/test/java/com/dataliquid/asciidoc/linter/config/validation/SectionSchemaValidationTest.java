package com.dataliquid.asciidoc.linter.config.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;

@DisplayName("Section Schema Validation")
class SectionSchemaValidationTest {

    private ConfigurationLoader loader;

    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader(); // Enable schema validation
    }

    @Test
    @DisplayName("should validate configuration with all block types in sections")
    void shouldValidateConfigurationWithAllBlockTypes() throws IOException {
        // Given
        String yaml = """
                document:
                  sections:
                    - name: comprehensive
                      level: 1
                      occurrence:
                        min: 1
                        max: 1
                      allowedBlocks:
                        - paragraph:
                            severity: warn
                            occurrence:
                              min: 1
                              max: 10
                        - listing:
                            severity: error
                            language:
                              required: true
                              severity: error
                        - image:
                            severity: info
                            url:
                              required: true
                        - table:
                            severity: warn
                            columns:
                              min: 2
                              max: 10
                        - verse:
                            severity: info
                            author:
                              required: false
                        - sidebar:
                            severity: info
                            title:
                              required: true
                              severity: error
                        - admonition:
                            severity: warn
                            type:
                              required: true
                              allowed: ["NOTE", "TIP", "WARNING"]
                        - pass:
                            severity: error
                            type:
                              required: true
                              allowed: ["html", "xml"]
                        - literal:
                            severity: warn
                            lines:
                              min: 1
                              max: 100
                        - audio:
                            severity: info
                            url:
                              required: true
                        - quote:
                            severity: info
                """;

        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));

        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }

    @Test
    @DisplayName("should validate mixed block types in nested sections")
    void shouldValidateMixedBlockTypesInNestedSections() throws IOException {
        // Given
        String yaml = """
                document:
                  sections:
                    - name: main
                      level: 1
                      occurrence:
                        min: 1
                        max: 5
                      allowedBlocks:
                        - paragraph:
                            severity: error
                        - listing:
                            severity: warn
                        - admonition:
                            severity: info
                            type:
                              required: true
                      subsections:
                        - name: details
                          level: 2
                          occurrence:
                            min: 0
                            max: 3
                          allowedBlocks:
                            - literal:
                                severity: warn
                            - quote:
                                severity: info
                            - sidebar:
                                severity: info
                        - level: 2
                          occurrence:
                            min: 0
                            max: 5
                          allowedBlocks:
                            - audio:
                                severity: info
                            - pass:
                                severity: error
                """;

        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));

        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }

    @Test
    @DisplayName("should validate document title (level 0) with max = 1")
    void shouldValidateDocumentTitleWithMaxOne() throws IOException {
        // Given - valid configuration with level 0 and max = 1
        String yaml = """
                document:
                  sections:
                    - name: documentTitle
                      level: 0
                      occurrence:
                        min: 1
                        max: 1
                      title:
                        pattern: "^[A-Z].*"
                        severity: error
                """;

        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));

        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }

    @Test
    @DisplayName("should reject document title (level 0) with max > 1")
    void shouldRejectDocumentTitleWithMaxGreaterThanOne() {
        // Given - invalid configuration with level 0 and max = 2
        String yaml = """
                document:
                  sections:
                    - name: documentTitle
                      level: 0
                      occurrence:
                        min: 1
                        max: 2
                      title:
                        pattern: "^[A-Z].*"
                        severity: error
                """;

        // When & Then - should throw schema validation exception
        assertThrows(com.dataliquid.asciidoc.linter.config.loader.ConfigurationException.class,
                () -> loader.loadConfiguration(yaml), "Should reject level 0 section with max > 1");
    }

    @Test
    @DisplayName("should accept non-document sections (level > 0) with max > 1")
    void shouldAcceptNonDocumentSectionsWithMaxGreaterThanOne() throws IOException {
        // Given - valid configuration with level 1 and max = 5
        String yaml = """
                document:
                  sections:
                    - name: mainSection
                      level: 1
                      occurrence:
                        min: 1
                        max: 5
                      title:
                        pattern: "^Chapter.*"
                        severity: error
                """;

        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));

        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }
}
