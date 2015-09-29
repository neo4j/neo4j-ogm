/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */
package org.neo4j.ogm.session.delegates;

import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.GraphCallback;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.transaction.Transaction;

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

        session.debug("beginTransaction() being called on thread: " + Thread.currentThread().getId());
        session.debug("Neo4jSession identity: " + this);

        //Transaction tx = session.transactionManager().openTransaction(session.context());
        Transaction tx = session.transactionManager().openTransaction();

        session.debug("Obtained new transaction, tx id: " + tx);
        return tx;
    }

    @Override
    public <T> T doInTransaction(GraphCallback<T> graphCallback) {
        return graphCallback.apply(session.requestHandler(), getCurrentOrTransientTransaction(), session.metaData());
    }


    @Override
    public Transaction getTransaction() {
        return session.transactionManager().getCurrentTransaction();
    }

    public Transaction getCurrentOrTransientTransaction() {

        session.debug("--------- new request ----------");
        session.debug("getOrCreateTransaction() being called on thread: " + Thread.currentThread().getId());
        session.debug("Session identity: " + this);

        Transaction tx = session.transactionManager().getCurrentTransaction();

        if (tx == null
                || tx.status().equals(Transaction.Status.CLOSED)
                || tx.status().equals(Transaction.Status.COMMITTED)
                || tx.status().equals(Transaction.Status.ROLLEDBACK)) {

            session.debug("There is no existing transaction, creating a transient one");
            return session.transactionManager().openTransientTransaction(session.context());

        }

        session.debug("Current transaction: , tx id: " + tx);
        return tx;

    }


}
