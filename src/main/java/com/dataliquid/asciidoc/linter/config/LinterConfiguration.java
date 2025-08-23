package com.dataliquid.asciidoc.linter.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import com.dataliquid.asciidoc.linter.config.document.DocumentConfiguration;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.DOCUMENT_CONFIG;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = LinterConfiguration.Builder.class)
public final class LinterConfiguration {
    private final DocumentConfiguration _document;

    private LinterConfiguration(Builder builder) {
        this._document = builder._document;
    }

    @JsonProperty(DOCUMENT_CONFIG)
    public DocumentConfiguration document() {
        return _document;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private DocumentConfiguration _document;

        @JsonProperty(DOCUMENT_CONFIG)
        public Builder document(DocumentConfiguration document) {
            this._document = document;
            return this;
        }

        public LinterConfiguration build() {
            // Allow empty configuration
            return new LinterConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LinterConfiguration that = (LinterConfiguration) o;
        return Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) document);
    }
}
