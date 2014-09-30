package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.strategy.simple.Setter;

public class SetterEntityAccessFactory implements EntityAccessFactory {

    @Override
    public EntityAccess forPersistentField(PersistentField pf) {
        return Setter.forProperty(String.valueOf(pf.getJavaObjectFieldName()));
    }

    @Override
    public EntityAccess forType(Class<?> type) {
        return Setter.forProperty(type.getSimpleName());
    }

}
