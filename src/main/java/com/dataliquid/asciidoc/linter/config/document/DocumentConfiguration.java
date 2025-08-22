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
    private final MetadataConfiguration metadata;
    private final List<SectionConfig> sections;

    private DocumentConfiguration(Builder builder) {
        this.metadata = builder.metadata;
        this.sections = Collections.unmodifiableList(new ArrayList<>(builder.sections));
    }

    @JsonProperty(METADATA)
    public MetadataConfiguration metadata() {
        return metadata;
    }

    @JsonProperty(SECTIONS)
    public List<SectionConfig> sections() {
        return sections;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private MetadataConfiguration metadata;
        private List<SectionConfig> sections = new ArrayList<>();

        @JsonProperty(METADATA)
        public Builder metadata(MetadataConfiguration metadata) {
            this.metadata = metadata;
            return this;
        }

        @JsonProperty(SECTIONS)
        public Builder sections(List<SectionConfig> sections) {
            this.sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
            return this;
        }

        public Builder addSection(SectionConfig section) {
            this.sections.add(section);
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
        return Objects.equals(metadata, that.metadata) && Objects.equals(sections, that.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, sections);
    }
}
