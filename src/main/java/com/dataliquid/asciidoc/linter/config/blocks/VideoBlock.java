package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        this.url = builder.url;
        this.width = builder.width;
        this.height = builder.height;
        this.poster = builder.poster;
        this.options = builder.options;
        this.caption = builder.caption;
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

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoBlock)) return false;
        if (!super.equals(o)) return false;
        VideoBlock that = (VideoBlock) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(width, that.width) &&
                Objects.equals(height, that.height) &&
                Objects.equals(poster, that.poster) &&
                Objects.equals(options, that.options) &&
                Objects.equals(caption, that.caption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, width, height, poster, options, caption);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlConfig url;
        private DimensionConfig width;
        private DimensionConfig height;
        private PosterConfig poster;
        private OptionsConfig options;
        private CaptionConfig caption;

        @JsonProperty("url")
        public Builder url(UrlConfig url) {
            this.url = url;
            return this;
        }

        @JsonProperty("width")
        public Builder width(DimensionConfig width) {
            this.width = width;
            return this;
        }

        @JsonProperty("height")
        public Builder height(DimensionConfig height) {
            this.height = height;
            return this;
        }

        @JsonProperty("poster")
        public Builder poster(PosterConfig poster) {
            this.poster = poster;
            return this;
        }

        @JsonProperty("options")
        public Builder options(OptionsConfig options) {
            this.options = options;
            return this;
        }

        @JsonProperty("caption")
        public Builder caption(CaptionConfig caption) {
            this.caption = caption;
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
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UrlConfig)) return false;
            UrlConfig urlConfig = (UrlConfig) o;
            return Objects.equals(required, urlConfig.required) &&
                    patternEquals(pattern, urlConfig.pattern) &&
                    severity == urlConfig.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, patternToString(pattern), severity);
        }

        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2) return true;
            if (p1 == null || p2 == null) return false;
            return p1.pattern().equals(p2.pattern());
        }

        private String patternToString(Pattern p) {
            return p != null ? p.pattern() : null;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Boolean required;
            private Pattern pattern;
            private Severity severity;

            @JsonProperty("required")
            public Builder required(Boolean required) {
                this.required = required;
                return this;
            }

            @JsonProperty("pattern")
            public Builder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }

            @JsonCreator
            public Builder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
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
            this.required = builder.required;
            this.minValue = builder.minValue;
            this.maxValue = builder.maxValue;
            this.severity = builder.severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionConfig)) return false;
            DimensionConfig that = (DimensionConfig) o;
            return Objects.equals(required, that.required) &&
                    Objects.equals(minValue, that.minValue) &&
                    Objects.equals(maxValue, that.maxValue) &&
                    severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minValue, maxValue, severity);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Boolean required;
            private Integer minValue;
            private Integer maxValue;
            private Severity severity;

            @JsonProperty("required")
            public Builder required(Boolean required) {
                this.required = required;
                return this;
            }

            @JsonProperty("minValue")
            public Builder minValue(Integer minValue) {
                this.minValue = minValue;
                return this;
            }

            @JsonProperty("maxValue")
            public Builder maxValue(Integer maxValue) {
                this.maxValue = maxValue;
                return this;
            }

            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
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
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PosterConfig)) return false;
            PosterConfig that = (PosterConfig) o;
            return Objects.equals(required, that.required) &&
                    patternEquals(pattern, that.pattern) &&
                    severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, patternToString(pattern), severity);
        }

        private boolean patternEquals(Pattern p1, Pattern p2) {
            if (p1 == p2) return true;
            if (p1 == null || p2 == null) return false;
            return p1.pattern().equals(p2.pattern());
        }

        private String patternToString(Pattern p) {
            return p != null ? p.pattern() : null;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Boolean required;
            private Pattern pattern;
            private Severity severity;

            @JsonProperty("required")
            public Builder required(Boolean required) {
                this.required = required;
                return this;
            }

            @JsonProperty("pattern")
            public Builder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }

            @JsonCreator
            public Builder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
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
            this.controls = builder.controls;
        }

        public ControlsConfig getControls() {
            return controls;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OptionsConfig)) return false;
            OptionsConfig that = (OptionsConfig) o;
            return Objects.equals(controls, that.controls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(controls);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private ControlsConfig controls;

            @JsonProperty("controls")
            public Builder controls(ControlsConfig controls) {
                this.controls = controls;
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
            this.required = builder.required;
            this.severity = builder.severity;
        }

        public Boolean getRequired() {
            return required;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ControlsConfig)) return false;
            ControlsConfig that = (ControlsConfig) o;
            return Objects.equals(required, that.required) &&
                    severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, severity);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Boolean required;
            private Severity severity;

            @JsonProperty("required")
            public Builder required(Boolean required) {
                this.required = required;
                return this;
            }

            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
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
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
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

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CaptionConfig)) return false;
            CaptionConfig that = (CaptionConfig) o;
            return Objects.equals(required, that.required) &&
                    Objects.equals(minLength, that.minLength) &&
                    Objects.equals(maxLength, that.maxLength) &&
                    severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;

            @JsonProperty("required")
            public Builder required(Boolean required) {
                this.required = required;
                return this;
            }

            @JsonProperty("minLength")
            public Builder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }

            @JsonProperty("maxLength")
            public Builder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }

            @JsonProperty("severity")
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }
    }
}