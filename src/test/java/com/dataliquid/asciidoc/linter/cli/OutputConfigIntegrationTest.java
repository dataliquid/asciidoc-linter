package com.dataliquid.asciidoc.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputConfigurationLoader;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;

/**
 * Integration test for output configuration loading.
 */
class OutputConfigIntegrationTest {

    @Test
    void testLoadEnhancedConfig() throws IOException {
        OutputConfigurationLoader loader = new OutputConfigurationLoader();
        OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.ENHANCED);
        
        assertNotNull(config);
        assertEquals(OutputFormat.ENHANCED, config.getFormat());
        assertEquals(3, config.getDisplay().getContextLines());
        assertEquals(HighlightStyle.UNDERLINE, config.getDisplay().getHighlightStyle());
        assertEquals(true, config.getDisplay().isUseColors());
        assertEquals(true, config.getSuggestions().isEnabled());
        assertEquals(3, config.getSuggestions().getMaxPerError());
    }

    @Test
    void testLoadSimpleConfig() throws IOException {
        OutputConfigurationLoader loader = new OutputConfigurationLoader();
        OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.SIMPLE);
        
        assertNotNull(config);
        assertEquals(OutputFormat.SIMPLE, config.getFormat());
        assertEquals(1, config.getDisplay().getContextLines());
        assertEquals(HighlightStyle.ARROW, config.getDisplay().getHighlightStyle());
        assertEquals(false, config.getSuggestions().isEnabled());
    }

    @Test
    void testLoadCompactConfig() throws IOException {
        OutputConfigurationLoader loader = new OutputConfigurationLoader();
        OutputConfiguration config = loader.loadPredefinedConfiguration(OutputFormat.COMPACT);
        
        assertNotNull(config);
        assertEquals(OutputFormat.COMPACT, config.getFormat());
        assertEquals(0, config.getDisplay().getContextLines());
        assertEquals(HighlightStyle.NONE, config.getDisplay().getHighlightStyle());
        assertEquals(false, config.getDisplay().isUseColors());
        assertEquals(false, config.getSuggestions().isEnabled());
        assertEquals(false, config.getSummary().isEnabled());
    }
}