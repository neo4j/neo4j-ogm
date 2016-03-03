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


import org.neo4j.ogm.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author vince
 */
public abstract class AbstractTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    protected final TransactionManager transactionManager;

    /* Neo4j does not support nested transactions, but it does allow a transaction to be declared in the context of
     * another one. These 'placebo' transactions only have an effect if their unit of work is rolled back, in which
     * case the entire transaction will be rolled back. In the OGM they are modelled as extensions to the top-level
     * transaction, and managed via a simple extension counter. Every new transaction increments the counter, every
     * rollback or commit decrements the counter. Rollback/Commit only get executed when the counter = 0, but a
     * rollback of an extension will mark the entire transaction to be rolled back, and an attempt to commit such
     * a transaction will fail.
     */
    protected final AtomicLong extendsCount = new AtomicLong();

    /* Objects which are newly persisted into the graph should be registered on the transaction.
     * In the event that a rollback occurs, these objects will have been assigned an id from the database
     * but will be deleted from the graph, so we can reset their ids to null, in order to allow
     * a subsequent save request to operate correctly */
    private List<Object> registeredNew = new ArrayList<>();

    private Transaction.Status status = Transaction.Status.OPEN;

    public AbstractTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void rollback() {

        long extensions = extendsCount.get();

        // is this the root transaction ?
        if (extensions == 0) {
            // transaction can always be rolled back
            if (transactionManager != null) {
                logger.debug("Rollback invoked");
                transactionManager.rollback(this);
                status = Status.ROLLEDBACK;
            }
        } else { 
            logger.debug("Rollback pending");
            status = Status.ROLLBACK_PENDING;  // a rollback-pending will eventually rollback the entire transaction
        }
    }

    public void commit() {

        long extensions = extendsCount.get();

        if (extensions == 0) {
            if (status == Status.OPEN || status == Status.PENDING || status == Status.COMMIT_PENDING) {
                if (transactionManager != null) {
                    logger.debug("Commit invoked");
                    transactionManager.commit(this);
                    status = Status.COMMITTED;
                }
            } else {
                throw new TransactionException("Transaction cannot commit");
            }
        } else {
            if (status == Status.ROLLBACK_PENDING) {
                throw new TransactionException("Transaction cannot commit: rollback pending");
            }
            else {
                logger.debug("Commit pending");
                status = Status.COMMIT_PENDING;
            }
        }
    }

    /**
     * Extends the current transaction. 
     */
    public void extend() {
        extendsCount.incrementAndGet();
        logger.debug("Transaction extended: {}", extendsCount.get());
    }

    public final Status status() {
        return status;
    }

    public void close()
    {
        long extensions = extendsCount.get();

        if (extensions == 0) {
            logger.debug("Closing transaction");

            if (status == Status.ROLLBACK_PENDING) {
                rollback();
            } else if (status == Status.COMMIT_PENDING) {
                commit();
            }
            else if (status == Status.PENDING || status == Status.OPEN) {
                rollback();
            }
            status = Status.CLOSED;
        }
        extendsCount.getAndDecrement();
    }

    public long extensions() {
        return extendsCount.get();
    }

    public void registerNew( Object persisted ) {
        registeredNew.add( persisted );
    }

    public List<Object> registeredNew() {
        return registeredNew;
    }
}
