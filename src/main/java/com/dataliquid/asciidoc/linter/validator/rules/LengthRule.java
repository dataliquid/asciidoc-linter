package com.dataliquid.asciidoc.linter.validator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Metadata.LENGTH;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Metadata.LENGTH_MIN;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Metadata.LENGTH_MAX;

public final class LengthRule implements AttributeRule {
    private final Map<String, LengthConfig> lengthConfigs;

    private LengthRule(Builder builder) {
        this.lengthConfigs = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.lengthConfigs));
    }

    @Override
    public String getRuleId() {
        return LENGTH;
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        List<ValidationMessage> messages = new ArrayList<>();

        LengthConfig config = lengthConfigs.get(attributeName);
        if (config != null && value != null) {
            int length = value.length();

            if (config.hasMinLength() && length < config.getMinLength()) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(config.getSeverity())
                                .ruleId(LENGTH_MIN)
                                .message("Attribute '" + attributeName + "' is too short: actual '" + value + "' ("
                                        + length + " characters), expected minimum " + config.getMinLength()
                                        + " characters")
                                .location(location)
                                .attributeName(attributeName)
                                .actualValue(value + " (" + length + " characters)")
                                .expectedValue("Minimum " + config.getMinLength() + " characters")
                                .errorType(ErrorType.OUT_OF_RANGE)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Provide a longer value for the attribute")
                                        .addExample("Add more descriptive content")
                                        .addExample(
                                                "Expand the value to at least " + config.getMinLength() + " characters")
                                        .explanation("Attribute value must be at least " + config.getMinLength()
                                                + " characters long")
                                        .build())
                                .build());
            }

            if (config.hasMaxLength() && length > config.getMaxLength()) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(config.getSeverity())
                                .ruleId(LENGTH_MAX)
                                .message("Attribute '" + attributeName + "' is too long: actual '" + value + "' ("
                                        + length + " characters), expected maximum " + config.getMaxLength()
                                        + " characters")
                                .location(location)
                                .attributeName(attributeName)
                                .actualValue(value + " (" + length + " characters)")
                                .expectedValue("Maximum " + config.getMaxLength() + " characters")
                                .errorType(ErrorType.OUT_OF_RANGE)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Shorten the attribute value")
                                        .addExample("Reduce to " + config.getMaxLength() + " characters or less")
                                        .addExample("Use more concise wording")
                                        .addExample("Remove unnecessary details")
                                        .explanation("Attribute value must not exceed " + config.getMaxLength()
                                                + " characters")
                                        .build())
                                .build());
            }
        }

        return messages;
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return lengthConfigs.containsKey(attributeName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, LengthConfig> lengthConfigs = new ConcurrentHashMap<>();

        private Builder() {
        }

        public Builder addLengthConstraint(String attributeName, Integer minLength, Integer maxLength,
                Severity severity) {
            Objects.requireNonNull(attributeName, "[" + getClass().getName() + "] attributeName must not be null");
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");

            if (minLength == null && maxLength == null) {
                throw new IllegalArgumentException("At least one of minLength or maxLength must be specified");
            }

            if (minLength != null && minLength < 0) {
                throw new IllegalArgumentException("minLength must be non-negative");
            }

            if (maxLength != null && maxLength < 1) {
                throw new IllegalArgumentException("maxLength must be positive");
            }

            if (minLength != null && maxLength != null && minLength > maxLength) {
                throw new IllegalArgumentException("minLength cannot be greater than maxLength");
            }

            lengthConfigs.put(attributeName, new LengthConfig(minLength, maxLength, severity));
            return this;
        }

        public LengthRule build() {
            return new LengthRule(this);
        }
    }

    private static final class LengthConfig {
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        LengthConfig(Integer minLength, Integer maxLength, Severity severity) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        boolean hasMinLength() {
            return minLength != null;
        }

        boolean hasMaxLength() {
            return maxLength != null;
        }

        int getMinLength() {
            return minLength != null ? minLength : 0;
        }

        int getMaxLength() {
            return maxLength != null ? maxLength : Integer.MAX_VALUE;
        }

        Severity getSeverity() {
            return severity;
        }
    }
}
