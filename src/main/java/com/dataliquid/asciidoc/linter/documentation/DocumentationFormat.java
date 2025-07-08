package com.dataliquid.asciidoc.linter.documentation;

/**
 * Supported documentation output formats.
 */
public enum DocumentationFormat {
    ASCIIDOC(".adoc", "AsciiDoc", "text/asciidoc"),
    MARKDOWN(".md", "Markdown", "text/markdown"),
    HTML(".html", "HTML", "text/html");
    
    private final String extension;
    private final String displayName;
    private final String mimeType;
    
    DocumentationFormat(String extension, String displayName, String mimeType) {
        this.extension = extension;
        this.displayName = displayName;
        this.mimeType = mimeType;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getMimeType() {
        return mimeType;
    }
}