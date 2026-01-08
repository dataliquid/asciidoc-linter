package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Interface for generating documentation for specific block types.
 */
public interface BlockDocGenerator {

    /**
     * Generates documentation for the given block.
     *
     * @param block            the block to document
     * @param writer           the writer to write to
     * @param patternHumanizer the pattern humanizer for converting regex patterns
     */
    void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer);

    /**
     * Returns the block class this generator handles.
     */
    Class<? extends Block> getBlockClass();
}
