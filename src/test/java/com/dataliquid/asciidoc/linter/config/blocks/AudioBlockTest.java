package com.dataliquid.asciidoc.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

class AudioBlockTest {

    @Nested
    class ConstructorTests {

        @Test
        void shouldConstructAudioBlockWithAllAttributes() {
            // given
            AudioBlock.UrlConfig urlRule = new AudioBlock.UrlConfig(true, "^(https?://|\\./|/).*\\.(mp3|ogg|wav|m4a)$",
                    Severity.ERROR);

            AudioBlock.AutoplayConfig autoplayRule = new AudioBlock.AutoplayConfig(false, Severity.ERROR);

            AudioBlock.ControlsConfig controlsRule = new AudioBlock.ControlsConfig(true, Severity.ERROR);

            AudioBlock.LoopConfig loopRule = new AudioBlock.LoopConfig(true, Severity.INFO);

            AudioBlock.OptionsConfig optionsRule = new AudioBlock.OptionsConfig(autoplayRule, controlsRule, loopRule);

            AudioBlock.TitleConfig titleRule = new AudioBlock.TitleConfig(true, 10, 100, Severity.WARN);

            // when
            AudioBlock audio = new AudioBlock(null, Severity.INFO, null, null, urlRule, optionsRule, titleRule);

            // then
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
        void shouldConstructAudioBlockWithMinimalAttributes() {
            // when
            AudioBlock audio = new AudioBlock(null, Severity.INFO, null, null, null, null, null);

            // then
            assertEquals(Severity.INFO, audio.getSeverity());
            assertNull(audio.getUrl());
            assertNull(audio.getOptions());
            assertNull(audio.getTitle());
        }
    }

    @Nested
    class UrlConfigTests {

        @Test
        void shouldCreateUrlConfigWithStringPattern() {
            // when
            AudioBlock.UrlConfig urlRule = new AudioBlock.UrlConfig(true, "^https?://.*\\.mp3$", Severity.ERROR);

            // then
            assertTrue(urlRule.isRequired());
            assertNotNull(urlRule.getPattern());
            assertEquals("^https?://.*\\.mp3$", urlRule.getPattern().pattern());
            assertEquals(Severity.ERROR, urlRule.getSeverity());
        }

        @Test
        void shouldHandleNullPatternAndSeverity() {
            // when
            AudioBlock.UrlConfig urlRule = new AudioBlock.UrlConfig(true, null, null);

            // then
            assertTrue(urlRule.isRequired());
            assertNull(urlRule.getPattern());
            assertNull(urlRule.getSeverity());
        }
    }

    @Nested
    class OptionsConfigTests {

        @Test
        void shouldCreateOptionsConfigWithAllOptions() {
            // given
            AudioBlock.AutoplayConfig autoplay = new AudioBlock.AutoplayConfig(false, Severity.ERROR);

            AudioBlock.ControlsConfig controls = new AudioBlock.ControlsConfig(true, Severity.ERROR);

            AudioBlock.LoopConfig loop = new AudioBlock.LoopConfig(true, null);

            // when
            AudioBlock.OptionsConfig options = new AudioBlock.OptionsConfig(autoplay, controls, loop);

            // then
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
        void shouldAllowNullOptions() {
            // when
            AudioBlock.OptionsConfig options = new AudioBlock.OptionsConfig(null, null, null);

            // then
            assertNull(options.getAutoplay());
            assertNull(options.getControls());
            assertNull(options.getLoop());
        }
    }

    @Nested
    class TitleConfigTests {

        @Test
        void shouldCreateTitleConfigWithLengthConstraints() {
            // when
            AudioBlock.TitleConfig title = new AudioBlock.TitleConfig(true, 10, 100, Severity.WARN);

            // then
            assertTrue(title.isRequired());
            assertEquals(10, title.getMinLength());
            assertEquals(100, title.getMaxLength());
            assertEquals(Severity.WARN, title.getSeverity());
        }

        @Test
        void shouldAllowOptionalTitle() {
            // when
            AudioBlock.TitleConfig title = new AudioBlock.TitleConfig(false, null, null, null);

            // then
            assertFalse(title.isRequired());
            assertNull(title.getMinLength());
            assertNull(title.getMaxLength());
            assertNull(title.getSeverity());
        }
    }

    @Nested
    class EqualsHashCodeTests {

        @Test
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // given
            AudioBlock.UrlConfig url1 = new AudioBlock.UrlConfig(true, ".*\\.mp3$", Severity.ERROR);

            AudioBlock.UrlConfig url2 = new AudioBlock.UrlConfig(true, ".*\\.mp3$", Severity.ERROR);

            AudioBlock.TitleConfig title1 = new AudioBlock.TitleConfig(true, 10, null, Severity.WARN);

            AudioBlock.TitleConfig title2 = new AudioBlock.TitleConfig(true, 10, null, Severity.WARN);

            // when
            AudioBlock audio1 = new AudioBlock(null, Severity.INFO, null, null, url1, null, title1);

            AudioBlock audio2 = new AudioBlock(null, Severity.INFO, null, null, url2, null, title2);

            AudioBlock audio3 = new AudioBlock(null, Severity.ERROR, null, null, url1, null, title1);

            // then
            assertEquals(audio1, audio2);
            assertNotEquals(audio1, audio3);
            assertEquals(audio1.hashCode(), audio2.hashCode());
            assertNotEquals(audio1.hashCode(), audio3.hashCode());
        }

        @Test
        void shouldTestInnerClassEqualsAndHashCode() {
            // given
            AudioBlock.UrlConfig url1 = new AudioBlock.UrlConfig(true, "test", Severity.ERROR);

            AudioBlock.UrlConfig url2 = new AudioBlock.UrlConfig(true, "test", Severity.ERROR);

            AudioBlock.AutoplayConfig autoplay1 = new AudioBlock.AutoplayConfig(false, Severity.ERROR);

            AudioBlock.AutoplayConfig autoplay2 = new AudioBlock.AutoplayConfig(false, Severity.ERROR);

            AudioBlock.ControlsConfig controls1 = new AudioBlock.ControlsConfig(true, Severity.ERROR);

            AudioBlock.ControlsConfig controls2 = new AudioBlock.ControlsConfig(true, Severity.ERROR);

            AudioBlock.LoopConfig loop1 = new AudioBlock.LoopConfig(true, Severity.INFO);

            AudioBlock.LoopConfig loop2 = new AudioBlock.LoopConfig(true, Severity.INFO);

            AudioBlock.TitleConfig title1 = new AudioBlock.TitleConfig(true, 5, 50, Severity.WARN);

            AudioBlock.TitleConfig title2 = new AudioBlock.TitleConfig(true, 5, 50, Severity.WARN);

            // then
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
