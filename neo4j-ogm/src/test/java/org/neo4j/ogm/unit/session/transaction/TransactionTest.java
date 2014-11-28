package org.neo4j.ogm.unit.session.transaction;

import org.junit.Test;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.transaction.Transaction;

import static org.junit.Assert.assertEquals;

public class TransactionTest {

    @Test public void createTransaction() {
        Transaction tx = new Transaction(new MappingContext(), "");
        assertEquals(Transaction.OPEN, tx.status());
    }

    @Test public void testCommit() {
        Transaction tx = new Transaction(new MappingContext(), "");
        tx.commit();
        assertEquals(Transaction.OPEN | Transaction.COMMITTED, tx.status());
    }

    @Test public void testRollback() {
        Transaction tx = new Transaction(new MappingContext(), "");
        tx.rollback();
        assertEquals(Transaction.OPEN | Transaction.ROLLEDBACK, tx.status());

    }
}
