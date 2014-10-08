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
}
