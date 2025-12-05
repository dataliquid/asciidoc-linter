package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Renders validation messages according to the configured output format.
 */
public class MessageRenderer {
    private final OutputConfiguration config;
    private final ContextRenderer contextRenderer;
    private final HighlightRenderer highlightRenderer;
    private final SuggestionRenderer suggestionRenderer;
    private final ColorScheme colorScheme;

    public MessageRenderer(OutputConfiguration config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.contextRenderer = new ContextRenderer(config.getDisplay());
        this.highlightRenderer = new HighlightRenderer(config.getDisplay());
        this.suggestionRenderer = new SuggestionRenderer(config.getSuggestions(), config.getDisplay());
        this.colorScheme = new ColorScheme(config.getDisplay().isUseColors());
    }

    public void render(ValidationMessage message, PrintWriter writer) {
        // Format depends on OutputFormat
        Runnable renderer = switch (config.getFormat()) {
        case ENHANCED -> () -> renderEnhanced(message, writer);
        case SIMPLE -> () -> renderSimple(message, writer);
        case COMPACT -> () -> renderCompact(message, writer);
        };
        renderer.run();
    }

    private void renderEnhanced(ValidationMessage message, PrintWriter writer) {
        // Header with severity and message
        String severityLabel = formatSeverity(message);
        String header = severityLabel + ": " + message.getMessage() + " [" + message.getRuleId() + "]";
        writer.println(colorScheme.colorize(header, message.getSeverity()));

        // File location
        writer.println("  File: " + message.getLocation().formatLocation());

        // Actual and expected values
        if (message.getActualValue().isPresent() || message.getExpectedValue().isPresent()) {
            message.getActualValue().ifPresent(value -> writer.println("  Actual: " + value));
            message.getExpectedValue().ifPresent(value -> writer.println("  Expected: " + value));
        }

        // Stack trace if available
        if (message.getCause().isPresent()) {
            writer.println();
            renderStackTrace(message.getCause().get(), writer);
        }

        // Source context with highlighting
        if (config.getDisplay().getContextLines() > 0) {
            writer.println();
            SourceContext context = contextRenderer.getContext(message);
            highlightRenderer.renderWithHighlight(context, message, writer);
        }

        // Suggestions
        if (config.getSuggestions().isEnabled() && message.hasSuggestions()) {
            writer.println();
            suggestionRenderer.render(message.getSuggestions(), writer);
        }
    }

    private void renderSimple(ValidationMessage message, PrintWriter writer) {
        String severityLabel = formatSeverity(message);
        String location = String.format("  Line %d", message.getLocation().getStartLine());

        if (message.getLocation().getStartColumn() > 0) {
            location += String.format(", Column %d", message.getLocation().getStartColumn());
        }

        writer.println(location + ": " + severityLabel + " " + message.getMessage());

        if (config.getDisplay().isShowLineNumbers()) {
            writer.println("    Rule: " + message.getRuleId());
        }

        if (message.getActualValue().isPresent() || message.getExpectedValue().isPresent()) {
            message.getActualValue().ifPresent(value -> writer.println("    Actual: " + value));
            message.getExpectedValue().ifPresent(value -> writer.println("    Expected: " + value));
        }

        // Stack trace if available
        if (message.getCause().isPresent()) {
            writer.println();
            renderStackTrace(message.getCause().get(), writer);
        }
    }

    private void renderCompact(ValidationMessage message, PrintWriter writer) {
        // Single-line output for CI/CD
        StringBuilder compact = new StringBuilder(50); // Increased buffer size
        compact
                .append(message.getLocation().formatLocation())
                .append(": ")
                .append(message.getSeverity())
                .append(": ")
                .append(message.getMessage())
                .append(" [")
                .append(message.getRuleId())
                .append(']');

        // Add actual/expected values inline if present
        if (message.getActualValue().isPresent() || message.getExpectedValue().isPresent()) {
            compact.append(" (");
            if (message.getActualValue().isPresent()) {
                compact.append("actual: ").append(message.getActualValue().get());
                if (message.getExpectedValue().isPresent()) {
                    compact.append(", ");
                }
            }
            if (message.getExpectedValue().isPresent()) {
                compact.append("expected: ").append(message.getExpectedValue().get());
            }
            compact.append(')');
        }

        // Add inline fix suggestion if available
        if (message.hasSuggestions() && !message.getSuggestions().isEmpty()) {
            String fixedValue = message.getSuggestions().get(0).getFixedValue();
            if (fixedValue != null && !fixedValue.isEmpty()) {
                compact.append(" → ").append(fixedValue);
            }
        }

        writer.println(compact);
    }

    private String formatSeverity(ValidationMessage message) {
        return switch (message.getSeverity()) {
        case ERROR -> colorScheme.error("[ERROR]");
        case WARN -> colorScheme.warning("[WARN]");
        case INFO -> colorScheme.info("[INFO]");
        };
    }

    private void renderStackTrace(Throwable throwable, PrintWriter writer) {
        writer.println("  Stack Trace:");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        // Indent each line of the stack trace
        String[] lines = sw.toString().split("\n");
        for (String line : lines) {
            writer.println("    " + line);
        }
    }
}
