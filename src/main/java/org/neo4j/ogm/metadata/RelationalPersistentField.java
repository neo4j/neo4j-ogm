package org.neo4j.ogm.metadata;

/**
 * Implementation of {@link PersistentField} that represents a field on an entity that relates to other persistent entities in a
 * one-to-one or one-to-many relationship.
 */
public class RelationalPersistentField implements PersistentField {

    private final String javaObjectFieldName;

    public RelationalPersistentField(String javaObjectFieldName) {
        this.javaObjectFieldName = javaObjectFieldName;
    }

    @Override
    public String getJavaObjectFieldName() {
        return this.javaObjectFieldName;
    }

    @Override
    public String getGraphElementPropertyName() {
        // this isn't actually appropriate because there'll be a relationship, so this is returning a relationship type
        return this.javaObjectFieldName.toUpperCase();
    }

    @Override
    public boolean isIdField() {
        return false; // it can't be
    }

    @Override
    public boolean isScalarValue() {
        return false;
    }

}
