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

package org.neo4j.ogm.drivers.embedded.transaction;

import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final org.neo4j.graphdb.Transaction nativeTransaction;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedTransaction.class);
    private boolean autoCommit;

    /**
     * Request a new transaction.
     *
     * Creates a new user transaction for the current thread, and associates it with
     * a new or existing native transaction in the underlying database. All commit and rollback operations
     * on the user transaction are delegated to the native transaction.
     *
     * @param transactionManager an instance of {@link TransactionManager}
     * @param nativeTransaction the {@link org.neo4j.graphdb.Transaction} backing this Transaction object
     */
    public EmbeddedTransaction(TransactionManager transactionManager, org.neo4j.graphdb.Transaction nativeTransaction) {
        super(transactionManager);
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    public void rollback() {

        try {
            if (transactionManager.canRollback()) {
                logger.debug("rolling back native transaction: {}", nativeTransaction );
                nativeTransaction.failure();
                nativeTransaction.close();
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.rollback();
        }
    }

    @Override
    public void commit() {
        try {
            if (transactionManager.canCommit()) {
                logger.debug("Committing native transaction: {}", nativeTransaction);
                nativeTransaction.success();
                nativeTransaction.close();
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.commit();
        }
    }

    public org.neo4j.graphdb.Transaction getNativeTransaction() {
        return nativeTransaction;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
}
