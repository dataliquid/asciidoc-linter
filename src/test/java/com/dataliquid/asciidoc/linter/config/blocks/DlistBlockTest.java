package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

class DlistBlockTest {

    @Nested
    class BuilderTests {

        @Test
        void shouldBuildDlistBlockWithAllAttributes() {
            // given
            DlistBlock.TermsConfig termsConfig = new DlistBlock.TermsConfig(1, // min
                    20, // max
                    "^[A-Z].*", // pattern
                    3, // minLength
                    50, // maxLength
                    Severity.ERROR); // severity

            DlistBlock.DescriptionsConfig descriptionsConfig = new DlistBlock.DescriptionsConfig(true, // required
                    1, // min
                    5, // max
                    ".*\\.$", // pattern
                    Severity.WARN); // severity

            DlistBlock.NestingLevelConfig nestingConfig = new DlistBlock.NestingLevelConfig(3, // max
                    Severity.INFO); // severity

            DlistBlock.DelimiterStyleConfig delimiterConfig = new DlistBlock.DelimiterStyleConfig(
                    new String[] { "::", ":::" }, // allowedDelimiters
                    true, // consistent
                    Severity.WARN); // severity

            // when
            DlistBlock dlist = new DlistBlock("glossary-list", // name
                    Severity.ERROR, // severity
                    null, // occurrence
                    null, // order
                    termsConfig, // terms
                    descriptionsConfig, // descriptions
                    nestingConfig, // nestingLevel
                    delimiterConfig); // delimiterStyle

            // then
            assertEquals("glossary-list", dlist.getName());
            assertEquals(Severity.ERROR, dlist.getSeverity());

            assertNotNull(dlist.getTerms());
            assertEquals(1, dlist.getTerms().getMin());
            assertEquals(20, dlist.getTerms().getMax());
            assertEquals("^[A-Z].*", dlist.getTerms().getPattern());
            assertEquals(3, dlist.getTerms().getMinLength());
            assertEquals(50, dlist.getTerms().getMaxLength());
            assertEquals(Severity.ERROR, dlist.getTerms().getSeverity());

            assertNotNull(dlist.getDescriptions());
            assertTrue(dlist.getDescriptions().getRequired());
            assertEquals(1, dlist.getDescriptions().getMin());
            assertEquals(5, dlist.getDescriptions().getMax());
            assertEquals(".*\\.$", dlist.getDescriptions().getPattern());
            assertEquals(Severity.WARN, dlist.getDescriptions().getSeverity());

            assertNotNull(dlist.getNestingLevel());
            assertEquals(3, dlist.getNestingLevel().getMax());
            assertEquals(Severity.INFO, dlist.getNestingLevel().getSeverity());

            assertNotNull(dlist.getDelimiterStyle());
            assertArrayEquals(new String[] { "::", ":::" }, dlist.getDelimiterStyle().getAllowedDelimiters());
            assertTrue(dlist.getDelimiterStyle().getConsistent());
            assertEquals(Severity.WARN, dlist.getDelimiterStyle().getSeverity());
        }

        @Test
        void shouldBuildDlistBlockWithMinimalAttributes() {
            // when
            DlistBlock dlist = new DlistBlock(null, // name
                    Severity.WARN, // severity
                    null, // occurrence
                    null, // order
                    null, // terms
                    null, // descriptions
                    null, // nestingLevel
                    null); // delimiterStyle

            // then
            assertNull(dlist.getName());
            assertEquals(Severity.WARN, dlist.getSeverity());
            assertNull(dlist.getTerms());
            assertNull(dlist.getDescriptions());
            assertNull(dlist.getNestingLevel());
            assertNull(dlist.getDelimiterStyle());
        }

        @Test
        void shouldRequireSeverity() {
            // when & then
            assertThrows(NullPointerException.class, () -> {
                new DlistBlock(null, null, null, null, null, null, null, null);
            });
        }
    }

    @Nested
    class TermsConfigTests {

        @Test
        void shouldCreateTermsConfigWithAllProperties() {
            // when
            DlistBlock.TermsConfig termsConfig = new DlistBlock.TermsConfig(2, // min
                    10, // max
                    "^[A-Z].*", // pattern
                    5, // minLength
                    100, // maxLength
                    Severity.ERROR); // severity

            // then
            assertEquals(2, termsConfig.getMin());
            assertEquals(10, termsConfig.getMax());
            assertEquals("^[A-Z].*", termsConfig.getPattern());
            assertEquals(5, termsConfig.getMinLength());
            assertEquals(100, termsConfig.getMaxLength());
            assertEquals(Severity.ERROR, termsConfig.getSeverity());
        }

        @Test
        void shouldCreateTermsConfigWithPartialProperties() {
            // when
            DlistBlock.TermsConfig termsConfig = new DlistBlock.TermsConfig(1, // min
                    null, // max
                    "[A-Za-z]+", // pattern
                    null, // minLength
                    null, // maxLength
                    null); // severity

            // then
            assertEquals(1, termsConfig.getMin());
            assertNull(termsConfig.getMax());
            assertEquals("[A-Za-z]+", termsConfig.getPattern());
            assertNull(termsConfig.getMinLength());
            assertNull(termsConfig.getMaxLength());
            assertNull(termsConfig.getSeverity());
        }
    }

    @Nested
    class DescriptionsConfigTests {

        @Test
        void shouldCreateDescriptionsConfigWithAllProperties() {
            // when
            DlistBlock.DescriptionsConfig descriptionsConfig = new DlistBlock.DescriptionsConfig(true, // required
                    1, // min
                    3, // max
                    "^\\w+", // pattern
                    Severity.WARN); // severity

            // then
            assertTrue(descriptionsConfig.getRequired());
            assertEquals(1, descriptionsConfig.getMin());
            assertEquals(3, descriptionsConfig.getMax());
            assertEquals("^\\w+", descriptionsConfig.getPattern());
            assertEquals(Severity.WARN, descriptionsConfig.getSeverity());
        }

        @Test
        void shouldCreateDescriptionsConfigWithOnlyRequired() {
            // when
            DlistBlock.DescriptionsConfig descriptionsConfig = new DlistBlock.DescriptionsConfig(false, // required
                    null, // min
                    null, // max
                    null, // pattern
                    null); // severity

            // then
            assertEquals(false, descriptionsConfig.getRequired());
            assertNull(descriptionsConfig.getMin());
            assertNull(descriptionsConfig.getMax());
            assertNull(descriptionsConfig.getPattern());
            assertNull(descriptionsConfig.getSeverity());
        }
    }

    @Nested
    class DelimiterStyleConfigTests {

        @Test
        void shouldCreateDelimiterStyleConfigWithAllProperties() {
            // when
            DlistBlock.DelimiterStyleConfig delimiterConfig = new DlistBlock.DelimiterStyleConfig(
                    new String[] { "::", ":::", "::::" }, // allowedDelimiters
                    true, // consistent
                    Severity.INFO); // severity

            // then
            assertArrayEquals(new String[] { "::", ":::", "::::" }, delimiterConfig.getAllowedDelimiters());
            assertTrue(delimiterConfig.getConsistent());
            assertEquals(Severity.INFO, delimiterConfig.getSeverity());
        }

        @Test
        void shouldCreateDelimiterStyleConfigWithSingleDelimiter() {
            // when
            DlistBlock.DelimiterStyleConfig delimiterConfig = new DlistBlock.DelimiterStyleConfig(new String[] { "::" }, // allowedDelimiters
                    false, // consistent
                    null); // severity

            // then
            assertArrayEquals(new String[] { "::" }, delimiterConfig.getAllowedDelimiters());
            assertEquals(false, delimiterConfig.getConsistent());
            assertNull(delimiterConfig.getSeverity());
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldCorrectlyImplementEqualsAndHashCodeForDlistBlock() {
            // given
            DlistBlock.TermsConfig terms1 = new DlistBlock.TermsConfig(1, // min
                    10, // max
                    "^[A-Z]", // pattern
                    null, // minLength
                    null, // maxLength
                    Severity.ERROR); // severity

            DlistBlock.TermsConfig terms2 = new DlistBlock.TermsConfig(1, // min
                    10, // max
                    "^[A-Z]", // pattern
                    null, // minLength
                    null, // maxLength
                    Severity.ERROR); // severity

            DlistBlock.DescriptionsConfig desc1 = new DlistBlock.DescriptionsConfig(true, // required
                    null, // min
                    null, // max
                    null, // pattern
                    Severity.WARN); // severity

            DlistBlock.DescriptionsConfig desc2 = new DlistBlock.DescriptionsConfig(true, // required
                    null, // min
                    null, // max
                    null, // pattern
                    Severity.WARN); // severity

            // when
            DlistBlock dlist1 = new DlistBlock(null, Severity.ERROR, null, null, terms1, desc1, null, null);

            DlistBlock dlist2 = new DlistBlock(null, Severity.ERROR, null, null, terms2, desc2, null, null);

            DlistBlock dlist3 = new DlistBlock(null, Severity.WARN, null, null, terms1, desc1, null, null);

            // then
            assertEquals(dlist1, dlist2);
            assertNotEquals(dlist1, dlist3);
            assertEquals(dlist1.hashCode(), dlist2.hashCode());
            assertNotEquals(dlist1.hashCode(), dlist3.hashCode());
        }

        @Test
        void shouldTestInnerClassEqualsAndHashCode() {
            // given
            DlistBlock.TermsConfig terms1 = new DlistBlock.TermsConfig(2, // min
                    null, // max
                    "\\w+", // pattern
                    3, // minLength
                    null, // maxLength
                    Severity.ERROR); // severity

            DlistBlock.TermsConfig terms2 = new DlistBlock.TermsConfig(2, // min
                    null, // max
                    "\\w+", // pattern
                    3, // minLength
                    null, // maxLength
                    Severity.ERROR); // severity

            DlistBlock.NestingLevelConfig nesting1 = new DlistBlock.NestingLevelConfig(2, // max
                    Severity.INFO); // severity

            DlistBlock.NestingLevelConfig nesting2 = new DlistBlock.NestingLevelConfig(2, // max
                    Severity.INFO); // severity

            DlistBlock.DelimiterStyleConfig delimiter1 = new DlistBlock.DelimiterStyleConfig(new String[] { "::" }, // allowedDelimiters
                    true, // consistent
                    Severity.WARN); // severity

            DlistBlock.DelimiterStyleConfig delimiter2 = new DlistBlock.DelimiterStyleConfig(new String[] { "::" }, // allowedDelimiters
                    true, // consistent
                    Severity.WARN); // severity

            // then
            assertEquals(terms1, terms2);
            assertEquals(terms1.hashCode(), terms2.hashCode());

            assertEquals(nesting1, nesting2);
            assertEquals(nesting1.hashCode(), nesting2.hashCode());

            assertEquals(delimiter1, delimiter2);
            assertEquals(delimiter1.hashCode(), delimiter2.hashCode());
        }

        @Test
        void shouldHandleDifferentDelimiterArrays() {
            // given
            DlistBlock.DelimiterStyleConfig delimiter1 = new DlistBlock.DelimiterStyleConfig(
                    new String[] { "::", ":::" }, // allowedDelimiters
                    null, // consistent
                    null); // severity

            DlistBlock.DelimiterStyleConfig delimiter2 = new DlistBlock.DelimiterStyleConfig(
                    new String[] { "::", "::::" }, // allowedDelimiters
                    null, // consistent
                    null); // severity

            // then
            assertNotEquals(delimiter1, delimiter2);
            assertNotEquals(delimiter1.hashCode(), delimiter2.hashCode());
        }
    }
}
