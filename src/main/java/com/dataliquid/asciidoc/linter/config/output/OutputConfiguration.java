package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Output.DISPLAY;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Output.ERROR_GROUPING;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Output.FORMAT;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Output.SUGGESTIONS;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Output.SUMMARY;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Root configuration for console output formatting.
 * This configuration is loaded from a separate YAML file and controls
 * how validation results are displayed to the user.
 */
@JsonDeserialize(builder = OutputConfiguration.Builder.class)
public final class OutputConfiguration {
    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.ENHANCED;
    
    private final OutputFormat format;
    private final DisplayConfig display;
    private final SuggestionsConfig suggestions;
    private final ErrorGroupingConfig errorGrouping;
    private final SummaryConfig summary;
    
    private OutputConfiguration(Builder builder) {
        this.format = Objects.requireNonNull(builder.format, "[" + getClass().getName() + "] format must not be null");
        this.display = Objects.requireNonNull(builder.display, "[" + getClass().getName() + "] display must not be null");
        this.suggestions = Objects.requireNonNull(builder.suggestions, "[" + getClass().getName() + "] suggestions must not be null");
        this.errorGrouping = Objects.requireNonNull(builder.errorGrouping, "[" + getClass().getName() + "] errorGrouping must not be null");
        this.summary = Objects.requireNonNull(builder.summary, "[" + getClass().getName() + "] summary must not be null");
    }
    
    public OutputFormat getFormat() {
        return format;
    }
    
    public DisplayConfig getDisplay() {
        return display;
    }
    
    public SuggestionsConfig getSuggestions() {
        return suggestions;
    }
    
    public ErrorGroupingConfig getErrorGrouping() {
        return errorGrouping;
    }
    
    public SummaryConfig getSummary() {
        return summary;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputConfiguration that = (OutputConfiguration) o;
        return format == that.format &&
                Objects.equals(display, that.display) &&
                Objects.equals(suggestions, that.suggestions) &&
                Objects.equals(errorGrouping, that.errorGrouping) &&
                Objects.equals(summary, that.summary);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(format, display, suggestions, errorGrouping, summary);
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
            .display(DisplayConfig.builder()
                .contextLines(0)
                .useColors(false)
                .showHeader(false)
                .build())
            .suggestions(SuggestionsConfig.builder()
                .enabled(false)
                .build())
            .errorGrouping(ErrorGroupingConfig.builder()
                .enabled(false)
                .build())
            .summary(SummaryConfig.builder()
                .enabled(false)
                .build())
            .build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static final class Builder {
        private OutputFormat format = DEFAULT_FORMAT;
        private DisplayConfig display = DisplayConfig.builder().build();
        private SuggestionsConfig suggestions = SuggestionsConfig.builder().build();
        private ErrorGroupingConfig errorGrouping = ErrorGroupingConfig.builder().build();
        private SummaryConfig summary = SummaryConfig.builder().build();
        
        private Builder() {
        }
        
        @JsonProperty(FORMAT)
        public Builder format(OutputFormat format) {
            this.format = format != null ? format : DEFAULT_FORMAT;
            return this;
        }
        
        @JsonProperty(DISPLAY)
        public Builder display(DisplayConfig display) {
            this.display = display != null ? display : DisplayConfig.builder().build();
            return this;
        }
        
        @JsonProperty(SUGGESTIONS)
        public Builder suggestions(SuggestionsConfig suggestions) {
            this.suggestions = suggestions != null ? suggestions : SuggestionsConfig.builder().build();
            return this;
        }
        
        @JsonProperty(ERROR_GROUPING)
        public Builder errorGrouping(ErrorGroupingConfig errorGrouping) {
            this.errorGrouping = errorGrouping != null ? errorGrouping : ErrorGroupingConfig.builder().build();
            return this;
        }
        
        @JsonProperty(SUMMARY)
        public Builder summary(SummaryConfig summary) {
            this.summary = summary != null ? summary : SummaryConfig.builder().build();
            return this;
        }
        
        public OutputConfiguration build() {
            return new OutputConfiguration(this);
        }
    }
}