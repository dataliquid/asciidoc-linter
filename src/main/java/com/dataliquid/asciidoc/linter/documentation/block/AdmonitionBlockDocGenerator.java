package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.AdmonitionBlock;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class AdmonitionBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return AdmonitionBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        AdmonitionBlock admonitionBlock = (AdmonitionBlock) block;

        if (admonitionBlock.getTitle() != null) {
            String pattern = admonitionBlock.getTitle().getPattern() != null
                    ? admonitionBlock.getTitle().getPattern().pattern()
                    : null;
            CommonBlockDocHelper
                    .writeTitleSection(writer, admonitionBlock.getTitle().isRequired(), pattern, patternHumanizer);
        }

        if (admonitionBlock.getContent() != null && admonitionBlock.getContent().isRequired()) {
            CommonBlockDocHelper.writeContentSection(writer, true);
        }

        if (admonitionBlock.getIcon() != null && admonitionBlock.getIcon().isRequired()) {
            writer.println("* **Icon:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (admonitionBlock.getTypeConfig() != null && admonitionBlock.getTypeConfig().getAllowed() != null
                && !admonitionBlock.getTypeConfig().getAllowed().isEmpty()) {
            writer.println("* **Allowed Types:** " + String.join(", ", admonitionBlock.getTypeConfig().getAllowed()));
        }
    }
}
