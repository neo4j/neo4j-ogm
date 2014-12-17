package org.neo4j.ogm.session.transaction;

public class TransactionException extends RuntimeException {
    public TransactionException(String msg) {
        super(msg);
    }
}
