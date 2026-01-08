package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling ulist.items.min and ulist.markerStyle errors.
 */
public class UlistContextStrategy implements ContextStrategy {

    private static final String ITEMS_MIN_RULE = "ulist.items.min";
    private static final String MARKER_STYLE_RULE = "ulist.markerStyle";

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        if (ITEMS_MIN_RULE.equals(ruleId) && errorType == ErrorType.MISSING_VALUE) {
            return true;
        }
        return MARKER_STYLE_RULE.equals(ruleId);
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        String ruleId = message.getRuleId();

        if (ITEMS_MIN_RULE.equals(ruleId)) {
            return handleItemsMin(contextLines, startLine, loc);
        } else if (MARKER_STYLE_RULE.equals(ruleId)) {
            return handleMarkerStyle(contextLines, startLine, message, loc);
        }

        return new SourceContext(contextLines, startLine, loc);
    }

    private SourceContext handleItemsMin(List<String> contextLines, int startLine, SourceLocation loc) {
        int ulistLineIndex = loc.getStartLine() - startLine;
        if (ulistLineIndex >= 0 && ulistLineIndex < contextLines.size()) {
            // Insert after the current line (which should be the last item)
            contextLines.add(ulistLineIndex + 1, "");
        }

        List<SourceContext.ContextLine> lines = new ArrayList<>();
        int lineNum = startLine;
        for (int i = 0; i < contextLines.size(); i++) {
            String content = contextLines.get(i);
            boolean isErrorLine = (i == ulistLineIndex + 1);
            lines.add(new SourceContext.ContextLine(lineNum, content, isErrorLine));
            lineNum++;
        }
        return new SourceContext(lines, loc);
    }

    private SourceContext handleMarkerStyle(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {
        if (message.getExpectedValue().isPresent()) {
            String expectedMarker = message.getExpectedValue().get();
            // Replace all list item markers with the expected marker
            for (int i = 0; i < contextLines.size(); i++) {
                String line = contextLines.get(i);
                if (line.trim().startsWith("*") || line.trim().startsWith("-")) {
                    contextLines.set(i, line.replaceFirst("[*-]", expectedMarker));
                }
            }
        }
        return new SourceContext(contextLines, startLine, loc);
    }
}
