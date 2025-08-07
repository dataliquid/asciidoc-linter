package com.dataliquid.asciidoc.linter.validator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

public final class OrderRule implements AttributeRule {
    private final Map<String, OrderConfig> orderConfigs;
    private final Map<String, AttributePosition> actualPositions = new HashMap<>();

    private OrderRule(Builder builder) {
        this.orderConfigs = Collections.unmodifiableMap(new HashMap<>(builder.orderConfigs));
    }

    @Override
    public String getRuleId() {
        return "metadata.order";
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        actualPositions.put(attributeName, new AttributePosition(location, actualPositions.size() + 1));
        return Collections.emptyList();
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return orderConfigs.containsKey(attributeName);
    }

    public List<ValidationMessage> validateOrder() {
        List<ValidationMessage> messages = new ArrayList<>();
        
        for (Map.Entry<String, OrderConfig> entry : orderConfigs.entrySet()) {
            String attrName = entry.getKey();
            OrderConfig config = entry.getValue();
            AttributePosition actual = actualPositions.get(attrName);
            
            if (actual != null && config.hasOrder()) {
                for (Map.Entry<String, OrderConfig> otherEntry : orderConfigs.entrySet()) {
                    String otherAttrName = otherEntry.getKey();
                    OrderConfig otherConfig = otherEntry.getValue();
                    AttributePosition otherActual = actualPositions.get(otherAttrName);
                    
                    if (!attrName.equals(otherAttrName) && otherActual != null && otherConfig.hasOrder()) {
                        if (config.getOrder() < otherConfig.getOrder() && actual.position > otherActual.position) {
                            messages.add(ValidationMessage.builder()
                                .severity(config.getSeverity())
                                .ruleId(getRuleId())
                                .message("Attribute '" + attrName + "' should appear before '" + otherAttrName + "': actual position line " + actual.location.getStartLine() + ", expected before line " + otherActual.location.getStartLine())
                                .location(actual.location)
                                .attributeName(attrName)
                                .actualValue("Line " + actual.location.getStartLine())
                                .expectedValue("Before line " + otherActual.location.getStartLine())
                                .build());
                        }
                    }
                }
            }
        }
        
        return messages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, OrderConfig> orderConfigs = new HashMap<>();

        private Builder() {
        }

        public Builder addOrderConstraint(String attributeName, Integer order, Severity severity) {
            Objects.requireNonNull(attributeName, "[" + getClass().getName() + "] attributeName must not be null");
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity must not be null");
            
            orderConfigs.put(attributeName, new OrderConfig(order, severity));
            return this;
        }

        public OrderRule build() {
            return new OrderRule(this);
        }
    }

    private static final class OrderConfig {
        private final Integer order;
        private final Severity severity;

        OrderConfig(Integer order, Severity severity) {
            this.order = order;
            this.severity = severity;
        }

        boolean hasOrder() {
            return order != null;
        }

        int getOrder() {
            return order != null ? order : Integer.MAX_VALUE;
        }

        Severity getSeverity() {
            return severity;
        }
    }

    private static final class AttributePosition {
        private final SourceLocation location;
        private final int position;

        AttributePosition(SourceLocation location, int position) {
            this.location = location;
            this.position = position;
        }
    }
}