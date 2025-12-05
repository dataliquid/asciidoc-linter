package com.dataliquid.asciidoc.linter.cli.display;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.output.OutputWriter;

/**
 * Utility class for drawing ASCII box borders and content. Provides methods to
 * create well-formatted text boxes for console output.
 */
public class AsciiBoxDrawer {

    // Constants
    private static final int MIN_BOX_WIDTH = 4;

    private final int width;
    private final PrintWriter printWriter;
    private final OutputWriter outputWriter;

    /**
     * Creates a new AsciiBoxDrawer with the specified width.
     *
     * @param width the total width of the box including borders
     */
    public AsciiBoxDrawer(int width) {
        this(width, new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
    }

    /**
     * Creates a new AsciiBoxDrawer with the specified width and print writer.
     *
     * @param width  the total width of the box including borders
     * @param writer the print writer to write to
     */
    public AsciiBoxDrawer(int width, PrintWriter writer) {
        if (width < MIN_BOX_WIDTH) {
            throw new IllegalArgumentException("Box width must be at least " + MIN_BOX_WIDTH);
        }
        this.width = width;
        this.printWriter = Objects.requireNonNull(writer, "PrintWriter must not be null");
        this.outputWriter = null; // Use PrintWriter for output
    }

    /**
     * Creates a new AsciiBoxDrawer with the specified width and output writer.
     *
     * @param width        the total width of the box including borders
     * @param outputWriter the output writer to write to
     */
    public AsciiBoxDrawer(int width, OutputWriter outputWriter) {
        if (width < MIN_BOX_WIDTH) {
            throw new IllegalArgumentException("Box width must be at least " + MIN_BOX_WIDTH);
        }
        this.width = width;
        this.printWriter = null; // Use OutputWriter for output
        this.outputWriter = Objects.requireNonNull(outputWriter, "OutputWriter must not be null");
    }

    /**
     * Outputs a line to the appropriate output.
     */
    private void println(String line) {
        if (outputWriter != null) {
            outputWriter.writeLine(line);
        } else {
            printWriter.println(line);
        }
    }

    /**
     * Draws the top border of the box.
     */
    public void drawTop() {
        println("+" + "-".repeat(width - 2) + "+");
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
        String titleText = title;
        if (titleText == null) {
            titleText = "";
        }

        int contentWidth = width - 2; // Subtract borders
        if (titleText.length() > contentWidth) {
            titleText = titleText.substring(0, contentWidth);
        }

        int totalPadding = contentWidth - titleText.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;

        println("|" + " ".repeat(leftPadding) + titleText + " ".repeat(rightPadding) + "|");
    }

    /**
     * Draws a simple content line with padding.
     *
     * @param content the content to draw
     */
    public void drawLine(String content) {
        String contentText = content;
        if (contentText == null) {
            contentText = "";
        }

        int contentWidth = width - 4; // Subtract borders and internal padding
        if (contentText.length() > contentWidth) {
            contentText = contentText.substring(0, contentWidth);
        }

        println("| " + contentText + " ".repeat(contentWidth - contentText.length()) + " |");
    }

    /**
     * Draws a labeled content line with proper alignment.
     *
     * @param label      the label text
     * @param value      the value text
     * @param labelWidth the width allocated for the label
     */
    public void drawLabeledLine(String label, String value, int labelWidth) {
        String labelText = label;
        String valueText = value;
        if (labelText == null)
            labelText = "";
        if (valueText == null)
            valueText = "";

        String paddedLabel = String.format("%-" + labelWidth + "s", labelText);
        int valueWidth = width - 4 - labelWidth; // Subtract borders, padding, and label

        if (valueText.length() > valueWidth) {
            valueText = valueText.substring(0, valueWidth);
        }

        println("| " + paddedLabel + valueText + " ".repeat(valueWidth - valueText.length()) + " |");
    }

    /**
     * Draws multiple lines for labeled content, handling wrapped text.
     *
     * @param label      the label text (only shown on first line)
     * @param lines      the wrapped content lines
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
