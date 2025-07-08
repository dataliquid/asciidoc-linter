package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.asciidoctor.ast.Document;

import com.dataliquid.asciidoc.linter.config.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.validator.rules.AttributeRule;
import com.dataliquid.asciidoc.linter.validator.rules.LengthRule;
import com.dataliquid.asciidoc.linter.validator.rules.OrderRule;
import com.dataliquid.asciidoc.linter.validator.rules.PatternRule;
import com.dataliquid.asciidoc.linter.validator.rules.RequiredRule;

public final class MetadataValidator {
    private final List<AttributeRule> rules;

    private MetadataValidator(Builder builder) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(builder.rules));
    }

    public ValidationResult validate(Document document) {
        long startTime = System.currentTimeMillis();
        ValidationResult.Builder resultBuilder = ValidationResult.builder().startTime(startTime);
        
        String filename = "unknown";
        Map<String, Object> attrs = document.getAttributes();
        if (attrs.containsKey("docfile")) {
            filename = attrs.get("docfile").toString();
        }
        
        Map<String, AttributeWithLocation> attributes = extractAttributesWithLocation(document, filename);
        
        validateAttributes(attributes, resultBuilder);
        
        RequiredRule requiredRule = findRequiredRule();
        if (requiredRule != null) {
            Set<String> presentAttributes = new HashSet<>(attributes.keySet());
            
            SourceLocation docLocation = SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
            
            List<ValidationMessage> missingMessages = requiredRule.validateMissingAttributes(presentAttributes, docLocation);
            missingMessages.forEach(resultBuilder::addMessage);
        }
        
        OrderRule orderRule = findOrderRule();
        if (orderRule != null) {
            List<ValidationMessage> orderMessages = orderRule.validateOrder();
            orderMessages.forEach(resultBuilder::addMessage);
        }
        
        return resultBuilder.complete().build();
    }


    private void validateAttributes(Map<String, AttributeWithLocation> attributes, ValidationResult.Builder resultBuilder) {
        for (Map.Entry<String, AttributeWithLocation> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            AttributeWithLocation attrWithLoc = entry.getValue();
            
            for (AttributeRule rule : rules) {
                if (rule.isApplicable(attrName)) {
                    List<ValidationMessage> messages = rule.validate(attrName, attrWithLoc.value, attrWithLoc.location);
                    messages.forEach(resultBuilder::addMessage);
                }
            }
        }
    }


    private Map<String, AttributeWithLocation> extractAttributesWithLocation(Document document, String filename) {
        Map<String, AttributeWithLocation> result = new LinkedHashMap<>();
        
        Map<String, Object> attributes = document.getAttributes();
        int lineNumber = 2;
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (isUserAttribute(key)) {
                SourceLocation location = SourceLocation.builder()
                    .filename(filename)
                    .line(lineNumber++)
                    .build();
                
                String stringValue = value != null ? value.toString() : "";
                result.put(key, new AttributeWithLocation(stringValue, location));
            }
        }
        
        return result;
    }

    private boolean isUserAttribute(String key) {
        return !key.startsWith("asciidoctor") && 
               !key.equals("doctype") && 
               !key.equals("backend") &&
               !key.equals("doctitle") &&
               !key.equals("docfile") &&
               !key.equals("docdir") &&
               !key.equals("docdatetime") &&
               !key.equals("localdate") &&
               !key.equals("localtime") &&
               !key.equals("localdatetime") &&
               !key.equals("outfile") &&
               !key.equals("filetype") &&
               !key.equals("notitle");
    }

    private RequiredRule findRequiredRule() {
        return rules.stream()
            .filter(rule -> rule instanceof RequiredRule)
            .map(rule -> (RequiredRule) rule)
            .findFirst()
            .orElse(null);
    }

    private OrderRule findOrderRule() {
        return rules.stream()
            .filter(rule -> rule instanceof OrderRule)
            .map(rule -> (OrderRule) rule)
            .findFirst()
            .orElse(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromConfiguration(MetadataConfiguration configuration) {
        Builder builder = new Builder();
        
        RequiredRule.Builder requiredBuilder = RequiredRule.builder();
        PatternRule.Builder patternBuilder = PatternRule.builder();
        LengthRule.Builder lengthBuilder = LengthRule.builder();
        OrderRule.Builder orderBuilder = OrderRule.builder();
        
        if (configuration.attributes() != null) {
            for (AttributeConfig attr : configuration.attributes()) {
                String name = attr.name();
                
                requiredBuilder.addAttribute(name, attr.required(), attr.severity());
                
                if (attr.pattern() != null) {
                    patternBuilder.addPattern(name, attr.pattern(), attr.severity());
                }
                
                if (attr.minLength() != null || attr.maxLength() != null) {
                    lengthBuilder.addLengthConstraint(name, attr.minLength(), attr.maxLength(), attr.severity());
                }
                
                if (attr.order() != null) {
                    orderBuilder.addOrderConstraint(name, attr.order(), attr.severity());
                }
            }
        }
        
        return builder
            .addRule(requiredBuilder.build())
            .addRule(patternBuilder.build())
            .addRule(lengthBuilder.build())
            .addRule(orderBuilder.build());
    }

    public static final class Builder {
        private final List<AttributeRule> rules = new ArrayList<>();

        private Builder() {
        }

        public Builder addRule(AttributeRule rule) {
            Objects.requireNonNull(rule, "[" + getClass().getName() + "] rule must not be null");
            this.rules.add(rule);
            return this;
        }

        public MetadataValidator build() {
            return new MetadataValidator(this);
        }
    }

    private static final class AttributeWithLocation {
        private final String value;
        private final SourceLocation location;

        AttributeWithLocation(String value, SourceLocation location) {
            this.value = value;
            this.location = location;
        }
    }
}