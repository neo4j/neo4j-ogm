package org.neo4j.ogm.session;

/**
 * Created by markangrish on 17/11/2016.
 */
public class Neo4jException extends RuntimeException {

    public Neo4jException(String s, Throwable t) {
        super(s, t);
    }

    public Neo4jException(String s) {
        super(s);
    }
}
