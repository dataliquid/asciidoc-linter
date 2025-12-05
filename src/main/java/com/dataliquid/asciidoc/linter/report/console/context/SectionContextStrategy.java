package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling section.min-occurrences errors. Inserts an empty line
 * at the appropriate position for the missing section.
 */
public class SectionContextStrategy implements ContextStrategy {

    private static final String RULE_ID = "section.min-occurrences";
    private static final int DOCUMENT_TITLE_LEVEL = 0;
    private static final int FIRST_SECTION_LEVEL = 1;

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        // Extract the section level from the placeholder hint (e.g., "== section" ->
        // level 1)
        String hint = message.getMissingValueHint();
        int sectionLevel = extractSectionLevel(hint);

        // Find the appropriate position to insert the section placeholder
        int insertIndex = findInsertPosition(contextLines, sectionLevel);

        // If we found where to insert, add an empty line
        if (insertIndex >= 0 && insertIndex <= contextLines.size()) {
            contextLines.add(insertIndex, "");
            return createContextWithErrorLine(contextLines, startLine, insertIndex, loc);
        }

        // Fallback: return context without modification
        return new SourceContext(contextLines, startLine, loc);
    }

    private int extractSectionLevel(String hint) {
        int sectionLevel = 0;
        if (hint != null) {
            for (int i = 0; i < hint.length() && hint.charAt(i) == '='; i++) {
                sectionLevel++;
            }
            sectionLevel--; // Convert to 0-based level
        }
        return sectionLevel;
    }

    private int findInsertPosition(List<String> contextLines, int sectionLevel) {
        if (sectionLevel == DOCUMENT_TITLE_LEVEL) {
            return 0;
        } else if (sectionLevel == FIRST_SECTION_LEVEL) {
            return findPositionAfterDocumentTitle(contextLines);
        } else {
            return findPositionAfterParentSection(contextLines, sectionLevel);
        }
    }

    private int findPositionAfterDocumentTitle(List<String> contextLines) {
        for (int i = 0; i < contextLines.size(); i++) {
            String line = contextLines.get(i).trim();
            if (line.startsWith("= ") && !line.startsWith("== ")) {
                int insertIndex = i + 1;
                if (insertIndex < contextLines.size()
                        && ContextHelper.isEmptyOrWhitespace(contextLines.get(insertIndex))) {
                    insertIndex++;
                }
                return insertIndex;
            }
        }
        return -1;
    }

    private int findPositionAfterParentSection(List<String> contextLines, int sectionLevel) {
        for (int i = contextLines.size() - 1; i >= 0; i--) {
            String line = contextLines.get(i).trim();
            int lineLevel = getSectionLevel(line);

            if (lineLevel >= 0 && lineLevel == sectionLevel - 1) {
                int insertIndex = i + 1;
                while (insertIndex < contextLines.size()
                        && ContextHelper.isEmptyOrWhitespace(contextLines.get(insertIndex))) {
                    insertIndex++;
                }
                while (insertIndex < contextLines.size()) {
                    String nextLine = contextLines.get(insertIndex).trim();
                    if (nextLine.startsWith("=") && nextLine.contains(" ")) {
                        break;
                    }
                    insertIndex++;
                }
                return insertIndex;
            }
        }
        return -1;
    }

    private int getSectionLevel(String line) {
        if (!line.startsWith("=")) {
            return -1;
        }
        int count = 0;
        for (int j = 0; j < line.length() && line.charAt(j) == '='; j++) {
            count++;
        }
        if (count > 0 && count < line.length() && line.charAt(count) == ' ') {
            return count - 1;
        }
        return -1;
    }

    private SourceContext createContextWithErrorLine(List<String> contextLines, int startLine, int insertIndex,
            SourceLocation loc) {
        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = (i == insertIndex);
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }
}
