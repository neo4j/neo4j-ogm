package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.strategy.simple.Setter;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    @Override
    public FieldEntityAccess forPersistentField(PersistentField pf) {
        return new FieldEntityAccess(String.valueOf(pf.getJavaObjectFieldName()));
    }

    @Override
    public FieldEntityAccess forType(Class<?> type) {
        return new FieldEntityAccess(type.getSimpleName());
    }

}
