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
        this.severity = severity;
        this.occurrence = occurrence;
        this.order = order;
    }

    protected AbstractBlock(AbstractBuilder<?> builder) {
        this(builder._name, builder._severity, builder._occurrence, builder._order);
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

    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        // Constants
        private static final String UNCHECKED_CAST_WARNING = "unchecked";

        protected String _name;
        protected Severity _severity;
        protected OccurrenceConfig _occurrence;
        protected Integer _order;

        @JsonProperty(NAME)
        @SuppressWarnings(UNCHECKED_CAST_WARNING)
        public T name(String name) {
            this._name = name;
            return (T) this;
        }

        @JsonProperty(SEVERITY)
        @SuppressWarnings(UNCHECKED_CAST_WARNING)
        public T severity(Severity severity) {
            this._severity = severity;
            return (T) this;
        }

        @JsonProperty(OCCURRENCE)
        @SuppressWarnings(UNCHECKED_CAST_WARNING)
        public T occurrence(OccurrenceConfig occurrence) {
            this._occurrence = occurrence;
            return (T) this;
        }

        @JsonProperty(ORDER)
        @SuppressWarnings(UNCHECKED_CAST_WARNING)
        public T order(Integer order) {
            this._order = order;
            return (T) this;
        }

        public abstract AbstractBlock build();
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
