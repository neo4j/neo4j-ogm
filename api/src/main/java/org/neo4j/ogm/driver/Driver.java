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

package org.neo4j.ogm.driver;

import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public interface Driver extends AutoCloseable {

    void configure(Configuration config);

    /**
     * Begins new transaction
     *
     * @param type      type of the transaction, see {@link org.neo4j.ogm.transaction.Transaction.Type}
     * @param bookmarks bookmarks to pass to the driver when transaction is started, NOTE: currently supported only
     *                  by bolt driver
     * @return new transaction
     */
    Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks);

    void close();

    Request request();

    void setTransactionManager(TransactionManager tx);

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
