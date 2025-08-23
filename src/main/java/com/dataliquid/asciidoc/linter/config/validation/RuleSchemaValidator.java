package com.dataliquid.asciidoc.linter.config.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;

/**
 * Validates user configuration files against the linter configuration schema.
 */
public class RuleSchemaValidator {
    private static final String SCHEMA_PATH = "/schemas/rules/linter-config-schema.yaml";

    // Constants for validation message types
    private static final String MSG_TYPE_ENUM = "enum";
    private static final String MSG_TYPE_REQUIRED = "required";

    private final JsonSchema schema;
    private final ObjectMapper yamlMapper;

    public RuleSchemaValidator() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.schema = loadSchema();
    }

    private JsonSchema loadSchema() {
        try {
            // Load the main schema from classpath
            try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMA_PATH)) {
                if (schemaStream == null) {
                    throw new RuleValidationException("Schema not found: " + SCHEMA_PATH);
                }

                // Convert YAML schema to JSON
                JsonNode schemaNode = yamlMapper.readTree(schemaStream);

                // Get the current classloader base URL for mapping
                String baseClasspathUrl = getClass().getResource("/schemas/").toString();

                // Configure JsonSchemaFactory for JSON Schema 2020-12 with schema mappings
                JsonSchemaFactory factory = JsonSchemaFactory
                        .builder()
                        .defaultMetaSchemaIri(JsonMetaSchema.getV202012().getIri())
                        .schemaMappers(schemaMappers -> {
                            // Map HTTPS references to actual classpath URLs
                            schemaMappers
                                    .mapPrefix("https://dataliquid.com/asciidoc/linter/schemas/", baseClasspathUrl);
                        })
                        .metaSchema(JsonMetaSchema.getV202012())
                        .build();

                // Configure validators
                SchemaValidatorsConfig config = SchemaValidatorsConfig
                        .builder()
                        .pathType(PathType.JSON_POINTER)
                        .build();

                // Load schema with the resource URL as base URI
                URI schemaUri = getClass().getResource(SCHEMA_PATH).toURI();
                return factory.getSchema(schemaUri, schemaNode, config);
            }

        } catch (IOException | URISyntaxException e) {
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
        Set<ValidationMessage> messages = schema.validate(userConfigNode);

        if (!messages.isEmpty()) {
            throw new RuleValidationException(formatErrors(messages));
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

    private String formatErrors(Set<ValidationMessage> messages) {
        StringBuilder sb = new StringBuilder(150); // Increased buffer size
        sb.append("User configuration does not match schema:");

        for (ValidationMessage msg : messages) {
            sb.append("\n\n  Error at ").append(msg.getInstanceLocation()).append(":\n    ").append(msg.getMessage());

            // Add helpful context for common errors
            if (MSG_TYPE_ENUM.equals(msg.getType())) {
                sb.append("\n    Valid values: error, warn, info");
            } else if (MSG_TYPE_REQUIRED.equals(msg.getType())) {
                sb.append("\n    This field is required");
            } else if ("minimum".equals(msg.getType()) || "maximum".equals(msg.getType())) {
                sb.append("\n    Check the allowed range");
            }
        }

        return sb.toString();
    }
}
