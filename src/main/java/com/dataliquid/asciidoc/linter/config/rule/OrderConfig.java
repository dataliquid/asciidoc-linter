package com.dataliquid.asciidoc.linter.config.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;

/**
 * Configuration for block ordering constraints.
 */
public final class OrderConfig {
    private final List<String> fixedOrder;
    private final List<OrderConstraint> before;
    private final List<OrderConstraint> after;
    private final Severity severity;
    
    private OrderConfig(Builder builder) {
        this.fixedOrder = Collections.unmodifiableList(new ArrayList<>(builder.fixedOrder));
        this.before = Collections.unmodifiableList(new ArrayList<>(builder.before));
        this.after = Collections.unmodifiableList(new ArrayList<>(builder.after));
        this.severity = builder.severity;
    }
    
    public List<String> fixedOrder() { return fixedOrder; }
    public List<OrderConstraint> before() { return before; }
    public List<OrderConstraint> after() { return after; }
    public Severity severity() { return severity; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Represents an order constraint between two blocks.
     */
    public static final class OrderConstraint {
        private final String first;
        private final String second;
        private final Severity severity;
        
        private OrderConstraint(String first, String second, Severity severity) {
            this.first = Objects.requireNonNull(first, "[" + getClass().getName() + "] first must not be null");
            this.second = Objects.requireNonNull(second, "[" + getClass().getName() + "] second must not be null");
            this.severity = Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
        }
        
        public String first() { return first; }
        public String second() { return second; }
        public Severity severity() { return severity; }
        
        public static OrderConstraint of(String first, String second, Severity severity) {
            return new OrderConstraint(first, second, severity);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderConstraint that = (OrderConstraint) o;
            return Objects.equals(first, that.first) &&
                   Objects.equals(second, that.second) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(first, second, severity);
        }
    }
    
    public static class Builder {
        private List<String> fixedOrder = new ArrayList<>();
        private List<OrderConstraint> before = new ArrayList<>();
        private List<OrderConstraint> after = new ArrayList<>();
        private Severity severity = Severity.ERROR;
        
        public Builder fixedOrder(List<String> fixedOrder) {
            this.fixedOrder = fixedOrder != null ? new ArrayList<>(fixedOrder) : new ArrayList<>();
            return this;
        }
        
        public Builder addFixedOrder(String blockName) {
            this.fixedOrder.add(blockName);
            return this;
        }
        
        public Builder before(List<OrderConstraint> before) {
            this.before = before != null ? new ArrayList<>(before) : new ArrayList<>();
            return this;
        }
        
        public Builder addBefore(String first, String second, Severity severity) {
            this.before.add(OrderConstraint.of(first, second, severity));
            return this;
        }
        
        public Builder after(List<OrderConstraint> after) {
            this.after = after != null ? new ArrayList<>(after) : new ArrayList<>();
            return this;
        }
        
        public Builder addAfter(String first, String second, Severity severity) {
            this.after.add(OrderConstraint.of(first, second, severity));
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
            return this;
        }
        
        public OrderConfig build() {
            return new OrderConfig(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderConfig that = (OrderConfig) o;
        return Objects.equals(fixedOrder, that.fixedOrder) &&
               Objects.equals(before, that.before) &&
               Objects.equals(after, that.after) &&
               severity == that.severity;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fixedOrder, before, after, severity);
    }
}