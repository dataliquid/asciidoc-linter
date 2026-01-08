package com.dataliquid.asciidoc.linter.config.loader;

import java.util.ArrayList;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.AdmonitionBlock;
import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.DlistBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for Block lists in YAML. Handles the special YAML
 * structure where block type is the key:
 *
 * <pre>
 * allowedBlocks:
 *   - paragraph:
 *       name: intro-paragraph
 *       severity: warn
 *       occurrence:
 *         min: 1
 *         max: 5
 *   - listing:
 *       name: code-example
 *       severity: error
 * </pre>
 *
 * This deserializer expects valid YAML that conforms to the schema. No
 * transformations or default values are applied.
 */
@SuppressWarnings("rawtypes")
public class BlockListDeserializer extends StdDeserializer<List> {

    public BlockListDeserializer() {
        super(List.class);
    }

    @Override
    public List<Block> deserialize(JsonParser p, DeserializationContext ctxt) {
        List<Block> blocks = new ArrayList<>();
        JsonNode node = p.readValueAsTree();

        if (!node.isArray()) {
            throw new IllegalArgumentException("Expected array for block list");
        }

        for (JsonNode blockNode : node) {
            if (!blockNode.isObject()) {
                continue;
            }

            // Each block is an object with a single key (the block type)
            String blockType = blockNode.propertyNames().iterator().next();
            JsonNode blockData = blockNode.get(blockType);

            // Convert blockType string to BlockType enum
            BlockType type = BlockType.fromValue(blockType);

            // Deserialize based on block type - Jackson will handle all validation
            Block block = switch (type) {
            case PARAGRAPH -> ctxt.readTreeAsValue(blockData, ParagraphBlock.class);
            case LISTING -> ctxt.readTreeAsValue(blockData, ListingBlock.class);
            case TABLE -> ctxt.readTreeAsValue(blockData, TableBlock.class);
            case IMAGE -> ctxt.readTreeAsValue(blockData, ImageBlock.class);
            case VERSE -> ctxt.readTreeAsValue(blockData, VerseBlock.class);
            case ADMONITION -> ctxt.readTreeAsValue(blockData, AdmonitionBlock.class);
            case PASS -> ctxt.readTreeAsValue(blockData, PassBlock.class);
            case LITERAL -> ctxt.readTreeAsValue(blockData, LiteralBlock.class);
            case AUDIO -> ctxt.readTreeAsValue(blockData, AudioBlock.class);
            case QUOTE -> ctxt.readTreeAsValue(blockData, QuoteBlock.class);
            case SIDEBAR -> ctxt.readTreeAsValue(blockData, SidebarBlock.class);
            case EXAMPLE -> ctxt.readTreeAsValue(blockData, ExampleBlock.class);
            case VIDEO -> ctxt.readTreeAsValue(blockData, VideoBlock.class);
            case ULIST -> ctxt.readTreeAsValue(blockData, UlistBlock.class);
            case DLIST -> ctxt.readTreeAsValue(blockData, DlistBlock.class);
            };

            blocks.add(block);
        }

        return blocks;
    }
}
