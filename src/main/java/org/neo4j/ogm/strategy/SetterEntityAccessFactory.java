package org.neo4j.ogm.strategy;

import org.neo4j.ogm.strategy.simple.Setter;

public class SetterEntityAccessFactory implements EntityAccessFactory {

    @Override
    public EntityAccess forProperty(String propertyName) {
        return Setter.forProperty(propertyName);
    }

}
