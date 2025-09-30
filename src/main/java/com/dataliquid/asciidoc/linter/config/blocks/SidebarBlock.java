package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Sidebar.POSITION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration for sidebar blocks in AsciiDoc. Sidebar blocks are used for
 * supplementary information displayed alongside the main content.
 * <p>
 * Example usage:
 *
 * <pre>
 * ****
 * Sidebar content here
 * ****
 * </pre>
 * <p>
 * Validation is based on the YAML schema configuration for sidebar blocks.
 */
@JsonDeserialize
public final class SidebarBlock extends AbstractBlock {
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(CONTENT)
    private final ContentConfig content;
    @JsonProperty(POSITION)
    private final PositionConfig position;

    @JsonCreator
    public SidebarBlock(@JsonProperty(NAME) String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence, @JsonProperty(ORDER) Integer order,
            @JsonProperty(TITLE) TitleConfig title, @JsonProperty(CONTENT) ContentConfig content,
            @JsonProperty(POSITION) PositionConfig position) {
        super(name, severity, occurrence, order);
        this.title = title;
        this.content = content;
        this.position = position;
    }

    @Override
    public BlockType getType() {
        return BlockType.SIDEBAR;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public ContentConfig getContent() {
        return content;
    }

    public PositionConfig getPosition() {
        return position;
    }

    @JsonDeserialize
    public static class TitleConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public TitleConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
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
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength)
                    && Objects
                            .equals(pattern != null ? pattern.pattern() : null,
                                    that.pattern != null ? that.pattern.pattern() : null)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, pattern != null ? pattern.pattern() : null, severity);
        }
    }

    @JsonDeserialize
    public static class ContentConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(LINES)
        private final LinesConfig lines;

        @JsonCreator
        public ContentConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(MIN_LENGTH) Integer minLength,
                @JsonProperty(MAX_LENGTH) Integer maxLength, @JsonProperty(LINES) LinesConfig lines) {
            this.required = required;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.lines = lines;
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

        public LinesConfig getLines() {
            return lines;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ContentConfig that))
                return false;
            return required == that.required && Objects.equals(minLength, that.minLength)
                    && Objects.equals(maxLength, that.maxLength) && Objects.equals(lines, that.lines);
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines);
        }
    }

    @JsonDeserialize
    public static class LinesConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public LinesConfig(@JsonProperty(MIN) Integer min, @JsonProperty(MAX) Integer max,
                @JsonProperty(SEVERITY) Severity severity) {
            this.min = min;
            this.max = max;
            this.severity = severity;
        }

        public Integer getMin() {
            return min;
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
            if (!(o instanceof LinesConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }

    @JsonDeserialize
    public static class PositionConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(ALLOWED)
        private final List<String> allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public PositionConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(ALLOWED) List<String> allowed,
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
            if (!(o instanceof PositionConfig that))
                return false;
            return required == that.required && Objects.equals(allowed, that.allowed) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SidebarBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(title, that.title) && Objects.equals(content, that.content)
                && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, content, position);
    }
}
