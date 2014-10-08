package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;

public interface EntityAccess {

    void set(Object instance, Object any) throws Exception;
    void setValue(Object instance, Object scalar) throws Exception;
    void setIterable(Object instance, Iterable<?> iterable) throws Exception;

    Object readValue(Object instance) throws MappingException;

}
