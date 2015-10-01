package org.neo4j.ogm.driver.impl.result;

/**
 * @author vince
 */
public class ResultErrorsException extends RuntimeException {
    public ResultErrorsException(String reasonMessage) {
        super(reasonMessage);
    }
}
