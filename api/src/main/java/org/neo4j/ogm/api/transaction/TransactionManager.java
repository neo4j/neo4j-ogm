package org.neo4j.ogm.api.transaction;

/**
 * @author vince
 */
public interface TransactionManager {

    /**
     * Opens a new transaction against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @return a new @{link Transaction}
     */
    public Transaction openTransaction();

    /**
     * Rolls back the specified transaction.
     *
     * The actual job of rolling back the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param transaction the transaction to rollback
     */
    public void rollback(Transaction transaction);


    /**
     * Commits the specified transaction.
     *
     * The actual job of committing the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param transaction the transaction to commit
     */
    public void commit(Transaction transaction);


    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    public Transaction getCurrentTransaction();
}
