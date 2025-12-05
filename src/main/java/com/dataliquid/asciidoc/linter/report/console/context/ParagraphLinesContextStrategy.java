package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling paragraph.lines.min errors. Adds empty lines for
 * missing paragraph lines.
 */
public class ParagraphLinesContextStrategy implements ContextStrategy {

    private static final String RULE_ID = "paragraph.lines.min";

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        int missingLines = calculateMissingLines(message);
        for (int i = 0; i < missingLines; i++) {
            contextLines.add("");
        }
        return ContextHelper.createContextWithExtraLines(contextLines, startLine, loc, missingLines);
    }

    private int calculateMissingLines(ValidationMessage message) {
        String actualValue = message.getActualValue().orElse("0");
        String expectedValue = message.getExpectedValue().orElse("");

        try {
            int actual = Integer.parseInt(actualValue);
            // Expected value is in format "At least X lines"
            String[] parts = expectedValue.split(" ");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    int expected = Integer.parseInt(part);
                    return Math.max(1, expected - actual);
                }
            }
        } catch (NumberFormatException e) {
            return 1; // Fallback to 1 line if parsing fails
        }
        return 1;
    }
}
