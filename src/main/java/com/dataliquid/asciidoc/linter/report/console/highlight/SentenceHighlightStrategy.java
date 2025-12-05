package com.dataliquid.asciidoc.linter.report.console.highlight;

import com.dataliquid.asciidoc.linter.report.console.ColorScheme;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for sentence-related rules that insert placeholder with special
 * positioning. Handles: paragraph.sentence.occurrence.min,
 * paragraph.sentence.words.min
 */
public class SentenceHighlightStrategy implements HighlightStrategy {

    private static final String OCCURRENCE_RULE = "paragraph.sentence.occurrence.min";
    private static final String WORDS_RULE = "paragraph.sentence.words.min";

    @Override
    public boolean supports(String ruleId) {
        return OCCURRENCE_RULE.equals(ruleId) || WORDS_RULE.equals(ruleId);
    }

    @Override
    public String highlight(String line, ValidationMessage message, ColorScheme colorScheme) {
        if (message.getMissingValueHint() == null) {
            return line;
        }

        if (OCCURRENCE_RULE.equals(message.getRuleId())) {
            return insertPlaceholderWithSpace(line, message, colorScheme);
        } else {
            return insertPlaceholderBeforePunctuation(line, message, colorScheme);
        }
    }

    private String insertPlaceholderWithSpace(String line, ValidationMessage message, ColorScheme colorScheme) {
        int col = message.getLocation().getStartColumn();
        String placeholder = " " + HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);

        if (col <= 0 || col > line.length() + 1) {
            return line + placeholder;
        }

        if (col > line.length()) {
            return line + placeholder;
        }

        String before = line.substring(0, col - 1);
        String after = line.substring(col - 1);
        return before + placeholder + after;
    }

    private String insertPlaceholderBeforePunctuation(String line, ValidationMessage message, ColorScheme colorScheme) {
        int col = message.getLocation().getStartColumn();
        String placeholder = " " + HighlightHelper.createPlaceholder(message.getMissingValueHint(), colorScheme);

        if (col <= 0 || col > line.length() + 1) {
            if (line.matches(".*[.!?]+$")) {
                int punctStart = line.length() - 1;
                while (punctStart > 0 && ".!?".indexOf(line.charAt(punctStart)) != -1) {
                    punctStart--;
                }
                punctStart++;
                return line.substring(0, punctStart) + placeholder + line.substring(punctStart);
            }
            return line + placeholder;
        }

        if (col > line.length()) {
            return line + placeholder;
        }

        return line.substring(0, col - 1) + placeholder + line.substring(col - 1);
    }
}
