package com.dataliquid.asciidoc.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.blocks.ImageBlock;
import com.dataliquid.asciidoc.linter.config.blocks.ParagraphBlock;
import com.dataliquid.asciidoc.linter.config.blocks.TableBlock;
import com.dataliquid.asciidoc.linter.config.rule.OrderConfig;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("BlockOrderValidator")
class BlockOrderValidatorTest {

    private BlockOrderValidator validator;
    private BlockValidationContext context;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        validator = new BlockOrderValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return empty list when context is null")
        void shouldReturnEmptyListWhenContextIsNull() {
            // Given
            OrderConfig orderConfig = new OrderConfig(null, null, null, Severity.ERROR);

            // When
            List<ValidationMessage> messages = validator.validate(null, orderConfig);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when order config is null")
        void shouldReturnEmptyListWhenOrderConfigIsNull() {
            // Given/When
            List<ValidationMessage> messages = validator.validate(context, null);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no order rules configured")
        void shouldReturnEmptyListWhenNoOrderRulesConfigured() {
            // Given
            OrderConfig orderConfig = new OrderConfig(null, null, null, Severity.ERROR);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("fixed order validation")
    class FixedOrderValidation {

        @Test
        @DisplayName("should validate correct fixed order")
        void shouldValidateCorrectFixedOrder() {
            // Given
            OrderConfig orderConfig = new OrderConfig(Arrays.asList("intro", "content", "summary"), null, null,
                    Severity.ERROR);

            // Add blocks in correct order
            ParagraphBlock introBlock = ParagraphBlock.builder().name("intro").severity(Severity.ERROR).build();
            ParagraphBlock contentBlock = ParagraphBlock.builder().name("content").severity(Severity.ERROR).build();
            TableBlock summaryBlock = TableBlock.builder().name("summary").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);

            context.trackBlock(introBlock, node1);
            context.trackBlock(contentBlock, node2);
            context.trackBlock(summaryBlock, node3);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should validate incorrect fixed order")
        void shouldValidateIncorrectFixedOrder() {
            // Given
            OrderConfig orderConfig = new OrderConfig(Arrays.asList("intro", "content", "summary"), null, null,
                    Severity.ERROR);

            // Add blocks in wrong order (content before intro)
            ParagraphBlock introBlock = ParagraphBlock.builder().name("intro").severity(Severity.ERROR).build();
            ParagraphBlock contentBlock = ParagraphBlock.builder().name("content").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            context.trackBlock(contentBlock, node1); // Wrong: content first
            context.trackBlock(introBlock, node2); // Wrong: intro second

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("block.order.fixed", msg.getRuleId());
            assertEquals("Block 'intro' appears out of order", msg.getMessage());
            assertEquals("Position 2", msg.getActualValue().orElse(null));
            assertEquals("Should appear after 'content'", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should handle partial fixed order")
        void shouldHandlePartialFixedOrder() {
            // Given
            OrderConfig orderConfig = new OrderConfig(Arrays.asList("header", "content"), null, null, Severity.WARN);

            // Add blocks including some not in fixed order
            ParagraphBlock headerBlock = ParagraphBlock.builder().name("header").severity(Severity.ERROR).build();
            ImageBlock imageBlock = new ImageBlock("diagram", Severity.ERROR, null, null, null, null, null, null);
            ParagraphBlock contentBlock = ParagraphBlock.builder().name("content").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);

            context.trackBlock(headerBlock, node1);
            context.trackBlock(imageBlock, node2); // Not in fixed order - OK
            context.trackBlock(contentBlock, node3);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty()); // Image block doesn't affect order
        }
    }

    @Nested
    @DisplayName("before constraint validation")
    class BeforeConstraintValidation {

        @Test
        @DisplayName("should validate satisfied before constraint")
        void shouldValidateSatisfiedBeforeConstraint() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint
                    .of("introduction", "conclusion", Severity.ERROR);
            OrderConfig orderConfig = new OrderConfig(null, Arrays.asList(constraint), null, Severity.ERROR);

            // Add blocks in correct order
            ParagraphBlock introBlock = ParagraphBlock.builder().name("introduction").severity(Severity.ERROR).build();
            ParagraphBlock conclusionBlock = ParagraphBlock
                    .builder()
                    .name("conclusion")
                    .severity(Severity.ERROR)
                    .build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            context.trackBlock(introBlock, node1);
            context.trackBlock(conclusionBlock, node2);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should validate violated before constraint")
        void shouldValidateViolatedBeforeConstraint() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint
                    .of("introduction", "conclusion", Severity.ERROR);
            OrderConfig orderConfig = new OrderConfig(null, Arrays.asList(constraint), null, Severity.ERROR);

            // Add blocks in wrong order
            ParagraphBlock introBlock = ParagraphBlock.builder().name("introduction").severity(Severity.ERROR).build();
            ParagraphBlock conclusionBlock = ParagraphBlock
                    .builder()
                    .name("conclusion")
                    .severity(Severity.ERROR)
                    .build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            context.trackBlock(conclusionBlock, node1); // Wrong order
            context.trackBlock(introBlock, node2);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("block.order.before", msg.getRuleId());
            assertEquals("Block 'introduction' must appear before 'conclusion'", msg.getMessage());
            assertEquals("'introduction' at position 2, 'conclusion' at position 1", msg.getActualValue().orElse(null));
            assertEquals("'introduction' before 'conclusion'", msg.getExpectedValue().orElse(null));
        }

        @Test
        @DisplayName("should handle missing blocks in before constraint")
        void shouldHandleMissingBlocksInBeforeConstraint() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint
                    .of("introduction", "conclusion", Severity.WARN);
            OrderConfig orderConfig = new OrderConfig(null, Arrays.asList(constraint), null, Severity.ERROR);

            // Add only one of the blocks
            ParagraphBlock introBlock = ParagraphBlock.builder().name("introduction").severity(Severity.ERROR).build();

            StructuralNode node = mock(StructuralNode.class);
            context.trackBlock(introBlock, node);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty()); // Constraint not applicable if one block missing
        }
    }

    @Nested
    @DisplayName("after constraint validation")
    class AfterConstraintValidation {

        @Test
        @DisplayName("should validate satisfied after constraint")
        void shouldValidateSatisfiedAfterConstraint() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint
                    .of("summary", "header", Severity.ERROR);
            OrderConfig orderConfig = new OrderConfig(null, null, Arrays.asList(constraint), Severity.ERROR);

            // Add blocks in correct order (summary after header)
            ParagraphBlock headerBlock = ParagraphBlock.builder().name("header").severity(Severity.ERROR).build();
            TableBlock summaryBlock = TableBlock.builder().name("summary").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            context.trackBlock(headerBlock, node1);
            context.trackBlock(summaryBlock, node2);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("should validate violated after constraint")
        void shouldValidateViolatedAfterConstraint() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint.of("summary", "header", Severity.INFO);
            OrderConfig orderConfig = new OrderConfig(null, null, Arrays.asList(constraint), Severity.ERROR);

            // Add blocks in wrong order (summary before header)
            ParagraphBlock headerBlock = ParagraphBlock.builder().name("header").severity(Severity.ERROR).build();
            TableBlock summaryBlock = TableBlock.builder().name("summary").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            context.trackBlock(summaryBlock, node1); // Wrong order
            context.trackBlock(headerBlock, node2);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("block.order.after", msg.getRuleId());
            assertEquals("Block 'summary' must appear after 'header'", msg.getMessage());
        }
    }

    @Nested
    @DisplayName("complex order scenarios")
    class ComplexOrderScenarios {

        @Test
        @DisplayName("should validate multiple constraints together")
        void shouldValidateMultipleConstraintsTogether() {
            // Given
            OrderConfig.OrderConstraint beforeConstraint = OrderConfig.OrderConstraint
                    .of("header", "footer", Severity.ERROR);
            OrderConfig.OrderConstraint afterConstraint = OrderConfig.OrderConstraint
                    .of("content", "header", Severity.WARN);
            OrderConfig orderConfig = new OrderConfig(Arrays.asList("header", "content"),
                    Arrays.asList(beforeConstraint), Arrays.asList(afterConstraint), Severity.ERROR);

            // Add blocks violating multiple constraints
            ParagraphBlock headerBlock = ParagraphBlock.builder().name("header").severity(Severity.ERROR).build();
            ParagraphBlock contentBlock = ParagraphBlock.builder().name("content").severity(Severity.ERROR).build();
            ParagraphBlock footerBlock = ParagraphBlock.builder().name("footer").severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);
            StructuralNode node3 = mock(StructuralNode.class);

            // Wrong order: footer, content, header
            context.trackBlock(footerBlock, node1);
            context.trackBlock(contentBlock, node2);
            context.trackBlock(headerBlock, node3);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertTrue(messages.size() >= 2); // Multiple violations
            assertTrue(messages.stream().anyMatch(m -> m.getRuleId().equals("block.order.fixed")));
            assertTrue(messages.stream().anyMatch(m -> m.getRuleId().equals("block.order.before")));
        }

        @Test
        @DisplayName("should use block type when name is not available")
        void shouldUseBlockTypeWhenNameIsNotAvailable() {
            // Given
            OrderConfig.OrderConstraint constraint = OrderConfig.OrderConstraint
                    .of("paragraph", "table", Severity.ERROR);
            OrderConfig orderConfig = new OrderConfig(null, Arrays.asList(constraint), null, Severity.ERROR);

            // Add blocks without names (uses type)
            ParagraphBlock paragraphBlock = ParagraphBlock.builder().severity(Severity.ERROR).build();
            TableBlock tableBlock = TableBlock.builder().severity(Severity.ERROR).build();

            StructuralNode node1 = mock(StructuralNode.class);
            StructuralNode node2 = mock(StructuralNode.class);

            // Wrong order
            context.trackBlock(tableBlock, node1);
            context.trackBlock(paragraphBlock, node2);

            // When
            List<ValidationMessage> messages = validator.validate(context, orderConfig);

            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("paragraph"));
            assertTrue(messages.get(0).getMessage().contains("table"));
        }
    }
}
