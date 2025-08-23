package com.dataliquid.asciidoc.linter.validator.block;

import com.dataliquid.asciidoc.linter.validator.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.block.BlockAttributes.*;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.VideoBlock;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import static com.dataliquid.asciidoc.linter.validator.RuleIds.Video.*;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.Suggestion;
import com.dataliquid.asciidoc.linter.util.StringUtils;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Validator for video blocks in AsciiDoc documents. Validates video blocks
 * based on the YAML schema configuration including: - URL validation (required
 * and pattern matching) - Dimension constraints (width and height) - Poster
 * image validation - Controls requirement - Caption validation
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
    protected List<ValidationMessage> performSpecificValidations(StructuralNode node, VideoBlock videoConfig,
            BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();

        // Validate URL
        if (videoConfig.getUrl() != null) {
            validateUrl(node, videoConfig, messages, context);
        }

        // Validate dimensions
        if (videoConfig.getWidth() != null) {
            validateDimension(node, videoConfig, WIDTH, videoConfig.getWidth(), messages, context);
        }

        if (videoConfig.getHeight() != null) {
            validateDimension(node, videoConfig, HEIGHT, videoConfig.getHeight(), messages, context);
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

    private void validateUrl(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages,
            BlockValidationContext context) {
        VideoBlock.UrlConfig urlConfig = videoConfig.getUrl();
        String url = (String) node.getAttribute(TARGET);

        // Determine severity
        Severity severity = resolveSeverity(urlConfig.getSeverity(), videoConfig.getSeverity());

        // Check if required
        if (Boolean.TRUE.equals(urlConfig.getRequired()) && StringUtils.isBlank(url)) {
            SourcePosition pos = findSourcePosition(node, context, url);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(URL_REQUIRED)
                            .message("Video URL is required but not provided")
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("target")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
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
                SourcePosition pos = findSourcePosition(node, context, url);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(URL_PATTERN)
                                .message("Video URL does not match required pattern")
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .errorType(ErrorType.INVALID_PATTERN)
                                .actualValue(url)
                                .expectedValue(pattern.pattern())
                                .addSuggestion(Suggestion
                                        .builder()
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
            VideoBlock.DimensionConfig dimensionConfig, List<ValidationMessage> messages,
            BlockValidationContext context) {
        String dimensionStr = (String) node.getAttribute(dimensionType);

        // Determine severity
        Severity severity = resolveSeverity(dimensionConfig.getSeverity(), videoConfig.getSeverity());

        // Check if required
        if (Boolean.TRUE.equals(dimensionConfig.getRequired()) && StringUtils.isBlank(dimensionStr)) {
            SourcePosition pos = findSourcePosition(node, context, dimensionType, dimensionStr);

            // Check if there are existing attributes
            boolean hasOtherAttributes = false;
            if (dimensionType.equals(WIDTH)) {
                hasOtherAttributes = node.getAttribute(HEIGHT) != null || node.getAttribute(POSTER) != null
                        || node.getAttribute(OPTIONS) != null;
            } else if (dimensionType.equals(HEIGHT)) {
                hasOtherAttributes = node.getAttribute(WIDTH) != null || node.getAttribute(POSTER) != null
                        || node.getAttribute(OPTIONS) != null;
            }

            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(dimensionType.equals(WIDTH) ? WIDTH_REQUIRED : HEIGHT_REQUIRED)
                            .message(String.format("Video %s is required but not provided", dimensionType))
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint(dimensionType.equals(WIDTH) ? "640" : "360")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(hasOtherAttributes ? PlaceholderContext.PlaceholderType.ATTRIBUTE_IN_LIST
                                            : PlaceholderContext.PlaceholderType.ATTRIBUTE_VALUE)
                                    .attributeName(dimensionType)
                                    .hasExistingAttributes(hasOtherAttributes)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
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
                    messages
                            .add(ValidationMessage
                                    .builder()
                                    .severity(severity)
                                    .ruleId(dimensionType.equals(WIDTH) ? WIDTH_MIN : HEIGHT_MIN)
                                    .message(String.format("Video %s is below minimum value", dimensionType))
                                    .location(context.createLocation(node))
                                    .errorType(ErrorType.OUT_OF_RANGE)
                                    .actualValue(String.valueOf(value))
                                    .expectedValue(String.format(">= %d", dimensionConfig.getMinValue()))
                                    .addSuggestion(Suggestion
                                            .builder()
                                            .description(String
                                                    .format("Increase %s to at least %d pixels", dimensionType,
                                                            dimensionConfig.getMinValue()))
                                            .addExample(String
                                                    .format("%s=%d", dimensionType, dimensionConfig.getMinValue()))
                                            .build())
                                    .build());
                }

                if (dimensionConfig.getMaxValue() != null && value > dimensionConfig.getMaxValue()) {
                    messages
                            .add(ValidationMessage
                                    .builder()
                                    .severity(severity)
                                    .ruleId(dimensionType.equals(WIDTH) ? WIDTH_MAX : HEIGHT_MAX)
                                    .message(String.format("Video %s exceeds maximum value", dimensionType))
                                    .location(context.createLocation(node))
                                    .errorType(ErrorType.OUT_OF_RANGE)
                                    .actualValue(String.valueOf(value))
                                    .expectedValue(String.format("<= %d", dimensionConfig.getMaxValue()))
                                    .addSuggestion(Suggestion
                                            .builder()
                                            .description(String
                                                    .format("Reduce %s to at most %d pixels", dimensionType,
                                                            dimensionConfig.getMaxValue()))
                                            .addExample(String
                                                    .format("%s=%d", dimensionType, dimensionConfig.getMaxValue()))
                                            .build())
                                    .build());
                }
            } catch (NumberFormatException e) {
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(dimensionType.equals(WIDTH) ? WIDTH_INVALID : HEIGHT_INVALID)
                                .message(String.format("Video %s is not a valid number", dimensionType))
                                .location(context.createLocation(node))
                                .errorType(ErrorType.INVALID_PATTERN)
                                .actualValue(dimensionStr)
                                .expectedValue("numeric value")
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description(String.format("Use a numeric value for %s", dimensionType))
                                        .addExample(String.format("%s=640", dimensionType))
                                        .build())
                                .build());
            }
        }
    }

    private void validatePoster(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages,
            BlockValidationContext context) {
        VideoBlock.PosterConfig posterConfig = videoConfig.getPoster();
        String poster = (String) node.getAttribute(POSTER);

        // Determine severity
        Severity severity = resolveSeverity(posterConfig.getSeverity(), videoConfig.getSeverity());

        // Check if required
        if (Boolean.TRUE.equals(posterConfig.getRequired()) && StringUtils.isBlank(poster)) {
            SourcePosition pos = findPosterPosition(node, context, poster);

            // Check if there are existing attributes
            boolean hasOtherAttributes = node.getAttribute(WIDTH) != null || node.getAttribute(HEIGHT) != null
                    || node.getAttribute(OPTIONS) != null;

            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(POSTER_REQUIRED)
                            .message("Video poster image is required but not provided")
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint("thumbnail.jpg")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(hasOtherAttributes ? PlaceholderContext.PlaceholderType.ATTRIBUTE_IN_LIST
                                            : PlaceholderContext.PlaceholderType.ATTRIBUTE_VALUE)
                                    .attributeName("poster")
                                    .hasExistingAttributes(hasOtherAttributes)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
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
                SourcePosition pos = findPosterPosition(node, context, poster);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(POSTER_PATTERN)
                                .message("Video poster does not match required pattern")
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .errorType(ErrorType.INVALID_PATTERN)
                                .actualValue(poster)
                                .expectedValue(pattern.pattern())
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Use a poster image path matching the required pattern")
                                        .addExample("Common image formats: .jpg, .jpeg, .png, .webp")
                                        .build())
                                .build());
            }
        }
    }

    private void validateControls(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages,
            BlockValidationContext context) {
        VideoBlock.ControlsConfig controlsConfig = videoConfig.getOptions().getControls();
        String controlsAttr = (String) node.getAttribute(OPTIONS);

        // Determine severity
        Severity severity = resolveSeverity(controlsConfig.getSeverity(), videoConfig.getSeverity());

        // Check if controls are required
        if (Boolean.TRUE.equals(controlsConfig.getRequired())) {
            boolean hasControls = controlsAttr != null && controlsAttr.contains("controls");

            if (!hasControls) {
                SourcePosition pos = findSourcePosition(node, context);

                // Check if there are existing attributes
                boolean hasOtherAttributes = node.getAttribute(WIDTH) != null || node.getAttribute(HEIGHT) != null
                        || node.getAttribute(POSTER) != null;

                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(CONTROLS_REQUIRED)
                                .message("Video controls are required but not enabled")
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .errorType(ErrorType.MISSING_VALUE)
                                .missingValueHint("controls")
                                .placeholderContext(PlaceholderContext
                                        .builder()
                                        .type(hasOtherAttributes ? PlaceholderContext.PlaceholderType.ATTRIBUTE_IN_LIST
                                                : PlaceholderContext.PlaceholderType.ATTRIBUTE_VALUE)
                                        .attributeName("options")
                                        .hasExistingAttributes(hasOtherAttributes)
                                        .build())
                                .actualValue(controlsAttr)
                                .expectedValue("controls")
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Enable video player controls")
                                        .addExample("video::video.mp4[options=controls]")
                                        .addExample("video::video.mp4[opts=controls]")
                                        .build())
                                .build());
            }
        }
    }

    private void validateCaption(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages,
            BlockValidationContext context) {
        VideoBlock.CaptionConfig captionConfig = videoConfig.getCaption();
        String caption = (String) node.getAttribute(CAPTION);

        // If no caption attribute, check for title
        if (caption == null) {
            caption = node.getTitle();
        }

        // Determine severity
        Severity severity = resolveSeverity(captionConfig.getSeverity(), videoConfig.getSeverity());

        // Check if required
        if (Boolean.TRUE.equals(captionConfig.getRequired()) && StringUtils.isBlank(caption)) {
            SourcePosition pos = findCaptionPosition(node, context);
            messages
                    .add(ValidationMessage
                            .builder()
                            .severity(severity)
                            .ruleId(CAPTION_REQUIRED)
                            .message("Video caption is required but not provided")
                            .location(
                                    SourceLocation.builder().filename(context.getFilename()).fromPosition(pos).build())
                            .errorType(ErrorType.MISSING_VALUE)
                            .missingValueHint(".Video Title")
                            .placeholderContext(PlaceholderContext
                                    .builder()
                                    .type(PlaceholderContext.PlaceholderType.INSERT_BEFORE)
                                    .build())
                            .addSuggestion(Suggestion
                                    .builder()
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
                SourcePosition pos = findCaptionPosition(node, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(CAPTION_MIN_LENGTH)
                                .message("Video caption is too short")
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .errorType(ErrorType.OUT_OF_RANGE)
                                .actualValue(String.format("%d characters", length))
                                .expectedValue(String.format(">= %d characters", captionConfig.getMinLength()))
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Provide a more descriptive caption")
                                        .addExample(String
                                                .format("Caption should be at least %d characters",
                                                        captionConfig.getMinLength()))
                                        .build())
                                .build());
            }

            if (captionConfig.getMaxLength() != null && length > captionConfig.getMaxLength()) {
                SourcePosition pos = findCaptionPosition(node, context);
                messages
                        .add(ValidationMessage
                                .builder()
                                .severity(severity)
                                .ruleId(CAPTION_MAX_LENGTH)
                                .message("Video caption is too long")
                                .location(SourceLocation
                                        .builder()
                                        .filename(context.getFilename())
                                        .fromPosition(pos)
                                        .build())
                                .errorType(ErrorType.OUT_OF_RANGE)
                                .actualValue(String.format("%d characters", length))
                                .expectedValue(String.format("<= %d characters", captionConfig.getMaxLength()))
                                .addSuggestion(Suggestion
                                        .builder()
                                        .description("Shorten the caption to fit the limit")
                                        .addExample(String
                                                .format("Caption should be at most %d characters",
                                                        captionConfig.getMaxLength()))
                                        .build())
                                .build());
            }
        }
    }

    /**
     * Finds the column position of URL in video macro.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context, String url) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }

        String sourceLine = fileLines.get(lineNum - 1);

        // Look for video:: macro pattern
        int videoStart = sourceLine.indexOf("video::");
        if (videoStart >= 0) {
            int urlEnd = sourceLine.indexOf('[', videoStart);
            if (urlEnd == -1) {
                urlEnd = sourceLine.length();
            }

            if (url != null && !url.isEmpty()) {
                // Find the specific URL position
                int urlStart = sourceLine.indexOf(url, videoStart + 7);
                if (urlStart > videoStart && urlStart < urlEnd) {
                    return new SourcePosition(urlStart + 1, urlStart + url.length(), lineNum);
                }
            } else {
                // No URL - position after "video::"
                return new SourcePosition(videoStart + 8, videoStart + 8, lineNum);
            }
        }

        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Finds the column position of a dimension attribute (width/height) in video
     * macro.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context,
            String dimensionType, String dimensionValue) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }

        String sourceLine = fileLines.get(lineNum - 1);

        // Look for attributes bracket
        int bracketStart = sourceLine.indexOf('[');
        if (bracketStart >= 0) {
            int bracketEnd = sourceLine.indexOf(']', bracketStart);
            if (bracketEnd > bracketStart) {
                String attributes = sourceLine.substring(bracketStart + 1, bracketEnd);

                if (dimensionValue != null && !dimensionValue.isEmpty()) {
                    // Find specific dimension
                    String pattern = dimensionType + "=" + dimensionValue;
                    int dimStart = attributes.indexOf(pattern);
                    if (dimStart >= 0) {
                        return new SourcePosition(bracketStart + 2 + dimStart,
                                bracketStart + 2 + dimStart + pattern.length() - 1, lineNum);
                    }
                } else {
                    // Missing dimension - position at end of attributes
                    return new SourcePosition(bracketEnd + 1, bracketEnd + 1, lineNum);
                }
            }
        }

        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Finds the column position of poster attribute in video macro.
     */
    private SourcePosition findPosterPosition(StructuralNode block, BlockValidationContext context, String poster) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }

        String sourceLine = fileLines.get(lineNum - 1);

        // Look for attributes bracket
        int bracketStart = sourceLine.indexOf('[');
        if (bracketStart >= 0) {
            int bracketEnd = sourceLine.indexOf(']', bracketStart);
            if (bracketEnd > bracketStart) {
                String attributes = sourceLine.substring(bracketStart + 1, bracketEnd);

                if (poster != null && !poster.isEmpty()) {
                    // Find poster= pattern
                    String posterPattern = "poster=" + poster;
                    int posterStart = attributes.indexOf(posterPattern);
                    if (posterStart >= 0) {
                        // Position of the poster value (after "poster=")
                        int valueStart = bracketStart + 1 + posterStart + 7; // 7 = length of "poster="
                        return new SourcePosition(valueStart + 1, valueStart + poster.length(), lineNum);
                    }
                } else {
                    // No poster - position at end of attributes
                    return new SourcePosition(bracketEnd + 1, bracketEnd + 1, lineNum);
                }
            }
        }

        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Finds the column position for controls attribute in video macro.
     */
    private SourcePosition findSourcePosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new SourcePosition(1, 1, lineNum);
        }

        String sourceLine = fileLines.get(lineNum - 1);

        // Look for attributes bracket
        int bracketStart = sourceLine.indexOf('[');
        if (bracketStart >= 0) {
            int bracketEnd = sourceLine.indexOf(']', bracketStart);
            if (bracketEnd > bracketStart) {
                return new SourcePosition(bracketEnd + 1, bracketEnd + 1, lineNum);
            }
        }

        return new SourcePosition(1, 1, lineNum);
    }

    /**
     * Finds the column position for caption in video macro.
     */
    private SourcePosition findCaptionPosition(StructuralNode block, BlockValidationContext context) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new SourcePosition(1, 1,
                    block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }

        int videoLineNum = block.getSourceLocation().getLineNumber();
        String caption = block.getTitle();

        // Caption (title) is typically on the line before the video macro
        if (caption != null && !caption.isEmpty() && videoLineNum > 1) {
            // Check line before video
            int captionLineNum = videoLineNum - 1;
            if (captionLineNum <= fileLines.size()) {
                String captionLine = fileLines.get(captionLineNum - 1);

                // Check if line starts with "." followed by caption
                if (captionLine.startsWith(".")) {
                    // Caption starts at column 1 (the dot) and ends at the line length
                    return new SourcePosition(1, captionLine.length(), captionLineNum);
                }
            }
        }

        // Default to video line if caption not found
        return new SourcePosition(1, 1, videoLineNum);
    }

}
