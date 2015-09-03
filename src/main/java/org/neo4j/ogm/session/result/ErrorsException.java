package org.neo4j.ogm.session.result;

/**
 * @author vince
 */
public class ErrorsException extends RuntimeException {
    public ErrorsException(String reasonMessage) {
        super(reasonMessage);
    }
}
