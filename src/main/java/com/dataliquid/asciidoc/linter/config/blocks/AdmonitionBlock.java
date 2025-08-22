package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Admonition.ICON;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Admonition.TYPE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = AdmonitionBlock.Builder.class)
public final class AdmonitionBlock extends AbstractBlock {
    @JsonProperty(TYPE)
    private final TypeConfig type;
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(CONTENT)
    private final ContentConfig content;
    @JsonProperty(ICON)
    private final IconConfig icon;

    private AdmonitionBlock(Builder builder) {
        super(builder);
        this.type = builder.type;
        this.title = builder.title;
        this.content = builder.content;
        this.icon = builder.icon;
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
            this.required = builder.required;
            this.allowed = builder.allowed != null
                    ? Collections.unmodifiableList(new ArrayList<>(builder.allowed))
                    : Collections.emptyList();
            this.severity = builder.severity;
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

        public static TypeConfigBuilder builder() {
            return new TypeConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TypeConfigBuilder {
            private boolean required;
            private List<String> allowed;
            private Severity severity;

            public TypeConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public TypeConfigBuilder allowed(List<String> allowed) {
                this.allowed = allowed;
                return this;
            }

            public TypeConfigBuilder severity(Severity severity) {
                this.severity = severity;
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

    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
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

        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
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

        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TitleConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;

            public TitleConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public TitleConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }

            public TitleConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public TitleConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }

            public TitleConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }

            public TitleConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public TitleConfig build() {
                return new TitleConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TitleConfig that))
                return false;
            return required == that.required
                    && Objects.equals(pattern == null ? null : pattern.pattern(),
                            that.pattern == null ? null : that.pattern.pattern())
                    && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), minLength, maxLength, severity);
        }
    }

    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
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

        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.lines = builder.lines;
            this.severity = builder.severity;
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

        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ContentConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private LineConfig lines;
            private Severity severity;

            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public ContentConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }

            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }

            public ContentConfigBuilder lines(LineConfig lines) {
                this.lines = lines;
                return this;
            }

            public ContentConfigBuilder severity(Severity severity) {
                this.severity = severity;
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
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && Objects.equals(lines, that.lines)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines, severity);
        }
    }

    @JsonDeserialize(builder = IconConfig.IconConfigBuilder.class)
    public static class IconConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private IconConfig(IconConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
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

        public static IconConfigBuilder builder() {
            return new IconConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class IconConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;

            public IconConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public IconConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }

            public IconConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public IconConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public IconConfig build() {
                return new IconConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof IconConfig that))
                return false;
            return required == that.required && Objects.equals(pattern == null ? null : pattern.pattern(),
                    that.pattern == null ? null : that.pattern.pattern()) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TypeConfig type;
        private TitleConfig title;
        private ContentConfig content;
        private IconConfig icon;

        public Builder type(TypeConfig type) {
            this.type = type;
            return this;
        }

        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }

        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }

        public Builder icon(IconConfig icon) {
            this.icon = icon;
            return this;
        }

        @Override
        public AdmonitionBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new AdmonitionBlock(this);
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
