package com.dataliquid.asciidoc.linter.config.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.ATTRIBUTES;

public final class MetadataConfiguration {
    private final List<AttributeConfig> attributesValue;

    @JsonCreator
    public MetadataConfiguration(@JsonProperty(ATTRIBUTES) List<AttributeConfig> attributes) {
        this.attributesValue = attributes != null ? Collections.unmodifiableList(new ArrayList<>(attributes))
                : Collections.emptyList();
    }

    @JsonProperty(ATTRIBUTES)
    public List<AttributeConfig> attributes() {
        return this.attributesValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MetadataConfiguration that = (MetadataConfiguration) o;
        return Objects.equals(attributesValue, that.attributesValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributesValue);
    }
}