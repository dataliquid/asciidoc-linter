package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.OUTPUT_CONFIG;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Wrapper class for the output configuration to match the YAML structure. The
 * YAML file has "output" as the root key.
 */
@JsonDeserialize(builder = OutputConfigWrapper.Builder.class)
public final class OutputConfigWrapper {
    private final OutputConfiguration output;

    private OutputConfigWrapper(Builder builder) {
        this.output = Objects.requireNonNull(builder.output, "[" + getClass().getName() + "] output must not be null");
    }

    public OutputConfiguration getOutput() {
        return this.output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OutputConfigWrapper that = (OutputConfigWrapper) o;
        return Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(output);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {
        private OutputConfiguration output;

        private Builder() {
        }

        @JsonProperty(OUTPUT_CONFIG)
        public Builder output(OutputConfiguration output) {
            this.output = output;
            return this;
        }

        public OutputConfigWrapper build() {
            return new OutputConfigWrapper(this);
        }
    }
}
