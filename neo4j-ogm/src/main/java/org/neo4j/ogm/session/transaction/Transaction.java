package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;

public interface Transaction extends AutoCloseable {


    /**
     * Adds a new cypher context to this transaction
     * @param context The CypherContext that forms part of this transaction when committed
     */
    void append(CypherContext context);

    /**
     * The endpoint for this transaction
     * @return
     */
    String url();

    /*
     * rollback a transaction that has pending writes
     * calling rollback on a transaction with no pending read/writes is an error
     */
    void rollback();

    /*
     * commit a transaction that has pending writes
     * calling commit on a transaction with no pending read/writes is an error
     */
    void commit();

    /**
     * return the status of the current transaction
     * @return the Status value associated with the current transaction
     */
    Status status();

    public enum Status {
        OPEN, PENDING, ROLLEDBACK, COMMITTED, CLOSED
    }

    void close();
}
