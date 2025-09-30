package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.rule.LineConfig;

@DisplayName("ListingBlock")
class ListingBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build ListingBlock with all attributes")
        void shouldBuildListingBlockWithAllAttributes() {
            // Given
            ListingBlock.LanguageConfig languageRule = new ListingBlock.LanguageConfig(true,
                    Arrays.asList("java", "python", "javascript", "yaml"), Severity.ERROR);

            LineConfig linesRule = new LineConfig(3, 100, null);

            ListingBlock.TitleConfig titleRule = new ListingBlock.TitleConfig(true,
                    "^(Example|Beispiel|Listing)\\s+\\d+", Severity.WARN);

            ListingBlock.CalloutsConfig calloutsRule = new ListingBlock.CalloutsConfig(true, 10, Severity.INFO);

            // When
            ListingBlock listing = new ListingBlock("api-examples", Severity.ERROR, null, null, languageRule, linesRule,
                    titleRule, calloutsRule);

            // Then
            assertEquals("api-examples", listing.getName());
            assertEquals(Severity.ERROR, listing.getSeverity());

            assertNotNull(listing.getLanguage());
            assertTrue(listing.getLanguage().isRequired());
            assertEquals(Arrays.asList("java", "python", "javascript", "yaml"), listing.getLanguage().getAllowed());
            assertEquals(Severity.ERROR, listing.getLanguage().getSeverity());

            assertNotNull(listing.getLines());
            assertEquals(3, listing.getLines().min());
            assertEquals(100, listing.getLines().max());

            assertNotNull(listing.getTitle());
            assertTrue(listing.getTitle().isRequired());
            assertNotNull(listing.getTitle().getPattern());
            assertEquals(Severity.WARN, listing.getTitle().getSeverity());

            assertNotNull(listing.getCallouts());
            assertTrue(listing.getCallouts().isAllowed());
            assertEquals(10, listing.getCallouts().getMax());
            assertEquals(Severity.INFO, listing.getCallouts().getSeverity());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                new ListingBlock(null, null, null, null, null, null, null, null);
            });
        }
    }

    @Nested
    @DisplayName("LanguageConfig Tests")
    class LanguageConfigTests {

        @Test
        @DisplayName("should create LanguageConfig with allowed languages")
        void shouldCreateLanguageConfigWithAllowedLanguages() {
            // Given
            List<String> allowedLanguages = Arrays.asList("java", "kotlin", "scala");

            // When
            ListingBlock.LanguageConfig languageRule = new ListingBlock.LanguageConfig(true, allowedLanguages,
                    Severity.ERROR);

            // Then
            assertTrue(languageRule.isRequired());
            assertEquals(allowedLanguages, languageRule.getAllowed());
            assertEquals(Severity.ERROR, languageRule.getSeverity());
        }

        @Test
        @DisplayName("should handle empty allowed list")
        void shouldHandleEmptyAllowedList() {
            // When
            ListingBlock.LanguageConfig languageRule = new ListingBlock.LanguageConfig(false, null, Severity.WARN);

            // Then
            assertFalse(languageRule.isRequired());
            assertTrue(languageRule.getAllowed().isEmpty());
        }

    }

    @Nested
    @DisplayName("TitleConfig Tests")
    class TitleConfigTests {

        @Test
        @DisplayName("should create TitleConfig with string pattern")
        void shouldCreateTitleConfigWithStringPattern() {
            // Given & When
            ListingBlock.TitleConfig titleRule = new ListingBlock.TitleConfig(true, "^Listing \\d+:", Severity.ERROR);

            // Then
            assertTrue(titleRule.isRequired());
            assertNotNull(titleRule.getPattern());
            assertEquals("^Listing \\d+:", titleRule.getPattern().pattern());
            assertEquals(Severity.ERROR, titleRule.getSeverity());
        }

        @Test
        @DisplayName("should create TitleConfig with Pattern object")
        void shouldCreateTitleConfigWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^Example.*");

            // When
            ListingBlock.TitleConfig titleRule = new ListingBlock.TitleConfig(false, pattern.pattern(), Severity.WARN);

            // Then
            assertFalse(titleRule.isRequired());
            assertEquals(pattern.pattern(), titleRule.getPattern().pattern());
            assertEquals(Severity.WARN, titleRule.getSeverity());
        }

    }

    @Nested
    @DisplayName("CalloutsConfig Tests")
    class CalloutsConfigTests {

        @Test
        @DisplayName("should create CalloutsConfig with max limit")
        void shouldCreateCalloutsConfigWithMaxLimit() {
            // Given & When
            ListingBlock.CalloutsConfig calloutsRule = new ListingBlock.CalloutsConfig(true, 15, Severity.INFO);

            // Then
            assertTrue(calloutsRule.isAllowed());
            assertEquals(15, calloutsRule.getMax());
            assertEquals(Severity.INFO, calloutsRule.getSeverity());
        }

        @Test
        @DisplayName("should create CalloutsConfig without max limit")
        void shouldCreateCalloutsConfigWithoutMaxLimit() {
            // Given & When
            ListingBlock.CalloutsConfig calloutsRule = new ListingBlock.CalloutsConfig(false, null, Severity.ERROR);

            // Then
            assertFalse(calloutsRule.isAllowed());
            assertNull(calloutsRule.getMax());
            assertEquals(Severity.ERROR, calloutsRule.getSeverity());
        }

    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            ListingBlock.LanguageConfig lang1 = new ListingBlock.LanguageConfig(true, Arrays.asList("java", "python"),
                    Severity.ERROR);

            ListingBlock.LanguageConfig lang2 = new ListingBlock.LanguageConfig(true, Arrays.asList("java", "python"),
                    Severity.ERROR);

            ListingBlock.TitleConfig title1 = new ListingBlock.TitleConfig(true, "^Example.*", Severity.WARN);

            ListingBlock.TitleConfig title2 = new ListingBlock.TitleConfig(true, "^Example.*", Severity.WARN);

            // When
            ListingBlock listing1 = new ListingBlock(null, Severity.ERROR, null, null, lang1, null, title1, null);

            ListingBlock listing2 = new ListingBlock(null, Severity.ERROR, null, null, lang2, null, title2, null);

            ListingBlock listing3 = new ListingBlock(null, Severity.WARN, null, null, lang1, null, title1, null);

            // Then
            assertEquals(listing1, listing2);
            assertNotEquals(listing1, listing3);
            assertEquals(listing1.hashCode(), listing2.hashCode());
            assertNotEquals(listing1.hashCode(), listing3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            ListingBlock.LanguageConfig lang1 = new ListingBlock.LanguageConfig(true, Arrays.asList("java"),
                    Severity.ERROR);

            ListingBlock.LanguageConfig lang2 = new ListingBlock.LanguageConfig(true, Arrays.asList("java"),
                    Severity.ERROR);

            ListingBlock.TitleConfig title1 = new ListingBlock.TitleConfig(false, "test", Severity.INFO);

            ListingBlock.TitleConfig title2 = new ListingBlock.TitleConfig(false, "test", Severity.INFO);

            ListingBlock.CalloutsConfig callouts1 = new ListingBlock.CalloutsConfig(true, 5, Severity.WARN);

            ListingBlock.CalloutsConfig callouts2 = new ListingBlock.CalloutsConfig(true, 5, Severity.WARN);

            // Then
            assertEquals(lang1, lang2);
            assertEquals(lang1.hashCode(), lang2.hashCode());

            assertEquals(title1, title2);
            assertEquals(title1.hashCode(), title2.hashCode());

            assertEquals(callouts1, callouts2);
            assertEquals(callouts1.hashCode(), callouts2.hashCode());
        }
    }
}
