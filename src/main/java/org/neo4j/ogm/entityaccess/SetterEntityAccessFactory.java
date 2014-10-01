package org.neo4j.ogm.entityaccess;

public class SetterEntityAccessFactory implements EntityAccessFactory {

    @Override
    public EntityAccess forProperty(String propertyName) {
        return SetterEntityAccess.forProperty(propertyName);
    }

}
