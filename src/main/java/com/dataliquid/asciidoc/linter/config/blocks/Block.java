package com.dataliquid.asciidoc.linter.config.blocks;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.NAME;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.OCCURRENCE;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ORDER;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.SEVERITY;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.TYPE;

public interface Block {
    @JsonProperty(TYPE)
    BlockType getType();
    
    @JsonProperty(NAME)
    String getName();
    
    @JsonProperty(SEVERITY)
    Severity getSeverity();
    
    @JsonProperty(OCCURRENCE)
    OccurrenceConfig getOccurrence();
    
    @JsonProperty(ORDER)
    Integer getOrder();
}