package org.neo4j.ogm.strategy.annotated;

import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

import java.lang.reflect.Field;

public class AnnotatedFieldDictionary extends FieldDictionary {

    @Override
    protected Field findScalarField(Object instance, Object value, String property) {
        return null;
    }

    @Override
    protected Field findCollectionField(Object instance, Object iterable, Class elementType, String property) {
        return null;
    }

    // the FieldInfo class should help here at some point, but its not accessible currently
    @Override
    public String resolveGraphAttribute(String name) {
        return null;
    }

    @Override
    public String resolveTypeAttribute(String typeAttributeName, Class<?> owningType) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
