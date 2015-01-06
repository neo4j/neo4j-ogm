package org.neo4j.ogm.entityaccess;

public class EntityAccessException extends RuntimeException {

    public EntityAccessException(String msg, Exception cause) {
        super(msg, cause);
    }
}
