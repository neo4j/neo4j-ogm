package org.neo4j.ogm.session.request;

import org.neo4j.ogm.session.response.Neo4jResponseHandler;

public interface Neo4jRequestHandler<T> {

    Neo4jResponseHandler<T> execute(String url, String... statements);

}
