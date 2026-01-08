package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.MAX_PER_ERROR;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_EXAMPLES;

/**
 * Configuration for fix suggestions in console output.
 */
public final class SuggestionsConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_MAX_PER_ERROR = 3;
    private static final boolean DEFAULT_SHOW_EXAMPLES = true;

    private final boolean enabledValue;
    private final int maxPerErrorValue;
    private final boolean showExamplesValue;

    @JsonCreator
    public SuggestionsConfig(@JsonProperty(ENABLED) Boolean enabled, @JsonProperty(MAX_PER_ERROR) Integer maxPerError,
            @JsonProperty(SHOW_EXAMPLES) Boolean showExamples) {
        this.enabledValue = enabled != null ? enabled : DEFAULT_ENABLED;
        this.maxPerErrorValue = maxPerError != null ? maxPerError : DEFAULT_MAX_PER_ERROR;
        this.showExamplesValue = showExamples != null ? showExamples : DEFAULT_SHOW_EXAMPLES;
    }

    public boolean isEnabled() {
        return this.enabledValue;
    }

    public int getMaxPerError() {
        return this.maxPerErrorValue;
    }

    public boolean isShowExamples() {
        return this.showExamplesValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SuggestionsConfig that = (SuggestionsConfig) o;
        return enabledValue == that.enabledValue && maxPerErrorValue == that.maxPerErrorValue
                && showExamplesValue == that.showExamplesValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabledValue, maxPerErrorValue, showExamplesValue);
    }
}
