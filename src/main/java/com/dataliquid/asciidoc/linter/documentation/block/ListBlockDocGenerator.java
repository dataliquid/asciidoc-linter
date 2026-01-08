package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.DlistBlock;
import com.dataliquid.asciidoc.linter.config.blocks.UlistBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Generates documentation for list blocks (ulist, dlist).
 */
public class ListBlockDocGenerator {

    private ListBlockDocGenerator() {
        // Static utility
    }

    public static void generateUlist(UlistBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getItems() != null) {
            writer.println("* **Items:**");
            CommonBlockDocHelper.writeMinimum(writer, block.getItems().getMin(), null);
            CommonBlockDocHelper.writeMaximum(writer, block.getItems().getMax(), null);
        }

        if (block.getMarkerStyle() != null) {
            writer.println("* **Marker Style:** " + block.getMarkerStyle());
        }
    }

    public static void generateDlist(DlistBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getDescriptions() != null && Boolean.TRUE.equals(block.getDescriptions().getRequired())) {
            writer.println("* **Descriptions:**");
            CommonBlockDocHelper.writeRequired(writer);
        }
    }

    public static BlockDocGenerator createUlistGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return UlistBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateUlist((UlistBlock) block, writer, patternHumanizer);
            }
        };
    }

    public static BlockDocGenerator createDlistGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return DlistBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateDlist((DlistBlock) block, writer, patternHumanizer);
            }
        };
    }
}
