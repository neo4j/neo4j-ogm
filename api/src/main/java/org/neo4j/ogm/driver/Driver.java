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

package org.neo4j.ogm.driver;

import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver extends AutoCloseable {

    void configure(DriverConfiguration config);

    Transaction newTransaction();

    Transaction newTransaction(Transaction.Type type);

    void close();

    Request request();

    void setTransactionManager(TransactionManager tx);

    DriverConfiguration getConfiguration();
}
