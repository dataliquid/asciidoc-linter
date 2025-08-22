package com.dataliquid.asciidoc.linter.validator.block;

import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.dataliquid.asciidoc.linter.config.blocks.BlockType;
import com.dataliquid.asciidoc.linter.config.blocks.Block;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

/**
 * Interface for block type specific validators. Each block type (paragraph,
 * table, image, etc.) has its own implementation.
 * <p>
 * Validators work based on the YAML schema structure for block configurations.
 * The YAML configuration defines validation rules that are mapped to
 * corresponding block configuration classes (e.g.,
 * {@link com.dataliquid.asciidoc.linter.config.blocks.ListingBlock},
 * {@link com.dataliquid.asciidoc.linter.config.blocks.TableBlock}, etc.).
 * </p>
 * <p>
 * The YAML schema is defined in {@code src/main/resources/schemas/blocks/} and
 * provides the structure for block-specific validation rules including severity
 * levels, patterns, constraints, and nested rule configurations.
 * </p>
 *
 * @see com.dataliquid.asciidoc.linter.config.blocks.Block
 * @see com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader
 */
public interface BlockTypeValidator {

    /**
     * Returns the block type that this validator supports.
     *
     * @return the supported block type
     */
    BlockType getSupportedType();

    /**
     * Validates a block against its configuration.
     *
     * @param  block   the AsciidoctorJ block to validate
     * @param  config  the block configuration containing validation rules
     * @param  context the validation context containing section information
     *
     * @return         list of validation messages (errors, warnings, info)
     */
    List<ValidationMessage> validate(StructuralNode block, Block config, BlockValidationContext context);
}
