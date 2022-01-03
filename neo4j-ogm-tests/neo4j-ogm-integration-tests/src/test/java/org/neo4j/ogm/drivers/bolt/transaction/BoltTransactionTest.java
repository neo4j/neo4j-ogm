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
package org.neo4j.ogm.drivers.bolt.transaction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.neo4j.ogm.transaction.Transaction.Type.*;

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

public class BoltTransactionTest {

    @Test
    public void commitShouldCloseTransactionAndSession() {
        Transaction nativeTx = openTransactionMock();
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(),  nativeSession, READ_ONLY);

        boltTx.commit();

        InOrder inOrder = inOrder(nativeSession, nativeTx);
        inOrder.verify(nativeTx).close();
        inOrder.verify(nativeSession).close();
    }

    @Test
    public void commitShouldCloseSessionWhenTransactionIsClosed() {
        Transaction nativeTx = closedTransactionMock();
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(),  nativeSession, READ_ONLY);

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
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(committingTxManager(), nativeSession, READ_ONLY);

        try {
            boltTx.commit();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Close failed");
        }

        verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseTransactionAndSession() {
        Transaction nativeTx = openTransactionMock();
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeSession, READ_ONLY);

        boltTx.rollback();

        InOrder inOrder = inOrder(nativeSession, nativeTx);
        inOrder.verify(nativeTx).close();
        inOrder.verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseSessionWhenTransactionIsClosed() {
        Transaction nativeTx = closedTransactionMock();
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeSession, READ_ONLY);

        boltTx.rollback();

        verify(nativeTx, never()).close();
        verify(nativeSession).close();
    }

    @Test
    public void rollbackShouldCloseSessionWhenTransactionCloseThrows() {
        Transaction nativeTx = openTransactionMock();
        doThrow(new RuntimeException("Close failed")).when(nativeTx).close();
        Session nativeSession = openSessionMock(nativeTx);
        BoltTransaction boltTx = new BoltTransaction(rollingBackTxManager(), nativeSession, READ_ONLY);

        try {
            boltTx.rollback();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Close failed");
        }

        verify(nativeSession).close();
    }

    private static Session openSessionMock(Transaction nativeTx) {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(session.beginTransaction()).thenReturn(nativeTx);
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
