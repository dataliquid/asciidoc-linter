package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.Suggestion;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for video blocks in AsciiDoc documents.
 * 
 * Validates video blocks based on the YAML schema configuration including:
 * - URL validation (required and pattern matching)
 * - Dimension constraints (width and height)
 * - Poster image validation
 * - Controls requirement
 * - Caption validation
 */
public final class VideoBlockValidator extends AbstractBlockValidator<VideoBlock> {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.VIDEO;
    }
    
    @Override
    protected Class<VideoBlock> getBlockConfigClass() {
        return VideoBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode node, 
                                                               VideoBlock videoConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate URL
        if (videoConfig.getUrl() != null) {
            validateUrl(node, videoConfig, messages, context);
        }
        
        // Validate dimensions
        if (videoConfig.getWidth() != null) {
            validateDimension(node, videoConfig, "width", videoConfig.getWidth(), messages, context);
        }
        
        if (videoConfig.getHeight() != null) {
            validateDimension(node, videoConfig, "height", videoConfig.getHeight(), messages, context);
        }
        
        // Validate poster
        if (videoConfig.getPoster() != null) {
            validatePoster(node, videoConfig, messages, context);
        }
        
        // Validate options (controls)
        if (videoConfig.getOptions() != null && videoConfig.getOptions().getControls() != null) {
            validateControls(node, videoConfig, messages, context);
        }
        
        // Validate caption
        if (videoConfig.getCaption() != null) {
            validateCaption(node, videoConfig, messages, context);
        }
        
        return messages;
    }
    
    private void validateUrl(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.UrlConfig urlConfig = videoConfig.getUrl();
        String url = (String) node.getAttribute("target");
        
        // Determine severity
        Severity severity = urlConfig.getSeverity() != null ? 
            urlConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(urlConfig.getRequired()) && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.url.required")
                    .message("Video URL is required but not provided")
                    .location(context.createLocation(node))
                    .errorType(ErrorType.MISSING_VALUE)
                    .missingValueHint("target")
                    .addSuggestion(Suggestion.builder()
                        .description("Add a video URL using the target attribute")
                        .addExample("video::https://www.youtube.com/embed/VIDEO_ID[width=640,height=360]")
                        .addExample("video::path/to/video.mp4[]")
                        .build())
                    .build());
            return;
        }
        
        // Check pattern
        if (url != null && urlConfig.getPattern() != null) {
            Pattern pattern = urlConfig.getPattern();
            if (!pattern.matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.url.pattern")
                        .message("Video URL does not match required pattern")
                        .location(context.createLocation(node))
                        .errorType(ErrorType.INVALID_PATTERN)
                        .actualValue(url)
                        .expectedValue(pattern.pattern())
                        .addSuggestion(Suggestion.builder()
                            .description("Use a URL matching the required pattern")
                            .addExample("For YouTube: https://www.youtube.com/embed/VIDEO_ID")
                            .addExample("For Vimeo: https://player.vimeo.com/video/VIDEO_ID")
                            .addExample("For local files: path/to/video.mp4")
                            .build())
                        .build());
            }
        }
    }
    
    private void validateDimension(StructuralNode node, VideoBlock videoConfig, String dimensionType, 
                                  VideoBlock.DimensionConfig dimensionConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        String dimensionStr = (String) node.getAttribute(dimensionType);
        
        // Determine severity
        Severity severity = dimensionConfig.getSeverity() != null ? 
            dimensionConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(dimensionConfig.getRequired()) && 
            (dimensionStr == null || dimensionStr.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video." + dimensionType + ".required")
                    .message(String.format("Video %s is required but not provided", dimensionType))
                    .location(context.createLocation(node))
                    .errorType(ErrorType.MISSING_VALUE)
                    .missingValueHint(dimensionType)
                    .addSuggestion(Suggestion.builder()
                        .description(String.format("Add %s attribute to video block", dimensionType))
                        .addExample(String.format("video::video.mp4[%s=640]", dimensionType))
                        .addExample("Common 16:9 dimensions: width=640,height=360 or width=1280,height=720")
                        .build())
                    .build());
            return;
        }
        
        // Validate dimension value
        if (dimensionStr != null) {
            try {
                int value = Integer.parseInt(dimensionStr);
                
                if (dimensionConfig.getMinValue() != null && value < dimensionConfig.getMinValue()) {
                    messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId("video." + dimensionType + ".min")
                            .message(String.format("Video %s is below minimum value", dimensionType))
                            .location(context.createLocation(node))
                            .errorType(ErrorType.OUT_OF_RANGE)
                            .actualValue(String.valueOf(value))
                            .expectedValue(String.format(">= %d", dimensionConfig.getMinValue()))
                            .addSuggestion(Suggestion.builder()
                                .description(String.format("Increase %s to at least %d pixels", dimensionType, dimensionConfig.getMinValue()))
                                .addExample(String.format("%s=%d", dimensionType, dimensionConfig.getMinValue()))
                                .build())
                            .build());
                }
                
                if (dimensionConfig.getMaxValue() != null && value > dimensionConfig.getMaxValue()) {
                    messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId("video." + dimensionType + ".max")
                            .message(String.format("Video %s exceeds maximum value", dimensionType))
                            .location(context.createLocation(node))
                            .errorType(ErrorType.OUT_OF_RANGE)
                            .actualValue(String.valueOf(value))
                            .expectedValue(String.format("<= %d", dimensionConfig.getMaxValue()))
                            .addSuggestion(Suggestion.builder()
                                .description(String.format("Reduce %s to at most %d pixels", dimensionType, dimensionConfig.getMaxValue()))
                                .addExample(String.format("%s=%d", dimensionType, dimensionConfig.getMaxValue()))
                                .build())
                            .build());
                }
            } catch (NumberFormatException e) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video." + dimensionType + ".invalid")
                        .message(String.format("Video %s is not a valid number", dimensionType))
                        .location(context.createLocation(node))
                        .errorType(ErrorType.INVALID_PATTERN)
                        .actualValue(dimensionStr)
                        .expectedValue("numeric value")
                        .addSuggestion(Suggestion.builder()
                            .description(String.format("Use a numeric value for %s", dimensionType))
                            .addExample(String.format("%s=640", dimensionType))
                            .build())
                        .build());
            }
        }
    }
    
    private void validatePoster(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.PosterConfig posterConfig = videoConfig.getPoster();
        String poster = (String) node.getAttribute("poster");
        
        // Determine severity
        Severity severity = posterConfig.getSeverity() != null ? 
            posterConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(posterConfig.getRequired()) && (poster == null || poster.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.poster.required")
                    .message("Video poster image is required but not provided")
                    .location(context.createLocation(node))
                    .errorType(ErrorType.MISSING_VALUE)
                    .missingValueHint("poster")
                    .addSuggestion(Suggestion.builder()
                        .description("Add a poster image for the video")
                        .addExample("video::video.mp4[poster=thumbnail.jpg]")
                        .addExample("poster=images/video-preview.png")
                        .build())
                    .build());
            return;
        }
        
        // Check pattern
        if (poster != null && posterConfig.getPattern() != null) {
            Pattern pattern = posterConfig.getPattern();
            if (!pattern.matcher(poster).matches()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.poster.pattern")
                        .message("Video poster does not match required pattern")
                        .location(context.createLocation(node))
                        .errorType(ErrorType.INVALID_PATTERN)
                        .actualValue(poster)
                        .expectedValue(pattern.pattern())
                        .addSuggestion(Suggestion.builder()
                            .description("Use a poster image path matching the required pattern")
                            .addExample("Common image formats: .jpg, .jpeg, .png, .webp")
                            .build())
                        .build());
            }
        }
    }
    
    private void validateControls(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.ControlsConfig controlsConfig = videoConfig.getOptions().getControls();
        String controlsAttr = (String) node.getAttribute("options");
        
        // Determine severity
        Severity severity = controlsConfig.getSeverity() != null ? 
            controlsConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if controls are required
        if (Boolean.TRUE.equals(controlsConfig.getRequired())) {
            boolean hasControls = controlsAttr != null && controlsAttr.contains("controls");
            
            if (!hasControls) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.controls.required")
                        .message("Video controls are required but not enabled")
                        .location(context.createLocation(node))
                        .errorType(ErrorType.MISSING_VALUE)
                        .missingValueHint("options=controls")
                        .actualValue(controlsAttr)
                        .expectedValue("controls")
                        .addSuggestion(Suggestion.builder()
                            .description("Enable video player controls")
                            .addExample("video::video.mp4[options=controls]")
                            .addExample("video::video.mp4[opts=controls]")
                            .autoFixable(true)
                            .build())
                        .build());
            }
        }
    }
    
    private void validateCaption(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.CaptionConfig captionConfig = videoConfig.getCaption();
        String caption = (String) node.getAttribute("caption");
        
        // If no caption attribute, check for title
        if (caption == null) {
            caption = node.getTitle();
        }
        
        // Determine severity
        Severity severity = captionConfig.getSeverity() != null ? 
            captionConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(captionConfig.getRequired()) && (caption == null || caption.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.caption.required")
                    .message("Video caption is required but not provided")
                    .location(context.createLocation(node))
                    .errorType(ErrorType.MISSING_VALUE)
                    .missingValueHint("caption or .Title")
                    .addSuggestion(Suggestion.builder()
                        .description("Add a caption or title to the video")
                        .addExample(".Video Title\nvideo::video.mp4[]")
                        .addExample("video::video.mp4[caption=\"My Video\"]")
                        .build())
                    .build());
            return;
        }
        
        // Check length constraints
        if (caption != null) {
            int length = caption.length();
            
            if (captionConfig.getMinLength() != null && length < captionConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.caption.minLength")
                        .message("Video caption is too short")
                        .location(context.createLocation(node))
                        .errorType(ErrorType.OUT_OF_RANGE)
                        .actualValue(String.format("%d characters", length))
                        .expectedValue(String.format(">= %d characters", captionConfig.getMinLength()))
                        .addSuggestion(Suggestion.builder()
                            .description("Provide a more descriptive caption")
                            .addExample(String.format("Caption should be at least %d characters", captionConfig.getMinLength()))
                            .build())
                        .build());
            }
            
            if (captionConfig.getMaxLength() != null && length > captionConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.caption.maxLength")
                        .message("Video caption is too long")
                        .location(context.createLocation(node))
                        .errorType(ErrorType.OUT_OF_RANGE)
                        .actualValue(String.format("%d characters", length))
                        .expectedValue(String.format("<= %d characters", captionConfig.getMaxLength()))
                        .addSuggestion(Suggestion.builder()
                            .description("Shorten the caption to fit the limit")
                            .addExample(String.format("Caption should be at most %d characters", captionConfig.getMaxLength()))
                            .build())
                        .build());
            }
        }
    }
}