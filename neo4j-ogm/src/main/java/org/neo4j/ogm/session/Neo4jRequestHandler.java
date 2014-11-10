package org.neo4j.ogm.session;

public interface Neo4jRequestHandler<T> {

    Neo4jResponseHandler<T> execute(String url, String... statements);
}
