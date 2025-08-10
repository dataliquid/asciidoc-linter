package com.dataliquid.asciidoc.linter.report;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dataliquid.asciidoc.linter.cli.display.AsciiBoxDrawer;
import com.dataliquid.asciidoc.linter.cli.display.DisplayConstants;
import com.dataliquid.asciidoc.linter.config.output.OutputConfiguration;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;
import com.dataliquid.asciidoc.linter.report.console.GroupingEngine;
import com.dataliquid.asciidoc.linter.report.console.MessageGroup;
import com.dataliquid.asciidoc.linter.report.console.MessageGroups;
import com.dataliquid.asciidoc.linter.report.console.MessageRenderer;
import com.dataliquid.asciidoc.linter.report.console.SummaryRenderer;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Completely redesigned console formatter with enhanced error display,
 * context visualization, and fix suggestions.
 * This replaces the old ConsoleFormatter entirely.
 */
public class ConsoleFormatter implements ReportFormatter {
    
    private final OutputConfiguration config;
    private final MessageRenderer messageRenderer;
    private final GroupingEngine groupingEngine;
    private final SummaryRenderer summaryRenderer;
    
    /**
     * Creates a console formatter with default enhanced configuration.
     */
    public ConsoleFormatter() {
        this(OutputConfiguration.defaultConfig());
    }
    
    /**
     * Creates a console formatter with the specified configuration.
     */
    public ConsoleFormatter(OutputConfiguration config) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.messageRenderer = new MessageRenderer(config);
        this.groupingEngine = new GroupingEngine(config.getErrorGrouping());
        this.summaryRenderer = new SummaryRenderer(config.getSummary(), config.getDisplay());
    }
    
    @Override
    public void format(ValidationResult result, PrintWriter writer) {
        // Header
        if (config.getDisplay().isShowHeader()) {
            renderHeader(writer);
        }
        
        // Messages with grouping
        if (!result.getMessages().isEmpty()) {
            renderMessages(result, writer);
        } else {
            renderNoIssuesFound(writer);
        }
        
        // Summary
        if (config.getSummary().isEnabled()) {
            summaryRenderer.render(result, writer);
        }
    }
    
    private void renderHeader(PrintWriter writer) {
        if (config.getFormat() != OutputFormat.COMPACT) {
            AsciiBoxDrawer boxDrawer = new AsciiBoxDrawer(DisplayConstants.DEFAULT_BOX_WIDTH, writer);
            boxDrawer.drawTop();
            boxDrawer.drawTitle("Validation Report");
            boxDrawer.drawBottom();
            writer.println();
        }
    }
    
    private void renderMessages(ValidationResult result, PrintWriter writer) {
        List<ValidationMessage> messages = result.getMessages();
        
        // Grouping if enabled
        if (config.getErrorGrouping().isEnabled() && config.getFormat() != OutputFormat.COMPACT) {
            MessageGroups groups = groupingEngine.group(messages);
            
            // Ungrouped messages first
            if (!groups.getUngroupedMessages().isEmpty()) {
                renderUngroupedMessages(groups.getUngroupedMessages(), writer);
            }
            
            // Then grouped messages
            for (MessageGroup group : groups.getGroups()) {
                renderGroupedMessages(group, writer);
                writer.println();
            }
        } else {
            // Normal output without grouping
            if (config.getFormat() == OutputFormat.COMPACT) {
                // Compact format: one line per message
                renderCompactMessages(messages, writer);
            } else {
                // Standard format: group by file
                Map<String, List<ValidationMessage>> byFile = groupByFile(messages);
                for (Map.Entry<String, List<ValidationMessage>> entry : byFile.entrySet()) {
                    renderFileMessages(entry.getKey(), entry.getValue(), writer);
                }
            }
        }
    }
    
    private void renderUngroupedMessages(List<ValidationMessage> messages, PrintWriter writer) {
        Map<String, List<ValidationMessage>> byFile = groupByFile(messages);
        for (Map.Entry<String, List<ValidationMessage>> entry : byFile.entrySet()) {
            renderFileMessages(entry.getKey(), entry.getValue(), writer);
        }
    }
    
    private Map<String, List<ValidationMessage>> groupByFile(List<ValidationMessage> messages) {
        return messages.stream()
            .sorted(Comparator
                .comparing((ValidationMessage msg) -> msg.getLocation().getFilename())
                .thenComparing(msg -> msg.getLocation().getStartLine())
                .thenComparing(msg -> msg.getLocation().getStartColumn()))
            .collect(Collectors.groupingBy(
                msg -> msg.getLocation().getFilename(),
                Collectors.toList()
            ));
    }
    
    private void renderFileMessages(String filename, List<ValidationMessage> messages, PrintWriter writer) {
        if (config.getFormat() != OutputFormat.COMPACT) {
            writer.println(filename + ":");
            writer.println();
        }
        
        for (ValidationMessage message : messages) {
            messageRenderer.render(message, writer);
            if (config.getFormat() == OutputFormat.ENHANCED) {
                writer.println();
            }
        }
        
        if (config.getFormat() != OutputFormat.COMPACT) {
            writer.println();
        }
    }
    
    private void renderCompactMessages(List<ValidationMessage> messages, PrintWriter writer) {
        for (ValidationMessage message : messages) {
            messageRenderer.render(message, writer);
        }
    }
    
    private void renderGroupedMessages(MessageGroup group, PrintWriter writer) {
        writer.printf("Found %d similar errors: %s%n%n", 
            group.getMessages().size(), 
            group.getCommonDescription());
        
        // Show locations
        for (ValidationMessage msg : group.getMessages()) {
            writer.printf("  %s", msg.getLocation().formatLocation());
            if (msg.getActualValue().isPresent()) {
                writer.printf("   %s", msg.getActualValue().get());
            }
            writer.println();
        }
        
        // Show common suggestion if available
        if (!group.getMessages().isEmpty() && 
            group.getMessages().get(0).hasSuggestions() &&
            config.getSuggestions().isEnabled()) {
            writer.println();
            writer.println("💡 Common fix: " + 
                group.getMessages().get(0).getSuggestions().get(0).getDescription());
            
            if (group.getMessages().get(0).hasAutoFixableSuggestions()) {
                writer.println("🔧 Auto-fixable: Use --fix to apply suggested changes");
            }
        }
    }
    
    private void renderNoIssuesFound(PrintWriter writer) {
        if (config.getFormat() != OutputFormat.COMPACT) {
            writer.println("No validation issues found.");
        }
    }
    
    @Override
    public String getName() {
        return "console";
    }
}