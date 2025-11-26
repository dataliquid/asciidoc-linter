package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.OUTPUT_CONFIG;

/**
 * Wrapper class for the output configuration to match the YAML structure. The
 * YAML file has "output" as the root key.
 */
public final class OutputConfigWrapper {
    private final OutputConfiguration outputValue;

    @JsonCreator
    public OutputConfigWrapper(@JsonProperty(OUTPUT_CONFIG) OutputConfiguration output) {
        this.outputValue = Objects.requireNonNull(output, "[" + getClass().getName() + "] output must not be null");
    }

    public OutputConfiguration getOutput() {
        return this.outputValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OutputConfigWrapper that = (OutputConfigWrapper) o;
        return Objects.equals(outputValue, that.outputValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputValue);
    }
}