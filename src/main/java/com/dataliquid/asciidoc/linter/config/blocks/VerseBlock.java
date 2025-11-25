package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.AUTHOR;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Quote.ATTRIBUTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.DEFAULT_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VerseBlock.Builder.class)
public final class VerseBlock extends AbstractBlock {
    @JsonProperty(AUTHOR)
    private final AuthorConfig author;
    @JsonProperty(ATTRIBUTION)
    private final AttributionConfig attribution;
    @JsonProperty(CONTENT)
    private final ContentConfig content;

    private VerseBlock(Builder builder) {
        super(builder);
        this.author = builder._author;
        this.attribution = builder._attribution;
        this.content = builder._content;
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

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = AuthorConfig.AuthorConfigBuilder.class)
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

        private AuthorConfig(AuthorConfigBuilder builder) {
            this.defaultValue = builder._defaultValue;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.required = builder._required;
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

        public static AuthorConfigBuilder builder() {
            return new AuthorConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class AuthorConfigBuilder {
            private String _defaultValue;
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
            private boolean _required;

            public AuthorConfigBuilder defaultValue(String defaultValue) {
                this._defaultValue = defaultValue;
                return this;
            }

            public AuthorConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public AuthorConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public AuthorConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public AuthorConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public AuthorConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public AuthorConfig build() {
                return new AuthorConfig(this);
            }
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

    @JsonDeserialize(builder = AttributionConfig.AttributionConfigBuilder.class)
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

        private AttributionConfig(AttributionConfigBuilder builder) {
            this.defaultValue = builder._defaultValue;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.required = builder._required;
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

        public static AttributionConfigBuilder builder() {
            return new AttributionConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class AttributionConfigBuilder {
            private String _defaultValue;
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
            private boolean _required;

            public AttributionConfigBuilder defaultValue(String defaultValue) {
                this._defaultValue = defaultValue;
                return this;
            }

            public AttributionConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public AttributionConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public AttributionConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public AttributionConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public AttributionConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public AttributionConfig build() {
                return new AttributionConfig(this);
            }
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

    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(REQUIRED)
        private final boolean required;

        private ContentConfig(ContentConfigBuilder builder) {
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
            this.required = builder._required;
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

        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ContentConfigBuilder {
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
            private boolean _required;

            public ContentConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
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

            public ContentConfigBuilder required(boolean required) {
                this._required = required;
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private AuthorConfig _author;
        private AttributionConfig _attribution;
        private ContentConfig _content;

        public Builder author(AuthorConfig author) {
            this._author = author;
            return this;
        }

        public Builder attribution(AttributionConfig attribution) {
            this._attribution = attribution;
            return this;
        }

        public Builder content(ContentConfig content) {
            this._content = content;
            return this;
        }

        @Override
        public VerseBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new VerseBlock(this);
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
