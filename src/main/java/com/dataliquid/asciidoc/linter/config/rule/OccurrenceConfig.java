package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("order")
    public Integer order() { return order; }
    
    @JsonProperty("min")
    public int min() { return min; }
    
    @JsonProperty("max")
    public int max() { return max; }
    
    @JsonProperty("severity")
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Integer order;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private Severity severity;

        @JsonProperty("order")
        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        @JsonProperty("min")
        public Builder min(int min) {
            this.min = min;
            return this;
        }

        @JsonProperty("max")
        public Builder max(int max) {
            this.max = max;
            return this;
        }

        @JsonProperty("severity")
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