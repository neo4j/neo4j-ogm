package org.neo4j.ogm.entityaccess;

/**
 * Simple interface through which properties of objects can be read or set.
 */
public interface PropertyObjectAccess {

    void write(Object instance, Object value);

    Object read(Object instance);

}
