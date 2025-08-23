package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
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
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VideoBlock.Builder.class)
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

    private VideoBlock(Builder builder) {
        super(builder);
        this.url = builder._url;
        this.width = builder._width;
        this.height = builder._height;
        this.poster = builder._poster;
        this.options = builder._options;
        this.caption = builder._caption;
    }

    public UrlConfig getUrl() {
        return _url;
    }

    public DimensionConfig getWidth() {
        return _width;
    }

    public DimensionConfig getHeight() {
        return _height;
    }

    public PosterConfig getPoster() {
        return _poster;
    }

    public OptionsConfig getOptions() {
        return _options;
    }

    public CaptionConfig getCaption() {
        return _caption;
    }

    public static Builder builder() {
        return new Builder();
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlConfig _url;
        private DimensionConfig _width;
        private DimensionConfig _height;
        private PosterConfig _poster;
        private OptionsConfig _options;
        private CaptionConfig _caption;

        @JsonProperty(URL)
        public Builder url(UrlConfig url) {
            this._url = url;
            return this;
        }

        @JsonProperty(WIDTH)
        public Builder width(DimensionConfig width) {
            this._width = width;
            return this;
        }

        @JsonProperty(HEIGHT)
        public Builder height(DimensionConfig height) {
            this._height = height;
            return this;
        }

        @JsonProperty(POSTER)
        public Builder poster(PosterConfig poster) {
            this._poster = poster;
            return this;
        }

        @JsonProperty(OPTIONS)
        public Builder options(OptionsConfig options) {
            this._options = options;
            return this;
        }

        @JsonProperty(CAPTION)
        public Builder caption(CaptionConfig caption) {
            this._caption = caption;
            return this;
        }

        @Override
        public VideoBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
            return new VideoBlock(this);
        }
    }

    @JsonDeserialize(builder = UrlConfig.Builder.class)
    public static class UrlConfig {
        private final Boolean required;
        private final Pattern pattern;
        private final Severity severity;

        private UrlConfig(Builder builder) {
            this.required = builder._required;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
        }

        public Boolean getRequired() {
            return _required;
        }

        public Pattern getPattern() {
            return _pattern;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Boolean _required;
            private Pattern _pattern;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(Boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(PATTERN)
            public Builder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @JsonCreator
            @SuppressWarnings("PMD.NullAssignment")
            public Builder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public UrlConfig build() {
                return new UrlConfig(this);
            }
        }
    }

    @JsonDeserialize(builder = DimensionConfig.Builder.class)
    public static class DimensionConfig {
        private final Boolean required;
        private final Integer minValue;
        private final Integer maxValue;
        private final Severity severity;

        private DimensionConfig(Builder builder) {
            this.required = builder._required;
            this.minValue = builder._minValue;
            this.maxValue = builder._maxValue;
            this.severity = builder._severity;
        }

        public Boolean getRequired() {
            return _required;
        }

        public Integer getMinValue() {
            return _minValue;
        }

        public Integer getMaxValue() {
            return _maxValue;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Boolean _required;
            private Integer _minValue;
            private Integer _maxValue;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(Boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(MIN_VALUE)
            public Builder minValue(Integer minValue) {
                this._minValue = minValue;
                return this;
            }

            @JsonProperty(MAX_VALUE)
            public Builder maxValue(Integer maxValue) {
                this._maxValue = maxValue;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public DimensionConfig build() {
                return new DimensionConfig(this);
            }
        }
    }

    @JsonDeserialize(builder = PosterConfig.Builder.class)
    public static class PosterConfig {
        private final Boolean required;
        private final Pattern pattern;
        private final Severity severity;

        private PosterConfig(Builder builder) {
            this.required = builder._required;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
        }

        public Boolean getRequired() {
            return _required;
        }

        public Pattern getPattern() {
            return _pattern;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Boolean _required;
            private Pattern _pattern;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(Boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(PATTERN)
            public Builder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @JsonCreator
            @SuppressWarnings("PMD.NullAssignment")
            public Builder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public PosterConfig build() {
                return new PosterConfig(this);
            }
        }
    }

    @JsonDeserialize(builder = OptionsConfig.Builder.class)
    public static class OptionsConfig {
        private final ControlsConfig controls;

        private OptionsConfig(Builder builder) {
            this.controls = builder._controls;
        }

        public ControlsConfig getControls() {
            return _controls;
        }

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private ControlsConfig _controls;

            @JsonProperty(CONTROLS)
            public Builder controls(ControlsConfig controls) {
                this._controls = controls;
                return this;
            }

            public OptionsConfig build() {
                return new OptionsConfig(this);
            }
        }
    }

    @JsonDeserialize(builder = ControlsConfig.Builder.class)
    public static class ControlsConfig {
        private final Boolean required;
        private final Severity severity;

        private ControlsConfig(Builder builder) {
            this.required = builder._required;
            this.severity = builder._severity;
        }

        public Boolean getRequired() {
            return _required;
        }

        public Severity getSeverity() {
            return _severity;
        }

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Boolean _required;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(Boolean required) {
                this._required = required;
                return this;
            }

            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public ControlsConfig build() {
                return new ControlsConfig(this);
            }
        }
    }

    @JsonDeserialize(builder = CaptionConfig.Builder.class)
    public static class CaptionConfig {
        private final Boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        private CaptionConfig(Builder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
        }

        public Boolean getRequired() {
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

        public static Builder builder() {
            return new Builder();
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

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            @JsonProperty(REQUIRED)
            public Builder required(Boolean required) {
                this._required = required;
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
}
