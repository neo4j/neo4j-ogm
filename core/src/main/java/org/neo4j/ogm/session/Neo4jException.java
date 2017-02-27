package org.neo4j.ogm.session;

/**
 * Top level Exception for Neo4j OGM.
 *
 * @author Mark Angrish
 */
public class Neo4jException extends RuntimeException {

    public Neo4jException(String s) {
        super(s);
    }
}
