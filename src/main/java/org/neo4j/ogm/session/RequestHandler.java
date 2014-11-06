package org.neo4j.ogm.session;

public interface RequestHandler<T> {

    ResponseStream<T> execute(String request);
}
