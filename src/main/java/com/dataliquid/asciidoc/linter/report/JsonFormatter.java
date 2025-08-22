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
 * Formats validation results as JSON using Jackson. Supports both pretty-printed and compact (single-line) output
 * formats.
 */
public class JsonFormatter implements ReportFormatter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final String name;
    private final ObjectMapper objectMapper;

    /**
     * Creates a JSON formatter with the specified name and pretty-print setting.
     *
     * @param name
     *            the formatter name (e.g., "json" or "json-compact")
     * @param prettyPrint
     *            whether to enable pretty printing
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
        Map<String, Object> root = new LinkedHashMap<>();

        // Timestamp
        root.put("timestamp", ISO_FORMATTER.format(Instant.now()));

        // Duration
        root.put("duration", formatDuration(result.getValidationTimeMillis()));

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalMessages", result.getMessages().size());
        summary.put("errors", result.getErrorCount());
        summary.put("warnings", result.getWarningCount());
        summary.put("infos", result.getInfoCount());
        root.put("summary", summary);

        // Messages
        List<Map<String, Object>> messages = result.getMessages().stream().map(this::formatMessage)
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

    private Map<String, Object> formatMessage(ValidationMessage msg) {
        Map<String, Object> msgMap = new LinkedHashMap<>();

        msgMap.put("file", msg.getLocation().getFilename());
        msgMap.put("line", msg.getLocation().getStartLine());

        if (msg.getLocation().getStartColumn() > 1) {
            msgMap.put("column", msg.getLocation().getStartColumn());
        }

        msgMap.put("severity", msg.getSeverity().toString());
        msgMap.put("message", msg.getMessage());

        // Optional fields
        if (msg.getRuleId() != null) {
            msgMap.put("ruleId", msg.getRuleId());
        }

        msg.getActualValue().ifPresent(value -> msgMap.put("actualValue", value));

        msg.getExpectedValue().ifPresent(value -> msgMap.put("expectedValue", value));

        return msgMap;
    }

    private String formatDuration(long millis) {
        if (millis < 1000) {
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
