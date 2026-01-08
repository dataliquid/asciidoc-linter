package com.dataliquid.asciidoc.linter.config.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.SchemaConstants;
import com.networknt.schema.Error;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;

/**
 * Validates output configuration against the output configuration schema.
 */
public class OutputSchemaValidator {
    private final String schemaPath;
    private final Schema schema;
    private final YAMLMapper yamlMapper;

    public OutputSchemaValidator(String schemaPath) {
        this.schemaPath = schemaPath;
        this.yamlMapper = new YAMLMapper();
        this.schema = loadSchema();
    }

    private Schema loadSchema() {
        try {
            // Load the schema from classpath
            try (InputStream schemaStream = getClass().getResourceAsStream(schemaPath)) {
                if (schemaStream == null) {
                    throw new OutputConfigurationException("Schema not found: " + schemaPath);
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
                String schemaUri = "classpath:" + schemaPath;
                return registry.getSchema(SchemaLocation.of(schemaUri), schemaNode);
            }

        } catch (IOException e) {
            throw new OutputConfigurationException("Failed to load schema: " + schemaPath, e);
        }
    }

    /**
     * Validates the given YAML configuration against the schema.
     */
    public void validate(String yamlContent) {
        try {
            JsonNode configNode = yamlMapper.readTree(yamlContent);
            List<Error> errors = schema.validate(configNode);

            if (!errors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Output configuration validation failed:\n");
                for (Error error : errors) {
                    errorMessage
                            .append("  - ")
                            .append(error.getInstanceLocation())
                            .append(": ")
                            .append(error.getMessage())
                            .append('\n');
                }
                throw new OutputConfigurationException(errorMessage.toString());
            }
        } catch (JacksonException e) {
            throw new OutputConfigurationException("Failed to parse YAML", e);
        }
    }
}
