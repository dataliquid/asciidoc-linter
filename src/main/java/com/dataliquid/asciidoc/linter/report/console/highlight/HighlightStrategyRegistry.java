package com.dataliquid.asciidoc.linter.report.console.highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry for highlight strategies.
 */
public class HighlightStrategyRegistry {

    private final List<HighlightStrategy> strategies = new ArrayList<>();

    public void register(HighlightStrategy strategy) {
        strategies.add(strategy);
    }

    public Optional<HighlightStrategy> findStrategy(String ruleId) {
        return strategies.stream().filter(s -> s.supports(ruleId)).findFirst();
    }
}
