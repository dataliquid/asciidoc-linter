package com.dataliquid.asciidoc.linter.config.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;

@DisplayName("OrderConfig")
class OrderConfigTest {
    
    @Nested
    @DisplayName("builder")
    class Builder {
        
        @Test
        @DisplayName("should build with fixed order")
        void shouldBuildWithFixedOrder() {
            // Given
            List<String> fixedOrder = Arrays.asList("intro", "body", "conclusion");
            
            // When
            OrderConfig config = OrderConfig.builder()
                .fixedOrder(fixedOrder)
                .severity(Severity.ERROR)
                .build();
            
            // Then
            assertEquals(fixedOrder, config.fixedOrder());
            assertEquals(Severity.ERROR, config.severity());
        }
        
        @Test
        @DisplayName("should build with empty lists by default")
        void shouldBuildWithEmptyListsByDefault() {
            // Given/When
            OrderConfig config = OrderConfig.builder()
                .severity(Severity.WARN)
                .build();
            
            // Then
            assertTrue(config.fixedOrder().isEmpty());
            assertTrue(config.before().isEmpty());
            assertTrue(config.after().isEmpty());
            assertEquals(Severity.WARN, config.severity());
        }
        
        @Test
        @DisplayName("should add individual fixed order items")
        void shouldAddIndividualFixedOrderItems() {
            // Given/When
            OrderConfig config = OrderConfig.builder()
                .addFixedOrder("first")
                .addFixedOrder("second")
                .addFixedOrder("third")
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(Arrays.asList("first", "second", "third"), config.fixedOrder());
        }
        
        @Test
        @DisplayName("should add before constraints")
        void shouldAddBeforeConstraints() {
            // Given/When
            OrderConfig config = OrderConfig.builder()
                .addBefore("intro", "body", Severity.ERROR)
                .addBefore("body", "conclusion", Severity.WARN)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(2, config.before().size());
            assertEquals("intro", config.before().get(0).first());
            assertEquals("body", config.before().get(0).second());
            assertEquals(Severity.ERROR, config.before().get(0).severity());
        }
        
        @Test
        @DisplayName("should add after constraints")
        void shouldAddAfterConstraints() {
            // Given/When
            OrderConfig config = OrderConfig.builder()
                .addAfter("conclusion", "body", Severity.ERROR)
                .severity(Severity.INFO)
                .build();
            
            // Then
            assertEquals(1, config.after().size());
            assertEquals("conclusion", config.after().get(0).first());
            assertEquals("body", config.after().get(0).second());
            assertEquals(Severity.ERROR, config.after().get(0).severity());
        }
        
        @Test
        @DisplayName("should default to ERROR severity")
        void shouldDefaultToErrorSeverity() {
            // Given/When
            OrderConfig config = OrderConfig.builder().build();
            
            // Then
            assertEquals(Severity.ERROR, config.severity());
        }
    }
    
    @Nested
    @DisplayName("OrderConstraint")
    class OrderConstraintTest {
        
        @Test
        @DisplayName("should create constraint with required fields")
        void shouldCreateConstraintWithRequiredFields() {
            // Given
            String first = "intro";
            String second = "body";
            Severity severity = Severity.ERROR;
            
            // When
            OrderConfig.OrderConstraint constraint = 
                OrderConfig.OrderConstraint.of(first, second, severity);
            
            // Then
            assertEquals(first, constraint.first());
            assertEquals(second, constraint.second());
            assertEquals(severity, constraint.severity());
        }
        
        @Test
        @DisplayName("should require non-null first")
        void shouldRequireNonNullFirst() {
            // Given
            String first = null;
            String second = "body";
            Severity severity = Severity.ERROR;
            
            // When/Then
            assertThrows(NullPointerException.class,
                () -> OrderConfig.OrderConstraint.of(first, second, severity));
        }
        
        @Test
        @DisplayName("should require non-null second")
        void shouldRequireNonNullSecond() {
            // Given
            String first = "intro";
            String second = null;
            Severity severity = Severity.ERROR;
            
            // When/Then
            assertThrows(NullPointerException.class,
                () -> OrderConfig.OrderConstraint.of(first, second, severity));
        }
        
        @Test
        @DisplayName("should require non-null severity")
        void shouldRequireNonNullSeverity() {
            // Given
            String first = "intro";
            String second = "body";
            Severity severity = null;
            
            // When/Then
            assertThrows(NullPointerException.class,
                () -> OrderConfig.OrderConstraint.of(first, second, severity));
        }
    }
    
    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {
        
        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Given
            OrderConfig config1 = OrderConfig.builder()
                .addFixedOrder("intro")
                .addBefore("intro", "body", Severity.ERROR)
                .severity(Severity.WARN)
                .build();
                
            OrderConfig config2 = OrderConfig.builder()
                .addFixedOrder("intro")
                .addBefore("intro", "body", Severity.ERROR)
                .severity(Severity.WARN)
                .build();
            
            // When/Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
        }
        
        @Test
        @DisplayName("should not be equal for different fixed order")
        void shouldNotBeEqualForDifferentFixedOrder() {
            // Given
            OrderConfig config1 = OrderConfig.builder()
                .addFixedOrder("intro")
                .severity(Severity.ERROR)
                .build();
                
            OrderConfig config2 = OrderConfig.builder()
                .addFixedOrder("body")
                .severity(Severity.ERROR)
                .build();
            
            // When/Then
            assertNotEquals(config1, config2);
        }
        
        @Test
        @DisplayName("should be equal for constraint with same values")
        void shouldBeEqualForConstraintWithSameValues() {
            // Given
            OrderConfig.OrderConstraint constraint1 = 
                OrderConfig.OrderConstraint.of("intro", "body", Severity.ERROR);
            OrderConfig.OrderConstraint constraint2 = 
                OrderConfig.OrderConstraint.of("intro", "body", Severity.ERROR);
            
            // When/Then
            assertEquals(constraint1, constraint2);
            assertEquals(constraint1.hashCode(), constraint2.hashCode());
        }
    }
}