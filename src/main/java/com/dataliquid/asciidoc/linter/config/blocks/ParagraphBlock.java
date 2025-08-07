package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Paragraph.*;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;

@JsonDeserialize(builder = ParagraphBlock.Builder.class)
public final class ParagraphBlock extends AbstractBlock {
    @JsonProperty(LINES)
    private final LineConfig lines;
    
    @JsonProperty(SENTENCE)
    private final SentenceConfig sentence;
    
    private ParagraphBlock(Builder builder) {
        super(builder);
        this.lines = builder.lines;
        this.sentence = builder.sentence;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.PARAGRAPH;
    }
    
    public LineConfig getLines() { return lines; }
    public SentenceConfig getSentence() { return sentence; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static class Builder extends AbstractBuilder<Builder> {
        private LineConfig lines;
        private SentenceConfig sentence;
        
        public Builder lines(LineConfig lines) {
            this.lines = lines;
            return this;
        }
        
        public Builder sentence(SentenceConfig sentence) {
            this.sentence = sentence;
            return this;
        }
        
        @Override
        public ParagraphBlock build() {
            Objects.requireNonNull(severity, "[" + getClass().getName() + "] severity is required");
            return new ParagraphBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParagraphBlock that = (ParagraphBlock) o;
        return Objects.equals(lines, that.lines) &&
               Objects.equals(sentence, that.sentence);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lines, sentence);
    }
    
    /**
     * Configuration for sentence-level validation in paragraph blocks.
     */
    @JsonDeserialize(builder = SentenceConfig.Builder.class)
    public static final class SentenceConfig {
        @JsonProperty(OCCURRENCE)
        private final OccurrenceConfig occurrence;
        
        @JsonProperty(WORDS)
        private final WordsConfig words;
        
        private SentenceConfig(Builder builder) {
            this.occurrence = builder.occurrence;
            this.words = builder.words;
        }
        
        public OccurrenceConfig getOccurrence() { return occurrence; }
        public WordsConfig getWords() { return words; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private OccurrenceConfig occurrence;
            private WordsConfig words;
            
            @JsonProperty(OCCURRENCE)
            public Builder occurrence(OccurrenceConfig occurrence) {
                this.occurrence = occurrence;
                return this;
            }
            
            @JsonProperty(WORDS)
            public Builder words(WordsConfig words) {
                this.words = words;
                return this;
            }
            
            public SentenceConfig build() {
                return new SentenceConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SentenceConfig that = (SentenceConfig) o;
            return Objects.equals(occurrence, that.occurrence) &&
                   Objects.equals(words, that.words);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(occurrence, words);
        }
    }
    
    /**
     * Configuration for word count validation in sentences.
     */
    @JsonDeserialize(builder = WordsConfig.Builder.class)
    public static final class WordsConfig {
        @JsonProperty(MIN)
        private final Integer min;
        
        @JsonProperty(MAX)
        private final Integer max;
        
        @JsonProperty(SEVERITY)
        private final Severity severity;
        
        private WordsConfig(Builder builder) {
            this.min = builder.min;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        public Integer getMin() { return min; }
        public Integer getMax() { return max; }
        public Severity getSeverity() { return severity; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        @JsonPOJOBuilder(withPrefix = EMPTY)
        public static class Builder {
            private Integer min;
            private Integer max;
            private Severity severity;
            
            @JsonProperty(MIN)
            public Builder min(Integer min) {
                this.min = min;
                return this;
            }
            
            @JsonProperty(MAX)
            public Builder max(Integer max) {
                this.max = max;
                return this;
            }
            
            @JsonProperty(SEVERITY)
            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public WordsConfig build() {
                return new WordsConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WordsConfig that = (WordsConfig) o;
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }
}