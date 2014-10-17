package org.neo4j.ogm.entityaccess;

public class MethodEntityAccessFactory implements EntityAccessFactory {

    @Override
    public EntityAccess forProperty(String propertyName) {
        return MethodEntityAccess.forProperty(propertyName);
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
