/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.ogm.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final AtomicLong extendsCount = new AtomicLong();

    /* Objects which are newly persisted into the graph should be registered on the transaction.
     * In the event that a rollback occurs, these objects will have been assigned an id from the database
     * but will be deleted from the graph, so we can reset their ids to null, in order to allow
     * a subsequent save request to operate correctly */
    private final List<Object> registeredNew = new ArrayList<>();

    private Transaction.Status status = Transaction.Status.OPEN;
    protected Transaction.Type type = Type.READ_WRITE;

    protected AbstractTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void rollback() {

        long extensions = extendsCount.get();
        logger.debug("Thread {}: Rollback transaction extent: {}", Thread.currentThread().getId(), extensions);

        // is this the root transaction ?
        if (extensions == 0) {
            // transaction can always be rolled back
            if (transactionManager != null) {
                transactionManager.rollback(this);
                status = Status.ROLLEDBACK;
                logger.debug("Thread {}: Rolled back", Thread.currentThread().getId());
            }
        } else {
            logger.debug("Thread {}: Rollback deferred", Thread.currentThread().getId());
            status = Status.ROLLBACK_PENDING;  // a rollback-pending will eventually rollback the entire transaction
        }
    }

    public void commit() {

        long extensions = extendsCount.get();
        logger.debug("Thread {}: Commit transaction extent: {}", Thread.currentThread().getId(), extensions);

        if (extensions == 0) {
            if (canCommit()) {
                if (transactionManager != null) {
                    transactionManager.commit(this);
                    status = Status.COMMITTED;
                    logger.debug("Thread {}: Committed", Thread.currentThread().getId());
                }
            } else {
                throw new TransactionException("Transaction cannot commit");
            }
        } else {
            if (status == Status.ROLLBACK_PENDING) {
                throw new TransactionException("Transaction cannot commit: rollback pending");
            } else {
                logger.debug("Thread {}: Commit deferred", Thread.currentThread().getId());
                status = Status.COMMIT_PENDING;
            }
        }
    }

    @Override
    public boolean canCommit() {
        return status == Status.OPEN || status == Status.PENDING || status == Status.COMMIT_PENDING;
    }

    /**
     * Extends the current transaction.
     *
     * @param otherType type of the other transaction
     */
    public void extend(Type otherType) {
        if (this.type == otherType) {
            long extensions = extendsCount.incrementAndGet();
            logger.debug("Thread {}: Transaction extended: {}", Thread.currentThread().getId(), extensions);
        } else {
            throw new TransactionException("Incompatible transaction type specified: must be '" + this.type + "'");
        }
    }

    public final Status status() {
        return status;
    }

    public boolean isReadOnly() {
        return type == Type.READ_ONLY;
    }

    @Override
    public Type type() {
        return type;
    }

    public void close() {
        long extensions = extendsCount.get();
        logger.debug("Thread {}: Close transaction extent: {}", Thread.currentThread().getId(), extensions);

        if (extensions == 0) {
            logger.debug("Thread {}: Closing transaction", Thread.currentThread().getId());

            if (status == Status.ROLLBACK_PENDING) {
                rollback();
            } else if (status == Status.COMMIT_PENDING) {
                commit();
            } else if (status == Status.PENDING || status == Status.OPEN) {
                rollback();
            }
            status = Status.CLOSED;
        } else {
            extendsCount.getAndDecrement();
            logger.debug("Thread {}: Close deferred", Thread.currentThread().getId());
        }
    }

    public long extensions() {
        return extendsCount.get();
    }

    public void registerNew(Object persisted) {
        registeredNew.add(persisted);
    }

    public List<Object> registeredNew() {
        return registeredNew;
    }
}
