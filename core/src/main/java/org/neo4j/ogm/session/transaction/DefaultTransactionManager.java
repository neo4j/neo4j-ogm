/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.neo4j.ogm.exception.TransactionManagerException;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DefaultTransactionManager implements TransactionManager {

    private final Driver driver;

    private static final ThreadLocal<Transaction> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();

    public DefaultTransactionManager(Driver driver) {
        this.driver = driver;
        this.driver.setTransactionManager(this);
        TRANSACTION_THREAD_LOCAL.remove();
    }

    /**
     * Opens a new TRANSACTION_THREAD_LOCAL against a database instance.
     *
     * Instantiation of the TRANSACTION_THREAD_LOCAL is left to the driver
     *
     * @return
     */
    public Transaction openTransaction() {
        if (TRANSACTION_THREAD_LOCAL.get() == null) {
            TRANSACTION_THREAD_LOCAL.set(driver.newTransaction());
            return TRANSACTION_THREAD_LOCAL.get();
        } else {
            throw new TransactionManagerException("Nested transactions not supported");
        }
    }


    /**
     * Rolls back the specified TRANSACTION_THREAD_LOCAL.
     *
     * The actual job of rolling back the TRANSACTION_THREAD_LOCAL is left to the relevant driver. if
     * this is successful, the TRANSACTION_THREAD_LOCAL is detached from this thread.
     *
     * If the specified TRANSACTION_THREAD_LOCAL is not the correct one for this thread, throws an exception
     *
     * @param transaction the TRANSACTION_THREAD_LOCAL to rollback
     */
    public void rollback(Transaction transaction) {
        if (transaction != getCurrentTransaction()) {
            throw new TransactionManagerException("Transaction is not current for this thread");
        }
        TRANSACTION_THREAD_LOCAL.remove();
    }

    /**
     * Commits the specified TRANSACTION_THREAD_LOCAL.
     *
     * The actual job of committing the TRANSACTION_THREAD_LOCAL is left to the relevant driver. if
     * this is successful, the TRANSACTION_THREAD_LOCAL is detached from this thread.
     *
     * If the specified TRANSACTION_THREAD_LOCAL is not the correct one for this thread, throws an exception
     *
     * @param tx the TRANSACTION_THREAD_LOCAL to commit
     */
    public void commit(Transaction tx) {
        if (tx != getCurrentTransaction()) {
            throw new TransactionManagerException("Transaction is not current for this thread");
        }
        TRANSACTION_THREAD_LOCAL.remove();
    }

    /**
     * Returns the current TRANSACTION_THREAD_LOCAL for this thread, or null if none exists
     *
     * @return this thread's TRANSACTION_THREAD_LOCAL
     */
    public Transaction getCurrentTransaction() {
        return TRANSACTION_THREAD_LOCAL.get();
    }


}
