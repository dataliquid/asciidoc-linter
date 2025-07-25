package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractBlock implements Block {
    private final String name;
    private final Severity severity;
    private final OccurrenceConfig occurrence;
    private final Integer order;
    
    protected AbstractBlock(AbstractBuilder<?> builder) {
        this.name = builder.name;
        this.severity = builder.severity;
        this.occurrence = builder.occurrence;
        this.order = builder.order;
    }
    
    public abstract BlockType getType();
    
    public String getName() { return name; }
    public Severity getSeverity() { return severity; }
    public OccurrenceConfig getOccurrence() { return occurrence; }
    public Integer getOrder() { return order; }
    
    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        protected String name;
        protected Severity severity;
        protected OccurrenceConfig occurrence;
        protected Integer order;
        
        @JsonProperty("name")
        @SuppressWarnings("unchecked")
        public T name(String name) {
            this.name = name;
            return (T) this;
        }
        
        @JsonProperty("severity")
        @SuppressWarnings("unchecked")
        public T severity(Severity severity) {
            this.severity = severity;
            return (T) this;
        }
        
        @JsonProperty("occurrence")
        @SuppressWarnings("unchecked")
        public T occurrence(OccurrenceConfig occurrence) {
            this.occurrence = occurrence;
            return (T) this;
        }
        
        @JsonProperty("order")
        @SuppressWarnings("unchecked")
        public T order(Integer order) {
            this.order = order;
            return (T) this;
        }
        
        public abstract AbstractBlock build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return Objects.equals(name, that.name) &&
               severity == that.severity &&
               Objects.equals(occurrence, that.occurrence) &&
               Objects.equals(order, that.order);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, severity, occurrence, order);
    }
}