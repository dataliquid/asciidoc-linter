package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration for EXAMPLE blocks. Validates example blocks with optional
 * numbering and caption format. Based on the YAML schema definition for the
 * linter.
 */
@JsonDeserialize
public class ExampleBlock extends AbstractBlock {

    private final CaptionConfig caption;
    private final CollapsibleConfig collapsible;

    @JsonCreator
    public ExampleBlock(@JsonProperty("name") String name, @JsonProperty("severity") Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("caption") CaptionConfig caption,
            @JsonProperty("collapsible") CollapsibleConfig collapsible) {
        super(name, severity, occurrence, order);
        this.caption = caption;
        this.collapsible = collapsible;
    }

    @Override
    public BlockType getType() {
        return BlockType.EXAMPLE;
    }

    public CaptionConfig getCaption() {
        return caption;
    }

    public CollapsibleConfig getCollapsible() {
        return collapsible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ExampleBlock that = (ExampleBlock) o;
        return Objects.equals(caption, that.caption) && Objects.equals(collapsible, that.collapsible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), caption, collapsible);
    }

    @Override
    public String toString() {
        return "ExampleBlock{" + "name='" + getName() + '\'' + ", severity=" + getSeverity() + ", occurrence="
                + getOccurrence() + ", caption=" + caption + ", collapsible=" + collapsible + '}';
    }

    /**
     * Configuration for the caption of an example block. Based on the YAML schema
     * requiring specific format.
     */
    @JsonDeserialize
    public static class CaptionConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public CaptionConfig(@JsonProperty("required") boolean required, @JsonProperty("pattern") String patternString,
                @JsonProperty("minLength") Integer minLength, @JsonProperty("maxLength") Integer maxLength,
                @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CaptionConfig that = (CaptionConfig) o;
            return required == that.required
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern())
                    && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), minLength, maxLength, severity);
        }

        @Override
        public String toString() {
            return "CaptionConfig{" + "required=" + required + ", pattern="
                    + (pattern == null ? null : pattern.pattern()) + ", minLength=" + minLength + ", maxLength="
                    + maxLength + ", severity=" + severity + '}';
        }
    }

    /**
     * Configuration for the collapsible attribute of an example block. Based on the
     * YAML schema allowing true/false values.
     */
    @JsonDeserialize
    public static class CollapsibleConfig {
        private final boolean required;
        private final List<Boolean> allowed;
        private final Severity severity;

        @JsonCreator
        public CollapsibleConfig(@JsonProperty("required") boolean required,
                @JsonProperty("allowed") List<Boolean> allowed, @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.allowed = allowed;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public List<Boolean> getAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CollapsibleConfig that = (CollapsibleConfig) o;
            return required == that.required && Objects.equals(allowed, that.allowed) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }

        @Override
        public String toString() {
            return "CollapsibleConfig{" + "required=" + required + ", allowed=" + allowed + ", severity=" + severity
                    + '}';
        }
    }
}
