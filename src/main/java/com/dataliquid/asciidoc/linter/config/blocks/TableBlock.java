package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.COLUMNS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.ROWS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.HEADER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.CAPTION;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.FORMAT;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.REQUIRED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.PATTERN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX_LENGTH;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Table.BORDERS;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
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

    @JsonCreator
    public TableBlock(@JsonProperty(NAME) String name, @JsonProperty(SEVERITY) Severity severity,
            @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence, @JsonProperty(ORDER) Integer order,
            @JsonProperty(COLUMNS) DimensionConfig columns, @JsonProperty(ROWS) DimensionConfig rows,
            @JsonProperty(HEADER) HeaderConfig header, @JsonProperty(CAPTION) CaptionConfig caption,
            @JsonProperty(FORMAT) FormatConfig format) {
        super(name, severity, occurrence, order);
        this.columns = columns;
        this.rows = rows;
        this.header = header;
        this.caption = caption;
        this.format = format;
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

    @JsonDeserialize
    public static class DimensionConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public DimensionConfig(@JsonProperty(MIN) Integer min, @JsonProperty(MAX) Integer max,
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
            if (!(o instanceof DimensionConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }

    @JsonDeserialize
    public static class HeaderConfig {
        @JsonProperty(REQUIRED)
        private final boolean required;
        @JsonProperty(PATTERN)
        private final Pattern pattern;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public HeaderConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(PATTERN) String patternString,
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

    @JsonDeserialize
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

        @JsonCreator
        @SuppressWarnings("PMD.NullAssignment")
        public CaptionConfig(@JsonProperty(REQUIRED) boolean required, @JsonProperty(PATTERN) String patternString,
                @JsonProperty(MIN_LENGTH) Integer minLength, @JsonProperty(MAX_LENGTH) Integer maxLength,
                @JsonProperty(SEVERITY) Severity severity) {
            this.required = required;
            this.pattern = patternString != null ? Pattern.compile(patternString) : null;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.severity = severity;
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

    @JsonDeserialize
    public static class FormatConfig {
        @JsonProperty(STYLE)
        private final String style;
        @JsonProperty(BORDERS)
        private final Boolean borders;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public FormatConfig(@JsonProperty(STYLE) String style, @JsonProperty(BORDERS) Boolean borders,
                @JsonProperty(SEVERITY) Severity severity) {
            this.style = style;
            this.borders = borders;
            this.severity = severity;
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
