package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.URL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.WIDTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.HEIGHT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Media.POSTER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OPTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CAPTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_VALUE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Media.CONTROLS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public class VideoBlock extends AbstractBlock {
    private final UrlConfig url;
    private final DimensionConfig width;
    private final DimensionConfig height;
    private final PosterConfig poster;
    private final OptionsConfig options;
    private final CaptionConfig caption;

    @Override
    public BlockType getType() {
        return BlockType.VIDEO;
    }

    @JsonCreator
    public VideoBlock(@JsonProperty("name") String name, @JsonProperty("severity") Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("url") UrlConfig url, @JsonProperty("width") DimensionConfig width,
            @JsonProperty("height") DimensionConfig height, @JsonProperty("poster") PosterConfig poster,
            @JsonProperty("options") OptionsConfig options, @JsonProperty("caption") CaptionConfig caption) {
        super(name, severity, occurrence, order);
        this.url = url;
        this.width = width;
        this.height = height;
        this.poster = poster;
        this.options = options;
        this.caption = caption;
    }

    public UrlConfig getUrl() {
        return url;
    }

    public DimensionConfig getWidth() {
        return width;
    }

    public DimensionConfig getHeight() {
        return height;
    }

    public PosterConfig getPoster() {
        return poster;
    }

    public OptionsConfig getOptions() {
        return options;
    }

    public CaptionConfig getCaption() {
        return caption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VideoBlock))
            return false;
        if (!super.equals(o))
            return false;
        VideoBlock that = (VideoBlock) o;
        return Objects.equals(url, that.url) && Objects.equals(width, that.width) && Objects.equals(height, that.height)
                && Objects.equals(poster, that.poster) && Objects.equals(options, that.options)
                && Objects.equals(caption, that.caption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, width, height, poster, options, caption);
    }

    @JsonDeserialize
    public static class UrlConfig {
        private final Boolean required;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public UrlConfig(@JsonProperty("required") Boolean required, @JsonProperty("pattern") String patternString,
                @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        public Boolean getRequired() {
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
            if (!(o instanceof UrlConfig))
                return false;
            UrlConfig urlConfig = (UrlConfig) o;
            return Objects.equals(required, urlConfig.required) && patternEquals(pattern, urlConfig.pattern)
                    && severity == urlConfig.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, patternToString(pattern), severity);
        }

        @SuppressWarnings("PMD.CompareObjectsWithEquals") // Pattern comparison optimization
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2)
                return true;
            if (p1 == null || p2 == null)
                return false;
            return p1.pattern().equals(p2.pattern());
        }

        private String patternToString(Pattern p) {
            return p != null ? p.pattern() : null;
        }

    }

    @JsonDeserialize
    public static class DimensionConfig {
        private final Boolean required;
        private final Integer minValue;
        private final Integer maxValue;
        private final Severity severity;

        @JsonCreator
        public DimensionConfig(@JsonProperty("required") Boolean required, @JsonProperty("minValue") Integer minValue,
                @JsonProperty("maxValue") Integer maxValue, @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.severity = severity;
        }

        public Boolean getRequired() {
            return required;
        }

        public Integer getMinValue() {
            return minValue;
        }

        public Integer getMaxValue() {
            return maxValue;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DimensionConfig))
                return false;
            DimensionConfig that = (DimensionConfig) o;
            return Objects.equals(required, that.required) && Objects.equals(minValue, that.minValue)
                    && Objects.equals(maxValue, that.maxValue) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minValue, maxValue, severity);
        }

    }

    @JsonDeserialize
    public static class PosterConfig {
        private final Boolean required;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public PosterConfig(@JsonProperty("required") Boolean required, @JsonProperty("pattern") String patternString,
                @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.severity = severity;
        }

        public Boolean getRequired() {
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
            if (!(o instanceof PosterConfig))
                return false;
            PosterConfig that = (PosterConfig) o;
            return Objects.equals(required, that.required) && patternEquals(pattern, that.pattern)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, patternToString(pattern), severity);
        }

        @SuppressWarnings("PMD.CompareObjectsWithEquals") // Pattern comparison optimization
        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2)
                return true;
            if (p1 == null || p2 == null)
                return false;
            return p1.pattern().equals(p2.pattern());
        }

        private String patternToString(Pattern p) {
            return p != null ? p.pattern() : null;
        }

    }

    @JsonDeserialize
    public static class OptionsConfig {
        private final ControlsConfig controls;

        @JsonCreator
        public OptionsConfig(@JsonProperty("controls") ControlsConfig controls) {
            this.controls = controls;
        }

        public ControlsConfig getControls() {
            return controls;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof OptionsConfig))
                return false;
            OptionsConfig that = (OptionsConfig) o;
            return Objects.equals(controls, that.controls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(controls);
        }

    }

    @JsonDeserialize
    public static class ControlsConfig {
        private final Boolean required;
        private final Severity severity;

        @JsonCreator
        public ControlsConfig(@JsonProperty("required") Boolean required, @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.severity = severity;
        }

        public Boolean getRequired() {
            return required;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ControlsConfig))
                return false;
            ControlsConfig that = (ControlsConfig) o;
            return Objects.equals(required, that.required) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, severity);
        }

    }

    @JsonDeserialize
    public static class CaptionConfig {
        private final Boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        @JsonCreator
        public CaptionConfig(@JsonProperty("required") Boolean required, @JsonProperty("minLength") Integer minLength,
                @JsonProperty("maxLength") Integer maxLength, @JsonProperty("severity") Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
        }

        public Boolean getRequired() {
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
            if (!(o instanceof CaptionConfig))
                return false;
            CaptionConfig that = (CaptionConfig) o;
            return Objects.equals(required, that.required) && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }

    }
}
