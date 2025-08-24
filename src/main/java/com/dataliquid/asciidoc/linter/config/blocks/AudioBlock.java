package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OPTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.URL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Media.AUTOPLAY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Media.CONTROLS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Media.LOOP;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = AudioBlock.Builder.class)
public final class AudioBlock extends AbstractBlock {
    @JsonProperty(URL)
    private final UrlConfig url;
    @JsonProperty(OPTIONS)
    private final OptionsConfig options;
    @JsonProperty(TITLE)
    private final TitleConfig title;

    private AudioBlock(Builder builder) {
        super(builder);
        this.url = builder._url;
        this.options = builder._options;
        this.title = builder._title;
    }

    @Override
    public BlockType getType() {
        return BlockType.AUDIO;
    }

    public UrlConfig getUrl() {
        return url;
    }

    public OptionsConfig getOptions() {
        return options;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = UrlConfig.UrlConfigBuilder.class)
    public static class UrlConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private UrlConfig(UrlConfigBuilder builder) {
            this.required = builder._required;
            this.pattern = builder._pattern;
            this.severity = builder._severity;
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

        public static UrlConfigBuilder builder() {
            return new UrlConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class UrlConfigBuilder {
            private boolean _required;
            private Pattern _pattern;
            private Severity _severity;

            public UrlConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public UrlConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public UrlConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public UrlConfigBuilder severity(Severity severity) {
                this._severity = severity;
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
            return required == that.required
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern())
                    && Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }

    @JsonDeserialize(builder = OptionsConfig.OptionsConfigBuilder.class)
    public static class OptionsConfig {
        @JsonProperty(AUTOPLAY)
        private final AutoplayConfig autoplay;
        @JsonProperty(CONTROLS)
        private final ControlsConfig controls;
        @JsonProperty(LOOP)
        private final LoopConfig loop;

        private OptionsConfig(OptionsConfigBuilder builder) {
            this.autoplay = builder._autoplay;
            this.controls = builder._controls;
            this.loop = builder._loop;
        }

        public AutoplayConfig getAutoplay() {
            return autoplay;
        }

        public ControlsConfig getControls() {
            return controls;
        }

        public LoopConfig getLoop() {
            return loop;
        }

        public static OptionsConfigBuilder builder() {
            return new OptionsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class OptionsConfigBuilder {
            private AutoplayConfig _autoplay;
            private ControlsConfig _controls;
            private LoopConfig _loop;

            public OptionsConfigBuilder autoplay(AutoplayConfig autoplay) {
                this._autoplay = autoplay;
                return this;
            }

            public OptionsConfigBuilder controls(ControlsConfig controls) {
                this._controls = controls;
                return this;
            }

            public OptionsConfigBuilder loop(LoopConfig loop) {
                this._loop = loop;
                return this;
            }

            public OptionsConfig build() {
                return new OptionsConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof OptionsConfig that))
                return false;
            return Objects.equals(autoplay, that.autoplay) && Objects.equals(controls, that.controls)
                    && Objects.equals(loop, that.loop);
        }

        @Override
        public int hashCode() {
            return Objects.hash(autoplay, controls, loop);
        }
    }

    @JsonDeserialize(builder = AutoplayConfig.AutoplayConfigBuilder.class)
    public static class AutoplayConfig {
        @JsonProperty(ALLOWED)
        private final boolean allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private AutoplayConfig(AutoplayConfigBuilder builder) {
            this.allowed = builder._allowed;
            this.severity = builder._severity;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static AutoplayConfigBuilder builder() {
            return new AutoplayConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class AutoplayConfigBuilder {
            private boolean _allowed;
            private Severity _severity;

            public AutoplayConfigBuilder allowed(boolean allowed) {
                this._allowed = allowed;
                return this;
            }

            public AutoplayConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public AutoplayConfig build() {
                return new AutoplayConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AutoplayConfig that))
                return false;
            return allowed == that.allowed && Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowed, severity);
        }
    }

    @JsonDeserialize(builder = ControlsConfig.ControlsConfigBuilder.class)
    public static class ControlsConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private ControlsConfig(ControlsConfigBuilder builder) {
            this.required = builder._required;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static ControlsConfigBuilder builder() {
            return new ControlsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ControlsConfigBuilder {
            private boolean _required;
            private Severity _severity;

            public ControlsConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public ControlsConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public ControlsConfig build() {
                return new ControlsConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ControlsConfig that))
                return false;
            return required == that.required && Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, severity);
        }
    }

    @JsonDeserialize(builder = LoopConfig.LoopConfigBuilder.class)
    public static class LoopConfig {
        @JsonProperty(ALLOWED)
        private final boolean allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private LoopConfig(LoopConfigBuilder builder) {
            this.allowed = builder._allowed;
            this.severity = builder._severity;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static LoopConfigBuilder builder() {
            return new LoopConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class LoopConfigBuilder {
            private boolean _allowed;
            private Severity _severity;

            public LoopConfigBuilder allowed(boolean allowed) {
                this._allowed = allowed;
                return this;
            }

            public LoopConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public LoopConfig build() {
                return new LoopConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof LoopConfig that))
                return false;
            return allowed == that.allowed && Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowed, severity);
        }
    }

    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
    public static class TitleConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
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

        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TitleConfigBuilder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            public TitleConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public TitleConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public TitleConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public TitleConfigBuilder severity(Severity severity) {
                this._severity = severity;
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
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlConfig _url;
        private OptionsConfig _options;
        private TitleConfig _title;

        public Builder url(UrlConfig url) {
            this._url = url;
            return this;
        }

        public Builder options(OptionsConfig options) {
            this._options = options;
            return this;
        }

        public Builder title(TitleConfig title) {
            this._title = title;
            return this;
        }

        @Override
        public AudioBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new AudioBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AudioBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(url, that.url) && Objects.equals(options, that.options)
                && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, options, title);
    }
}
