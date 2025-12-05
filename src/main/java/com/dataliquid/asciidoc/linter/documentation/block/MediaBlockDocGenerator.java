package com.dataliquid.asciidoc.linter.documentation.block;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;
import com.dataliquid.asciidoc.linter.documentation.PatternHumanizer;

/**
 * Generates documentation for audio and video blocks.
 */
public class MediaBlockDocGenerator {

    private MediaBlockDocGenerator() {
        // Static utility
    }

    public static void generateAudio(AudioBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getTitle() != null) {
            // AudioBlock.TitleConfig has no pattern field
            CommonBlockDocHelper.writeTitleSection(writer, block.getTitle().isRequired(), null, patternHumanizer);
        }
    }

    public static void generateVideo(VideoBlock block, PrintWriter writer, PatternHumanizer patternHumanizer) {
        if (block.getCaption() != null) {
            writer.println("* **Caption:**");
            if (Boolean.TRUE.equals(block.getCaption().getRequired())) {
                CommonBlockDocHelper.writeRequired(writer);
            }
            CommonBlockDocHelper.writeMinLength(writer, block.getCaption().getMinLength());
            CommonBlockDocHelper.writeMaxLength(writer, block.getCaption().getMaxLength());
        }
    }

    public static BlockDocGenerator createAudioGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return AudioBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateAudio((AudioBlock) block, writer, patternHumanizer);
            }
        };
    }

    public static BlockDocGenerator createVideoGenerator() {
        return new BlockDocGenerator() {
            @Override
            public Class<? extends Block> getBlockClass() {
                return VideoBlock.class;
            }

            @Override
            public void generate(Block block, PrintWriter writer, PatternHumanizer patternHumanizer) {
                generateVideo((VideoBlock) block, writer, patternHumanizer);
            }
        };
    }
}
