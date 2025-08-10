package com.dataliquid.asciidoc.linter.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Defines command line options for the AsciiDoc linter CLI.
 */
public class CLIOptions {
    
    private final Options options;
    
    public CLIOptions() {
        this.options = new Options();
        defineOptions();
    }
    
    private void defineOptions() {
        // Input patterns (required for validation, optional for doc generation)
        options.addOption(Option.builder("i")
            .longOpt("input")
            .hasArg()
            .argName("patterns")
            .desc("Comma-separated Ant file patterns (e.g., '**/*.adoc,docs/**/*.asciidoc')")
            .build());
        
        // Configuration file
        options.addOption(Option.builder("r")
            .longOpt("rule")
            .hasArg()
            .argName("file")
            .desc("YAML rule configuration file (default: .linter-rule-config.yaml)")
            .build());
        
        // Report format
        options.addOption(Option.builder("f")
            .longOpt("report-format")
            .hasArg()
            .argName("format")
            .desc("Report format: console, json, json-compact (default: console)")
            .build());
        
        // Report output
        options.addOption(Option.builder("o")
            .longOpt("report-output")
            .hasArg()
            .argName("file/directory")
            .desc("Report output file or directory (default: stdout)")
            .build());
        
        // Fail level
        options.addOption(Option.builder("l")
            .longOpt("fail-level")
            .hasArg()
            .argName("level")
            .desc("Exit code 1 on: error, warn, info (default: error)")
            .build());
        
        // Output configuration (predefined)
        options.addOption(Option.builder()
            .longOpt("output-config")
            .hasArg()
            .argName("name")
            .desc("Predefined output configuration: enhanced, simple, compact (default: enhanced)")
            .build());
        
        // Output configuration (custom file)
        options.addOption(Option.builder()
            .longOpt("output-config-file")
            .hasArg()
            .argName("file")
            .desc("Custom YAML output configuration file for console formatting")
            .build());
        
        // Help
        options.addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show help message")
            .build());
        
        // Version
        options.addOption(Option.builder("v")
            .longOpt("version")
            .desc("Show version")
            .build());
        
        // Generate documentation
        options.addOption(Option.builder()
            .longOpt("generate-guidelines")
            .desc("Generate author guidelines showing all validation requirements")
            .build());
        
        // Visualization style for documentation
        options.addOption(Option.builder()
            .longOpt("viz-style")
            .hasArg()
            .argName("styles")
            .desc("Comma-separated visualization styles: tree, nested, breadcrumb, table (default: tree)")
            .build());
        
        // No splash screen
        options.addOption(Option.builder()
            .longOpt("no-splash")
            .desc("Suppress splash screen on startup")
            .build());
    }
    
    public Options getOptions() {
        return options;
    }
}