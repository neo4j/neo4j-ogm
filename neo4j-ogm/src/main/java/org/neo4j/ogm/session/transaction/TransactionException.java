package org.neo4j.ogm.session.transaction;

public class TransactionException extends RuntimeException {
    public TransactionException() {
        super("The current transaction has uncommitted operations that should be rolled back or committed before beginning a new one");
    }
}
