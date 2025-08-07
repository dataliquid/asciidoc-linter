package com.dataliquid.asciidoc.linter.documentation;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

/**
 * Generates AsciiDoc documentation from linter configuration rules.
 * 
 * <p>This generator creates human-readable documentation in AsciiDoc format,
 * suitable for content authors who need to understand the validation rules
 * for their documents.</p>
 */
public class AsciiDocRuleGenerator implements RuleDocumentationGenerator {
    
    private final Set<VisualizationStyle> visualizationStyles;
    private final PatternHumanizer patternHumanizer;
    private final HierarchyVisualizerFactory visualizerFactory;
    
    /**
     * Creates a new AsciiDoc rule generator with default visualization styles.
     */
    public AsciiDocRuleGenerator() {
        this(Set.of(VisualizationStyle.TREE));
    }
    
    /**
     * Creates a new AsciiDoc rule generator with specified visualization styles.
     * 
     * @param visualizationStyles the visualization styles to use
     */
    public AsciiDocRuleGenerator(Set<VisualizationStyle> visualizationStyles) {
        this.visualizationStyles = new HashSet<>(visualizationStyles);
        this.patternHumanizer = new PatternHumanizer();
        this.visualizerFactory = new HierarchyVisualizerFactory();
    }
    
    @Override
    public void generate(LinterConfiguration config, PrintWriter writer) {
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        Objects.requireNonNull(writer, "[" + getClass().getName() + "] writer must not be null");
        
        generateHeader(writer);
        generateIntroduction(writer);
        generateMetadataSection(config.document().metadata(), writer);
        generateStructureSection(config.document(), writer);
        generateBlockReferenceSection(config.document(), writer);
        generateValidationLevelsSection(writer);
        generateTipsSection(writer);
    }
    
    @Override
    public DocumentationFormat getFormat() {
        return DocumentationFormat.ASCIIDOC;
    }
    
    @Override
    public String getName() {
        return "AsciiDoc Rule Documentation Generator";
    }
    
    private void generateHeader(PrintWriter writer) {
        writer.println("= AsciiDoc Document Guidelines");
        writer.println(":toc: left");
        writer.println(":toclevels: 3");
        writer.println(":icons: font");
        writer.println(":source-highlighter: rouge");
        writer.println();
    }
    
    private void generateIntroduction(PrintWriter writer) {
        writer.println("== Introduction");
        writer.println();
        writer.println("These guidelines describe the requirements for AsciiDoc documents in this project.");
        writer.println("All documents are automatically validated against these rules.");
        writer.println();
    }
    
    private void generateMetadataSection(MetadataConfiguration metadata, PrintWriter writer) {
        if (metadata == null || metadata.attributes() == null || metadata.attributes().isEmpty()) {
            return;
        }
        
        writer.println("== Document Metadata");
        writer.println();
        writer.println("Each document must define the following metadata attributes:");
        writer.println();
        
        // Separate required and optional attributes
        List<AttributeConfig> requiredAttrs = metadata.attributes().stream()
            .filter(AttributeConfig::required)
            .toList();
        
        List<AttributeConfig> optionalAttrs = metadata.attributes().stream()
            .filter(attr -> !attr.required())
            .toList();
        
        if (!requiredAttrs.isEmpty()) {
            writer.println("=== Required Attributes");
            writer.println();
            generateAttributeTable(requiredAttrs, writer);
        }
        
        if (!optionalAttrs.isEmpty()) {
            writer.println("=== Optional Attributes");
            writer.println();
            generateAttributeTable(optionalAttrs, writer);
        }
    }
    
    private void generateAttributeTable(List<AttributeConfig> attributes, PrintWriter writer) {
        writer.println("[cols=\"1,2,1,3\", options=\"header\"]");
        writer.println("|===");
        writer.println("|Attribute |Description |Severity |Requirements");
        writer.println();
        
        for (AttributeConfig attr : attributes) {
            writer.println("|" + attr.name());
            writer.println("|" + getAttributeDescription(attr));
            writer.println("|" + formatSeverity(attr.severity()));
            writer.println("a|");
            generateAttributeRequirements(attr, writer);
            writer.println();
        }
        
        writer.println("|===");
        writer.println();
    }
    
    private void generateAttributeRequirements(AttributeConfig attr, PrintWriter writer) {
        if (attr.required()) {
            writer.println("* Required");
        } else {
            writer.println("* Optional");
        }
        
        if (attr.minLength() != null) {
            writer.println("* Minimum length: " + attr.minLength() + " characters");
        }
        
        if (attr.maxLength() != null) {
            writer.println("* Maximum length: " + attr.maxLength() + " characters");
        }
        
        if (attr.pattern() != null) {
            writer.println("* Format: " + patternHumanizer.humanize(attr.pattern()));
        }
        
        // TODO: Add support for allowed values when available in AttributeConfig
        
        writer.println("* Example: `:" + attr.name() + ": " + generateAttributeExample(attr) + "`");
    }
    
    private void generateStructureSection(DocumentConfiguration document, PrintWriter writer) {
        writer.println("== Document Structure");
        writer.println();
        
        // Generate hierarchy visualization based on selected styles
        for (VisualizationStyle style : visualizationStyles) {
            HierarchyVisualizer visualizer = visualizerFactory.create(style);
            writer.println("=== " + style.getDescription());
            writer.println();
            visualizer.visualize(LinterConfiguration.builder().document(document).build(), writer);
            writer.println();
        }
        
        // Generate detailed section documentation
        if (document.sections() != null && !document.sections().isEmpty()) {
            writer.println("== Section Details");
            writer.println();
            
            for (SectionConfig section : document.sections()) {
                generateSectionDetails(section, writer);
            }
        }
    }
    
    private void generateSectionDetails(SectionConfig section, PrintWriter writer) {
        writer.println("=== Section: " + section.name());
        writer.println();
        
        generateSectionNote(section, writer);
        
        if (section.allowedBlocks() != null && !section.allowedBlocks().isEmpty()) {
            writer.println(".Allowed Content");
            writer.println("[cols=\"1,3\", options=\"header\"]");
            writer.println("|===");
            writer.println("|Block Type |Requirements");
            writer.println();
            
            for (Block block : section.allowedBlocks()) {
                writer.println("|" + formatBlockType(block));
                writer.println("a|");
                generateBlockSummary(block, writer);
                writer.println();
            }
            
            writer.println("|===");
            writer.println();
        }
        
        generateSectionExample(section, writer);
        
        // Generate subsections
        if (section.subsections() != null && !section.subsections().isEmpty()) {
            for (SectionConfig subsection : section.subsections()) {
                generateSectionDetails(subsection, writer);
            }
        }
    }
    
    private void generateSectionNote(SectionConfig section, PrintWriter writer) {
        String severity = section.min() > 0 ? "CAUTION" : "NOTE";
        
        writer.println("[" + severity + "]");
        writer.println("====");
        writer.println("**Position**: " + (section.order() != null ? 
            "Position " + section.order() : "Any"));
        writer.println("**Level**: " + section.level());
        writer.println("**Required**: " + (section.min() > 0 ? "Yes" : "No"));
        
        writer.print("**Count**: ");
        if (section.max() > 0) {
            writer.println(section.min() + "-" + section.max());
        } else {
            writer.println("At least " + section.min());
        }
        
        writer.println("====");
        writer.println();
    }
    
    private void generateBlockReferenceSection(DocumentConfiguration document, PrintWriter writer) {
        writer.println("== Block Reference");
        writer.println();
        writer.println("Detailed description of all available block types and their validation rules.");
        writer.println();
        
        // TODO: Collect all unique block types from configuration and generate detailed docs
    }
    
    private void generateValidationLevelsSection(PrintWriter writer) {
        writer.println("== Validation Levels");
        writer.println();
        
        writer.println("[cols=\"1,1,3\", options=\"header\"]");
        writer.println("|===");
        writer.println("|Level |Symbol |Meaning");
        writer.println();
        writer.println("|ERROR");
        writer.println("|icon:times-circle[role=\"red\"]");
        writer.println("|Document will be rejected, must be corrected");
        writer.println();
        writer.println("|WARN");
        writer.println("|icon:exclamation-triangle[role=\"yellow\"]");
        writer.println("|Should be fixed, but document will be accepted");
        writer.println();
        writer.println("|INFO");
        writer.println("|icon:info-circle[role=\"blue\"]");
        writer.println("|Suggestion for improvement, optional");
        writer.println("|===");
        writer.println();
    }
    
    private void generateTipsSection(PrintWriter writer) {
        writer.println("== Tips for Authors");
        writer.println();
        writer.println("TIP: Use the linter while writing with `--watch` mode.");
        writer.println();
        writer.println("TIP: Error messages always contain the expected values.");
        writer.println();
        writer.println("IMPORTANT: For questions about the rules, contact the Documentation Team.");
        writer.println();
    }
    
    // Helper methods
    private String getAttributeDescription(AttributeConfig attr) {
        // TODO: Load from schema or generate based on name
        return switch (attr.name()) {
            case "title" -> "Document title";
            case "author" -> "Document author";
            case "version" -> "Document version";
            case "email" -> "Contact email";
            default -> attr.name();
        };
    }
    
    private String formatSeverity(Severity severity) {
        return severity.name();
    }
    
    private String generateAttributeExample(AttributeConfig attr) {
        return switch (attr.name()) {
            case "title" -> "User Guide for AsciiDoc Linter";
            case "author" -> "John Doe";
            case "version" -> "1.0.0";
            case "email" -> "author@example.com";
            default -> "Example value";
        };
    }
    
    private String formatBlockType(Block block) {
        return block.getType().toValue();
    }
    
    private void generateBlockSummary(Block block, PrintWriter writer) {
        // TODO: Generate summary based on block type
        writer.println("* Count: " + getOccurrenceText(block));
        if (block.getSeverity() != null) {
            writer.println("* Severity: " + block.getSeverity());
        }
    }
    
    private String getOccurrenceText(Block block) {
        if (block.getOccurrence() == null) {
            return "Any";
        }
        
        int min = block.getOccurrence().min();
        int max = block.getOccurrence().max();
        
        if (min > 0 && max < Integer.MAX_VALUE) {
            return min + "-" + max;
        } else if (min > 0) {
            return "At least " + min;
        } else if (max < Integer.MAX_VALUE) {
            return "At most " + max;
        }
        return "Any";
    }
    
    private void generateSectionExample(SectionConfig section, PrintWriter writer) {
        writer.println(".Example");
        writer.println("[source,asciidoc]");
        writer.println("----");
        
        // Generate example based on section level
        String prefix = "=".repeat(section.level() + 1);
        writer.println(prefix + " " + section.name());
        writer.println();
        writer.println("Example content for this section.");
        
        writer.println("----");
        writer.println();
    }
}