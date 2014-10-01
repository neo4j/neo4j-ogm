package org.neo4j.ogm.entityaccess;

public interface EntityAccess {

    void set(Object instance, Object any) throws Exception;
    void setValue(Object instance, Object scalar) throws Exception;
    void setIterable(Object instance, Iterable<?> iterable) throws Exception;

}
