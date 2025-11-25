package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.ITEMS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.NESTING_LEVEL;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.List.MARKER_STYLE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MIN;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.MAX;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for unordered list (ulist) blocks in AsciiDoc. Represents
 * bullet point lists with * or - markers.
 */
@JsonDeserialize(builder = UlistBlock.Builder.class)
public final class UlistBlock extends AbstractBlock {
    @JsonProperty(ITEMS)
    private final ItemsConfig items;
    @JsonProperty(NESTING_LEVEL)
    private final NestingLevelConfig nestingLevel;
    @JsonProperty(MARKER_STYLE)
    private final String markerStyle;

    private UlistBlock(Builder builder) {
        super(builder);
        this.items = builder._items;
        this.nestingLevel = builder._nestingLevel;
        this.markerStyle = builder._markerStyle;
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Configuration for list items count validation.
     */
    @JsonDeserialize(builder = ItemsConfig.ItemsConfigBuilder.class)
    public static class ItemsConfig {
        @JsonProperty(MIN)
        private final Integer min;
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private ItemsConfig(ItemsConfigBuilder builder) {
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

        public static ItemsConfigBuilder builder() {
            return new ItemsConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class ItemsConfigBuilder {
            private Integer _min;
            private Integer _max;
            private Severity _severity;

            public ItemsConfigBuilder min(Integer min) {
                this._min = min;
                return this;
            }

            public ItemsConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public ItemsConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public ItemsConfig build() {
                return new ItemsConfig(this);
            }
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
    @JsonDeserialize(builder = NestingLevelConfig.NestingLevelConfigBuilder.class)
    public static class NestingLevelConfig {
        @JsonProperty(MAX)
        private final Integer max;
        @JsonProperty(SEVERITY)
        private final Severity severity;

        private NestingLevelConfig(NestingLevelConfigBuilder builder) {
            this.max = builder._max;
            this.severity = builder._severity;
        }

        public Integer getMax() {
            return max;
        }

        public Severity getSeverity() {
            return severity;
        }

        public static NestingLevelConfigBuilder builder() {
            return new NestingLevelConfigBuilder();
        }

        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class NestingLevelConfigBuilder {
            private Integer _max;
            private Severity _severity;

            public NestingLevelConfigBuilder max(Integer max) {
                this._max = max;
                return this;
            }

            public NestingLevelConfigBuilder severity(Severity severity) {
                this._severity = severity;
                return this;
            }

            public NestingLevelConfig build() {
                return new NestingLevelConfig(this);
            }
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

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private ItemsConfig _items;
        private NestingLevelConfig _nestingLevel;
        private String _markerStyle;

        public Builder items(ItemsConfig items) {
            this._items = items;
            return this;
        }

        public Builder nestingLevel(NestingLevelConfig nestingLevel) {
            this._nestingLevel = nestingLevel;
            return this;
        }

        public Builder markerStyle(String markerStyle) {
            this._markerStyle = markerStyle;
            return this;
        }

        @Override
        public UlistBlock build() {
            Objects.requireNonNull(_severity, "[" + getClass().getName() + "] severity is required");
            return new UlistBlock(this);
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
