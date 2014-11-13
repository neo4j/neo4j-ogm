package org.neo4j.ogm.session.result;

public class SessionException extends RuntimeException {

    public SessionException(String reasonMessage, Exception cause) {
        super(reasonMessage, cause);
    }
}
