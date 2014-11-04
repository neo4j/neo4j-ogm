package org.neo4j.ogm.mapper.cypher;

public interface ResponseStream<T> {

    T next();
    boolean hasNext();

}
