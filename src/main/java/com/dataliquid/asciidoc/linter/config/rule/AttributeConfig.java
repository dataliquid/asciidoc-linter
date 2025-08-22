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
    private final String name;
    private final Integer order;
    private final boolean required;
    private final Integer minLength;
    private final Integer maxLength;
    private final String pattern;
    private final Severity severity;

    private AttributeConfig(Builder builder) {
        this.name = builder.name;
        this.order = builder.order;
        this.required = builder.required;
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.pattern = builder.pattern;
        this.severity = builder.severity;
    }

    @JsonProperty(NAME)
    public String name() {
        return name;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return order;
    }

    @JsonProperty(REQUIRED)
    public boolean required() {
        return required;
    }

    @JsonProperty(MIN_LENGTH)
    public Integer minLength() {
        return minLength;
    }

    @JsonProperty(MAX_LENGTH)
    public Integer maxLength() {
        return maxLength;
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
        private String name;
        private Integer order;
        private boolean required;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private Severity severity;

        @JsonProperty(NAME)
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        @JsonProperty(REQUIRED)
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        @JsonProperty(MIN_LENGTH)
        public Builder minLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        @JsonProperty(MAX_LENGTH)
        public Builder maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        @JsonProperty(PATTERN)
        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        @JsonProperty(SEVERITY)
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public AttributeConfig build() {
            Objects.requireNonNull(name, "[" + getClass().getName() + "] name is required");
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
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
        return required == that.required && Objects.equals(name, that.name) && Objects.equals(order, that.order)
                && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                && Objects.equals(pattern, that.pattern) && severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, required, minLength, maxLength, pattern, severity);
    }
}
