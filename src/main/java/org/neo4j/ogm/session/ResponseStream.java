package org.neo4j.ogm.session;

public interface ResponseStream<T> {

    T next();
    boolean hasNext();

}
