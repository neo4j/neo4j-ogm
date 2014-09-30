package org.neo4j.ogm.strategy;

public interface EntityAccessFactory {

    EntityAccess forProperty(String propertyName);

}
