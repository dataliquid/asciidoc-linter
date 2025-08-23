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

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Listing.CALLOUTS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Listing.LANGUAGE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ListingBlock.Builder.class)
public final class ListingBlock extends AbstractBlock {
    @JsonProperty(LANGUAGE)
    private final LanguageConfig language;
    @JsonProperty(LINES)
    private final LineConfig lines;
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(CALLOUTS)
    private final CalloutsConfig callouts;

    private ListingBlock(Builder builder) {
        super(builder);
        this.language = builder._language;
        this.lines = builder._lines;
        this.title = builder._title;
        this.callouts = builder._callouts;
    }

    @Override
    public BlockType getType() {
        return BlockType.LISTING;
    }

    public LanguageConfig getLanguage() {
        return language;
    }

    public LineConfig getLines() {
        return lines;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public CalloutsConfig getCallouts() {
        return callouts;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = LanguageConfig.LanguageConfigBuilder.class)
    public static class LanguageConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(ALLOWED)
        private final List<String> allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private LanguageConfig(LanguageConfigBuilder builder) {
            this.required = builder._required;
            this.allowed = builder._allowed != null ? Collections.unmodifiableList(new ArrayList<>(builder._allowed))
                    : Collections.emptyList();
            this.severity = builder._severity;
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

        public static LanguageConfigBuilder builder() {
            return new LanguageConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class LanguageConfigBuilder {
            private boolean _required;
            private List<String> _allowed;
            private Severity _severity;

            public LanguageConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public LanguageConfigBuilder allowed(List<String> allowed) {
                this._allowed = allowed;
                return this;
            }

            public LanguageConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public LanguageConfig build() {
                return new LanguageConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof LanguageConfig that))
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
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private TitleConfig(TitleConfigBuilder builder) {
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

        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class TitleConfigBuilder {
            private boolean _required;
            private Pattern _pattern;
            private Severity _severity;

            public TitleConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public TitleConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public TitleConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
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
            return required == that.required && Objects
                    .equals(pattern == null ? null : pattern.pattern(),
                            that.pattern == null ? null : that.pattern.pattern())
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }

    @JsonDeserialize(builder = CalloutsConfig.CalloutsConfigBuilder.class)
    public static class CalloutsConfig {
        @JsonProperty(ALLOWED)
        private final boolean allowed;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private CalloutsConfig(CalloutsConfigBuilder builder) {
            this.allowed = builder._allowed;
            this.max = builder._max;
            this.severity = builder._severity;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Integer getMax() {
            return max;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static CalloutsConfigBuilder builder() {
            return new CalloutsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class CalloutsConfigBuilder {
            private boolean _allowed;
            private Integer _max;
            private Severity _severity;

            public CalloutsConfigBuilder allowed(boolean allowed) {
                this._allowed = allowed;
                return this;
            }

            public CalloutsConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public CalloutsConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public CalloutsConfig build() {
                return new CalloutsConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CalloutsConfig that))
                return false;
            return allowed == that.allowed && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowed, max, severity);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private LanguageConfig _language;
        private LineConfig _lines;
        private TitleConfig _title;
        private CalloutsConfig _callouts;

        public Builder language(LanguageConfig language) {
            this._language = language;
            return this;
        }

        public Builder lines(LineConfig lines) {
            this._lines = lines;
            return this;
        }

        public Builder title(TitleConfig title) {
            this._title = title;
            return this;
        }

        public Builder callouts(CalloutsConfig callouts) {
            this._callouts = callouts;
            return this;
        }

        @Override
        public ListingBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new ListingBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ListingBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(language, that.language) && Objects.equals(lines, that.lines)
                && Objects.equals(title, that.title) && Objects.equals(callouts, that.callouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), language, lines, title, callouts);
    }
}
