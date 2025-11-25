package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = LineConfig.Builder.class)
public final class LineConfig {
    private final Integer _min;
    private final Integer _max;
    private final Severity _severity;

    private LineConfig(Builder builder) {
        this._min = builder._min;
        this._max = builder._max;
        this._severity = builder._severity;
    }

    @JsonProperty(MIN)
    public Integer min() {
        return this._min;
    }

    @JsonProperty(MAX)
    public Integer max() {
        return this._max;
    }

    @JsonProperty(SEVERITY)
    public Severity severity() {
        return this._severity;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private Integer _min;
        private Integer _max;
        private Severity _severity;

        @JsonProperty(MIN)
        public Builder min(Integer min) {
            this._min = min;
            return this;
        }

        @JsonProperty(MAX)
        public Builder max(Integer max) {
            this._max = max;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this._severity = severity;
            return this;
        }

        public LineConfig build() {
            return new LineConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LineConfig lineRule = (LineConfig) o;
        return Objects.equals(min(), lineRule.min()) && Objects.equals(max(), lineRule.max())
                && severity() == lineRule.severity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(_min, _max, _severity);
    }
}
