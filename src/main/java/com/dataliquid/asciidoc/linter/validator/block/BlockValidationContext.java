package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;

/**
 * Context for block validation containing section or document information and tracking data.
 */
public final class BlockValidationContext {
    private final StructuralNode container; // Can be Section or Document
    private final String filename;
    private final Map<String, List<BlockOccurrence>> occurrences;
    private final List<BlockPosition> blockOrder;

    /**
     * Constructor for section validation.
     */
    public BlockValidationContext(Section section, String filename) {
        this.container = Objects.requireNonNull(section, "[" + getClass().getName() + "] section must not be null");
        this.filename = Objects.requireNonNull(filename, "[" + getClass().getName() + "] filename must not be null");
        this.occurrences = new HashMap<>();
        this.blockOrder = new ArrayList<>();
    }

    /**
     * Constructor for document validation.
     */
    public BlockValidationContext(Document document, String filename) {
        this.container = Objects.requireNonNull(document, "[" + getClass().getName() + "] document must not be null");
        this.filename = Objects.requireNonNull(filename, "[" + getClass().getName() + "] filename must not be null");
        this.occurrences = new HashMap<>();
        this.blockOrder = new ArrayList<>();
    }

    public Section getSection() {
        return container instanceof Section ? (Section) container : null;
    }

    public StructuralNode getContainer() {
        return container;
    }

    public String getFilename() {
        return filename;
    }

    /**
     * Creates a source location for the given block.
     */
    public SourceLocation createLocation(StructuralNode block) {
        int line = 1;
        if (block.getSourceLocation() != null) {
            line = block.getSourceLocation().getLineNumber();
        }

        return SourceLocation.builder().filename(filename).startLine(line).build();
    }

    /**
     * Creates a source location for the given block with column information.
     */
    public SourceLocation createLocation(StructuralNode block, int startColumn, int endColumn) {
        int line = 1;
        if (block.getSourceLocation() != null) {
            line = block.getSourceLocation().getLineNumber();
        }

        return SourceLocation.builder().filename(filename).startLine(line).endLine(line).startColumn(startColumn)
                .endColumn(endColumn).build();
    }

    /**
     * Tracks a block occurrence for validation.
     */
    public void trackBlock(Block config, StructuralNode block) {
        String key = createOccurrenceKey(config);

        BlockOccurrence occurrence = new BlockOccurrence(config, block, blockOrder.size());
        occurrences.computeIfAbsent(key, k -> new ArrayList<>()).add(occurrence);

        blockOrder.add(new BlockPosition(config, block, blockOrder.size()));
    }

    /**
     * Gets all occurrences for a specific block configuration.
     */
    public List<BlockOccurrence> getOccurrences(Block config) {
        String key = createOccurrenceKey(config);
        return occurrences.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Gets the count of occurrences for a specific block configuration.
     */
    public int getOccurrenceCount(Block config) {
        return getOccurrences(config).size();
    }

    /**
     * Gets all tracked blocks in order.
     */
    public List<BlockPosition> getBlockOrder() {
        return new ArrayList<>(blockOrder);
    }

    /**
     * Gets a human-readable name for the block.
     */
    public String getBlockName(Block config) {
        if (config.getName() != null) {
            return "block '" + config.getName() + "'";
        }
        return config.getType().toString().toLowerCase() + " block";
    }

    private String createOccurrenceKey(Block config) {
        if (config.getName() != null) {
            return config.getType() + ":" + config.getName();
        }
        return config.getType().toString();
    }

    /**
     * Represents a block occurrence with its configuration and position.
     */
    public static final class BlockOccurrence {
        private final Block config;
        private final StructuralNode block;
        private final int position;

        BlockOccurrence(Block config, StructuralNode block, int position) {
            this.config = config;
            this.block = block;
            this.position = position;
        }

        public Block getConfig() {
            return config;
        }

        public StructuralNode getBlock() {
            return block;
        }

        public int getPosition() {
            return position;
        }
    }

    /**
     * Represents a block's position in the document.
     */
    public static final class BlockPosition {
        private final Block config;
        private final StructuralNode block;
        private final int index;

        BlockPosition(Block config, StructuralNode block, int index) {
            this.config = config;
            this.block = block;
            this.index = index;
        }

        public Block getConfig() {
            return config;
        }

        public StructuralNode getBlock() {
            return block;
        }

        public int getIndex() {
            return index;
        }
    }
}
