package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public final class ListingBlock extends AbstractBlock {
    private static final String SEVERITY = "severity";
    private final LanguageConfig language;
    private final LineConfig lines;
    private final TitleConfig title;
    private final CalloutsConfig callouts;

    @JsonCreator
    public ListingBlock(@JsonProperty("name") String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("language") LanguageConfig language, @JsonProperty("lines") LineConfig lines,
            @JsonProperty("title") TitleConfig title, @JsonProperty("callouts") CalloutsConfig callouts) {
        super(name, severity, occurrence, order);
        this.language = language;
        this.lines = lines;
        this.title = title;
        this.callouts = callouts;
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

    @JsonDeserialize
    public static class LanguageConfig {
        private final boolean required;
        private final List<String> allowed;
        private final Severity severity;

        @JsonCreator
        public LanguageConfig(@JsonProperty("required") boolean required, @JsonProperty("allowed") List<String> allowed,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.allowed = allowed != null ? Collections.unmodifiableList(new ArrayList<>(allowed))
                    : Collections.emptyList();
            this.severity = severity;
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

    @JsonDeserialize
    public static class TitleConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public TitleConfig(@JsonProperty("required") boolean required, @JsonProperty("pattern") String patternString,
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

    @JsonDeserialize
    public static class CalloutsConfig {
        private final boolean allowed;
        private final Integer max;
        private final Severity severity;

        @JsonCreator
        public CalloutsConfig(@JsonProperty("allowed") boolean allowed, @JsonProperty("max") Integer max,
                @JsonProperty(SEVERITY) Severity severity) {
            this.allowed = allowed;
            this.max = max;
            this.severity = severity;
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
