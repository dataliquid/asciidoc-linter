package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.LiteralBlock;
import com.dataliquid.asciidoc.linter.config.blocks.PassBlock;
import com.dataliquid.asciidoc.linter.config.blocks.VerseBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Generates documentation for special blocks (literal, pass, verse).
 */
public class SpecialBlockDocGenerator {

    private SpecialBlockDocGenerator() {
        // Static utility
    }

    public static void generateLiteral(LiteralBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getTitle() != null) {
            // LiteralBlock.TitleConfig has no pattern field
            CommonBlockDocHelper.writeTitleSection(writer, block.getTitle().isRequired(), null, patternHumanizer);
        }
    }

    public static void generatePass(PassBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getContent() != null && block.getContent().isRequired()) {
            CommonBlockDocHelper.writeContentSection(writer, true);
        }

        if (block.getReason() != null && block.getReason().isRequired()) {
            writer.println("* **Reason:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (block.getTypeConfig() != null && block.getTypeConfig().isRequired()) {
            writer.println("* **Type:**");
            CommonBlockDocHelper.writeRequired(writer);
        }
    }

    public static void generateVerse(VerseBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getAuthor() != null && block.getAuthor().isRequired()) {
            writer.println("* **Author:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (block.getAttribution() != null && block.getAttribution().isRequired()) {
            writer.println("* **Attribution:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (block.getContent() != null && block.getContent().isRequired()) {
            CommonBlockDocHelper.writeContentSection(writer, true);
        }
    }

    public static BlockDocGenerator createLiteralGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return LiteralBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateLiteral((LiteralBlock) block, writer, patternHumanizer);
            }
        };
    }

    public static BlockDocGenerator createPassGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return PassBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generatePass((PassBlock) block, writer, patternHumanizer);
            }
        };
    }

    public static BlockDocGenerator createVerseGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return VerseBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateVerse((VerseBlock) block, writer, patternHumanizer);
            }
        };
    }
}
