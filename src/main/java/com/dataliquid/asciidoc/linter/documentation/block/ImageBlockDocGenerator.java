package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class ImageBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return ImageBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        ImageBlock imageBlock = (ImageBlock) block;

        if (imageBlock.getUrl() != null) {
            writer.println("* **URL Requirements:**");
            if (imageBlock.getUrl().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            if (imageBlock.getUrl().getPattern() != null) {
                CommonBlockDocHelper.writePattern(writer, imageBlock.getUrl().getPattern().pattern(), patternHumanizer);
            }
        }

        if (imageBlock.getWidth() != null) {
            writer.println("* **Width:**");
            if (imageBlock.getWidth().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinimum(writer, imageBlock.getWidth().getMinValue(), "px");
            CommonBlockDocHelper.writeMaximum(writer, imageBlock.getWidth().getMaxValue(), "px");
        }

        if (imageBlock.getHeight() != null) {
            writer.println("* **Height:**");
            if (imageBlock.getHeight().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinimum(writer, imageBlock.getHeight().getMinValue(), "px");
            CommonBlockDocHelper.writeMaximum(writer, imageBlock.getHeight().getMaxValue(), "px");
        }

        if (imageBlock.getAlt() != null) {
            writer.println("* **Alt Text:**");
            if (imageBlock.getAlt().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinLength(writer, imageBlock.getAlt().getMinLength());
            CommonBlockDocHelper.writeMaxLength(writer, imageBlock.getAlt().getMaxLength());
        }
    }
}
