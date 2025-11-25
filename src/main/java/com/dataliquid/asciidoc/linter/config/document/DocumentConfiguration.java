package com.dataliquid.asciidoc.linter.config.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.METADATA;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.SECTIONS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = DocumentConfiguration.Builder.class)
public final class DocumentConfiguration {
    private final MetadataConfiguration _metadata;
    private final List<SectionConfig> _sections;

    private DocumentConfiguration(Builder builder) {
        this._metadata = builder._metadata;
        this._sections = Collections.unmodifiableList(new ArrayList<>(builder._sections));
    }

    @JsonProperty(METADATA)
    public MetadataConfiguration metadata() {
        return this._metadata;
    }

    @JsonProperty(SECTIONS)
    public List<SectionConfig> sections() {
        return this._sections;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private MetadataConfiguration _metadata;
        private List<SectionConfig> _sections = new ArrayList<>();

        @JsonProperty(METADATA)
        public Builder metadata(MetadataConfiguration metadata) {
            this._metadata = metadata;
            return this;
        }

        @JsonProperty(SECTIONS)
        public Builder sections(List<SectionConfig> sections) {
            this._sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
            return this;
        }

        public Builder addSection(SectionConfig section) {
            this._sections.add(section);
            return this;
        }

        public DocumentConfiguration build() {
            return new DocumentConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocumentConfiguration that = (DocumentConfiguration) o;
        return Objects.equals(_metadata, that._metadata) && Objects.equals(_sections, that._sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_metadata, _sections);
    }
}
