package org.neo4j.ogm.api.model;

/**
 * @author vince
 */
public interface Property<K, V> {
    K getKey();

    V getValue();

    Object asParameter();
}
