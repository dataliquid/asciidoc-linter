package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.SidebarBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class SidebarBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return SidebarBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        SidebarBlock sidebarBlock = (SidebarBlock) block;

        if (sidebarBlock.getTitle() != null) {
            String pattern = sidebarBlock.getTitle().getPattern() != null
                    ? sidebarBlock.getTitle().getPattern().pattern()
                    : null;
            CommonBlockDocHelper
                    .writeTitleSection(writer, sidebarBlock.getTitle().isRequired(), pattern, patternHumanizer);
        }

        if (sidebarBlock.getContent() != null && sidebarBlock.getContent().isRequired()) {
            CommonBlockDocHelper.writeContentSection(writer, true);
        }

        if (sidebarBlock.getPosition() != null && sidebarBlock.getPosition().isRequired()) {
            writer.println("* **Position:**");
            CommonBlockDocHelper.writeRequired(writer);
        }
    }
}
