package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
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

@JsonDeserialize
public final class AudioBlock extends AbstractBlock {
    private final UrlConfig url;
    private final OptionsConfig options;
    private final TitleConfig title;

    @JsonCreator
    public AudioBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty(URL) UrlConfig url, @JsonProperty(OPTIONS) OptionsConfig options,
            @JsonProperty(TITLE) TitleConfig title) {
        super(name, severity, occurrence, order);
        this.url = url;
        this.options = options;
        this.title = title;
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

    @JsonDeserialize
    public static class UrlConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public UrlConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(PATTERN) String patternString,
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

    @JsonDeserialize
    public static class OptionsConfig {
        private final AutoplayConfig autoplay;
        private final ControlsConfig controls;
        private final LoopConfig loop;

        @JsonCreator
        public OptionsConfig(@JsonProperty("autoplay") AutoplayConfig autoplay,
                @JsonProperty("controls") ControlsConfig controls, @JsonProperty("loop") LoopConfig loop) {
            this.autoplay = autoplay;
            this.controls = controls;
            this.loop = loop;
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

    @JsonDeserialize
    public static class AutoplayConfig {
        private final boolean allowed;
        private final Severity severity;

        @JsonCreator
        public AutoplayConfig(@JsonProperty(ALLOWED) boolean allowed, @JsonProperty(SEVERITY) Severity severity) {
            this.allowed = allowed;
            this.severity = severity;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
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

    @JsonDeserialize
    public static class ControlsConfig {
        private final boolean required;
        private final Severity severity;

        @JsonCreator
        public ControlsConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.severity = severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Severity getSeverity() {
            return severity;
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

    @JsonDeserialize
    public static class LoopConfig {
        private final boolean allowed;
        private final Severity severity;

        @JsonCreator
        public LoopConfig(@JsonProperty(ALLOWED) boolean allowed, @JsonProperty(SEVERITY) Severity severity) {
            this.allowed = allowed;
            this.severity = severity;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Severity getSeverity() {
            return severity;
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

    @JsonDeserialize
    public static class TitleConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;

        @JsonCreator
        public TitleConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
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

        public Severity getSeverity() {
            return severity;
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
