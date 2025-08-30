package com.dataliquid.asciidoc.linter.validator.block;

/**
 * Constants for AsciiDoc syntax elements.
 */
public final class AsciiDocConstants {

    // Block delimiters
    public static final String DELIMITER_LISTING = "----";
    public static final String DELIMITER_LITERAL = "....";
    public static final String DELIMITER_EXAMPLE = "====";
    public static final String DELIMITER_SIDEBAR = "****";
    public static final String DELIMITER_QUOTE = "____";
    public static final String DELIMITER_PASS = "++++";
    public static final String DELIMITER_OPEN = "--";
    public static final String DELIMITER_COMMENT = "////";
    public static final String DELIMITER_TABLE = "|===";

    // Directive prefixes
    public static final String DIRECTIVE_INCLUDE = "include::";
    public static final String DIRECTIVE_IMAGE = "image::";
    public static final String DIRECTIVE_VIDEO = "video::";
    public static final String DIRECTIVE_AUDIO = "audio::";

    // Special characters
    public static final String ATTRIBUTE_START = "[";
    public static final String SECTION_START = "=";

    private AsciiDocConstants() {
        // Private constructor to prevent instantiation
    }
}