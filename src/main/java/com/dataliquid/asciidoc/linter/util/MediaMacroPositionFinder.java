package com.dataliquid.asciidoc.linter.util;

import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;
import com.dataliquid.asciidoc.linter.validator.block.BlockValidationContext;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

/**
 * Utility class for finding source positions in media macro blocks (video,
 * audio, image). Centralizes the common logic for locating URLs within media
 * macro syntax to eliminate code duplication.
 */
public final class MediaMacroPositionFinder {

    private MediaMacroPositionFinder() {
        // Private constructor to prevent instantiation
    }

    /**
     * Finds the column position of URL in a media macro (video::, audio::,
     * image::).
     *
     * @param  block     the structural node representing the media block
     * @param  context   the validation context containing filename and file cache
     * @param  macroName the macro name without colons (e.g., "video", "audio",
     *                   "image")
     * @param  url       the URL to find, or null if URL is missing
     * @param  fileCache the file cache to retrieve source lines
     *
     * @return           the source position of the URL, or a fallback position if
     *                   not found
     */
    public static SourcePosition findMacroUrlPosition(StructuralNode block, BlockValidationContext context,
            String macroName, String url, FileContentCache fileCache) {

        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }

        String sourceLine = fileLines.get(lineNum - 1);

        // Look for macro pattern (e.g., "video::", "audio::", "image::")
        String macroPattern = macroName + "::";
        int macroStart = sourceLine.indexOf(macroPattern);

        if (macroStart >= 0) {
            int urlEnd = sourceLine.indexOf('[', macroStart);
            if (urlEnd == -1) {
                urlEnd = sourceLine.length();
            }

            if (url != null && !url.isEmpty()) {
                // Find the specific URL position
                int urlStart = sourceLine.indexOf(url, macroStart + macroPattern.length());
                if (urlStart > macroStart && urlStart < urlEnd) {
                    return new SourcePosition(urlStart + 1, urlStart + url.length(), lineNum);
                }
            } else {
                // No URL - position after macro pattern (e.g., after "video::")
                return new SourcePosition(macroStart + macroPattern.length() + 1,
                        macroStart + macroPattern.length() + 1, lineNum);
            }
        }

        return new SourcePosition(1, 1, lineNum);
    }
}
