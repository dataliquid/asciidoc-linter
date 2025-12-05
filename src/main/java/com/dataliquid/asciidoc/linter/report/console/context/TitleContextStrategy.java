package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling title required errors. Inserts an empty line before the
 * block where the title should be.
 */
public class TitleContextStrategy implements ContextStrategy {

    private static final String ADMONITION_TITLE_RULE = "admonition.title.required";
    private static final String SIDEBAR_TITLE_RULE = "sidebar.title.required";
    private static final String LITERAL_TITLE_RULE = "literal.title.required";

    private static final Set<String> SUPPORTED_RULES = new HashSet<>(
            Arrays.asList(ADMONITION_TITLE_RULE, SIDEBAR_TITLE_RULE, LITERAL_TITLE_RULE));

    // Rules that need offset adjustment (insert before the attribute line)
    private static final Set<String> OFFSET_RULES = new HashSet<>(
            Arrays.asList(ADMONITION_TITLE_RULE, SIDEBAR_TITLE_RULE));

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return SUPPORTED_RULES.contains(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {
        String ruleId = message.getRuleId();

        // admonition and sidebar titles go before the attribute line (offset -1)
        // literal titles go at the block line position (no offset)
        int blockLineIndex = OFFSET_RULES.contains(ruleId) ? loc.getStartLine() - startLine - 1
                : loc.getStartLine() - startLine;

        if (blockLineIndex >= 0 && blockLineIndex <= contextLines.size()) {
            contextLines.add(blockLineIndex, "");
        }
        return ContextHelper.createContextWithInsertedLine(contextLines, startLine, blockLineIndex, loc);
    }
}
