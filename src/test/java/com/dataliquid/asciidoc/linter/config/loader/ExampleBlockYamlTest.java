package com.dataliquid.asciidoc.linter.config.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;

@DisplayName("ExampleBlock YAML Loading")
class ExampleBlockYamlTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader(true); // Skip schema validation for tests
    }
    
    @Test
    @DisplayName("should load example block with minimal configuration")
    void shouldLoadExampleBlockWithMinimalConfiguration() throws Exception {
        // Given
        String yaml = """
            document:
              sections:
                - name: test-section
                  occurrence:
                    min: 1
                    max: 1
                  allowedBlocks:
                    - example:
                        name: minimal-example
                        severity: warn
            """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        
        // When
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        // Then
        var section = config.document().sections().get(0);
        var block = section.allowedBlocks().get(0);
        
        assertInstanceOf(ExampleBlock.class, block);
        ExampleBlock exampleBlock = (ExampleBlock) block;
        
        assertEquals("minimal-example", exampleBlock.getName());
        assertEquals(Severity.WARN, exampleBlock.getSeverity());
        assertNull(exampleBlock.getCaption());
        assertNull(exampleBlock.getCollapsible());
    }
    
    @Test
    @DisplayName("should load example block with caption configuration")
    void shouldLoadExampleBlockWithCaptionConfiguration() throws Exception {
        // Given
        String yaml = """
            document:
              sections:
                - name: test-section
                  occurrence:
                    min: 1
                    max: 1
                  allowedBlocks:
                    - example:
                        name: example-with-caption
                        severity: warn
                        caption:
                          required: true
                          pattern: "^(Example|Beispiel)\\\\s+\\\\d+\\\\.\\\\d*:.*"
                          minLength: 15
                          maxLength: 100
                          severity: error
            """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        
        // When
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        // Then
        var section = config.document().sections().get(0);
        var block = section.allowedBlocks().get(0);
        
        assertInstanceOf(ExampleBlock.class, block);
        ExampleBlock exampleBlock = (ExampleBlock) block;
        
        assertNotNull(exampleBlock.getCaption());
        var caption = exampleBlock.getCaption();
        
        assertTrue(caption.isRequired());
        assertNotNull(caption.getPattern());
        assertEquals("^(Example|Beispiel)\\s+\\d+\\.\\d*:.*", caption.getPattern().pattern());
        assertEquals(15, caption.getMinLength());
        assertEquals(100, caption.getMaxLength());
        assertEquals(Severity.ERROR, caption.getSeverity());
    }
    
    @Test
    @DisplayName("should load example block with collapsible configuration")
    void shouldLoadExampleBlockWithCollapsibleConfiguration() throws Exception {
        // Given
        String yaml = """
            document:
              sections:
                - name: test-section
                  occurrence:
                    min: 1
                    max: 1
                  allowedBlocks:
                    - example:
                        name: example-with-collapsible
                        severity: warn
                        collapsible:
                          required: false
                          allowed: [true, false]
                          severity: info
            """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        
        // When
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        // Then
        var section = config.document().sections().get(0);
        var block = section.allowedBlocks().get(0);
        
        assertInstanceOf(ExampleBlock.class, block);
        ExampleBlock exampleBlock = (ExampleBlock) block;
        
        assertNotNull(exampleBlock.getCollapsible());
        var collapsible = exampleBlock.getCollapsible();
        
        assertFalse(collapsible.isRequired());
        assertEquals(Arrays.asList(true, false), collapsible.getAllowed());
        assertEquals(Severity.INFO, collapsible.getSeverity());
    }
    
    @Test
    @DisplayName("should load example block with full configuration")
    void shouldLoadExampleBlockWithFullConfiguration() throws Exception {
        // Given
        String yaml = """
            document:
              sections:
                - name: test-section
                  occurrence:
                    min: 1
                    max: 1
                  allowedBlocks:
                    - example:
                        name: full-example
                        severity: warn
                        occurrence:
                          min: 0
                          max: 10
                        caption:
                          required: true
                          pattern: "^Example \\\\d+:.*"
                          minLength: 10
                          maxLength: 50
                          severity: error
                        collapsible:
                          required: false
                          allowed: [true, false]
                          severity: info
            """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        
        // When
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        // Then
        var section = config.document().sections().get(0);
        var block = section.allowedBlocks().get(0);
        
        assertInstanceOf(ExampleBlock.class, block);
        ExampleBlock exampleBlock = (ExampleBlock) block;
        
        assertEquals("full-example", exampleBlock.getName());
        assertEquals(Severity.WARN, exampleBlock.getSeverity());
        
        assertNotNull(exampleBlock.getOccurrence());
        assertEquals(0, exampleBlock.getOccurrence().min());
        assertEquals(10, exampleBlock.getOccurrence().max());
        
        assertNotNull(exampleBlock.getCaption());
        assertTrue(exampleBlock.getCaption().isRequired());
        
        assertNotNull(exampleBlock.getCollapsible());
        assertFalse(exampleBlock.getCollapsible().isRequired());
    }
}