package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

class VerseBlockTest {

    @Test
    void testBuilder() {
        VerseBlock.AuthorConfig authorRule = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Carl Sandburg")
                .required(true)
                .minLength(3)
                .maxLength(50)
                .pattern("^[A-Z][a-zA-Z\\s\\.]+$")
                .build();

        VerseBlock.AttributionConfig attributionRule = VerseBlock.AttributionConfig
                .builder()
                .defaultValue("Fog")
                .required(false)
                .minLength(5)
                .maxLength(100)
                .pattern("^[A-Za-z0-9\\s,\\.]+$")
                .build();

        VerseBlock.ContentConfig contentRule = VerseBlock.ContentConfig
                .builder()
                .required(true)
                .minLength(20)
                .maxLength(500)
                .pattern(".*\\n.*")
                .build();

        VerseBlock verse = VerseBlock
                .builder()
                .severity(Severity.WARN)
                .author(authorRule)
                .attribution(attributionRule)
                .content(contentRule)
                .build();

        assertEquals(Severity.WARN, verse.getSeverity());

        assertNotNull(verse.getAuthor());
        assertEquals("Carl Sandburg", verse.getAuthor().getDefaultValue());
        assertTrue(verse.getAuthor().isRequired());
        assertEquals(3, verse.getAuthor().getMinLength());
        assertEquals(50, verse.getAuthor().getMaxLength());
        assertNotNull(verse.getAuthor().getPattern());

        assertNotNull(verse.getAttribution());
        assertEquals("Fog", verse.getAttribution().getDefaultValue());
        assertFalse(verse.getAttribution().isRequired());
        assertEquals(5, verse.getAttribution().getMinLength());
        assertEquals(100, verse.getAttribution().getMaxLength());
        assertNotNull(verse.getAttribution().getPattern());

        assertNotNull(verse.getContent());
        assertTrue(verse.getContent().isRequired());
        assertEquals(20, verse.getContent().getMinLength());
        assertEquals(500, verse.getContent().getMaxLength());
        assertNotNull(verse.getContent().getPattern());
    }

    @Test
    void testPatternStringConstructor() {
        VerseBlock.AuthorConfig authorRule = VerseBlock.AuthorConfig.builder().pattern("^[A-Z].*").build();

        VerseBlock.AttributionConfig attributionRule = VerseBlock.AttributionConfig
                .builder()
                .pattern(Pattern.compile("[0-9]+"))
                .build();

        VerseBlock verse = VerseBlock
                .builder()
                .severity(Severity.ERROR)
                .author(authorRule)
                .attribution(attributionRule)
                .build();

        assertNotNull(verse.getAuthor().getPattern());
        assertEquals("^[A-Z].*", verse.getAuthor().getPattern().pattern());
        assertNotNull(verse.getAttribution().getPattern());
        assertEquals("[0-9]+", verse.getAttribution().getPattern().pattern());
    }

    @Test
    void testNullPatterns() {
        VerseBlock.AuthorConfig authorRule = VerseBlock.AuthorConfig.builder().pattern((String) null).build();

        VerseBlock.AttributionConfig attributionRule = VerseBlock.AttributionConfig
                .builder()
                .pattern((Pattern) null)
                .build();

        VerseBlock verse = VerseBlock
                .builder()
                .severity(Severity.INFO)
                .author(authorRule)
                .attribution(attributionRule)
                .build();

        assertNull(verse.getAuthor().getPattern());
        assertNull(verse.getAttribution().getPattern());
    }

    @Test
    void testEqualsAndHashCode() {
        VerseBlock.AuthorConfig authorRule1 = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Author1")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();

        VerseBlock.AuthorConfig authorRule2 = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Author1")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();

        VerseBlock.AuthorConfig authorRule3 = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Author2")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();

        VerseBlock verse1 = VerseBlock.builder().severity(Severity.WARN).author(authorRule1).build();

        VerseBlock verse2 = VerseBlock.builder().severity(Severity.WARN).author(authorRule2).build();

        VerseBlock verse3 = VerseBlock.builder().severity(Severity.WARN).author(authorRule3).build();

        assertEquals(verse1, verse2);
        assertNotEquals(verse1, verse3);
        assertEquals(verse1.hashCode(), verse2.hashCode());
        assertNotEquals(verse1.hashCode(), verse3.hashCode());
    }

    @Test
    void testRequiredSeverity() {
        assertThrows(NullPointerException.class, () -> {
            VerseBlock.builder().build();
        });
    }

    @Test
    void testInnerClassEqualsAndHashCode() {
        VerseBlock.AuthorConfig author1 = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Test")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();

        VerseBlock.AuthorConfig author2 = VerseBlock.AuthorConfig
                .builder()
                .defaultValue("Test")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();

        VerseBlock.AttributionConfig attr1 = VerseBlock.AttributionConfig
                .builder()
                .defaultValue("Source")
                .required(false)
                .build();

        VerseBlock.AttributionConfig attr2 = VerseBlock.AttributionConfig
                .builder()
                .defaultValue("Source")
                .required(false)
                .build();

        VerseBlock.ContentConfig content1 = VerseBlock.ContentConfig.builder().required(true).minLength(10).build();

        VerseBlock.ContentConfig content2 = VerseBlock.ContentConfig.builder().required(true).minLength(10).build();

        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertEquals(content1, content2);
        assertEquals(content1.hashCode(), content2.hashCode());
    }
}
