package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

public class TableBlockDocGenerator implements BlockDocGenerator {

    @Override
    public Class<? extends Block> getBlockClass() {
        return TableBlock.class;
    }

    @Override
    public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        TableBlock tableBlock = (TableBlock) block;

        if (tableBlock.getCaption() != null) {
            writer.println("* **Caption:**");
            if (tableBlock.getCaption().isRequired()) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinLength(writer, tableBlock.getCaption().getMinLength());
            CommonBlockDocHelper.writeMaxLength(writer, tableBlock.getCaption().getMaxLength());
        }

        if (tableBlock.getHeader() != null && tableBlock.getHeader().isRequired()) {
            writer.println("* **Header:**");
            CommonBlockDocHelper.writeRequired(writer);
        }

        if (tableBlock.getColumns() != null) {
            writer.println("* **Columns:**");
            CommonBlockDocHelper.writeMinimum(writer, tableBlock.getColumns().getMin(), null);
            CommonBlockDocHelper.writeMaximum(writer, tableBlock.getColumns().getMax(), null);
        }

        if (tableBlock.getRows() != null) {
            writer.println("* **Rows:**");
            CommonBlockDocHelper.writeMinimum(writer, tableBlock.getRows().getMin(), null);
            CommonBlockDocHelper.writeMaximum(writer, tableBlock.getRows().getMax(), null);
        }
    }
}
