package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Registry for block documentation generators.
 */
public class BlockDocGeneratorRegistry {

    @SuppressWarnings("PMD.UseConcurrentHashMap") // Immutable after construction
    private final Map<Class<? extends Block>, BlockDocGenerator> generators = new HashMap<>();

    public void register(BlockDocGenerator generator) {
        generators.put(generator.getBlockClass(), generator);
    }

    public void generateBlockDetails(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        BlockDocGenerator generator = generators.get(block.getClass());
        if (generator != null) {
            generator.generate(block, writer, patternHumanizer);
        }
    }
}
