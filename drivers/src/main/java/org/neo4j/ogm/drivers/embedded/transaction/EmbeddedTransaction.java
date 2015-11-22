package org.neo4j.ogm.drivers.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final org.neo4j.graphdb.Transaction nativeTransaction;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedTransaction.class);

    /**
     * Request a new transaction.
     *
     * Creates a new user transaction for the current thread, and associates it with
     * a native transaction in the underlying database. All commit and rollback operations
     * on the user transaction are delegated to the native transaction.
     *
     * If no native transaction exists for this thread, a new TopLevel transaction will be created.
     * If a native transaction is already open, a "placebo" transaction is returned instead.
     *
     * @param transactionManager an instance of {@link TransactionManager}
     * @param databaseService an in-memory Neo4j database
     */
    public EmbeddedTransaction(TransactionManager transactionManager, GraphDatabaseService databaseService) {
        super(transactionManager);
        this.nativeTransaction = databaseService.beginTx();
        logger.debug("Transaction: {}, native: ", this, nativeTransaction);
    }

    @Override
    public void rollback() {
        nativeTransaction.failure();
        super.rollback();
        nativeTransaction.close();
    }

    @Override
    public void commit() {
        //System.out.println("explicit transaction committed");nativeTransaction.success();
        super.commit();
        nativeTransaction.close();
    }
}
