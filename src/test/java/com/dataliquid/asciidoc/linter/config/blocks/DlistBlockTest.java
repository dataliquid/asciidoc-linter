package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("DlistBlock")
class DlistBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build DlistBlock with all attributes")
        void shouldBuildDlistBlockWithAllAttributes() {
            // Given
            DlistBlock.TermsConfig termsConfig = DlistBlock.TermsConfig.builder().min(1).max(20).pattern("^[A-Z].*")
                    .minLength(3).maxLength(50).severity(Severity.ERROR).build();

            DlistBlock.DescriptionsConfig descriptionsConfig = DlistBlock.DescriptionsConfig.builder().required(true)
                    .min(1).max(5).pattern(".*\\.$").severity(Severity.WARN).build();

            DlistBlock.NestingLevelConfig nestingConfig = DlistBlock.NestingLevelConfig.builder().max(3)
                    .severity(Severity.INFO).build();

            DlistBlock.DelimiterStyleConfig delimiterConfig = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", ":::"}).consistent(true).severity(Severity.WARN).build();

            // When
            DlistBlock dlist = DlistBlock.builder().name("glossary-list").severity(Severity.ERROR).terms(termsConfig)
                    .descriptions(descriptionsConfig).nestingLevel(nestingConfig).delimiterStyle(delimiterConfig)
                    .build();

            // Then
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
            assertArrayEquals(new String[]{"::", ":::"}, dlist.getDelimiterStyle().getAllowedDelimiters());
            assertTrue(dlist.getDelimiterStyle().getConsistent());
            assertEquals(Severity.WARN, dlist.getDelimiterStyle().getSeverity());
        }

        @Test
        @DisplayName("should build DlistBlock with minimal attributes")
        void shouldBuildDlistBlockWithMinimalAttributes() {
            // When
            DlistBlock dlist = DlistBlock.builder().severity(Severity.WARN).build();

            // Then
            assertNull(dlist.getName());
            assertEquals(Severity.WARN, dlist.getSeverity());
            assertNull(dlist.getTerms());
            assertNull(dlist.getDescriptions());
            assertNull(dlist.getNestingLevel());
            assertNull(dlist.getDelimiterStyle());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                DlistBlock.builder().build();
            });
        }
    }

    @Nested
    @DisplayName("TermsConfig Tests")
    class TermsConfigTests {

        @Test
        @DisplayName("should create TermsConfig with all properties")
        void shouldCreateTermsConfigWithAllProperties() {
            // When
            DlistBlock.TermsConfig termsConfig = DlistBlock.TermsConfig.builder().min(2).max(10).pattern("^[A-Z].*")
                    .minLength(5).maxLength(100).severity(Severity.ERROR).build();

            // Then
            assertEquals(2, termsConfig.getMin());
            assertEquals(10, termsConfig.getMax());
            assertEquals("^[A-Z].*", termsConfig.getPattern());
            assertEquals(5, termsConfig.getMinLength());
            assertEquals(100, termsConfig.getMaxLength());
            assertEquals(Severity.ERROR, termsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create TermsConfig with partial properties")
        void shouldCreateTermsConfigWithPartialProperties() {
            // When
            DlistBlock.TermsConfig termsConfig = DlistBlock.TermsConfig.builder().min(1).pattern("[A-Za-z]+").build();

            // Then
            assertEquals(1, termsConfig.getMin());
            assertNull(termsConfig.getMax());
            assertEquals("[A-Za-z]+", termsConfig.getPattern());
            assertNull(termsConfig.getMinLength());
            assertNull(termsConfig.getMaxLength());
            assertNull(termsConfig.getSeverity());
        }
    }

    @Nested
    @DisplayName("DescriptionsConfig Tests")
    class DescriptionsConfigTests {

        @Test
        @DisplayName("should create DescriptionsConfig with all properties")
        void shouldCreateDescriptionsConfigWithAllProperties() {
            // When
            DlistBlock.DescriptionsConfig descriptionsConfig = DlistBlock.DescriptionsConfig.builder().required(true)
                    .min(1).max(3).pattern("^\\w+").severity(Severity.WARN).build();

            // Then
            assertTrue(descriptionsConfig.getRequired());
            assertEquals(1, descriptionsConfig.getMin());
            assertEquals(3, descriptionsConfig.getMax());
            assertEquals("^\\w+", descriptionsConfig.getPattern());
            assertEquals(Severity.WARN, descriptionsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create DescriptionsConfig with only required flag")
        void shouldCreateDescriptionsConfigWithOnlyRequired() {
            // When
            DlistBlock.DescriptionsConfig descriptionsConfig = DlistBlock.DescriptionsConfig.builder().required(false)
                    .build();

            // Then
            assertEquals(false, descriptionsConfig.getRequired());
            assertNull(descriptionsConfig.getMin());
            assertNull(descriptionsConfig.getMax());
            assertNull(descriptionsConfig.getPattern());
            assertNull(descriptionsConfig.getSeverity());
        }
    }

    @Nested
    @DisplayName("DelimiterStyleConfig Tests")
    class DelimiterStyleConfigTests {

        @Test
        @DisplayName("should create DelimiterStyleConfig with all properties")
        void shouldCreateDelimiterStyleConfigWithAllProperties() {
            // When
            DlistBlock.DelimiterStyleConfig delimiterConfig = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", ":::", "::::"}).consistent(true).severity(Severity.INFO)
                    .build();

            // Then
            assertArrayEquals(new String[]{"::", ":::", "::::"}, delimiterConfig.getAllowedDelimiters());
            assertTrue(delimiterConfig.getConsistent());
            assertEquals(Severity.INFO, delimiterConfig.getSeverity());
        }

        @Test
        @DisplayName("should create DelimiterStyleConfig with single delimiter")
        void shouldCreateDelimiterStyleConfigWithSingleDelimiter() {
            // When
            DlistBlock.DelimiterStyleConfig delimiterConfig = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::"}).consistent(false).build();

            // Then
            assertArrayEquals(new String[]{"::"}, delimiterConfig.getAllowedDelimiters());
            assertEquals(false, delimiterConfig.getConsistent());
            assertNull(delimiterConfig.getSeverity());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode for DlistBlock")
        void shouldCorrectlyImplementEqualsAndHashCodeForDlistBlock() {
            // Given
            DlistBlock.TermsConfig terms1 = DlistBlock.TermsConfig.builder().min(1).max(10).pattern("^[A-Z]")
                    .severity(Severity.ERROR).build();

            DlistBlock.TermsConfig terms2 = DlistBlock.TermsConfig.builder().min(1).max(10).pattern("^[A-Z]")
                    .severity(Severity.ERROR).build();

            DlistBlock.DescriptionsConfig desc1 = DlistBlock.DescriptionsConfig.builder().required(true)
                    .severity(Severity.WARN).build();

            DlistBlock.DescriptionsConfig desc2 = DlistBlock.DescriptionsConfig.builder().required(true)
                    .severity(Severity.WARN).build();

            // When
            DlistBlock dlist1 = DlistBlock.builder().severity(Severity.ERROR).terms(terms1).descriptions(desc1).build();

            DlistBlock dlist2 = DlistBlock.builder().severity(Severity.ERROR).terms(terms2).descriptions(desc2).build();

            DlistBlock dlist3 = DlistBlock.builder().severity(Severity.WARN).terms(terms1).descriptions(desc1).build();

            // Then
            assertEquals(dlist1, dlist2);
            assertNotEquals(dlist1, dlist3);
            assertEquals(dlist1.hashCode(), dlist2.hashCode());
            assertNotEquals(dlist1.hashCode(), dlist3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            DlistBlock.TermsConfig terms1 = DlistBlock.TermsConfig.builder().min(2).pattern("\\w+").minLength(3)
                    .severity(Severity.ERROR).build();

            DlistBlock.TermsConfig terms2 = DlistBlock.TermsConfig.builder().min(2).pattern("\\w+").minLength(3)
                    .severity(Severity.ERROR).build();

            DlistBlock.NestingLevelConfig nesting1 = DlistBlock.NestingLevelConfig.builder().max(2)
                    .severity(Severity.INFO).build();

            DlistBlock.NestingLevelConfig nesting2 = DlistBlock.NestingLevelConfig.builder().max(2)
                    .severity(Severity.INFO).build();

            DlistBlock.DelimiterStyleConfig delimiter1 = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::"}).consistent(true).severity(Severity.WARN).build();

            DlistBlock.DelimiterStyleConfig delimiter2 = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::"}).consistent(true).severity(Severity.WARN).build();

            // Then
            assertEquals(terms1, terms2);
            assertEquals(terms1.hashCode(), terms2.hashCode());

            assertEquals(nesting1, nesting2);
            assertEquals(nesting1.hashCode(), nesting2.hashCode());

            assertEquals(delimiter1, delimiter2);
            assertEquals(delimiter1.hashCode(), delimiter2.hashCode());
        }

        @Test
        @DisplayName("should handle different delimiter arrays")
        void shouldHandleDifferentDelimiterArrays() {
            // Given
            DlistBlock.DelimiterStyleConfig delimiter1 = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", ":::"}).build();

            DlistBlock.DelimiterStyleConfig delimiter2 = DlistBlock.DelimiterStyleConfig.builder()
                    .allowedDelimiters(new String[]{"::", "::::"}).build();

            // Then
            assertNotEquals(delimiter1, delimiter2);
            assertNotEquals(delimiter1.hashCode(), delimiter2.hashCode());
        }
    }
}
