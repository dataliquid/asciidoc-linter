package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = TitleConfig.Builder.class)
public final class TitleConfig {
    private final String pattern;
    private final Severity severity;

    private TitleConfig(Builder builder) {
        this.pattern = builder.pattern;
        this.severity = builder.severity;
    }

    @JsonProperty(PATTERN)
    public String pattern() {
        return pattern;
    }

    @JsonProperty(SEVERITY)
    public Severity severity() {
        return severity;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private String pattern;
        private Severity severity = Severity.ERROR;

        @JsonProperty(PATTERN)
        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this.severity = Objects.requireNonNull(severity,
                    "[" + getClass().getName() + "] severity must not be null");
            return this;
        }

        public TitleConfig build() {
            if (pattern == null) {
                throw new IllegalStateException("Pattern must be specified");
            }
            return new TitleConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TitleConfig that = (TitleConfig) o;
        return Objects.equals(pattern, that.pattern) && severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, severity);
    }
}
