package com.dataliquid.asciidoc.linter.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import static com.dataliquid.asciidoc.linter.validator.RuleIds.Block.*;

import com.dataliquid.asciidoc.linter.config.BlockType;
import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.config.rule.SectionConfig;
import com.dataliquid.asciidoc.linter.validator.block.BlockContainer;
import com.dataliquid.asciidoc.linter.validator.block.BlockOccurrenceValidator;
import com.dataliquid.asciidoc.linter.validator.block.BlockTypeDetector;
import com.dataliquid.asciidoc.linter.validator.block.BlockTypeValidator;
import com.dataliquid.asciidoc.linter.validator.block.BlockValidationContext;
import com.dataliquid.asciidoc.linter.validator.block.BlockValidatorFactory;

/**
 * Main validator for blocks within sections.
 * Orchestrates block type validation and occurrence validation.
 */
public final class BlockValidator {
    
    private final BlockValidatorFactory validatorFactory;
    private final BlockTypeDetector typeDetector;
    private final BlockOccurrenceValidator occurrenceValidator;
    
    public BlockValidator() {
        this.validatorFactory = new BlockValidatorFactory();
        this.typeDetector = new BlockTypeDetector();
        this.occurrenceValidator = new BlockOccurrenceValidator();
    }
    
    /**
     * Validates all blocks at document level (level 0) against the configuration.
     * 
     * @param document the AsciiDoc document to validate
     * @param config the section configuration containing block rules (level 0)
     * @param filename the filename for error reporting
     * @return validation result containing all messages
     */
    public ValidationResult validate(Document document, 
                                   SectionConfig config,
                                   String filename) {
        Objects.requireNonNull(document, "[" + getClass().getName() + "] document must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        Objects.requireNonNull(filename, "[" + getClass().getName() + "] filename must not be null");
        
        // Use the generic validation method with a document container
        BlockContainer container = BlockContainer.fromDocument(document);
        BlockValidationContext context = new BlockValidationContext(document, filename);
        // Starting document validation
        return validateContainer(container, config, context, filename);
    }

    /**
     * Validates all blocks within a section against the configuration.
     * 
     * @param section the AsciiDoc section to validate
     * @param config the section configuration containing block rules
     * @param filename the filename for error reporting
     * @return validation result containing all messages
     */
    public ValidationResult validate(Section section, 
                                   SectionConfig config,
                                   String filename) {
        Objects.requireNonNull(section, "[" + getClass().getName() + "] section must not be null");
        Objects.requireNonNull(config, "[" + getClass().getName() + "] config must not be null");
        Objects.requireNonNull(filename, "[" + getClass().getName() + "] filename must not be null");
        
        // Use the generic validation method with a section container
        BlockContainer container = BlockContainer.fromSection(section);
        BlockValidationContext context = new BlockValidationContext(section, filename);
        return validateContainer(container, config, context, filename);
    }
    
    /**
     * Generic validation method for any block container.
     */
    private ValidationResult validateContainer(BlockContainer container,
                                             SectionConfig config,
                                             BlockValidationContext context,
                                             String filename) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        // No block validation if no blocks configured
        if (config.allowedBlocks() == null || config.allowedBlocks().isEmpty()) {
            return ValidationResult.builder()
                .addMessages(messages)
                .build();
        }
        
        // First pass: validate individual blocks and track occurrences
        validateContainerBlocks(container, config, context, messages);
        
        // Second pass: validate occurrences
        messages.addAll(occurrenceValidator.validate(context, config.allowedBlocks()));
        
        // Third pass: validate block order based on order attribute
        validateBlockOrder(container, config, context, messages);
        
        return ValidationResult.builder()
            .addMessages(messages)
            .build();
    }
    
    /**
     * Validates individual blocks from the container and tracks them in the context.
     */
    private void validateContainerBlocks(BlockContainer container,
                                       SectionConfig config,
                                       BlockValidationContext context,
                                       List<ValidationMessage> messages) {
        
        // Get all blocks from the container (handles preamble expansion automatically)
        List<StructuralNode> blocks = container.getBlocks();
        
        if (blocks.isEmpty()) {
            return;
        }
        
        for (StructuralNode block : blocks) {
            try {
                // Skip sections - they are handled by SectionValidator
                if (block instanceof Section) {
                    continue;
                }
                
                // Detect block type
                BlockType actualType = typeDetector.detectType(block);
                
                if (actualType == null) {
                    // Unknown block type - add validation message
                    messages.add(ValidationMessage.builder()
                        .severity(Severity.ERROR)
                        .ruleId(TYPE_UNKNOWN)
                        .location(context.createLocation(block))
                        .message("Unknown block type: " + block.getContext())
                        .actualValue(block.getContext())
                        .expectedValue("Valid AsciiDoc block type")
                        .build());
                    continue;
                }
                
                // Find matching configuration
                Block blockConfig = findBlockConfig(actualType, block, config.allowedBlocks());
                
                if (blockConfig == null) {
                    // Block type not allowed
                    messages.add(ValidationMessage.builder()
                        .severity(Severity.ERROR)
                        .ruleId(TYPE_NOT_ALLOWED)
                        .location(context.createLocation(block))
                        .message("Block type not allowed in " + container.getContainerType())
                        .actualValue(actualType.toString())
                        .expectedValue("One of the allowed block types")
                        .build());
                    continue;
                }
                
                // Track the block
                context.trackBlock(blockConfig, block);
                
                // Debug only for document container
                // Track the block
                
                // Validate if we have a validator for this type
                BlockTypeValidator validator = validatorFactory.getValidator(actualType);
                if (validator != null) {
                    messages.addAll(validator.validate(block, blockConfig, context));
                }
            } catch (Exception e) {
                // Handle validation exceptions gracefully
                messages.add(ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId(VALIDATION_ERROR)
                    .location(context.createLocation(block))
                    .message("Error validating block: " + e.getMessage())
                    .build());
            }
        }
    }

    /**
     * Finds the configuration for a specific block.
     * 
     * Matching logic:
     * 1. If block has a name attribute, try to find config with matching name and type
     * 2. Otherwise, find any config with matching type
     * 3. Config names are for identification only and don't prevent matching unnamed blocks
     */
    private Block findBlockConfig(BlockType type, 
                                        StructuralNode block,
                                        List<Block> configs) {
        // First try to match by name attribute if block has one
        Object nameAttr = block.getAttribute("name");
        if (nameAttr != null) {
            String blockName = nameAttr.toString();
            for (Block config : configs) {
                if (config.getType() == type && blockName.equals(config.getName())) {
                    return config;
                }
            }
        }
        
        // Then match by type only (config name is just for identification)
        for (Block config : configs) {
            if (config.getType() == type) {
                return config;
            }
        }
        
        return null;
    }
    
    /**
     * Special version of findBlockConfig that tracks which configs have already been matched.
     * This ensures each config is only matched once when multiple blocks of the same type exist.
     */
    private Block findBlockConfigForOrder(BlockType type, 
                                         StructuralNode block,
                                         List<Block> configs,
                                         Set<Block> alreadyMatched) {
        // First try to match by name attribute if block has one
        Object nameAttr = block.getAttribute("name");
        if (nameAttr != null) {
            String blockName = nameAttr.toString();
            for (Block config : configs) {
                if (config.getType() == type && 
                    blockName.equals(config.getName()) && 
                    !alreadyMatched.contains(config)) {
                    return config;
                }
            }
        }
        
        // Then match by type only, but skip already matched configs
        for (Block config : configs) {
            if (config.getType() == type && !alreadyMatched.contains(config)) {
                return config;
            }
        }
        
        return null;
    }
    
    /**
     * Validates block order based on the order attribute in block configurations.
     */
    private void validateBlockOrder(BlockContainer container, SectionConfig config, 
                                   BlockValidationContext context,
                                   List<ValidationMessage> messages) {
        List<Block> orderedBlocks = config.allowedBlocks().stream()
            .filter(block -> block.getOrder() != null)
            .sorted((b1, b2) -> b1.getOrder().compareTo(b2.getOrder()))
            .collect(Collectors.toList());
        
        if (orderedBlocks.isEmpty()) {
            return; // No order constraints
        }
        
        List<StructuralNode> blocks = container.getBlocks();
        List<Block> matchedBlockConfigs = new ArrayList<>();
        Set<Block> alreadyMatched = new HashSet<>();
        
        // First, match each document block to its configuration
        for (StructuralNode block : blocks) {
            if (block instanceof Section) {
                continue;
            }
            
            BlockType type = typeDetector.detectType(block);
            if (type == null) {
                continue;
            }
            
            Block blockConfig = findBlockConfigForOrder(type, block, config.allowedBlocks(), alreadyMatched);
            if (blockConfig != null && blockConfig.getOrder() != null) {
                matchedBlockConfigs.add(blockConfig);
                alreadyMatched.add(blockConfig);
            }
        }
        
        // Now check if the matched blocks are in the correct order
        for (int i = 0; i < matchedBlockConfigs.size() - 1; i++) {
            Block current = matchedBlockConfigs.get(i);
            Block next = matchedBlockConfigs.get(i + 1);
            
            if (current.getOrder() > next.getOrder()) {
                // Find the actual block positions for error reporting
                int currentBlockIndex = -1;
                int nextBlockIndex = -1;
                
                for (int j = 0; j < blocks.size(); j++) {
                    StructuralNode block = blocks.get(j);
                    if (block instanceof Section) {
                        continue;
                    }
                    
                    BlockType type = typeDetector.detectType(block);
                    if (type != null) {
                        Block blockCfg = findBlockConfig(type, block, config.allowedBlocks());
                        if (blockCfg == current && currentBlockIndex == -1) {
                            currentBlockIndex = j;
                        } else if (blockCfg == next && nextBlockIndex == -1) {
                            nextBlockIndex = j;
                        }
                    }
                }
                
                String currentKey = current.getName() != null ? current.getName() : current.getType().toString();
                String nextKey = next.getName() != null ? next.getName() : next.getType().toString();
                
                messages.add(ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId(ORDER)
                    .location(context.createLocation(blocks.get(currentBlockIndex)))
                    .message("Block order violation: '" + currentKey + "' (order=" + current.getOrder() + 
                            ") appears after '" + nextKey + "' (order=" + next.getOrder() + ")")
                    .actualValue(currentKey + " at position " + i)
                    .expectedValue(currentKey + " should appear before " + nextKey)
                    .build());
            }
        }
    }
}