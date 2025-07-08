package com.dataliquid.asciidoc.linter.documentation.visualizers;

import java.io.PrintWriter;
import java.util.List;

import com.dataliquid.asciidoc.linter.config.DocumentConfiguration;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.MetadataConfiguration;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.documentation.HierarchyVisualizer;
import com.dataliquid.asciidoc.linter.documentation.VisualizationStyle;

/**
 * Visualizes rule hierarchy as nested lists with severity indicators.
 */
public class NestedListVisualizer implements HierarchyVisualizer {
    
    @Override
    public void visualize(LinterConfiguration config, PrintWriter writer) {
        writer.println("=== icon:file-alt[] Document Level");
        writer.println();
        
        DocumentConfiguration doc = config.document();
        if (doc.metadata() != null) {
            visualizeMetadata(doc.metadata(), writer);
        }
        
        if (doc.sections() != null && !doc.sections().isEmpty()) {
            visualizeSections(doc.sections(), "", writer);
        }
    }
    
    @Override
    public VisualizationStyle getStyle() {
        return VisualizationStyle.NESTED;
    }
    
    private void visualizeMetadata(MetadataConfiguration metadata, PrintWriter writer) {
        writer.println("* " + getSeverityIcon(Severity.ERROR) + " **metadata** _(Required)_");
        
        if (metadata.attributes() != null) {
            for (AttributeConfig attr : metadata.attributes()) {
                String icon = getSeverityIcon(attr.severity());
                String required = attr.required() ? "Required" : "Optional";
                
                writer.println("** " + icon + " **" + attr.name() + "** - " + getAttributeDescription(attr));
                writer.println("*** " + required);
                
                if (attr.minLength() != null || attr.maxLength() != null) {
                    writer.print("*** ");
                    if (attr.minLength() != null) {
                        writer.print("Min: " + attr.minLength());
                    }
                    if (attr.minLength() != null && attr.maxLength() != null) {
                        writer.print(", ");
                    }
                    if (attr.maxLength() != null) {
                        writer.print("Max: " + attr.maxLength());
                    }
                    writer.println(" characters");
                }
                
                if (attr.pattern() != null) {
                    writer.println("*** Pattern: " + attr.pattern());
                }
            }
        }
        writer.println();
    }
    
    private void visualizeSections(List<SectionConfig> sections, String indent, 
                                  PrintWriter writer) {
        writer.println(indent + "* icon:folder[] **sections** _(Required)_");
        
        for (SectionConfig section : sections) {
            visualizeSection(section, indent + "*", writer);
        }
    }
    
    private void visualizeSection(SectionConfig section, String indent, PrintWriter writer) {
        String required = section.min() > 0 ? 
            getSeverityIcon(Severity.ERROR) : getSeverityIcon(Severity.INFO);
        
        writer.println(indent + " " + required + " **" + section.name() + "** - " + 
                      getSectionDescription(section));
        writer.println(indent + "* Level: " + section.level());
        
        if (section.order() != null) {
            writer.println(indent + "* Position: " + section.order());
        }
        
        if (section.allowedBlocks() != null && !section.allowedBlocks().isEmpty()) {
            writer.println(indent + "* icon:cubes[] **allowedBlocks**");
            for (Block block : section.allowedBlocks()) {
                visualizeBlock(block, indent + "**", writer);
            }
        }
        
        if (section.subsections() != null && !section.subsections().isEmpty()) {
            writer.println(indent + "* **subsections**");
            for (SectionConfig subsection : section.subsections()) {
                visualizeSection(subsection, indent + "*", writer);
            }
        }
    }
    
    private void visualizeBlock(Block block, String indent, PrintWriter writer) {
        String blockIcon = getBlockIcon(block);
        
        writer.print(indent + " " + blockIcon + " **" + block.getType().toValue() + "**");
        if (block.getName() != null) {
            writer.print(" (" + block.getName() + ")");
        }
        
        if (block.getOccurrence() != null) {
            writer.print(" (");
            int min = block.getOccurrence().min();
            int max = block.getOccurrence().max();
            if (min > 0 && max < Integer.MAX_VALUE) {
                writer.print(min + "-" + max + " items");
            } else if (min > 0) {
                writer.print("min. " + min + " items");
            } else if (max < Integer.MAX_VALUE) {
                writer.print("max. " + max + " items");
            }
            writer.print(")");
        }
        writer.println();
        
        // Add block-specific rules
        visualizeBlockRules(block, indent + "*", writer);
    }
    
    private void visualizeBlockRules(Block block, String indent, PrintWriter writer) {
        // TODO: Add specific rules based on block type
        // For now, just show severity if it differs from default
        writer.println(indent + " " + getSeverityIcon(block.getSeverity()) + 
                      " Severity: " + block.getSeverity());
    }
    
    private String getSeverityIcon(Severity severity) {
        return switch (severity) {
            case ERROR -> "icon:times-circle[role=\"red\"]";
            case WARN -> "icon:exclamation-triangle[role=\"yellow\"]";
            case INFO -> "icon:info-circle[role=\"blue\"]";
        };
    }
    
    private String getBlockIcon(Block block) {
        return switch (block.getType()) {
            case PARAGRAPH -> "icon:paragraph[]";
            case LISTING -> "icon:code[]";
            case TABLE -> "icon:table[]";
            case IMAGE -> "icon:image[]";
            case VERSE -> "icon:quote-left[]";
            case ADMONITION -> "icon:exclamation[]";
            case PASS -> "icon:forward[]";
            case LITERAL -> "icon:file-code[]";
            default -> "icon:file[]";
        };
    }
    
    private String getAttributeDescription(AttributeConfig attr) {
        return switch (attr.name()) {
            case "title" -> "Document title";
            case "author" -> "Author";
            case "version" -> "Version number";
            case "email" -> "Contact email";
            default -> attr.name();
        };
    }
    
    private String getSectionDescription(SectionConfig section) {
        return switch (section.name()) {
            case "introduction" -> "Introduction section";
            case "implementation" -> "Implementation details";
            case "conclusion" -> "Summary";
            default -> section.name();
        };
    }
}