/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * This test class defines the behaviour of a transaction which is open
 * on the client, but closed on the server (for whatever reason), under
 * the different scenarios of commit, rollback and close.
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class ClosedTransactionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private DefaultTransactionManager transactionManager;

    private Transaction tx;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.tree");
    }

    @Before
    public void init() {
        Session session = sessionFactory.openSession();
        // The session actually has it's own transaction manager, which is btw tied to thread locally to the driver.
        // We could force get the sessions transaction manager or just create a new one here and tie it to the driver.
        // Both feel broken, this here a little less painfull, though.
        transactionManager = new DefaultTransactionManager(session, getDriver().getTransactionFactorySupplier());
    }

    @Before
    public void createTransactionAndCloseOnServerButNotOnClient() {
        tx = transactionManager.openTransaction();
        tx.close();

        reOpen(transactionManager, tx);
    }

    @After
    public void clearTransactionManager() {
        getTransactionThreadLocal(transactionManager).remove();
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

    private static void reOpen(TransactionManager transactionManager, Transaction transaction) {

        try {
            Field statusField = AbstractTransaction.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(transaction, Transaction.Status.OPEN);

            getTransactionThreadLocal(transactionManager).set(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ThreadLocal<Transaction> getTransactionThreadLocal(TransactionManager transactionManager) {
        try {
            Field transactionField = DefaultTransactionManager.class.getDeclaredField("currentThreadLocalTransaction");
            transactionField.setAccessible(true);
            return (ThreadLocal<Transaction>) transactionField.get(transactionManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
