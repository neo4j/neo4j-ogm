package org.neo4j.ogm.driver.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.session.transaction.AbstractTransaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final org.neo4j.graphdb.Transaction wrappedTransaction;

    public EmbeddedTransaction(TransactionManager txManager, GraphDatabaseService graphDb) {
        super(txManager);
        this.wrappedTransaction = graphDb.beginTx();
    }

    @Override
    public void rollback() {

        wrappedTransaction.failure();
        super.rollback();
        wrappedTransaction.close();
    }

    @Override
    public void commit() {

        wrappedTransaction.success();
        super.commit();
        wrappedTransaction.close();
    }

    @Override
    public void close() {
        commit();
    }

}
