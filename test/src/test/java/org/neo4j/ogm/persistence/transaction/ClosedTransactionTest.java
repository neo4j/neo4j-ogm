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
