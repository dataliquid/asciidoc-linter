package com.dataliquid.asciidoc.linter.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Formats validation results as JSON using Jackson. Supports both
 * pretty-printed and compact (single-line) output formats.
 */
public class JsonFormatter implements ReportFormatter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    // Constants
    private static final int MILLIS_PER_SECOND = 1000;

    private final String name;
    private final ObjectMapper objectMapper;

    /**
     * Creates a JSON formatter with the specified name and pretty-print setting.
     *
     * @param name        the formatter name (e.g., "json" or "json-compact")
     * @param prettyPrint whether to enable pretty printing
     */
    public JsonFormatter(String name, boolean prettyPrint) {
        this.name = name;
        this.objectMapper = new ObjectMapper();

        if (prettyPrint) {
            this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    /**
     * Creates a pretty-printing JSON formatter.
     *
     * @return a formatter that produces human-readable JSON
     */
    public static JsonFormatter pretty() {
        return new JsonFormatter("json", true);
    }

    /**
     * Creates a compact JSON formatter.
     *
     * @return a formatter that produces single-line JSON
     */
    public static JsonFormatter compact() {
        return new JsonFormatter("json-compact", false);
    }

    @Override
    public void format(ValidationResult result, PrintWriter writer) {
        @SuppressWarnings("PMD.UseConcurrentHashMap") // Local variable, no concurrency needed
        Map<String, Object> root = new LinkedHashMap<>();

        // Timestamp
        root.put("timestamp", ISO_FORMATTER.format(Instant.now()));

        // Duration
        root.put("duration", formatDuration(result.getValidationTimeMillis()));

        // Summary
        @SuppressWarnings("PMD.UseConcurrentHashMap") // Local variable, no concurrency needed
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalMessages", result.getMessages().size());
        summary.put("errors", result.getErrorCount());
        summary.put("warnings", result.getWarningCount());
        summary.put("infos", result.getInfoCount());
        root.put("summary", summary);

        // Messages
        List<Map<String, Object>> messages = result
                .getMessages()
                .stream()
                .map(this::formatMessage)
                .collect(Collectors.toList());
        root.put("messages", messages);

        // Write JSON to PrintWriter
        try {
            objectMapper.writeValue(writer, root);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON output", e);
        }
    }

    private Map<String, Object> formatMessage(ValidationMessage message) {
        @SuppressWarnings("PMD.UseConcurrentHashMap") // Local variable, no concurrency needed
        Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("severity", message.getSeverity().toString());
        messageMap.put("ruleId", message.getRuleId());
        messageMap.put("message", message.getMessage());
        if (message.getLocation() != null) {
            messageMap.put("filename", message.getLocation().getFilename());
            Map<String, Object> location = new LinkedHashMap<>();
            location.put("line", message.getLocation().getStartLine());
            location.put("column", message.getLocation().getStartColumn());
            messageMap.put("location", location);
        }
        return messageMap;
    }

    private String formatDuration(long millis) {
        if (millis < MILLIS_PER_SECOND) {
            return millis + "ms";
        } else {
            return String.format("%.3fs", millis / 1000.0);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
