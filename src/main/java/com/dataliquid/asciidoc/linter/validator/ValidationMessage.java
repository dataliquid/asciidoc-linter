package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.dataliquid.asciidoc.linter.config.common.Severity;

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
    private final PlaceholderContext placeholderContext;
    private final List<Suggestion> suggestions;
    private final List<String> contextLines;
    private final Throwable cause;

    private ValidationMessage(Builder builder) {
        this.severity = Objects
                .requireNonNull(builder._severity, "[" + getClass().getName() + "] severity must not be null");
        this.ruleId = Objects.requireNonNull(builder._ruleId, "[" + getClass().getName() + "] ruleId must not be null");
        this.message = Objects
                .requireNonNull(builder._message, "[" + getClass().getName() + "] message must not be null");
        this.location = Objects
                .requireNonNull(builder._location, "[" + getClass().getName() + "] location must not be null");
        this.attributeName = builder._attributeName;
        this.actualValue = builder._actualValue;
        this.expectedValue = builder._expectedValue;
        this.errorType = builder._errorType != null ? builder._errorType : ErrorType.GENERIC;
        this.missingValueHint = builder._missingValueHint;
        this.placeholderContext = builder._placeholderContext;
        this.suggestions = new ArrayList<>(builder._suggestions);
        this.contextLines = new ArrayList<>(builder._contextLines);
        this.cause = builder._cause;
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

    public PlaceholderContext getPlaceholderContext() {
        return placeholderContext;
    }

    public List<Suggestion> getSuggestions() {
        return new ArrayList<>(suggestions);
    }

    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }

    public List<String> getContextLines() {
        return new ArrayList<>(contextLines);
    }

    public Optional<Throwable> getCause() {
        return Optional.ofNullable(cause);
    }

    public String format() {
        StringBuilder sb = new StringBuilder(100); // Increased buffer size
        sb.append(location.formatLocation()).append(": [").append(severity).append("] ").append(message);

        if (actualValue != null || expectedValue != null) {
            sb.append('\n');
            if (actualValue != null) {
                sb.append("  Found: \"").append(actualValue).append('\"');
            }
            if (expectedValue != null) {
                if (actualValue != null) {
                    sb.append('\n');
                }
                sb.append("  Expected: ").append(expectedValue);
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ValidationMessage that = (ValidationMessage) o;
        return severity == that.severity && Objects.equals(ruleId, that.ruleId) && Objects.equals(message, that.message)
                && Objects.equals(location, that.location) && Objects.equals(attributeName, that.attributeName)
                && Objects.equals(actualValue, that.actualValue) && Objects.equals(expectedValue, that.expectedValue)
                && errorType == that.errorType && Objects.equals(missingValueHint, that.missingValueHint)
                && Objects.equals(placeholderContext, that.placeholderContext)
                && Objects.equals(suggestions, that.suggestions) && Objects.equals(contextLines, that.contextLines)
                && Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(severity, ruleId, message, location, attributeName, actualValue, expectedValue, errorType,
                        missingValueHint, placeholderContext, suggestions, contextLines, cause);
    }

    @Override
    public String toString() {
        return format();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Severity _severity;
        private String _ruleId;
        private String _message;
        private SourceLocation _location;
        private String _attributeName;
        private String _actualValue;
        private String _expectedValue;
        private ErrorType _errorType;
        private String _missingValueHint;
        private PlaceholderContext _placeholderContext;
        private final List<Suggestion> _suggestions = new ArrayList<>();
        private final List<String> _contextLines = new ArrayList<>();
        private Throwable _cause;

        private Builder() {
        }

        public Builder severity(Severity severity) {
            this._severity = severity;
            return this;
        }

        public Builder ruleId(String ruleId) {
            this._ruleId = ruleId;
            return this;
        }

        public Builder message(String message) {
            this._message = message;
            return this;
        }

        public Builder location(SourceLocation location) {
            this._location = location;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this._attributeName = attributeName;
            return this;
        }

        public Builder actualValue(String actualValue) {
            this._actualValue = actualValue;
            return this;
        }

        public Builder expectedValue(String expectedValue) {
            this._expectedValue = expectedValue;
            return this;
        }

        public Builder errorType(ErrorType errorType) {
            this._errorType = errorType;
            return this;
        }

        public Builder missingValueHint(String missingValueHint) {
            this._missingValueHint = missingValueHint;
            return this;
        }

        public Builder placeholderContext(PlaceholderContext placeholderContext) {
            this._placeholderContext = placeholderContext;
            return this;
        }

        public Builder addSuggestion(Suggestion suggestion) {
            if (suggestion != null) {
                this._suggestions.add(suggestion);
            }
            return this;
        }

        public Builder suggestions(List<Suggestion> suggestions) {
            this._suggestions.clear();
            if (suggestions != null) {
                this._suggestions.addAll(suggestions);
            }
            return this;
        }

        public Builder addContextLine(String line) {
            if (line != null) {
                this._contextLines.add(line);
            }
            return this;
        }

        public Builder contextLines(List<String> contextLines) {
            this._contextLines.clear();
            if (contextLines != null) {
                this._contextLines.addAll(contextLines);
            }
            return this;
        }

        public Builder cause(Throwable cause) {
            this._cause = cause;
            return this;
        }

        public ValidationMessage build() {
            return new ValidationMessage(this);
        }
    }
}
