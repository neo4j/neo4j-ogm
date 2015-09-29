package org.neo4j.ogm.driver.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.session.transaction.AbstractTransaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final org.neo4j.graphdb.Transaction nativeTransaction;

    public EmbeddedTransaction(TransactionManager txManager, GraphDatabaseService transport) {
        super(txManager);
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

    @Override
    public void close() {
        commit();
    }

}
