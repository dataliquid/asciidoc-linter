package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.DISPLAY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.ERROR_GROUPING;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.FORMAT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SUGGESTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SUMMARY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Root configuration for console output formatting. This configuration is
 * loaded from a separate YAML file and controls how validation results are
 * displayed to the user.
 */
@JsonDeserialize(builder = OutputConfiguration.Builder.class)
public final class OutputConfiguration {
    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.ENHANCED;

    private final OutputFormat _format;
    private final DisplayConfig _display;
    private final SuggestionsConfig _suggestions;
    private final ErrorGroupingConfig _errorGrouping;
    private final SummaryConfig _summary;

    private OutputConfiguration(Builder builder) {
        this._format = Objects
                .requireNonNull(builder._format, "[" + getClass().getName() + "] format must not be null");
        this._display = Objects
                .requireNonNull(builder._display, "[" + getClass().getName() + "] display must not be null");
        this._suggestions = Objects
                .requireNonNull(builder._suggestions, "[" + getClass().getName() + "] suggestions must not be null");
        this._errorGrouping = Objects
                .requireNonNull(builder._errorGrouping,
                        "[" + getClass().getName() + "] errorGrouping must not be null");
        this._summary = Objects
                .requireNonNull(builder._summary, "[" + getClass().getName() + "] summary must not be null");
    }

    public OutputFormat getFormat() {
        return this._format;
    }

    public DisplayConfig getDisplay() {
        return this._display;
    }

    public SuggestionsConfig getSuggestions() {
        return this._suggestions;
    }

    public ErrorGroupingConfig getErrorGrouping() {
        return this._errorGrouping;
    }

    public SummaryConfig getSummary() {
        return this._summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OutputConfiguration that = (OutputConfiguration) o;
        return _format == that._format && Objects.equals(_display, that._display)
                && Objects.equals(_suggestions, that._suggestions)
                && Objects.equals(_errorGrouping, that._errorGrouping) && Objects.equals(_summary, that._summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_format, _display, _suggestions, _errorGrouping, _summary);
    }

    /**
     * Creates a default output configuration with enhanced format.
     */
    public static OutputConfiguration defaultConfig() {
        return builder().build();
    }

    /**
     * Creates a compact output configuration for CI/CD environments.
     */
    public static OutputConfiguration compactConfig() {
        return builder()
                .format(OutputFormat.COMPACT)
                .display(DisplayConfig.builder().contextLines(0).useColors(false).showHeader(false).build())
                .suggestions(SuggestionsConfig.builder().enabled(false).build())
                .errorGrouping(ErrorGroupingConfig.builder().enabled(false).build())
                .summary(SummaryConfig.builder().enabled(false).build())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static final class Builder {
        private OutputFormat _format = DEFAULT_FORMAT;
        private DisplayConfig _display = DisplayConfig.builder().build();
        private SuggestionsConfig _suggestions = SuggestionsConfig.builder().build();
        private ErrorGroupingConfig _errorGrouping = ErrorGroupingConfig.builder().build();
        private SummaryConfig _summary = SummaryConfig.builder().build();

        private Builder() {
        }

        @JsonProperty(FORMAT)
        public Builder format(OutputFormat format) {
            this._format = format != null ? format : DEFAULT_FORMAT;
            return this;
        }

        @JsonProperty(DISPLAY)
        public Builder display(DisplayConfig display) {
            this._display = display != null ? display : DisplayConfig.builder().build();
            return this;
        }

        @JsonProperty(SUGGESTIONS)
        public Builder suggestions(SuggestionsConfig suggestions) {
            this._suggestions = suggestions != null ? suggestions : SuggestionsConfig.builder().build();
            return this;
        }

        @JsonProperty(ERROR_GROUPING)
        public Builder errorGrouping(ErrorGroupingConfig errorGrouping) {
            this._errorGrouping = errorGrouping != null ? errorGrouping : ErrorGroupingConfig.builder().build();
            return this;
        }

        @JsonProperty(SUMMARY)
        public Builder summary(SummaryConfig summary) {
            this._summary = summary != null ? summary : SummaryConfig.builder().build();
            return this;
        }

        public OutputConfiguration build() {
            return new OutputConfiguration(this);
        }
    }
}
