package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = AttributeConfig.Builder.class)
public final class AttributeConfig {
    private final String _name;
    private final Integer _order;
    private final boolean _required;
    private final Integer _minLength;
    private final Integer _maxLength;
    private final String _pattern;
    private final Severity _severity;

    private AttributeConfig(Builder builder) {
        this._name = builder._name;
        this._order = builder._order;
        this._required = builder._required;
        this._minLength = builder._minLength;
        this._maxLength = builder._maxLength;
        this._pattern = builder._pattern;
        this._severity = builder._severity;
    }

    @JsonProperty(NAME)
    public String name() {
        return this._name;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this._order;
    }

    @JsonProperty(REQUIRED)
    public boolean required() {
        return this._required;
    }

    @JsonProperty(MIN_LENGTH)
    public Integer minLength() {
        return this._minLength;
    }

    @JsonProperty(MAX_LENGTH)
    public Integer maxLength() {
        return this._maxLength;
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
        private String _name;
        private Integer _order;
        private boolean _required;
        private Integer _minLength;
        private Integer _maxLength;
        private String _pattern;
        private Severity _severity;

        @JsonProperty(NAME)
        public Builder name(String name) {
            this._name = name;
            return this;
        }

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this._order = order;
            return this;
        }

        @JsonProperty(REQUIRED)
        public Builder required(boolean required) {
            this._required = required;
            return this;
        }

        @JsonProperty(MIN_LENGTH)
        public Builder minLength(Integer minLength) {
            this._minLength = minLength;
            return this;
        }

        @JsonProperty(MAX_LENGTH)
        public Builder maxLength(Integer maxLength) {
            this._maxLength = maxLength;
            return this;
        }

        @JsonProperty(PATTERN)
        public Builder pattern(String pattern) {
            this._pattern = pattern;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this._severity = severity;
            return this;
        }

        public AttributeConfig build() {
            Objects.requireNonNull(_name, "[" + getClass().getName() + "] name is required");
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new AttributeConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AttributeConfig that = (AttributeConfig) o;
        return _required == that._required && Objects.equals(_name, that._name) && Objects.equals(_order, that._order)
                && Objects.equals(_minLength, that._minLength) && Objects.equals(_maxLength, that._maxLength)
                && Objects.equals(_pattern, that._pattern) && _severity == that._severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _order, _required, _minLength, _maxLength, _pattern, _severity);
    }
}
