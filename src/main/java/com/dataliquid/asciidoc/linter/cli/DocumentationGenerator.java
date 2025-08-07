package com.dataliquid.asciidoc.linter.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.documentation.AsciiDocRuleGenerator;
import com.dataliquid.asciidoc.linter.documentation.RuleDocumentationGenerator;
import com.dataliquid.asciidoc.linter.documentation.VisualizationStyle;

/**
 * Runner for generating documentation from linter configuration.
 */
public class DocumentationGenerator {
    
    private final ConfigurationLoader configLoader;
    
    public DocumentationGenerator() {
        this.configLoader = new ConfigurationLoader();
    }
    
    /**
     * Generates documentation based on CLI arguments.
     * 
     * @param cmd the parsed command line
     * @return exit code (0 for success, non-zero for error)
     */
    public int run(CommandLine cmd) {
        try {
            // Load configuration
            String configPath = cmd.getOptionValue("rule", ".linter-rule-config.yaml");
            LinterConfiguration config = loadConfiguration(configPath);
            
            // Parse visualization styles
            Set<VisualizationStyle> styles = parseVisualizationStyles(cmd.getOptionValue("viz-style"));
            
            // Create generator
            RuleDocumentationGenerator generator = new AsciiDocRuleGenerator(styles);
            
            // Determine output
            String outputPath = cmd.getOptionValue("report-output");
            
            if (outputPath != null) {
                // Write to file
                generateToFile(generator, config, outputPath);
                System.out.println("Documentation generated successfully: " + outputPath);
            } else {
                // Write to stdout
                generateToStdout(generator, config);
            }
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Error generating documentation: " + e.getMessage());
            return 2;
        }
    }
    
    private LinterConfiguration loadConfiguration(String configPath) throws IOException {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath);
        }
        
        return configLoader.loadConfiguration(configFile.toPath());
    }
    
    private Set<VisualizationStyle> parseVisualizationStyles(String stylesArg) {
        if (stylesArg == null || stylesArg.trim().isEmpty()) {
            // Default to tree visualization
            return Set.of(VisualizationStyle.TREE);
        }
        
        return Arrays.stream(stylesArg.split(","))
            .map(String::trim)
            .map(VisualizationStyle::fromName)
            .collect(Collectors.toSet());
    }
    
    private void generateToFile(RuleDocumentationGenerator generator, 
                               LinterConfiguration config, 
                               String outputPath) throws IOException {
        
        File outputFile = new File(outputPath);
        
        // Create parent directories if needed
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create output directory: " + parentDir);
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            generator.generate(config, writer);
        }
    }
    
    private void generateToStdout(RuleDocumentationGenerator generator, 
                                 LinterConfiguration config) {
        PrintWriter writer = new PrintWriter(System.out);
        generator.generate(config, writer);
        writer.flush();
    }
}