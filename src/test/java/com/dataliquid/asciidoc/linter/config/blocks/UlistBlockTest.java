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
            UlistBlock.ItemsConfig itemsConfig = UlistBlock.ItemsConfig.builder().min(2).max(10)
                    .severity(Severity.ERROR).build();

            UlistBlock.NestingLevelConfig nestingConfig = UlistBlock.NestingLevelConfig.builder().max(3)
                    .severity(Severity.WARN).build();

            // When
            UlistBlock ulist = UlistBlock.builder().name("requirements-list").severity(Severity.ERROR)
                    .items(itemsConfig).nestingLevel(nestingConfig).markerStyle("*").build();

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
            UlistBlock ulist = UlistBlock.builder().severity(Severity.WARN).build();

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
                UlistBlock.builder().build();
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
            UlistBlock.ItemsConfig itemsConfig = UlistBlock.ItemsConfig.builder().min(1).max(5).severity(Severity.ERROR)
                    .build();

            // Then
            assertEquals(1, itemsConfig.getMin());
            assertEquals(5, itemsConfig.getMax());
            assertEquals(Severity.ERROR, itemsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create ItemsConfig with only min")
        void shouldCreateItemsConfigWithOnlyMin() {
            // When
            UlistBlock.ItemsConfig itemsConfig = UlistBlock.ItemsConfig.builder().min(3).build();

            // Then
            assertEquals(3, itemsConfig.getMin());
            assertNull(itemsConfig.getMax());
            assertNull(itemsConfig.getSeverity());
        }

        @Test
        @DisplayName("should create ItemsConfig with only max")
        void shouldCreateItemsConfigWithOnlyMax() {
            // When
            UlistBlock.ItemsConfig itemsConfig = UlistBlock.ItemsConfig.builder().max(10).build();

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
            UlistBlock.NestingLevelConfig nestingConfig = UlistBlock.NestingLevelConfig.builder().max(2)
                    .severity(Severity.WARN).build();

            // Then
            assertEquals(2, nestingConfig.getMax());
            assertEquals(Severity.WARN, nestingConfig.getSeverity());
        }

        @Test
        @DisplayName("should create NestingLevelConfig without severity")
        void shouldCreateNestingLevelConfigWithoutSeverity() {
            // When
            UlistBlock.NestingLevelConfig nestingConfig = UlistBlock.NestingLevelConfig.builder().max(4).build();

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
            UlistBlock.ItemsConfig items1 = UlistBlock.ItemsConfig.builder().min(1).max(5).severity(Severity.ERROR)
                    .build();

            UlistBlock.ItemsConfig items2 = UlistBlock.ItemsConfig.builder().min(1).max(5).severity(Severity.ERROR)
                    .build();

            UlistBlock.NestingLevelConfig nesting1 = UlistBlock.NestingLevelConfig.builder().max(2)
                    .severity(Severity.WARN).build();

            UlistBlock.NestingLevelConfig nesting2 = UlistBlock.NestingLevelConfig.builder().max(2)
                    .severity(Severity.WARN).build();

            // When
            UlistBlock ulist1 = UlistBlock.builder().severity(Severity.ERROR).items(items1).nestingLevel(nesting1)
                    .markerStyle("*").build();

            UlistBlock ulist2 = UlistBlock.builder().severity(Severity.ERROR).items(items2).nestingLevel(nesting2)
                    .markerStyle("*").build();

            UlistBlock ulist3 = UlistBlock.builder().severity(Severity.WARN).items(items1).nestingLevel(nesting1)
                    .markerStyle("*").build();

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
            UlistBlock.ItemsConfig items1 = UlistBlock.ItemsConfig.builder().min(2).max(8).severity(Severity.ERROR)
                    .build();

            UlistBlock.ItemsConfig items2 = UlistBlock.ItemsConfig.builder().min(2).max(8).severity(Severity.ERROR)
                    .build();

            UlistBlock.NestingLevelConfig nesting1 = UlistBlock.NestingLevelConfig.builder().max(3)
                    .severity(Severity.INFO).build();

            UlistBlock.NestingLevelConfig nesting2 = UlistBlock.NestingLevelConfig.builder().max(3)
                    .severity(Severity.INFO).build();

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
            UlistBlock ulist1 = UlistBlock.builder().severity(Severity.ERROR).markerStyle("*").build();

            UlistBlock ulist2 = UlistBlock.builder().severity(Severity.ERROR).markerStyle("-").build();

            // Then
            assertNotEquals(ulist1, ulist2);
            assertNotEquals(ulist1.hashCode(), ulist2.hashCode());
        }
    }
}
