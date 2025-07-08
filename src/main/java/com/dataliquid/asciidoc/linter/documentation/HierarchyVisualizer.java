package com.dataliquid.asciidoc.linter.documentation;

import java.io.PrintWriter;

import com.dataliquid.asciidoc.linter.config.LinterConfiguration;

/**
 * Interface for visualizing rule hierarchies in documentation.
 */
public interface HierarchyVisualizer {
    
    /**
     * Visualizes the rule hierarchy from the given configuration.
     * 
     * @param config the linter configuration
     * @param writer the output writer
     */
    void visualize(LinterConfiguration config, PrintWriter writer);
    
    /**
     * Returns the visualization style this visualizer implements.
     * 
     * @return the visualization style
     */
    VisualizationStyle getStyle();
}