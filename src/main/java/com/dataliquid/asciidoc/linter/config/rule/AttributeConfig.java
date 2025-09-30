package com.dataliquid.asciidoc.linter.config.rule;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;

public final class AttributeConfig {
    private final String nameValue;
    private final Integer orderValue;
    private final boolean requiredValue;
    private final Integer minLengthValue;
    private final Integer maxLengthValue;
    private final String patternValue;
    private final Severity severityValue;

    @JsonCreator
    public AttributeConfig(@JsonProperty(NAME) String name, @JsonProperty(ORDER) Integer order,
            @JsonProperty(REQUIRED) Boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
            @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(PATTERN) String pattern,
            @JsonProperty(SEVERITY) Severity severity) {
        this.nameValue = Objects.requireNonNull(name, "name is required");
        this.orderValue = order;
        this.requiredValue = required != null ? required : false;
        this.minLengthValue = minLength;
        this.maxLengthValue = maxLength;
        this.patternValue = pattern;
        this.severityValue = Objects.requireNonNull(severity, "severity is required");
    }

    @JsonProperty(NAME)
    public String name() {
        return this.nameValue;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this.orderValue;
    }

    @JsonProperty(REQUIRED)
    public boolean required() {
        return this.requiredValue;
    }

    @JsonProperty(MIN_LENGTH)
    public Integer minLength() {
        return this.minLengthValue;
    }

    @JsonProperty(MAX_LENGTH)
    public Integer maxLength() {
        return this.maxLengthValue;
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
        AttributeConfig that = (AttributeConfig) o;
        return requiredValue == that.requiredValue && Objects.equals(nameValue, that.nameValue)
                && Objects.equals(orderValue, that.orderValue) && Objects.equals(minLengthValue, that.minLengthValue)
                && Objects.equals(maxLengthValue, that.maxLengthValue)
                && Objects.equals(patternValue, that.patternValue) && severityValue == that.severityValue;
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(nameValue, orderValue, requiredValue, minLengthValue, maxLengthValue, patternValue,
                        severityValue);
    }
}
