package com.dataliquid.asciidoc.linter.validator.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("OrderRule")
class OrderRuleTest {

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with order constraints")
        void shouldBuildRuleWithOrderConstraints() {
            // Given
            OrderRule.Builder builder = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.WARN);
            
            // When
            OrderRule rule = builder.build();
            
            // Then
            assertEquals("metadata.order", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("author"));
            assertTrue(rule.isApplicable("revdate"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should allow null order for flexible positioning")
        void shouldAllowNullOrderForFlexiblePositioning() {
            // Given
            OrderRule.Builder builder = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("optional", null, Severity.INFO);
            
            // When
            OrderRule rule = builder.build();
            
            // Then
            assertTrue(rule.isApplicable("optional"));
        }
    }

    @Nested
    @DisplayName("Order Validation")
    class OrderValidation {
        
        @Test
        @DisplayName("should pass when attributes are in correct order")
        void shouldPassWhenAttributesAreInCorrectOrder() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 3));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when attributes are out of order")
        void shouldFailWhenAttributesAreOutOfOrder() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 3));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 4));
            
            List<ValidationMessage> messages = rule.validateOrder();
            
            // Then
            assertFalse(messages.isEmpty());
            
            assertTrue(messages.stream()
                .anyMatch(m -> m.getMessage().equals("Attribute 'title' should appear before 'author': actual position line 3, expected before line 2")));
        }
        
        @Test
        @DisplayName("should detect multiple order violations")
        void shouldDetectMultipleOrderViolations() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("version", 3, Severity.ERROR)
                .addOrderConstraint("revdate", 4, Severity.ERROR)
                .build();
            
            // When
            rule.validate("version", "1.0", createLocation("test.adoc", 1));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 3));
            rule.validate("author", "John Doe", createLocation("test.adoc", 4));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.size() >= 2);
        }
    }

    @Nested
    @DisplayName("Partial Order Validation")
    class PartialOrderValidation {
        
        @Test
        @DisplayName("should handle missing attributes in order check")
        void shouldHandleMissingAttributesInOrderCheck() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("version", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("version", "1.0", createLocation("test.adoc", 2));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should ignore attributes without order constraint")
        void shouldIgnoreAttributesWithoutOrderConstraint() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("keywords", "test, doc", createLocation("test.adoc", 2));
            rule.validate("author", "John Doe", createLocation("test.adoc", 3));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("Message Details")
    class MessageDetails {
        
        @Test
        @DisplayName("should include line numbers in error messages")
        void shouldIncludeLineNumbersInErrorMessages() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .build();
            
            // When
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 5));
            
            List<ValidationMessage> messages = rule.validateOrder();
            
            // Then
            assertEquals(1, messages.size());
            
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            // actualValue is already tested in the message
            // expectedValue is already tested in the message
            assertTrue(message.getMessage().contains("actual position line 5"));
            assertTrue(message.getMessage().contains("expected before line 2"));
        }
    }
    
    private SourceLocation createLocation(String filename, int line) {
        return SourceLocation.builder()
            .filename(filename)
            .line(line)
            .build();
    }
}