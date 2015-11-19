package org.neo4j.ogm.drivers.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;

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
        //System.out.println("explicit transaction rolled back");
        nativeTransaction.failure();
        super.rollback();
        nativeTransaction.close();
    }

    @Override
    public void commit() {
        //System.out.println("explicit transaction committed");
        nativeTransaction.success();
        super.commit();
        nativeTransaction.close();
    }
}
