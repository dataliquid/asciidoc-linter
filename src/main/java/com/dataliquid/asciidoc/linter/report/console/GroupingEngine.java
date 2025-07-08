package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.ErrorGroupingConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Groups similar validation messages together.
 */
public class GroupingEngine {
    private final ErrorGroupingConfig config;
    
    public GroupingEngine(ErrorGroupingConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
    }
    
    /**
     * Groups validation messages by rule ID.
     */
    public MessageGroups group(List<ValidationMessage> messages) {
        if (!config.isEnabled()) {
            // Return all messages as ungrouped
            return new MessageGroups(List.of(), messages);
        }
        
        // Group by rule ID
        Map<String, List<ValidationMessage>> byRuleId = new HashMap<>();
        for (ValidationMessage msg : messages) {
            byRuleId.computeIfAbsent(msg.getRuleId(), k -> new ArrayList<>()).add(msg);
        }
        
        List<MessageGroup> groups = new ArrayList<>();
        List<ValidationMessage> ungrouped = new ArrayList<>();
        
        // Separate into groups and ungrouped based on threshold
        for (Map.Entry<String, List<ValidationMessage>> entry : byRuleId.entrySet()) {
            List<ValidationMessage> ruleMessages = entry.getValue();
            
            if (ruleMessages.size() >= config.getThreshold()) {
                // Create a group
                groups.add(new MessageGroup(entry.getKey(), ruleMessages));
            } else {
                // Add to ungrouped
                ungrouped.addAll(ruleMessages);
            }
        }
        
        // Sort groups by size (largest first)
        groups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        
        return new MessageGroups(groups, ungrouped);
    }
}