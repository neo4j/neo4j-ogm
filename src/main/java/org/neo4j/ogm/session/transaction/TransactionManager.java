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
import org.neo4j.ogm.mapper.MappingContext;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransactionManager {

    private final Driver driver;

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    // if supplied, all threads share the same mapping context
    private final MappingContext mappingContext;

//    public TransactionManager(Driver driver) {
//        this.driver = driver;
//        this.mappingContext = null;
//        transaction.remove();
//    }

    public TransactionManager(Driver driver, MappingContext context) {
        this.driver = driver;
        driver.setTransactionManager(this);
        this.mappingContext = context;

        transaction.remove();
    }

//    /**
//     * Opens a new transaction against a database instance.
//     *
//     * Instantiation of the transaction is left to the driver
//     *
//     * @param mappingContext The session's mapping context. This may be required by the transaction?
//     * @return
//     */
//    public Transaction openTransaction(MappingContext mappingContext) {
//        transaction.set(driver.newTransaction(mappingContext, this, false));
//        return transaction.get();
//    }

    /**
     * Opens a new transaction against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @return
     */// half-way house: we want drivers to be unaware of mapping contexts.
    public Transaction openTransaction() {
        transaction.set(driver.newTransaction(this.mappingContext, this, false));
        return transaction.get();
    }

    /**
     * Opens an auto-commit transaction. An auto-commit transaction will immediately
     * invoke commit as soon as any request is made on it. The mechanism for handling
     * this is managed in @{link AbstractTransaction}
     *
     * Instantiation of the transaction is left to the driver.
     *
     * @param mappingContext The session's mapping context. This may be required by the transaction?
     * @return
     */
    public Transaction openTransientTransaction(MappingContext mappingContext) {
        transaction.set(driver.newTransaction(mappingContext, this, true));
        return transaction.get();
    }

    /**
     * Rolls back the specified transaction.
     *
     * The actual job of rolling back the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param tx the transaction to rollback
     */
    public void rollback(Transaction tx) {
        if (tx != transaction.get()) {
            throw new TransactionException("Transaction is not current for this thread");
        }
        //driver.rollback(tx);
        transaction.remove();
    }

    /**
     * Commits the specified transaction.
     *
     * The actual job of committing the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param tx the transaction to commit
     */
    public void commit(Transaction tx) {
        if (tx != transaction.get()) {
            throw new TransactionException("Transaction is not current for this thread");
        }
        //driver.commit(tx);
        transaction.remove();
    }

    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    public Transaction getCurrentTransaction() {
        return transaction.get();
    }


}
