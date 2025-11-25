package com.dataliquid.asciidoc.linter.validator;

/**
 * Context information for placeholder insertion in error messages. This helps
 * the renderer determine the correct syntax for placeholders.
 */
public class PlaceholderContext {

    public enum PlaceholderType {
        ATTRIBUTE_VALUE, // e.g., width=«100»
        ATTRIBUTE_IN_LIST, // e.g., ,width=«100»
        SIMPLE_VALUE, // e.g., «language»
        LIST_VALUE, // e.g., ,«language»
        INSERT_BEFORE // Insert before the given position (e.g., before ])
    }

    private final PlaceholderType _type;
    private final String _attributeName;
    private final boolean _isFirstAttribute;
    private final boolean _hasExistingAttributes;

    private PlaceholderContext(Builder builder) {
        this._type = builder._type;
        this._attributeName = builder._attributeName;
        this._isFirstAttribute = builder._isFirstAttribute;
        this._hasExistingAttributes = builder._hasExistingAttributes;
    }

    public PlaceholderType getType() {
        return this._type;
    }

    public String getAttributeName() {
        return this._attributeName;
    }

    public boolean isFirstAttribute() {
        return this._isFirstAttribute;
    }

    public boolean hasExistingAttributes() {
        return this._hasExistingAttributes;
    }

    /**
     * Generates the complete placeholder string based on context.
     *
     * @param  value The placeholder value (without delimiters)
     *
     * @return       The complete placeholder with appropriate syntax
     */
    public String generatePlaceholder(String value) {
        String placeholder = "«" + value + "»";

        switch (_type) {
        case ATTRIBUTE_VALUE:
            return _attributeName + "=" + placeholder;

        case ATTRIBUTE_IN_LIST:
            return "," + _attributeName + "=" + placeholder;

        case LIST_VALUE:
            return "," + placeholder;

        case SIMPLE_VALUE:
        default:
            return placeholder;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PlaceholderType _type = PlaceholderType.SIMPLE_VALUE;
        private String _attributeName;
        private boolean _isFirstAttribute; // Default false
        private boolean _hasExistingAttributes; // Default false

        public Builder type(PlaceholderType type) {
            this._type = type;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this._attributeName = attributeName;
            return this;
        }

        public Builder isFirstAttribute(boolean isFirstAttribute) {
            this._isFirstAttribute = isFirstAttribute;
            return this;
        }

        public Builder hasExistingAttributes(boolean hasExistingAttributes) {
            this._hasExistingAttributes = hasExistingAttributes;
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
        return _isFirstAttribute == that._isFirstAttribute && _hasExistingAttributes == that._hasExistingAttributes
                && _type == that._type && java.util.Objects.equals(_attributeName, that._attributeName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(_type, _attributeName, _isFirstAttribute, _hasExistingAttributes);
    }
}
