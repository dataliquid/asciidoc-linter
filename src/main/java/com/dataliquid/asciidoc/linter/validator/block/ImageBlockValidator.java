package com.dataliquid.asciidoc.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

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
        Object target = block.getAttribute("target");
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
        Object alt = block.getAttribute("alt");
        return alt != null ? alt.toString() : null;
    }
    
    private void validateUrl(String url, ImageBlock.UrlConfig urlConfig,
                           BlockValidationContext context,
                           StructuralNode block,
                           List<ValidationMessage> messages,
                           ImageBlock imageConfig) {
        
        // Check if URL is required
        if (urlConfig.isRequired() && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image.url.required")
                .location(context.createLocation(block))
                .message("Image must have a URL")
                .actualValue("No URL")
                .expectedValue("URL required")
                .build());
            return;
        }
        
        if (url != null && !url.trim().isEmpty() && urlConfig.getPattern() != null) {
            // Validate URL pattern
            if (!urlConfig.getPattern().matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(imageConfig.getSeverity())
                    .ruleId("image.url.pattern")
                    .location(context.createLocation(block))
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
        
        if (dimConfig.isRequired() && (valueStr == null || valueStr.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image." + dimensionName + ".required")
                .location(context.createLocation(block))
                .message("Image must have " + dimensionName + " specified")
                .actualValue("No " + dimensionName)
                .expectedValue(dimensionName + " required")
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
                        .location(context.createLocation(block))
                        .message("Image " + dimensionName + " is too small")
                        .actualValue(numericValue + "px")
                        .expectedValue("At least " + dimConfig.getMinValue() + "px")
                        .build());
                }
                
                if (dimConfig.getMaxValue() != null && numericValue > dimConfig.getMaxValue()) {
                    messages.add(ValidationMessage.builder()
                        .severity(imageConfig.getSeverity())
                        .ruleId("image." + dimensionName + ".max")
                        .location(context.createLocation(block))
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
        
        if (altConfig.isRequired() && (altText == null || altText.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(imageConfig.getSeverity())
                .ruleId("image.alt.required")
                .location(context.createLocation(block))
                .message("Image must have alt text")
                .actualValue("No alt text")
                .expectedValue("Alt text required")
                .build());
            return;
        }
        
        if (altText != null && !altText.trim().isEmpty()) {
            // Validate min length
            if (altConfig.getMinLength() != null && altText.length() < altConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(imageConfig.getSeverity())
                    .ruleId("image.alt.minLength")
                    .location(context.createLocation(block))
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
                    .location(context.createLocation(block))
                    .message("Image alt text is too long")
                    .actualValue(altText.length() + " characters")
                    .expectedValue("At most " + altConfig.getMaxLength() + " characters")
                    .build());
            }
        }
    }
}