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
    private final Integer _order;
    private final int _min;
    private final int _max;
    private final Severity _severity;

    private OccurrenceConfig(Builder builder) {
        this._order = builder._order;
        this._min = builder._min;
        this._max = builder._max;
        this._severity = builder._severity;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this._order;
    }

    @JsonProperty(MIN)
    public int min() {
        return this._min;
    }

    @JsonProperty(MAX)
    public int max() {
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
        private Integer _order;
        private int _min; // Default value 0 will be set by constructor if needed
        private int _max = Integer.MAX_VALUE;
        private Severity _severity;

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this._order = order;
            return this;
        }

        @JsonProperty(MIN)
        public Builder min(int min) {
            this._min = min;
            return this;
        }

        @JsonProperty(MAX)
        public Builder max(int max) {
            this._max = max;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this._severity = severity;
            return this;
        }

        public OccurrenceConfig build() {
            return new OccurrenceConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OccurrenceConfig that = (OccurrenceConfig) o;
        return _min == that._min && _max == that._max && Objects.equals(_order, that._order)
                && _severity == that._severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_order, _min, _max, _severity);
    }
}
