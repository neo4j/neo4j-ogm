package org.neo4j.ogm.entityaccess;

public interface EntityAccessFactory {

    EntityAccess forProperty(String propertyName);

}
