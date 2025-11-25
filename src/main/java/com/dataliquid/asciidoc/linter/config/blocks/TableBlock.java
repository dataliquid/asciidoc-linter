package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.COLUMNS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.ROWS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.HEADER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CAPTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.FORMAT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.BORDERS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = TableBlock.Builder.class)
public final class TableBlock extends AbstractBlock {
    @JsonProperty(COLUMNS)
    private final DimensionConfig columns;
    @JsonProperty(ROWS)
    private final DimensionConfig rows;
    @JsonProperty(HEADER)
    private final HeaderConfig header;
    @JsonProperty(CAPTION)
    private final CaptionConfig caption;
    @JsonProperty(FORMAT)
    private final FormatConfig format;

    private TableBlock(Builder builder) {
        super(builder);
        this.columns = builder._columns;
        this.rows = builder._rows;
        this.header = builder._header;
        this.caption = builder._caption;
        this.format = builder._format;
    }

    @Override
    public BlockType getType() {
        return BlockType.TABLE;
    }

    public DimensionConfig getColumns() {
        return columns;
    }

    public DimensionConfig getRows() {
        return rows;
    }

    public HeaderConfig getHeader() {
        return header;
    }

    public CaptionConfig getCaption() {
        return caption;
    }

    public FormatConfig getFormat() {
        return format;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private DimensionConfig(DimensionConfigBuilder builder) {
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

        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class DimensionConfigBuilder {
            private Integer _min;
            private Integer _max;
            private Severity _severity;

            public DimensionConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public DimensionConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public DimensionConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public DimensionConfig build() {
                return new DimensionConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DimensionConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }

    @JsonDeserialize(builder = HeaderConfig.HeaderConfigBuilder.class)
    public static class HeaderConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private HeaderConfig(HeaderConfigBuilder builder) {
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

        public static HeaderConfigBuilder builder() {
            return new HeaderConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class HeaderConfigBuilder {
            private boolean _required;
            private Pattern _pattern;
            private Severity _severity;

            public HeaderConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public HeaderConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public HeaderConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public HeaderConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public HeaderConfig build() {
                return new HeaderConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HeaderConfig that))
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

    @JsonDeserialize(builder = CaptionConfig.CaptionConfigBuilder.class)
    public static class CaptionConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(MIN_LENGTH)
        private final Integer minLength;
        @JsonProperty(MAX_LENGTH)
        private final Integer maxLength;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private CaptionConfig(CaptionConfigBuilder builder) {
            this.required = builder._required;
            this.pattern = builder._pattern;
            this.minLength = builder._minLength;
            this.maxLength = builder._maxLength;
            this.severity = builder._severity;
        }

        public boolean isRequired() {
            return required;
        }

        public Pattern getPattern() {
            return pattern;
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

        public static CaptionConfigBuilder builder() {
            return new CaptionConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class CaptionConfigBuilder {
            private boolean _required;
            private Pattern _pattern;
            private Integer _minLength;
            private Integer _maxLength;
            private Severity _severity;

            public CaptionConfigBuilder required(boolean required) {
                this._required = required;
                return this;
            }

            public CaptionConfigBuilder pattern(Pattern pattern) {
                this._pattern = pattern;
                return this;
            }

            @SuppressWarnings("PMD.NullAssignment")
            public CaptionConfigBuilder pattern(String pattern) {
                this._pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }

            public CaptionConfigBuilder minLength(Integer minLength) {
                this._minLength = minLength;
                return this;
            }

            public CaptionConfigBuilder maxLength(Integer maxLength) {
                this._maxLength = maxLength;
                return this;
            }

            public CaptionConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CaptionConfig that))
                return false;
            return required == that.required
                    && Objects
                            .equals(pattern == null ? null : pattern.pattern(),
                                    that.pattern == null ? null : that.pattern.pattern())
                    && Objects.equals(minLength, that.minLength) && Objects.equals(maxLength, that.maxLength)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), minLength, maxLength, severity);
        }
    }

    @JsonDeserialize(builder = FormatConfig.FormatConfigBuilder.class)
    public static class FormatConfig {
        @JsonProperty(STYLE)
        private final String style;
        @JsonProperty(BORDERS)
        private final Boolean borders;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private FormatConfig(FormatConfigBuilder builder) {
            this.style = builder._style;
            this.borders = builder._borders;
            this.severity = builder._severity;
        }

        public String getStyle() {
            return style;
        }

        public Boolean getBorders() {
            return borders;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static FormatConfigBuilder builder() {
            return new FormatConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class FormatConfigBuilder {
            private String _style;
            private Boolean _borders;
            private Severity _severity;

            public FormatConfigBuilder style(String style) {
                this._style = style;
                return this;
            }

            public FormatConfigBuilder borders(Boolean borders) {
                this._borders = borders;
                return this;
            }

            public FormatConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public FormatConfig build() {
                return new FormatConfig(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof FormatConfig that))
                return false;
            return Objects.equals(style, that.style) && Objects.equals(borders, that.borders)
                    && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(style, borders, severity);
        }
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private DimensionConfig _columns;
        private DimensionConfig _rows;
        private HeaderConfig _header;
        private CaptionConfig _caption;
        private FormatConfig _format;

        public Builder columns(DimensionConfig columns) {
            this._columns = columns;
            return this;
        }

        public Builder rows(DimensionConfig rows) {
            this._rows = rows;
            return this;
        }

        public Builder header(HeaderConfig header) {
            this._header = header;
            return this;
        }

        public Builder caption(CaptionConfig caption) {
            this._caption = caption;
            return this;
        }

        public Builder format(FormatConfig format) {
            this._format = format;
            return this;
        }

        @Override
        public TableBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new TableBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TableBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(columns, that.columns) && Objects.equals(rows, that.rows)
                && Objects.equals(header, that.header) && Objects.equals(caption, that.caption)
                && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), columns, rows, header, caption, format);
    }
}
