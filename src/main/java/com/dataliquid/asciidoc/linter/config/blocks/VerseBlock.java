package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.AUTHOR;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.ATTRIBUTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.DEFAULT_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public final class VerseBlock extends AbstractBlock {
    @JsonProperty(AUTHOR)
    private final AuthorConfig author;
    @JsonProperty(ATTRIBUTION)
    private final AttributionConfig attribution;
    @JsonProperty(CONTENT)
    private final ContentConfig content;

    @JsonCreator
    public VerseBlock(@JsonProperty(NAME) String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence, @JsonProperty(ORDER) Integer order,
            @JsonProperty(AUTHOR) AuthorConfig author, @JsonProperty(ATTRIBUTION) AttributionConfig attribution,
            @JsonProperty(CONTENT) ContentConfig content) {
        super(name, severity, occurrence, order);
        this.author = author;
        this.attribution = attribution;
        this.content = content;
    }

    @Override
    public BlockType getType() {
        return BlockType.VERSE;
    }

    public AuthorConfig getAuthor() {
        return author;
    }

    public AttributionConfig getAttribution() {
        return attribution;
    }

    public ContentConfig getContent() {
        return content;
    }

    @JsonDeserialize
    public static class AuthorConfig {
        @JsonProperty(DEFAULT_VALUE)
        private final String defaultValue;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(REQUIRED)
        private final boolean required;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public AuthorConfig(@JsonProperty(DEFAULT_VALUE) String defaultValue,
                @JsonProperty(MIN_LENGTH) Integer minLength, @JsonProperty(MAX_LENGTH) Integer maxLength,
                @JsonProperty(PATTERN) String patternString, @JsonProperty(REQUIRED) boolean required) {
            this.defaultValue = defaultValue;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.required = required;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AuthorConfig that))
                return false;
            return required == that.required && Objects.equals(defaultValue, that.defaultValue)
                    && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern());
        }

        @Override
        public int hashCode() {
            return Objects
                    .hash(defaultValue, minLength, maxLength, pattern == null ? null : pattern.pattern(), required);
        }
    }

    @JsonDeserialize
    public static class AttributionConfig {
        @JsonProperty(DEFAULT_VALUE)
        private final String defaultValue;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(REQUIRED)
        private final boolean required;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public AttributionConfig(@JsonProperty(DEFAULT_VALUE) String defaultValue,
                @JsonProperty(MIN_LENGTH) Integer minLength, @JsonProperty(MAX_LENGTH) Integer maxLength,
                @JsonProperty(PATTERN) String patternString, @JsonProperty(REQUIRED) boolean required) {
            this.defaultValue = defaultValue;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.required = required;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AttributionConfig that))
                return false;
            return required == that.required && Objects.equals(defaultValue, that.defaultValue)
                    && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern());
        }

        @Override
        public int hashCode() {
            return Objects
                    .hash(defaultValue, minLength, maxLength, pattern == null ? null : pattern.pattern(), required);
        }
    }

    @JsonDeserialize
    public static class ContentConfig {
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(REQUIRED)
        private final boolean required;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public ContentConfig(@JsonProperty(MIN_LENGTH) Integer minLength, @JsonProperty(MAX_LENGTH) Integer maxLength,
                @JsonProperty(PATTERN) String patternString, @JsonProperty(REQUIRED) boolean required) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.required = required;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ContentConfig that))
                return false;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength)
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern());
        }

        @Override
        public int hashCode() {
            return Objects.hash(minLength, maxLength, pattern == null ? null : pattern.pattern(), required);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VerseBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(author, that.author) && Objects.equals(attribution, that.attribution)
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, attribution, content);
    }
}
