package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;

@DisplayName("SidebarBlock")
class SidebarBlockTest {
    
    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        
        @Test
        @DisplayName("should build with required fields")
        void shouldBuildWithRequiredFields() {
            // Given
            String name = "sidebar-test";
            Severity severity = Severity.WARN;
            
            // When
            SidebarBlock block = SidebarBlock.builder()
                .name(name)
                .severity(severity)
                .build();
            
            // Then
            assertEquals(name, block.getName());
            assertEquals(severity, block.getSeverity());
            assertEquals(BlockType.SIDEBAR, block.getType());
            assertNull(block.getTitle());
            assertNull(block.getContent());
            assertNull(block.getPosition());
        }
        
        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            // Given
            String name = "sidebar-full";
            Severity severity = Severity.ERROR;
            SidebarBlock.TitleConfig title = SidebarBlock.TitleConfig.builder()
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*$")
                .severity(Severity.INFO)
                .build();
            SidebarBlock.ContentConfig content = SidebarBlock.ContentConfig.builder()
                .required(true)
                .minLength(50)
                .maxLength(800)
                .lines(SidebarBlock.LinesConfig.builder()
                    .min(3)
                    .max(30)
                    .severity(Severity.INFO)
                    .build())
                .build();
            SidebarBlock.PositionConfig position = SidebarBlock.PositionConfig.builder()
                .required(false)
                .allowed(List.of("left", "right", "float"))
                .severity(Severity.INFO)
                .build();
            
            // When
            SidebarBlock block = SidebarBlock.builder()
                .name(name)
                .severity(severity)
                .title(title)
                .content(content)
                .position(position)
                .build();
            
            // Then
            assertEquals(name, block.getName());
            assertEquals(severity, block.getSeverity());
            assertEquals(title, block.getTitle());
            assertEquals(content, block.getContent());
            assertEquals(position, block.getPosition());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When/Then
            assertThrows(NullPointerException.class, () ->
                SidebarBlock.builder()
                    .name("test")
                    .build(),
                "severity is required"
            );
        }
    }
    
    @Nested
    @DisplayName("TitleConfig")
    class TitleConfigTest {
        
        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            // Given
            boolean required = true;
            Integer minLength = 10;
            Integer maxLength = 100;
            String patternStr = "^[A-Z].*$";
            Pattern pattern = Pattern.compile(patternStr);
            Severity severity = Severity.WARN;
            
            // When
            SidebarBlock.TitleConfig config = SidebarBlock.TitleConfig.builder()
                .required(required)
                .minLength(minLength)
                .maxLength(maxLength)
                .pattern(pattern)
                .severity(severity)
                .build();
            
            // Then
            assertEquals(required, config.isRequired());
            assertEquals(minLength, config.getMinLength());
            assertEquals(maxLength, config.getMaxLength());
            assertEquals(pattern.pattern(), config.getPattern().pattern());
            assertEquals(severity, config.getSeverity());
        }
        
        @Test
        @DisplayName("should build with pattern string")
        void shouldBuildWithPatternString() {
            // Given
            String patternStr = "^[A-Z].*$";
            
            // When
            SidebarBlock.TitleConfig config = SidebarBlock.TitleConfig.builder()
                .pattern(patternStr)
                .build();
            
            // Then
            assertNotNull(config.getPattern());
            assertEquals(patternStr, config.getPattern().pattern());
        }
        
        @Test
        @DisplayName("should handle null pattern string")
        void shouldHandleNullPatternString() {
            // When
            SidebarBlock.TitleConfig config = SidebarBlock.TitleConfig.builder()
                .pattern((String) null)
                .build();
            
            // Then
            assertNull(config.getPattern());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            SidebarBlock.TitleConfig config1 = SidebarBlock.TitleConfig.builder()
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*$")
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.TitleConfig config2 = SidebarBlock.TitleConfig.builder()
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*$")
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.TitleConfig config3 = SidebarBlock.TitleConfig.builder()
                .required(false)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*$")
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("ContentConfig")
    class ContentConfigTest {
        
        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            // Given
            boolean required = true;
            Integer minLength = 50;
            Integer maxLength = 800;
            SidebarBlock.LinesConfig lines = SidebarBlock.LinesConfig.builder()
                .min(3)
                .max(30)
                .severity(Severity.INFO)
                .build();
            
            // When
            SidebarBlock.ContentConfig config = SidebarBlock.ContentConfig.builder()
                .required(required)
                .minLength(minLength)
                .maxLength(maxLength)
                .lines(lines)
                .build();
            
            // Then
            assertEquals(required, config.isRequired());
            assertEquals(minLength, config.getMinLength());
            assertEquals(maxLength, config.getMaxLength());
            assertEquals(lines, config.getLines());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            SidebarBlock.LinesConfig lines = SidebarBlock.LinesConfig.builder()
                .min(3)
                .max(30)
                .build();
            
            SidebarBlock.ContentConfig config1 = SidebarBlock.ContentConfig.builder()
                .required(true)
                .minLength(50)
                .maxLength(800)
                .lines(lines)
                .build();
            
            SidebarBlock.ContentConfig config2 = SidebarBlock.ContentConfig.builder()
                .required(true)
                .minLength(50)
                .maxLength(800)
                .lines(lines)
                .build();
            
            SidebarBlock.ContentConfig config3 = SidebarBlock.ContentConfig.builder()
                .required(false)
                .minLength(50)
                .maxLength(800)
                .lines(lines)
                .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("LinesConfig")
    class LinesConfigTest {
        
        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            // Given
            Integer min = 3;
            Integer max = 30;
            Severity severity = Severity.INFO;
            
            // When
            SidebarBlock.LinesConfig config = SidebarBlock.LinesConfig.builder()
                .min(min)
                .max(max)
                .severity(severity)
                .build();
            
            // Then
            assertEquals(min, config.getMin());
            assertEquals(max, config.getMax());
            assertEquals(severity, config.getSeverity());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            SidebarBlock.LinesConfig config1 = SidebarBlock.LinesConfig.builder()
                .min(3)
                .max(30)
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.LinesConfig config2 = SidebarBlock.LinesConfig.builder()
                .min(3)
                .max(30)
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.LinesConfig config3 = SidebarBlock.LinesConfig.builder()
                .min(5)
                .max(30)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("PositionConfig")
    class PositionConfigTest {
        
        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            // Given
            boolean required = false;
            List<String> allowed = List.of("left", "right", "float");
            Severity severity = Severity.INFO;
            
            // When
            SidebarBlock.PositionConfig config = SidebarBlock.PositionConfig.builder()
                .required(required)
                .allowed(allowed)
                .severity(severity)
                .build();
            
            // Then
            assertEquals(required, config.isRequired());
            assertEquals(allowed, config.getAllowed());
            assertEquals(severity, config.getSeverity());
        }
        
        @Test
        @DisplayName("should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            List<String> allowed = List.of("left", "right");
            
            SidebarBlock.PositionConfig config1 = SidebarBlock.PositionConfig.builder()
                .required(false)
                .allowed(allowed)
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.PositionConfig config2 = SidebarBlock.PositionConfig.builder()
                .required(false)
                .allowed(allowed)
                .severity(Severity.INFO)
                .build();
            
            SidebarBlock.PositionConfig config3 = SidebarBlock.PositionConfig.builder()
                .required(true)
                .allowed(allowed)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCodeTest {
        
        @Test
        @DisplayName("should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            SidebarBlock.TitleConfig title = SidebarBlock.TitleConfig.builder()
                .required(true)
                .build();
            
            SidebarBlock block1 = SidebarBlock.builder()
                .name("sidebar1")
                .severity(Severity.ERROR)
                .title(title)
                .build();
            
            SidebarBlock block2 = SidebarBlock.builder()
                .name("sidebar1")
                .severity(Severity.ERROR)
                .title(title)
                .build();
            
            SidebarBlock block3 = SidebarBlock.builder()
                .name("sidebar2")
                .severity(Severity.ERROR)
                .title(title)
                .build();
            
            // Then
            assertEquals(block1, block2);
            assertEquals(block1.hashCode(), block2.hashCode());
            assertNotEquals(block1, block3);
            assertNotEquals(block1, null);
            assertNotEquals(block1, new Object());
            assertEquals(block1, block1);
        }
    }
}