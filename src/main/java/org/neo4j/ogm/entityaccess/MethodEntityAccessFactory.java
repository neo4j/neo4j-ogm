package org.neo4j.ogm.entityaccess;

public class MethodEntityAccessFactory implements EntityAccessFactory {

    @Override
    public EntityAccess forProperty(String propertyName) {
        return MethodEntityAccess.forProperty(propertyName);
    }

}
