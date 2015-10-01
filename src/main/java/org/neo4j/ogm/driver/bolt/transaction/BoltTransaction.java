package org.neo4j.ogm.driver.bolt.transaction;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.ogm.driver.api.transaction.TransactionManager;
import org.neo4j.ogm.driver.impl.transaction.AbstractTransaction;

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
