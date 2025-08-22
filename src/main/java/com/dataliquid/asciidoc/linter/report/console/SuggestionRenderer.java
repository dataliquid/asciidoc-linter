package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.SuggestionsConfig;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

/**
 * Renders fix suggestions for validation errors.
 */
public class SuggestionRenderer {
    private final SuggestionsConfig config;
    private final ColorScheme colorScheme;

    public SuggestionRenderer(SuggestionsConfig config, DisplayConfig displayConfig) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.colorScheme = new ColorScheme(displayConfig.isUseColors());
    }

    /**
     * Renders a list of suggestions.
     */
    public void render(List<Suggestion> suggestions, PrintWriter writer) {
        if (!config.isEnabled() || suggestions.isEmpty()) {
            return;
        }

        // Suggestion header
        writer.print("  " + colorScheme.suggestionIcon("💡 "));
        writer.println("Suggested fix" + (suggestions.size() > 1 ? "es:" : ":"));

        // Render each suggestion (up to max)
        int count = 0;
        for (Suggestion suggestion : suggestions) {
            if (++count > config.getMaxPerError()) {
                break;
            }

            renderSuggestion(suggestion, count, suggestions.size(), writer);
        }
    }

    private void renderSuggestion(Suggestion suggestion, int index, int total, PrintWriter writer) {
        // Prefix for multiple suggestions
        String prefix = total > 1 ? String.format("  %d. ", index) : "  ";

        // Description
        writer.println(prefix + colorScheme.suggestion(suggestion.getDescription()));

        // Fixed value if available
        if (suggestion.hasFixedValue()) {
            String indent = total > 1 ? "     " : "  ";
            writer.println(indent + colorScheme.code(suggestion.getFixedValue()));
        }

        // Explanation if available
        if (suggestion.getExplanation() != null) {
            String indent = total > 1 ? "     " : "  ";
            writer.println(indent + suggestion.getExplanation());
        }

        // Examples if configured
        if (config.isShowExamples() && suggestion.hasExamples()) {
            renderExamples(suggestion.getExamples(), total > 1, writer);
        }
    }

    private void renderExamples(List<String> examples, boolean numbered, PrintWriter writer) {
        String indent = numbered ? "     " : "  ";
        writer.println(indent + "Examples:");

        for (String example : examples) {
            writer.println(indent + "- " + colorScheme.code(example));
        }
    }
}
