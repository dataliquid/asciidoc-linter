package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
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
@JsonDeserialize
public final class PassBlock extends AbstractBlock {
    private static final String SEVERITY = "severity";
    private final TypeConfig type;
    private final ContentConfig content;
    private final ReasonConfig reason;

    @JsonCreator
    public PassBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("type") TypeConfig type, @JsonProperty("content") ContentConfig content,
            @JsonProperty("reason") ReasonConfig reason) {
        super(name, severity, occurrence, order);
        this.type = type;
        this.content = content;
        this.reason = reason;
    }

    @Override
    public BlockType getType() {
        return BlockType.PASS;
    }

    public TypeConfig getTypeConfig() {
        return type;
    }

    public ContentConfig getContent() {
        return content;
    }

    public ReasonConfig getReason() {
        return reason;
    }

    @JsonDeserialize
    public static class TypeConfig {
        private final boolean required;
        private final List<String> allowed;
        private final Severity severity;

        @JsonCreator
        public TypeConfig(@JsonProperty("required") boolean required, @JsonProperty("allowed") List<String> allowed,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.allowed = allowed != null ? Collections.unmodifiableList(new ArrayList<>(allowed))
                    : Collections.emptyList();
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public List<String> getAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
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

    @JsonDeserialize
    public static class ContentConfig {
        private final boolean required;
        private final Integer maxLength;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public ContentConfig(@JsonProperty("required") boolean required, @JsonProperty("maxLength") Integer maxLength,
                @JsonProperty("pattern") String patternString, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Severity getSeverity() {
            return severity;
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

    @JsonDeserialize
    public static class ReasonConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        @JsonCreator
        public ReasonConfig(@JsonProperty("required") boolean required, @JsonProperty("minLength") Integer minLength,
                @JsonProperty("maxLength") Integer maxLength, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
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
