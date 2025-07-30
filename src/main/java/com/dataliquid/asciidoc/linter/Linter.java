package com.dataliquid.asciidoc.linter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.validator.BlockValidator;
import com.dataliquid.asciidoc.linter.validator.MetadataValidator;
import com.dataliquid.asciidoc.linter.validator.SectionValidator;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.ValidationResult;

/**
 * Main entry point for the AsciiDoc linter.
 * Provides methods to validate AsciiDoc files against a configuration.
 */
public class Linter {
    
    private static final Logger logger = LogManager.getLogger(Linter.class);
    
    private final Asciidoctor asciidoctor;
    
    public Linter() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }
    
    /**
     * Validates a single AsciiDoc file.
     * 
     * @param file the file to validate
     * @param config the linter configuration
     * @return validation result
     * @throws IOException if the file cannot be read
     */
    public ValidationResult validateFile(Path file, LinterConfiguration config) throws IOException {
        Objects.requireNonNull(file, "[" + getClass().getName() + "] file must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        
        if (!Files.exists(file)) {
            throw new IOException("File does not exist: " + file);
        }
        
        if (!Files.isRegularFile(file)) {
            throw new IOException("Not a regular file: " + file);
        }
        
        return performValidation(file, config);
    }
    
    /**
     * Validates multiple AsciiDoc files.
     * 
     * @param files the files to validate
     * @param config the linter configuration
     * @return map of file to validation result
     */
    public Map<Path, ValidationResult> validateFiles(List<Path> files, LinterConfiguration config) {
        Objects.requireNonNull(files, "[" + getClass().getName() + "] files must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        
        Map<Path, ValidationResult> results = new LinkedHashMap<>();
        
        for (Path file : files) {
            try {
                ValidationResult result = validateFile(file, config);
                results.put(file, result);
            } catch (IOException e) {
                // Create error result
                ValidationResult errorResult = createIOErrorResult(file, e);
                results.put(file, errorResult);
            }
        }
        
        return results;
    }
    
    /**
     * Validates all matching files in a directory.
     * 
     * @param directory the directory to scan
     * @param pattern file pattern (e.g., "*.adoc")
     * @param recursive whether to scan subdirectories
     * @param config the linter configuration
     * @return map of file to validation result
     * @throws IOException if the directory cannot be read
     */
    public Map<Path, ValidationResult> validateDirectory(Path directory, String pattern, 
                                                       boolean recursive, LinterConfiguration config) throws IOException {
        Objects.requireNonNull(directory, "[" + getClass().getName() + "] directory must not be null");
        Objects.requireNonNull(pattern, "[" + getClass().getName() + "] pattern must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        
        if (!Files.isDirectory(directory)) {
            throw new IOException("Not a directory: " + directory);
        }
        
        List<Path> files = findMatchingFiles(directory, pattern, recursive);
        return validateFiles(files, config);
    }
    
    /**
     * Validates AsciiDoc content from a string.
     * 
     * @param content the AsciiDoc content to validate
     * @param config the linter configuration
     * @return validation result
     */
    public ValidationResult validateContent(String content, LinterConfiguration config) {
        Objects.requireNonNull(content, "[" + getClass().getName() + "] content must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        
        String filename = "inline-content";
        
        try {
            // Parse the document from string
            // Enable AsciidoctorJ's built-in front matter handling
            Attributes documentAttributes = Attributes.builder()
                .skipFrontMatter(true)
                .build();
            
            Options options = Options.builder()
                .sourcemap(true)  // Enable source location tracking
                .toFile(false)    // Don't write output file
                .attributes(documentAttributes)
                .build();
            Document document = asciidoctor.load(content, options);
            
            // Extract filename from document title if available
            if (document.getTitle() != null && !document.getTitle().isEmpty()) {
                filename = document.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase() + ".adoc";
            }
            
            return performValidation(document, filename, config);
        } catch (Exception e) {
            // Create error result for parse failure
            return ValidationResult.builder()
                .addScannedFile(filename)
                .addMessage(createParseErrorMessage(filename, e))
                .complete()
                .build();
        }
    }
    
    /**
     * Closes the linter and releases resources.
     */
    public void close() {
        if (asciidoctor != null) {
            asciidoctor.close();
        }
    }
    
    private ValidationResult performValidation(Path file, LinterConfiguration config) {
        try {
            // Parse the document
            // Enable AsciidoctorJ's built-in front matter handling
            Attributes documentAttributes = Attributes.builder()
                .skipFrontMatter(true)
                .build();
            
            Options options = Options.builder()
                .sourcemap(true)  // Enable source location tracking
                .toFile(false)    // Don't write output file
                .attributes(documentAttributes)
                .build();
            Document document = asciidoctor.loadFile(file.toFile(), options);
            
            return performValidation(document, file.toString(), config);
        } catch (Exception e) {
            // Create error result for parse failure
            return ValidationResult.builder()
                .addScannedFile(file.toString())
                .addMessage(createParseErrorMessage(file, e))
                .complete()
                .build();
        }
    }
    
    private ValidationResult performValidation(Document document, String filename, LinterConfiguration config) {
        ValidationResult.Builder resultBuilder = ValidationResult.builder()
                .addScannedFile(filename);
        
        // Run validators
        List<ValidationMessage> messages = new ArrayList<>();
        
        if (config.document() != null) {
            // Metadata validation
            if (config.document().metadata() != null) {
                MetadataValidator metadataValidator = MetadataValidator
                    .fromConfiguration(config.document().metadata())
                    .build();
                ValidationResult metadataResult = metadataValidator.validate(document);
                messages.addAll(metadataResult.getMessages());
            }
            
            // Section validation
            if (config.document().sections() != null) {
                SectionValidator sectionValidator = SectionValidator.builder()
                    .configuration(config.document())
                    .build();
                ValidationResult sectionResult = sectionValidator.validate(document, filename);
                messages.addAll(sectionResult.getMessages());
                
                // Block validation within sections
                messages.addAll(validateBlocks(document, config.document().sections(), filename));
            }
        }
        
        // Add all messages to result
        messages.forEach(resultBuilder::addMessage);
        
        return resultBuilder.complete().build();
    }
    
    private List<ValidationMessage> validateBlocks(Document document, List<SectionConfig> sectionConfigs, String filename) {
        List<ValidationMessage> messages = new ArrayList<>();
        BlockValidator blockValidator = new BlockValidator();
        
        // Process level 0 (document title) configurations
        List<SectionConfig> level0Configs = extractLevel0Configs(sectionConfigs);
        List<SectionConfig> configsForLevel1Sections = determineConfigsForLevel1Sections(level0Configs, sectionConfigs);
        
        // Debug logging
        logger.debug("validateBlocks: level0Configs.size()={}, configsForLevel1Sections.size()={}", 
            level0Configs.size(), configsForLevel1Sections.size());
        
        // Validate document-level blocks (only if Level 0 config exists)
        if (!level0Configs.isEmpty()) {
            validateDocumentLevelBlocks(document, level0Configs, blockValidator, filename, messages);
        }
        
        // Validate sections and their blocks
        validateDocumentSections(document, configsForLevel1Sections, blockValidator, filename, messages);
        
        return messages;
    }
    
    private List<SectionConfig> extractLevel0Configs(List<SectionConfig> sectionConfigs) {
        return sectionConfigs.stream()
            .filter(config -> config.level() == 0)
            .collect(Collectors.toList());
    }
    
    private List<SectionConfig> determineConfigsForLevel1Sections(List<SectionConfig> level0Configs, 
                                                                   List<SectionConfig> allConfigs) {
        // If level 0 config has subsections, use those for level 1 sections
        // Otherwise, use all non-level-0 configs from the top level
        for (SectionConfig level0Config : level0Configs) {
            if (level0Config.subsections() != null && !level0Config.subsections().isEmpty()) {
                return level0Config.subsections();
            }
        }
        
        // Fallback: use all non-level-0 configs
        return allConfigs.stream()
            .filter(config -> config.level() != 0)
            .collect(Collectors.toList());
    }
    
    private void validateDocumentLevelBlocks(Document document, List<SectionConfig> level0Configs,
                                           BlockValidator blockValidator, String filename,
                                           List<ValidationMessage> messages) {
        for (SectionConfig level0Config : level0Configs) {
            // Only validate document-level blocks if level 0 config has allowedBlocks
            if (level0Config.allowedBlocks() != null && !level0Config.allowedBlocks().isEmpty()) {
                ValidationResult blockResult = blockValidator.validate(document, level0Config, filename);
                messages.addAll(blockResult.getMessages());
            }
        }
    }
    
    private void validateDocumentSections(Document document, List<SectionConfig> sectionConfigs,
                                        BlockValidator blockValidator, String filename,
                                        List<ValidationMessage> messages) {
        for (StructuralNode node : document.getBlocks()) {
            // Only process sections, skip preamble and other document-level blocks
            if (node instanceof org.asciidoctor.ast.Section) {
                org.asciidoctor.ast.Section section = (org.asciidoctor.ast.Section) node;
                validateSectionBlocks(section, sectionConfigs, blockValidator, filename, messages);
            } else {
                // Debug: Log what we're skipping
                logger.debug("validateDocumentSections: Skipping non-section node: context={}, nodeName={}", 
                    node.getContext(), node.getNodeName());
            }
            // Note: Document-level blocks (preamble) are handled by validateDocumentLevelBlocks
        }
    }
    
    private void validateSectionBlocks(org.asciidoctor.ast.Section section, 
                                     List<SectionConfig> sectionConfigs,
                                     BlockValidator blockValidator,
                                     String filename,
                                     List<ValidationMessage> messages) {
        
        Optional<SectionConfig> matchingConfig = findMatchingSectionConfig(section, sectionConfigs);
        
        if (matchingConfig.isPresent()) {
            SectionConfig config = matchingConfig.get();
            
            // Validate blocks in this section
            validateSectionContent(section, config, blockValidator, filename, messages);
            
            // Process subsections with appropriate configs
            List<SectionConfig> subsectionConfigs = determineSubsectionConfigs(config, sectionConfigs);
            processSubsections(section, subsectionConfigs, blockValidator, filename, messages);
        } else {
            // No matching config found - still process subsections with parent configs
            processSubsections(section, sectionConfigs, blockValidator, filename, messages);
        }
    }
    
    private Optional<SectionConfig> findMatchingSectionConfig(org.asciidoctor.ast.Section section, 
                                                            List<SectionConfig> sectionConfigs) {
        return sectionConfigs.stream()
            .filter(config -> config.level() != 0 && matchesSection(section, config))
            .findFirst();
    }
    
    private void validateSectionContent(org.asciidoctor.ast.Section section, SectionConfig config,
                                      BlockValidator blockValidator, String filename,
                                      List<ValidationMessage> messages) {
        ValidationResult blockResult = blockValidator.validate(section, config, filename);
        messages.addAll(blockResult.getMessages());
    }
    
    private List<SectionConfig> determineSubsectionConfigs(SectionConfig parentConfig, 
                                                          List<SectionConfig> fallbackConfigs) {
        if (parentConfig.subsections() != null && !parentConfig.subsections().isEmpty()) {
            return parentConfig.subsections();
        }
        return fallbackConfigs;
    }
    
    private void processSubsections(org.asciidoctor.ast.Section section, 
                                  List<SectionConfig> subsectionConfigs,
                                  BlockValidator blockValidator, String filename,
                                  List<ValidationMessage> messages) {
        for (StructuralNode node : section.getBlocks()) {
            if (node instanceof org.asciidoctor.ast.Section) {
                org.asciidoctor.ast.Section subsection = (org.asciidoctor.ast.Section) node;
                validateSectionBlocks(subsection, subsectionConfigs, blockValidator, filename, messages);
            }
        }
    }
    
    private List<Path> findMatchingFiles(Path directory, String pattern, boolean recursive) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        PathMatcher pathMatcher = directory.getFileSystem().getPathMatcher("glob:" + pattern);
        int maxDepth = recursive ? Integer.MAX_VALUE : 1;
        
        Files.walkFileTree(directory, new HashSet<>(), maxDepth, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (pathMatcher.matches(file.getFileName())) {
                    matchingFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log warning but continue
                logger.warn("Could not access file: {} ({})", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        return matchingFiles;
    }
    
    private boolean matchesSection(org.asciidoctor.ast.Section section, SectionConfig config) {
        // First check level match
        if (config.level() != section.getLevel()) {
            return false;
        }
        
        // Then check title constraints if configured
        if (config.title() != null && config.title().pattern() != null) {
            String title = section.getTitle();
            if (title == null) {
                return false;
            }
            
            // Check pattern match
            return title.matches(config.title().pattern());
        }
        
        // Level matches and no title constraints
        return true;
    }
    
    private ValidationResult createIOErrorResult(Path file, IOException e) {
        return ValidationResult.builder()
            .addScannedFile(file.toString())
            .addMessage(ValidationMessage.builder()
                .severity(com.dataliquid.asciidoc.linter.config.Severity.ERROR)
                .ruleId("io-error")
                .location(SourceLocation.builder()
                    .filename(file.toString())
                    .startLine(1)
                    .build())
                .message("I/O error: " + e.getMessage())
                .cause(e)
                .build())
            .complete()
            .build();
    }
    
    private ValidationMessage createParseErrorMessage(Path file, Exception e) {
        return createParseErrorMessage(file.toString(), e);
    }
    
    private ValidationMessage createParseErrorMessage(String filename, Exception e) {
        return ValidationMessage.builder()
            .severity(com.dataliquid.asciidoc.linter.config.Severity.ERROR)
            .ruleId("parse-error")
            .location(SourceLocation.builder()
                .filename(filename)
                .startLine(1)
                .build())
            .message("Failed to parse AsciiDoc file: " + e.getMessage())
            .cause(e)
            .build();
    }
}