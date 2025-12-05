package com.dataliquid.asciidoc.linter.documentation;

import java.io.PrintWriter;
import java.util.EnumSet;
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
import com.dataliquid.asciidoc.linter.documentation.block.AdmonitionBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.BlockDocGeneratorRegistry;
import com.dataliquid.asciidoc.linter.documentation.block.ExampleBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.ImageBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.ListBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.ListingBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.MediaBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.ParagraphBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.QuoteBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.SidebarBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.SpecialBlockDocGenerator;
import com.dataliquid.asciidoc.linter.documentation.block.TableBlockDocGenerator;

/**
 * Generates author guidelines from linter configuration rules.
 * <p>
 * This generator creates human-readable author guidelines in AsciiDoc format,
 * helping content authors understand the validation requirements for their
 * documents.
 * </p>
 */
public class AsciiDocAuthorGuidelineGenerator implements RuleDocumentationGenerator {

    // Constants for duplicate literals
    private static final String TABLE_SEPARATOR = "|===";
    private static final String CHARACTERS_SUFFIX = " characters";
    private static final String PMD_UNUSED_PARAM = "PMD.UnusedFormalParameter";

    private final Set<VisualizationStyle> visualizationStyles;
    private final PatternHumanizer patternHumanizer;
    private final HierarchyVisualizerFactory visualizerFactory;
    private final BlockDocGeneratorRegistry blockDocRegistry;

    /**
     * Creates a new AsciiDoc author guideline generator with default visualization
     * styles.
     */
    public AsciiDocAuthorGuidelineGenerator() {
        this(Set.of((VisualizationStyle) VisualizationStyle.TREE));
    }

    /**
     * Creates a new AsciiDoc author guideline generator with specified
     * visualization styles.
     *
     * @param visualizationStyles the visualization styles to use
     */
    public AsciiDocAuthorGuidelineGenerator(Set<VisualizationStyle> visualizationStyles) {
        this.visualizationStyles = EnumSet.copyOf(visualizationStyles);
        this.patternHumanizer = new PatternHumanizer();
        this.visualizerFactory = new HierarchyVisualizerFactory();
        this.blockDocRegistry = initializeBlockDocRegistry();
    }

    private BlockDocGeneratorRegistry initializeBlockDocRegistry() {
        BlockDocGeneratorRegistry registry = new BlockDocGeneratorRegistry();
        registry.register(new ImageBlockDocGenerator());
        registry.register(new ListingBlockDocGenerator());
        registry.register(new ParagraphBlockDocGenerator());
        registry.register(MediaBlockDocGenerator.createAudioGenerator());
        registry.register(MediaBlockDocGenerator.createVideoGenerator());
        registry.register(new TableBlockDocGenerator());
        registry.register(new AdmonitionBlockDocGenerator());
        registry.register(new ExampleBlockDocGenerator());
        registry.register(new QuoteBlockDocGenerator());
        registry.register(new SidebarBlockDocGenerator());
        registry.register(ListBlockDocGenerator.createUlistGenerator());
        registry.register(ListBlockDocGenerator.createDlistGenerator());
        registry.register(SpecialBlockDocGenerator.createLiteralGenerator());
        registry.register(SpecialBlockDocGenerator.createPassGenerator());
        registry.register(SpecialBlockDocGenerator.createVerseGenerator());
        return registry;
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
    }

    @Override
    public DocumentationFormat getFormat() {
        return DocumentationFormat.ASCIIDOC;
    }

    @Override
    public String getName() {
        return "AsciiDoc Author Guideline Generator";
    }

    private void generateHeader(PrintWriter writer) {
        writer.println("= AsciiDoc Author Guidelines");
        writer.println(":toc: left");
        writer.println(":toclevels: 3");
        writer.println(":icons: font");
        writer.println(":source-highlighter: rouge");
        writer.println();
    }

    private void generateIntroduction(PrintWriter writer) {
        writer.println("== Introduction");
        writer.println();
        writer.println("These author guidelines describe the requirements for AsciiDoc documents in this project.");
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
        List<AttributeConfig> requiredAttrs = metadata.attributes().stream().filter(AttributeConfig::required).toList();

        List<AttributeConfig> optionalAttrs = metadata.attributes().stream().filter(attr -> !attr.required()).toList();

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
        writer.println(TABLE_SEPARATOR);
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

        writer.println(TABLE_SEPARATOR);
        writer.println();
    }

    private void generateAttributeRequirements(AttributeConfig attr, PrintWriter writer) {
        if (attr.required()) {
            writer.println("* Required");
        } else {
            writer.println("* Optional");
        }

        if (attr.minLength() != null) {
            writer.println("* Minimum length: " + attr.minLength() + CHARACTERS_SUFFIX);
        }

        if (attr.maxLength() != null) {
            writer.println("* Maximum length: " + attr.maxLength() + CHARACTERS_SUFFIX);
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
            writer.println(TABLE_SEPARATOR);
            writer.println("|Block Type |Requirements");
            writer.println();

            for (Block block : section.allowedBlocks()) {
                writer.println("|" + formatBlockType(block));
                writer.println("a|");
                generateBlockSummary(block, writer);
                writer.println();
            }

            writer.println(TABLE_SEPARATOR);
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
        int min = section.occurrence() != null ? section.occurrence().min() : 0;
        int max = section.occurrence() != null ? section.occurrence().max() : Integer.MAX_VALUE;
        String severity = min > 0 ? "CAUTION" : "NOTE";

        writer.println("[" + severity + "]");
        writer.println("====");
        writer.println("**Position**: " + (section.order() != null ? "Position " + section.order() : "Any"));
        writer.println("**Level**: " + section.level());
        writer.println("**Required**: " + (min > 0 ? "Yes" : "No"));

        writer.print("**Count**: ");
        if (max > 0 && max != Integer.MAX_VALUE) {
            writer.println(min + "-" + max);
        } else {
            writer.println("At least " + min);
        }

        // Add title pattern if present
        if (section.title() != null && section.title().pattern() != null) {
            writer.println("**Title Pattern**: " + patternHumanizer.humanize(section.title().pattern()));
        }

        writer.println("====");
        writer.println();
    }

    @SuppressWarnings(PMD_UNUSED_PARAM)
    private void generateBlockReferenceSection(DocumentConfiguration document, PrintWriter writer) {
        writer.println("== Block Reference");
        writer.println();
        writer.println("Detailed description of all available block types and their validation rules.");
        writer.println();

        // TODO: Collect all unique block types from configuration and generate detailed
        // docs
    }

    private void generateValidationLevelsSection(PrintWriter writer) {
        writer.println("== Validation Levels");
        writer.println();

        writer.println("[cols=\"1,1,3\", options=\"header\"]");
        writer.println(TABLE_SEPARATOR);
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
        writer.println(TABLE_SEPARATOR);
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
        // Basic information
        writer.println("* Count: " + getOccurrenceText(block));
        if (block.getSeverity() != null) {
            writer.println("* Severity: " + block.getSeverity());
        }

        // Delegate to registered block doc generator
        blockDocRegistry.generateBlockDetails(block, writer, patternHumanizer);
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
