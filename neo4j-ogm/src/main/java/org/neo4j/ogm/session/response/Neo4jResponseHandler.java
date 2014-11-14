package org.neo4j.ogm.session.response;

public interface Neo4jResponseHandler<T> {

    T next();
    void close();
    void setScanToken(String token);

}
