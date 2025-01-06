/*
 * Copyright (c) 2002-2025 "Neo4j,"
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

import java.util.List;
import java.util.function.Consumer;

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
     * A handler that can be called per entity during commit or rollback operations. It will we passed
     * to the callback of the {@link #close(Transaction, Consumer)} operation. Transactions may
     * choose to call this or not depending on what actually did cause the closing of a transaction.
     */
    @FunctionalInterface
    interface TransactionClosedListener {
        /**
         * Indicate a commit event per entity
         *
         * @param newsStatus         the new status
         * @param entitiesRegistered A list of entities registered in the transaction being closed. A handler might
         *                           choose to clean up their state.
         */
        void onTransactionClosed(Transaction.Status newsStatus, List<Object> entitiesRegistered);
    }

    /**
     * Closes the specified transaction if it belongs to the current thread. After the {@code callback} has successfully
     * been called, the transaction will be detached from this transaction manager and the executing thread.
     * <p>
     * The method must throw an exception if the {@code transaction} does not belong to the current thread.
     * @param transaction The transaction to be closed
     * @param callback A callback to be executed prior to detaching the transaction from the thread.
     */
    void close(Transaction transaction, Consumer<TransactionClosedListener> callback);

    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    Transaction getCurrentTransaction();

    void bookmark(String bookmark);
}
