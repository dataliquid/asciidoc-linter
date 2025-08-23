package com.dataliquid.asciidoc.linter.output;

/**
 * Console implementation of OutputWriter that uses System.out and System.err
 * for output operations. This implementation maintains backward compatibility
 * with existing console-based output.
 */
public class ConsoleWriter implements OutputWriter {

    private static final ConsoleWriter INSTANCE = new ConsoleWriter();
    private boolean debugEnabled = false;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ConsoleWriter() {
    }

    /**
     * Gets the singleton instance of ConsoleWriter.
     *
     * @return the ConsoleWriter instance
     */
    public static ConsoleWriter getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new ConsoleWriter instance with debug mode enabled or disabled.
     *
     * @param  debugEnabled whether debug output should be enabled
     *
     * @return              a new ConsoleWriter instance
     */
    public static ConsoleWriter withDebugEnabled(boolean debugEnabled) {
        ConsoleWriter writer = new ConsoleWriter();
        writer.debugEnabled = debugEnabled;
        return writer;
    }

    @Override
    public void write(String message) {
        System.out.print(message); // NOPMD - intentional console output
    }

    @Override
    public void writeLine(String message) {
        System.out.println(message); // NOPMD - intentional console output
    }

    @Override
    public void writeError(String message) {
        System.err.println(message); // NOPMD - intentional console output
    }

    @Override
    public void writeDebug(String message) {
        if (debugEnabled) {
            System.out.println("[DEBUG] " + message); // NOPMD - intentional console output
        }
    }

    @Override
    public void writeWarning(String message) {
        System.err.println("[WARNING] " + message); // NOPMD - intentional console output
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Sets the debug mode for this writer.
     *
     * @param debugEnabled whether debug output should be enabled
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
}