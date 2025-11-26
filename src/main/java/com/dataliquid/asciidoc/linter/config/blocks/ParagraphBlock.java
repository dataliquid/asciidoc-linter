package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Paragraph.*;

@JsonDeserialize
public final class ParagraphBlock extends AbstractBlock {
    @JsonProperty(LINES)
    private final LineConfig lines;

    @JsonProperty(SENTENCE)
    private final SentenceConfig sentence;

    @JsonCreator
    public ParagraphBlock(@JsonProperty("name") String name, @JsonProperty("severity") Severity severity,
            @JsonProperty("occurrence") OccurrenceConfig occurrence, @JsonProperty("order") Integer order,
            @JsonProperty("lines") LineConfig lines, @JsonProperty("sentence") SentenceConfig sentence) {
        super(name, severity, occurrence, order);
        this.lines = lines;
        this.sentence = sentence;
    }

    @Override
    public BlockType getType() {
        return BlockType.PARAGRAPH;
    }

    public LineConfig getLines() {
        return lines;
    }

    public SentenceConfig getSentence() {
        return sentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ParagraphBlock that = (ParagraphBlock) o;
        return Objects.equals(lines, that.lines) && Objects.equals(sentence, that.sentence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lines, sentence);
    }

    /**
     * Configuration for sentence-level validation in paragraph blocks.
     */
    @JsonDeserialize
    public static final class SentenceConfig {
        @JsonProperty(OCCURRENCE)
        private final OccurrenceConfig occurrence;

        @JsonProperty(WORDS)
        private final WordsConfig words;

        @JsonCreator
        public SentenceConfig(@JsonProperty("occurrence") OccurrenceConfig occurrence,
                @JsonProperty("words") WordsConfig words) {
            this.occurrence = occurrence;
            this.words = words;
        }

        public OccurrenceConfig getOccurrence() {
            return occurrence;
        }

        public WordsConfig getWords() {
            return words;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            SentenceConfig that = (SentenceConfig) o;
            return Objects.equals(occurrence, that.occurrence) && Objects.equals(words, that.words);
        }

        @Override
        public int hashCode() {
            return Objects.hash(occurrence, words);
        }
    }

    /**
     * Configuration for word count validation in sentences.
     */
    @JsonDeserialize
    public static final class WordsConfig {
        @JsonProperty(MIN)
        private final Integer min;

        @JsonProperty(MAX)
        private final Integer max;

        @JsonProperty(SEVERITY)
        private final Severity severity;

        @JsonCreator
        public WordsConfig(@JsonProperty("min") Integer min, @JsonProperty("max") Integer max,
                @JsonProperty("severity") Severity severity) {
            this.min = min;
            this.max = max;
            this.severity = severity;
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            WordsConfig that = (WordsConfig) o;
            return Objects.equals(min, that.min) && Objects.equals(max, that.max) && severity == that.severity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }
}
