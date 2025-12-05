package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("UlistBlock")
class UlistBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build UlistBlock with all attributes")
        void shouldBuildUlistBlockWithAllAttributes() {
            // Given
            UlistBlock.ItemsConfig itemsConfig = new UlistBlock.ItemsConfig(2, 10, Severity.ERROR);

            UlistBlock.NestingLevelConfig nestingConfig = new UlistBlock.NestingLevelConfig(3, Severity.WARN);

            // When
            UlistBlock ulist = new UlistBlock("requirements-list", Severity.ERROR, null, null, itemsConfig,
                    nestingConfig, "*");

            // Then
            assertEquals("requirements-list", ulist.getName());
            assertEquals(Severity.ERROR, ulist.getSeverity());

            assertNotNull(ulist.getItems());
            assertEquals(2, ulist.getItems().getMin());
            assertEquals(10, ulist.getItems().getMax());
            assertEquals(Severity.ERROR, ulist.getItems().getSeverity());

            assertNotNull(ulist.getNestingLevel());
            assertEquals(3, ulist.getNestingLevel().getMax());
            assertEquals(Severity.WARN, ulist.getNestingLevel().getSeverity());

            assertEquals("*", ulist.getMarkerStyle());
        }

        @Test
        @DisplayName("should build UlistBlock with minimal attributes")
        void shouldBuildUlistBlockWithMinimalAttributes() {
            // When
            UlistBlock ulist = new UlistBlock(null, Severity.WARN, null, null, null, null, null);

            // Then
            assertNull(ulist.getName());
            assertEquals(Severity.WARN, ulist.getSeverity());
            assertNull(ulist.getItems());
            assertNull(ulist.getNestingLevel());
            assertNull(ulist.getMarkerStyle());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                new UlistBlock(null, null, null, null, null, null, null);
            });
        }
    }

    @Nested
    @DisplayName("ItemsConfig Tests")
    class ItemsConfigTests {

        @Test
        @DisplayName("should create ItemsConfig with min and max")
        void shouldCreateItemsConfigWithMinAndMax() {
            // When
            UlistBlock.ItemsConfig itemsConfig = new UlistBlock.ItemsConfig(1, 5, Severity.ERROR);

            // Then
            assertEquals(1, itemsConfig.getMin());
            assertEquals(5, itemsConfig.getMax());
            assertEquals(Severity.ERROR, itemsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create ItemsConfig with only min")
        void shouldCreateItemsConfigWithOnlyMin() {
            // When
            UlistBlock.ItemsConfig itemsConfig = new UlistBlock.ItemsConfig(3, null, null);

            // Then
            assertEquals(3, itemsConfig.getMin());
            assertNull(itemsConfig.getMax());
            assertNull(itemsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create ItemsConfig with only max")
        void shouldCreateItemsConfigWithOnlyMax() {
            // When
            UlistBlock.ItemsConfig itemsConfig = new UlistBlock.ItemsConfig(null, 10, null);

            // Then
            assertNull(itemsConfig.getMin());
            assertEquals(10, itemsConfig.getMax());
            assertNull(itemsConfig.getSeverity());
        }
    }

    @Nested
    @DisplayName("NestingLevelConfig Tests")
    class NestingLevelConfigTests {

        @Test
        @DisplayName("should create NestingLevelConfig with max")
        void shouldCreateNestingLevelConfigWithMax() {
            // When
            UlistBlock.NestingLevelConfig nestingConfig = new UlistBlock.NestingLevelConfig(2, Severity.WARN);

            // Then
            assertEquals(2, nestingConfig.getMax());
            assertEquals(Severity.WARN, nestingConfig.getSeverity());
        }

        @Test
        @DisplayName("should create NestingLevelConfig without severity")
        void shouldCreateNestingLevelConfigWithoutSeverity() {
            // When
            UlistBlock.NestingLevelConfig nestingConfig = new UlistBlock.NestingLevelConfig(4, null);

            // Then
            assertEquals(4, nestingConfig.getMax());
            assertNull(nestingConfig.getSeverity());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            UlistBlock.ItemsConfig items1 = new UlistBlock.ItemsConfig(1, 5, Severity.ERROR);

            UlistBlock.ItemsConfig items2 = new UlistBlock.ItemsConfig(1, 5, Severity.ERROR);

            UlistBlock.NestingLevelConfig nesting1 = new UlistBlock.NestingLevelConfig(2, Severity.WARN);

            UlistBlock.NestingLevelConfig nesting2 = new UlistBlock.NestingLevelConfig(2, Severity.WARN);

            // When
            UlistBlock ulist1 = new UlistBlock(null, Severity.ERROR, null, null, items1, nesting1, "*");

            UlistBlock ulist2 = new UlistBlock(null, Severity.ERROR, null, null, items2, nesting2, "*");

            UlistBlock ulist3 = new UlistBlock(null, Severity.WARN, null, null, items1, nesting1, "*");

            // Then
            assertEquals(ulist1, ulist2);
            assertNotEquals(ulist1, ulist3);
            assertEquals(ulist1.hashCode(), ulist2.hashCode());
            assertNotEquals(ulist1.hashCode(), ulist3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            UlistBlock.ItemsConfig items1 = new UlistBlock.ItemsConfig(2, 8, Severity.ERROR);

            UlistBlock.ItemsConfig items2 = new UlistBlock.ItemsConfig(2, 8, Severity.ERROR);

            UlistBlock.NestingLevelConfig nesting1 = new UlistBlock.NestingLevelConfig(3, Severity.INFO);

            UlistBlock.NestingLevelConfig nesting2 = new UlistBlock.NestingLevelConfig(3, Severity.INFO);

            // Then
            assertEquals(items1, items2);
            assertEquals(items1.hashCode(), items2.hashCode());

            assertEquals(nesting1, nesting2);
            assertEquals(nesting1.hashCode(), nesting2.hashCode());
        }

        @Test
        @DisplayName("should handle different marker styles")
        void shouldHandleDifferentMarkerStyles() {
            // Given
            UlistBlock ulist1 = new UlistBlock(null, Severity.ERROR, null, null, null, null, "*");

            UlistBlock ulist2 = new UlistBlock(null, Severity.ERROR, null, null, null, null, "-");

            // Then
            assertNotEquals(ulist1, ulist2);
            assertNotEquals(ulist1.hashCode(), ulist2.hashCode());
        }
    }
}
