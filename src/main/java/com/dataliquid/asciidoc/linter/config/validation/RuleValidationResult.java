package com.dataliquid.asciidoc.linter.config.validation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.networknt.schema.Error;

/**
 * Result of schema validation containing validation errors.
 */
public class RuleValidationResult {
    private final List<ValidationError> errors;

    private RuleValidationResult(List<ValidationError> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Creates a result from networknt validation errors.
     */
    public static RuleValidationResult from(List<Error> schemaErrors) {
        List<ValidationError> errors = schemaErrors
                .stream()
                .map(RuleValidationResult::convertError)
                .collect(Collectors.toList());

        return new RuleValidationResult(errors);
    }

    private static ValidationError convertError(Error error) {
        return new ValidationError(error.getInstanceLocation().toString(), error.getMessage(), error.getKeyword());
    }

    /**
     * Represents a single validation error.
     */
    public static class ValidationError {
        private final String path;
        private final String message;
        private final String keyword;

        public ValidationError(String path, String message, String keyword) {
            this.path = path;
            this.message = message;
            this.keyword = keyword;
        }

        public String getPath() {
            return path;
        }

        public String getMessage() {
            return message;
        }

        public String getKeyword() {
            return keyword;
        }

        public String format() {
            return String.format("  - %s: %s", path, message);
        }
    }
}
