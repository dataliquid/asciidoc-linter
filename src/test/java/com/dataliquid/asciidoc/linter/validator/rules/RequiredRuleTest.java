package com.dataliquid.asciidoc.linter.validator.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

@DisplayName("RequiredRule")
class RequiredRuleTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder()
            .filename("test.adoc")
            .line(1)
            .build();
    }

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with required attributes")
        void shouldBuildRuleWithRequiredAttributes() {
            // Given
            RequiredRule.Builder builder = RequiredRule.builder()
                .addAttribute("title", true, Severity.ERROR)
                .addAttribute("author", true, Severity.ERROR)
                .addAttribute("email", false, Severity.WARN);
            
            // When
            RequiredRule rule = builder.build();
            
            // Then
            assertEquals("metadata.required", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("author"));
            assertTrue(rule.isApplicable("email"));
            assertFalse(rule.isApplicable("unknown"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {
        
        @Test
        @DisplayName("should pass when required attribute has value")
        void shouldPassWhenRequiredAttributeHasValue() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("title", true, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("title", "My Document", testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when required attribute is null")
        void shouldFailWhenRequiredAttributeIsNull() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("author", true, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("author", null, testLocation);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.required", message.getRuleId());
            assertEquals("Missing required attribute 'author'", message.getMessage());
        }
        
        @Test
        @DisplayName("should pass when required attribute is empty")
        void shouldPassWhenRequiredAttributeIsEmpty() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("version", true, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("version", "", testLocation);
            
            // Then
            assertEquals(0, messages.size()); // Empty values are valid for metadata attributes
        }
        
        @Test
        @DisplayName("should pass when required attribute is whitespace only")
        void shouldPassWhenRequiredAttributeIsWhitespaceOnly() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("revdate", true, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("revdate", "   ", testLocation);
            
            // Then
            assertEquals(0, messages.size()); // Whitespace-only values are valid for metadata attributes
        }
        
        @Test
        @DisplayName("should pass when optional attribute is missing")
        void shouldPassWhenOptionalAttributeIsMissing() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("email", false, Severity.WARN)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("email", null, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("Missing Attributes Validation")
    class MissingAttributesValidation {
        
        @Test
        @DisplayName("should detect all missing required attributes")
        void shouldDetectAllMissingRequiredAttributes() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("title", true, Severity.ERROR)
                .addAttribute("author", true, Severity.ERROR)
                .addAttribute("version", true, Severity.ERROR)
                .addAttribute("email", false, Severity.WARN)
                .build();
            
            Set<String> presentAttributes = new HashSet<>();
            presentAttributes.add("title");
            
            // When
            List<ValidationMessage> messages = rule.validateMissingAttributes(presentAttributes, testLocation);
            
            // Then
            assertEquals(2, messages.size());
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Missing required attribute 'author'")));
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Missing required attribute 'version'")));
            assertFalse(messages.stream().anyMatch(m -> m.getMessage().contains("Missing required attribute 'email'")));
        }
        
        @Test
        @DisplayName("should return empty list when all required attributes present")
        void shouldReturnEmptyListWhenAllRequiredAttributesPresent() {
            // Given
            RequiredRule rule = RequiredRule.builder()
                .addAttribute("title", true, Severity.ERROR)
                .addAttribute("author", true, Severity.ERROR)
                .build();
            
            Set<String> presentAttributes = new HashSet<>();
            presentAttributes.add("title");
            presentAttributes.add("author");
            
            // When
            List<ValidationMessage> messages = rule.validateMissingAttributes(presentAttributes, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
}