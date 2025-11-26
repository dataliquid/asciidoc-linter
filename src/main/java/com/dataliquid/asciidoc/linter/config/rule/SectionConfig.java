package com.dataliquid.asciidoc.linter.config.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.loader.BlockListDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.ALLOWED_BLOCKS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.SUBSECTIONS;

public final class SectionConfig {
    private final String nameValue;
    private final Integer orderValue;
    private final int levelValue;
    private final OccurrenceConfig occurrenceValue;
    private final TitleConfig titleValue;
    private final List<Block> allowedBlocksValue;
    private final List<SectionConfig> subsectionsValue;

    @JsonCreator
    public SectionConfig(@JsonProperty(NAME) String name, @JsonProperty(ORDER) Integer order,
            @JsonProperty(LEVEL) int level, @JsonProperty(OCCURRENCE) OccurrenceConfig occurrence,
            @JsonProperty(TITLE) TitleConfig title,
            @JsonProperty(ALLOWED_BLOCKS) @JsonDeserialize(using = BlockListDeserializer.class) List<Block> allowedBlocks,
            @JsonProperty(SUBSECTIONS) List<SectionConfig> subsections) {
        this.nameValue = name;
        this.orderValue = order;
        this.levelValue = level;
        this.occurrenceValue = occurrence;
        this.titleValue = title;
        this.allowedBlocksValue = allowedBlocks != null ? Collections.unmodifiableList(new ArrayList<>(allowedBlocks))
                : Collections.emptyList();
        this.subsectionsValue = subsections != null ? Collections.unmodifiableList(new ArrayList<>(subsections))
                : Collections.emptyList();
    }

    @JsonProperty(NAME)
    public String name() {
        return this.nameValue;
    }

    @JsonProperty(ORDER)
    public Integer order() {
        return this.orderValue;
    }

    @JsonProperty(LEVEL)
    public int level() {
        return this.levelValue;
    }

    @JsonProperty(OCCURRENCE)
    public OccurrenceConfig occurrence() {
        return this.occurrenceValue;
    }

    @JsonProperty(TITLE)
    public TitleConfig title() {
        return this.titleValue;
    }

    @JsonProperty(ALLOWED_BLOCKS)
    public List<Block> allowedBlocks() {
        return this.allowedBlocksValue;
    }

    @JsonProperty(SUBSECTIONS)
    public List<SectionConfig> subsections() {
        return this.subsectionsValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SectionConfig that = (SectionConfig) o;
        return levelValue == that.levelValue && Objects.equals(occurrenceValue, that.occurrenceValue)
                && Objects.equals(nameValue, that.nameValue) && Objects.equals(orderValue, that.orderValue)
                && Objects.equals(titleValue, that.titleValue)
                && Objects.equals(allowedBlocksValue, that.allowedBlocksValue)
                && Objects.equals(subsectionsValue, that.subsectionsValue);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(nameValue, orderValue, levelValue, occurrenceValue, titleValue, allowedBlocksValue,
                        subsectionsValue);
    }
}