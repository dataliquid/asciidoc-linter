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
    private final List<String> fixedOrderValue;
    private final List<OrderConstraint> beforeValue;
    private final List<OrderConstraint> afterValue;
    private final Severity severityValue;

    public OrderConfig(List<String> fixedOrder, List<OrderConstraint> before, List<OrderConstraint> after,
            Severity severity) {
        this.fixedOrderValue = fixedOrder != null ? Collections.unmodifiableList(new ArrayList<>(fixedOrder))
                : Collections.emptyList();
        this.beforeValue = before != null ? Collections.unmodifiableList(new ArrayList<>(before))
                : Collections.emptyList();
        this.afterValue = after != null ? Collections.unmodifiableList(new ArrayList<>(after))
                : Collections.emptyList();
        this.severityValue = severity != null ? severity : Severity.ERROR;
    }

    public List<String> fixedOrder() {
        return this.fixedOrderValue;
    }

    public List<OrderConstraint> before() {
        return this.beforeValue;
    }

    public List<OrderConstraint> after() {
        return this.afterValue;
    }

    public Severity severity() {
        return this.severityValue;
    }

    /**
     * Represents an order constraint between two blocks.
     */
    public static final class OrderConstraint {
        private final String firstValue;
        private final String secondValue;
        private final Severity severityValue;

        private OrderConstraint(String first, String second, Severity severity) {
            this.firstValue = Objects.requireNonNull(first, "first must not be null");
            this.secondValue = Objects.requireNonNull(second, "second must not be null");
            this.severityValue = Objects.requireNonNull(severity, "severity must not be null");
        }

        public String first() {
            return this.firstValue;
        }

        public String second() {
            return this.secondValue;
        }

        public Severity severity() {
            return this.severityValue;
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
            return Objects.equals(firstValue, that.firstValue) && Objects.equals(secondValue, that.secondValue)
                    && severityValue == that.severityValue;
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstValue, secondValue, severityValue);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderConfig that = (OrderConfig) o;
        return Objects.equals(fixedOrderValue, that.fixedOrderValue) && Objects.equals(beforeValue, that.beforeValue)
                && Objects.equals(afterValue, that.afterValue) && severityValue == that.severityValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fixedOrderValue, beforeValue, afterValue, severityValue);
    }
}
