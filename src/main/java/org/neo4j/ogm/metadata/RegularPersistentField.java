package org.neo4j.ogm.metadata;

/**
 * Just a regular field that stores data and has no special properties or responsibilities.
 */
public class RegularPersistentField implements PersistentField {

    private final String fieldName;
    private final String propertyName;

    public RegularPersistentField(String fieldName, String propertyName) {
        this.fieldName = fieldName;
        this.propertyName = propertyName;
    }

    public RegularPersistentField(String name) {
        this(name, name);
    }

    @Override
    public String getJavaObjectFieldName() {
        return this.fieldName;
    }

    @Override
    public String getGraphElementPropertyName() {
        return this.propertyName;
    }

    @Override
    public boolean isIdField() {
        return this.fieldName.equals("id"); // yes, it's a hack
    }

    @Override
    public boolean isScalarValue() {
        return true;
    }

}
