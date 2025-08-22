package com.dataliquid.asciidoc.linter.validator.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;

/**
 * Factory for creating block type validators.
 */
public final class BlockValidatorFactory {

    private final Map<BlockType, BlockTypeValidator> validators;

    public BlockValidatorFactory() {
        this.validators = createValidators();
    }

    /**
     * Gets a validator for the specified block type.
     *
     * @param type
     *            the block type
     * @return the validator, or null if no validator exists for the type
     */
    public BlockTypeValidator getValidator(BlockType type) {
        return validators.get(type);
    }

    /**
     * Checks if a validator exists for the specified block type.
     *
     * @param type
     *            the block type
     * @return true if a validator exists, false otherwise
     */
    public boolean hasValidator(BlockType type) {
        return validators.containsKey(type);
    }

    /**
     * Creates and registers all available validators.
     */
    private Map<BlockType, BlockTypeValidator> createValidators() {
        Map<BlockType, BlockTypeValidator> map = new HashMap<>();

        // Register all validators
        registerValidator(map, new ParagraphBlockValidator());
        registerValidator(map, new TableBlockValidator());
        registerValidator(map, new ImageBlockValidator());
        registerValidator(map, new ListingBlockValidator());
        registerValidator(map, new VerseBlockValidator());
        registerValidator(map, new AdmonitionBlockValidator());
        registerValidator(map, new PassBlockValidator());
        registerValidator(map, new LiteralBlockValidator());
        registerValidator(map, new AudioBlockValidator());
        registerValidator(map, new QuoteBlockValidator());
        registerValidator(map, new SidebarBlockValidator());
        registerValidator(map, new ExampleBlockValidator());
        registerValidator(map, new VideoBlockValidator());
        registerValidator(map, new UlistBlockValidator());
        registerValidator(map, new DlistBlockValidator());

        return map;
    }

    /**
     * Registers a validator in the map.
     */
    private void registerValidator(Map<BlockType, BlockTypeValidator> map, BlockTypeValidator validator) {
        Objects.requireNonNull(validator, "[" + getClass().getName() + "] validator must not be null");
        BlockType type = validator.getSupportedType();

        if (type == null) {
            throw new IllegalStateException(
                    "Validator " + validator.getClass().getName() + " returned null from getSupportedType()");
        }

        if (map.containsKey(type)) {
            throw new IllegalStateException("Duplicate validator for type " + type + ": "
                    + map.get(type).getClass().getName() + " and " + validator.getClass().getName());
        }

        map.put(type, validator);
    }
}
