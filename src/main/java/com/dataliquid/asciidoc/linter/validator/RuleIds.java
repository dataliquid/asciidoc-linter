package com.dataliquid.asciidoc.linter.validator;

/**
 * Central repository for all validation rule IDs.
 * This class provides constants for rule IDs used across all validators
 * to ensure consistency and maintainability.
 */
public final class RuleIds {
    
    // Prevent instantiation
    private RuleIds() {}
    
    /**
     * Rule IDs for general block validation
     */
    public static final class Block {
        public static final String TYPE_UNKNOWN = "block.type.unknown";
        public static final String TYPE_NOT_ALLOWED = "block.type.not-allowed";
        public static final String VALIDATION_ERROR = "block.validation.error";
        public static final String ORDER = "block.order";
        public static final String ORDER_FIXED = "block.order.fixed";
        public static final String ORDER_BEFORE = "block.order.before";
        public static final String ORDER_AFTER = "block.order.after";
        public static final String OCCURRENCE_MIN = "block.occurrence.min";
        public static final String OCCURRENCE_MAX = "block.occurrence.max";
    }
    
    /**
     * Rule IDs for section validation
     */
    public static final class Section {
        public static final String TITLE_PATTERN = "section.title.pattern";
        public static final String UNEXPECTED = "section.unexpected";
        public static final String LEVEL = "section.level";
        public static final String MIN_OCCURRENCES = "section.min-occurrences";
        public static final String MAX_OCCURRENCES = "section.max-occurrences";
        public static final String LEVEL0_MISSING = "section.level0.missing";
        public static final String ORDER = "section.order";
    }
    
    /**
     * Rule IDs for metadata validation
     */
    public static final class Metadata {
        public static final String REQUIRED = "metadata.required";
        public static final String PATTERN = "metadata.pattern";
        public static final String LENGTH = "metadata.length";
        public static final String LENGTH_MIN = "metadata.length.min";
        public static final String LENGTH_MAX = "metadata.length.max";
        public static final String ORDER = "metadata.order";
    }
    
    /**
     * Rule IDs for admonition block validation
     */
    public static final class Admonition {
        public static final String TYPE_REQUIRED = "admonition.type.required";
        public static final String TYPE_ALLOWED = "admonition.type.allowed";
        public static final String TITLE_REQUIRED = "admonition.title.required";
        public static final String TITLE_PATTERN = "admonition.title.pattern";
        public static final String TITLE_MIN_LENGTH = "admonition.title.minLength";
        public static final String TITLE_MAX_LENGTH = "admonition.title.maxLength";
        public static final String CONTENT_REQUIRED = "admonition.content.required";
        public static final String CONTENT_MIN_LENGTH = "admonition.content.minLength";
        public static final String CONTENT_MAX_LENGTH = "admonition.content.maxLength";
        public static final String CONTENT_LINES_MIN = "admonition.content.lines.min";
        public static final String CONTENT_LINES_MAX = "admonition.content.lines.max";
        public static final String ICON_REQUIRED = "admonition.icon.required";
        public static final String ICON_PATTERN = "admonition.icon.pattern";
    }
    
    /**
     * Rule IDs for audio block validation
     */
    public static final class Audio {
        public static final String URL_REQUIRED = "audio.url.required";
        public static final String URL_PATTERN = "audio.url.pattern";
        public static final String OPTIONS_AUTOPLAY_NOT_ALLOWED = "audio.options.autoplay.notAllowed";
        public static final String OPTIONS_CONTROLS_REQUIRED = "audio.options.controls.required";
        public static final String OPTIONS_LOOP_NOT_ALLOWED = "audio.options.loop.notAllowed";
        public static final String TITLE_REQUIRED = "audio.title.required";
        public static final String TITLE_MIN_LENGTH = "audio.title.minLength";
        public static final String TITLE_MAX_LENGTH = "audio.title.maxLength";
    }
    
    /**
     * Rule IDs for description list (dlist) validation
     */
    public static final class Dlist {
        public static final String TERMS_MIN = "dlist.terms.min";
        public static final String TERMS_MAX = "dlist.terms.max";
        public static final String TERMS_PATTERN = "dlist.terms.pattern";
        public static final String TERMS_MIN_LENGTH = "dlist.terms.minLength";
        public static final String TERMS_MAX_LENGTH = "dlist.terms.maxLength";
        public static final String DESCRIPTIONS_REQUIRED = "dlist.descriptions.required";
        public static final String DESCRIPTIONS_PATTERN = "dlist.descriptions.pattern";
    }
    
    /**
     * Rule IDs for example block validation
     */
    public static final class Example {
        public static final String CAPTION_REQUIRED = "example.caption.required";
        public static final String CAPTION_MIN_LENGTH = "example.caption.minLength";
        public static final String CAPTION_MAX_LENGTH = "example.caption.maxLength";
        public static final String CAPTION_PATTERN = "example.caption.pattern";
        public static final String COLLAPSIBLE_REQUIRED = "example.collapsible.required";
        public static final String COLLAPSIBLE_ALLOWED = "example.collapsible.allowed";
    }
    
    /**
     * Rule IDs for image block validation
     */
    public static final class Image {
        public static final String URL_REQUIRED = "image.url.required";
        public static final String URL_PATTERN = "image.url.pattern";
        public static final String WIDTH_REQUIRED = "image.width.required";
        public static final String WIDTH_MIN = "image.width.min";
        public static final String WIDTH_MAX = "image.width.max";
        public static final String HEIGHT_REQUIRED = "image.height.required";
        public static final String HEIGHT_MIN = "image.height.min";
        public static final String HEIGHT_MAX = "image.height.max";
        public static final String ALT_REQUIRED = "image.alt.required";
        public static final String ALT_MIN_LENGTH = "image.alt.minLength";
        public static final String ALT_MAX_LENGTH = "image.alt.maxLength";
    }
    
    /**
     * Rule IDs for listing block validation
     */
    public static final class Listing {
        public static final String LANGUAGE_REQUIRED = "listing.language.required";
        public static final String LANGUAGE_ALLOWED = "listing.language.allowed";
        public static final String TITLE_REQUIRED = "listing.title.required";
        public static final String TITLE_PATTERN = "listing.title.pattern";
        public static final String LINES_MIN = "listing.lines.min";
        public static final String LINES_MAX = "listing.lines.max";
        public static final String CALLOUTS_NOT_ALLOWED = "listing.callouts.notAllowed";
        public static final String CALLOUTS_MAX = "listing.callouts.max";
    }
    
    /**
     * Rule IDs for literal block validation
     */
    public static final class Literal {
        public static final String TITLE_REQUIRED = "literal.title.required";
        public static final String INDENTATION_MIN_SPACES = "literal.indentation.minSpaces";
        public static final String INDENTATION_MAX_SPACES = "literal.indentation.maxSpaces";
        public static final String INDENTATION_CONSISTENT = "literal.indentation.consistent";
        public static final String LINES_MIN = "literal.lines.min";
    }
    
    /**
     * Rule IDs for paragraph block validation
     */
    public static final class Paragraph {
        public static final String CONTENT_REQUIRED = "paragraph.content.required";
        public static final String CONTENT_MIN_LENGTH = "paragraph.content.minLength";
        public static final String CONTENT_MAX_LENGTH = "paragraph.content.maxLength";
        public static final String CONTENT_PATTERN = "paragraph.content.pattern";
        public static final String LINES_MIN = "paragraph.lines.min";
        public static final String LINES_MAX = "paragraph.lines.max";
        public static final String SENTENCE_OCCURRENCE_MIN = "paragraph.sentence.occurrence.min";
        public static final String SENTENCE_OCCURRENCE_MAX = "paragraph.sentence.occurrence.max";
        public static final String SENTENCE_WORDS_MIN = "paragraph.sentence.words.min";
        public static final String SENTENCE_WORDS_MAX = "paragraph.sentence.words.max";
    }
    
    /**
     * Rule IDs for pass block validation
     */
    public static final class Pass {
        public static final String TYPE_REQUIRED = "pass.type.required";
        public static final String TYPE_ALLOWED = "pass.type.allowed";
        public static final String CONTENT_REQUIRED = "pass.content.required";
        public static final String CONTENT_MAX_LENGTH = "pass.content.maxLength";
        public static final String CONTENT_PATTERN = "pass.content.pattern";
        public static final String REASON_REQUIRED = "pass.reason.required";
        public static final String REASON_MIN_LENGTH = "pass.reason.minLength";
        public static final String REASON_MAX_LENGTH = "pass.reason.maxLength";
    }
    
    /**
     * Rule IDs for quote block validation
     */
    public static final class Quote {
        public static final String ATTRIBUTION_REQUIRED = "quote.attribution.required";
        public static final String ATTRIBUTION_MIN_LENGTH = "quote.attribution.minLength";
        public static final String ATTRIBUTION_MAX_LENGTH = "quote.attribution.maxLength";
        public static final String ATTRIBUTION_PATTERN = "quote.attribution.pattern";
        public static final String CITATION_REQUIRED = "quote.citation.required";
        public static final String CITATION_MIN_LENGTH = "quote.citation.minLength";
        public static final String CITATION_MAX_LENGTH = "quote.citation.maxLength";
        public static final String CITATION_PATTERN = "quote.citation.pattern";
        public static final String CONTENT_REQUIRED = "quote.content.required";
        public static final String CONTENT_MIN_LENGTH = "quote.content.minLength";
        public static final String CONTENT_MAX_LENGTH = "quote.content.maxLength";
        public static final String CONTENT_LINES_MIN = "quote.content.lines.min";
        public static final String CONTENT_LINES_MAX = "quote.content.lines.max";
    }
    
    /**
     * Rule IDs for sidebar block validation
     */
    public static final class Sidebar {
        public static final String TITLE_REQUIRED = "sidebar.title.required";
        public static final String TITLE_MIN_LENGTH = "sidebar.title.minLength";
        public static final String TITLE_MAX_LENGTH = "sidebar.title.maxLength";
        public static final String TITLE_PATTERN = "sidebar.title.pattern";
        public static final String CONTENT_REQUIRED = "sidebar.content.required";
        public static final String CONTENT_MIN_LENGTH = "sidebar.content.minLength";
        public static final String CONTENT_MAX_LENGTH = "sidebar.content.maxLength";
        public static final String LINES_MIN = "sidebar.lines.min";
        public static final String LINES_MAX = "sidebar.lines.max";
        public static final String POSITION_REQUIRED = "sidebar.position.required";
        public static final String POSITION_ALLOWED = "sidebar.position.allowed";
    }
    
    /**
     * Rule IDs for table block validation
     */
    public static final class Table {
        public static final String COLUMNS_MIN = "table.columns.min";
        public static final String COLUMNS_MAX = "table.columns.max";
        public static final String ROWS_MIN = "table.rows.min";
        public static final String ROWS_MAX = "table.rows.max";
        public static final String HEADER_REQUIRED = "table.header.required";
        public static final String HEADER_PATTERN = "table.header.pattern";
        public static final String CAPTION_REQUIRED = "table.caption.required";
        public static final String CAPTION_PATTERN = "table.caption.pattern";
        public static final String CAPTION_MIN_LENGTH = "table.caption.minLength";
        public static final String CAPTION_MAX_LENGTH = "table.caption.maxLength";
        public static final String FORMAT_STYLE = "table.format.style";
        public static final String FORMAT_BORDERS = "table.format.borders";
    }
    
    /**
     * Rule IDs for unordered list validation
     */
    public static final class Ulist {
        public static final String ITEMS_MIN = "ulist.items.min";
        public static final String ITEMS_MAX = "ulist.items.max";
        public static final String NESTING_LEVEL_MAX = "ulist.nestingLevel.max";
        public static final String MARKER_STYLE = "ulist.markerStyle";
    }
    
    /**
     * Rule IDs for verse block validation
     */
    public static final class Verse {
        public static final String AUTHOR_REQUIRED = "verse.author.required";
        public static final String AUTHOR_MIN_LENGTH = "verse.author.minLength";
        public static final String AUTHOR_MAX_LENGTH = "verse.author.maxLength";
        public static final String AUTHOR_PATTERN = "verse.author.pattern";
        public static final String ATTRIBUTION_REQUIRED = "verse.attribution.required";
        public static final String ATTRIBUTION_MIN_LENGTH = "verse.attribution.minLength";
        public static final String ATTRIBUTION_MAX_LENGTH = "verse.attribution.maxLength";
        public static final String ATTRIBUTION_PATTERN = "verse.attribution.pattern";
        public static final String CONTENT_REQUIRED = "verse.content.required";
        public static final String CONTENT_MIN_LENGTH = "verse.content.minLength";
        public static final String CONTENT_MAX_LENGTH = "verse.content.maxLength";
        public static final String CONTENT_PATTERN = "verse.content.pattern";
    }
    
    /**
     * Rule IDs for video block validation
     */
    public static final class Video {
        public static final String URL_REQUIRED = "video.url.required";
        public static final String URL_PATTERN = "video.url.pattern";
        public static final String WIDTH_REQUIRED = "video.width.required";
        public static final String WIDTH_MIN = "video.width.min";
        public static final String WIDTH_MAX = "video.width.max";
        public static final String WIDTH_INVALID = "video.width.invalid";
        public static final String HEIGHT_REQUIRED = "video.height.required";
        public static final String HEIGHT_MIN = "video.height.min";
        public static final String HEIGHT_MAX = "video.height.max";
        public static final String HEIGHT_INVALID = "video.height.invalid";
        public static final String POSTER_REQUIRED = "video.poster.required";
        public static final String POSTER_PATTERN = "video.poster.pattern";
        public static final String CONTROLS_REQUIRED = "video.controls.required";
        public static final String CAPTION_REQUIRED = "video.caption.required";
        public static final String CAPTION_MIN_LENGTH = "video.caption.minLength";
        public static final String CAPTION_MAX_LENGTH = "video.caption.maxLength";
        public static final String CAPTION_PATTERN = "video.caption.pattern";
    }
}