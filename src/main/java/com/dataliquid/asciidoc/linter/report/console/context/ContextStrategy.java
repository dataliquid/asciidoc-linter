package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.List;

import com.dataliquid.asciidoc.linter.report.console.SourceContext;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Strategy interface for creating source context based on validation message
 * type. Each implementation handles a specific category of validation rules.
 */
public interface ContextStrategy {

    /**
     * Checks if this strategy can handle the given rule ID and error type.
     *
     * @param  ruleId    the rule ID from the validation message
     * @param  errorType the error type from the validation message
     *
     * @return           true if this strategy can handle this combination
     */
    boolean supports(String ruleId, ErrorType errorType);

    /**
     * Creates a source context for the given validation message.
     *
     * @param  contextLines the raw context lines from the file
     * @param  startLine    the starting line number in the file
     * @param  message      the validation message
     * @param  loc          the source location
     *
     * @return              the modified source context with appropriate error line
     *                      marking
     */
    SourceContext createContext(List<String> contextLines, int startLine, ValidationMessage message,
            SourceLocation loc);
}
