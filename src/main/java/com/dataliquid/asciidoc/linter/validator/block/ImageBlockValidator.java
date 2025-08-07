package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.report.console.FileContentCache;
import com.dataliquid.asciidoc.linter.validator.ErrorType;
import com.dataliquid.asciidoc.linter.validator.PlaceholderContext;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Validator for image blocks in AsciiDoc documents.
 * 
 * <p>This validator validates image blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/image-block.yaml}.
 * The YAML configuration is parsed into {@link ImageBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>url</b>: Validates image URL/path (required, pattern matching)</li>
 *   <li><b>width</b>: Validates image width constraints (required, min/max values)</li>
 *   <li><b>height</b>: Validates image height constraints (required, min/max values)</li>
 *   <li><b>alt</b>: Validates alternative text (required, length constraints)</li>
 * </ul>
 * 
 * <p>Note: Image block nested configurations do not support individual severity levels.
 * All validations use the block-level severity.</p>
 * 
 * @see ImageBlock
 * @see BlockTypeValidator
 */
public final class ImageBlockValidator extends AbstractBlockValidator<ImageBlock> {
    private static final Logger logger = LogManager.getLogger(ImageBlockValidator.class);
    private final FileContentCache fileCache = new FileContentCache();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.IMAGE;
    }
    
    @Override
    protected Class<ImageBlock> getBlockConfigClass() {
        return ImageBlock.class;
    }
    
    @Override
    protected List<ValidationMessage> performSpecificValidations(StructuralNode block, 
                                                               ImageBlock imageConfig,
                                                               BlockValidationContext context) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get image attributes
        String imageUrl = getImageUrl(block);
        String altText = getAltText(block);
        
        // Validate URL
        if (imageConfig.getUrl() != null) {
            validateUrl(imageUrl, imageConfig.getUrl(), context, block, messages, imageConfig);
        }
        
        // Validate width
        if (imageConfig.getWidth() != null) {
            validateDimension(block, "width", imageConfig.getWidth(), context, messages, imageConfig);
        }
        
        // Validate height
        if (imageConfig.getHeight() != null) {
            validateDimension(block, "height", imageConfig.getHeight(), context, messages, imageConfig);
        }
        
        // Validate alt text
        if (imageConfig.getAlt() != null) {
            validateAltText(altText, imageConfig.getAlt(), context, block, messages, imageConfig);
        }
        
        return messages;
    }
    
    private String getImageUrl(StructuralNode block) {
        // Try different ways to get image URL
        Object target = block.getAttribute(BlockAttributes.TARGET);
        if (target != null) {
            return target.toString();
        }
        
        // For image blocks, the content might contain the path
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        return null;
    }
    
    private String getAltText(StructuralNode block) {
        Object alt = block.getAttribute(BlockAttributes.ALT);
        if (alt != null) {
            String altStr = alt.toString();
            // AsciidoctorJ generates default alt text from filename when none is provided
            // e.g., "missing-image.png" becomes "missing image"
            Object target = block.getAttribute(BlockAttributes.TARGET);
            if (target != null) {
                String targetStr = target.toString();
                // Remove file extension and convert to expected alt text format
                String generatedAlt = targetStr
                    .replaceFirst("\\.[^.]+$", "")  // Remove extension
                    .replace("-", " ")              // Replace hyphens with spaces
                    .replace("_", " ");             // Replace underscores with spaces
                
                if (altStr.equals(generatedAlt)) {
                    // Alt text was auto-generated from filename - treat as missing
                    logger.debug("Detected auto-generated alt text '{}' from target '{}'", altStr, targetStr);
                    return null;
                }
            }
            return altStr;
        }
        return null;
    }
    
    private void validateUrl(String url, ImageBlock.UrlConfig urlConfig,
                           BlockValidationContext context,
                           StructuralNode block,
                           List<ValidationMessage> messages,
                           ImageBlock imageConfig) {
        
        UrlPosition pos = findUrlPosition(block, context, url);
        
        // Check if URL is required
        if (urlConfig.isRequired() && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image.url.required")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Image must have a URL")
                .actualValue("No URL")
                .expectedValue("URL required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("filename.png")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (url != null && !url.trim().isEmpty() && urlConfig.getPattern() != null) {
            // Validate URL pattern
            if (!urlConfig.getPattern().matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(imageConfig.getSeverity())
                    .ruleId("image.url.pattern")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Image URL does not match required pattern")
                    .actualValue(url)
                    .expectedValue("Pattern: " + urlConfig.getPattern().pattern())
                    .build());
            }
        }
    }
    
    private void validateDimension(StructuralNode block, String dimensionName,
                                 ImageBlock.DimensionConfig dimConfig,
                                 BlockValidationContext context,
                                 List<ValidationMessage> messages,
                                 ImageBlock imageConfig) {
        
        Object value = block.getAttribute(dimensionName);
        String valueStr = value != null ? value.toString() : null;
        DimensionPosition pos = findDimensionPosition(block, context, dimensionName, valueStr);
        
        if (dimConfig.isRequired() && (valueStr == null || valueStr.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image." + dimensionName + ".required")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Image must have " + dimensionName + " specified")
                .actualValue("No " + dimensionName)
                .expectedValue(dimensionName + " required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("100")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.ATTRIBUTE_IN_LIST)
                    .attributeName(dimensionName)
                    .hasExistingAttributes(true)
                    .build())
                .build());
            return;
        }
        
        if (valueStr != null) {
            Integer numericValue = parseNumericValue(valueStr);
            if (numericValue != null) {
                // Validate min/max
                if (dimConfig.getMinValue() != null && numericValue < dimConfig.getMinValue()) {
                    messages.add(ValidationMessage.builder()
                        .severity(imageConfig.getSeverity())
                        .ruleId("image." + dimensionName + ".min")
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Image " + dimensionName + " is too small")
                        .actualValue(numericValue + "px")
                        .expectedValue("At least " + dimConfig.getMinValue() + "px")
                        .build());
                }
                
                if (dimConfig.getMaxValue() != null && numericValue > dimConfig.getMaxValue()) {
                    messages.add(ValidationMessage.builder()
                        .severity(imageConfig.getSeverity())
                        .ruleId("image." + dimensionName + ".max")
                        .location(SourceLocation.builder()
                            .filename(context.getFilename())
                            .startLine(pos.lineNumber)
                            .endLine(pos.lineNumber)
                            .startColumn(pos.startColumn)
                            .endColumn(pos.endColumn)
                            .build())
                        .message("Image " + dimensionName + " is too large")
                        .actualValue(numericValue + "px")
                        .expectedValue("At most " + dimConfig.getMaxValue() + "px")
                        .build());
                }
            }
        }
    }
    
    private Integer parseNumericValue(String value) {
        if (value == null) return null;
        
        // Remove units (px, %, etc) and parse
        String numeric = value.replaceAll("[^0-9]", "");
        if (!numeric.isEmpty()) {
            try {
                return Integer.parseInt(numeric);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private void validateAltText(String altText, ImageBlock.AltTextConfig altConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages,
                               ImageBlock imageConfig) {
        
        AltTextPosition pos = findAltTextPosition(block, context, altText);
        
        if (altConfig.isRequired() && (altText == null || altText.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image.alt.required")
                .location(SourceLocation.builder()
                    .filename(context.getFilename())
                    .startLine(pos.lineNumber)
                    .endLine(pos.lineNumber)
                    .startColumn(pos.startColumn)
                    .endColumn(pos.endColumn)
                    .build())
                .message("Image must have alt text")
                .actualValue("No alt text")
                .expectedValue("Alt text required")
                .errorType(ErrorType.MISSING_VALUE)
                .missingValueHint("Alt text")
                .placeholderContext(PlaceholderContext.builder()
                    .type(PlaceholderContext.PlaceholderType.SIMPLE_VALUE)
                    .build())
                .build());
            return;
        }
        
        if (altText != null && !altText.trim().isEmpty()) {
            // Validate min length
            if (altConfig.getMinLength() != null && altText.length() < altConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(imageConfig.getSeverity())
                    .ruleId("image.alt.minLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Image alt text is too short")
                    .actualValue(altText.length() + " characters")
                    .expectedValue("At least " + altConfig.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (altConfig.getMaxLength() != null && altText.length() > altConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(imageConfig.getSeverity())
                    .ruleId("image.alt.maxLength")
                    .location(SourceLocation.builder()
                        .filename(context.getFilename())
                        .startLine(pos.lineNumber)
                        .endLine(pos.lineNumber)
                        .startColumn(pos.startColumn)
                        .endColumn(pos.endColumn)
                        .build())
                    .message("Image alt text is too long")
                    .actualValue(altText.length() + " characters")
                    .expectedValue("At most " + altConfig.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    /**
     * Finds the column position of alt text in image macro.
     */
    private AltTextPosition findAltTextPosition(StructuralNode block, BlockValidationContext context, String altText) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new AltTextPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new AltTextPosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for image:: macro pattern
        int imageStart = sourceLine.indexOf("image::");
        if (imageStart >= 0) {
            int bracketStart = sourceLine.indexOf("[", imageStart);
            if (bracketStart > imageStart) {
                int bracketEnd = sourceLine.indexOf("]", bracketStart);
                if (bracketEnd > bracketStart) {
                    if (altText != null && !altText.isEmpty()) {
                        // Find the alt text position
                        int altStart = sourceLine.indexOf(altText, bracketStart);
                        if (altStart > bracketStart && altStart < bracketEnd) {
                            return new AltTextPosition(altStart + 1, altStart + altText.length(), lineNum);
                        }
                    } else {
                        // No alt text - position inside brackets
                        if (bracketEnd == bracketStart + 1) {
                            // Empty brackets []
                            // bracketStart is 0-based index, +1 converts to 1-based, +1 more to position after [
                            return new AltTextPosition(bracketStart + 2, bracketStart + 2, lineNum);
                        } else {
                            // Has content but no alt text
                            return new AltTextPosition(bracketStart + 2, bracketEnd, lineNum);
                        }
                    }
                }
            }
        }
        
        return new AltTextPosition(1, 1, lineNum);
    }
    
    private static class AltTextPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        AltTextPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    /**
     * Finds the column position of URL in image macro.
     */
    private UrlPosition findUrlPosition(StructuralNode block, BlockValidationContext context, String url) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new UrlPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new UrlPosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for image:: macro pattern
        int imageStart = sourceLine.indexOf("image::");
        if (imageStart >= 0) {
            int urlEnd = sourceLine.indexOf("[", imageStart);
            if (urlEnd == -1) {
                urlEnd = sourceLine.length();
            }
            
            if (url != null && !url.isEmpty()) {
                // Find the specific URL position
                int urlStart = sourceLine.indexOf(url, imageStart + 7);
                if (urlStart > imageStart && urlStart < urlEnd) {
                    return new UrlPosition(urlStart + 1, urlStart + url.length(), lineNum);
                }
            } else {
                // No URL - position after "image::"
                return new UrlPosition(imageStart + 8, imageStart + 8, lineNum);
            }
        }
        
        return new UrlPosition(1, 1, lineNum);
    }
    
    private static class UrlPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        UrlPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
    
    /**
     * Finds the column position of a dimension attribute (width/height) in image macro.
     */
    private DimensionPosition findDimensionPosition(StructuralNode block, BlockValidationContext context, 
                                                   String dimensionName, String value) {
        List<String> fileLines = fileCache.getFileLines(context.getFilename());
        if (fileLines.isEmpty() || block.getSourceLocation() == null) {
            return new DimensionPosition(1, 1, block.getSourceLocation() != null ? block.getSourceLocation().getLineNumber() : 1);
        }
        
        int lineNum = block.getSourceLocation().getLineNumber();
        if (lineNum <= 0 || lineNum > fileLines.size()) {
            return new DimensionPosition(1, 1, lineNum);
        }
        
        String sourceLine = fileLines.get(lineNum - 1);
        
        // Look for the dimension attribute pattern
        String pattern = dimensionName + "=";
        int attrStart = sourceLine.indexOf(pattern);
        
        if (attrStart >= 0) {
            int valueStart = attrStart + pattern.length();
            
            if (value != null && !value.isEmpty()) {
                // Find where the value ends
                int valueEnd = valueStart + value.length();
                return new DimensionPosition(valueStart + 1, valueEnd, lineNum);
            } else {
                // Empty value - position after "width=" or "height="
                return new DimensionPosition(valueStart + 1, valueStart + 1, lineNum);
            }
        } else {
            // Dimension attribute not found - need to add it
            // Find position before closing bracket
            int bracketEnd = sourceLine.lastIndexOf("]");
            if (bracketEnd > 0) {
                // Return position of the bracket (1-based)
                return new DimensionPosition(bracketEnd + 1, bracketEnd + 1, lineNum);
            }
        }
        
        return new DimensionPosition(1, 1, lineNum);
    }
    
    private static class DimensionPosition {
        final int startColumn;
        final int endColumn;
        final int lineNumber;
        
        DimensionPosition(int startColumn, int endColumn, int lineNumber) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.lineNumber = lineNumber;
        }
    }
}