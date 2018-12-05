package org.neo4j.ogm.session;

/**
 * Top level Exception for Neo4j OGM.
 *
 * @author Mark Angrish
 *
 * @deprecated Since 3.1.6, will be removed in 3.2 Neo4j-OGM doesn't use this and there will be no replacement.
 */
@Deprecated
public class Neo4jException extends RuntimeException {

    public Neo4jException(String s) {
        super(s);
    }
}
