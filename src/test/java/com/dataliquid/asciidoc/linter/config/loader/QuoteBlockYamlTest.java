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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;

@DisplayName("QuoteBlock YAML Loading Tests")
class QuoteBlockYamlTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader(true); // Skip schema validation for tests
    }
    
    @Test
    @DisplayName("should load quote block from YAML with all configurations")
    void shouldLoadQuoteBlockFromYaml() throws Exception {
        String yaml = """
                document:
                  sections:
                    - name: "test-section"
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - quote:
                            name: "important-quote"
                            severity: info
                            occurrence:
                              min: 0
                              max: 3
                              severity: warn
                            author:
                              required: true
                              minLength: 3
                              maxLength: 100
                              pattern: "^[A-Z][a-zA-Z\\\\s\\\\.\\\\-,]+$"
                              severity: error
                            source:
                              required: false
                              minLength: 5
                              maxLength: 200
                              pattern: "^[A-Za-z0-9\\\\s,\\\\.\\\\-\\\\(\\\\)]+$"
                              severity: warn
                            content:
                              required: true
                              minLength: 20
                              maxLength: 1000
                              lines:
                                min: 1
                                max: 20
                                severity: info
                """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
        assertEquals(1, config.document().sections().size());
        
        var section = config.document().sections().get(0);
        assertNotNull(section.allowedBlocks());
        assertEquals(1, section.allowedBlocks().size());
        
        var block = section.allowedBlocks().get(0);
        assertInstanceOf(QuoteBlock.class, block);
        
        QuoteBlock quoteBlock = (QuoteBlock) block;
        assertEquals("important-quote", quoteBlock.getName());
        assertEquals(Severity.INFO, quoteBlock.getSeverity());
        assertEquals(BlockType.QUOTE, quoteBlock.getType());
        
        // Verify occurrence
        assertNotNull(quoteBlock.getOccurrence());
        assertEquals(0, quoteBlock.getOccurrence().min());
        assertEquals(3, quoteBlock.getOccurrence().max());
        assertEquals(Severity.WARN, quoteBlock.getOccurrence().severity());
        
        // Verify author config
        assertNotNull(quoteBlock.getAuthor());
        assertTrue(quoteBlock.getAuthor().isRequired());
        assertEquals(3, quoteBlock.getAuthor().getMinLength());
        assertEquals(100, quoteBlock.getAuthor().getMaxLength());
        assertEquals("^[A-Z][a-zA-Z\\s\\.\\-,]+$", quoteBlock.getAuthor().getPattern().pattern());
        assertEquals(Severity.ERROR, quoteBlock.getAuthor().getSeverity());
        
        // Verify source config
        assertNotNull(quoteBlock.getSource());
        assertFalse(quoteBlock.getSource().isRequired());
        assertEquals(5, quoteBlock.getSource().getMinLength());
        assertEquals(200, quoteBlock.getSource().getMaxLength());
        assertEquals("^[A-Za-z0-9\\s,\\.\\-\\(\\)]+$", quoteBlock.getSource().getPattern().pattern());
        assertEquals(Severity.WARN, quoteBlock.getSource().getSeverity());
        
        // Verify content config
        assertNotNull(quoteBlock.getContent());
        assertTrue(quoteBlock.getContent().isRequired());
        assertEquals(20, quoteBlock.getContent().getMinLength());
        assertEquals(1000, quoteBlock.getContent().getMaxLength());
        
        // Verify lines config
        assertNotNull(quoteBlock.getContent().getLines());
        assertEquals(1, quoteBlock.getContent().getLines().getMin());
        assertEquals(20, quoteBlock.getContent().getLines().getMax());
        assertEquals(Severity.INFO, quoteBlock.getContent().getLines().getSeverity());
    }
    
    @Test
    @DisplayName("should load quote block with minimal configuration")
    void shouldLoadQuoteBlockWithMinimalConfig() throws Exception {
        String yaml = """
                document:
                  sections:
                    - name: "test-section"
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - quote:
                            severity: info
                """;
        
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        LinterConfiguration config = loader.loadConfiguration(stream);
        
        var block = config.document().sections().get(0).allowedBlocks().get(0);
        assertInstanceOf(QuoteBlock.class, block);
        
        QuoteBlock quoteBlock = (QuoteBlock) block;
        assertEquals(Severity.INFO, quoteBlock.getSeverity());
        assertNull(quoteBlock.getName());
        assertNull(quoteBlock.getOccurrence());
        assertNull(quoteBlock.getAuthor());
        assertNull(quoteBlock.getSource());
        assertNull(quoteBlock.getContent());
    }
}