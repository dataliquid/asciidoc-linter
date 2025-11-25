package com.dataliquid.asciidoc.linter.config.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

/**
 * Tests for YAML parsing of sidebar block configurations.
 */
@DisplayName("SidebarBlock YAML parsing")
class SidebarBlockYamlTest {

    @Test
    @DisplayName("should parse sidebar block with all configurations")
    void shouldParseSidebarBlockWithAllConfigurations() throws Exception {
        // Given
        String yaml = """
                document:
                  sections:
                    - level: 1
                      title:
                        pattern: "Introduction"
                      allowedBlocks:
                        - sidebar:
                            name: "additional-info"
                            severity: info
                            occurrence:
                              min: 0
                              max: 2
                              severity: warn
                            title:
                              required: false
                              minLength: 5
                              maxLength: 50
                              pattern: "^[A-Z].*$"
                              severity: info
                            content:
                              required: true
                              minLength: 50
                              maxLength: 800
                              lines:
                                min: 3
                                max: 30
                                severity: info
                            position:
                              required: false
                              allowed: ["left", "right", "float"]
                              severity: info
                """;

        ConfigurationLoader loader = new ConfigurationLoader(true); // Skip schema validation

        // When
        LinterConfiguration config = loader.loadConfiguration(yaml);

        // Then
        assertNotNull(config);
        assertNotNull(config.document());

        DocumentConfiguration doc = config.document();
        assertNotNull(doc.sections());
        assertEquals(1, doc.sections().size());

        SectionConfig section = doc.sections().get(0);
        assertNotNull(section.allowedBlocks());
        assertEquals(1, section.allowedBlocks().size());

        Block block = section.allowedBlocks().get(0);
        assertInstanceOf(SidebarBlock.class, block);

        SidebarBlock sidebar = (SidebarBlock) block;
        assertEquals("additional-info", sidebar.getName());
        assertEquals(Severity.INFO, sidebar.getSeverity());

        // Validate occurrence
        assertNotNull(sidebar.getOccurrence());
        assertEquals(0, sidebar.getOccurrence().min());
        assertEquals(2, sidebar.getOccurrence().max());
        assertEquals(Severity.WARN, sidebar.getOccurrence().severity());

        // Validate title config
        assertNotNull(sidebar.getTitle());
        assertFalse(sidebar.getTitle().isRequired());
        assertEquals(5, sidebar.getTitle().getMinLength());
        assertEquals(50, sidebar.getTitle().getMaxLength());
        assertEquals("^[A-Z].*$", sidebar.getTitle().getPattern().pattern());
        assertEquals(Severity.INFO, sidebar.getTitle().getSeverity());

        // Validate content config
        assertNotNull(sidebar.getContent());
        assertTrue(sidebar.getContent().isRequired());
        assertEquals(50, sidebar.getContent().getMinLength());
        assertEquals(800, sidebar.getContent().getMaxLength());

        // Validate lines config
        assertNotNull(sidebar.getContent().getLines());
        assertEquals(3, sidebar.getContent().getLines().getMin());
        assertEquals(30, sidebar.getContent().getLines().getMax());
        assertEquals(Severity.INFO, sidebar.getContent().getLines().getSeverity());

        // Validate position config
        assertNotNull(sidebar.getPosition());
        assertFalse(sidebar.getPosition().isRequired());
        assertEquals(List.of("left", "right", "float"), sidebar.getPosition().getAllowed());
        assertEquals(Severity.INFO, sidebar.getPosition().getSeverity());
    }

    @Test
    @DisplayName("should parse sidebar block with minimal configuration")
    void shouldParseSidebarBlockWithMinimalConfiguration() throws Exception {
        // Given
        String yaml = """
                document:
                  sections:
                    - level: 1
                      title:
                        pattern: "Section"
                      allowedBlocks:
                        - sidebar:
                            name: "simple-sidebar"
                            severity: warn
                """;

        ConfigurationLoader loader = new ConfigurationLoader(true);

        // When
        LinterConfiguration config = loader.loadConfiguration(yaml);

        // Then
        Block block = config.document().sections().get(0).allowedBlocks().get(0);
        assertInstanceOf(SidebarBlock.class, block);

        SidebarBlock sidebar = (SidebarBlock) block;
        assertEquals("simple-sidebar", sidebar.getName());
        assertEquals(Severity.WARN, sidebar.getSeverity());
        assertNull(sidebar.getTitle());
        assertNull(sidebar.getContent());
        assertNull(sidebar.getPosition());
    }
}
