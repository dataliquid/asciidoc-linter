package com.dataliquid.asciidoc.linter.validator.rules;

import java.util.List;

import com.dataliquid.asciidoc.linter.validator.SourceLocation;
import com.dataliquid.asciidoc.linter.validator.ValidationMessage;

public interface AttributeRule {

    String getRuleId();

    List<ValidationMessage> validate(String attributeName, String value, SourceLocation location);

    boolean isApplicable(String attributeName);
}
