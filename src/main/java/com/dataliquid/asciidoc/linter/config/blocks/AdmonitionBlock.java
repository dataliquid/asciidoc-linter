package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Admonition.ICON;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Admonition.TYPE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;

@JsonDeserialize
public final class AdmonitionBlock extends AbstractBlock {
    @JsonProperty(TYPE)
    private final TypeConfig type;
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(CONTENT)
    private final ContentConfig content;
    @JsonProperty(ICON)
    private final IconConfig icon;

    @JsonCreator
    public AdmonitionBlock(@JsonProperty(NAME) String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence, @JsonProperty(ORDER) Integer order,
            @JsonProperty(TYPE) TypeConfig type, @JsonProperty(TITLE) TitleConfig title,
            @JsonProperty(CONTENT) ContentConfig content, @JsonProperty(ICON) IconConfig icon) {
        super(name, severity, occurrence, order);
        this.type = type;
        this.title = title;
        this.content = content;
        this.icon = icon;
    }

    @Override
    public BlockType getType() {
        return BlockType.ADMONITION;
    }

    public TypeConfig getTypeConfig() {
        return type;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public ContentConfig getContent() {
        return content;
    }

    public IconConfig getIcon() {
        return icon;
    }

    @JsonDeserialize
    public static class TypeConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(ALLOWED)
        private final List<String> allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public TypeConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(ALLOWED) List<String> allowed,
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
    public static class TitleConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public TitleConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(MIN_LENGTH) Integer minLength, @JsonProperty(MAX_LENGTH) Integer maxLength,
                @JsonProperty(SEVERITY) Severity severity) {
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
            if (!(o instanceof TitleConfig that))
                return false;
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
    }

    @JsonDeserialize
    public static class ContentConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(LINES)
        private final LineConfig lines;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public ContentConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(LINES) LineConfig lines,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.lines = lines;
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

        public LineConfig getLines() {
            return lines;
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
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && Objects.equals(lines, that.lines)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines, severity);
        }
    }

    @JsonDeserialize
    public static class IconConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public IconConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
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
            if (!(o instanceof IconConfig that))
                return false;
            return required == that.required && Objects
                    .equals(pattern == null ? null : pattern.pattern(),
                            that.pattern == null ? null : that.pattern.pattern())
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AdmonitionBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(type, that.type) && Objects.equals(title, that.title)
                && Objects.equals(content, that.content) && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, title, content, icon);
    }
}
