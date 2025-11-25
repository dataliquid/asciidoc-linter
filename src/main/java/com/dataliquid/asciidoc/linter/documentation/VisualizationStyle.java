package com.dataliquid.asciidoc.linter.documentation;

/**
 * Available visualization styles for rule hierarchy documentation.
 */
public enum VisualizationStyle {
    TREE("tree", "ASCII-art tree structure"), NESTED("nested", "Nested lists with severity indicators"),
    BREADCRUMB("breadcrumb", "Path-style representation"), TABLE("table", "Hierarchical table with indentation"),
    PLANTUML("plantuml", "PlantUML diagram"), SEVERITY_FLOW("severity-flow", "Severity inheritance visualization");

    private final String name;
    private final String description;

    VisualizationStyle(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static VisualizationStyle fromName(String name) {
        for (VisualizationStyle style : values()) {
            if (style.name.equalsIgnoreCase(name)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Unknown visualization style: " + name);
    }
}
