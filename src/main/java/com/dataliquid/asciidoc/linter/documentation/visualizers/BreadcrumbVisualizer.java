package com.dataliquid.asciidoc.linter.documentation.visualizers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.documentation.HierarchyVisualizer;
import com.dataliquid.asciidoc.linter.documentation.VisualizationStyle;

/**
 * Visualizes rule hierarchy as breadcrumb paths.
 */
public class BreadcrumbVisualizer implements HierarchyVisualizer {

    private static class RulePath {
        final String path;
        final Severity severity;
        final String description;

        RulePath(String path, Severity severity, String description) {
            this.path = path;
            this.severity = severity;
            this.description = description;
        }
    }

    @Override
    public void visualize(LinterConfiguration config, PrintWriter writer) {
        List<RulePath> paths = collectAllPaths(config.document());

        writer.println("[%header,cols=\"3,1,2\"]");
        writer.println("|===");
        writer.println("|Rule Path |Severity |Description");
        writer.println();

        for (RulePath path : paths) {
            writer.println("|`" + path.path + "`");
            writer.println("|" + path.severity);
            writer.println("|" + path.description);
            writer.println();
        }

        writer.println("|===");
    }

    @Override
    public VisualizationStyle getStyle() {
        return VisualizationStyle.BREADCRUMB;
    }

    private List<RulePath> collectAllPaths(DocumentConfiguration doc) {
        List<RulePath> paths = new ArrayList<>();

        // Add document root
        paths.add(new RulePath("document", null, "Document root"));

        // Collect metadata paths
        if (doc.metadata() != null) {
            collectMetadataPaths(doc.metadata(), "document", paths);
        }

        // Collect section paths
        if (doc.sections() != null) {
            paths.add(new RulePath("document > sections", Severity.ERROR, "Sections (at least one required)"));

            for (SectionConfig section : doc.sections()) {
                collectSectionPaths(section, "document > sections", paths);
            }
        }

        return paths;
    }

    private void collectMetadataPaths(MetadataConfiguration metadata, String parentPath, List<RulePath> paths) {
        String metadataPath = parentPath + " > metadata";
        paths.add(new RulePath(metadataPath, Severity.ERROR, "Metadata container (required)"));

        if (metadata.attributes() != null) {
            for (AttributeConfig attr : metadata.attributes()) {
                String attrPath = metadataPath + " > " + attr.name();
                String description = getAttributeDescription(attr) + " (" + (attr.required() ? "Required" : "Optional")
                        + ")";
                paths.add(new RulePath(attrPath, attr.severity(), description));
            }
        }
    }

    private void collectSectionPaths(SectionConfig section, String parentPath, List<RulePath> paths) {
        String sectionPath = parentPath + " > " + section.name();
        String description = getSectionDescription(section) + " (Level " + section.level() + ")";
        Severity severity = (section.occurrence() != null && section.occurrence().min() > 0) ? Severity.ERROR
                : Severity.INFO;

        paths.add(new RulePath(sectionPath, severity, description));

        // Collect allowed blocks
        if (section.allowedBlocks() != null && !section.allowedBlocks().isEmpty()) {
            String blocksPath = sectionPath + " > allowedBlocks";
            paths.add(new RulePath(blocksPath, null, "Allowed block types"));

            for (Block block : section.allowedBlocks()) {
                collectBlockPaths(block, blocksPath, paths);
            }
        }

        // Collect subsections
        if (section.subsections() != null && !section.subsections().isEmpty()) {
            String subsectionsPath = sectionPath + " > subsections";
            paths.add(new RulePath(subsectionsPath, null, "Subsections"));

            for (SectionConfig subsection : section.subsections()) {
                collectSectionPaths(subsection, subsectionsPath, paths);
            }
        }
    }

    private void collectBlockPaths(Block block, String parentPath, List<RulePath> paths) {
        String blockPath = parentPath + " > " + block.getType().toValue();
        String description = getBlockDescription(block);

        paths.add(new RulePath(blockPath, block.getSeverity(), description));

        // Add block-specific rule paths
        collectBlockRulePaths(block, blockPath, paths);
    }

    private void collectBlockRulePaths(Block block, String blockPath, List<RulePath> paths) {
        // TODO: Add specific rule paths based on block type
        // For example, for AudioBlock:
        // - blockPath > url
        // - blockPath > options > autoplay
        // - blockPath > options > controls
        // - blockPath > title

        if (block.getOccurrence() != null) {
            String occurrencePath = blockPath + " > occurrence";
            String description = "Occurrence constraints";
            Severity severity = block.getOccurrence().severity() != null ? block.getOccurrence().severity()
                    : block.getSeverity();
            paths.add(new RulePath(occurrencePath, severity, description));
        }
    }

    private String getAttributeDescription(AttributeConfig attr) {
        return switch (attr.name()) {
        case "title" -> "Document title";
        case "author" -> "Author";
        case "version" -> "Version number";
        case "email" -> "Contact email";
        default -> attr.name();
        };
    }

    private String getSectionDescription(SectionConfig section) {
        return switch (section.name()) {
        case "introduction" -> "Introduction section";
        case "implementation" -> "Implementation details";
        case "conclusion" -> "Summary";
        default -> section.name();
        };
    }

    private String getBlockDescription(Block block) {
        String desc = switch (block.getType()) {
        case PARAGRAPH -> "Text paragraphs";
        case LISTING -> "Code blocks";
        case TABLE -> "Tables";
        case IMAGE -> "Images";
        case VERSE -> "Quotes/Verses";
        case ADMONITION -> "Notes/Warnings";
        case PASS -> "Pass-through blocks";
        case LITERAL -> "Literal blocks";
        default -> "Block type " + block.getType().toValue();
        };

        if (block.getName() != null) {
            desc += " (" + block.getName() + ")";
        }

        return desc;
    }
}
