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
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.config.blocks.AdmonitionBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.config.blocks.DlistBlock;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;

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
    private static final String REQUIRED_YES = "  - Required: Yes";
    private static final String PMD_UNUSED_PARAM = "PMD.UnusedFormalParameter";
    private static final String PATTERN_PREFIX = "  - Pattern: ";
    private static final String MINIMUM_PREFIX = "  - Minimum: ";
    private static final String MAXIMUM_PREFIX = "  - Maximum: ";
    private static final String MINIMUM_LENGTH_PREFIX = "  - Minimum length: ";
    private static final String MAXIMUM_LENGTH_PREFIX = "  - Maximum length: ";
    private static final String TITLE_HEADER = "* **Title:**";

    private final Set<VisualizationStyle> visualizationStyles;
    private final PatternHumanizer patternHumanizer;
    private final HierarchyVisualizerFactory visualizerFactory;

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

        // Generate detailed requirements based on block type
        if (block instanceof ImageBlock) {
            generateImageBlockDetails((ImageBlock) block, writer);
        } else if (block instanceof ListingBlock) {
            generateListingBlockDetails((ListingBlock) block, writer);
        } else if (block instanceof ParagraphBlock) {
            generateParagraphBlockDetails((ParagraphBlock) block, writer);
        } else if (block instanceof AudioBlock) {
            generateAudioBlockDetails((AudioBlock) block, writer);
        } else if (block instanceof VideoBlock) {
            generateVideoBlockDetails((VideoBlock) block, writer);
        } else if (block instanceof TableBlock) {
            generateTableBlockDetails((TableBlock) block, writer);
        } else if (block instanceof AdmonitionBlock) {
            generateAdmonitionBlockDetails((AdmonitionBlock) block, writer);
        } else if (block instanceof ExampleBlock) {
            generateExampleBlockDetails((ExampleBlock) block, writer);
        } else if (block instanceof QuoteBlock) {
            generateQuoteBlockDetails((QuoteBlock) block, writer);
        } else if (block instanceof SidebarBlock) {
            generateSidebarBlockDetails((SidebarBlock) block, writer);
        } else if (block instanceof UlistBlock) {
            generateUlistBlockDetails((UlistBlock) block, writer);
        } else if (block instanceof DlistBlock) {
            generateDlistBlockDetails((DlistBlock) block, writer);
        } else if (block instanceof LiteralBlock) {
            generateLiteralBlockDetails((LiteralBlock) block, writer);
        } else if (block instanceof PassBlock) {
            generatePassBlockDetails((PassBlock) block, writer);
        } else if (block instanceof VerseBlock) {
            generateVerseBlockDetails((VerseBlock) block, writer);
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

    private void generateImageBlockDetails(ImageBlock block, PrintWriter writer) {
        if (block.getUrl() != null) {
            writer.println("* **URL Requirements:**");
            if (block.getUrl().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getUrl().getPattern() != null) {
                writer.println(PATTERN_PREFIX + patternHumanizer.humanize(block.getUrl().getPattern().pattern()));
            }
        }

        if (block.getWidth() != null) {
            writer.println("* **Width:**");
            if (block.getWidth().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getWidth().getMinValue() != null) {
                writer.println(MINIMUM_PREFIX + block.getWidth().getMinValue() + "px");
            }
            if (block.getWidth().getMaxValue() != null) {
                writer.println(MAXIMUM_PREFIX + block.getWidth().getMaxValue() + "px");
            }
        }

        if (block.getHeight() != null) {
            writer.println("* **Height:**");
            if (block.getHeight().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getHeight().getMinValue() != null) {
                writer.println(MINIMUM_PREFIX + block.getHeight().getMinValue() + "px");
            }
            if (block.getHeight().getMaxValue() != null) {
                writer.println(MAXIMUM_PREFIX + block.getHeight().getMaxValue() + "px");
            }
        }

        if (block.getAlt() != null) {
            writer.println("* **Alt Text:**");
            if (block.getAlt().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getAlt().getMinLength() != null) {
                writer.println(MINIMUM_LENGTH_PREFIX + block.getAlt().getMinLength() + CHARACTERS_SUFFIX);
            }
            if (block.getAlt().getMaxLength() != null) {
                writer.println(MAXIMUM_LENGTH_PREFIX + block.getAlt().getMaxLength() + CHARACTERS_SUFFIX);
            }
        }
    }

    private void generateListingBlockDetails(ListingBlock block, PrintWriter writer) {
        if (block.getLanguage() != null) {
            writer.println("* **Language:**");
            if (block.getLanguage().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getLanguage().getAllowed() != null && !block.getLanguage().getAllowed().isEmpty()) {
                writer.println("  - Allowed: " + String.join(", ", block.getLanguage().getAllowed()));
            }
        }

        if (block.getLines() != null) {
            writer.println("* **Lines:**");
            if (block.getLines().min() != null) {
                writer.println(MINIMUM_PREFIX + block.getLines().min());
            }
            if (block.getLines().max() != null) {
                writer.println(MAXIMUM_PREFIX + block.getLines().max());
            }
        }

        if (block.getTitle() != null) {
            writer.println(TITLE_HEADER);
            if (block.getTitle().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getTitle().getPattern() != null) {
                writer.println(PATTERN_PREFIX + patternHumanizer.humanize(block.getTitle().getPattern().pattern()));
            }
        }

        if (block.getCallouts() != null) {
            writer.println("* **Callouts:**");
            if (block.getCallouts().getMax() != null) {
                writer.println(MAXIMUM_PREFIX + block.getCallouts().getMax());
            }
        }
    }

    private void generateParagraphBlockDetails(ParagraphBlock block, PrintWriter writer) {
        if (block.getLines() != null) {
            writer.println("* **Lines:**");
            if (block.getLines().min() != null) {
                writer.println(MINIMUM_PREFIX + block.getLines().min());
            }
            if (block.getLines().max() != null) {
                writer.println(MAXIMUM_PREFIX + block.getLines().max());
            }
        }

        if (block.getSentence() != null) {
            if (block.getSentence().getOccurrence() != null) {
                writer.println("* **Sentences:**");
                writer.println(MINIMUM_PREFIX + block.getSentence().getOccurrence().min());
                if (block.getSentence().getOccurrence().max() < Integer.MAX_VALUE) {
                    writer.println(MAXIMUM_PREFIX + block.getSentence().getOccurrence().max());
                }
            }

            if (block.getSentence().getWords() != null) {
                writer.println("* **Words per sentence:**");
                if (block.getSentence().getWords().getMin() != null) {
                    writer.println(MINIMUM_PREFIX + block.getSentence().getWords().getMin());
                }
                if (block.getSentence().getWords().getMax() != null) {
                    writer.println(MAXIMUM_PREFIX + block.getSentence().getWords().getMax());
                }
            }
        }
    }

    private void generateAudioBlockDetails(AudioBlock block, PrintWriter writer) {
        if (block.getUrl() != null) {
            writer.println("* **URL:**");
            if (block.getUrl().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getUrl().getPattern() != null) {
                writer.println(PATTERN_PREFIX + patternHumanizer.humanize(block.getUrl().getPattern().pattern()));
            }
        }

        if (block.getOptions() != null) {
            writer.println("* **Options:**");
            if (block.getOptions().getAutoplay() != null && !block.getOptions().getAutoplay().isAllowed()) {
                writer.println("  - Autoplay: Not allowed");
            }
            if (block.getOptions().getControls() != null && block.getOptions().getControls().isRequired()) {
                writer.println("  - Controls: Required");
            }
            if (block.getOptions().getLoop() != null && !block.getOptions().getLoop().isAllowed()) {
                writer.println("  - Loop: Not allowed");
            }
        }

        if (block.getTitle() != null) {
            writer.println(TITLE_HEADER);
            if (block.getTitle().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getTitle().getMinLength() != null) {
                writer.println(MINIMUM_LENGTH_PREFIX + block.getTitle().getMinLength() + CHARACTERS_SUFFIX);
            }
            if (block.getTitle().getMaxLength() != null) {
                writer.println(MAXIMUM_LENGTH_PREFIX + block.getTitle().getMaxLength() + CHARACTERS_SUFFIX);
            }
        }
    }

    private void generateVideoBlockDetails(VideoBlock block, PrintWriter writer) {
        // VideoBlock has different structure - needs proper implementation based on
        // actual methods
        // For now, just show basic info
        if (block.getUrl() != null) {
            writer.println("* **URL configuration present**");
        }
        if (block.getWidth() != null) {
            writer.println("* **Width configuration present**");
        }
        if (block.getHeight() != null) {
            writer.println("* **Height configuration present**");
        }
        if (block.getOptions() != null) {
            writer.println("* **Options configuration present**");
        }
    }

    private void generateTableBlockDetails(TableBlock block, PrintWriter writer) {
        if (block.getColumns() != null) {
            writer.println("* **Columns:**");
            if (block.getColumns().getMin() != null) {
                writer.println(MINIMUM_PREFIX + block.getColumns().getMin());
            }
            if (block.getColumns().getMax() != null) {
                writer.println(MAXIMUM_PREFIX + block.getColumns().getMax());
            }
        }

        if (block.getRows() != null) {
            writer.println("* **Rows:**");
            if (block.getRows().getMin() != null) {
                writer.println(MINIMUM_PREFIX + block.getRows().getMin());
            }
            if (block.getRows().getMax() != null) {
                writer.println(MAXIMUM_PREFIX + block.getRows().getMax());
            }
        }

        if (block.getHeader() != null) {
            writer.println("* **Header:**");
            if (block.getHeader().isRequired()) {
                writer.println(REQUIRED_YES);
            }
        }

        if (block.getCaption() != null) {
            writer.println("* **Caption:**");
            if (block.getCaption().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getCaption().getPattern() != null) {
                writer.println(PATTERN_PREFIX + patternHumanizer.humanize(block.getCaption().getPattern().pattern()));
            }
        }
    }

    private void generateAdmonitionBlockDetails(AdmonitionBlock block, PrintWriter writer) {
        if (block.getTitle() != null) {
            writer.println(TITLE_HEADER);
            if (block.getTitle().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getTitle().getMinLength() != null) {
                writer.println(MINIMUM_LENGTH_PREFIX + block.getTitle().getMinLength() + CHARACTERS_SUFFIX);
            }
            if (block.getTitle().getMaxLength() != null) {
                writer.println(MAXIMUM_LENGTH_PREFIX + block.getTitle().getMaxLength() + CHARACTERS_SUFFIX);
            }
        }
        // AdmonitionBlock might have other specific attributes
    }

    @SuppressWarnings(PMD_UNUSED_PARAM)
    private void generateExampleBlockDetails(ExampleBlock block, PrintWriter writer) {
        // ExampleBlock typically doesn't have many specific validation rules
        // It's mainly a container for example content
    }

    private void generateQuoteBlockDetails(QuoteBlock block, PrintWriter writer) {
        if (block.getAttribution() != null) {
            writer.println("* **Attribution:**");
            if (block.getAttribution().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getAttribution().getPattern() != null) {
                writer
                        .println(PATTERN_PREFIX
                                + patternHumanizer.humanize(block.getAttribution().getPattern().pattern()));
            }
        }

        if (block.getCitation() != null) {
            writer.println("* **Citation:**");
            if (block.getCitation().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getCitation().getPattern() != null) {
                writer.println(PATTERN_PREFIX + patternHumanizer.humanize(block.getCitation().getPattern().pattern()));
            }
        }
    }

    private void generateSidebarBlockDetails(SidebarBlock block, PrintWriter writer) {
        if (block.getTitle() != null) {
            writer.println(TITLE_HEADER);
            if (block.getTitle().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getTitle().getMinLength() != null) {
                writer.println(MINIMUM_LENGTH_PREFIX + block.getTitle().getMinLength() + CHARACTERS_SUFFIX);
            }
            if (block.getTitle().getMaxLength() != null) {
                writer.println(MAXIMUM_LENGTH_PREFIX + block.getTitle().getMaxLength() + CHARACTERS_SUFFIX);
            }
        }
    }

    private void generateUlistBlockDetails(UlistBlock block, PrintWriter writer) {
        if (block.getItems() != null) {
            writer.println("* **Items:**");
            if (block.getItems().getMin() != null) {
                writer.println(MINIMUM_PREFIX + block.getItems().getMin());
            }
            if (block.getItems().getMax() != null) {
                writer.println(MAXIMUM_PREFIX + block.getItems().getMax());
            }
        }
        // UlistBlock might have other attributes like nesting depth
    }

    @SuppressWarnings(PMD_UNUSED_PARAM)
    private void generateDlistBlockDetails(DlistBlock block, PrintWriter writer) {
        // DlistBlock specific attributes would go here
        // Currently DlistBlock doesn't expose specific validation methods
    }

    private void generateLiteralBlockDetails(LiteralBlock block, PrintWriter writer) {
        if (block.getLines() != null) {
            writer.println("* **Lines:**");
            if (block.getLines().getMin() != null) {
                writer.println(MINIMUM_PREFIX + block.getLines().getMin());
            }
            if (block.getLines().getMax() != null) {
                writer.println(MAXIMUM_PREFIX + block.getLines().getMax());
            }
        }
    }

    @SuppressWarnings(PMD_UNUSED_PARAM)
    private void generatePassBlockDetails(PassBlock block, PrintWriter writer) {
        // PassBlock typically doesn't have specific validation rules
        // It passes content through without processing
    }

    private void generateVerseBlockDetails(VerseBlock block, PrintWriter writer) {
        if (block.getAttribution() != null) {
            writer.println("* **Attribution:**");
            if (block.getAttribution().isRequired()) {
                writer.println(REQUIRED_YES);
            }
            if (block.getAttribution().getPattern() != null) {
                writer
                        .println(PATTERN_PREFIX
                                + patternHumanizer.humanize(block.getAttribution().getPattern().pattern()));
            }
        }
        // VerseBlock might have other attributes like citation
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
