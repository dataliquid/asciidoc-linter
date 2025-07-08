package com.dataliquid.asciidoc.linter.cli;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.dataliquid.asciidoc.linter.report.ReportWriter;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Handles output routing for CLI based on configuration.
 */
public class CLIOutputHandler {
    
    private final ReportWriter reportWriter;
    
    public CLIOutputHandler() {
        this.reportWriter = new ReportWriter();
    }
    
    /**
     * Writes a single validation result based on the CLI configuration.
     */
    public void writeReport(ValidationResult result, CLIConfig config) throws IOException {
        if (config.isOutputToFile()) {
            // Write to file
            Path outputFile = config.getReportOutput();
            ensureParentDirectoryExists(outputFile);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile.toFile()))) {
                reportWriter.write(result, config.getReportFormat(), writer);
            }
        } else {
            // Write to console (stdout)
            reportWriter.writeToConsole(result, config.getReportFormat());
        }
    }
    
    /**
     * Writes multiple validation results for directory input.
     */
    public void writeMultipleReports(Map<Path, ValidationResult> results, CLIConfig config, 
                                   ValidationResult aggregated) throws IOException {
        if (!config.isOutputToFile()) {
            // Write aggregated result to console
            reportWriter.writeToConsole(aggregated, config.getReportFormat());
            return;
        }
        
        Path output = config.getReportOutput();
        
        if (Files.isDirectory(output) || output.toString().endsWith("/") || output.toString().endsWith("\\")) {
            // Write individual reports to directory
            writeIndividualReports(results, config, output);
        } else {
            // Write aggregated report to single file
            writeReport(aggregated, config);
        }
    }
    
    private void writeIndividualReports(Map<Path, ValidationResult> results, CLIConfig config, 
                                      Path outputDir) throws IOException {
        // Ensure output directory exists
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        for (Map.Entry<Path, ValidationResult> entry : results.entrySet()) {
            Path inputFile = entry.getKey();
            ValidationResult result = entry.getValue();
            
            // Generate output filename based on input filename
            String outputFileName = generateOutputFileName(inputFile, config.getReportFormat());
            Path outputFile = outputDir.resolve(outputFileName);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile.toFile()))) {
                reportWriter.write(result, config.getReportFormat(), writer);
            }
        }
    }
    
    private String generateOutputFileName(Path inputFile, String format) {
        String baseName = inputFile.getFileName().toString();
        
        // Remove .adoc extension if present
        if (baseName.endsWith(".adoc")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        
        // Add format extension
        String extension = "json".equals(format) ? ".json" : ".txt";
        return baseName + "-report" + extension;
    }
    
    private void ensureParentDirectoryExists(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}