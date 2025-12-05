package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;

public final class OccurrenceConfig {
    private final Integer orderValue;
    private final int minValue;
    private final int maxValue;
    private final Severity severityValue;

    @JsonCreator
    public OccurrenceConfig(@JsonProperty(ORDER) Integer order, @JsonProperty(MIN) Integer min,
            @JsonProperty(MAX) Integer max, @JsonProperty(SEVERITY) Severity severity) {
        this.orderValue = order;
        this.minValue = min != null ? min : 0;
        this.maxValue = max != null ? max : Integer.MAX_VALUE;
        this.severityValue = severity;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this.orderValue;
    }

    @JsonProperty(MIN)
    public int min() {
        return this.minValue;
    }

    @JsonProperty(MAX)
    public int max() {
        return this.maxValue;
    }

    @JsonProperty(SEVERITY)
    public Severity severity() {
        return this.severityValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OccurrenceConfig that = (OccurrenceConfig) o;
        return minValue == that.minValue && maxValue == that.maxValue && Objects.equals(orderValue, that.orderValue)
                && severityValue == that.severityValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderValue, minValue, maxValue, severityValue);
    }
}
