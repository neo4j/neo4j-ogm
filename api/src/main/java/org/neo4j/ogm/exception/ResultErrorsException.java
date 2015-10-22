package org.neo4j.ogm.exception;

/**
 * @author vince
 */
public class ResultErrorsException extends RuntimeException {
    public ResultErrorsException(String reasonMessage) {
        super(reasonMessage);
    }
}
