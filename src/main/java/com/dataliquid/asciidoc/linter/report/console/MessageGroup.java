package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Represents a group of similar validation messages.
 */
public final class MessageGroup {
    private final String ruleId;
    private final List<ValidationMessage> messages;
    private final String commonDescription;
    
    public MessageGroup(String ruleId, List<ValidationMessage> messages) {
        this.ruleId = Objects.requireNonNull(ruleId, "[" + getClass().getName() + "] ruleId must not be null");
        this.messages = new ArrayList<>(Objects.requireNonNull(messages, "[" + getClass().getName() + "] messages must not be null"));
        
        // Extract common description from first message
        this.commonDescription = messages.isEmpty() ? 
            "Unknown error" : 
            extractCommonDescription(messages.get(0));
    }
    
    private String extractCommonDescription(ValidationMessage firstMessage) {
        String msg = firstMessage.getMessage();
        // Try to extract the generic part of the message
        int colonIndex = msg.indexOf(':');
        if (colonIndex > 0) {
            return msg.substring(0, colonIndex).trim();
        }
        // If no colon, try to find pattern-like descriptions
        if (msg.contains("must")) {
            int mustIndex = msg.indexOf("must");
            return msg.substring(mustIndex).trim();
        }
        return msg;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public List<ValidationMessage> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public String getCommonDescription() {
        return commonDescription;
    }
    
    public int size() {
        return messages.size();
    }
}