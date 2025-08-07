package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Unit tests for {@link AudioBlockValidator}.
 * 
 * <p>This test class validates the behavior of the audio block validator,
 * which processes audio elements in AsciiDoc documents. The tests cover
 * validation rules for audio URLs, playback options, and titles.</p>
 * 
 * <p>Test structure follows a nested class pattern for better organization:</p>
 * <ul>
 *   <li>Validate - Basic validator functionality and type checking</li>
 *   <li>UrlValidation - URL requirements and pattern matching with severity support</li>
 *   <li>OptionsValidation - Autoplay, controls, and loop option constraints</li>
 *   <li>TitleValidation - Title requirements and length constraints</li>
 *   <li>SeverityHierarchy - Nested severity support for all configurations</li>
 *   <li>ComplexScenarios - Combined validation scenarios</li>
 * </ul>
 * 
 * <p>Note: AudioBlock supports individual severity levels for all nested rules.
 * If not specified, they fall back to the block-level severity.</p>
 * 
 * @see AudioBlockValidator
 * @see AudioBlock
 */
@DisplayName("AudioBlockValidator")
class AudioBlockValidatorTest {
    
    private AudioBlockValidator validator;
    private BlockValidationContext context;
    private Block mockBlock;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new AudioBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(Block.class);
    }
    
    @Test
    @DisplayName("should return AUDIO as supported type")
    void shouldReturnAudioAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.AUDIO, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when block is not Audio instance")
        void shouldReturnEmptyListWhenNotAudioInstance() {
            // Given
            StructuralNode notAnAudio = mock(StructuralNode.class);
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(notAnAudio, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("UrlValidation")
    class UrlValidation {
        
        @Test
        @DisplayName("should validate required URL")
        void shouldValidateRequiredUrl() {
            // Given
            AudioBlock.UrlConfig urlConfig = AudioBlock.UrlConfig.builder()
                .required(true)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.ERROR)
                .url(urlConfig)
                .build();
            
            when(mockBlock.getAttribute("target")).thenReturn(null);
            when(mockBlock.getContent()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("audio.url.required", message.getRuleId());
            assertEquals("Audio URL is required but not provided", message.getMessage());
            assertTrue(message.getActualValue().isEmpty());
            assertTrue(message.getExpectedValue().isEmpty());
        }
        
        @Test
        @DisplayName("should validate URL pattern")
        void shouldValidateUrlPattern() {
            // Given
            Pattern pattern = Pattern.compile("^(https?://|\\./|/).*\\.(mp3|ogg|wav|m4a)$");
            AudioBlock.UrlConfig urlConfig = AudioBlock.UrlConfig.builder()
                .required(true)
                .pattern(pattern)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.WARN)
                .url(urlConfig)
                .build();
            
            when(mockBlock.getAttribute("target")).thenReturn("http://example.com/audio.txt");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("audio.url.pattern", message.getRuleId());
            assertEquals("Audio URL does not match required pattern", message.getMessage());
            assertEquals("http://example.com/audio.txt", message.getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should use URL-specific severity when configured")
        void shouldUseUrlSpecificSeverity() {
            // Given
            AudioBlock.UrlConfig urlConfig = AudioBlock.UrlConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.INFO)
                .url(urlConfig)
                .build();
            
            when(mockBlock.getAttribute("target")).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }
    }
    
    @Nested
    @DisplayName("OptionsValidation")
    class OptionsValidation {
        
        @Test
        @DisplayName("should validate autoplay not allowed")
        void shouldValidateAutoplayNotAllowed() {
            // Given
            AudioBlock.AutoplayConfig autoplayConfig = AudioBlock.AutoplayConfig.builder()
                .allowed(false)
                .severity(Severity.ERROR)
                .build();
            
            AudioBlock.OptionsConfig optionsConfig = AudioBlock.OptionsConfig.builder()
                .autoplay(autoplayConfig)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.INFO)
                .options(optionsConfig)
                .build();
            
            when(mockBlock.getAttribute("opts")).thenReturn("autoplay");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("audio.options.autoplay.notAllowed", message.getRuleId());
            assertEquals("Audio autoplay is not allowed", message.getMessage());
        }
        
        @Test
        @DisplayName("should validate controls required")
        void shouldValidateControlsRequired() {
            // Given
            AudioBlock.ControlsConfig controlsConfig = AudioBlock.ControlsConfig.builder()
                .required(true)
                .build();
            
            AudioBlock.OptionsConfig optionsConfig = AudioBlock.OptionsConfig.builder()
                .controls(controlsConfig)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.WARN)
                .options(optionsConfig)
                .build();
            
            when(mockBlock.getAttribute("opts")).thenReturn("nocontrols");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("audio.options.controls.required", message.getRuleId());
            assertEquals("Audio controls are required but not enabled", message.getMessage());
        }
        
        @Test
        @DisplayName("should validate loop allowed")
        void shouldValidateLoopAllowed() {
            // Given
            AudioBlock.LoopConfig loopConfig = AudioBlock.LoopConfig.builder()
                .allowed(true)
                .severity(Severity.INFO)
                .build();
            
            AudioBlock.OptionsConfig optionsConfig = AudioBlock.OptionsConfig.builder()
                .loop(loopConfig)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.ERROR)
                .options(optionsConfig)
                .build();
            
            when(mockBlock.getAttribute("opts")).thenReturn("loop");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("TitleValidation")
    class TitleValidation {
        
        @Test
        @DisplayName("should validate required title")
        void shouldValidateRequiredTitle() {
            // Given
            AudioBlock.TitleConfig titleConfig = AudioBlock.TitleConfig.builder()
                .required(true)
                .severity(Severity.WARN)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.ERROR)
                .title(titleConfig)
                .build();
            
            when(mockBlock.getTitle()).thenReturn(null);
            when(mockBlock.getAttribute("caption")).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("audio.title.required", message.getRuleId());
            assertEquals("Audio title is required but not provided", message.getMessage());
        }
        
        @Test
        @DisplayName("should validate title length constraints")
        void shouldValidateTitleLengthConstraints() {
            // Given
            AudioBlock.TitleConfig titleConfig = AudioBlock.TitleConfig.builder()
                .required(true)
                .minLength(10)
                .maxLength(100)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.INFO)
                .title(titleConfig)
                .build();
            
            when(mockBlock.getTitle()).thenReturn("Short");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.INFO, message.getSeverity());
            assertEquals("audio.title.minLength", message.getRuleId());
            assertEquals("Audio title is too short", message.getMessage());
            assertEquals("5 characters", message.getActualValue().orElse(null));
            assertEquals("At least 10 characters", message.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("SeverityHierarchy")
    class SeverityHierarchy {
        
        @Test
        @DisplayName("should use block severity when nested severity not specified")
        void shouldUseBlockSeverityWhenNestedSeverityNotSpecified() {
            // Given
            AudioBlock.UrlConfig urlConfig = AudioBlock.UrlConfig.builder()
                .required(true)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.WARN)
                .url(urlConfig)
                .build();
            
            when(mockBlock.getAttribute("target")).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
        
        @Test
        @DisplayName("should prefer nested severity over block severity")
        void shouldPreferNestedSeverityOverBlockSeverity() {
            // Given
            AudioBlock.TitleConfig titleConfig = AudioBlock.TitleConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.INFO)
                .title(titleConfig)
                .build();
            
            when(mockBlock.getTitle()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        }
    }
    
    @Nested
    @DisplayName("ComplexScenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate all configured rules")
        void shouldValidateAllConfiguredRules() {
            // Given
            AudioBlock config = AudioBlock.builder()
                .severity(Severity.INFO)
                .url(AudioBlock.UrlConfig.builder()
                    .required(true)
                    .pattern("^https?://.*\\.mp3$")
                    .severity(Severity.ERROR)
                    .build())
                .options(AudioBlock.OptionsConfig.builder()
                    .autoplay(AudioBlock.AutoplayConfig.builder()
                        .allowed(false)
                        .severity(Severity.ERROR)
                        .build())
                    .controls(AudioBlock.ControlsConfig.builder()
                        .required(true)
                        .severity(Severity.ERROR)
                        .build())
                    .build())
                .title(AudioBlock.TitleConfig.builder()
                    .required(true)
                    .minLength(10)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            when(mockBlock.getAttribute("target")).thenReturn("file://audio.wav");
            when(mockBlock.getAttribute("opts")).thenReturn("autoplay,nocontrols");
            when(mockBlock.getTitle()).thenReturn("Short");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(4, messages.size());
            
            // URL pattern validation
            assertTrue(messages.stream().anyMatch(m -> 
                "audio.url.pattern".equals(m.getRuleId()) && 
                Severity.ERROR.equals(m.getSeverity())));
            
            // Autoplay not allowed
            assertTrue(messages.stream().anyMatch(m -> 
                "audio.options.autoplay.notAllowed".equals(m.getRuleId()) && 
                Severity.ERROR.equals(m.getSeverity())));
            
            // Controls required
            assertTrue(messages.stream().anyMatch(m -> 
                "audio.options.controls.required".equals(m.getRuleId()) && 
                Severity.ERROR.equals(m.getSeverity())));
            
            // Title too short
            assertTrue(messages.stream().anyMatch(m -> 
                "audio.title.minLength".equals(m.getRuleId()) && 
                Severity.WARN.equals(m.getSeverity())));
        }
    }
}