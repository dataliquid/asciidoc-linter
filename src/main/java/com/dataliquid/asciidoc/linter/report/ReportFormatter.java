package com.dataliquid.asciidoc.linter.report;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Interface for formatting validation results in different output formats.
 */
public interface ReportFormatter {

    /**
     * Formats the validation result and writes it to the provided writer.
     *
     * @param result the validation result to format
     * @param writer the writer to output the formatted result
     */
    void format(ValidationResult result, PrintWriter writer);

    /**
     * Returns the name of this formatter (e.g., "console", "json").
     *
     * @return the formatter name
     */
    String getName();
}
