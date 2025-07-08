package com.dataliquid.asciidoc.linter.report;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Facade for writing validation reports in different formats.
 * Manages available formatters and handles output to files or console.
 */
public class ReportWriter {
    
    private final Map<String, ReportFormatter> formatters;
    
    public ReportWriter() {
        this.formatters = new HashMap<>();
        registerDefaultFormatters();
    }
    
    private void registerDefaultFormatters() {
        // Console formatter will be created dynamically with output config
        registerFormatter(JsonFormatter.pretty());
        registerFormatter(JsonFormatter.compact());
    }
    
    /**
     * Registers a formatter for use by this writer.
     * 
     * @param formatter the formatter to register
     */
    public void registerFormatter(ReportFormatter formatter) {
        Objects.requireNonNull(formatter, "[" + getClass().getName() + "] formatter must not be null");
        formatters.put(formatter.getName(), formatter);
    }
    
    /**
     * Writes the validation result using the specified format.
     * If outputPath is null, writes to standard output.
     * 
     * @param result the validation result to write
     * @param format the output format (e.g., "console", "json")
     * @param outputPath the output file path, or null for standard output
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the format is not supported
     */
    public void write(ValidationResult result, String format, String outputPath) throws IOException {
        write(result, format, outputPath, null);
    }
    
    /**
     * Writes the validation result using the specified format with optional output configuration.
     * 
     * @param result the validation result to write
     * @param format the output format (e.g., "console", "json")
     * @param outputPath the output file path, or null for standard output
     * @param outputConfig the output configuration for console format, or null for default
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the format is not supported
     */
    public void write(ValidationResult result, String format, String outputPath, OutputConfiguration outputConfig) throws IOException {
        Objects.requireNonNull(result, "[" + getClass().getName() + "] result must not be null");
        
        ReportFormatter formatter = getFormatter(format, outputConfig);
        
        if (outputPath == null) {
            writeToConsole(result, formatter);
        } else {
            writeToFile(result, formatter, outputPath);
        }
    }
    
    /**
     * Writes the validation result using the specified format to a Path.
     * 
     * @param result the validation result to write
     * @param format the output format
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public void write(ValidationResult result, String format, Path outputPath) throws IOException {
        write(result, format, outputPath != null ? outputPath.toString() : null);
    }
    
    /**
     * Writes the validation result to a PrintWriter using the specified format.
     * 
     * @param result the validation result to write
     * @param format the output format
     * @param writer the writer to write to
     */
    public void write(ValidationResult result, String format, PrintWriter writer) {
        write(result, format, writer, null);
    }
    
    /**
     * Writes the validation result to a PrintWriter using the specified format with optional output configuration.
     * 
     * @param result the validation result to write
     * @param format the output format
     * @param writer the writer to write to
     * @param outputConfig the output configuration for console format, or null for default
     */
    public void write(ValidationResult result, String format, PrintWriter writer, OutputConfiguration outputConfig) {
        Objects.requireNonNull(result, "[" + getClass().getName() + "] result must not be null");
        Objects.requireNonNull(writer, "[" + getClass().getName() + "] writer must not be null");
        
        ReportFormatter formatter = getFormatter(format, outputConfig);
        formatter.format(result, writer);
        writer.flush();
    }
    
    /**
     * Writes the validation result to the console using the specified format.
     * 
     * @param result the validation result to write
     * @param format the output format
     */
    public void writeToConsole(ValidationResult result, String format) {
        writeToConsole(result, format, null);
    }
    
    /**
     * Writes the validation result to the console using the specified format with optional output configuration.
     * 
     * @param result the validation result to write
     * @param format the output format
     * @param outputConfig the output configuration for console format, or null for default
     */
    public void writeToConsole(ValidationResult result, String format, OutputConfiguration outputConfig) {
        Objects.requireNonNull(result, "[" + getClass().getName() + "] result must not be null");
        
        ReportFormatter formatter = getFormatter(format, outputConfig);
        writeToConsole(result, formatter);
    }
    
    private void writeToConsole(ValidationResult result, ReportFormatter formatter) {
        try (PrintWriter writer = new PrintWriter(System.out)) {
            formatter.format(result, writer);
            writer.flush();
        }
    }
    
    private void writeToFile(ValidationResult result, ReportFormatter formatter, String outputPath) 
            throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            formatter.format(result, writer);
        }
    }
    
    private ReportFormatter getFormatter(String format, OutputConfiguration outputConfig) {
        String formatName = format != null ? format.toLowerCase() : "console";
        
        // Special handling for console format with output configuration
        if ("console".equals(formatName)) {
            if (outputConfig != null) {
                return new ConsoleFormatter(outputConfig);
            } else {
                return new ConsoleFormatter();
            }
        }
        
        ReportFormatter formatter = formatters.get(formatName);
        
        if (formatter == null) {
            throw new IllegalArgumentException(
                "Unsupported format: " + format + ". Available formats: console, " + getAvailableFormats());
        }
        
        return formatter;
    }
    
    /**
     * Returns the set of available format names.
     * 
     * @return the available format names
     */
    public Set<String> getAvailableFormats() {
        return formatters.keySet();
    }
    
    /**
     * Calculates the exit code based on the validation result.
     * 
     * @param result the validation result
     * @return 0 if no errors, 1 if errors found
     */
    public static int calculateExitCode(ValidationResult result) {
        return result.hasErrors() ? 1 : 0;
    }
}