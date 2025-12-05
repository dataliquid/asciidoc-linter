package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.QuoteBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class QuoteBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return QuoteBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        QuoteBlock quoteBlock = (QuoteBlock) block;

        if (quoteBlock.getAttribution() != null && quoteBlock.getAttribution().isRequired()) {
            writer.println("* **Attribution:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (quoteBlock.getCitation() != null && quoteBlock.getCitation().isRequired()) {
            writer.println("* **Citation:**");
            CommonBlockDocHelper.writeRequired(writer);
        }
    }
}
