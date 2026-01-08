package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

class VerseBlockTest {

    @Test
    void testBuilder() {
        // given
        VerseBlock.AuthorConfig authorRule = new VerseBlock.AuthorConfig("Carl Sandburg", 3, 50,
                "^[A-Z][a-zA-Z\\s\\.]+$", true);

        VerseBlock.AttributionConfig attributionRule = new VerseBlock.AttributionConfig("Fog", 5, 100,
                "^[A-Za-z0-9\\s,\\.]+$", false);

        VerseBlock.ContentConfig contentRule = new VerseBlock.ContentConfig(20, 500, ".*\\n.*", true);

        // when
        VerseBlock verse = new VerseBlock(null, Severity.WARN, null, null, authorRule, attributionRule, contentRule);

        // then
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
        // given
        VerseBlock.AuthorConfig authorRule = new VerseBlock.AuthorConfig(null, null, null, "^[A-Z].*", false);
        VerseBlock.AttributionConfig attributionRule = new VerseBlock.AttributionConfig(null, null, null, "[0-9]+",
                false);

        // when
        VerseBlock verse = new VerseBlock(null, Severity.ERROR, null, null, authorRule, attributionRule, null);

        // then
        assertNotNull(verse.getAuthor().getPattern());
        assertEquals("^[A-Z].*", verse.getAuthor().getPattern().pattern());
        assertNotNull(verse.getAttribution().getPattern());
        assertEquals("[0-9]+", verse.getAttribution().getPattern().pattern());
    }

    @Test
    void testNullPatterns() {
        // given
        VerseBlock.AuthorConfig authorRule = new VerseBlock.AuthorConfig(null, null, null, null, false);
        VerseBlock.AttributionConfig attributionRule = new VerseBlock.AttributionConfig(null, null, null, null, false);

        // when
        VerseBlock verse = new VerseBlock(null, Severity.INFO, null, null, authorRule, attributionRule, null);

        // then
        assertNull(verse.getAuthor().getPattern());
        assertNull(verse.getAttribution().getPattern());
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        VerseBlock.AuthorConfig authorRule1 = new VerseBlock.AuthorConfig("Author1", 5, 50, "^[A-Z].*", true);
        VerseBlock.AuthorConfig authorRule2 = new VerseBlock.AuthorConfig("Author1", 5, 50, "^[A-Z].*", true);
        VerseBlock.AuthorConfig authorRule3 = new VerseBlock.AuthorConfig("Author2", 5, 50, "^[A-Z].*", true);

        // when
        VerseBlock verse1 = new VerseBlock(null, Severity.WARN, null, null, authorRule1, null, null);
        VerseBlock verse2 = new VerseBlock(null, Severity.WARN, null, null, authorRule2, null, null);
        VerseBlock verse3 = new VerseBlock(null, Severity.WARN, null, null, authorRule3, null, null);

        // then
        assertEquals(verse1, verse2);
        assertNotEquals(verse1, verse3);
        assertEquals(verse1.hashCode(), verse2.hashCode());
        assertNotEquals(verse1.hashCode(), verse3.hashCode());
    }

    @Test
    void testRequiredSeverity() {
        // given, when, then
        assertThrows(NullPointerException.class, () -> {
            new VerseBlock(null, null, null, null, null, null, null);
        });
    }

    @Test
    void testInnerClassEqualsAndHashCode() {
        // given
        VerseBlock.AuthorConfig author1 = new VerseBlock.AuthorConfig("Test", 5, 50, "^[A-Z].*", true);
        VerseBlock.AuthorConfig author2 = new VerseBlock.AuthorConfig("Test", 5, 50, "^[A-Z].*", true);

        VerseBlock.AttributionConfig attr1 = new VerseBlock.AttributionConfig("Source", null, null, null, false);
        VerseBlock.AttributionConfig attr2 = new VerseBlock.AttributionConfig("Source", null, null, null, false);

        VerseBlock.ContentConfig content1 = new VerseBlock.ContentConfig(10, null, null, true);
        VerseBlock.ContentConfig content2 = new VerseBlock.ContentConfig(10, null, null, true);

        // when, then
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertEquals(content1, content2);
        assertEquals(content1.hashCode(), content2.hashCode());
    }
}
