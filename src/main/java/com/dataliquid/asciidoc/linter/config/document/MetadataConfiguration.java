package com.dataliquid.asciidoc.linter.config.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.rule.AttributeConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Document.ATTRIBUTES;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = MetadataConfiguration.Builder.class)
public final class MetadataConfiguration {
    private final List<AttributeConfig> attributes;

    private MetadataConfiguration(Builder builder) {
        this.attributes = Collections.unmodifiableList(new ArrayList<>(builder.attributes));
    }

    @JsonProperty(ATTRIBUTES)
    public List<AttributeConfig> attributes() { 
        return attributes; 
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder {
        private List<AttributeConfig> attributes = new ArrayList<>();

        @JsonProperty(ATTRIBUTES)
        public Builder attributes(List<AttributeConfig> attributes) {
            this.attributes = attributes != null ? new ArrayList<>(attributes) : new ArrayList<>();
            return this;
        }

        public Builder addAttribute(AttributeConfig attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public MetadataConfiguration build() {
            return new MetadataConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataConfiguration that = (MetadataConfiguration) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }
}