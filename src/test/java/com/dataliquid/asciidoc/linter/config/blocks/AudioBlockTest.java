package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("AudioBlock")
class AudioBlockTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build AudioBlock with all attributes")
        void shouldBuildAudioBlockWithAllAttributes() {
            // Given
            AudioBlock.UrlConfig urlRule = AudioBlock.UrlConfig.builder().required(true)
                    .pattern("^(https?://|\\./|/).*\\.(mp3|ogg|wav|m4a)$").severity(Severity.ERROR).build();

            AudioBlock.AutoplayConfig autoplayRule = AudioBlock.AutoplayConfig.builder().allowed(false)
                    .severity(Severity.ERROR).build();

            AudioBlock.ControlsConfig controlsRule = AudioBlock.ControlsConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            AudioBlock.LoopConfig loopRule = AudioBlock.LoopConfig.builder().allowed(true).severity(Severity.INFO)
                    .build();

            AudioBlock.OptionsConfig optionsRule = AudioBlock.OptionsConfig.builder().autoplay(autoplayRule)
                    .controls(controlsRule).loop(loopRule).build();

            AudioBlock.TitleConfig titleRule = AudioBlock.TitleConfig.builder().required(true).minLength(10)
                    .maxLength(100).severity(Severity.WARN).build();

            // When
            AudioBlock audio = AudioBlock.builder().severity(Severity.INFO).url(urlRule).options(optionsRule)
                    .title(titleRule).build();

            // Then
            assertEquals(Severity.INFO, audio.getSeverity());

            assertNotNull(audio.getUrl());
            assertTrue(audio.getUrl().isRequired());
            assertNotNull(audio.getUrl().getPattern());
            assertEquals(Severity.ERROR, audio.getUrl().getSeverity());

            assertNotNull(audio.getOptions());
            assertNotNull(audio.getOptions().getAutoplay());
            assertFalse(audio.getOptions().getAutoplay().isAllowed());
            assertEquals(Severity.ERROR, audio.getOptions().getAutoplay().getSeverity());

            assertNotNull(audio.getOptions().getControls());
            assertTrue(audio.getOptions().getControls().isRequired());
            assertEquals(Severity.ERROR, audio.getOptions().getControls().getSeverity());

            assertNotNull(audio.getOptions().getLoop());
            assertTrue(audio.getOptions().getLoop().isAllowed());
            assertEquals(Severity.INFO, audio.getOptions().getLoop().getSeverity());

            assertNotNull(audio.getTitle());
            assertTrue(audio.getTitle().isRequired());
            assertEquals(10, audio.getTitle().getMinLength());
            assertEquals(100, audio.getTitle().getMaxLength());
            assertEquals(Severity.WARN, audio.getTitle().getSeverity());
        }

        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                AudioBlock.builder().build();
            });
        }
    }

    @Nested
    @DisplayName("UrlConfig Tests")
    class UrlConfigTests {

        @Test
        @DisplayName("should create UrlConfig with string pattern")
        void shouldCreateUrlConfigWithStringPattern() {
            // Given & When
            AudioBlock.UrlConfig urlRule = AudioBlock.UrlConfig.builder().required(true).pattern("^https?://.*\\.mp3$")
                    .severity(Severity.ERROR).build();

            // Then
            assertTrue(urlRule.isRequired());
            assertNotNull(urlRule.getPattern());
            assertEquals("^https?://.*\\.mp3$", urlRule.getPattern().pattern());
            assertEquals(Severity.ERROR, urlRule.getSeverity());
        }

        @Test
        @DisplayName("should create UrlConfig with Pattern object")
        void shouldCreateUrlConfigWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile(".*\\.(ogg|wav)$");

            // When
            AudioBlock.UrlConfig urlRule = AudioBlock.UrlConfig.builder().required(false).pattern(pattern).build();

            // Then
            assertFalse(urlRule.isRequired());
            assertEquals(pattern, urlRule.getPattern());
            assertNull(urlRule.getSeverity());
        }

        @Test
        @DisplayName("should handle null pattern and severity")
        void shouldHandleNullPatternAndSeverity() {
            // Given & When
            AudioBlock.UrlConfig urlRule = AudioBlock.UrlConfig.builder().required(true).pattern((String) null).build();

            // Then
            assertTrue(urlRule.isRequired());
            assertNull(urlRule.getPattern());
            assertNull(urlRule.getSeverity());
        }
    }

    @Nested
    @DisplayName("OptionsConfig Tests")
    class OptionsConfigTests {

        @Test
        @DisplayName("should create OptionsConfig with all options")
        void shouldCreateOptionsConfigWithAllOptions() {
            // Given
            AudioBlock.AutoplayConfig autoplay = AudioBlock.AutoplayConfig.builder().allowed(false)
                    .severity(Severity.ERROR).build();

            AudioBlock.ControlsConfig controls = AudioBlock.ControlsConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            AudioBlock.LoopConfig loop = AudioBlock.LoopConfig.builder().allowed(true).build();

            // When
            AudioBlock.OptionsConfig options = AudioBlock.OptionsConfig.builder().autoplay(autoplay).controls(controls)
                    .loop(loop).build();

            // Then
            assertNotNull(options.getAutoplay());
            assertFalse(options.getAutoplay().isAllowed());
            assertEquals(Severity.ERROR, options.getAutoplay().getSeverity());

            assertNotNull(options.getControls());
            assertTrue(options.getControls().isRequired());
            assertEquals(Severity.ERROR, options.getControls().getSeverity());

            assertNotNull(options.getLoop());
            assertTrue(options.getLoop().isAllowed());
            assertNull(options.getLoop().getSeverity());
        }

        @Test
        @DisplayName("should allow null options")
        void shouldAllowNullOptions() {
            // Given & When
            AudioBlock.OptionsConfig options = AudioBlock.OptionsConfig.builder().build();

            // Then
            assertNull(options.getAutoplay());
            assertNull(options.getControls());
            assertNull(options.getLoop());
        }
    }

    @Nested
    @DisplayName("TitleConfig Tests")
    class TitleConfigTests {

        @Test
        @DisplayName("should create TitleConfig with length constraints")
        void shouldCreateTitleConfigWithLengthConstraints() {
            // Given & When
            AudioBlock.TitleConfig title = AudioBlock.TitleConfig.builder().required(true).minLength(10).maxLength(100)
                    .severity(Severity.WARN).build();

            // Then
            assertTrue(title.isRequired());
            assertEquals(10, title.getMinLength());
            assertEquals(100, title.getMaxLength());
            assertEquals(Severity.WARN, title.getSeverity());
        }

        @Test
        @DisplayName("should allow optional title")
        void shouldAllowOptionalTitle() {
            // Given & When
            AudioBlock.TitleConfig title = AudioBlock.TitleConfig.builder().required(false).build();

            // Then
            assertFalse(title.isRequired());
            assertNull(title.getMinLength());
            assertNull(title.getMaxLength());
            assertNull(title.getSeverity());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            AudioBlock.UrlConfig url1 = AudioBlock.UrlConfig.builder().required(true).pattern(".*\\.mp3$")
                    .severity(Severity.ERROR).build();

            AudioBlock.UrlConfig url2 = AudioBlock.UrlConfig.builder().required(true).pattern(".*\\.mp3$")
                    .severity(Severity.ERROR).build();

            AudioBlock.TitleConfig title1 = AudioBlock.TitleConfig.builder().required(true).minLength(10)
                    .severity(Severity.WARN).build();

            AudioBlock.TitleConfig title2 = AudioBlock.TitleConfig.builder().required(true).minLength(10)
                    .severity(Severity.WARN).build();

            // When
            AudioBlock audio1 = AudioBlock.builder().severity(Severity.INFO).url(url1).title(title1).build();

            AudioBlock audio2 = AudioBlock.builder().severity(Severity.INFO).url(url2).title(title2).build();

            AudioBlock audio3 = AudioBlock.builder().severity(Severity.ERROR).url(url1).title(title1).build();

            // Then
            assertEquals(audio1, audio2);
            assertNotEquals(audio1, audio3);
            assertEquals(audio1.hashCode(), audio2.hashCode());
            assertNotEquals(audio1.hashCode(), audio3.hashCode());
        }

        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            AudioBlock.UrlConfig url1 = AudioBlock.UrlConfig.builder().required(true).pattern("test")
                    .severity(Severity.ERROR).build();

            AudioBlock.UrlConfig url2 = AudioBlock.UrlConfig.builder().required(true).pattern("test")
                    .severity(Severity.ERROR).build();

            AudioBlock.AutoplayConfig autoplay1 = AudioBlock.AutoplayConfig.builder().allowed(false)
                    .severity(Severity.ERROR).build();

            AudioBlock.AutoplayConfig autoplay2 = AudioBlock.AutoplayConfig.builder().allowed(false)
                    .severity(Severity.ERROR).build();

            AudioBlock.ControlsConfig controls1 = AudioBlock.ControlsConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            AudioBlock.ControlsConfig controls2 = AudioBlock.ControlsConfig.builder().required(true)
                    .severity(Severity.ERROR).build();

            AudioBlock.LoopConfig loop1 = AudioBlock.LoopConfig.builder().allowed(true).severity(Severity.INFO).build();

            AudioBlock.LoopConfig loop2 = AudioBlock.LoopConfig.builder().allowed(true).severity(Severity.INFO).build();

            AudioBlock.TitleConfig title1 = AudioBlock.TitleConfig.builder().required(true).minLength(5).maxLength(50)
                    .severity(Severity.WARN).build();

            AudioBlock.TitleConfig title2 = AudioBlock.TitleConfig.builder().required(true).minLength(5).maxLength(50)
                    .severity(Severity.WARN).build();

            // Then
            assertEquals(url1, url2);
            assertEquals(url1.hashCode(), url2.hashCode());

            assertEquals(autoplay1, autoplay2);
            assertEquals(autoplay1.hashCode(), autoplay2.hashCode());

            assertEquals(controls1, controls2);
            assertEquals(controls1.hashCode(), controls2.hashCode());

            assertEquals(loop1, loop2);
            assertEquals(loop1.hashCode(), loop2.hashCode());

            assertEquals(title1, title2);
            assertEquals(title1.hashCode(), title2.hashCode());
        }
    }
}
