package com.dataliquid.asciidoc.linter.report.console.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dataliquid.asciidoc.linter.validator.ErrorType;

/**
 * Registry for context strategies. Finds the appropriate strategy for a given
 * rule ID.
 */
public class ContextStrategyRegistry {

    private final List<ContextStrategy> strategies = new ArrayList<>();

    /**
     * Registers a strategy.
     *
     * @param strategy the strategy to register
     */
    public void register(ContextStrategy strategy) {
        strategies.add(strategy);
    }

    /**
     * Finds a strategy that can handle the given rule ID and error type.
     *
     * @param  ruleId    the rule ID
     * @param  errorType the error type
     *
     * @return           the matching strategy, or empty if none found
     */
    public Optional<ContextStrategy> findStrategy(String ruleId, ErrorType errorType) {
        for (ContextStrategy strategy : strategies) {
            if (strategy.supports(ruleId, errorType)) {
                return Optional.of(strategy);
            }
        }
        return Optional.empty();
    }
}
