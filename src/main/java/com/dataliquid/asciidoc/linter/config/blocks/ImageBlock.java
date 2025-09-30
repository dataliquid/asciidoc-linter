package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public final class ImageBlock extends AbstractBlock {
    private static final String SEVERITY = "severity";
    private final UrlConfig url;
    private final DimensionConfig height;
    private final DimensionConfig width;
    private final AltTextConfig alt;

    @JsonCreator
    public ImageBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("url") UrlConfig url, @JsonProperty("height") DimensionConfig height,
            @JsonProperty("width") DimensionConfig width, @JsonProperty("alt") AltTextConfig alt) {
        super(name, severity, occurrence, order);
        this.url = url;
        this.height = height;
        this.width = width;
        this.alt = alt;
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

    @JsonDeserialize
    public static class UrlConfig {
        private final Pattern pattern;
        private final boolean required;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public UrlConfig(@JsonProperty("pattern") String patternString, @JsonProperty("required") boolean required) {
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.required = required;
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

    @JsonDeserialize
    public static class DimensionConfig {
        private final Integer minValue;
        private final Integer maxValue;
        private final boolean required;

        @JsonCreator
        public DimensionConfig(@JsonProperty("minValue") Integer minValue, @JsonProperty("maxValue") Integer maxValue,
                @JsonProperty("required") boolean required) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.required = required;
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

    @JsonDeserialize
    public static class AltTextConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;

        @JsonCreator
        public AltTextConfig(@JsonProperty("required") boolean required, @JsonProperty("minLength") Integer minLength,
                @JsonProperty("maxLength") Integer maxLength) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
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
