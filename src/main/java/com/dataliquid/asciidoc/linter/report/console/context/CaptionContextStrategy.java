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
 * Strategy for handling caption/title required errors. Inserts an empty line
 * before the block where the caption should be.
 */
public class CaptionContextStrategy implements ContextStrategy {

    private static final Set<String> SUPPORTED_RULES = new HashSet<>(Arrays
            .asList("video.caption.required", "audio.title.required", "table.caption.required",
                    "example.caption.required", "example.collapsible.required", "table.header.required",
                    "pass.reason.required", "pass.type.required"));

    @Override
    public boolean supports(String ruleId, ErrorType errorType) {
        return SUPPORTED_RULES.contains(ruleId) && errorType == ErrorType.MISSING_VALUE;
    }

    @Override
    public SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc) {
        int blockLineIndex = loc.getStartLine() - startLine;
        if (blockLineIndex >= 0 && blockLineIndex <= contextLines.size()) {
            contextLines.add(blockLineIndex, "");
        }
        return ContextHelper.createContextWithCaptionLine(contextLines, startLine, loc);
    }
}
