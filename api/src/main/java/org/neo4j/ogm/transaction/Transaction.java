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
     * return the status of the current transaction
     * @return the Status value associated with the current transaction
     */
    Status status();

    /**
     * Obtains the read-only status of a transaction.
     * Transaction are read-write by default
     * @return true if this is a read-only transaction, false otherwise
     */
    boolean isReadOnly();

//    /**
//     * Register a new object on the transaction. A new object is
//     * one that was not previously stored in the graph.
//     *
//     * @param persisted a reference to the newly created object
//     */
//    void registerNew( Object persisted );
//
//    /**
//     * Returns the list of all objects that have been created in the
//     * graph during the lifetime of this transaction.
//     *
//     * @return a list of newly persisted objects.
//     */
//    Iterable<? extends Object> registeredNew();

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
