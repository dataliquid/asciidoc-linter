package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling admonition.icon.required errors. Inserts an empty line
 * after the document title for the icons directive.
 */
public class IconContextStrategy implements ContextStrategy {

    private static final String RULE_ID = "admonition.icon.required";

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_ID.equals(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {

        // Icon directive should go in the document header, after the title
        for (int i = 0; i < contextLines.size(); i++) {
            String line = contextLines.get(i).trim();
            if (line.startsWith("=") && !line.startsWith("==")) {
                // Insert after the title line
                contextLines.add(i + 1, "");
                break;
            }
        }

        return ContextHelper.createContextWithCaptionLine(contextLines, startLine, loc);
    }
}
