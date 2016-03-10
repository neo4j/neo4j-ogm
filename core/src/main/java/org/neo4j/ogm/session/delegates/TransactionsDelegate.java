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
package org.neo4j.ogm.session.delegates;

import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.GraphCallback;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class TransactionsDelegate implements Capability.Transactions {

    private final Neo4jSession session;

    public TransactionsDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
    }

    @Override
    public Transaction beginTransaction() {

        session.debug("beginTransaction()");
        session.debug("Neo4jSession identity: " + this);

        Transaction tx = session.transactionManager().openTransaction();

        session.debug("Transaction, tx id: " + tx);
        return tx;
    }

    @Override
    @Deprecated
    public <T> T doInTransaction(GraphCallback<T> graphCallback) {
        return graphCallback.apply(session.requestHandler(), getTransaction(), session.metaData());
    }


    @Override
    public Transaction getTransaction() {
        return session.transactionManager().getCurrentTransaction();
    }


}
