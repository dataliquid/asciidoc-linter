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
    private final String _pattern;
    private final Severity _severity;

    private TitleConfig(Builder builder) {
        this._pattern = builder._pattern;
        this._severity = builder._severity;
    }

    @JsonProperty(PATTERN)
    public String pattern() {
        return this._pattern;
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
        private String _pattern;
        private Severity _severity = Severity.ERROR;

        @JsonProperty(PATTERN)
        public Builder pattern(String pattern) {
            this._pattern = pattern;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this._severity = Objects
                    .requireNonNull(_severity, "[" + getClass().getName() + "] severity must not be null");
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
        return Objects.equals(_pattern, that._pattern) && _severity == that._severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_pattern, _severity);
    }
}
