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

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.neo4j.ogm.transaction.Transaction.Type.READ_ONLY;

public class BoltTransactionTest {

    @Test
    public void commitShouldCloseTransactionAndSession() {
        Transaction nativeTx = openTransactionMock();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(), nativeTx, nativeSession, READ_ONLY);

        boltTx.commit();

        InOrder inOrder = inOrder(nativeSession, nativeTx);
        inOrder.verify(nativeTx).close();
        inOrder.verify(nativeSession).close();
    }

    @Test
    public void commitShouldCloseSessionWhenTransactionIsClosed() {
        Transaction nativeTx = closedTransactionMock();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(), nativeTx, nativeSession, READ_ONLY);

        try {
            boltTx.commit();
        } catch (Exception ignore) {
            // do not care about exceptions, native session should just get closed at the end
        }

        verify(nativeTx, never()).close();
        verify(nativeSession).close();
    }

    @Test
    public void commitShouldCloseSessionWhenTransactionCloseThrows() {
        Transaction nativeTx = openTransactionMock();
        doThrow(new RuntimeException("Close failed")).when(nativeTx).close();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(), nativeTx, nativeSession, READ_ONLY);

        try {
            boltTx.commit();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Close failed", e.getMessage());
        }

        verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseTransactionAndSession() {
        Transaction nativeTx = openTransactionMock();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeTx, nativeSession, READ_ONLY);

        boltTx.rollback();

        InOrder inOrder = inOrder(nativeSession, nativeTx);
        inOrder.verify(nativeTx).close();
        inOrder.verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseSessionWhenTransactionIsClosed() {
        Transaction nativeTx = closedTransactionMock();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeTx, nativeSession, READ_ONLY);

        boltTx.rollback();

        verify(nativeTx, never()).close();
        verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseSessionWhenTransactionCloseThrows() {
        Transaction nativeTx = openTransactionMock();
        doThrow(new RuntimeException("Close failed")).when(nativeTx).close();
        Session nativeSession = openSessionMock();
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeTx, nativeSession, READ_ONLY);

        try {
            boltTx.rollback();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Close failed", e.getMessage());
        }

        verify(nativeSession).close();
    }

    private static Session openSessionMock() {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private static Transaction openTransactionMock() {
        Transaction tx = mock(Transaction.class);
        when(tx.isOpen()).thenReturn(true);
        return tx;
    }

    private static Transaction closedTransactionMock() {
        return mock(Transaction.class);
    }

    private static TransactionManager committingTxManager() {
        TransactionManager txManager = mock(TransactionManager.class);
        when(txManager.canCommit()).thenReturn(true);
        return txManager;
    }

    private static TransactionManager rollingBackTxManager() {
        TransactionManager txManager = mock(TransactionManager.class);
        when(txManager.canRollback()).thenReturn(true);
        return txManager;
    }
}
