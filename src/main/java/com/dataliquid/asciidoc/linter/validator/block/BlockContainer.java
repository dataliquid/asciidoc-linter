package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

/**
 * Wrapper for block containers (Document or Section) to provide unified access
 * to blocks.
 */
public final class BlockContainer {
    private final StructuralNode node;
    private final String containerType;

    private BlockContainer(StructuralNode node, String containerType) {
        this.node = Objects.requireNonNull(node, "Node must not be null");
        this.containerType = Objects.requireNonNull(containerType, "Container type must not be null");
    }

    /**
     * Creates a BlockContainer from a Document.
     */
    public static BlockContainer fromDocument(Document document) {
        return new BlockContainer(document, "document");
    }

    /**
     * Creates a BlockContainer from a Section.
     */
    public static BlockContainer fromSection(Section section) {
        return new BlockContainer(section, "section");
    }

    /**
     * Gets all blocks from the container, handling preamble expansion for
     * documents.
     */
    public List<StructuralNode> getBlocks() {
        if (node instanceof Document) {
            return getDocumentBlocks((Document) node);
        } else if (node instanceof Section) {
            // For sections, filter out subsections as they are handled by SectionValidator
            List<StructuralNode> blocks = new ArrayList<>();
            List<StructuralNode> nodeBlocks = node.getBlocks();
            if (nodeBlocks != null) {
                for (StructuralNode child : nodeBlocks) {
                    if (!(child instanceof Section)) {
                        blocks.add(child);
                    }
                }
            }
            return blocks;
        } else {
            List<StructuralNode> nodeBlocks = node.getBlocks();
            return nodeBlocks != null ? nodeBlocks : new ArrayList<>();
        }
    }

    /**
     * Gets blocks from a document, expanding preamble if present. Only returns
     * blocks at the document level, not blocks within sections.
     */
    private List<StructuralNode> getDocumentBlocks(Document document) {
        List<StructuralNode> blocks = new ArrayList<>();
        List<StructuralNode> docBlocks = document.getBlocks();

        if (docBlocks == null) {
            return blocks;
        }

        // First, check if there's a preamble - it contains the document-level blocks
        for (StructuralNode child : docBlocks) {
            if (child != null && child.getContext() != null && child.getContext().equals("preamble")) {
                // The preamble contains all document-level blocks
                List<StructuralNode> preambleBlocks = child.getBlocks();
                if (preambleBlocks != null) {
                    blocks.addAll(preambleBlocks);
                }
                // Once we found the preamble, we're done - no other document-level blocks exist
                return blocks;
            }
        }

        // If no preamble, look for blocks before the first section
        for (StructuralNode child : docBlocks) {
            if (child instanceof Section) {
                // Stop when we hit the first section
                break;
            }
            if (child != null) {
                blocks.add(child);
            }
        }

        return blocks;
    }

    /**
     * Gets the wrapped node.
     */
    public StructuralNode getNode() {
        return node;
    }

    /**
     * Gets the container type (document or section).
     */
    public String getContainerType() {
        return containerType;
    }

    /**
     * Checks if this is a document container.
     */
    public boolean isDocument() {
        return node instanceof Document;
    }

    /**
     * Checks if this is a section container.
     */
    public boolean isSection() {
        return node instanceof Section;
    }
}
