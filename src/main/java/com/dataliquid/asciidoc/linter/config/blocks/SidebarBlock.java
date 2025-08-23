package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TITLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CONTENT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Sidebar.POSITION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.LINES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ALLOWED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

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
@JsonDeserialize(builder = SidebarBlock.Builder.class)
public final class SidebarBlock extends AbstractBlock {
    @JsonProperty(TITLE)
    private final TitleConfig title;
    @JsonProperty(CONTENT)
    private final ContentConfig content;
    @JsonProperty(POSITION)
    private final PositionConfig position;

    private SidebarBlock(Builder builder) {
        super(builder);
        this.title = builder._title;
        this.content = builder._content;
        this.position = builder._position;
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

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
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

        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.pattern = builder._pattern;
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
            private Integer _minLength;
            private Integer _maxLength;
            private Pattern _pattern;
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

            @SuppressWarnings("PMD.NullAssignment")
            public TitleConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public TitleConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
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

    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(LINES)
        private final LinesConfig lines;

        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder._required;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.lines = builder._lines;
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

        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ContentConfigBuilder {
            private boolean _required;
            private Integer _minLength;
            private Integer _maxLength;
            private LinesConfig _lines;

            public ContentConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public ContentConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public ContentConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public ContentConfigBuilder lines(LinesConfig lines) {
                this._lines = lines;
                return this;
            }

            public ContentConfig build() {
                return new ContentConfig(this);
            }
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

    @JsonDeserialize(builder = LinesConfig.LinesConfigBuilder.class)
    public static class LinesConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private LinesConfig(LinesConfigBuilder builder) {
            this.min = builder._min;
            this.max = builder._max;
            this.severity = builder._severity;
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

        public static LinesConfigBuilder builder() {
            return new LinesConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class LinesConfigBuilder {
            private Integer _min;
            private Integer _max;
            private Severity _severity;

            public LinesConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public LinesConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public LinesConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public LinesConfig build() {
                return new LinesConfig(this);
            }
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

    @JsonDeserialize(builder = PositionConfig.PositionConfigBuilder.class)
    public static class PositionConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(ALLOWED)
        private final List<String> allowed;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private PositionConfig(PositionConfigBuilder builder) {
            this.required = builder._required;
            this.allowed = builder._allowed;
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

        public static PositionConfigBuilder builder() {
            return new PositionConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class PositionConfigBuilder {
            private boolean _required;
            private List<String> _allowed;
            private Severity _severity;

            public PositionConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public PositionConfigBuilder allowed(List<String> allowed) {
                this._allowed = allowed;
                return this;
            }

            public PositionConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public PositionConfig build() {
                return new PositionConfig(this);
            }
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private TitleConfig _title;
        private ContentConfig _content;
        private PositionConfig _position;

        public Builder title(TitleConfig title) {
            this._title = title;
            return this;
        }

        public Builder content(ContentConfig content) {
            this._content = content;
            return this;
        }

        public Builder position(PositionConfig position) {
            this._position = position;
            return this;
        }

        @Override
        public SidebarBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new SidebarBlock(this);
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
