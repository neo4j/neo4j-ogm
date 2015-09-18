package org.neo4j.ogm.session.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final GraphDatabaseService graphDb;
    private final org.neo4j.graphdb.Transaction wrappedTransaction;
    private final TransactionManager transactionManager;
    private final boolean autoCommit;

    public EmbeddedTransaction(MappingContext mappingContext, TransactionManager txManager, boolean autoCommit, GraphDatabaseService graphDb) {
        super(mappingContext);
        this.graphDb = graphDb;
        this.autoCommit = autoCommit;
        this.transactionManager = txManager;
        this.wrappedTransaction = graphDb.beginTx();
    }

    @Override
    public boolean autoCommit() {
        return autoCommit;
    }

    @Override
    public String url() {
        return ((GraphDatabaseAPI) graphDb).getStoreDir();
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
        rollback();
    }

}
