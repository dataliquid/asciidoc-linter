package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;

public final class LineConfig {
    private final Integer minValue;
    private final Integer maxValue;
    private final Severity severityValue;

    @JsonCreator
    public LineConfig(@JsonProperty(MIN) Integer min, @JsonProperty(MAX) Integer max,
            @JsonProperty(SEVERITY) Severity severity) {
        this.minValue = min;
        this.maxValue = max;
        this.severityValue = severity;
    }

    @JsonProperty(MIN)
    public Integer min() {
        return this.minValue;
    }

    @JsonProperty(MAX)
    public Integer max() {
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
        LineConfig lineRule = (LineConfig) o;
        return Objects.equals(min(), lineRule.min()) && Objects.equals(max(), lineRule.max())
                && severity() == lineRule.severity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue, severityValue);
    }
}
