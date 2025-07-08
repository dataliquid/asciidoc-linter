package com.dataliquid.asciidoc.linter.config.blocks;

import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;
import com.dataliquid.asciidoc.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ParagraphBlock.Builder.class)
public final class ParagraphBlock extends AbstractBlock {
    @JsonProperty("lines")
    private final LineConfig lines;
    
    @JsonProperty("sentence")
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
    
    @JsonPOJOBuilder(withPrefix = "")
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
        @JsonProperty("occurrence")
        private final OccurrenceConfig occurrence;
        
        @JsonProperty("words")
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private OccurrenceConfig occurrence;
            private WordsConfig words;
            
            @JsonProperty("occurrence")
            public Builder occurrence(OccurrenceConfig occurrence) {
                this.occurrence = occurrence;
                return this;
            }
            
            @JsonProperty("words")
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
        @JsonProperty("min")
        private final Integer min;
        
        @JsonProperty("max")
        private final Integer max;
        
        @JsonProperty("severity")
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
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private Integer min;
            private Integer max;
            private Severity severity;
            
            @JsonProperty("min")
            public Builder min(Integer min) {
                this.min = min;
                return this;
            }
            
            @JsonProperty("max")
            public Builder max(Integer max) {
                this.max = max;
                return this;
            }
            
            @JsonProperty("severity")
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