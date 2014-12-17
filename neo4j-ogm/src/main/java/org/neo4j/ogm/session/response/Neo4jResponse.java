package org.neo4j.ogm.session.response;

public interface Neo4jResponse<T> extends AutoCloseable {

    T next();
    void close();
    void initialiseScan(String token);
    String[] columns();
    int rowId();
}
