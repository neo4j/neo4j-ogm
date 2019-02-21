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
package org.neo4j.ogm.driver;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public interface Driver extends AutoCloseable {

    void configure(Configuration config);

    /**
     * Begins new transaction
     *
     * @deprecated Since 3.1.8 as it was faulty. Will be removed in 3.2
     * @param type      type of the transaction, see {@link org.neo4j.ogm.transaction.Transaction.Type}
     * @param bookmarks bookmarks to pass to the driver when transaction is started, NOTE: currently supported only
     *                  by bolt driver
     * @return new transaction
     */
    @Deprecated
    default Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return A factory method for creating suppliers of transactions for a given transaction manager
     */
    default Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier() {
        return transactionManager -> (type, bookmarks) -> null;
    }

    void close();

    /**
     * @return A new request handler
     * @deprecated Since 3.1.8 as it was faulty. Will be removed in 3.2
     */
    @Deprecated
    default Request request() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param transaction Current transaction, may be null, depending on the driver
     * @return A new request handler
     */
    Request request(Transaction transaction);

    /**
     * @param tx new transaction manager
     * @deprecated Since 3.1.8 as it was faulty. Will be removed in 3.2
     */
    @Deprecated
    default void setTransactionManager(TransactionManager tx) {
        throw new UnsupportedOperationException();
    }

    Configuration getConfiguration();

    default Function<String, String> getCypherModification() {
        return Function.identity();
    }

    /**
     * Indicates if the driver requires an explicit transaction to run queries.
     * Used to know if transactions has to be explicitly triggered by OGM before accessing DB.
     * @return true if client code has to setup a transaction, false if the driver can auto-commit.
     */
    default boolean requiresTransaction() {
        return true;
    }
}
