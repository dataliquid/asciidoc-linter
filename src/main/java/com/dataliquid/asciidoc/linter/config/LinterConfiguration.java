package com.dataliquid.asciidoc.linter.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.DOCUMENT_CONFIG;

public final class LinterConfiguration {
    private final DocumentConfiguration documentValue;

    @JsonCreator
    public LinterConfiguration(@JsonProperty(DOCUMENT_CONFIG) DocumentConfiguration document) {
        this.documentValue = document;
    }

    @JsonProperty(DOCUMENT_CONFIG)
    public DocumentConfiguration document() {
        return documentValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LinterConfiguration that = (LinterConfiguration) o;
        return Objects.equals(document(), that.document());
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) document());
    }
}