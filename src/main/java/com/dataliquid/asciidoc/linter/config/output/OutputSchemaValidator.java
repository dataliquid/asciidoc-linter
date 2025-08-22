package com.dataliquid.asciidoc.linter.config.output;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
 * Validates output configuration against the output configuration schema.
 */
public class OutputSchemaValidator {
    private final String schemaPath;
    private final JsonSchema schema;
    private final ObjectMapper yamlMapper;

    public OutputSchemaValidator(String schemaPath) {
        this.schemaPath = schemaPath;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.schema = loadSchema();
    }

    private JsonSchema loadSchema() {
        try {
            // Load the schema from classpath
            InputStream schemaStream = getClass().getResourceAsStream(schemaPath);
            if (schemaStream == null) {
                throw new OutputConfigurationException("Schema not found: " + schemaPath);
            }

            // Convert YAML schema to JSON
            JsonNode schemaNode = yamlMapper.readTree(schemaStream);

            // Get the current classloader base URL for mapping
            String baseClasspathUrl = getClass().getResource("/schemas/").toString();

            // Configure JsonSchemaFactory for JSON Schema 2020-12
            JsonSchemaFactory factory = JsonSchemaFactory.builder()
                    .defaultMetaSchemaIri(JsonMetaSchema.getV202012().getIri()).schemaMappers(schemaMappers -> {
                        // Map HTTPS references to actual classpath URLs
                        schemaMappers.mapPrefix("https://dataliquid.com/asciidoc/linter/schemas/", baseClasspathUrl);
                    }).metaSchema(JsonMetaSchema.getV202012()).build();

            // Configure validators
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().pathType(PathType.JSON_POINTER).build();

            // Load schema with the resource URL as base URI
            URI schemaUri = getClass().getResource(schemaPath).toURI();
            return factory.getSchema(schemaUri, schemaNode, config);

        } catch (IOException | URISyntaxException e) {
            throw new OutputConfigurationException("Failed to load schema: " + schemaPath, e);
        }
    }

    /**
     * Validates the given YAML configuration against the schema.
     */
    public void validate(String yamlContent) {
        try {
            JsonNode configNode = yamlMapper.readTree(yamlContent);
            Set<ValidationMessage> errors = schema.validate(configNode);

            if (!errors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Output configuration validation failed:\n");
                for (ValidationMessage error : errors) {
                    errorMessage.append("  - ").append(error.getInstanceLocation()).append(": ")
                            .append(error.getMessage()).append("\n");
                }
                throw new OutputConfigurationException(errorMessage.toString());
            }
        } catch (IOException e) {
            throw new OutputConfigurationException("Failed to parse YAML", e);
        }
    }
}
