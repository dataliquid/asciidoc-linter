package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A suggestion for fixing a validation error.
 */
public final class Suggestion {
    private final String description;
    private final String fixedValue;
    private final String explanation;
    private final boolean preferred;
    private final List<String> examples;

    private Suggestion(Builder builder) {
        this.description = Objects.requireNonNull(builder.description,
                "[" + getClass().getName() + "] description must not be null");
        this.fixedValue = builder.fixedValue;
        this.explanation = builder.explanation;
        this.preferred = builder.preferred;
        this.examples = new ArrayList<>(builder.examples);
    }

    public String getDescription() {
        return description;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public boolean hasFixedValue() {
        return fixedValue != null && !fixedValue.isEmpty();
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public List<String> getExamples() {
        return new ArrayList<>(examples);
    }

    public boolean hasExamples() {
        return !examples.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Suggestion that = (Suggestion) o;
        return preferred == that.preferred && Objects.equals(description, that.description)
                && Objects.equals(fixedValue, that.fixedValue) && Objects.equals(explanation, that.explanation)
                && Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, fixedValue, explanation, preferred, examples);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String description;
        private String fixedValue;
        private String explanation;
        private boolean preferred;
        private final List<String> examples = new ArrayList<>();

        private Builder() {
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder fixedValue(String fixedValue) {
            this.fixedValue = fixedValue;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder preferred(boolean preferred) {
            this.preferred = preferred;
            return this;
        }

        public Builder addExample(String example) {
            this.examples.add(example);
            return this;
        }

        public Builder examples(List<String> examples) {
            this.examples.clear();
            if (examples != null) {
                this.examples.addAll(examples);
            }
            return this;
        }

        public Suggestion build() {
            return new Suggestion(this);
        }
    }
}
