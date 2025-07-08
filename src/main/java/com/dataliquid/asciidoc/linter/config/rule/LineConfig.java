package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = LineConfig.Builder.class)
public final class LineConfig {
    private final Integer min;
    private final Integer max;
    private final Severity severity;

    private LineConfig(Builder builder) {
        this.min = builder.min;
        this.max = builder.max;
        this.severity = builder.severity;
    }

    @JsonProperty("min")
    public Integer min() { return min; }
    
    @JsonProperty("max")
    public Integer max() { return max; }
    
    @JsonProperty("severity")
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Integer min;
        private Integer max;
        private Severity severity;

        @JsonProperty("min")
        public Builder min(Integer min) {
            this.min = min;
            return this;
        }

        @JsonProperty("max")
        public Builder max(Integer max) {
            this.max = max;
            return this;
        }

        @JsonProperty("severity")
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public LineConfig build() {
            return new LineConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineConfig lineRule = (LineConfig) o;
        return Objects.equals(min, lineRule.min) &&
               Objects.equals(max, lineRule.max) &&
               severity == lineRule.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, severity);
    }
}