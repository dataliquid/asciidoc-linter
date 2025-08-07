package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.THRESHOLD;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for grouping similar errors in console output.
 */
@JsonDeserialize(builder = ErrorGroupingConfig.Builder.class)
public final class ErrorGroupingConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_THRESHOLD = 3;
    
    private final boolean enabled;
    private final int threshold;
    
    private ErrorGroupingConfig(Builder builder) {
        this.enabled = builder.enabled;
        this.threshold = builder.threshold;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getThreshold() {
        return threshold;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorGroupingConfig that = (ErrorGroupingConfig) o;
        return enabled == that.enabled && threshold == that.threshold;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enabled, threshold);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static final class Builder {
        private boolean enabled = DEFAULT_ENABLED;
        private int threshold = DEFAULT_THRESHOLD;
        
        private Builder() {
        }
        
        @JsonProperty(ENABLED)
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        @JsonProperty(THRESHOLD)
        public Builder threshold(int threshold) {
            this.threshold = threshold;
            return this;
        }
        
        public ErrorGroupingConfig build() {
            return new ErrorGroupingConfig(this);
        }
    }
}