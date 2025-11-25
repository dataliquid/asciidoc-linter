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

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.ALLOWED_BLOCKS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.SUBSECTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = SectionConfig.Builder.class)
public final class SectionConfig {
    private final String _name;
    private final Integer _order;
    private final int _level;
    private final OccurrenceConfig _occurrence;
    private final TitleConfig _title;
    private final List<Block> _allowedBlocks;
    private final List<SectionConfig> _subsections;

    private SectionConfig(Builder builder) {
        this._name = builder._name;
        this._order = builder._order;
        this._level = builder._level;
        this._occurrence = builder._occurrence;
        this._title = builder._title;
        this._allowedBlocks = Collections.unmodifiableList(new ArrayList<>(builder._allowedBlocks));
        this._subsections = Collections.unmodifiableList(new ArrayList<>(builder._subsections));
    }

    @JsonProperty(NAME)
    public String name() {
        return this._name;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this._order;
    }

    @JsonProperty(LEVEL)
    public int level() {
        return this._level;
    }

    @JsonProperty(OCCURRENCE)
    public OccurrenceConfig occurrence() {
        return this._occurrence;
    }

    @JsonProperty(TITLE)
    public TitleConfig title() {
        return this._title;
    }

    @JsonProperty(ALLOWED_BLOCKS)
    public List<Block> allowedBlocks() {
        return this._allowedBlocks;
    }

    @JsonProperty(SUBSECTIONS)
    public List<SectionConfig> subsections() {
        return this._subsections;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private String _name;
        private Integer _order;
        private int _level;
        private OccurrenceConfig _occurrence;
        private TitleConfig _title;
        private List<Block> _allowedBlocks = new ArrayList<>();
        private List<SectionConfig> _subsections = new ArrayList<>();

        @JsonProperty(NAME)
        public Builder name(String name) {
            this._name = name;
            return this;
        }

        @JsonProperty(ORDER)
        public Builder order(Integer order) {
            this._order = order;
            return this;
        }

        @JsonProperty(LEVEL)
        public Builder level(int level) {
            this._level = level;
            return this;
        }

        @JsonProperty(OCCURRENCE)
        public Builder occurrence(OccurrenceConfig occurrence) {
            this._occurrence = occurrence;
            return this;
        }

        @JsonProperty(TITLE)
        public Builder title(TitleConfig title) {
            this._title = title;
            return this;
        }

        @JsonProperty(ALLOWED_BLOCKS)
        @JsonDeserialize(using = BlockListDeserializer.class)
        public Builder allowedBlocks(List<Block> allowedBlocks) {
            this._allowedBlocks = allowedBlocks != null ? new ArrayList<>(allowedBlocks) : new ArrayList<>();
            return this;
        }

        public Builder addAllowedBlock(Block block) {
            this._allowedBlocks.add(block);
            return this;
        }

        @JsonProperty(SUBSECTIONS)
        public Builder subsections(List<SectionConfig> subsections) {
            this._subsections = subsections != null ? new ArrayList<>(subsections) : new ArrayList<>();
            return this;
        }

        public Builder addSubsection(SectionConfig subsection) {
            this._subsections.add(subsection);
            return this;
        }

        public SectionConfig build() {
            return new SectionConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SectionConfig that = (SectionConfig) o;
        return _level == that._level && Objects.equals(_occurrence, that._occurrence)
                && Objects.equals(_name, that._name) && Objects.equals(_order, that._order)
                && Objects.equals(_title, that._title) && Objects.equals(_allowedBlocks, that._allowedBlocks)
                && Objects.equals(_subsections, that._subsections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _order, _level, _occurrence, _title, _allowedBlocks, _subsections);
    }
}
