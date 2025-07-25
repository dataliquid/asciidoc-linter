package com.dataliquid.asciidoc.linter.cli.display;

import java.io.PrintStream;
import java.util.List;

/**
 * Utility class for drawing ASCII box borders and content.
 * Provides methods to create well-formatted text boxes for console output.
 */
public class AsciiBoxDrawer {
    
    private final int width;
    private final PrintStream out;
    
    /**
     * Creates a new AsciiBoxDrawer with the specified width.
     * 
     * @param width the total width of the box including borders
     */
    public AsciiBoxDrawer(int width) {
        this(width, System.out);
    }
    
    /**
     * Creates a new AsciiBoxDrawer with the specified width and output stream.
     * 
     * @param width the total width of the box including borders
     * @param out the output stream to write to
     */
    public AsciiBoxDrawer(int width, PrintStream out) {
        if (width < 4) {
            throw new IllegalArgumentException("Box width must be at least 4");
        }
        this.width = width;
        this.out = out;
    }
    
    /**
     * Draws the top border of the box.
     */
    public void drawTop() {
        out.println("+" + "-".repeat(width - 2) + "+");
    }
    
    /**
     * Draws the bottom border of the box.
     */
    public void drawBottom() {
        drawTop(); // Same as top border
    }
    
    /**
     * Draws a separator line within the box.
     */
    public void drawSeparator() {
        drawTop(); // Same as borders
    }
    
    /**
     * Draws a centered title within the box.
     * 
     * @param title the title text to center
     */
    public void drawTitle(String title) {
        if (title == null) {
            title = "";
        }
        
        int contentWidth = width - 2; // Subtract borders
        if (title.length() > contentWidth) {
            title = title.substring(0, contentWidth);
        }
        
        int totalPadding = contentWidth - title.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        out.println("|" + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + "|");
    }
    
    /**
     * Draws a simple content line with padding.
     * 
     * @param content the content to draw
     */
    public void drawLine(String content) {
        if (content == null) {
            content = "";
        }
        
        int contentWidth = width - 4; // Subtract borders and internal padding
        if (content.length() > contentWidth) {
            content = content.substring(0, contentWidth);
        }
        
        out.println("| " + content + " ".repeat(contentWidth - content.length()) + " |");
    }
    
    /**
     * Draws a labeled content line with proper alignment.
     * 
     * @param label the label text
     * @param value the value text
     * @param labelWidth the width allocated for the label
     */
    public void drawLabeledLine(String label, String value, int labelWidth) {
        if (label == null) label = "";
        if (value == null) value = "";
        
        String paddedLabel = String.format("%-" + labelWidth + "s", label);
        int valueWidth = width - 4 - labelWidth; // Subtract borders, padding, and label
        
        if (value.length() > valueWidth) {
            value = value.substring(0, valueWidth);
        }
        
        out.println("| " + paddedLabel + value + " ".repeat(valueWidth - value.length()) + " |");
    }
    
    /**
     * Draws multiple lines for labeled content, handling wrapped text.
     * 
     * @param label the label text (only shown on first line)
     * @param lines the wrapped content lines
     * @param labelWidth the width allocated for the label
     */
    public void drawLabeledLines(String label, List<String> lines, int labelWidth) {
        if (lines == null || lines.isEmpty()) {
            drawLabeledLine(label, "", labelWidth);
            return;
        }
        
        // First line with label
        drawLabeledLine(label, lines.get(0), labelWidth);
        
        // Additional lines with empty label space
        for (int i = 1; i < lines.size(); i++) {
            drawLabeledLine("", lines.get(i), labelWidth);
        }
    }
    
    /**
     * Draws an empty line within the box.
     */
    public void drawEmptyLine() {
        drawLine("");
    }
    
    /**
     * Gets the width of the box.
     * 
     * @return the box width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the available content width (excluding borders and padding).
     * 
     * @return the content width
     */
    public int getContentWidth() {
        return width - 4; // 2 for borders, 2 for internal padding
    }
}