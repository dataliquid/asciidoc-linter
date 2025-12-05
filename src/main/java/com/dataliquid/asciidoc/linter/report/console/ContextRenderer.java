package com.dataliquid.asciidoc.linter.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.report.console.context.BlockContentContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.BlockOccurrenceContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.CaptionContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.ContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.ContextStrategyRegistry;
import com.dataliquid.asciidoc.linter.report.console.context.IconContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.MetadataContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.ParagraphLinesContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.PositionContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.SectionContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.TitleContextStrategy;
import com.dataliquid.asciidoc.linter.report.console.context.UlistContextStrategy;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Extracts source code context around validation errors.
 */
public class ContextRenderer {

    private final DisplayConfig config;
    private final FileContentCache fileCache;
    private final ContextStrategyRegistry strategyRegistry;

    public ContextRenderer(DisplayConfig config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.fileCache = new FileContentCache();
        this.strategyRegistry = initializeStrategyRegistry();
    }

    private ContextStrategyRegistry initializeStrategyRegistry() {
        ContextStrategyRegistry registry = new ContextStrategyRegistry();
        registry.register(new CaptionContextStrategy());
        registry.register(new BlockContentContextStrategy());
        registry.register(new TitleContextStrategy());
        registry.register(new SectionContextStrategy());
        registry.register(new BlockOccurrenceContextStrategy());
        registry.register(new ParagraphLinesContextStrategy());
        registry.register(new MetadataContextStrategy());
        registry.register(new UlistContextStrategy());
        registry.register(new PositionContextStrategy());
        registry.register(new IconContextStrategy());
        return registry;
    }

    /**
     * Gets the source context for a validation message.
     */
    public SourceContext getContext(ValidationMessage message) {
        SourceLocation loc = message.getLocation();

        // If message already has context lines, use them
        if (!message.getContextLines().isEmpty()) {
            return new SourceContext(message.getContextLines(),
                    Math.max(1, loc.getStartLine() - config.getContextLines()), loc);
        }

        // Read from file
        List<String> fileLines = fileCache.getFileLines(loc.getFilename());

        if (fileLines.isEmpty()) {
            return handleEmptyFile(message, loc);
        }

        // Calculate context bounds
        int startLine = Math.max(1, loc.getStartLine() - config.getContextLines());
        int endLine = calculateEndLine(message, fileLines, loc);

        // Extract context lines
        List<String> contextLines = extractContextLines(fileLines, startLine, endLine);

        // Try to find a strategy for this message
        Optional<ContextStrategy> strategy = strategyRegistry.findStrategy(message.getRuleId(), message.getErrorType());

        if (strategy.isPresent()) {
            return strategy.get().createContext(contextLines, startLine, message, loc);
        }

        return new SourceContext(contextLines, startLine, loc);
    }

    private SourceContext handleEmptyFile(ValidationMessage message, SourceLocation loc) {
        if ("metadata.required".equals(message.getRuleId()) && message.getErrorType() == ErrorType.MISSING_VALUE) {
            List<SourceContext.ContextLine> lines = new ArrayList<>();
            lines.add(new SourceContext.ContextLine(1, "", true));
            return new SourceContext(lines, loc);
        }
        return new SourceContext(List.of(), loc.getStartLine(), loc);
    }

    private int calculateEndLine(ValidationMessage message, List<String> fileLines, SourceLocation loc) {
        int endLine = Math.min(fileLines.size(), loc.getEndLine() + config.getContextLines());

        // For verse blocks, ensure we include the closing delimiter
        if (("verse.author.required".equals(message.getRuleId())
                || "verse.attribution.required".equals(message.getRuleId())) && endLine < fileLines.size()) {
            endLine = Math.min(fileLines.size(), endLine + 1);
        }

        return endLine;
    }

    private List<String> extractContextLines(List<String> fileLines, int startLine, int endLine) {
        int fromIndex = Math.max(0, Math.min(startLine - 1, fileLines.size()));
        int toIndex = Math.max(fromIndex, Math.min(endLine, fileLines.size()));
        return new ArrayList<>(fileLines.subList(fromIndex, toIndex));
    }

    /**
     * Clears the file cache to free memory.
     */
    public void clearCache() {
        fileCache.clear();
    }
}
