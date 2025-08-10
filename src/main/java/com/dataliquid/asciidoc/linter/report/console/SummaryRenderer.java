package com.dataliquid.asciidoc.linter.report.console;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dataliquid.asciidoc.linter.cli.display.AsciiBoxDrawer;
import com.dataliquid.asciidoc.linter.cli.display.DisplayConstants;
import com.dataliquid.asciidoc.linter.config.output.DisplayConfig;
import com.dataliquid.asciidoc.linter.config.output.SummaryConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Renders an enhanced validation summary with statistics and visualizations.
 */
public class SummaryRenderer {
    private final SummaryConfig config;
    private final ColorScheme colorScheme;
    
    public SummaryRenderer(SummaryConfig config, DisplayConfig displayConfig) {
        this.config = Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        this.colorScheme = new ColorScheme(displayConfig.isUseColors());
    }
    
    /**
     * Renders the validation summary.
     */
    public void render(ValidationResult result, PrintWriter writer) {
        if (!config.isEnabled()) {
            return;
        }
        
        writer.println();
        AsciiBoxDrawer boxDrawer = new AsciiBoxDrawer(DisplayConstants.DEFAULT_BOX_WIDTH, writer);
        boxDrawer.drawTop();
        boxDrawer.drawTitle("Validation Summary");
        boxDrawer.drawBottom();
        
        if (config.isShowStatistics()) {
            renderStatistics(result, writer);
        }
        
        if (config.isShowMostCommon()) {
            renderMostCommonIssues(result, writer);
        }
        
        if (config.isShowFileList()) {
            renderFileList(result, writer);
        }
        
        renderSummaryLine(result, writer);
        
        boxDrawer.drawTop();
    }
    
    private void renderStatistics(ValidationResult result, PrintWriter writer) {
        // File statistics
        int totalFiles = getUniqueFileCount(result);
        int filesWithErrors = getFilesWithErrorCount(result);
        
        writer.println("  Total files scanned:     " + totalFiles);
        writer.println("  Files with errors:       " + filesWithErrors);
        writer.println();
        
        // Error counts with visual bars
        int errors = result.getErrorCount();
        int warnings = result.getWarningCount();
        int infos = result.getInfoCount();
        int total = errors + warnings + infos;
        
        if (total > 0) {
            writer.println("  Errors:   " + colorScheme.error(String.valueOf(errors)));
            writer.println("  Warnings: " + colorScheme.warning(String.valueOf(warnings)));
            writer.println("  Info:     " + colorScheme.info(String.valueOf(infos)));
            writer.println();
        }
    }
    
    private void renderMostCommonIssues(ValidationResult result, PrintWriter writer) {
        Map<String, Long> issueFrequency = result.getMessages().stream()
            .collect(Collectors.groupingBy(
                ValidationMessage::getRuleId,
                LinkedHashMap::new,
                Collectors.counting()
            ));
        
        if (issueFrequency.isEmpty()) {
            return;
        }
        
        writer.println("  Most common issues:");
        
        // Get top 5 issues
        issueFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                String ruleId = entry.getKey();
                long count = entry.getValue();
                
                // Try to get a description from the first message with this rule
                String description = result.getMessages().stream()
                    .filter(msg -> msg.getRuleId().equals(ruleId))
                    .findFirst()
                    .map(msg -> extractShortDescription(msg.getMessage()))
                    .orElse(ruleId);
                
                writer.printf("  - %s (%d occurrence%s)%n",
                    description,
                    count,
                    count == 1 ? "" : "s"
                );
            });
        
        writer.println();
    }
    
    private void renderFileList(ValidationResult result, PrintWriter writer) {
        Map<String, List<ValidationMessage>> byFile = result.getMessagesByFile();
        
        if (byFile.isEmpty()) {
            return;
        }
        
        writer.println("  Files with issues:");
        byFile.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String filename = entry.getKey();
                List<ValidationMessage> messages = entry.getValue();
                
                long errorCount = messages.stream()
                    .filter(msg -> msg.getSeverity() == com.dataliquid.asciidoc.linter.config.common.Severity.ERROR)
                    .count();
                long warnCount = messages.stream()
                    .filter(msg -> msg.getSeverity() == com.dataliquid.asciidoc.linter.config.common.Severity.WARN)
                    .count();
                
                writer.printf("  - %s: ", filename);
                if (errorCount > 0) {
                    writer.print(colorScheme.error(errorCount + " error" + (errorCount == 1 ? "" : "s")));
                    if (warnCount > 0) {
                        writer.print(", ");
                    }
                }
                if (warnCount > 0) {
                    writer.print(colorScheme.warning(warnCount + " warning" + (warnCount == 1 ? "" : "s")));
                }
                writer.println();
            });
        writer.println();
    }
    
    private void renderSummaryLine(ValidationResult result, PrintWriter writer) {
        int errors = result.getErrorCount();
        int warnings = result.getWarningCount();
        int infos = result.getInfoCount();
        
        String summary = String.format("Summary: %d error%s, %d warning%s, %d info message%s",
            errors, errors == 1 ? "" : "s",
            warnings, warnings == 1 ? "" : "s",
            infos, infos == 1 ? "" : "s");
        
        // Color based on severity
        if (errors > 0) {
            summary = colorScheme.error(summary);
        } else if (warnings > 0) {
            summary = colorScheme.warning(summary);
        } else {
            summary = colorScheme.success(summary);
        }
        
        writer.println();
        writer.println(summary);
        writer.println("Validation completed in " + result.getValidationTimeMillis() + "ms");
    }
    
    
    private int getUniqueFileCount(ValidationResult result) {
        // Use the actual scanned file count from ValidationResult
        return result.getScannedFileCount();
    }
    
    private int getFilesWithErrorCount(ValidationResult result) {
        return (int) result.getMessages().stream()
            .filter(msg -> msg.getSeverity() == com.dataliquid.asciidoc.linter.config.common.Severity.ERROR)
            .map(msg -> msg.getLocation().getFilename())
            .distinct()
            .count();
    }
    
    private String extractShortDescription(String message) {
        // Extract only the first line to avoid showing stack traces in summary
        if (message == null) {
            return "";
        }
        
        // Find the first newline
        int newlineIndex = message.indexOf('\n');
        if (newlineIndex > 0) {
            // Return only the first line (before stack trace)
            return message.substring(0, newlineIndex).trim();
        }
        
        // No newline found, return the full message
        return message.trim();
    }
}