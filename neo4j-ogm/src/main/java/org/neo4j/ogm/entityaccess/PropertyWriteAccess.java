package org.neo4j.ogm.entityaccess;

/**
 * Simple interface through which a particular property of a given object can be set.
 */
public interface PropertyWriteAccess {

    void write(Object instance, Object valueToSet);

}
