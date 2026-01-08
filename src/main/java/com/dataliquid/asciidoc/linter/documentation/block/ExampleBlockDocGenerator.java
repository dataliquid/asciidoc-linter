package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ExampleBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class ExampleBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return ExampleBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        ExampleBlock exampleBlock = (ExampleBlock) block;

        if (exampleBlock.getCaption() != null) {
            writer.println("* **Caption:**");
            if (exampleBlock.getCaption().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinLength(writer, exampleBlock.getCaption().getMinLength());
            CommonBlockDocHelper.writeMaxLength(writer, exampleBlock.getCaption().getMaxLength());
        }

        if (exampleBlock.getCollapsible() != null && exampleBlock.getCollapsible().isRequired()) {
            writer.println("* **Collapsible:** Required");
        }
    }
}
