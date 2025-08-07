package com.dataliquid.asciidoc.linter.config.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.loader.BlockListDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Document.ALLOWED_BLOCKS;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Document.SUBSECTIONS;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = SectionConfig.Builder.class)
public final class SectionConfig {
    private final String name;
    private final Integer order;
    private final int level;
    private final int min;
    private final int max;
    private final TitleConfig title;
    private final List<Block> allowedBlocks;
    private final List<SectionConfig> subsections;

    private SectionConfig(Builder builder) {
        this.name = builder.name;
        this.order = builder.order;
        this.level = builder.level;
        this.min = builder.min;
        this.max = builder.max;
        this.title = builder.title;
        this.allowedBlocks = Collections.unmodifiableList(new ArrayList<>(builder.allowedBlocks));
        this.subsections = Collections.unmodifiableList(new ArrayList<>(builder.subsections));
    }

    @JsonProperty(NAME)
    public String name() { return name; }
    
    @JsonProperty(ORDER)
    public Integer order() { return order; }
    
    @JsonProperty(LEVEL)
    public int level() { return level; }
    
    @JsonProperty(MIN)
    public int min() { return min; }
    
    @JsonProperty(MAX)
    public int max() { return max; }
    
    @JsonProperty(TITLE)
    public TitleConfig title() { return title; }
    
    @JsonProperty(ALLOWED_BLOCKS)
    public List<Block> allowedBlocks() { return allowedBlocks; }
    
    @JsonProperty(SUBSECTIONS)
    public List<SectionConfig> subsections() { return subsections; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private String name;
        private Integer order;
        private int level;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private TitleConfig title;
        private List<Block> allowedBlocks = new ArrayList<>();
        private List<SectionConfig> subsections = new ArrayList<>();

        @JsonProperty(NAME)
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        @JsonProperty(LEVEL)
        public Builder level(int level) {
            this.level = level;
            return this;
        }

        @JsonProperty(MIN)
        public Builder min(int min) {
            this.min = min;
            return this;
        }

        @JsonProperty(MAX)
        public Builder max(int max) {
            this.max = max;
            return this;
        }

        @JsonProperty(TITLE)
        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }

        @JsonProperty(ALLOWED_BLOCKS)
        @JsonDeserialize(using = BlockListDeserializer.class)
        public Builder allowedBlocks(List<Block> allowedBlocks) {
            this.allowedBlocks = allowedBlocks != null ? new ArrayList<>(allowedBlocks) : new ArrayList<>();
            return this;
        }

        public Builder addAllowedBlock(Block block) {
            this.allowedBlocks.add(block);
            return this;
        }

        @JsonProperty(SUBSECTIONS)
        public Builder subsections(List<SectionConfig> subsections) {
            this.subsections = subsections != null ? new ArrayList<>(subsections) : new ArrayList<>();
            return this;
        }

        public Builder addSubsection(SectionConfig subsection) {
            this.subsections.add(subsection);
            return this;
        }

        public SectionConfig build() {
            return new SectionConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionConfig that = (SectionConfig) o;
        return level == that.level &&
               min == that.min &&
               max == that.max &&
               Objects.equals(name, that.name) &&
               Objects.equals(order, that.order) &&
               Objects.equals(title, that.title) &&
               Objects.equals(allowedBlocks, that.allowedBlocks) &&
               Objects.equals(subsections, that.subsections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, level, min, max, title, allowedBlocks, subsections);
    }
}