package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy for handling block content required errors. Inserts an empty line
 * after the block's opening delimiter.
 */
public class BlockContentContextStrategy implements ContextStrategy {

    private static final Map<String, String> RULE_DELIMITERS = createRuleDelimiters();

    @SuppressWarnings("PMD.UseConcurrentHashMap") // Immutable after creation, no concurrency needed
    private static Map<String, String> createRuleDelimiters() {
        Map<String, String> delimiters = new HashMap<>();
        delimiters.put("admonition.content.required", "====");
        delimiters.put("sidebar.content.required", "****");
        delimiters.put("verse.content.required", "____");
        delimiters.put("pass.content.required", "++++");
        return Collections.unmodifiableMap(delimiters);
    }

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return RULE_DELIMITERS.containsKey(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {
        String ruleId = message.getRuleId();
        String delimiter = RULE_DELIMITERS.get(ruleId);

        int blockLineIndex = loc.getStartLine() - startLine;
        int insertedLineIndex = ContextHelper.insertAfterDelimiter(contextLines, blockLineIndex, delimiter);

        return ContextHelper.createContextWithInsertedLine(contextLines, startLine, insertedLineIndex, loc);
    }
}
