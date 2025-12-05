package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ListingBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class ListingBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return ListingBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        ListingBlock listingBlock = (ListingBlock) block;

        if (listingBlock.getLanguage() != null) {
            writer.println("* **Language:**");
            if (listingBlock.getLanguage().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            if (listingBlock.getLanguage().getAllowed() != null && !listingBlock.getLanguage().getAllowed().isEmpty()) {
                writer.println("  - Allowed: " + String.join(", ", listingBlock.getLanguage().getAllowed()));
            }
        }

        if (listingBlock.getLines() != null) {
            CommonBlockDocHelper
                    .writeLinesSection(writer, listingBlock.getLines().min(), listingBlock.getLines().max());
        }

        if (listingBlock.getTitle() != null) {
            String pattern = listingBlock.getTitle().getPattern() != null
                    ? listingBlock.getTitle().getPattern().pattern()
                    : null;
            CommonBlockDocHelper
                    .writeTitleSection(writer, listingBlock.getTitle().isRequired(), pattern, patternHumanizer);
        }

        if (listingBlock.getCallouts() != null && listingBlock.getCallouts().getMax() != null) {
            writer.println("* **Callouts:**");
            CommonBlockDocHelper.writeMaximum(writer, listingBlock.getCallouts().getMax(), null);
        }
    }
}
