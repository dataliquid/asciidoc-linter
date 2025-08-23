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
    private final List<String> _fixedOrder;
    private final List<OrderConstraint> _before;
    private final List<OrderConstraint> _after;
    private final Severity _severity;

    private OrderConfig(Builder builder) {
        this._fixedOrder = Collections.unmodifiableList(new ArrayList<>(builder._fixedOrder));
        this._before = Collections.unmodifiableList(new ArrayList<>(builder._before));
        this._after = Collections.unmodifiableList(new ArrayList<>(builder._after));
        this._severity = builder._severity;
    }

    public List<String> fixedOrder() {
        return this._fixedOrder;
    }

    public List<OrderConstraint> before() {
        return this._before;
    }

    public List<OrderConstraint> after() {
        return this._after;
    }

    public Severity severity() {
        return this._severity;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents an order constraint between two blocks.
     */
    public static final class OrderConstraint {
        private final String _first;
        private final String _second;
        private final Severity _severity;

        private OrderConstraint(String first, String second, Severity severity) {
            this._first = Objects.requireNonNull(first, "[" + getClass().getName() + "] first must not be null");
            this._second = Objects.requireNonNull(second, "[" + getClass().getName() + "] second must not be null");
            this._severity = Objects
                    .requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
        }

        public String first() {
            return this._first;
        }

        public String second() {
            return this._second;
        }

        public Severity severity() {
            return this._severity;
        }

        public static OrderConstraint of(String first, String second, Severity severity) {
            return new OrderConstraint(first, second, severity);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            OrderConstraint that = (OrderConstraint) o;
            return Objects.equals(_first, that._first) && Objects.equals(_second, that._second)
                    && _severity == that._severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(_first, _second, _severity);
        }
    }

    public static class Builder {
        private List<String> _fixedOrder = new ArrayList<>();
        private List<OrderConstraint> _before = new ArrayList<>();
        private List<OrderConstraint> _after = new ArrayList<>();
        private Severity _severity = Severity.ERROR;

        public Builder fixedOrder(List<String> fixedOrder) {
            this._fixedOrder = fixedOrder != null ? new ArrayList<>(fixedOrder) : new ArrayList<>();
            return this;
        }

        public Builder addFixedOrder(String blockName) {
            this._fixedOrder.add(blockName);
            return this;
        }

        public Builder before(List<OrderConstraint> before) {
            this._before = before != null ? new ArrayList<>(before) : new ArrayList<>();
            return this;
        }

        public Builder addBefore(String first, String second, Severity severity) {
            this._before.add(OrderConstraint.of(first, second, severity));
            return this;
        }

        public Builder after(List<OrderConstraint> after) {
            this._after = after != null ? new ArrayList<>(after) : new ArrayList<>();
            return this;
        }

        public Builder addAfter(String first, String second, Severity severity) {
            this._after.add(OrderConstraint.of(first, second, severity));
            return this;
        }

        public Builder severity(Severity severity) {
            this._severity = Objects
                    .requireNonNull(_severity, "[" + getClass().getName() + "] severity must not be null");
            return this;
        }

        public OrderConfig build() {
            return new OrderConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderConfig that = (OrderConfig) o;
        return Objects.equals(_fixedOrder, that._fixedOrder) && Objects.equals(_before, that._before)
                && Objects.equals(_after, that._after) && _severity == that._severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_fixedOrder, _before, _after, _severity);
    }
}
