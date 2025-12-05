package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class ParagraphBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return ParagraphBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        ParagraphBlock paragraphBlock = (ParagraphBlock) block;

        if (paragraphBlock.getLines() != null) {
            CommonBlockDocHelper
                    .writeLinesSection(writer, paragraphBlock.getLines().min(), paragraphBlock.getLines().max());
        }

        if (paragraphBlock.getSentence() != null) {
            writer.println("* **Sentences:**");
            var occurrence = paragraphBlock.getSentence().getOccurrence();
            if (occurrence != null) {
                CommonBlockDocHelper.writeMinimum(writer, occurrence.min(), " sentences");
                CommonBlockDocHelper.writeMaximum(writer, occurrence.max(), " sentences");
            }
            var words = paragraphBlock.getSentence().getWords();
            if (words != null) {
                writer
                        .println("  - Words per sentence: " + (words.getMin() != null ? words.getMin() + "-" : "")
                                + (words.getMax() != null ? words.getMax() : "unlimited"));
            }
        }
    }
}
