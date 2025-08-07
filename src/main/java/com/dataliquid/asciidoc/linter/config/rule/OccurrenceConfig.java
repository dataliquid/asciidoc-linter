package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = OccurrenceConfig.Builder.class)
public final class OccurrenceConfig {
    private final Integer order;
    private final int min;
    private final int max;
    private final Severity severity;

    private OccurrenceConfig(Builder builder) {
        this.order = builder.order;
        this.min = builder.min;
        this.max = builder.max;
        this.severity = builder.severity;
    }

    @JsonProperty(ORDER)
    public Integer order() { return order; }
    
    @JsonProperty(MIN)
    public int min() { return min; }
    
    @JsonProperty(MAX)
    public int max() { return max; }
    
    @JsonProperty(SEVERITY)
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private Integer order;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private Severity severity;

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        @JsonProperty(MIN)
        public Builder min(int min) {
            this.min = min;
            return this;
        }

        @JsonProperty(MAX)
        public Builder max(int max) {
            this.max = max;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public OccurrenceConfig build() {
            return new OccurrenceConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OccurrenceConfig that = (OccurrenceConfig) o;
        return min == that.min &&
               max == that.max &&
               Objects.equals(order, that.order) &&
               severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, min, max, severity);
    }
}