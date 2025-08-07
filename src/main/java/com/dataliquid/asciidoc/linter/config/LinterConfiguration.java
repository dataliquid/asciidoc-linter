package com.dataliquid.asciidoc.linter.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.Document.DOCUMENT;
import static com.dataliquid.asciidoc.linter.config.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = LinterConfiguration.Builder.class)
public final class LinterConfiguration {
    private final DocumentConfiguration document;

    private LinterConfiguration(Builder builder) {
        this.document = builder.document;
    }

    @JsonProperty(DOCUMENT)
    public DocumentConfiguration document() { return document; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private DocumentConfiguration document;

        @JsonProperty(DOCUMENT)
        public Builder document(DocumentConfiguration document) {
            this.document = document;
            return this;
        }

        public LinterConfiguration build() {
            // Allow empty configuration
            return new LinterConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinterConfiguration that = (LinterConfiguration) o;
        return Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document);
    }
}