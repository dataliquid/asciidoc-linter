package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.HEIGHT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.URL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.WIDTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Image.ALT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ImageBlock.Builder.class)
public final class ImageBlock extends AbstractBlock {
    @JsonProperty(URL)
    private final UrlConfig url;
    @JsonProperty(HEIGHT)
    private final DimensionConfig height;
    @JsonProperty(WIDTH)
    private final DimensionConfig width;
    @JsonProperty(ALT)
    private final AltTextConfig alt;

    private ImageBlock(Builder builder) {
        super(builder);
        this.url = builder._url;
        this.height = builder._height;
        this.width = builder._width;
        this.alt = builder._alt;
    }

    @Override
    public BlockType getType() {
        return BlockType.IMAGE;
    }

    public UrlConfig getUrl() {
        return url;
    }

    public DimensionConfig getHeight() {
        return height;
    }

    public DimensionConfig getWidth() {
        return width;
    }

    public AltTextConfig getAlt() {
        return alt;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = UrlConfig.UrlConfigBuilder.class)
    public static class UrlConfig {
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(REQUIRED)
        private final boolean required;

        private UrlConfig(UrlConfigBuilder builder) {
            this.pattern = builder._pattern;
            this.required = builder._required;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public boolean isRequired() {
            return required;
        }

        public static UrlConfigBuilder builder() {
            return new UrlConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class UrlConfigBuilder {
            private Pattern _pattern;
            private boolean _required;

            public UrlConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public UrlConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public UrlConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public UrlConfig build() {
                return new UrlConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof UrlConfig that))
                return false;
            return required == that.required && Objects
                    .equals(pattern == null ? null : pattern.pattern(),
                            that.pattern == null ? null : that.pattern.pattern());
        }

        @Override
        public int hashCode() {
            return Objects.hash(pattern == null ? null : pattern.pattern(), required);
        }
    }

    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        @JsonProperty(MIN_VALUE)
        private final Integer minValue;
        @JsonProperty(MAX_VALUE)
        private final Integer maxValue;
        @JsonProperty(REQUIRED)
        private final boolean required;

        private DimensionConfig(DimensionConfigBuilder builder) {
            this.minValue = builder._minValue;
            this.maxValue = builder._maxValue;
            this.required = builder._required;
        }

        public Integer getMinValue() {
            return minValue;
        }

        public Integer getMaxValue() {
            return maxValue;
        }

        public boolean isRequired() {
            return required;
        }

        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class DimensionConfigBuilder {
            private Integer _minValue;
            private Integer _maxValue;
            private boolean _required;

            public DimensionConfigBuilder minValue(Integer minValue) {
                this._minValue = minValue;
                return this;
            }

            public DimensionConfigBuilder maxValue(Integer maxValue) {
                this._maxValue = maxValue;
                return this;
            }

            public DimensionConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public DimensionConfig build() {
                return new DimensionConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DimensionConfig that))
                return false;
            return required == that.required && Objects.equals(minValue, that.minValue)
                    && Objects.equals(maxValue, that.maxValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(minValue, maxValue, required);
        }
    }

    @JsonDeserialize(builder = AltTextConfig.AltTextConfigBuilder.class)
    public static class AltTextConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;

        private AltTextConfig(AltTextConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
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

        public static AltTextConfigBuilder builder() {
            return new AltTextConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class AltTextConfigBuilder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;

            public AltTextConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public AltTextConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public AltTextConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public AltTextConfig build() {
                return new AltTextConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AltTextConfig that))
                return false;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlConfig _url;
        private DimensionConfig _height;
        private DimensionConfig _width;
        private AltTextConfig _alt;

        public Builder url(UrlConfig url) {
            this._url = url;
            return this;
        }

        public Builder height(DimensionConfig height) {
            this._height = height;
            return this;
        }

        public Builder width(DimensionConfig width) {
            this._width = width;
            return this;
        }

        public Builder alt(AltTextConfig alt) {
            this._alt = alt;
            return this;
        }

        @Override
        public ImageBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new ImageBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImageBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(url, that.url) && Objects.equals(height, that.height) && Objects.equals(width, that.width)
                && Objects.equals(alt, that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, height, width, alt);
    }
}
