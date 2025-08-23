package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Pass.REASON;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

/**
 * Configuration for pass blocks (passthrough content) in AsciiDoc. Pass blocks
 * are delimited by ++++ and pass content through without processing.
 * <p>
 * This validator supports custom attributes that are not native to AsciiDoc:
 * <ul>
 * <li>{@code pass-type}: Specifies the content type (html, xml, svg)</li>
 * <li>{@code pass-reason}: Provides reason for using raw passthrough</li>
 * </ul>
 * <p>
 * Example usage:
 *
 * <pre>
 * [pass,pass-type=html,pass-reason="Custom widget for product gallery"]
 * ++++
 * &lt;div class="product-slider"&gt;
 *   &lt;img src="product1.jpg" alt="Product 1"&gt;
 * &lt;/div&gt;
 * ++++
 * </pre>
 * <p>
 * Validation is based on the YAML schema configuration for pass blocks.
 */
@JsonDeserialize(builder = PassBlock.Builder.class)
public final class PassBlock extends AbstractBlock {
    @JsonProperty(TYPE)
    private final TypeConfig type;
    @JsonProperty(CONTENT)
    private final ContentConfig content;
    @JsonProperty(REASON)
    private final ReasonConfig reason;

    private PassBlock(Builder builder) {
        super(builder);
        this.type = builder._type;
        this.content = builder._content;
        this.reason = builder._reason;
    }

    @Override
    public BlockType getType() {
        return BlockType.PASS;
    }

    public TypeConfig getTypeConfig() {
        return _type;
    }

    public ContentConfig getContent() {
        return _content;
    }

    public ReasonConfig getReason() {
        return _reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = TypeConfig.TypeConfigBuilder.class)
    public static class TypeConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(ALLOWED)
        private final List<String> allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private TypeConfig(TypeConfigBuilder builder) {
            this.required = builder._required;
            this.allowed = builder._allowed != null ? Collections.unmodifiableList(new ArrayList<>(builder._allowed))
                    : Collections.emptyList();
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return _required;
        }

        public List<String> getAllowed() {
            return _allowed;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static TypeConfigBuilder builder() {
            return new TypeConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TypeConfigBuilder {
            private boolean _required;
            private List<String> _allowed;
            private Severity _severity;

            public TypeConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public TypeConfigBuilder allowed(List<String> allowed) {
                this._allowed = allowed;
                return this;
            }

            public TypeConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public TypeConfig build() {
                return new TypeConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TypeConfig that))
                return false;
            return required == that.required && Objects.equals(allowed, that.allowed) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
    }

    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder._required;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return _required;
        }

        public Integer getMaxLength() {
            return _maxLength;
        }

        public Pattern getPattern() {
            return _pattern;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ContentConfigBuilder {
            private boolean _required;
            private Integer _maxLength;
            private Pattern _pattern;
            private Severity _severity;

            public ContentConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public ContentConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public ContentConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public ContentConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public ContentConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public ContentConfig build() {
                return new ContentConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ContentConfig that))
                return false;
            return required == that.required && Objects.equals(maxLength, that.maxLength)
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern())
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, maxLength, pattern == null ? null : pattern.pattern(), severity);
        }
    }

    @JsonDeserialize(builder = ReasonConfig.ReasonConfigBuilder.class)
    public static class ReasonConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private ReasonConfig(ReasonConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return _required;
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

        public static ReasonConfigBuilder builder() {
            return new ReasonConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ReasonConfigBuilder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            public ReasonConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public ReasonConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public ReasonConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public ReasonConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public ReasonConfig build() {
                return new ReasonConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ReasonConfig that))
                return false;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TypeConfig _type;
        private ContentConfig _content;
        private ReasonConfig _reason;

        public Builder type(TypeConfig type) {
            this._type = type;
            return this;
        }

        public Builder content(ContentConfig content) {
            this._content = content;
            return this;
        }

        public Builder reason(ReasonConfig reason) {
            this._reason = reason;
            return this;
        }

        @Override
        public PassBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new PassBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PassBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(type, that.type) && Objects.equals(content, that.content)
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, content, reason);
    }
}
