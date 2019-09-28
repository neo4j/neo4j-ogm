/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
 * @author Mark Angrish
 */
public interface TransactionManager {

    /**
     * Opens a new READ_WRITE transaction against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @return a new @{link Transaction}
     */
    Transaction openTransaction();

    /**
     * Opens a new transaction of the specified type against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @param type      type of the transaction
     * @param bookmarks bookmarks to be passed to driver
     * @return a new @{link Transaction}
     */
    Transaction openTransaction(Transaction.Type type, Iterable<String> bookmarks);

    /**
     * Rolls back the specified transaction.
     * The actual job of rolling back the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * <strong>Warning</strong>: This method is meant to be called from actual transactions only!!!
     *
     * @param transaction the transaction to rollback
     */
    void rollback(Transaction transaction);

    /**
     * Commits the specified transaction.
     * The actual job of committing the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * <strong>Warning</strong>: This method is meant to be called from actual transactions only!!!
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
