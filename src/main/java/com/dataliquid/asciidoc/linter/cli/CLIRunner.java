package com.dataliquid.asciidoc.linter.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dataliquid.asciidoc.linter.Linter;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputConfigurationLoader;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Executes the linter based on CLI configuration.
 */
public class CLIRunner {
    
    private static final Logger logger = LogManager.getLogger(CLIRunner.class);
    private static final String DEFAULT_CONFIG_FILE = ".linter-config.yaml";
    
    private final FileDiscoveryService fileDiscoveryService;
    private final CLIOutputHandler outputHandler;
    private final ConfigurationLoader configurationLoader;
    private final OutputConfigurationLoader outputConfigurationLoader;
    private final Linter linter;
    
    public CLIRunner() {
        this.fileDiscoveryService = new FileDiscoveryService();
        this.outputHandler = new CLIOutputHandler();
        this.configurationLoader = new ConfigurationLoader();
        this.outputConfigurationLoader = new OutputConfigurationLoader();
        this.linter = new Linter();
    }
    
    /**
     * Runs the linter with the given configuration.
     * 
     * @param config CLI configuration
     * @return Exit code (0 = success, 1 = violations, 2 = error)
     */
    public int run(CLIConfig config) {
        try {
            // Load linter configuration
            LinterConfiguration linterConfig = loadLinterConfiguration(config);
            
            // Load output configuration
            OutputConfiguration outputConfig = loadOutputConfiguration(config);
            
            // Discover files
            List<Path> filesToValidate = fileDiscoveryService.discoverFiles(config);
            
            if (filesToValidate.isEmpty()) {
                logger.error("No files found matching patterns: {}", String.join(", ", config.getInputPatterns()));
                return 2;
            }
            
            // Print files being validated
            if (filesToValidate.size() > 1) {
                logger.info("Validating {} files...", filesToValidate.size());
            }
            
            // Validate files
            if (filesToValidate.size() == 1) {
                // Single file validation
                ValidationResult result = linter.validateFile(filesToValidate.get(0), linterConfig);
                outputHandler.writeReport(result, config, outputConfig);
                return determineExitCode(result, config.getFailLevel());
            } else {
                // Multiple file validation
                Map<Path, ValidationResult> results = linter.validateFiles(filesToValidate, linterConfig);
                ValidationResult aggregated = aggregateResults(results);
                outputHandler.writeMultipleReports(results, config, aggregated, outputConfig);
                return determineExitCode(aggregated, config.getFailLevel());
            }
            
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
            return 2;
        } finally {
            linter.close();
        }
    }
    
    private LinterConfiguration loadLinterConfiguration(CLIConfig config) throws IOException {
        Path configFile = config.getConfigFile();
        
        if (configFile == null) {
            // Look for default config file in current directory
            Path defaultConfig = Paths.get(DEFAULT_CONFIG_FILE);
            if (Files.exists(defaultConfig)) {
                configFile = defaultConfig;
            } else {
                // Return empty configuration
                return LinterConfiguration.builder().build();
            }
        }
        
        if (!Files.exists(configFile)) {
            throw new IOException("Configuration file not found: " + configFile);
        }
        
        return configurationLoader.loadConfiguration(configFile);
    }
    
    private OutputConfiguration loadOutputConfiguration(CLIConfig config) throws IOException {
        OutputFormat outputConfigFormat = config.getOutputConfigFormat();
        Path outputConfigFile = config.getOutputConfigFile();
        
        // If neither is specified, use default (enhanced)
        if (outputConfigFormat == null && outputConfigFile == null) {
            return outputConfigurationLoader.loadPredefinedConfiguration(OutputFormat.ENHANCED);
        }
        
        // If predefined format is specified
        if (outputConfigFormat != null) {
            return outputConfigurationLoader.loadPredefinedConfiguration(outputConfigFormat);
        }
        
        // If custom file is specified
        if (outputConfigFile != null) {
            if (!Files.exists(outputConfigFile)) {
                throw new IOException("Output configuration file not found: " + outputConfigFile);
            }
            return outputConfigurationLoader.loadConfiguration(outputConfigFile.toString());
        }
        
        // Should never reach here due to the first check
        throw new IllegalStateException("No output configuration specified");
    }
    
    private int determineExitCode(ValidationResult result, Severity failLevel) {
        switch (failLevel) {
            case ERROR:
                return result.hasErrors() ? 1 : 0;
            case WARN:
                return (result.hasErrors() || result.hasWarnings()) ? 1 : 0;
            case INFO:
                return result.hasMessages() ? 1 : 0;
            default:
                return 0;
        }
    }
    
    private ValidationResult aggregateResults(Map<Path, ValidationResult> results) {
        ValidationResult.Builder aggregated = ValidationResult.builder();
        
        for (Map.Entry<Path, ValidationResult> entry : results.entrySet()) {
            // Add all scanned files
            aggregated.addScannedFiles(entry.getValue().getScannedFiles());
            
            // Add all messages
            for (ValidationMessage message : entry.getValue().getMessages()) {
                aggregated.addMessage(message);
            }
        }
        
        return aggregated.complete().build();
    }
}