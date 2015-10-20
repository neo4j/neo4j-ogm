package org.neo4j.ogm.drivers.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.api.transaction.TransactionManager;
import org.neo4j.ogm.api.transaction.AbstractTransaction;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final org.neo4j.graphdb.Transaction nativeTransaction;

    public EmbeddedTransaction(TransactionManager transactionManager, GraphDatabaseService transport) {
        super(transactionManager);
        this.nativeTransaction = transport.beginTx();
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
