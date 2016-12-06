/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.transaction;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public interface TransactionManager {

    /**
     * Opens a new READ_WRITE transaction against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @return a new @{link Transaction}
     */
    Transaction openTransaction();

    /**
     * Opens a new transaction of the specified type against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @return a new @{link Transaction}
     */
    Transaction openTransaction(Transaction.Type type);

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
    void rollback(Transaction transaction);


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
    void commit(Transaction transaction);


    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    Transaction getCurrentTransaction();

    boolean canCommit();

    boolean canRollback();

    void bookmark(String bookmark);
}
