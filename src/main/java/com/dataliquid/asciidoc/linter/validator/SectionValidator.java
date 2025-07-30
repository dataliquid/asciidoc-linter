package com.dataliquid.asciidoc.linter.validator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.config.rule.TitleConfig;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

public final class SectionValidator {
    private final DocumentConfiguration configuration;
    private final Map<String, Integer> sectionOccurrences;
    private final List<SectionConfig> rootSections;
    private final FileContentCache fileCache;

    private SectionValidator(Builder builder) {
        this.configuration = Objects.requireNonNull(builder.configuration, "[" + getClass().getName() + "] configuration must not be null");
        this.sectionOccurrences = new HashMap<>();
        this.rootSections = configuration.sections() != null ? configuration.sections() : Collections.emptyList();
        this.fileCache = new FileContentCache();
    }

    public ValidationResult validate(Document document) {
        // FIXME: refactor to more clear architecture design
        return validate(document, extractFilename(document));
    }
    
    public ValidationResult validate(Document document, String filename) {
        long startTime = System.currentTimeMillis();
        ValidationResult.Builder resultBuilder = ValidationResult.builder().startTime(startTime);
        
        // Validate document title as level 0 section
        validateDocumentTitle(document, filename, resultBuilder);
        
        List<StructuralNode> sections = document.getBlocks().stream()
            .filter(block -> block instanceof Section)
            .collect(Collectors.toList());
        
        validateRootSections(sections, filename, resultBuilder);
        
        validateMinMaxOccurrences(filename, resultBuilder);
        
        validateSectionOrder(sections, filename, resultBuilder);
        
        return resultBuilder.complete().build();
    }

    private void validateRootSections(List<StructuralNode> sections, String filename, ValidationResult.Builder resultBuilder) {
        // Determine which configs to use for level 1 sections
        List<SectionConfig> level1Configs = determineLevel1Configs();
        
        for (StructuralNode node : sections) {
            if (node instanceof Section) {
                Section section = (Section) node;
                validateSection(section, level1Configs, filename, resultBuilder, null);
            }
        }
    }
    
    private List<SectionConfig> determineLevel1Configs() {
        // Check if level 0 config has subsections defined
        for (SectionConfig config : rootSections) {
            if (config.level() == 0 && config.subsections() != null && !config.subsections().isEmpty()) {
                return config.subsections();
            }
        }
        
        // Fallback: use all non-level-0 configs from root
        return rootSections.stream()
            .filter(config -> config.level() != 0)
            .collect(Collectors.toList());
    }

    private void validateSection(Section section, List<SectionConfig> allowedConfigs, 
                                String filename, ValidationResult.Builder resultBuilder, 
                                SectionConfig parentConfig) {
        
        int level = section.getLevel();
        String title = section.getTitle();
        
        SectionConfig matchingConfig = findMatchingConfig(section, allowedConfigs);
        
        if (matchingConfig == null && !allowedConfigs.isEmpty()) {
            // Check if there are configs for this level with pattern mismatches
            List<SectionConfig> levelConfigs = findConfigsForLevel(level, allowedConfigs);
            
            SourceLocation location = createLocation(filename, section);
            
            if (!levelConfigs.isEmpty()) {
                // There are configs for this level, but title doesn't match any patterns
                // Find the first config with a pattern to show in error message
                SectionConfig configWithPattern = levelConfigs.stream()
                    .filter(config -> config.title() != null && 
                           config.title().pattern() != null)
                    .findFirst()
                    .orElse(levelConfigs.get(0));
                
                String expectedPattern = null;
                if (configWithPattern.title() != null && configWithPattern.title().pattern() != null) {
                    expectedPattern = "Pattern: " + configWithPattern.title().pattern();
                }
                
                ValidationMessage message = ValidationMessage.builder()
                    .severity(configWithPattern.title() != null && configWithPattern.title().severity() != null ? 
                            configWithPattern.title().severity() : Severity.ERROR)
                    .ruleId("section.title.pattern")
                    .location(location)
                    .message("Section title does not match required pattern")
                    .actualValue(title)
                    .expectedValue(expectedPattern != null ? expectedPattern : "One of configured patterns")
                    .build();
                resultBuilder.addMessage(message);
            } else {
                // No configs for this level at all
                ValidationMessage message = ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("section.unexpected")
                    .location(location)
                    .message("Section not allowed at level " + level + ": '" + title + "'")
                    .actualValue(title)
                    .expectedValue("No sections configured for level " + level)
                    .build();
                resultBuilder.addMessage(message);
            }
            // Don't return - still need to validate subsections
        }
        
        if (matchingConfig != null) {
            trackOccurrence(matchingConfig);
            
            validateTitle(section, matchingConfig.title(), filename, resultBuilder);
            
            validateLevel(section, matchingConfig, filename, resultBuilder);
        } else {
            // Even without a pattern match, we need to track occurrences for min/max validation
            SectionConfig configForTracking = findConfigForOccurrenceTracking(section, allowedConfigs);
            if (configForTracking != null) {
                trackOccurrence(configForTracking);
            }
        }
        
        // Always validate subsections
        List<StructuralNode> subsections = section.getBlocks().stream()
            .filter(block -> block instanceof Section)
            .collect(Collectors.toList());
        
        for (StructuralNode subsection : subsections) {
            // Determine which configs to use for subsections
            List<SectionConfig> subsectionConfigs;
            
            if (matchingConfig != null && matchingConfig.subsections() != null) {
                // Use the subsections from the matching config
                subsectionConfigs = matchingConfig.subsections();
            } else {
                // Important: When parent doesn't match, we need to determine the right configs for subsections
                // Look for configs that have subsections defined and could be parent configs
                subsectionConfigs = determineSubsectionConfigsFromParent(allowedConfigs, level);
            }
            
            validateSection((Section) subsection, subsectionConfigs, 
                           filename, resultBuilder, matchingConfig);
        }
    }

    private void validateTitle(Section section, TitleConfig titleConfig, 
                              String filename, ValidationResult.Builder resultBuilder) {
        if (titleConfig == null) {
            return;
        }
        
        String title = section.getTitle();
        SourceLocation location = createLocation(filename, section);
        
        if (titleConfig.pattern() != null) {
            Pattern pattern = Pattern.compile(titleConfig.pattern());
            if (!pattern.matcher(title).matches()) {
                ValidationMessage message = ValidationMessage.builder()
                    .severity(titleConfig.severity())
                    .ruleId("section.title.pattern")
                    .location(location)
                    .message("Section title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + titleConfig.pattern())
                    .build();
                resultBuilder.addMessage(message);
            }
        }
    }

    private void validateLevel(Section section, SectionConfig config, 
                              String filename, ValidationResult.Builder resultBuilder) {
        int actualLevel = section.getLevel();
        int expectedLevel = config.level();
        
        if (actualLevel != expectedLevel) {
            SourceLocation location = createLocation(filename, section);
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.level")
                .location(location)
                .message("Section level mismatch")
                .actualValue(String.valueOf(actualLevel))
                .expectedValue(String.valueOf(expectedLevel))
                .build();
            resultBuilder.addMessage(message);
        }
    }

    private void validateMinMaxOccurrences(String filename, ValidationResult.Builder resultBuilder) {
        // Validate root level configs (this will recursively validate subsections)
        for (SectionConfig config : rootSections) {
            validateOccurrenceForConfig(config, filename, resultBuilder);
        }
    }

    private void validateOccurrenceForConfig(SectionConfig config, String filename, 
                                            ValidationResult.Builder resultBuilder) {
        String key = createOccurrenceKey(config);
        int occurrences = sectionOccurrences.getOrDefault(key, 0);
        
        if (occurrences < config.min()) {
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .startLine(1)
                .endLine(1)
                .startColumn(0)  // No column for section errors
                .endColumn(0)
                .build();
            
            // Generate section placeholder based on level
            String sectionPlaceholder = "=".repeat(config.level() + 1) + " " + config.name();
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.min-occurrences")
                .location(location)
                .message("Too few occurrences of section: " + config.name())
                .actualValue(String.valueOf(occurrences))
                .expectedValue("At least " + config.min())
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(sectionPlaceholder)
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .build();
            resultBuilder.addMessage(message);
        }
        
        if (occurrences > config.max()) {
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.max-occurrences")
                .location(location)
                .message("Too many occurrences of section: " + config.name())
                .actualValue(String.valueOf(occurrences))
                .expectedValue("At most " + config.max())
                .build();
            resultBuilder.addMessage(message);
        }
        
        // Recursively validate subsection occurrences
        if (config.subsections() != null) {
            for (SectionConfig subsection : config.subsections()) {
                validateOccurrenceForConfig(subsection, filename, resultBuilder);
            }
        }
    }

    private void validateDocumentTitleConfig(String title, TitleConfig titleConfig, 
                                            SourceLocation location, ValidationResult.Builder resultBuilder) {
        if (titleConfig.pattern() != null) {
            Pattern pattern = Pattern.compile(titleConfig.pattern());
            if (!pattern.matcher(title).matches()) {
                ValidationMessage message = ValidationMessage.builder()
                    .severity(titleConfig.severity())
                    .ruleId("section.title.pattern")
                    .location(location)
                    .message("Document title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + titleConfig.pattern())
                    .build();
                resultBuilder.addMessage(message);
            }
        }
    }
    
    private void validateDocumentTitle(Document document, String filename, ValidationResult.Builder resultBuilder) {
        // Find level 0 section config (document title)
        SectionConfig titleConfig = rootSections.stream()
            .filter(config -> config.level() == 0)
            .findFirst()
            .orElse(null);
        
        if (titleConfig == null) {
            return; // No level 0 validation configured
        }
        
        String documentTitle = document.getTitle();
        SourceLocation location = createDocumentTitleLocation(filename, documentTitle);
        
        // Check if title is required
        if (titleConfig.min() > 0 && (documentTitle == null || documentTitle.trim().isEmpty())) {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.level0.missing")
                .location(location)
                .message("Document title is required")
                .actualValue("No title")
                .expectedValue("Document title")
                .build();
            resultBuilder.addMessage(message);
            return;
        }
        
        // Validate title pattern if title exists
        if (documentTitle != null && titleConfig.title() != null) {
            validateDocumentTitleConfig(documentTitle, titleConfig.title(), location, resultBuilder);
        }
        
        // Track occurrence for min/max validation
        String configKey = createOccurrenceKey(titleConfig);
        sectionOccurrences.put(configKey, documentTitle != null ? 1 : 0);
    }
    
    private void validateSectionOrder(List<StructuralNode> sections, String filename, 
                                     ValidationResult.Builder resultBuilder) {
        List<SectionConfig> orderedConfigs = rootSections.stream()
            .filter(config -> config.order() != null)
            .sorted(Comparator.comparing(SectionConfig::order))
            .collect(Collectors.toList());
        
        if (orderedConfigs.isEmpty()) {
            return;
        }
        
        Map<String, Integer> actualOrder = new HashMap<>();
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i) instanceof Section) {
                Section section = (Section) sections.get(i);
                SectionConfig config = findMatchingConfig(section, rootSections);
                if (config != null && config.order() != null) {
                    actualOrder.put(config.name(), i);
                }
            }
        }
        
        for (int i = 0; i < orderedConfigs.size() - 1; i++) {
            SectionConfig current = orderedConfigs.get(i);
            SectionConfig next = orderedConfigs.get(i + 1);
            
            Integer currentPos = actualOrder.get(current.name());
            Integer nextPos = actualOrder.get(next.name());
            
            if (currentPos != null && nextPos != null && currentPos > nextPos) {
                SourceLocation location = SourceLocation.builder()
                    .filename(filename)
                    .line(1)
                    .build();
                
                ValidationMessage message = ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("section.order")
                    .location(location)
                    .message("Section order violation")
                    .actualValue(current.name() + " appears after " + next.name())
                    .expectedValue(current.name() + " should appear before " + next.name())
                    .build();
                resultBuilder.addMessage(message);
            }
        }
    }

    private SectionConfig findMatchingConfig(Section section, List<SectionConfig> configs) {
        String title = section.getTitle();
        int level = section.getLevel();
        
        for (SectionConfig config : configs) {
            if (config.level() != level) {
                continue;
            }
            
            if (config.title() != null && config.title().pattern() != null) {
                Pattern pattern = Pattern.compile(config.title().pattern());
                if (pattern.matcher(title).matches()) {
                    return config;
                }
            } else if (config.name() != null) {
                return config;
            }
        }
        
        return null;
    }
    
    private List<SectionConfig> findConfigsForLevel(int level, List<SectionConfig> configs) {
        return configs.stream()
            .filter(config -> config.level() == level)
            .collect(Collectors.toList());
    }
    
    private SectionConfig findConfigForOccurrenceTracking(Section section, List<SectionConfig> configs) {
        int level = section.getLevel();
        
        // Find the first config that matches the level and has a name (for occurrence tracking)
        return configs.stream()
            .filter(config -> config.level() == level && config.name() != null)
            .findFirst()
            .orElse(null);
    }
    
    private List<SectionConfig> determineSubsectionConfigsFromParent(List<SectionConfig> parentConfigs, int parentLevel) {
        // When a parent section doesn't match its pattern, we still need to find the right configs for its subsections
        // First, check if any of the parent-level configs have subsections defined
        for (SectionConfig config : parentConfigs) {
            if (config.level() == parentLevel && config.subsections() != null && !config.subsections().isEmpty()) {
                return config.subsections();
            }
        }
        
        // If no subsections found in parent-level configs, return configs for the next level
        int subsectionLevel = parentLevel + 1;
        List<SectionConfig> subsectionConfigs = findConfigsForLevel(subsectionLevel, parentConfigs);
        
        if (!subsectionConfigs.isEmpty()) {
            return subsectionConfigs;
        }
        
        // Last resort: return original configs
        return parentConfigs;
    }

    private void trackOccurrence(SectionConfig config) {
        String key = createOccurrenceKey(config);
        sectionOccurrences.merge(key, 1, Integer::sum);
    }

    private String createOccurrenceKey(SectionConfig config) {
        return config.name() + "_" + config.level();
    }

    private String extractFilename(Document document) {
        Map<String, Object> attrs = document.getAttributes();
        if (attrs.containsKey("docfile")) {
            return attrs.get("docfile").toString();
        }
        return "unknown";
    }
    
    private SourceLocation createDocumentTitleLocation(String filename, String title) {
        List<String> fileLines = fileCache.getFileLines(filename);
        
        if (fileLines.isEmpty()) {
            return SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
        }
        
        // Document title is typically on line 1
        String firstLine = fileLines.get(0);
        
        // Document title has format: = Title
        if (firstLine.startsWith("= ") && title != null) {
            int titleStart = 2; // After "= "
            int titleEnd = titleStart + title.length() - 1;
            
            return SourceLocation.builder()
                .filename(filename)
                .startLine(1)
                .endLine(1)
                .startColumn(titleStart + 1) // Convert to 1-based
                .endColumn(titleEnd + 1)     // Convert to 1-based
                .build();
        }
        
        // Fallback
        return SourceLocation.builder()
            .filename(filename)
            .line(1)
            .build();
    }

    private SourceLocation createLocation(String filename, Section section) {
        int lineNumber = section.getSourceLocation() != null ? section.getSourceLocation().getLineNumber() : 1;
        List<String> fileLines = fileCache.getFileLines(filename);
        
        if (fileLines.isEmpty() || lineNumber <= 0 || lineNumber > fileLines.size()) {
            return SourceLocation.builder()
                .filename(filename)
                .line(lineNumber)
                .build();
        }
        
        String line = fileLines.get(lineNumber - 1);
        String title = section.getTitle();
        int level = section.getLevel();
        
        // Find the title position in the line
        // Section titles have format: == Title or === Title etc.
        String prefix = "=".repeat(level + 1) + " ";
        int prefixIndex = line.indexOf(prefix);
        
        if (prefixIndex >= 0 && title != null) {
            int titleStart = prefixIndex + prefix.length();
            int titleEnd = titleStart + title.length() - 1;
            
            return SourceLocation.builder()
                .filename(filename)
                .startLine(lineNumber)
                .endLine(lineNumber)
                .startColumn(titleStart + 1) // Convert to 1-based
                .endColumn(titleEnd + 1)     // Convert to 1-based
                .build();
        }
        
        // Fallback if we can't find the exact position
        return SourceLocation.builder()
            .filename(filename)
            .line(lineNumber)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromConfiguration(DocumentConfiguration configuration) {
        return builder().configuration(configuration);
    }

    public static final class Builder {
        private DocumentConfiguration configuration;

        private Builder() {
        }

        public Builder configuration(DocumentConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public SectionValidator build() {
            return new SectionValidator(this);
        }
    }
}