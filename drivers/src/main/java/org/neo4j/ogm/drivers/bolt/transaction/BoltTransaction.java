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

package org.neo4j.ogm.drivers.bolt.transaction;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author vince
 */
public class BoltTransaction extends AbstractTransaction {

    private final Transaction nativeTransaction;

    public BoltTransaction(TransactionManager transactionManager, Session transport) {
        super(transactionManager);
        this.nativeTransaction = transport.newTransaction();
    }

    @Override
    public void rollback() {
        nativeTransaction.failure();
        super.rollback();
        nativeTransaction.close();
    }

    @Override
    public void commit() {
        nativeTransaction.success();
        super.commit();
        nativeTransaction.close();
    }
}
