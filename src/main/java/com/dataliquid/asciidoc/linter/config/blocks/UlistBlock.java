package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration for unordered list (ulist) blocks in AsciiDoc. Represents
 * bullet point lists with * or - markers.
 */
@JsonDeserialize
public final class UlistBlock extends AbstractBlock {
    private final ItemsConfig items;
    private final NestingLevelConfig nestingLevel;
    private final String markerStyle;

    @JsonCreator
    public UlistBlock(@JsonProperty("name") String name, @JsonProperty("severity") Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("items") ItemsConfig items, @JsonProperty("nestingLevel") NestingLevelConfig nestingLevel,
            @JsonProperty("markerStyle") String markerStyle) {
        super(name, severity, occurrence, order);
        this.items = items;
        this.nestingLevel = nestingLevel;
        this.markerStyle = markerStyle;
    }

    @Override
    public BlockType getType() {
        return BlockType.ULIST;
    }

    public ItemsConfig getItems() {
        return items;
    }

    public NestingLevelConfig getNestingLevel() {
        return nestingLevel;
    }

    public String getMarkerStyle() {
        return markerStyle;
    }

    /**
     * Configuration for list items count validation.
     */
    @JsonDeserialize
    public static class ItemsConfig {
        private final Integer min;
        private final Integer max;
        private final Severity severity;

        @JsonCreator
        public ItemsConfig(@JsonProperty("min") Integer min, @JsonProperty("max") Integer max,
                @JsonProperty("severity") Severity severity) {
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
            if (!(o instanceof ItemsConfig that))
                return false;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }

    /**
     * Configuration for nesting level validation.
     */
    @JsonDeserialize
    public static class NestingLevelConfig {
        private final Integer max;
        private final Severity severity;

        @JsonCreator
        public NestingLevelConfig(@JsonProperty("max") Integer max, @JsonProperty("severity") Severity severity) {
            this.max = max;
            this.severity = severity;
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
            if (!(o instanceof NestingLevelConfig that))
                return false;
            return Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(max, severity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UlistBlock that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(items, that.items) && Objects.equals(nestingLevel, that.nestingLevel)
                && Objects.equals(markerStyle, that.markerStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), items, nestingLevel, markerStyle);
    }
}
