package com.dataliquid.asciidoc.linter.config.common;

/**
 * Central repository for all JSON property names used in configuration classes.
 * This class provides constants for JSON property names to ensure consistency
 * and maintainability across all configuration POJOs.
 */
public final class JsonPropertyNames {
    
    // Prevent instantiation
    private JsonPropertyNames() {}
    
    /**
     * Empty string constant for use in annotations like @JsonPOJOBuilder
     */
    public static final String EMPTY = "";
    
    /**
     * Common property names used across multiple configuration classes
     */
    public static final class Common {
        public static final String NAME = "name";
        public static final String SEVERITY = "severity";
        public static final String REQUIRED = "required";
        public static final String PATTERN = "pattern";
        public static final String MIN_LENGTH = "minLength";
        public static final String MAX_LENGTH = "maxLength";
        public static final String MIN = "min";
        public static final String MAX = "max";
        public static final String ENABLED = "enabled";
        public static final String ALLOWED = "allowed";
        public static final String ORDER = "order";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String OPTIONS = "options";
        public static final String LINES = "lines";
        public static final String CONTENT = "content";
        public static final String TYPE = "type";
        public static final String OCCURRENCE = "occurrence";
        public static final String MIN_VALUE = "minValue";
        public static final String MAX_VALUE = "maxValue";
        public static final String LEVEL = "level";
        public static final String CAPTION = "caption";
        public static final String THRESHOLD = "threshold";
        public static final String DEFAULT_VALUE = "defaultValue";
    }
    
    /**
     * Property names for document configuration
     */
    public static final class Document {
        public static final String DOCUMENT = "document";
        public static final String METADATA = "metadata";
        public static final String SECTIONS = "sections";
        public static final String ATTRIBUTES = "attributes";
        public static final String SUBSECTIONS = "subsections";
        public static final String ALLOWED_BLOCKS = "allowedBlocks";
    }
    
    /**
     * Property names for admonition block configuration
     */
    public static final class Admonition {
        public static final String TYPE = "type";
        public static final String ICON = "icon";
    }
    
    /**
     * Property names for audio/video block configuration
     */
    public static final class Media {
        public static final String AUTOPLAY = "autoplay";
        public static final String CONTROLS = "controls";
        public static final String LOOP = "loop";
        public static final String POSTER = "poster";
        public static final String NO_CONTROLS = "nocontrols";
    }
    
    /**
     * Property names for quote/verse block configuration
     */
    public static final class Quote {
        public static final String ATTRIBUTION = "attribution";
        public static final String CITATION = "citation";
        public static final String AUTHOR = "author";
    }
    
    /**
     * Property names for listing block configuration
     */
    public static final class Listing {
        public static final String LANGUAGE = "language";
        public static final String CALLOUTS = "callouts";
    }
    
    /**
     * Property names for literal block configuration
     */
    public static final class Literal {
        public static final String INDENTATION = "indentation";
        public static final String MIN_SPACES = "minSpaces";
        public static final String MAX_SPACES = "maxSpaces";
        public static final String CONSISTENT = "consistent";
    }
    
    /**
     * Property names for example block configuration
     */
    public static final class Example {
        public static final String COLLAPSIBLE = "collapsible";
    }
    
    /**
     * Property names for sidebar block configuration
     */
    public static final class Sidebar {
        public static final String POSITION = "position";
    }
    
    /**
     * Property names for table block configuration
     */
    public static final class Table {
        public static final String COLUMNS = "columns";
        public static final String ROWS = "rows";
        public static final String HEADER = "header";
        public static final String FORMAT = "format";
        public static final String STYLE = "style";
        public static final String BORDERS = "borders";
    }
    
    /**
     * Property names for list block configuration
     */
    public static final class List {
        public static final String ITEMS = "items";
        public static final String TERMS = "terms";
        public static final String DESCRIPTIONS = "descriptions";
        public static final String NESTING_LEVEL = "nestingLevel";
        public static final String MARKER_STYLE = "markerStyle";
        public static final String DELIMITER_STYLE = "delimiterStyle";
        public static final String ALLOWED_DELIMITERS = "allowedDelimiters";
    }
    
    /**
     * Property names for paragraph block configuration
     */
    public static final class Paragraph {
        public static final String SENTENCE = "sentence";
        public static final String WORDS = "words";
    }
    
    /**
     * Property names for pass block configuration
     */
    public static final class Pass {
        public static final String REASON = "reason";
    }
    
    /**
     * Property names for image block configuration
     */
    public static final class Image {
        public static final String ALT = "alt";
    }
    
    /**
     * Property names for output configuration
     */
    public static final class Output {
        public static final String OUTPUT = "output";
        public static final String FORMAT = "format";
        public static final String DISPLAY = "display";
        public static final String SUGGESTIONS = "suggestions";
        public static final String ERROR_GROUPING = "errorGrouping";
        public static final String SUMMARY = "summary";
        public static final String CONTEXT_LINES = "contextLines";
        public static final String HIGHLIGHT_STYLE = "highlightStyle";
        public static final String USE_COLORS = "useColors";
        public static final String SHOW_LINE_NUMBERS = "showLineNumbers";
        public static final String MAX_LINE_WIDTH = "maxLineWidth";
        public static final String SHOW_HEADER = "showHeader";
        public static final String MAX_PER_ERROR = "maxPerError";
        public static final String SHOW_EXAMPLES = "showExamples";
        public static final String SHOW_STATISTICS = "showStatistics";
        public static final String SHOW_MOST_COMMON = "showMostCommon";
        public static final String SHOW_FILE_LIST = "showFileList";
    }
}