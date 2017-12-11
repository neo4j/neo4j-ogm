/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
 */
public interface Transaction extends AutoCloseable {

    /**
     * rollback a transaction that has pending writes
     */
    void rollback();

    /**
     * commit a transaction that has pending writes
     */
    void commit();

    /**
     * If this transaction can be committed
     *
     * @return true if this transaction can be committed
     */
    boolean canCommit();

    /**
     * return the status of the current transaction
     *
     * @return the Status value associated with the current transaction
     */
    Status status();

    /**
     * Obtains the read-only status of a transaction.
     * Transaction are read-write by default
     *
     * @return true if this is a read-only transaction, false otherwise
     */
    boolean isReadOnly();

    /**
     * Returns type of the transaction - READ_ONLY / READ_WRITE
     * The value corresponds to type returned by {@link #isReadOnly()}
     *
     * @return type of the transaction
     */
    Type type();

    enum Status {
        OPEN, PENDING, ROLLEDBACK, COMMITTED, CLOSED, ROLLBACK_PENDING, COMMIT_PENDING
    }

    enum Type {
        READ_ONLY, READ_WRITE
    }

    /**
     * close this transaction.
     */
    void close();
}
