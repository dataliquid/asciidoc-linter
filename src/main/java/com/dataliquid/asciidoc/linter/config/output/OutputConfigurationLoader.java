package com.dataliquid.asciidoc.linter.config.output;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads output configuration from YAML files.
 */
public class OutputConfigurationLoader {
    private static final String SCHEMA_PATH = "/schemas/output/output-config-schema.yaml";
    
    private final ObjectMapper mapper;
    private final OutputSchemaValidator validator;
    
    /**
     * Creates a loader with schema validation enabled.
     */
    public OutputConfigurationLoader() {
        this(false);
    }
    
    /**
     * Creates a loader with optional schema validation.
     */
    public OutputConfigurationLoader(boolean skipValidation) {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.validator = skipValidation ? null : new OutputSchemaValidator(SCHEMA_PATH);
    }
    
    /**
     * Loads output configuration from a file path.
     */
    public OutputConfiguration loadConfiguration(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Configuration file not found: " + filePath);
        }
        
        try (InputStream input = Files.newInputStream(path)) {
            return loadConfiguration(input);
        }
    }
    
    /**
     * Loads output configuration from an input stream.
     */
    public OutputConfiguration loadConfiguration(InputStream input) throws IOException {
        // Parse YAML
        OutputConfigWrapper wrapper = mapper.readValue(input, OutputConfigWrapper.class);
        
        // Validate against schema if enabled
        if (validator != null) {
            String yaml = mapper.writeValueAsString(wrapper);
            validator.validate(yaml);
        }
        
        return wrapper.getOutput();
    }
    
    /**
     * Returns the default output configuration.
     */
    public OutputConfiguration getDefaultConfiguration() {
        return OutputConfiguration.defaultConfig();
    }
    
    /**
     * Returns a compact configuration for CI/CD environments.
     */
    public OutputConfiguration getCompactConfiguration() {
        return OutputConfiguration.compactConfig();
    }
}