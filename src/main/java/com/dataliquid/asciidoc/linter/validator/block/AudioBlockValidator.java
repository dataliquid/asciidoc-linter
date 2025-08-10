package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Audio.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import com.dataliquid.asciidoc.linter.validator.Suggestion;

/**
 * Validator for audio blocks in AsciiDoc documents.
 * 
 * <p>This validator validates audio blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/audio-block.yaml}.
 * The YAML configuration is parsed into {@link AudioBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>url</b>: Validates audio URL/path (required, pattern matching, severity support)</li>
 *   <li><b>options.autoplay</b>: Validates autoplay option (allowed/disallowed, severity support)</li>
 *   <li><b>options.controls</b>: Validates controls requirement (required, severity support)</li>
 *   <li><b>options.loop</b>: Validates loop option (allowed/disallowed, severity support)</li>
 *   <li><b>title</b>: Validates title/description (required, length constraints, severity support)</li>
 * </ul>
 * 
 * <p>Note: Audio block nested configurations support individual severity levels.
 * If not specified, they fall back to the block-level severity.</p>
 * 
 * @see AudioBlock
 * @see BlockTypeValidator
 */
public final class AudioBlockValidator extends AbstractBlockValidator<AudioBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.AUDIO;
    }
    
    @Override
    protected Class<AudioBlock> getBlockConfigClass() {
        return AudioBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               AudioBlock audioConfig,
                                                               BlockValidationContext context) {
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get audio attributes
        String audioUrl = getAudioUrl(block);
        String title = getTitle(block);
        
        // Validate URL
        if (audioConfig.getUrl() != null) {
            validateUrl(audioUrl, audioConfig.getUrl(), context, block, messages, audioConfig);
        }
        
        // Validate options
        if (audioConfig.getOptions() != null) {
            validateOptions(block, audioConfig.getOptions(), context, messages, audioConfig);
        }
        
        // Validate title
        if (audioConfig.getTitle() != null) {
            validateTitle(title, audioConfig.getTitle(), context, block, messages, audioConfig);
        }
        
        return messages;
    }
    
    private String getAudioUrl(StructuralNode block) {
        // Try different ways to get audio URL
        Object target = block.getAttribute(TARGET);
        if (target != null) {
            return target.toString();
        }
        
        // For audio blocks, the content might contain the path
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        return null;
    }
    
    private String getTitle(StructuralNode block) {
        // First try the title attribute
        String title = block.getTitle();
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        
        // Then try the caption attribute
        Object caption = block.getAttribute(CAPTION);
        if (caption != null) {
            return caption.toString();
        }
        
        return null;
    }
    
    private void validateUrl(String url, AudioBlock.UrlConfig urlConfig,
                           BlockValidationContext context,
                           StructuralNode block,
                           List<ValidationMessage> messages,
                           AudioBlock audioConfig) {
        
        Severity severity = resolveSeverity(urlConfig.getSeverity(), audioConfig.getSeverity());
        
        // Check if URL is required
        if (urlConfig.isRequired() && (url == null || url.trim().isEmpty())) {
            SourcePosition pos = findSourcePosition(block, context, url);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(URL_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .fromPosition(pos)
                    .build())
                .message("Audio URL is required but not provided")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("target")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Add audio URL")
                    .fixedValue("audio.mp3")
                    .addExample("audio::music.mp3[]")
                    .addExample("audio::sounds/notification.ogg[]")
                    .addExample("audio::https://example.com/audio.wav[]")
                    .build())
                .build());
            return;
        }
        
        if (url != null && !url.trim().isEmpty() && urlConfig.getPattern() != null) {
            // Validate URL pattern
            if (!urlConfig.getPattern().matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(URL_PATTERN)
                    .location(context.createLocation(block))
                    .message("Audio URL does not match required pattern")
                    .actualValue(url)
                    .expectedValue("Pattern: " + urlConfig.getPattern().pattern())
                    .addSuggestion(Suggestion.builder()
                        .description("Use valid audio URL format")
                        .addExample("audio::file.mp3[]")
                        .addExample("audio::sounds/music.wav[]")
                        .explanation("URL must match pattern: " + urlConfig.getPattern().pattern())
                        .build())
                    .build());
            }
        }
    }
    
    private void validateOptions(StructuralNode block, AudioBlock.OptionsConfig optionsConfig,
                               BlockValidationContext context,
                               List<ValidationMessage> messages,
                               AudioBlock audioConfig) {
        
        // Validate autoplay
        if (optionsConfig.getAutoplay() != null) {
            validateAutoplay(block, optionsConfig.getAutoplay(), context, messages, audioConfig);
        }
        
        // Validate controls
        if (optionsConfig.getControls() != null) {
            validateControls(block, optionsConfig.getControls(), context, messages, audioConfig);
        }
        
        // Validate loop
        if (optionsConfig.getLoop() != null) {
            validateLoop(block, optionsConfig.getLoop(), context, messages, audioConfig);
        }
    }
    
    private void validateAutoplay(StructuralNode block, AudioBlock.AutoplayConfig autoplayConfig,
                                BlockValidationContext context,
                                List<ValidationMessage> messages,
                                AudioBlock audioConfig) {
        
        Severity severity = resolveSeverity(autoplayConfig.getSeverity(), audioConfig.getSeverity());
        
        boolean hasAutoplay = hasOption(block, AUTOPLAY);
        
        if (!autoplayConfig.isAllowed() && hasAutoplay) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(OPTIONS_AUTOPLAY_NOT_ALLOWED)
                .location(context.createLocation(block))
                .message("Audio autoplay is not allowed")
                .actualValue("autoplay enabled")
                .expectedValue("autoplay must not be used")
                .addSuggestion(Suggestion.builder()
                    .description("Remove autoplay option")
                    .addExample("audio::file.mp3[options=controls]")
                    .addExample("Remove 'autoplay' from options attribute")
                    .explanation("Autoplay can be disruptive for users")
                    .build())
                .build());
        }
    }
    
    private void validateControls(StructuralNode block, AudioBlock.ControlsConfig controlsConfig,
                                BlockValidationContext context,
                                List<ValidationMessage> messages,
                                AudioBlock audioConfig) {
        
        Severity severity = resolveSeverity(controlsConfig.getSeverity(), audioConfig.getSeverity());
        
        boolean hasControls = !hasOption(block, NOCONTROLS);
        
        if (controlsConfig.isRequired() && !hasControls) {
            SourcePosition pos = findSourcePosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(OPTIONS_CONTROLS_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .fromPosition(pos)
                    .build())
                .message("Audio controls are required but not enabled")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("controls")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.ATTRIBUTE_VALUE)
                    .attributeName("options")
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Enable audio controls")
                    .addExample("audio::file.mp3[options=controls]")
                    .addExample("audio::file.mp3[opts=controls]")
                    .explanation("Controls allow users to play/pause the audio")
                    .build())
                .build());
        }
    }
    
    private void validateLoop(StructuralNode block, AudioBlock.LoopConfig loopConfig,
                            BlockValidationContext context,
                            List<ValidationMessage> messages,
                            AudioBlock audioConfig) {
        
        Severity severity = resolveSeverity(loopConfig.getSeverity(), audioConfig.getSeverity());
        
        boolean hasLoop = hasOption(block, LOOP);
        
        if (!loopConfig.isAllowed() && hasLoop) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(OPTIONS_LOOP_NOT_ALLOWED)
                .location(context.createLocation(block))
                .message("Audio loop is not allowed")
                .actualValue("loop enabled")
                .expectedValue("loop must not be used")
                .addSuggestion(Suggestion.builder()
                    .description("Remove loop option")
                    .addExample("audio::file.mp3[options=controls]")
                    .addExample("Remove 'loop' from options attribute")
                    .explanation("Looping audio can be annoying for users")
                    .build())
                .build());
        }
    }
    
    private boolean hasOption(StructuralNode block, String option) {
        Object opts = block.getAttribute(OPTS);
        if (opts != null) {
            String optsStr = opts.toString();
            return optsStr.contains(option);
        }
        
        // Also check individual attribute
        Object attr = block.getAttribute(option);
        return attr != null && !"false".equals(attr.toString());
    }
    
    private void validateTitle(String title, AudioBlock.TitleConfig titleConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages,
                             AudioBlock audioConfig) {
        
        Severity severity = resolveSeverity(titleConfig.getSeverity(), audioConfig.getSeverity());
        
        if (titleConfig.isRequired() && (title == null || title.trim().isEmpty())) {
            SourcePosition pos = findTitlePosition(block, context);
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId(TITLE_REQUIRED)
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .fromPosition(pos)
                    .build())
                .message("Audio title is required but not provided")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint(".Audio Title")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                    .build())
                .addSuggestion(Suggestion.builder()
                    .description("Add a title for the audio")
                    .addExample(".Background Music\naudio::music.mp3[]")
                    .addExample(".Podcast Episode\naudio::episode-01.mp3[]")
                    .explanation("Titles help identify audio content")
                    .build())
                .build());
            return;
        }
        
        if (title != null && !title.trim().isEmpty()) {
            // Validate min length
            if (titleConfig.getMinLength() != null && title.length() < titleConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_MIN_LENGTH)
                    .location(context.createLocation(block))
                    .message("Audio title is too short")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At least " + titleConfig.getMinLength() + " characters")
                    .addSuggestion(Suggestion.builder()
                        .description("Provide a more descriptive title")
                        .addExample("Instead of 'Audio', use 'Introduction Speech'")
                        .addExample("Instead of 'Sound', use 'Background Music Track'")
                        .build())
                    .build());
            }
            
            // Validate max length
            if (titleConfig.getMaxLength() != null && title.length() > titleConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId(TITLE_MAX_LENGTH)
                    .location(context.createLocation(block))
                    .message("Audio title is too long")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At most " + titleConfig.getMaxLength() + " characters")
                    .addSuggestion(Suggestion.builder()
                        .description("Shorten the title while keeping it descriptive")
                        .addExample(String.format("Keep title under %d characters", titleConfig.getMaxLength()))
                        .build())
                    .build());
            }
        }
    }
    
    /**
     * Finds the column position of URL in audio macro.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context, String url) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for audio:: macro pattern
        int audioStart = sourceLine.indexOf("audio::");
        if (audioStart >= 0) {
            int urlEnd = sourceLine.indexOf("[", audioStart);
            if (urlEnd == -1) {
                urlEnd = sourceLine.length();
            }
            
            if (url != null && !url.isEmpty()) {
                // Find the specific URL position
                int urlStart = sourceLine.indexOf(url, audioStart + 7);
                if (urlStart > audioStart && urlStart < urlEnd) {
                    return new SourcePosition(urlStart + 1, urlStart + url.length(), lineNum);
                }
            } else {
                // No URL - position after "audio::"
                return new SourcePosition(audioStart + 8, audioStart + 8, lineNum);
            }
        }
        
        return new SourcePosition(1, 1, lineNum);
    }
    
    /**
     * Finds the column position for controls attribute in audio macro.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for attributes bracket
        int bracketStart = sourceLine.indexOf("[");
        if (bracketStart >= 0) {
            int bracketEnd = sourceLine.indexOf("]", bracketStart);
            if (bracketEnd > bracketStart) {
                return new SourcePosition(bracketEnd + 1, bracketEnd + 1, lineNum);
            }
        }
        
        return new SourcePosition(1, 1, lineNum);
    }
    
    /**
     * Finds the column position for title in audio macro.
     */
    private SourcePosition findTitlePosition(StructuralNode block, BlockValidationContext context) {
        if (block.getSourceLocation() == null) {
            return new SourcePosition(1, 1, 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        
        // Title is typically on the line before the audio macro
        // Return position for the line before the audio
        return new SourcePosition(1, 1, lineNum);
    }
    
    
    
}