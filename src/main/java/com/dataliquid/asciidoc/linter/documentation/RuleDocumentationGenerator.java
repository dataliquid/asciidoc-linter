package com.dataliquid.asciidoc.linter.documentation;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;

/**
 * Interface for generating human-readable documentation from linter configuration rules.
 *
 * <p>
 * Implementations of this interface convert YAML-based linter rules into documentation formats suitable for content
 * authors.
 * </p>
 */
public interface RuleDocumentationGenerator {

    /**
     * Generates documentation from the given linter configuration.
     *
     * @param config
     *            the linter configuration containing all rules
     * @param writer
     *            the writer to output the documentation to
     */
    void generate(LinterConfiguration config, PrintWriter writer);

    /**
     * Returns the documentation format this generator produces.
     *
     * @return the documentation format
     */
    DocumentationFormat getFormat();

    /**
     * Returns the name of this generator for display purposes.
     *
     * @return the generator name
     */
    String getName();
}
