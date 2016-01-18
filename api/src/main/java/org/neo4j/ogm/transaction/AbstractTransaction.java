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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author vince
 */
public abstract class AbstractTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    protected final TransactionManager transactionManager;
    protected final AtomicLong extendsCount = new AtomicLong();

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
}
