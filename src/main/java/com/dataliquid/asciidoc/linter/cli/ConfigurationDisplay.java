package com.dataliquid.asciidoc.linter.cli;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.cli.display.AsciiBoxDrawer;
import com.dataliquid.asciidoc.linter.cli.display.TextWrapper;

/**
 * Handles the display of configuration information for the AsciiDoc linter.
 * Formats and presents the configuration in a visually appealing ASCII box.
 */
public class ConfigurationDisplay {
    
    private static final int BOX_WIDTH = 80;
    private static final int LABEL_WIDTH = 20;
    
    private final AsciiBoxDrawer boxDrawer;
    private final TextWrapper textWrapper;
    
    /**
     * Creates a new ConfigurationDisplay with default settings.
     */
    public ConfigurationDisplay() {
        this.boxDrawer = new AsciiBoxDrawer(BOX_WIDTH);
        this.textWrapper = new TextWrapper();
    }
    
    /**
     * Displays the configuration information in a formatted box.
     * 
     * @param config the CLI configuration to display
     */
    public void display(CLIConfig config) {
        System.out.println(); // Empty line before box
        
        boxDrawer.drawTop();
        boxDrawer.drawTitle("Configuration");
        boxDrawer.drawSeparator();
        
        drawConfigurationLines(config);
        
        boxDrawer.drawBottom();
        
        System.out.println(); // Empty line after box
        System.out.println("Starting validation...");
        System.out.println();
    }
    
    /**
     * Draws all configuration lines within the box.
     * 
     * @param config the CLI configuration
     */
    private void drawConfigurationLines(CLIConfig config) {
        // Input patterns - always shown
        drawConfigLine("Input patterns:", 
            String.join(", ", config.getInputPatterns()));
        
        // Base directory - always shown
        drawConfigLine("Base directory:", 
            config.getBaseDirectory().toString());
        
        // Configuration file - show "default" if not specified
        String configFile = config.getConfigFile() != null ? 
            config.getConfigFile().toString() : "default";
        drawConfigLine("Configuration:", configFile);
        
        // Output config - show name or file if specified
        if (config.getOutputConfigName() != null) {
            drawConfigLine("Output config:", 
                config.getOutputConfigName() + " (predefined)");
        } else if (config.getOutputConfigFile() != null) {
            drawConfigLine("Output config:", 
                config.getOutputConfigFile().toString());
        }
        
        // Report format - always shown
        drawConfigLine("Report format:", config.getReportFormat());
        
        // Report output file - only shown if specified
        if (config.getReportOutput() != null) {
            drawConfigLine("Report output:", 
                config.getReportOutput().toString());
        }
        
        // Fail level - always shown
        drawConfigLine("Fail level:", config.getFailLevel().toString());
    }
    
    /**
     * Draws a single configuration line, handling text wrapping if necessary.
     * 
     * @param label the configuration label
     * @param value the configuration value
     */
    private void drawConfigLine(String label, String value) {
        int valueWidth = BOX_WIDTH - 4 - LABEL_WIDTH; // 4 for borders and padding
        List<String> wrappedLines = textWrapper.wrap(value, valueWidth);
        boxDrawer.drawLabeledLines(label, wrappedLines, LABEL_WIDTH);
    }
    
    /**
     * Creates a list of configuration entries for display.
     * This method can be used for alternative display formats.
     * 
     * @param config the CLI configuration
     * @return a list of configuration entries
     */
    public List<ConfigEntry> getConfigurationEntries(CLIConfig config) {
        List<ConfigEntry> entries = new ArrayList<>();
        
        entries.add(new ConfigEntry("Input patterns", 
            String.join(", ", config.getInputPatterns())));
        entries.add(new ConfigEntry("Base directory", 
            config.getBaseDirectory().toString()));
        
        String configFile = config.getConfigFile() != null ? 
            config.getConfigFile().toString() : "default";
        entries.add(new ConfigEntry("Configuration", configFile));
        
        if (config.getOutputConfigFile() != null) {
            entries.add(new ConfigEntry("Output config", 
                config.getOutputConfigFile().toString()));
        }
        
        entries.add(new ConfigEntry("Report format", config.getReportFormat()));
        
        if (config.getReportOutput() != null) {
            entries.add(new ConfigEntry("Report output", 
                config.getReportOutput().toString()));
        }
        
        entries.add(new ConfigEntry("Fail level", 
            config.getFailLevel().toString()));
        
        return entries;
    }
    
    /**
     * Represents a single configuration entry.
     */
    public static class ConfigEntry {
        private final String label;
        private final String value;
        
        public ConfigEntry(String label, String value) {
            this.label = label;
            this.value = value;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getValue() {
            return value;
        }
    }
}