package com.dataliquid.asciidoc.linter.validator.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("PatternRule")
class PatternRuleTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder()
            .filename("test.adoc")
            .line(2)
            .build();
    }

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with valid patterns")
        void shouldBuildRuleWithValidPatterns() {
            // Given
            PatternRule.Builder builder = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .addPattern("version", "^\\d+\\.\\d+(\\.\\d+)?$", Severity.ERROR)
                .addPattern("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", Severity.WARN);
            
            // When
            PatternRule rule = builder.build();
            
            // Then
            assertEquals("metadata.pattern", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("version"));
            assertTrue(rule.isApplicable("email"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should reject invalid regex pattern")
        void shouldRejectInvalidRegexPattern() {
            // Given
            PatternRule.Builder builder = PatternRule.builder();
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                builder.addPattern("invalid", "[", Severity.ERROR)
            );
        }
    }

    @Nested
    @DisplayName("Title Pattern Validation")
    class TitlePatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should pass when title starts with uppercase")
        void shouldPassWhenTitleStartsWithUppercase() {
            // Given
            String titleValue = "My Document Title";
            
            // When
            List<ValidationMessage> messages = rule.validate("title", titleValue, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when title starts with lowercase")
        void shouldFailWhenTitleStartsWithLowercase() {
            // Given
            String titleValue = "my document title";
            
            // When
            List<ValidationMessage> messages = rule.validate("title", titleValue, testLocation);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.pattern", message.getRuleId());
            assertEquals("Attribute 'title' does not match required pattern: actual 'my document title', expected pattern '^[A-Z].*'", message.getMessage());
            // actualValue and expectedValue are already included in the message
        }
    }

    @Nested
    @DisplayName("Version Pattern Validation")
    class VersionPatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("version", "^\\d+\\.\\d+(\\.\\d+)?$", Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should accept semantic version format")
        void shouldAcceptSemanticVersionFormat() {
            // Given
            String[] validVersions = {"1.0", "1.0.0", "2.15.3"};
            
            // When/Then
            for (String version : validVersions) {
                assertTrue(rule.validate("version", version, testLocation).isEmpty());
            }
        }
        
        @Test
        @DisplayName("should reject invalid version format")
        void shouldRejectInvalidVersionFormat() {
            // Given
            String[] invalidVersions = {"1", "1.0.0.0", "v1.0.0", "1.0-SNAPSHOT"};
            
            // When/Then
            for (String version : invalidVersions) {
                assertFalse(rule.validate("version", version, testLocation).isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Email Pattern Validation")
    class EmailPatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", Severity.WARN)
                .build();
        }
        
        @Test
        @DisplayName("should accept valid email formats")
        void shouldAcceptValidEmailFormats() {
            // Given
            String[] validEmails = {"user@example.com", "john.doe@company.co.uk", "test+tag@domain.org"};
            
            // When/Then
            for (String email : validEmails) {
                assertTrue(rule.validate("email", email, testLocation).isEmpty());
            }
        }
        
        @Test
        @DisplayName("should reject invalid email formats")
        void shouldRejectInvalidEmailFormats() {
            // Given
            String[] invalidEmails = {"invalid", "@example.com", "user@", "user@domain"};
            
            // When/Then
            for (String email : invalidEmails) {
                assertFalse(rule.validate("email", email, testLocation).isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should skip validation for null values")
        void shouldSkipValidationForNullValues() {
            // Given
            PatternRule rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("title", null, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should skip validation for empty values")
        void shouldSkipValidationForEmptyValues() {
            // Given
            PatternRule rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("title", "", testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
}