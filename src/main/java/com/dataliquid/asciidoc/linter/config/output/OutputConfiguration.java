package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.DISPLAY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.ERROR_GROUPING;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.FORMAT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SUGGESTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SUMMARY;

/**
 * Root configuration for console output formatting. This configuration is
 * loaded from a separate YAML file and controls how validation results are
 * displayed to the user.
 */
public final class OutputConfiguration {
    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.ENHANCED;

    private final OutputFormat formatValue;
    private final DisplayConfig displayValue;
    private final SuggestionsConfig suggestionsValue;
    private final ErrorGroupingConfig errorGroupingValue;
    private final SummaryConfig summaryValue;

    @JsonCreator
    public OutputConfiguration(@JsonProperty(FORMAT) OutputFormat format, @JsonProperty(DISPLAY) DisplayConfig display,
            @JsonProperty(SUGGESTIONS) SuggestionsConfig suggestions,
            @JsonProperty(ERROR_GROUPING) ErrorGroupingConfig errorGrouping,
            @JsonProperty(SUMMARY) SummaryConfig summary) {
        this.formatValue = format != null ? format : DEFAULT_FORMAT;
        this.displayValue = display != null ? display : new DisplayConfig(null, null, null, null, null, null);
        this.suggestionsValue = suggestions != null ? suggestions : new SuggestionsConfig(null, null, null);
        this.errorGroupingValue = errorGrouping != null ? errorGrouping : new ErrorGroupingConfig(null, null);
        this.summaryValue = summary != null ? summary : new SummaryConfig(null, null, null, null);

        Objects.requireNonNull(this.formatValue, "[" + getClass().getName() + "] format must not be null");
        Objects.requireNonNull(this.displayValue, "[" + getClass().getName() + "] display must not be null");
        Objects.requireNonNull(this.suggestionsValue, "[" + getClass().getName() + "] suggestions must not be null");
        Objects
                .requireNonNull(this.errorGroupingValue,
                        "[" + getClass().getName() + "] errorGrouping must not be null");
        Objects.requireNonNull(this.summaryValue, "[" + getClass().getName() + "] summary must not be null");
    }

    @JsonProperty(FORMAT)
    public OutputFormat getFormat() {
        return this.formatValue;
    }

    @JsonProperty(DISPLAY)
    public DisplayConfig getDisplay() {
        return this.displayValue;
    }

    @JsonProperty(SUGGESTIONS)
    public SuggestionsConfig getSuggestions() {
        return this.suggestionsValue;
    }

    @JsonProperty(ERROR_GROUPING)
    public ErrorGroupingConfig getErrorGrouping() {
        return this.errorGroupingValue;
    }

    @JsonProperty(SUMMARY)
    public SummaryConfig getSummary() {
        return this.summaryValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OutputConfiguration that = (OutputConfiguration) o;
        return formatValue == that.formatValue && Objects.equals(displayValue, that.displayValue)
                && Objects.equals(suggestionsValue, that.suggestionsValue)
                && Objects.equals(errorGroupingValue, that.errorGroupingValue)
                && Objects.equals(summaryValue, that.summaryValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatValue, displayValue, suggestionsValue, errorGroupingValue, summaryValue);
    }

    /**
     * Creates a default output configuration with enhanced format.
     */
    public static OutputConfiguration defaultConfig() {
        return new OutputConfiguration(null, null, null, null, null);
    }

    /**
     * Creates a compact output configuration for CI/CD environments.
     */
    public static OutputConfiguration compactConfig() {
        return new OutputConfiguration(OutputFormat.COMPACT, new DisplayConfig(0, null, false, null, null, false),
                new SuggestionsConfig(false, null, null), new ErrorGroupingConfig(false, null),
                new SummaryConfig(false, null, null, null));
    }
}