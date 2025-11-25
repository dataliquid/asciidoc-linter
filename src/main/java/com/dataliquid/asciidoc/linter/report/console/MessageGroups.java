package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Container for grouped and ungrouped validation messages.
 */
public final class MessageGroups {
    private final List<MessageGroup> groups;
    private final List<ValidationMessage> ungroupedMessages;

    public MessageGroups(List<MessageGroup> groups, List<ValidationMessage> ungroupedMessages) {
        this.groups = new ArrayList<>(
                Objects.requireNonNull(groups, "[" + getClass().getName() + "] groups must not be null"));
        this.ungroupedMessages = new ArrayList<>(Objects
                .requireNonNull(ungroupedMessages,
                        "[" + getClass().getName() + "] ungroupedMessages must not be null"));
    }

    public List<MessageGroup> getGroups() {
        return new ArrayList<>(groups);
    }

    public List<ValidationMessage> getUngroupedMessages() {
        return new ArrayList<>(ungroupedMessages);
    }

    public boolean hasGroups() {
        return !groups.isEmpty();
    }

    public int getTotalMessages() {
        int total = ungroupedMessages.size();
        for (MessageGroup group : groups) {
            total += group.size();
        }
        return total;
    }
}
