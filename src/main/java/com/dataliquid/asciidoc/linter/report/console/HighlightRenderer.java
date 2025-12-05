package com.dataliquid.asciidoc.linter.report.console;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Block.OCCURRENCE_MIN;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.HighlightStyle;
import com.dataliquid.asciidoc.linter.report.console.highlight.BlockOccurrenceHighlightStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.DlistHighlightStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.EmptyLinePlaceholderStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.HighlightHelper;
import com.dataliquid.asciidoc.linter.report.console.highlight.HighlightStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.HighlightStrategyRegistry;
import com.dataliquid.asciidoc.linter.report.console.highlight.SentenceHighlightStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.UlistMarkerHighlightStrategy;
import com.dataliquid.asciidoc.linter.report.console.highlight.VerseAttributeHighlightStrategy;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Renders source code with visual error highlighting.
 */
public class HighlightRenderer {

    private final DisplayConfig config;
    private final ColorScheme colorScheme;
    private final HighlightStrategyRegistry strategyRegistry;

    public HighlightRenderer(DisplayConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.colorScheme = new ColorScheme(config.isUseColors());
        this.strategyRegistry = initializeStrategyRegistry();
    }

    private HighlightStrategyRegistry initializeStrategyRegistry() {
        HighlightStrategyRegistry registry = new HighlightStrategyRegistry();
        // Order matters: more specific strategies first
        registry.register(new BlockOccurrenceHighlightStrategy());
        registry.register(new SentenceHighlightStrategy());
        registry.register(new VerseAttributeHighlightStrategy());
        registry.register(new DlistHighlightStrategy());
        registry.register(new UlistMarkerHighlightStrategy());
        registry.register(new EmptyLinePlaceholderStrategy());
        return registry;
    }

    /**
     * Renders source context with error highlighting.
     */
    public void renderWithHighlight(SourceContext context, ValidationMessage message, PrintWriter writer) {
        List<SourceContext.ContextLine> lines = context.getLines();
        for (SourceContext.ContextLine line : lines) {
            if (shouldRenderMultiLinePlaceholder(line, message)) {
                renderMultiLinePlaceholder(line, message, writer);
            } else {
                renderLine(line, message, writer);
            }
        }
    }

    private boolean shouldRenderMultiLinePlaceholder(SourceContext.ContextLine line, ValidationMessage message) {
        return line.isErrorLine() && line.getContent().isEmpty() && OCCURRENCE_MIN.equals(message.getRuleId())
                && message.getMissingValueHint() != null && message.getMissingValueHint().contains("\n");
    }

    private void renderMultiLinePlaceholder(SourceContext.ContextLine line, ValidationMessage message,
            PrintWriter writer) {
        String[] placeholderLines = message.getMissingValueHint().split("\n");

        for (int j = 0; j < placeholderLines.length; j++) {
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Necessary for each placeholder line
            SourceContext.ContextLine placeholderLine = new SourceContext.ContextLine(line.getNumber() + j,
                    placeholderLines[j], true);
            renderPlaceholderLine(placeholderLine, writer, j == 0, j == placeholderLines.length - 1);
        }
    }

    private void renderPlaceholderLine(SourceContext.ContextLine line, PrintWriter writer, boolean isFirstLine,
            boolean isLastLine) {
        String linePrefix = formatLinePrefix(line, false);

        StringBuilder contentBuilder = new StringBuilder(line.getContent().length() + 20);
        contentBuilder.append(line.getContent());
        if (isFirstLine) {
            contentBuilder.insert(0, HighlightHelper.PLACEHOLDER_START);
        }
        if (isLastLine) {
            contentBuilder.append(HighlightHelper.PLACEHOLDER_END);
        }

        writer.println(linePrefix + colorScheme.error(contentBuilder.toString()));
    }

    private void renderLine(SourceContext.ContextLine line, ValidationMessage message, PrintWriter writer) {
        String linePrefix = formatLinePrefix(line, line.isErrorLine());

        if (line.isErrorLine()) {
            String highlightedContent = highlightErrorInLine(line.getContent(), message);
            writer.println(linePrefix + highlightedContent);

            if (config.getHighlightStyle() == HighlightStyle.UNDERLINE && shouldShowUnderline(message)) {
                renderUnderline(line, message, writer);
            }
        } else {
            writer.println(linePrefix + colorScheme.contextLine(line.getContent()));
        }
    }

    private String formatLinePrefix(SourceContext.ContextLine line, boolean isError) {
        if (!config.isShowLineNumbers()) {
            return "";
        }
        String lineNum = String.format("%4d", line.getNumber());
        return (isError ? colorScheme.errorLineNumber(lineNum) : colorScheme.contextLineNumber(lineNum)) + " | ";
    }

    private String highlightErrorInLine(String line, ValidationMessage message) {
        if (message.getErrorType() != ErrorType.MISSING_VALUE || message.getMissingValueHint() == null) {
            return line;
        }

        // Try to find a strategy for this rule
        Optional<HighlightStrategy> strategy = strategyRegistry.findStrategy(message.getRuleId());
        if (strategy.isPresent()) {
            String result = strategy.get().highlight(line, message, colorScheme);
            if (result != null) {
                return result;
            }
        }

        // Default placeholder insertion
        return insertDefaultPlaceholder(line, message);
    }

    private String insertDefaultPlaceholder(String line, ValidationMessage message) {
        int col = message.getLocation().getStartColumn();

        String placeholderText;
        PlaceholderContext context = message.getPlaceholderContext();
        if (context != null) {
            placeholderText = context.generatePlaceholder(message.getMissingValueHint());
        } else {
            placeholderText = HighlightHelper.createPlaceholderText(message.getMissingValueHint());
        }

        String placeholder = colorScheme.error(placeholderText);

        if (col <= 0 || col > line.length() + 1) {
            return line + placeholder;
        }

        if (col > line.length()) {
            return line + placeholder;
        }

        return line.substring(0, col - 1) + placeholder + line.substring(col - 1);
    }

    private boolean shouldShowUnderline(ValidationMessage message) {
        return message.getErrorType() != ErrorType.MISSING_VALUE;
    }

    private void renderUnderline(SourceContext.ContextLine line, ValidationMessage message, PrintWriter writer) {
        int startCol = message.getLocation().getStartColumn();
        int endCol = message.getLocation().getEndColumn();

        if (startCol < 0) {
            return;
        }

        if (startCol == 0) {
            startCol = 1;
        }

        if (endCol <= 0 || endCol < startCol) {
            endCol = Math.min(line.getContent().length(), startCol + 20);
        }

        StringBuilder underline = new StringBuilder();

        if (config.isShowLineNumbers()) {
            underline.append("     | ");
        }

        underline.append(" ".repeat(Math.max(0, startCol - 1)));

        int length = Math.min(endCol - startCol + 1, config.getMaxLineWidth());
        underline.append(colorScheme.errorMarker("~".repeat(length)));

        writer.println(underline);
    }
}
