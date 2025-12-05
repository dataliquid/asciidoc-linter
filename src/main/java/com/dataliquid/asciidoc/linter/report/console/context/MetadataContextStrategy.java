package com.dataliquid.asciidoc.linter.report.console.context;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Metadata.REQUIRED;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling metadata.required errors. Inserts an empty line at the
 * position where the metadata should be.
 */
public class MetadataContextStrategy implements ContextStrategy {

    private static final String RULE_ID = REQUIRED;

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        int insertLineNumber = loc.getStartLine();
        int insertIndex = calculateInsertIndex(contextLines, startLine, insertLineNumber);

        // Insert the empty line at the correct position
        if (insertIndex <= contextLines.size()) {
            contextLines.add(insertIndex, "");
        }

        return createContextWithInsertedLine(contextLines, startLine, insertIndex, loc);
    }

    private int calculateInsertIndex(List<String> contextLines, int startLine, int insertLineNumber) {
        int insertIndex = insertLineNumber - startLine;

        if (insertIndex < 0) {
            return 0;
        } else if (insertIndex > contextLines.size()) {
            // Add empty lines to reach the insert position
            while (contextLines.size() < insertIndex) {
                contextLines.add("");
            }
            return contextLines.size();
        }
        return insertIndex;
    }

    private SourceContext createContextWithInsertedLine(List<String> contextLines, int startLine, int insertIndex,
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
