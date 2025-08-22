package com.dataliquid.asciidoc.linter.validator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Metadata.PATTERN;

public final class PatternRule implements AttributeRule {
    private final Map<String, PatternConfig> patternConfigs;

    private PatternRule(Builder builder) {
        this.patternConfigs = Collections.unmodifiableMap(new HashMap<>(builder.patternConfigs));
    }

    @Override
    public String getRuleId() {
        return PATTERN;
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        List<ValidationMessage> messages = new ArrayList<>();

        PatternConfig config = patternConfigs.get(attributeName);
        if (config != null && value != null && !value.isEmpty()) {
            if (!config.getPattern().matcher(value).matches()) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(config.getSeverity())
                                .ruleId(getRuleId())
                                .message("Attribute '" + attributeName + "' does not match required pattern: actual '"
                                        + value + "', expected pattern '" + config.getPatternString() + "'")
                                .location(location)
                                .attributeName(attributeName)
                                .actualValue(value)
                                .expectedValue("Pattern '" + config.getPatternString() + "'")
                                .errorType(ErrorType.INVALID_PATTERN)
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Format attribute value to match pattern")
                                        .addExample(":" + attributeName + ": [value matching pattern]")
                                        .addExample("Check pattern: " + config.getPatternString())
                                        .explanation(
                                                "Attribute value must match the configured regex pattern for validation")
                                        .build())
                                .build());
            }
        }

        return messages;
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return patternConfigs.containsKey(attributeName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, PatternConfig> patternConfigs = new HashMap<>();

        private Builder() {
        }

        public Builder addPattern(String attributeName, String pattern, Severity severity) {
            Objects.requireNonNull(attributeName, "[" + getClass().getName() + "] attributeName must not be null");
            Objects.requireNonNull(pattern, "[" + getClass().getName() + "] pattern must not be null");
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");

            try {
                Pattern compiledPattern = Pattern.compile(pattern);
                patternConfigs.put(attributeName, new PatternConfig(compiledPattern, pattern, severity));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(
                        "Invalid pattern for attribute '" + attributeName + "': " + e.getMessage());
            }

            return this;
        }

        public PatternRule build() {
            return new PatternRule(this);
        }
    }

    private static final class PatternConfig {
        private final Pattern pattern;
        private final String patternString;
        private final Severity severity;

        PatternConfig(Pattern pattern, String patternString, Severity severity) {
            this.pattern = pattern;
            this.patternString = patternString;
            this.severity = severity;
        }

        Pattern getPattern() {
            return pattern;
        }

        String getPatternString() {
            return patternString;
        }

        Severity getSeverity() {
            return severity;
        }
    }
}
