package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.strategy.simple.Setter;

public class SetterEntityAccessStrategyFactory implements EntityAccessStrategyFactory {

    @Override
    public EntityAccessStrategy forPersistentField(PersistentField pf) {
        return Setter.forProperty(String.valueOf(pf.getJavaObjectFieldName()));
    }

    @Override
    public EntityAccessStrategy forType(Class<?> type) {
        return Setter.forProperty(type.getSimpleName());
    }

}
