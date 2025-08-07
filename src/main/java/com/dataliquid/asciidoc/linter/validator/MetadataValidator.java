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

import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.validator.rules.AttributeRule;
import com.dataliquid.asciidoc.linter.validator.rules.LengthRule;
import com.dataliquid.asciidoc.linter.validator.rules.OrderRule;
import com.dataliquid.asciidoc.linter.validator.rules.PatternRule;
import com.dataliquid.asciidoc.linter.validator.rules.RequiredRule;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

public final class MetadataValidator {
    private final List<AttributeRule> rules;
    private final FileContentCache fileCache;

    private MetadataValidator(Builder builder) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(builder.rules));
        this.fileCache = new FileContentCache();
    }

    public ValidationResult validate(Document document) {
        return validate(document, extractFilename(document));
    }
    
    public ValidationResult validate(Document document, String filename) {
        long startTime = System.currentTimeMillis();
        ValidationResult.Builder resultBuilder = ValidationResult.builder().startTime(startTime);
        
        Map<String, AttributeWithLocation> attributes = extractAttributesWithLocation(document, filename);
        
        validateAttributes(attributes, resultBuilder);
        
        RequiredRule requiredRule = findRequiredRule();
        if (requiredRule != null) {
            Set<String> presentAttributes = new HashSet<>(attributes.keySet());
            
            SourceLocation docLocation = findLocationForMissingAttributes(filename);
            
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
    
    private String extractFilename(Document document) {
        Map<String, Object> attrs = document.getAttributes();
        if (attrs.containsKey("docfile")) {
            return attrs.get("docfile").toString();
        }
        return "unknown";
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
        List<String> fileLines = fileCache.getFileLines(filename);
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (isUserAttribute(key)) {
                String stringValue = value != null ? value.toString() : "";
                
                // Find the attribute in the file
                SourceLocation location = findAttributeLocation(key, stringValue, filename, fileLines);
                
                result.put(key, new AttributeWithLocation(stringValue, location));
            }
        }
        
        return result;
    }

    private SourceLocation findAttributeLocation(String key, String value, String filename, List<String> fileLines) {
        // Search for the attribute in the file
        // Attributes have format: :key: value
        String attributePattern = ":" + key + ":";
        
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            int attrIndex = line.indexOf(attributePattern);
            
            if (attrIndex >= 0) {
                // Found the attribute, now find where the value starts
                int valueStartIndex = attrIndex + attributePattern.length();
                
                // Skip whitespace after the colon
                while (valueStartIndex < line.length() && Character.isWhitespace(line.charAt(valueStartIndex))) {
                    valueStartIndex++;
                }
                
                // Calculate end position based on value length
                int valueEndIndex = valueStartIndex;
                if (!value.isEmpty() && valueStartIndex < line.length()) {
                    // Find the actual value in the line
                    int valueIndex = line.indexOf(value, valueStartIndex);
                    if (valueIndex >= 0) {
                        valueStartIndex = valueIndex;
                        valueEndIndex = valueStartIndex + value.length() - 1;
                    } else {
                        // If we can't find the exact value, use the rest of the line
                        valueEndIndex = line.length() - 1;
                    }
                }
                
                return SourceLocation.builder()
                    .filename(filename)
                    .line(i + 1)  // Line numbers are 1-based
                    .startColumn(valueStartIndex + 1)  // Columns are 1-based
                    .endColumn(valueEndIndex + 1)
                    .sourceLine(line)
                    .build();
            }
        }
        
        // Fallback if we can't find the attribute
        return SourceLocation.builder()
            .filename(filename)
            .line(1)
            .build();
    }
    
    private SourceLocation findLocationForMissingAttributes(String filename) {
        List<String> fileLines = fileCache.getFileLines(filename);
        if (fileLines.isEmpty()) {
            return SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
        }
        
        int lineNumber = 1;
        boolean inFrontMatter = false;
        boolean foundTitle = false;
        int titleLine = -1;
        int lastAttributeLine = -1;
        
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            String trimmed = line.trim();
            
            // Check for front matter
            if (i == 0 && trimmed.equals("---")) {
                inFrontMatter = true;
                continue;
            }
            
            if (inFrontMatter && trimmed.equals("---")) {
                inFrontMatter = false;
                continue;
            }
            
            if (inFrontMatter) {
                continue;
            }
            
            // Check for document title (level 0)
            if (!foundTitle && trimmed.startsWith("= ")) {
                foundTitle = true;
                titleLine = i + 1;
                continue;
            }
            
            // Check for metadata attributes
            if (trimmed.matches("^:[^:]+:.*")) {
                lastAttributeLine = i + 1;
            }
        }
        
        // Determine where to suggest placing missing attributes
        if (foundTitle) {
            // If there's a title and existing attributes, place after last attribute
            if (lastAttributeLine > titleLine) {
                lineNumber = lastAttributeLine + 1;
            } else {
                // If there's a title but no attributes, place after title
                lineNumber = titleLine + 1;
            }
        } else {
            // No title found
            if (lastAttributeLine > 0) {
                // Place after last attribute
                lineNumber = lastAttributeLine + 1;
            } else {
                // Place at beginning of document
                lineNumber = 1;
            }
        }
        
        // Ensure we don't go beyond file bounds
        if (lineNumber > fileLines.size()) {
            lineNumber = fileLines.size() + 1;
        }
        
        return SourceLocation.builder()
            .filename(filename)
            .line(lineNumber)
            .build();
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