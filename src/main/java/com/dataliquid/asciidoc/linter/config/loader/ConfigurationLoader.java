package com.dataliquid.asciidoc.linter.config.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.validation.RuleSchemaValidator;
import com.dataliquid.asciidoc.linter.config.validation.RuleValidationException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class ConfigurationLoader {

    private static final Logger logger = LogManager.getLogger(ConfigurationLoader.class);

    private final YAMLMapper mapper;
    private final RuleSchemaValidator schemaValidator;
    private final boolean skipRuleSchemaValidation;

    public ConfigurationLoader() {
        this(false);
    }

    public ConfigurationLoader(boolean skipRuleSchemaValidation) {
        this.mapper = YAMLMapper
                .builder()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        this.skipRuleSchemaValidation = skipRuleSchemaValidation;

        if (!skipRuleSchemaValidation) {
            this.schemaValidator = new RuleSchemaValidator();
        } else {
            this.schemaValidator = null;
            logger.warn("Rule configuration schema validation is DISABLED");
        }
    }

    public LinterConfiguration loadConfiguration(Path configPath) throws IOException {
        // First: Validate user config against schema
        if (!skipRuleSchemaValidation && schemaValidator != null) {
            try {
                schemaValidator.validateUserConfig(configPath);
            } catch (RuleValidationException e) {
                throw new ConfigurationException("User configuration does not match schema: " + e.getMessage(), e);
            }
        }

        // Then: Parse the validated config
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            return loadConfiguration(inputStream);
        }
    }

    public LinterConfiguration loadConfiguration(String yamlContent) {
        // First: Validate string config against schema
        if (!skipRuleSchemaValidation && schemaValidator != null) {
            try {
                schemaValidator.validateYamlString(yamlContent);
            } catch (RuleValidationException e) {
                throw new ConfigurationException("User configuration does not match schema: " + e.getMessage(), e);
            }
        }

        // Then: Parse the validated config
        try {
            LinterConfiguration config = mapper.readValue(yamlContent, LinterConfiguration.class);
            if (config == null || config.document() == null) {
                throw new ConfigurationException("Missing required 'document' section in configuration");
            }
            return config;
        } catch (JacksonException e) {
            throw new ConfigurationException("Failed to parse YAML configuration: " + e.getMessage(), e);
        }
    }

    public LinterConfiguration loadConfiguration(InputStream inputStream) {
        try {
            LinterConfiguration config = mapper.readValue(inputStream, LinterConfiguration.class);
            if (config == null || config.document() == null) {
                throw new ConfigurationException("Missing required 'document' section in configuration");
            }
            return config;
        } catch (JacksonException e) {
            throw new ConfigurationException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
}
