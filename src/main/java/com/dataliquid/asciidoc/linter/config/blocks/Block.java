package com.dataliquid.asciidoc.linter.config.blocks;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface Block {
    @JsonProperty("type")
    BlockType getType();
    
    @JsonProperty("name")
    String getName();
    
    @JsonProperty("severity")
    Severity getSeverity();
    
    @JsonProperty("occurrence")
    OccurrenceConfig getOccurrence();
    
    @JsonProperty("order")
    Integer getOrder();
}