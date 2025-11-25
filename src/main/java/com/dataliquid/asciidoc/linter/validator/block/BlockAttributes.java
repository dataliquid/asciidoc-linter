package com.dataliquid.asciidoc.linter.validator.block;

/**
 * Central repository for all AsciiDoc block attribute names. This class
 * provides constants for attribute names used across all block validators to
 * ensure consistency and maintainability.
 */
public final class BlockAttributes {

    // Prevent instantiation
    private BlockAttributes() {
    }

    // Common Attributes
    public static final String TARGET = "target";
    public static final String CAPTION = "caption";
    public static final String TITLE = "title";
    public static final String ROLE = "role";
    public static final String ICONS = "icons";
    public static final String ICON = "icon";
    public static final String TYPE = "type";
    public static final String REASON = "reason";
    public static final String OPTIONS = "options";
    public static final String OPTS = "opts";
    public static final String POSITION = "position";
    public static final String ALT = "alt";

    // Dimension Attributes
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    // Quote/Verse Attributes
    public static final String ATTRIBUTION = "attribution";
    public static final String AUTHOR = "author";
    public static final String CITETITLE = "citetitle";
    public static final String SOURCE = "source";

    // Numbered attributes (used in quote/verse blocks)
    public static final String ATTR_1 = "1";
    public static final String ATTR_2 = "2";
    public static final String ATTR_3 = "3";

    // Media Control Attributes
    public static final String POSTER = "poster";
    public static final String CONTROLS = "controls";
    public static final String NOCONTROLS = "nocontrols";
    public static final String AUTOPLAY = "autoplay";
    public static final String LOOP = "loop";

    // Code Attributes
    public static final String LANGUAGE = "language";

    // Example Block Attributes
    public static final String COLLAPSIBLE = "collapsible";
    public static final String COLLAPSIBLE_OPTION = "collapsible-option";

    // Table Attributes
    public static final String FRAME = "frame";
}
