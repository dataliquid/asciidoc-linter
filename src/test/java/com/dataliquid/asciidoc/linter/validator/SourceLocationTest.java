package com.dataliquid.asciidoc.linter.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SourceLocation")
class SourceLocationTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        
        @Test
        @DisplayName("should create location with all fields")
        void shouldCreateLocationWithAllFields() {
            // Given
            String filename = "test.adoc";
            int startLine = 10;
            int startColumn = 5;
            int endLine = 12;
            int endColumn = 15;
            String sourceLine = "= Test Document";
            
            // When
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .startLine(startLine)
                .startColumn(startColumn)
                .endLine(endLine)
                .endColumn(endColumn)
                .sourceLine(sourceLine)
                .build();
            
            // Then
            assertEquals(filename, location.getFilename());
            assertEquals(startLine, location.getStartLine());
            assertEquals(startColumn, location.getStartColumn());
            assertEquals(endLine, location.getEndLine());
            assertEquals(endColumn, location.getEndColumn());
            assertEquals(sourceLine, location.getSourceLine());
        }
        
        @Test
        @DisplayName("should create single line location")
        void shouldCreateSingleLineLocation() {
            // Given
            String filename = "test.adoc";
            int line = 5;
            int startColumn = 10;
            int endColumn = 20;
            
            // When
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .line(line)
                .columns(startColumn, endColumn)
                .build();
            
            // Then
            assertEquals(line, location.getStartLine());
            assertEquals(line, location.getEndLine());
            assertEquals(startColumn, location.getStartColumn());
            assertEquals(endColumn, location.getEndColumn());
            assertFalse(location.isMultiLine());
        }
        
        @Test
        @DisplayName("should require filename")
        void shouldRequireFilename() {
            // Given
            SourceLocation.Builder builder = SourceLocation.builder();
            
            // When/Then
            assertThrows(NullPointerException.class, () -> 
                builder.build()
            );
        }
    }

    @Nested
    @DisplayName("Formatting")
    class FormattingTest {
        
        @Test
        @DisplayName("should format single line with column range")
        void shouldFormatSingleLineWithColumnRange() {
            // Given
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .columns(5, 15)
                .build();
            
            // When
            String formatted = location.formatLocation();
            
            // Then
            assertEquals("test.adoc:10:5-15", formatted);
        }
        
        @Test
        @DisplayName("should format single line with single column")
        void shouldFormatSingleLineWithSingleColumn() {
            // Given
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .startColumn(5)
                .endColumn(5)
                .build();
            
            // When
            String formatted = location.formatLocation();
            
            // Then
            assertEquals("test.adoc:10:5", formatted);
        }
        
        @Test
        @DisplayName("should format multi-line location")
        void shouldFormatMultiLineLocation() {
            // Given
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .startLine(10)
                .endLine(15)
                .build();
            
            // When
            String formatted = location.formatLocation();
            boolean isMultiLine = location.isMultiLine();
            
            // Then
            assertEquals("test.adoc:10-15", formatted);
            assertTrue(isMultiLine);
        }
        
        @Test
        @DisplayName("should format line only location")
        void shouldFormatLineOnlyLocation() {
            // Given
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .build();
            
            // When
            String formatted = location.formatLocation();
            
            // Then
            assertEquals("test.adoc:10", formatted);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTest {
        
        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Given
            String filename = "test.adoc";
            int line = 10;
            
            // When
            SourceLocation location1 = SourceLocation.builder()
                .filename(filename)
                .line(line)
                .build();
            
            SourceLocation location2 = SourceLocation.builder()
                .filename(filename)
                .line(line)
                .build();
            
            // Then
            assertEquals(location1, location2);
            assertEquals(location1.hashCode(), location2.hashCode());
        }
        
        @Test
        @DisplayName("should not be equal for different filenames")
        void shouldNotBeEqualForDifferentFilenames() {
            // Given
            int sameLine = 10;
            
            // When
            SourceLocation location1 = SourceLocation.builder()
                .filename("test1.adoc")
                .line(sameLine)
                .build();
            
            SourceLocation location2 = SourceLocation.builder()
                .filename("test2.adoc")
                .line(sameLine)
                .build();
            
            // Then
            assertNotEquals(location1, location2);
        }
    }
}