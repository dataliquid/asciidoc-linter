package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;

public final class TitleConfig {
    private final String patternValue;
    private final Severity severityValue;

    @JsonCreator
    public TitleConfig(@JsonProperty(PATTERN) String pattern, @JsonProperty(SEVERITY) Severity severity) {
        this.patternValue = Objects.requireNonNull(pattern, "Pattern must be specified");
        this.severityValue = severity != null ? severity : Severity.ERROR;
    }

    @JsonProperty(PATTERN)
    public String pattern() {
        return this.patternValue;
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
        TitleConfig that = (TitleConfig) o;
        return Objects.equals(patternValue, that.patternValue) && severityValue == that.severityValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternValue, severityValue);
    }
}
