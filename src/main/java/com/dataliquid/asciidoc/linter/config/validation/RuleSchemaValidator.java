package com.dataliquid.asciidoc.linter.config.validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.SchemaConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;

/**
 * Validates user configuration files against the linter configuration schema.
 */
public class RuleSchemaValidator {
    private static final String SCHEMA_PATH = "/schemas/rules/linter-config-schema.yaml";

    // Constants for validation message keys
    private static final String MSG_KEY_ENUM = "enum";
    private static final String MSG_KEY_REQUIRED = "required";

    private final Schema schema;
    private final ObjectMapper yamlMapper;

    public RuleSchemaValidator() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.schema = loadSchema();
    }

    private Schema loadSchema() {
        try {
            // Load the main schema from classpath
            try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMA_PATH)) {
                if (schemaStream == null) {
                    throw new RuleValidationException("Schema not found: " + SCHEMA_PATH);
                }

                // Convert YAML schema to JSON
                JsonNode schemaNode = yamlMapper.readTree(schemaStream);

                // Map HTTPS schema URLs to classpath resources
                String classpathBaseUrl = "classpath:/schemas/";

                // Configure SchemaRegistryConfig for path type
                SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(PathType.JSON_POINTER).build();

                // Configure SchemaRegistry for JSON Schema 2020-12 with schema mappings
                SchemaRegistry registry = SchemaRegistry
                        .withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                                builder -> builder
                                        .schemaRegistryConfig(config)
                                        .schemaIdResolvers(resolvers -> resolvers
                                                .mapPrefix(SchemaConstants.SCHEMA_URL_PREFIX, classpathBaseUrl)));

                // Load schema with classpath URI as base
                String schemaUri = "classpath:" + SCHEMA_PATH;
                return registry.getSchema(SchemaLocation.of(schemaUri), schemaNode);
            }

        } catch (IOException e) {
            throw new RuleValidationException("Failed to load schema", e);
        }
    }

    /**
     * Validates a user configuration file against the schema.
     *
     * @param  userConfigFile          the user's configuration file
     *
     * @throws RuleValidationException if validation fails
     */
    public void validateUserConfig(Path userConfigFile) throws RuleValidationException {
        if (!Files.exists(userConfigFile)) {
            throw new RuleValidationException("Configuration file not found: " + userConfigFile);
        }

        try {
            JsonNode configNode = yamlMapper.readTree(userConfigFile.toFile());
            validateUserConfig(configNode);
        } catch (IOException e) {
            throw new RuleValidationException("Failed to read configuration file: " + userConfigFile, e);
        }
    }

    /**
     * Validates a user configuration JsonNode against the schema.
     *
     * @param  userConfigNode          the configuration as JsonNode
     *
     * @throws RuleValidationException if validation fails
     */
    public void validateUserConfig(JsonNode userConfigNode) throws RuleValidationException {
        List<Error> errors = schema.validate(userConfigNode);

        if (!errors.isEmpty()) {
            throw new RuleValidationException(formatErrors(errors));
        }
    }

    /**
     * Validates a user configuration from an InputStream.
     *
     * @param  yamlStream              the YAML configuration stream
     *
     * @throws RuleValidationException if validation fails
     */
    public void validateUserConfig(InputStream yamlStream) throws RuleValidationException {
        try {
            JsonNode configNode = yamlMapper.readTree(yamlStream);
            validateUserConfig(configNode);
        } catch (IOException e) {
            throw new RuleValidationException("Failed to parse YAML configuration", e);
        }
    }

    /**
     * Validates a user configuration from a YAML string.
     *
     * @param  yamlContent             the YAML configuration as string
     *
     * @throws RuleValidationException if validation fails
     */
    public void validateYamlString(String yamlContent) throws RuleValidationException {
        try {
            JsonNode configNode = yamlMapper.readTree(yamlContent);
            validateUserConfig(configNode);
        } catch (IOException e) {
            throw new RuleValidationException("Failed to parse YAML configuration string", e);
        }
    }

    private String formatErrors(List<Error> errors) {
        StringBuilder sb = new StringBuilder(150); // Increased buffer size
        sb.append("User configuration does not match schema:");

        for (Error error : errors) {
            sb
                    .append("\n\n  Error at ")
                    .append(error.getInstanceLocation())
                    .append(":\n    ")
                    .append(error.getMessage());

            // Add helpful context for common errors based on messageKey
            String messageKey = error.getMessageKey();
            if (messageKey != null) {
                if (messageKey.contains(MSG_KEY_ENUM)) {
                    sb.append("\n    Valid values: error, warn, info");
                } else if (messageKey.contains(MSG_KEY_REQUIRED)) {
                    sb.append("\n    This field is required");
                } else if (messageKey.contains("minimum") || messageKey.contains("maximum")) {
                    sb.append("\n    Check the allowed range");
                }
            }
        }

        return sb.toString();
    }
}
