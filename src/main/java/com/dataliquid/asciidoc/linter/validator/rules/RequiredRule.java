package com.dataliquid.asciidoc.linter.validator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

public final class RequiredRule implements AttributeRule {
    private final Map<String, RequiredAttribute> requiredAttributes;

    private RequiredRule(Builder builder) {
        this.requiredAttributes = Collections.unmodifiableMap(new HashMap<>(builder.requiredAttributes));
    }

    @Override
    public String getRuleId() {
        return "metadata.required";
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        RequiredAttribute config = requiredAttributes.get(attributeName);
        if (config != null && config.isRequired() && value == null) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId(getRuleId())
                .message("Missing required attribute '" + attributeName + "'")
                .location(location)
                .attributeName(attributeName)
                .actualValue(null)
                .expectedValue("Attribute must be present")
                .build());
        }
        
        return messages;
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return requiredAttributes.containsKey(attributeName);
    }

    public List<ValidationMessage> validateMissingAttributes(Set<String> presentAttributes, SourceLocation documentLocation) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        for (Map.Entry<String, RequiredAttribute> entry : requiredAttributes.entrySet()) {
            String attrName = entry.getKey();
            RequiredAttribute config = entry.getValue();
            
            if (config.isRequired() && !presentAttributes.contains(attrName)) {
                messages.add(ValidationMessage.builder()
                    .severity(config.getSeverity())
                    .ruleId(getRuleId())
                    .message("Missing required attribute '" + attrName + "'")
                    .location(documentLocation)
                    .attributeName(attrName)
                    .actualValue(null)
                    .expectedValue("Attribute must be present")
                    .errorType(ErrorType.MISSING_VALUE)
                    .missingValueHint(getPlaceholderHint(attrName))
                    .build());
            }
        }
        
        return messages;
    }
    
    private String getPlaceholderHint(String attributeName) {
        return ":" + attributeName + ": value";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, RequiredAttribute> requiredAttributes = new HashMap<>();

        private Builder() {
        }

        public Builder addAttribute(String name, boolean required, Severity severity) {
            Objects.requireNonNull(name, "[" + getClass().getName() + "] name must not be null");
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
            requiredAttributes.put(name, new RequiredAttribute(required, severity));
            return this;
        }

        public RequiredRule build() {
            return new RequiredRule(this);
        }
    }

    private static final class RequiredAttribute {
        private final boolean required;
        private final Severity severity;

        RequiredAttribute(boolean required, Severity severity) {
            this.required = required;
            this.severity = severity;
        }

        boolean isRequired() {
            return required;
        }

        Severity getSeverity() {
            return severity;
        }
    }
}