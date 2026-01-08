package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames;

/**
 * Central repository for all AsciiDoc block attribute names. This class
 * provides constants for attribute names used across all block validators to
 * ensure consistency and maintainability. Note: Many constants are delegated to
 * JsonPropertyNames to avoid duplication with configuration property names, as
 * they often represent the same concepts.
 */
public final class BlockAttributes {

    // Prevent instantiation
    private BlockAttributes() {
    }

    // Common Attributes - delegated to JsonPropertyNames
    public static final String CAPTION = JsonPropertyNames.Common.CAPTION;
    public static final String TITLE = JsonPropertyNames.Common.TITLE;
    public static final String TYPE = JsonPropertyNames.Common.TYPE;
    public static final String OPTIONS = JsonPropertyNames.Common.OPTIONS;
    public static final String WIDTH = JsonPropertyNames.Common.WIDTH;
    public static final String HEIGHT = JsonPropertyNames.Common.HEIGHT;
    public static final String POSTER = JsonPropertyNames.Media.POSTER;
    public static final String CONTROLS = JsonPropertyNames.Media.CONTROLS;
    public static final String AUTOPLAY = JsonPropertyNames.Media.AUTOPLAY;
    public static final String LOOP = JsonPropertyNames.Media.LOOP;
    public static final String ATTRIBUTION = JsonPropertyNames.Quote.ATTRIBUTION;
    public static final String AUTHOR = JsonPropertyNames.Quote.AUTHOR;

    // Block-specific Attributes
    public static final String TARGET = "target";
    public static final String ROLE = "role";
    public static final String ICONS = "icons";
    public static final String ICON = "icon";
    public static final String REASON = "reason";
    public static final String OPTS = "opts";
    public static final String POSITION = "position";
    public static final String ALT = "alt";

    // Quote/Verse Attributes
    public static final String CITETITLE = "citetitle";
    public static final String SOURCE = "source";

    // Numbered attributes (used in quote/verse blocks)
    public static final String ATTR_1 = "1";
    public static final String ATTR_2 = "2";
    public static final String ATTR_3 = "3";

    // Media Control Attributes
    public static final String NOCONTROLS = "nocontrols";

    // Code Attributes
    public static final String LANGUAGE = "language";

    // Example Block Attributes
    public static final String COLLAPSIBLE = "collapsible";
    public static final String COLLAPSIBLE_OPTION = "collapsible-option";

    // Table Attributes
    public static final String FRAME = "frame";
}
