package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.dataliquid.asciidoc.linter.config.Severity;

public final class ValidationResult {
    private final List<ValidationMessage> messages;
    private final Set<String> scannedFiles;
    private final long startTime;
    private final long endTime;

    private ValidationResult(Builder builder) {
        this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
        this.scannedFiles = Collections.unmodifiableSet(new HashSet<>(builder.scannedFiles));
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public List<ValidationMessage> getMessages() {
        return messages;
    }

    public Set<String> getScannedFiles() {
        return scannedFiles;
    }

    public int getScannedFileCount() {
        return scannedFiles.size();
    }

    public List<ValidationMessage> getMessagesBySeverity(Severity severity) {
        return messages.stream()
                .filter(msg -> msg.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    public Map<String, List<ValidationMessage>> getMessagesByFile() {
        return messages.stream()
                .collect(Collectors.groupingBy(
                    msg -> msg.getLocation().getFilename(),
                    TreeMap::new,
                    Collectors.toList()
                ));
    }

    public Map<Integer, List<ValidationMessage>> getMessagesByLine(String filename) {
        return messages.stream()
                .filter(msg -> msg.getLocation().getFilename().equals(filename))
                .collect(Collectors.groupingBy(
                    msg -> msg.getLocation().getStartLine(),
                    TreeMap::new,
                    Collectors.toList()
                ));
    }

    public boolean isValid() {
        return !hasErrors();
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(msg -> msg.getSeverity() == Severity.ERROR);
    }

    public boolean hasWarnings() {
        return messages.stream().anyMatch(msg -> msg.getSeverity() == Severity.WARN);
    }
    
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public int getErrorCount() {
        return (int) messages.stream()
                .filter(msg -> msg.getSeverity() == Severity.ERROR)
                .count();
    }

    public int getWarningCount() {
        return (int) messages.stream()
                .filter(msg -> msg.getSeverity() == Severity.WARN)
                .count();
    }

    public int getInfoCount() {
        return (int) messages.stream()
                .filter(msg -> msg.getSeverity() == Severity.INFO)
                .count();
    }

    public long getValidationTimeMillis() {
        return endTime - startTime;
    }

    public void printReport() {
        System.out.println("Validation Report");
        System.out.println("=================");
        System.out.println();

        if (messages.isEmpty()) {
            System.out.println("No validation issues found.");
        } else {
            Map<String, List<ValidationMessage>> messagesByFile = getMessagesByFile();
            
            for (Map.Entry<String, List<ValidationMessage>> entry : messagesByFile.entrySet()) {
                List<ValidationMessage> fileMessages = entry.getValue();
                fileMessages.sort(Comparator
                    .comparing((ValidationMessage msg) -> msg.getLocation().getStartLine())
                    .thenComparing(msg -> msg.getLocation().getStartColumn()));
                
                for (ValidationMessage msg : fileMessages) {
                    System.out.println(msg.format());
                    System.out.println();
                }
            }
        }

        System.out.println("Summary: " + getErrorCount() + " errors, " + 
                         getWarningCount() + " warnings, " + 
                         getInfoCount() + " info messages");
        System.out.println("Validation completed in " + getValidationTimeMillis() + "ms");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ValidationMessage> messages = new ArrayList<>();
        private final Set<String> scannedFiles = new HashSet<>();
        private long startTime = System.currentTimeMillis();
        private long endTime;

        private Builder() {
        }

        public Builder addMessage(ValidationMessage message) {
            Objects.requireNonNull(message, "[" + getClass().getName() + "] message must not be null");
            this.messages.add(message);
            return this;
        }

        public Builder addMessages(Collection<ValidationMessage> messages) {
            Objects.requireNonNull(messages, "[" + getClass().getName() + "] messages must not be null");
            this.messages.addAll(messages);
            return this;
        }

        public Builder addScannedFile(String filename) {
            Objects.requireNonNull(filename, "[" + getClass().getName() + "] filename must not be null");
            this.scannedFiles.add(filename);
            return this;
        }

        public Builder addScannedFiles(Collection<String> filenames) {
            Objects.requireNonNull(filenames, "[" + getClass().getName() + "] filenames must not be null");
            this.scannedFiles.addAll(filenames);
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder complete() {
            this.endTime = System.currentTimeMillis();
            return this;
        }

        public ValidationResult build() {
            if (endTime == 0) {
                endTime = System.currentTimeMillis();
            }
            return new ValidationResult(this);
        }
    }
}