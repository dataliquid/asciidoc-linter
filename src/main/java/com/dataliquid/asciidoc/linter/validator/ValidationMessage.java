package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.dataliquid.asciidoc.linter.config.Severity;

public final class ValidationMessage {
    private final Severity severity;
    private final String ruleId;
    private final String message;
    private final SourceLocation location;
    private final String attributeName;
    private final String actualValue;
    private final String expectedValue;
    
    // Enhanced fields for improved console output
    private final ErrorType errorType;
    private final String missingValueHint;
    private final String placeholderPrefix;
    private final List<Suggestion> suggestions;
    private final List<String> contextLines;
    private final Throwable cause;

    private ValidationMessage(Builder builder) {
        this.severity = Objects.requireNonNull(builder.severity, "[" + getClass().getName() + "] severity must not be null");
        this.ruleId = Objects.requireNonNull(builder.ruleId, "[" + getClass().getName() + "] ruleId must not be null");
        this.message = Objects.requireNonNull(builder.message, "[" + getClass().getName() + "] message must not be null");
        this.location = Objects.requireNonNull(builder.location, "[" + getClass().getName() + "] location must not be null");
        this.attributeName = builder.attributeName;
        this.actualValue = builder.actualValue;
        this.expectedValue = builder.expectedValue;
        this.errorType = builder.errorType != null ? builder.errorType : ErrorType.GENERIC;
        this.missingValueHint = builder.missingValueHint;
        this.placeholderPrefix = builder.placeholderPrefix;
        this.suggestions = new ArrayList<>(builder.suggestions);
        this.contextLines = new ArrayList<>(builder.contextLines);
        this.cause = builder.cause;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getMessage() {
        return message;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public Optional<String> getAttributeName() {
        return Optional.ofNullable(attributeName);
    }

    public Optional<String> getActualValue() {
        return Optional.ofNullable(actualValue);
    }

    public Optional<String> getExpectedValue() {
        return Optional.ofNullable(expectedValue);
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getMissingValueHint() {
        return missingValueHint;
    }
    
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }
    
    public List<Suggestion> getSuggestions() {
        return new ArrayList<>(suggestions);
    }
    
    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }
    
    public boolean hasAutoFixableSuggestions() {
        return suggestions.stream().anyMatch(Suggestion::isAutoFixable);
    }
    
    public List<String> getContextLines() {
        return new ArrayList<>(contextLines);
    }
    
    public Optional<Throwable> getCause() {
        return Optional.ofNullable(cause);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(location.formatLocation())
          .append(": [")
          .append(severity)
          .append("] ")
          .append(message);
        
        if (actualValue != null || expectedValue != null) {
            sb.append("\n");
            if (actualValue != null) {
                sb.append("  Found: \"").append(actualValue).append("\"");
            }
            if (expectedValue != null) {
                if (actualValue != null) {
                    sb.append("\n");
                }
                sb.append("  Expected: ").append(expectedValue);
            }
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationMessage that = (ValidationMessage) o;
        return severity == that.severity &&
                Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(location, that.location) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(actualValue, that.actualValue) &&
                Objects.equals(expectedValue, that.expectedValue) &&
                errorType == that.errorType &&
                Objects.equals(missingValueHint, that.missingValueHint) &&
                Objects.equals(placeholderPrefix, that.placeholderPrefix) &&
                Objects.equals(suggestions, that.suggestions) &&
                Objects.equals(contextLines, that.contextLines) &&
                Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, ruleId, message, location, attributeName, actualValue, 
                          expectedValue, errorType, missingValueHint, placeholderPrefix, suggestions, contextLines, cause);
    }

    @Override
    public String toString() {
        return format();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Severity severity;
        private String ruleId;
        private String message;
        private SourceLocation location;
        private String attributeName;
        private String actualValue;
        private String expectedValue;
        private ErrorType errorType;
        private String missingValueHint;
        private String placeholderPrefix;
        private final List<Suggestion> suggestions = new ArrayList<>();
        private final List<String> contextLines = new ArrayList<>();
        private Throwable cause;

        private Builder() {
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder location(SourceLocation location) {
            this.location = location;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder actualValue(String actualValue) {
            this.actualValue = actualValue;
            return this;
        }

        public Builder expectedValue(String expectedValue) {
            this.expectedValue = expectedValue;
            return this;
        }
        
        public Builder errorType(ErrorType errorType) {
            this.errorType = errorType;
            return this;
        }
        
        public Builder missingValueHint(String missingValueHint) {
            this.missingValueHint = missingValueHint;
            return this;
        }
        
        public Builder placeholderPrefix(String placeholderPrefix) {
            this.placeholderPrefix = placeholderPrefix;
            return this;
        }
        
        public Builder addSuggestion(Suggestion suggestion) {
            if (suggestion != null) {
                this.suggestions.add(suggestion);
            }
            return this;
        }
        
        public Builder suggestions(List<Suggestion> suggestions) {
            this.suggestions.clear();
            if (suggestions != null) {
                this.suggestions.addAll(suggestions);
            }
            return this;
        }
        
        public Builder addContextLine(String line) {
            if (line != null) {
                this.contextLines.add(line);
            }
            return this;
        }
        
        public Builder contextLines(List<String> contextLines) {
            this.contextLines.clear();
            if (contextLines != null) {
                this.contextLines.addAll(contextLines);
            }
            return this;
        }
        
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public ValidationMessage build() {
            return new ValidationMessage(this);
        }
    }
}