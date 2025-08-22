package com.dataliquid.asciidoc.linter.documentation.visualizers;

import java.io.PrintWriter;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.document.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.documentation.HierarchyVisualizer;
import com.dataliquid.asciidoc.linter.documentation.VisualizationStyle;

/**
 * Visualizes rule hierarchy as an ASCII-art tree structure.
 */
public class TreeVisualizer implements HierarchyVisualizer {

    private static final String VERTICAL = "│   ";
    private static final String BRANCH = "├── ";
    private static final String LAST_BRANCH = "└── ";
    private static final String EMPTY = "    ";

    @Override
    public void visualize(LinterConfiguration config, PrintWriter writer) {
        writer.println("[literal]");
        writer.println("....");
        visualizeDocument(config.document(), "", writer);
        writer.println("....");
    }

    @Override
    public VisualizationStyle getStyle() {
        return VisualizationStyle.TREE;
    }

    private void visualizeDocument(DocumentConfiguration doc, String prefix, PrintWriter writer) {
        writer.println(prefix + "document/");

        boolean hasMetadata = doc.metadata() != null;
        boolean hasSections = doc.sections() != null && !doc.sections().isEmpty();

        if (hasMetadata) {
            String childPrefix = prefix + (hasSections ? VERTICAL : EMPTY);
            visualizeMetadata(doc.metadata(), prefix + BRANCH, childPrefix, writer);
        }

        if (hasSections) {
            visualizeSections(doc.sections(), prefix + LAST_BRANCH, prefix + EMPTY, writer);
        }
    }

    private void visualizeMetadata(MetadataConfiguration metadata, String nodePrefix, String childPrefix,
            PrintWriter writer) {
        writer.println(nodePrefix + "metadata/");

        if (metadata.attributes() != null && !metadata.attributes().isEmpty()) {
            List<AttributeConfig> attrs = metadata.attributes();
            for (int i = 0; i < attrs.size(); i++) {
                AttributeConfig attr = attrs.get(i);
                boolean isLast = i == attrs.size() - 1;
                String branch = isLast ? LAST_BRANCH : BRANCH;

                String attrLine = childPrefix + branch + attr.name();
                if (attr.required()) {
                    attrLine += " (required)";
                } else {
                    attrLine += " (optional)";
                }
                attrLine += " [" + attr.severity() + "]";

                writer.println(attrLine);
            }
        }
    }

    private void visualizeSections(List<SectionConfig> sections, String nodePrefix, String childPrefix,
            PrintWriter writer) {
        writer.println(nodePrefix + "sections/");

        for (int i = 0; i < sections.size(); i++) {
            SectionConfig section = sections.get(i);
            boolean isLast = i == sections.size() - 1;
            String branch = isLast ? LAST_BRANCH : BRANCH;
            String nextChildPrefix = childPrefix + (isLast ? EMPTY : VERTICAL);

            visualizeSection(section, childPrefix + branch, nextChildPrefix, writer);
        }
    }

    private void visualizeSection(SectionConfig section, String nodePrefix, String childPrefix, PrintWriter writer) {
        String sectionLine = nodePrefix + section.name() + "/";
        sectionLine += " [Level " + section.level();
        if (section.order() != null) {
            sectionLine += ", Order " + section.order();
        }
        sectionLine += "]";
        writer.println(sectionLine);

        boolean hasBlocks = section.allowedBlocks() != null && !section.allowedBlocks().isEmpty();
        boolean hasSubsections = section.subsections() != null && !section.subsections().isEmpty();

        if (hasBlocks) {
            String blocksPrefix = hasSubsections ? BRANCH : LAST_BRANCH;
            String nextChildPrefix = childPrefix + (hasSubsections ? VERTICAL : EMPTY);
            visualizeBlocks(section.allowedBlocks(), childPrefix + blocksPrefix, nextChildPrefix, writer);
        }

        if (hasSubsections) {
            visualizeSections(section.subsections(), childPrefix + LAST_BRANCH, childPrefix + EMPTY, writer);
        }
    }

    private void visualizeBlocks(List<Block> blocks, String nodePrefix, String childPrefix, PrintWriter writer) {
        writer.println(nodePrefix + "allowedBlocks/");

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            boolean isLast = i == blocks.size() - 1;
            String branch = isLast ? LAST_BRANCH : BRANCH;
            String nextChildPrefix = childPrefix + (isLast ? EMPTY : VERTICAL);

            visualizeBlock(block, childPrefix + branch, nextChildPrefix, writer);
        }
    }

    private void visualizeBlock(Block block, String nodePrefix, String childPrefix, PrintWriter writer) {
        String blockLine = nodePrefix + block.getType().toValue() + "/";
        if (block.getName() != null) {
            blockLine += " (" + block.getName() + ")";
        }
        blockLine += " [" + block.getSeverity() + "]";
        writer.println(blockLine);

        // Add block-specific details
        visualizeBlockDetails(block, childPrefix, writer);
    }

    private void visualizeBlockDetails(Block block, String prefix, PrintWriter writer) {
        // TODO: Add specific details based on block type
        // For example, for AudioBlock show url, options, title requirements
        if (block.getOccurrence() != null) {
            String occurrenceLine = prefix + LAST_BRANCH + "occurrence: ";
            Integer min = block.getOccurrence().min();
            Integer max = block.getOccurrence().max();
            if (min != null) {
                occurrenceLine += "min " + min;
            }
            if (max != null) {
                if (min != null) {
                    occurrenceLine += ", ";
                }
                occurrenceLine += "max " + max;
            }
            if (block.getOccurrence().severity() != null) {
                occurrenceLine += " [" + block.getOccurrence().severity() + "]";
            }
            writer.println(occurrenceLine);
        }
    }
}
