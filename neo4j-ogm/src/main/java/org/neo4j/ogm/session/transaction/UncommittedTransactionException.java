package org.neo4j.ogm.session.transaction;

public class UncommittedTransactionException extends RuntimeException {
    public UncommittedTransactionException() {
        super("The current transaction has uncommitted operations that should be rolled back or committed before beginning a new one");
    }
}
