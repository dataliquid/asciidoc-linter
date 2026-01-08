package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling sidebar.position.required errors. Inserts an empty line
 * before the sidebar block for the position attribute.
 */
public class PositionContextStrategy implements ContextStrategy {

    private static final String RULE_ID = "sidebar.position.required";

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        int sidebarLineIndex = loc.getStartLine() - startLine;
        if (sidebarLineIndex >= 0 && sidebarLineIndex <= contextLines.size()) {
            contextLines.add(sidebarLineIndex, "");
        }

        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = (i == sidebarLineIndex);
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }
}
