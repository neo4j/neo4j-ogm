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
