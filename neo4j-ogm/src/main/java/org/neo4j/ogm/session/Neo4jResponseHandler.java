package org.neo4j.ogm.session;

public interface Neo4jResponseHandler<T> {

    T next();

}
