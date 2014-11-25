package org.neo4j.ogm.session.result;

public class ResultProcessingException extends RuntimeException {

    public ResultProcessingException(String reasonMessage, Exception cause) {
        super(reasonMessage, cause);
    }
}
