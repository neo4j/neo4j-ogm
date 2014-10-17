package org.neo4j.ogm.entityaccess;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    @Override
    public FieldEntityAccess forProperty(String property) {
        return FieldEntityAccess.forProperty(property);
    }

    @Override
    public EntityAccess forAttributeOfType(String attributeName, Class<?> type) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

    @Override
    public EntityAccess forIdAttributeOfType(Class<?> type) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
