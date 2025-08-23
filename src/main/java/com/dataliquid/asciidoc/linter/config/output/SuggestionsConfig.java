package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.MAX_PER_ERROR;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_EXAMPLES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for fix suggestions in console output.
 */
@JsonDeserialize(builder = SuggestionsConfig.Builder.class)
public final class SuggestionsConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_MAX_PER_ERROR = 3;
    private static final boolean DEFAULT_SHOW_EXAMPLES = true;

    private final boolean enabled;
    private final int maxPerError;
    private final boolean showExamples;

    private SuggestionsConfig(Builder builder) {
        this.enabled = builder._enabled;
        this.maxPerError = builder._maxPerError;
        this.showExamples = builder._showExamples;
    }

    public boolean isEnabled() {
        return this._enabled;
    }

    public int getMaxPerError() {
        return this._maxPerError;
    }

    public boolean isShowExamples() {
        return this._showExamples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SuggestionsConfig that = (SuggestionsConfig) o;
        return enabled == that.enabled && maxPerError == that.maxPerError && showExamples == that.showExamples;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, maxPerError, showExamples);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {
        private boolean enabled = DEFAULT_ENABLED;
        private int maxPerError = DEFAULT_MAX_PER_ERROR;
        private boolean showExamples = DEFAULT_SHOW_EXAMPLES;

        private Builder() {
        }

        @JsonProperty(ENABLED)
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @JsonProperty(MAX_PER_ERROR)
        public Builder maxPerError(int maxPerError) {
            this.maxPerError = maxPerError;
            return this;
        }

        @JsonProperty(SHOW_EXAMPLES)
        public Builder showExamples(boolean showExamples) {
            this.showExamples = showExamples;
            return this;
        }

        public SuggestionsConfig build() {
            return new SuggestionsConfig(this);
        }
    }
}
