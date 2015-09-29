package org.neo4j.ogm.driver.embedded.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.transaction.AbstractTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedTransaction extends AbstractTransaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final GraphDatabaseService graphDb;
    private final org.neo4j.graphdb.Transaction wrappedTransaction;

    public EmbeddedTransaction(MappingContext mappingContext, TransactionManager txManager, GraphDatabaseService graphDb) {
        super(mappingContext, txManager);
        this.graphDb = graphDb;
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
        rollback();
    }

}
