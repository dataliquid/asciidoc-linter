package com.dataliquid.asciidoc.linter.validator.block;

import java.util.List;
import org.asciidoctor.ast.StructuralNode;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;

/**
 * Utility class to calculate the actual end line of blocks by analyzing source
 * files. Provides generic support for all AsciiDoc block types.
 */
public class BlockEndCalculator {
    private final FileContentCache fileCache;

    public BlockEndCalculator(FileContentCache fileCache) {
        this.fileCache = fileCache;
    }

    /**
     * Calculates the actual end line of a block by analyzing the source file. Works
     * generically for ALL block types.
     *
     * @param  block    the block to analyze
     * @param  filename the source file name
     *
     * @return          the 1-based line number where the block ends
     */
    public int calculateBlockEndLine(StructuralNode block, String filename) {
        if (block.getSourceLocation() == null) {
            return 1;
        }

        int startLine = block.getSourceLocation().getLineNumber();
        List<String> fileLines = fileCache.getFileLines(filename);

        if (fileLines.isEmpty() || startLine > fileLines.size()) {
            return startLine;
        }

        // Generic detection based on block context
        String context = block.getContext();

        switch (context) {
        // List blocks
        case "dlist":
            return findDlistEnd(fileLines, startLine);
        case "ulist":
        case "olist":
        case "colist":
            return findListEnd(fileLines, startLine, context);

        // Delimited blocks
        case "listing":
        case "literal":
        case "example":
        case "sidebar":
        case "quote":
        case "verse":
        case "pass":
        case "open":
        case "comment":
        case "admonition":
            return findDelimitedBlockEnd(fileLines, startLine, context);

        // Table blocks
        case "table":
            return findTableEnd(fileLines, startLine);

        // Content blocks
        case "paragraph":
            return findParagraphEnd(fileLines, startLine);

        // Media blocks (single line)
        case "image":
        case "video":
        case "audio":
            return startLine;

        // Other blocks
        case "stem":
        case "toc":
        case "preamble":
        case "abstract":
            return findSpecialBlockEnd(fileLines, startLine, context);

        default:
            // Generic fallback
            return findGenericBlockEnd(fileLines, startLine);
        }
    }

    /**
     * Finds the end of a description list (dlist). Continues while finding ::
     * patterns, stops at empty line or section.
     */
    private int findDlistEnd(List<String> lines, int startLine) {
        int line = startLine - 1; // Convert to 0-based index
        int lastDlistLine = line; // Track the last actual dlist content line

        while (line < lines.size()) {
            String currentLine = lines.get(line).trim();

            if (currentLine.contains("::")) {
                // This is a dlist term
                lastDlistLine = line; // Update last dlist line
                line++;
                // Skip description lines (they don't contain ::)
                while (line < lines.size()) {
                    String descLine = lines.get(line).trim();
                    if (descLine.isEmpty()) {
                        // Check if there's another dlist entry after this empty line
                        if (line + 1 < lines.size() && lines.get(line + 1).trim().contains("::")) {
                            // Continue with the dlist
                            line++;
                            break;
                        } else {
                            // This empty line ends the dlist
                            return lastDlistLine + 1; // Return 1-based line number
                        }
                    } else if (descLine.contains("::") || descLine.startsWith("=") || isBlockStart(descLine)) {
                        break;
                    } else {
                        // This is part of the description
                        lastDlistLine = line;
                    }
                    line++;
                }
            } else if (currentLine.isEmpty()) {
                // Empty line ends the dlist
                break;
            } else if (isBlockStart(currentLine)) {
                // Another block starts
                break;
            } else {
                // This shouldn't happen in a well-formed dlist, but handle it
                lastDlistLine = line;
                line++;
            }
        }

        // Return 1-based line number (the last line that was part of the dlist)
        return lastDlistLine + 1;
    }

    /**
     * Finds the end of delimited blocks (listing, literal, example, etc.). Searches
     * for matching closing delimiter.
     */
    private int findDelimitedBlockEnd(List<String> lines, int startLine, String context) {
        String delimiter = getDelimiterForType(context);
        int line = startLine - 1; // Convert to 0-based

        // Skip attributes and find opening delimiter
        while (line < lines.size() && !lines.get(line).trim().equals(delimiter)) {
            line++;
        }

        if (line >= lines.size()) {
            return lines.size();
        }

        // Skip opening delimiter
        line++;

        // Find closing delimiter
        while (line < lines.size() && !lines.get(line).trim().equals(delimiter)) {
            line++;
        }

        // Include the closing delimiter
        if (line < lines.size()) {
            line++;
        }

        return line + 1; // Return 1-based
    }

    /**
     * Returns the delimiter string for a block type.
     */
    private String getDelimiterForType(String type) {
        switch (type) {
        case "listing":
            return "----";
        case "literal":
            return "....";
        case "example":
            return "====";
        case "sidebar":
            return "****";
        case "quote":
        case "verse":
            return "____";
        case "pass":
            return "++++";
        case "open":
            return "--";
        case "comment":
            return "////";
        case "admonition":
            return "====";
        case "stem":
            return "++++";
        default:
            return "----";
        }
    }

    /**
     * Finds the end of a table block. Searches for closing |===.
     */
    private int findTableEnd(List<String> lines, int startLine) {
        int line = startLine - 1;

        // Find opening |===
        while (line < lines.size() && !lines.get(line).trim().equals("|===")) {
            line++;
        }

        // Skip to content
        line++;

        // Find closing |===
        while (line < lines.size() && !lines.get(line).trim().equals("|===")) {
            line++;
        }

        // Include closing delimiter
        if (line < lines.size()) {
            line++;
        }

        return line + 1; // Return 1-based
    }

    /**
     * Finds the end of a paragraph. Stops at empty line or block start.
     */
    private int findParagraphEnd(List<String> lines, int startLine) {
        int line = startLine - 1;

        while (line < lines.size()) {
            String currentLine = lines.get(line).trim();

            if (currentLine.isEmpty() || isBlockStart(currentLine)) {
                break;
            }
            line++;
        }

        return line > 0 ? line : 1; // Return 1-based
    }

    /**
     * Finds the end of lists (ulist, olist). Continues while list markers match
     * pattern.
     */
    private int findListEnd(List<String> lines, int startLine, String listType) {
        int line = startLine - 1;
        String markerPattern = getListMarkerPattern(listType);

        while (line < lines.size()) {
            String currentLine = lines.get(line).trim();

            if (currentLine.isEmpty()) {
                // Empty line ends list
                break;
            }

            if (!matchesListMarker(currentLine, markerPattern) && !isContinuationLine(lines.get(line))) {
                // Not a list item or continuation
                break;
            }

            line++;
        }

        return line > 0 ? line : 1; // Return 1-based
    }

    /**
     * Generic fallback for unknown block types.
     */
    private int findGenericBlockEnd(List<String> lines, int startLine) {
        int line = startLine - 1;
        int initialIndent = getIndentLevel(lines.get(line));

        while (line < lines.size()) {
            String current = lines.get(line);

            if (current.trim().isEmpty()) {
                // Empty line likely ends block
                break;
            }

            if (isBlockStart(current)) {
                // New block starts
                break;
            }

            if (getIndentLevel(current) < initialIndent && initialIndent > 0) {
                // Dedent indicates end
                break;
            }

            line++;
        }

        return line > 0 ? line : 1; // Return 1-based
    }

    /**
     * Checks if a line starts a new block.
     */
    private boolean isBlockStart(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("=") || // Section
                trimmed.startsWith("image::") || // Image
                trimmed.startsWith("video::") || // Video
                trimmed.startsWith("audio::") || // Audio
                trimmed.startsWith("include::") || // Include
                trimmed.startsWith("[") || // Block attribute
                trimmed.startsWith("|===") || // Table
                trimmed.equals("----") || // Listing
                trimmed.equals("....") || // Literal
                trimmed.equals("====") || // Example
                trimmed.equals("****") || // Sidebar
                trimmed.equals("____") || // Quote/Verse
                trimmed.equals("++++") || // Pass
                trimmed.equals("--") || // Open
                trimmed.equals("////"); // Comment
    }

    private String getListMarkerPattern(String listType) {
        switch (listType) {
        case "ulist":
            return "[*\\-•‣⁃]";
        case "olist":
            return "[0-9]+\\.|\\.|[a-zA-Z]\\.|[ivxIVX]+\\.";
        case "colist":
            return "<[0-9]+>";
        default:
            return "";
        }
    }

    private boolean matchesListMarker(String line, String pattern) {
        if (pattern.isEmpty()) {
            return false;
        }
        return line.matches("^\\s*" + pattern + "\\s+.*");
    }

    private boolean isContinuationLine(String line) {
        return line.startsWith(" ") || line.startsWith("\t") || line.startsWith("+");
    }

    private int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ')
                indent++;
            else if (c == '\t')
                indent += 4;
            else
                break;
        }
        return indent;
    }

    private int findSpecialBlockEnd(List<String> lines, int startLine, String context) {
        // Handle special blocks like stem, toc, etc.
        switch (context) {
        case "stem":
            return findDelimitedBlockEnd(lines, startLine, context);
        case "toc":
        case "preamble":
        case "abstract":
            return findParagraphEnd(lines, startLine);
        default:
            return findGenericBlockEnd(lines, startLine);
        }
    }
}
