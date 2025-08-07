package com.dataliquid.asciidoc.linter.config.loader;

import java.io.IOException;
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom deserializer for Block lists in YAML.
 * Handles the special YAML structure where block type is the key:
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
 * This deserializer expects valid YAML that conforms to the schema.
 * No transformations or default values are applied.
 */
public class BlockListDeserializer extends JsonDeserializer<List<Block>> {
    
    @Override
    public List<Block> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<Block> blocks = new ArrayList<>();
        JsonNode node = p.getCodec().readTree(p);
        
        if (!node.isArray()) {
            throw new IOException("Expected array for block list");
        }
        
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        
        for (JsonNode blockNode : node) {
            if (!blockNode.isObject()) {
                continue;
            }
            
            // Each block is an object with a single key (the block type)
            String blockType = blockNode.fieldNames().next();
            JsonNode blockData = blockNode.get(blockType);
            
            // Convert blockType string to BlockType enum
            BlockType type = BlockType.fromValue(blockType);
            
            // Deserialize based on block type - Jackson will handle all validation
            Block block = switch (type) {
                case PARAGRAPH -> mapper.treeToValue(blockData, ParagraphBlock.class);
                case LISTING -> mapper.treeToValue(blockData, ListingBlock.class);
                case TABLE -> mapper.treeToValue(blockData, TableBlock.class);
                case IMAGE -> mapper.treeToValue(blockData, ImageBlock.class);
                case VERSE -> mapper.treeToValue(blockData, VerseBlock.class);
                case ADMONITION -> mapper.treeToValue(blockData, AdmonitionBlock.class);
                case PASS -> mapper.treeToValue(blockData, PassBlock.class);
                case LITERAL -> mapper.treeToValue(blockData, LiteralBlock.class);
                case AUDIO -> mapper.treeToValue(blockData, AudioBlock.class);
                case QUOTE -> mapper.treeToValue(blockData, QuoteBlock.class);
                case SIDEBAR -> mapper.treeToValue(blockData, SidebarBlock.class);
                case EXAMPLE -> mapper.treeToValue(blockData, ExampleBlock.class);
                case VIDEO -> mapper.treeToValue(blockData, VideoBlock.class);
                case ULIST -> mapper.treeToValue(blockData, UlistBlock.class);
                case DLIST -> mapper.treeToValue(blockData, DlistBlock.class);
            };
            
            blocks.add(block);
        }
        
        return blocks;
    }
}