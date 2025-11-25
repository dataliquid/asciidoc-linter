package com.dataliquid.asciidoc.linter.output;

/**
 * Interface for abstracting output operations in the AsciiDoc linter. This
 * interface provides methods for writing different types of messages to various
 * output targets while maintaining backward compatibility.
 */
public interface OutputWriter {

    /**
     * Writes a message without a newline.
     *
     * @param message the message to write
     */
    void write(String message);

    /**
     * Writes a message followed by a newline.
     *
     * @param message the message to write
     */
    void writeLine(String message);

    /**
     * Writes an empty line.
     */
    default void writeLine() {
        writeLine("");
    }

    /**
     * Writes an error message to the error output.
     *
     * @param message the error message to write
     */
    void writeError(String message);

    /**
     * Writes a debug message if debug mode is enabled.
     *
     * @param message the debug message to write
     */
    void writeDebug(String message);

    /**
     * Writes a warning message.
     *
     * @param message the warning message to write
     */
    void writeWarning(String message);

    /**
     * Checks if debug output is enabled.
     *
     * @return true if debug output is enabled, false otherwise
     */
    boolean isDebugEnabled();
}