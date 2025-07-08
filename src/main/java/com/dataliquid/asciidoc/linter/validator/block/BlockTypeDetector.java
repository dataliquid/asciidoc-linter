package com.dataliquid.asciidoc.linter.validator.block;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;

/**
 * Detects the type of AsciidoctorJ blocks and maps them to our BlockType enum.
 */
public final class BlockTypeDetector {
    
    /**
     * Detects the block type from an AsciidoctorJ StructuralNode.
     * 
     * @param node the node to analyze
     * @return the detected block type, or null if type cannot be determined
     */
    public BlockType detectType(StructuralNode node) {
        if (node == null) {
            return null;
        }
        
        try {
            // Check node context
            String context = node.getContext();
            if (context == null) {
                return null;
            }
        
        // Map AsciidoctorJ contexts to our BlockTypes
        switch (context) {
            case "paragraph":
                return BlockType.PARAGRAPH;
                
            case "listing":
                return BlockType.LISTING;
                
            case "literal":
                return BlockType.LITERAL;
                
            case "table":
                return BlockType.TABLE;
                
            case "image":
                return BlockType.IMAGE;
                
            case "verse":
            case "quote":
                return detectVerseOrQuote(node);
                
            case "admonition":
                return BlockType.ADMONITION;
                
            case "pass":
                return BlockType.PASS;
                
            case "sidebar":
                return BlockType.SIDEBAR;
                
            case "audio":
                return BlockType.AUDIO;
                
            case "example":
            case "open":
                // These could contain other blocks, check content
                return detectFromContent(node);
                
            case "preamble":
                // Preamble is a container, not a block itself
                return null;
                
            case "ulist":
                return BlockType.ULIST;
                
            default:
                return null;
        }
        } catch (Exception e) {
            // Handle any exceptions gracefully
            return null;
        }
    }
    
    /**
     * Determines if a quote/verse block is actually a verse block or quote block.
     */
    private BlockType detectVerseOrQuote(StructuralNode node) {
        // Check if it has verse style explicitly
        if ("verse".equals(node.getStyle())) {
            return BlockType.VERSE;
        }
        
        // If context is "verse", it's definitely a verse block
        if ("verse".equals(node.getContext())) {
            return BlockType.VERSE;
        }
        
        // If context is "quote", it's a quote block
        if ("quote".equals(node.getContext())) {
            return BlockType.QUOTE;
        }
        
        // Default: if it has attribution or citetitle, treat as quote
        if (node.getAttribute("attribution") != null || 
            node.getAttribute("citetitle") != null ||
            node.getAttribute("author") != null ||
            node.getAttribute("source") != null) {
            return BlockType.QUOTE;
        }
        
        // Could be a quote block containing other content
        return null;
    }
    
    /**
     * Attempts to detect block type from content for container blocks.
     */
    private BlockType detectFromContent(StructuralNode node) {
        // For container blocks, we might need to look at style or role
        String style = node.getStyle();
        if (style != null) {
            switch (style) {
                case "source":
                case "listing":
                    return BlockType.LISTING;
                case "verse":
                    return BlockType.VERSE;
            }
        }
        
        // Check if it's an image block by role
        if (node.hasRole("image") || "image".equals(node.getNodeName())) {
            return BlockType.IMAGE;
        }
        
        // Default to null for unknown container blocks
        return null;
    }
    
    /**
     * Checks if a node is a specific block type.
     */
    public boolean isBlockType(StructuralNode node, BlockType type) {
        return type == detectType(node);
    }
}