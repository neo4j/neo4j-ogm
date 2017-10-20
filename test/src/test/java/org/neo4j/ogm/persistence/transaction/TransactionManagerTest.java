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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.*;
import org.neo4j.ogm.exception.core.TransactionManagerException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * Transactions in the OGM
 * A managed transaction is one whose lifecycle is managed via a TransactionManager instance
 * Transactions managed by a Transaction Manager will automatically:
 * 1)  maintain single per-thread transaction instances
 * 2)  manage the calls to the appropriate driver to implement the relevant transaction semantics
 * 3)  manage transaction lifecycle and state correctly
 *
 * @author Michal Bachman
 * @author Vince Bickers
 */
public class TransactionManagerTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void setUpClass() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void destroy() {
        session.purgeDatabase();
    }


    @Test
    public void shouldBeAbleToCreateManagedTransaction() {
        DefaultTransactionManager transactionManager = new DefaultTransactionManager(session, sessionFactory.getDriver());
        assertThat(session.getLastBookmark()).isNull();
        try (Transaction tx = transactionManager.openTransaction()) {
            assertThat(tx.status()).isEqualTo(Transaction.Status.OPEN);
        }
    }

    @Test(expected = TransactionManagerException.class)
    @Ignore("What's the rationale of this test ? Actually leaves tx in an inconsistent state")
    public void shouldFailCommitFreeTransactionInManagedContext() {
        DefaultTransactionManager transactionManager = new DefaultTransactionManager(null, sessionFactory.getDriver());
        try (Transaction tx = sessionFactory.getDriver().newTransaction(Transaction.Type.READ_WRITE, null)) {
            transactionManager.commit(tx);
        }
    }

    @Test(expected = TransactionManagerException.class)
    @Ignore("What's the rationale of this test ? Actually leaves tx in an inconsistent state")
    public void shouldFailRollbackFreeTransactionInManagedContext() {
        DefaultTransactionManager transactionManager = new DefaultTransactionManager(null, sessionFactory.getDriver());
        try (Transaction tx = sessionFactory.getDriver().newTransaction(Transaction.Type.READ_WRITE, null)) {
            transactionManager.rollback(tx);
        }
    }

    @Test
    public void shouldRollbackManagedTransaction() {
        DefaultTransactionManager transactionManager = new DefaultTransactionManager(session, sessionFactory.getDriver());
        assertThat(session.getLastBookmark()).isNull();

        try (Transaction tx = transactionManager.openTransaction()) {
            assertThat(tx.status()).isEqualTo(Transaction.Status.OPEN);
            tx.rollback();
            assertThat(tx.status()).isEqualTo(Transaction.Status.ROLLEDBACK);
        }
    }
}
