package com.dataliquid.asciidoc.linter.config;

/**
 * Constants for JSON Schema configuration.
 */
public final class SchemaConstants {

    /**
     * Base URL prefix for schema references. This URL is mapped to classpath
     * resources at runtime for schema validation.
     */
    public static final String SCHEMA_URL_PREFIX = "https://dataliquid.com/asciidoc/linter/schemas/";

    private SchemaConstants() {
        // Private constructor to prevent instantiation
    }
}