package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.dictionary.MethodDictionary;

public class MethodEntityAccessFactory implements EntityAccessFactory {

    private final MethodDictionary methodDictionary;

    public MethodEntityAccessFactory(MethodDictionary dictionary) {
        methodDictionary = dictionary;
    }

    @Override
    public EntityAccess forProperty(String propertyName) {
        return MethodEntityAccess.forProperty(methodDictionary, propertyName);
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
