package com.dataliquid.asciidoc.linter.validator;

/**
 * Context information for placeholder insertion in error messages. This helps the renderer determine the correct syntax
 * for placeholders.
 */
public class PlaceholderContext {

    public enum PlaceholderType {
        ATTRIBUTE_VALUE, // e.g., width=«100»
        ATTRIBUTE_IN_LIST, // e.g., ,width=«100»
        SIMPLE_VALUE, // e.g., «language»
        LIST_VALUE, // e.g., ,«language»
        INSERT_BEFORE // Insert before the given position (e.g., before ])
    }

    private final PlaceholderType type;
    private final String attributeName;
    private final boolean isFirstAttribute;
    private final boolean hasExistingAttributes;

    private PlaceholderContext(Builder builder) {
        this.type = builder.type;
        this.attributeName = builder.attributeName;
        this.isFirstAttribute = builder.isFirstAttribute;
        this.hasExistingAttributes = builder.hasExistingAttributes;
    }

    public PlaceholderType getType() {
        return type;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public boolean isFirstAttribute() {
        return isFirstAttribute;
    }

    public boolean hasExistingAttributes() {
        return hasExistingAttributes;
    }

    /**
     * Generates the complete placeholder string based on context.
     *
     * @param value
     *            The placeholder value (without delimiters)
     * @return The complete placeholder with appropriate syntax
     */
    public String generatePlaceholder(String value) {
        String placeholder = "«" + value + "»";

        switch (type) {
            case ATTRIBUTE_VALUE :
                return attributeName + "=" + placeholder;

            case ATTRIBUTE_IN_LIST :
                return "," + attributeName + "=" + placeholder;

            case LIST_VALUE :
                return "," + placeholder;

            case SIMPLE_VALUE :
            default :
                return placeholder;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PlaceholderType type = PlaceholderType.SIMPLE_VALUE;
        private String attributeName;
        private boolean isFirstAttribute = false;
        private boolean hasExistingAttributes = false;

        public Builder type(PlaceholderType type) {
            this.type = type;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder isFirstAttribute(boolean isFirstAttribute) {
            this.isFirstAttribute = isFirstAttribute;
            return this;
        }

        public Builder hasExistingAttributes(boolean hasExistingAttributes) {
            this.hasExistingAttributes = hasExistingAttributes;
            return this;
        }

        public PlaceholderContext build() {
            return new PlaceholderContext(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PlaceholderContext that = (PlaceholderContext) o;
        return isFirstAttribute == that.isFirstAttribute && hasExistingAttributes == that.hasExistingAttributes
                && type == that.type && java.util.Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, attributeName, isFirstAttribute, hasExistingAttributes);
    }
}
