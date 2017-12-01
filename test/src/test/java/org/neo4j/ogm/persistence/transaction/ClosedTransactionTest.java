/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.persistence.transaction;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;

/**
 * This test class defines the behaviour of a transaction which is open
 * on the client, but closed on the server (for whatever reason), under
 * the different scenarios of commit, rollback and close.
 *
 * @author vince
 */
public class ClosedTransactionTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;
    private DefaultTransactionManager transactionManager;

    private Transaction tx;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.tree");
    }

    @Before
    public void init() {
        transactionManager = new DefaultTransactionManager(session, sessionFactory.getDriver());
        session = sessionFactory.openSession();
    }

    @Before
    public void createTransactionAndCloseOnServerButNotOnClient() {
        tx = transactionManager.openTransaction();
        tx.close();
        transactionManager.reinstate((AbstractTransaction) tx);
    }

    @After
    public void clearTransactionManager() {
        transactionManager.clear();
    }

    @Test
    public void shouldNotThrowExceptionWhenRollingBackAClosedTransaction() {
        tx.rollback();
    }

    @Test
    public void shouldNotThrowExceptionWhenClosingAClosedTransaction() {
        tx.close();
    }

    @Test(expected = TransactionException.class)
    public void shouldThrowExceptionWhenCommittingAClosedTransaction() {
        tx.commit();
    }
}
