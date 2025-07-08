package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.AudioBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

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
        Object target = block.getAttribute("target");
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
        Object caption = block.getAttribute("caption");
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
        
        Severity severity = urlConfig.getSeverity() != null ? 
            urlConfig.getSeverity() : audioConfig.getSeverity();
        
        // Check if URL is required
        if (urlConfig.isRequired() && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("audio.url.required")
                .location(context.createLocation(block))
                .message("Audio must have a URL")
                .actualValue("No URL")
                .expectedValue("URL required")
                .build());
            return;
        }
        
        if (url != null && !url.trim().isEmpty() && urlConfig.getPattern() != null) {
            // Validate URL pattern
            if (!urlConfig.getPattern().matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("audio.url.pattern")
                    .location(context.createLocation(block))
                    .message("Audio URL does not match required pattern")
                    .actualValue(url)
                    .expectedValue("Pattern: " + urlConfig.getPattern().pattern())
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
        
        Severity severity = autoplayConfig.getSeverity() != null ? 
            autoplayConfig.getSeverity() : audioConfig.getSeverity();
        
        boolean hasAutoplay = hasOption(block, "autoplay");
        
        if (!autoplayConfig.isAllowed() && hasAutoplay) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("audio.options.autoplay.notAllowed")
                .location(context.createLocation(block))
                .message("Audio autoplay is not allowed")
                .actualValue("autoplay enabled")
                .expectedValue("autoplay must not be used")
                .build());
        }
    }
    
    private void validateControls(StructuralNode block, AudioBlock.ControlsConfig controlsConfig,
                                BlockValidationContext context,
                                List<ValidationMessage> messages,
                                AudioBlock audioConfig) {
        
        Severity severity = controlsConfig.getSeverity() != null ? 
            controlsConfig.getSeverity() : audioConfig.getSeverity();
        
        boolean hasControls = !hasOption(block, "nocontrols");
        
        if (controlsConfig.isRequired() && !hasControls) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("audio.options.controls.required")
                .location(context.createLocation(block))
                .message("Audio must display controls")
                .actualValue("controls hidden")
                .expectedValue("controls must be visible")
                .build());
        }
    }
    
    private void validateLoop(StructuralNode block, AudioBlock.LoopConfig loopConfig,
                            BlockValidationContext context,
                            List<ValidationMessage> messages,
                            AudioBlock audioConfig) {
        
        Severity severity = loopConfig.getSeverity() != null ? 
            loopConfig.getSeverity() : audioConfig.getSeverity();
        
        boolean hasLoop = hasOption(block, "loop");
        
        if (!loopConfig.isAllowed() && hasLoop) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("audio.options.loop.notAllowed")
                .location(context.createLocation(block))
                .message("Audio loop is not allowed")
                .actualValue("loop enabled")
                .expectedValue("loop must not be used")
                .build());
        }
    }
    
    private boolean hasOption(StructuralNode block, String option) {
        Object opts = block.getAttribute("opts");
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
        
        Severity severity = titleConfig.getSeverity() != null ? 
            titleConfig.getSeverity() : audioConfig.getSeverity();
        
        if (titleConfig.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("audio.title.required")
                .location(context.createLocation(block))
                .message("Audio must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null && !title.trim().isEmpty()) {
            // Validate min length
            if (titleConfig.getMinLength() != null && title.length() < titleConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("audio.title.minLength")
                    .location(context.createLocation(block))
                    .message("Audio title is too short")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At least " + titleConfig.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (titleConfig.getMaxLength() != null && title.length() > titleConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("audio.title.maxLength")
                    .location(context.createLocation(block))
                    .message("Audio title is too long")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At most " + titleConfig.getMaxLength() + " characters")
                    .build());
            }
        }
    }
}