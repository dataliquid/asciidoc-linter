package com.dataliquid.asciidoc.linter.report.console.context;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Block.OCCURRENCE_MIN;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling block.occurrence.min errors. Inserts an empty line at
 * the appropriate position for the missing block.
 */
public class BlockOccurrenceContextStrategy implements ContextStrategy {

    private static final String RULE_ID = OCCURRENCE_MIN;

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        int insertLineNumber = loc.getStartLine();
        int insertIndex = findInsertIndex(contextLines, startLine, insertLineNumber);

        // Insert the empty line at the correct position
        if (insertIndex >= 0 && insertIndex <= contextLines.size()) {
            contextLines.add(insertIndex, "");
        }

        return createContextWithLineNumbering(contextLines, startLine, insertIndex, insertLineNumber, loc);
    }

    private int findInsertIndex(List<String> contextLines, int startLine, int insertLineNumber) {
        // Find the position in context lines where we should insert the placeholder
        for (int i = 0; i < contextLines.size(); i++) {
            int currentLineNumber = startLine + i;
            if (currentLineNumber >= insertLineNumber) {
                return i;
            }
        }
        // Position is after our context - expand with empty lines
        int currentLineNumber = startLine + contextLines.size();
        while (currentLineNumber < insertLineNumber) {
            contextLines.add("");
            currentLineNumber++;
        }
        return contextLines.size();
    }

    private SourceContext createContextWithLineNumbering(List<String> contextLines, int startLine, int insertIndex,
            int insertLineNumber, SourceLocation loc) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();

        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);

            if (i < insertIndex) {
                // Before the placeholder - normal line numbering
                int lineNum = startLine + i;
                lines.add(new SourceContext.ContextLine(lineNum, content, false));
            } else if (i == insertIndex) {
                // This is the placeholder line
                lines.add(new SourceContext.ContextLine(insertLineNumber, content, true));
                // Add empty line after placeholder if next line is not empty
                if (i + 1 < contextLines.size()) {
                    String nextContent = contextLines.get(i + 1).trim();
                    if (!nextContent.isEmpty()) {
                        lines.add(new SourceContext.ContextLine(insertLineNumber + 1, "", false));
                    }
                }
            } else {
                // After the placeholder
                int originalIndex = i - 1; // Account for inserted placeholder
                int lineNum = calculateLineNumberAfterPlaceholder(content, i, insertIndex, startLine, originalIndex,
                        insertLineNumber);
                lines.add(new SourceContext.ContextLine(lineNum, content, false));
            }
        }
        return new SourceContext(lines, loc);
    }

    private int calculateLineNumberAfterPlaceholder(String content, int currentIndex, int insertIndex, int startLine,
            int originalIndex, int insertLineNumber) {
        // If we're right after placeholder and current line is empty
        if (currentIndex == insertIndex + 1 && ContextHelper.isEmptyOrWhitespace(content)) {
            return insertLineNumber + 1;
        }
        // Shift by 2 to account for placeholder and empty line
        return startLine + originalIndex + 2;
    }
}
