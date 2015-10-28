package org.neo4j.ogm.model;

/**
 * @author vince
 */
public interface Property<K, V> {
    K getKey();

    V getValue();

    Object asParameter();
}
