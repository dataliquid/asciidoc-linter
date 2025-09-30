package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.THRESHOLD;

/**
 * Configuration for grouping similar errors in console output.
 */
public final class ErrorGroupingConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_THRESHOLD = 3;

    private final boolean enabledValue;
    private final int thresholdValue;

    @JsonCreator
    public ErrorGroupingConfig(@JsonProperty(ENABLED) Boolean enabled, @JsonProperty(THRESHOLD) Integer threshold) {
        this.enabledValue = enabled != null ? enabled : DEFAULT_ENABLED;
        this.thresholdValue = threshold != null ? threshold : DEFAULT_THRESHOLD;
    }

    public boolean isEnabled() {
        return this.enabledValue;
    }

    public int getThreshold() {
        return this.thresholdValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ErrorGroupingConfig that = (ErrorGroupingConfig) o;
        return enabledValue == that.enabledValue && thresholdValue == that.thresholdValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabledValue, thresholdValue);
    }
}