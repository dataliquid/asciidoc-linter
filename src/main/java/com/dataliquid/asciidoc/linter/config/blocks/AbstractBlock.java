package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;

public abstract class AbstractBlock implements Block {
    private final String name;
    private final Severity severity;
    private final OccurrenceConfig occurrence;
    private final Integer order;

    protected AbstractBlock(String name, Severity severity, OccurrenceConfig occurrence, Integer order) {
        this.name = name;
        this.severity = Objects.requireNonNull(severity, "severity is required");
        this.occurrence = occurrence;
        this.order = order;
    }

    @Override
    public abstract BlockType getType();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public OccurrenceConfig getOccurrence() {
        return occurrence;
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractBlock that = (AbstractBlock) o;
        return Objects.equals(name, that.name) && severity == that.severity
                && Objects.equals(occurrence, that.occurrence) && Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, severity, occurrence, order);
    }
}
