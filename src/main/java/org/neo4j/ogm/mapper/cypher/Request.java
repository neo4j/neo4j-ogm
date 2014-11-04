package org.neo4j.ogm.mapper.cypher;

public interface Request<T> {

    ResponseStream<T> execute();
}
