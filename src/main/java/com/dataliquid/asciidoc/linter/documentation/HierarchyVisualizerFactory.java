package com.dataliquid.asciidoc.linter.documentation;

import java.util.HashMap;
import java.util.Map;

import com.dataliquid.asciidoc.linter.documentation.visualizers.BreadcrumbVisualizer;
import com.dataliquid.asciidoc.linter.documentation.visualizers.NestedListVisualizer;
import com.dataliquid.asciidoc.linter.documentation.visualizers.TableVisualizer;
import com.dataliquid.asciidoc.linter.documentation.visualizers.TreeVisualizer;

/**
 * Factory for creating hierarchy visualizers.
 */
public class HierarchyVisualizerFactory {

    private final Map<VisualizationStyle, HierarchyVisualizer> visualizers;

    public HierarchyVisualizerFactory() {
        this.visualizers = new HashMap<>();
        registerVisualizers();
    }

    private void registerVisualizers() {
        visualizers.put(VisualizationStyle.TREE, new TreeVisualizer());
        visualizers.put(VisualizationStyle.NESTED, new NestedListVisualizer());
        visualizers.put(VisualizationStyle.BREADCRUMB, new BreadcrumbVisualizer());
        visualizers.put(VisualizationStyle.TABLE, new TableVisualizer());
    }

    /**
     * Creates a visualizer for the specified style.
     *
     * @param  style                    the visualization style
     *
     * @return                          the visualizer
     *
     * @throws IllegalArgumentException if no visualizer exists for the style
     */
    public HierarchyVisualizer create(VisualizationStyle style) {
        HierarchyVisualizer visualizer = visualizers.get(style);
        if (visualizer == null) {
            throw new IllegalArgumentException("No visualizer available for style: " + style);
        }
        return visualizer;
    }

    /**
     * Checks if a visualizer is available for the specified style.
     *
     * @param  style the visualization style
     *
     * @return       true if a visualizer exists, false otherwise
     */
    public boolean hasVisualizer(VisualizationStyle style) {
        return visualizers.containsKey(style);
    }
}
