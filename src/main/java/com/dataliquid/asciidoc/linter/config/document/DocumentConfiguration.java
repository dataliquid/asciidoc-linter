package com.dataliquid.asciidoc.linter.config.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.METADATA;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.SECTIONS;

public final class DocumentConfiguration {
    private final MetadataConfiguration metadataValue;
    private final List<SectionConfig> sectionsValue;

    @JsonCreator
    public DocumentConfiguration(@JsonProperty(METADATA) MetadataConfiguration metadata,
            @JsonProperty(SECTIONS) List<SectionConfig> sections) {
        this.metadataValue = metadata;
        this.sectionsValue = sections != null ? Collections.unmodifiableList(new ArrayList<>(sections))
                : Collections.emptyList();
    }

    @JsonProperty(METADATA)
    public MetadataConfiguration metadata() {
        return this.metadataValue;
    }

    @JsonProperty(SECTIONS)
    public List<SectionConfig> sections() {
        return this.sectionsValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocumentConfiguration that = (DocumentConfiguration) o;
        return Objects.equals(metadataValue, that.metadataValue) && Objects.equals(sectionsValue, that.sectionsValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadataValue, sectionsValue);
    }
}