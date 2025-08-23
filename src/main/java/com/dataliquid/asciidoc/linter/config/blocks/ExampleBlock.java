package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CAPTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Example.COLLAPSIBLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for EXAMPLE blocks. Validates example blocks with optional
 * numbering and caption format. Based on the YAML schema definition for the
 * linter.
 */
@JsonDeserialize(builder = ExampleBlock.Builder.class)
public class ExampleBlock extends AbstractBlock {

    private final CaptionConfig caption;
    private final CollapsibleConfig collapsible;

    private ExampleBlock(Builder builder) {
        super(builder);
        this.caption = builder._caption;
        this.collapsible = builder._collapsible;
    }

    @Override
    public BlockType getType() {
        return BlockType.EXAMPLE;
    }

    public CaptionConfig getCaption() {
        return _caption;
    }

    public CollapsibleConfig getCollapsible() {
        return _collapsible;
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

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBlock.AbstractBuilder<Builder> {
        private CaptionConfig _caption;
        private CollapsibleConfig _collapsible;

        @JsonProperty(CAPTION)
        public Builder caption(CaptionConfig caption) {
            this._caption = caption;
            return this;
        }

        @JsonProperty(COLLAPSIBLE)
        public Builder collapsible(CollapsibleConfig collapsible) {
            this._collapsible = collapsible;
            return this;
        }

        @Override
        public ExampleBlock build() {
            return new ExampleBlock(this);
        }
    }

    /**
     * Configuration for the caption of an example block. Based on the YAML schema
     * requiring specific format.
     */
    @JsonDeserialize(builder = CaptionConfig.Builder.class)
    public static class CaptionConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        private CaptionConfig(Builder builder) {
            this.required = builder._required;
            this.pattern = builder._pattern;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return _required;
        }

        public Pattern getPattern() {
            return _pattern;
        }

        public Integer getMinLength() {
            return _minLength;
        }

        public Integer getMaxLength() {
            return _maxLength;
        }

        public Severity getSeverity() {
            return _severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private boolean _required;
            private Pattern _pattern;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(PATTERN)
            @SuppressWarnings("PMD.NullAssignment")
            public Builder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty(MIN_LENGTH)
            public Builder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            @JsonProperty(MAX_LENGTH)
            public Builder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }
    }

    /**
     * Configuration for the collapsible attribute of an example block. Based on the
     * YAML schema allowing true/false values.
     */
    @JsonDeserialize(builder = CollapsibleConfig.Builder.class)
    public static class CollapsibleConfig {
        private final boolean required;
        private final List<Boolean> allowed;
        private final Severity severity;

        private CollapsibleConfig(Builder builder) {
            this.required = builder._required;
            this.allowed = builder._allowed;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return _required;
        }

        public List<Boolean> getAllowed() {
            return _allowed;
        }

        public Severity getSeverity() {
            return _severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private boolean _required;
            private List<Boolean> _allowed;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(ALLOWED)
            public Builder allowed(List<Boolean> allowed) {
                this._allowed = allowed;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public CollapsibleConfig build() {
                return new CollapsibleConfig(this);
            }
        }
    }
}
